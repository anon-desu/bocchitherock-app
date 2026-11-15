package com.example.data

import com.example.data.local.SessionPreferences
import com.example.data.local.StoredSession
import com.example.data.remote.ApiMessageResponse
import com.example.data.remote.CaptchaChallengeResponse
import com.example.data.remote.CloudflareApiService
import com.example.data.remote.LikeRequest
import com.example.data.remote.LoginRequest
import com.example.data.remote.LogoutRequest
import com.example.data.remote.RefreshRequest
import com.example.data.remote.RegisterRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonArray
import retrofit2.Response

class AuthRepository(
    private val api: CloudflareApiService,
    private val sessionPreferences: SessionPreferences
) {
    val currentUsername: Flow<String?> = sessionPreferences.sessionFlow.map { it?.username }

    private val _likedSongs = MutableStateFlow<Set<String>>(emptySet())
    val likedSongs: StateFlow<Set<String>> = _likedSongs.asStateFlow()

    private val refreshMutex = Mutex()

    suspend fun fetchCaptcha(): Result<CaptchaChallengeResponse> {
        val candidateBases = listOf(
            CloudflareApiService.BASE_URL.trimEnd('/'),
            CloudflareApiService.CAPTCHA_URL.trimEnd('/'),
            CloudflareApiService.CAPTCHA_API_FALLBACK_HOST.trimEnd('/')
        ).distinct()

        var lastError: Throwable? = null
        for (base in candidateBases) {
            val attempt = runCatching {
                val response = api.fetchCaptcha("$base/api/captcha")
                if (!response.isSuccessful) {
                    error("验证码获取失败 (${response.code()})")
                }
                val body = response.body() ?: error("验证码数据为空")
                val resolvedImages = body.images.map { raw ->
                    val absolute = when {
                        raw.startsWith("https://", ignoreCase = true) -> raw
                        raw.startsWith("http://", ignoreCase = true) -> "https://" + raw.removePrefix("http://")
                        raw.startsWith("//") -> "https:$raw"
                        raw.startsWith("/") -> "$base$raw"
                        else -> "$base/$raw"
                    }
                    absolute
                }
                body.copy(images = resolvedImages)
            }
            if (attempt.isSuccess) {
                return attempt
            }
            lastError = attempt.exceptionOrNull()
        }
        return Result.failure(lastError ?: IllegalStateException("验证码加载失败，请刷新重试"))
    }

    suspend fun register(
        username: String,
        password: String,
        captchaId: String,
        selectedIndexes: JsonArray
    ): Result<Unit> {
        return runCatching {
            val response = api.register(
                RegisterRequest(
                    username = username.trim(),
                    password = password,
                    captchaId = captchaId,
                    selectedIndexes = selectedIndexes
                )
            )
            if (!response.isSuccessful) {
                val rawError = parseError(response.errorBody()?.string()) ?: "注册失败"
                val friendly = when {
                    rawError.contains("Username exists", ignoreCase = true) -> "该用户名已被占用，请换一个试试"
                    rawError.contains("Too fast", ignoreCase = true) -> "操作太快了，请稍等片刻再试"
                    rawError.contains("Captcha", ignoreCase = true) -> "验证码已过期，请重新验证"
                    else -> rawError
                }
                error(friendly)
            }
        }
    }

    suspend fun login(username: String, password: String): Result<String> {
        return runCatching {
            val response = api.login(LoginRequest(username.trim(), password))
            if (response.code() == 429) {
                error("操作太快了，歇会儿吧！")
            }
            if (!response.isSuccessful) {
                val rawError = parseError(response.errorBody()?.string()) ?: "登录失败"
                val friendly = if (rawError == "Auth failed") "账号或密码错误" else rawError
                error(friendly)
            }
            val body = response.body() ?: error("服务器返回数据为空")
            val accessToken = body.accessToken ?: error("登录响应缺少访问令牌")
            val resolvedUsername = body.user?.username ?: username.trim()
            sessionPreferences.saveSession(
                username = resolvedUsername,
                accessToken = accessToken,
                refreshToken = body.refreshToken
            )
            refreshLikes()
            resolvedUsername
        }
    }

    suspend fun refreshLikes() {
        val session = sessionPreferences.sessionFlow.first()
        if (session == null) {
            _likedSongs.value = emptySet()
            return
        }
        runCatching {
            val response = executeWithTokenRefresh { token ->
                api.getLikes("Bearer $token")
            }
            if (response != null && response.isSuccessful) {
                _likedSongs.value = response.body()?.toSet() ?: emptySet()
            }
        }
    }

    suspend fun toggleLike(songName: String): Result<Boolean> {
        val session = sessionPreferences.sessionFlow.first()
            ?: return Result.failure(IllegalStateException("请先登录才能收藏歌曲哦！"))

        val previous = _likedSongs.value
        val nextLiked = songName !in previous
        _likedSongs.value = if (nextLiked) previous + songName else previous - songName

        return try {
            val response = executeWithTokenRefresh(session) { token ->
                api.toggleLike("Bearer $token", LikeRequest(songName))
            }
            if (response != null && response.isSuccessful) {
                Result.success(nextLiked)
            } else {
                _likedSongs.value = previous
                Result.failure(IllegalStateException("同步收藏状态失败，请稍后重试"))
            }
        } catch (e: Exception) {
            _likedSongs.value = previous
            Result.failure(e)
        }
    }

    suspend fun logout() {
        val session = sessionPreferences.sessionFlow.first()
        try {
            val refreshToken = session?.refreshToken
            if (!refreshToken.isNullOrBlank()) {
                val authHeader = session.accessToken.let { "Bearer $it" }
                api.logout(authHeader, LogoutRequest(refreshToken))
            }
        } catch (_: Exception) {
            // Force local logout even if network call fails
        } finally {
            sessionPreferences.clearSession()
            _likedSongs.value = emptySet()
        }
    }

    private suspend fun <T> executeWithTokenRefresh(
        initialSession: StoredSession? = null,
        block: suspend (String) -> Response<T>
    ): Response<T>? {
        val session = initialSession ?: sessionPreferences.sessionFlow.first() ?: return null
        val firstResponse = block(session.accessToken)
        if (firstResponse.code() != 401) {
            return firstResponse
        }

        val newAccessToken = refreshAccessToken() ?: return firstResponse
        return block(newAccessToken)
    }

    private suspend fun refreshAccessToken(): String? {
        return refreshMutex.withLock {
            val latestSession = sessionPreferences.sessionFlow.first() ?: return@withLock null
            val refreshToken = latestSession.refreshToken
            if (refreshToken.isNullOrBlank()) {
                sessionPreferences.clearSession()
                _likedSongs.value = emptySet()
                return@withLock null
            }

            try {
                val response = api.refresh(RefreshRequest(refreshToken))
                if (!response.isSuccessful) {
                    sessionPreferences.clearSession()
                    _likedSongs.value = emptySet()
                    return@withLock null
                }
                val newAccess = response.body()?.accessToken
                if (newAccess.isNullOrBlank()) {
                    sessionPreferences.clearSession()
                    _likedSongs.value = emptySet()
                    return@withLock null
                }
                sessionPreferences.updateAccessToken(newAccess)
                newAccess
            } catch (_: Exception) {
                sessionPreferences.clearSession()
                _likedSongs.value = emptySet()
                null
            }
        }
    }

    private fun parseError(rawBody: String?): String? {
        if (rawBody.isNullOrBlank()) return null
        return runCatching {
            CloudflareApiService.json.decodeFromString<ApiMessageResponse>(rawBody).error
        }.getOrNull()
    }
}
