package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.BandCatalog
import com.example.ui.components.AssetImage
import com.example.ui.theme.BocchiPink
import com.example.ui.theme.BrushScriptFontFamily
import com.example.ui.theme.NoteScriptFontFamily
import com.example.viewmodel.LoginUiState
import com.example.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    factory: LoginViewModel.Factory,
    onNavigateBack: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var closeButtonReady by remember { mutableStateOf(false) }
    var hasNavigatedBack by remember { mutableStateOf(false) }

    val safeNavigateBack: () -> Unit = {
        if (!hasNavigatedBack) {
            hasNavigatedBack = true
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        delay(500L)
        closeButtonReady = true
    }

    LaunchedEffect(state.loginSucceeded) {
        if (state.loginSucceeded) {
            safeNavigateBack()
        }
    }

    val currentBg = BandCatalog.loginBackgrounds[
        state.backgroundIndex.coerceIn(BandCatalog.loginBackgrounds.indices)
    ]

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        val isWide = maxWidth >= 740.dp

        AnimatedContent(
            targetState = currentBg,
            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
            label = "loginBg"
        ) { bgPath ->
            AssetImage(
                assetPath = bgPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0x33FF8282),
                            Color(0x22FFD470),
                            Color(0x55141414)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = if (isWide) 780.dp else 440.dp)
                    .fillMaxWidth(if (isWide) 0.84f else 0.92f)
            ) {
                if (isWide) {
                    WideLoginCard(
                        state = state,
                        currentBg = currentBg,
                        onUsernameChange = viewModel::onUsernameChanged,
                        onPasswordChange = viewModel::onPasswordChanged,
                        onRePasswordChange = viewModel::onRePasswordChanged,
                        onToggleMode = viewModel::toggleMode,
                        onSubmit = viewModel::submit
                    )
                } else {
                    CompactLoginCard(
                        state = state,
                        onUsernameChange = viewModel::onUsernameChanged,
                        onPasswordChange = viewModel::onPasswordChanged,
                        onRePasswordChange = viewModel::onRePasswordChanged,
                        onToggleMode = viewModel::toggleMode,
                        onSubmit = viewModel::submit
                    )
                }
            }

            IconButton(
                onClick = viewModel::previousBackground,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.25f))
                    .testTag("login_bg_prev")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = "上一张背景",
                    tint = Color.White.copy(alpha = 0.78f)
                )
            }

            IconButton(
                onClick = viewModel::nextBackground,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.25f))
                    .testTag("login_bg_next")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "下一张背景",
                    tint = Color.White.copy(alpha = 0.78f)
                )
            }

            IconButton(
                onClick = { if (closeButtonReady) safeNavigateBack() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .testTag("close_login_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "返回主页",
                    tint = Color(0xFFFF66B2)
                )
            }
        }
    }

    if (state.showCaptchaDialog) {
        BangCaptchaDialog(
            challenge = state.captchaChallenge,
            selectedIndexes = state.selectedCaptchaIndexes,
            isLoading = state.isCaptchaLoading,
            errorMessage = state.captchaError,
            isConfirmed = state.isCaptchaConfirmed,
            onToggleIndex = viewModel::toggleCaptchaIndex,
            onRefresh = viewModel::loadCaptchaChallenge,
            onConfirm = viewModel::confirmNativeCaptcha,
            onWebCaptchaResult = viewModel::onCaptchaPayloadReceived,
            onDismiss = viewModel::dismissCaptcha
        )
    }
}

@Composable
private fun WideLoginCard(
    state: LoginUiState,
    currentBg: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRePasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit
) {
    val target1 = BandCatalog.loginTargetImages[state.targetPairIndex]
    val target2 = BandCatalog.loginTargetImages[state.targetPairIndex + 1]

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(470.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.34f)
                    .fillMaxHeight()
                    .background(Color(0xFFFFECD7))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!state.isRegisterMode) {
                        AssetImage(
                            assetPath = currentBg,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        AssetImage(
                            assetPath = target2,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .weight(0.8f),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AssetImage(
                            assetPath = target1,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .weight(0.8f),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        AssetImage(
                            assetPath = currentBg,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            LoginFormContent(
                state = state,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onRePasswordChange = onRePasswordChange,
                onToggleMode = onToggleMode,
                onSubmit = onSubmit,
                modifier = Modifier
                    .weight(0.66f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun CompactLoginCard(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRePasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        LoginFormContent(
            state = state,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onRePasswordChange = onRePasswordChange,
            onToggleMode = onToggleMode,
            onSubmit = onSubmit,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LoginFormContent(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRePasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val guitarTilt by animateFloatAsState(
        targetValue = if (state.isRegisterMode) 18f else -12f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "guitarTilt"
    )

    Box(
        modifier = modifier
            .drawBehind {
                val stroke = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.25f),
                    topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                    size = Size(
                        size.width - 20.dp.toPx(),
                        size.height - 20.dp.toPx()
                    ),
                    style = stroke
                )
            }
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = state.isRegisterMode,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "loginHeading"
            ) { isReg ->
                Text(
                    text = if (isReg) stringResource(R.string.register_title) else stringResource(R.string.login_title),
                    fontFamily = BrushScriptFontFamily,
                    fontSize = 38.sp,
                    color = Color(0xFF1A1A1E)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            UnderlinedFormField(
                label = stringResource(R.string.username_label),
                value = state.username,
                onValueChange = onUsernameChange,
                isPassword = false,
                testTag = "username_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            UnderlinedFormField(
                label = stringResource(R.string.password_label),
                value = state.password,
                onValueChange = onPasswordChange,
                isPassword = true,
                testTag = "password_input"
            )

            AnimatedVisibility(visible = state.isRegisterMode) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    UnderlinedFormField(
                        label = stringResource(R.string.repassword_label),
                        value = state.rePassword,
                        onValueChange = onRePasswordChange,
                        isPassword = true,
                        testTag = "repassword_input"
                    )
                }
            }

            state.message?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = msg,
                    color = if (msg.contains("成功")) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            val buttonLabel = when {
                state.isLoading -> "Processing..."
                !state.isRegisterMode -> stringResource(R.string.login_submit)
                state.isCaptchaVerified -> stringResource(R.string.register_verified_submit)
                else -> stringResource(R.string.register_submit)
            }

            Button(
                onClick = onSubmit,
                enabled = !state.isLoading,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isRegisterMode && state.isCaptchaVerified) {
                        BocchiPink
                    } else {
                        Color(0xFFFF3AA0).copy(alpha = 0.85f)
                    },
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .widthIn(min = 160.dp)
                    .testTag("auth_submit_button")
            ) {
                Text(
                    text = buttonLabel,
                    fontFamily = if (state.isRegisterMode && state.isCaptchaVerified) null else NoteScriptFontFamily,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val switchAnnotated = buildAnnotatedString {
                if (!state.isRegisterMode) {
                    append("还没有账户？")
                    withStyle(SpanStyle(color = BocchiPink, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                        append("立即注册")
                    }
                } else {
                    append("已有账户？")
                    withStyle(SpanStyle(color = BocchiPink, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                        append("立即登录")
                    }
                }
            }

            Text(
                text = switchAnnotated,
                color = Color(0xFF666666),
                fontSize = 13.sp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { onToggleMode() }
                    .padding(8.dp)
                    .testTag("switch_auth_mode_text")
            )
        }

        // Placed after the scrollable Column with higher zIndex so it is always clickable
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(10f)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x14EC407A))
                .clickable { onToggleMode() }
                .padding(6.dp)
                .testTag("guitar_switch_mode_button"),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_electric_guitar),
                contentDescription = "点击吉他切换登录或注册",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = guitarTilt },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun UnderlinedFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = NoteScriptFontFamily,
            fontSize = 18.sp,
            color = Color(0xFF2B2B30)
        )
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color(0xFF141418),
                fontSize = 16.sp
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag(testTag)
        )
        HorizontalDivider(color = Color.Black.copy(alpha = 0.75f), thickness = 1.dp)
    }
}
