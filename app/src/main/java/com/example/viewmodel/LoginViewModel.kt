package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.BandCatalog
import com.example.data.remote.CaptchaChallengeResponse
import com.example.data.remote.CloudflareApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class VerifiedCaptchaData(
    val captchaId: String,
    val selectedIndexes: JsonArray
)

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val rePassword: String = "",
    val isRegisterMode: Boolean = false,
    val backgroundIndex: Int = 0,
    val targetPairIndex: Int = 0,
    val showCaptchaDialog: Boolean = false,
    val captchaChallenge: CaptchaChallengeResponse? = null,
    val selectedCaptchaIndexes: Set<Int> = emptySet(),
    val isCaptchaLoading: Boolean = false,
    val captchaError: String? = null,
    val isCaptchaConfirmed: Boolean = false,
    val verifiedCaptcha: VerifiedCaptchaData? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val loginSucceeded: Boolean = false
) {
    val isCaptchaVerified: Boolean
        get() = verifiedCaptcha != null
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(value: String) {
        if (value.length <= 20) {
            _uiState.update { it.copy(username = value, message = null) }
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, message = null) }
    }

    fun onRePasswordChanged(value: String) {
        _uiState.update { it.copy(rePassword = value, message = null) }
    }

    fun toggleMode() {
        _uiState.update { state ->
            val nextPair = (state.targetPairIndex + 2) % (BandCatalog.loginTargetImages.size - 1)
            state.copy(
                isRegisterMode = !state.isRegisterMode,
                targetPairIndex = nextPair,
                verifiedCaptcha = null,
                captchaChallenge = null,
                selectedCaptchaIndexes = emptySet(),
                isCaptchaConfirmed = false,
                captchaError = null,
                message = null
            )
        }
    }

    fun nextBackground() {
        _uiState.update { state ->
            state.copy(
                backgroundIndex = (state.backgroundIndex + 1) % BandCatalog.loginBackgrounds.size
            )
        }
    }

    fun previousBackground() {
        _uiState.update { state ->
            val size = BandCatalog.loginBackgrounds.size
            state.copy(
                backgroundIndex = (state.backgroundIndex - 1 + size) % size
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(message = "用户名和密码不能为空！") }
            return
        }
        if (state.username.length > 20) {
            _uiState.update { it.copy(message = "用户名长度不能超过 20 个字符！") }
            return
        }

        if (state.isRegisterMode) {
            if (state.password != state.rePassword) {
                _uiState.update { it.copy(message = "两次输入的密码不一致！") }
                return
            }
            val captcha = state.verifiedCaptcha
            if (captcha == null) {
                openAndLoadCaptcha()
            } else {
                performRegister(captcha)
            }
        } else {
            performLogin()
        }
    }

    fun openAndLoadCaptcha() {
        _uiState.update {
            it.copy(
                showCaptchaDialog = true,
                selectedCaptchaIndexes = emptySet(),
                isCaptchaConfirmed = false,
                captchaError = null
            )
        }
        loadCaptchaChallenge()
    }

    fun loadCaptchaChallenge() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCaptchaLoading = true,
                    captchaError = null,
                    isCaptchaConfirmed = false,
                    selectedCaptchaIndexes = emptySet()
                )
            }
            val result = authRepository.fetchCaptcha()
            result.fold(
                onSuccess = { challenge ->
                    _uiState.update {
                        it.copy(
                            isCaptchaLoading = false,
                            captchaChallenge = challenge,
                            captchaError = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isCaptchaLoading = false,
                            captchaError = err.message ?: "图片加载失败，请刷新重试"
                        )
                    }
                }
            )
        }
    }

    fun toggleCaptchaIndex(index: Int) {
        val state = _uiState.value
        if (state.isCaptchaLoading || state.isCaptchaConfirmed) return
        _uiState.update { current ->
            val updated = if (index in current.selectedCaptchaIndexes) {
                current.selectedCaptchaIndexes - index
            } else {
                current.selectedCaptchaIndexes + index
            }
            current.copy(selectedCaptchaIndexes = updated)
        }
    }

    fun confirmNativeCaptcha() {
        val state = _uiState.value
        val challenge = state.captchaChallenge ?: return
        if (state.isCaptchaLoading || state.selectedCaptchaIndexes.isEmpty()) return

        val sortedIndexes = JsonArray(
            state.selectedCaptchaIndexes.sorted().map { JsonPrimitive(it) }
        )
        val verified = VerifiedCaptchaData(
            captchaId = challenge.id,
            selectedIndexes = sortedIndexes
        )
        _uiState.update {
            it.copy(
                isCaptchaConfirmed = true,
                showCaptchaDialog = false,
                verifiedCaptcha = verified,
                message = null
            )
        }
        performRegister(verified)
    }

    fun onCaptchaPayloadReceived(payloadJson: String) {
        val parsed = runCatching {
            CloudflareApiService.json.decodeFromString<VerifiedCaptchaData>(payloadJson)
        }.getOrNull()

        if (parsed == null || parsed.captchaId.isBlank()) {
            _uiState.update {
                it.copy(
                    showCaptchaDialog = false,
                    verifiedCaptcha = null,
                    message = "验证数据异常，请重试"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                showCaptchaDialog = false,
                verifiedCaptcha = parsed,
                message = null
            )
        }
        performRegister(parsed)
    }

    fun dismissCaptcha() {
        _uiState.update {
            it.copy(
                showCaptchaDialog = false,
                verifiedCaptcha = null,
                isCaptchaConfirmed = false
            )
        }
    }

    private fun performRegister(captcha: VerifiedCaptchaData) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = authRepository.register(
                username = state.username,
                password = state.password,
                captchaId = captcha.captchaId,
                selectedIndexes = captcha.selectedIndexes
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRegisterMode = false,
                            password = "",
                            rePassword = "",
                            verifiedCaptcha = null,
                            captchaChallenge = null,
                            selectedCaptchaIndexes = emptySet(),
                            isCaptchaConfirmed = false,
                            message = "注册成功！请登录。"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            verifiedCaptcha = null,
                            captchaChallenge = null,
                            selectedCaptchaIndexes = emptySet(),
                            isCaptchaConfirmed = false,
                            message = (err.message ?: "注册失败") + "\n(请重新进行验证)"
                        )
                    }
                }
            )
        }
    }

    private fun performLogin() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = authRepository.login(state.username, state.password)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginSucceeded = true
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = err.message ?: "登录失败"
                        )
                    }
                }
            )
        }
    }

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository) as T
        }
    }
}
