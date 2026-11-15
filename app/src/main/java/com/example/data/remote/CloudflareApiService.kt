package com.example.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val captchaId: String,
    val selectedIndexes: JsonArray
)

@Serializable
data class CaptchaChallengeResponse(
    val id: String,
    val targetName: String,
    val images: List<String>
)

@Serializable
data class UserDto(
    val id: JsonElement? = null,
    val username: String
)

@Serializable
data class LoginResponse(
    val user: UserDto? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val error: String? = null
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class RefreshResponse(
    val accessToken: String? = null,
    val error: String? = null
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class LikeRequest(
    val songName: String
)

@Serializable
data class ApiMessageResponse(
    val error: String? = null,
    val message: String? = null
)

interface CloudflareApiService {

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiMessageResponse>

    @GET
    suspend fun fetchCaptcha(@Url fullUrl: String): Response<CaptchaChallengeResponse>

    @POST("api/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<RefreshResponse>

    @POST("api/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String?,
        @Body body: LogoutRequest
    ): Response<ApiMessageResponse>

    @GET("api/likes")
    suspend fun getLikes(
        @Header("Authorization") authorization: String
    ): Response<List<String>>

    @POST("api/like")
    suspend fun toggleLike(
        @Header("Authorization") authorization: String,
        @Body body: LikeRequest
    ): Response<ApiMessageResponse>

    companion object {
        const val BASE_URL = "https://band.bocchi.us.kg/"
        const val CAPTCHA_URL = "https://bangcaptcha.bocchi.us.kg"
        const val CAPTCHA_API_FALLBACK_HOST = "https://band.53113007.xyz"

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }

        fun create(): CloudflareApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(CloudflareApiService::class.java)
        }
    }
}
