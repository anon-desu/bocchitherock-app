package com.example.ui.screens

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.example.data.remote.CaptchaChallengeResponse
import com.example.data.remote.CloudflareApiService
import com.example.ui.theme.BocchiPink

private class CaptchaJsBridge(
    private val onResult: (String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onCaptchaResult(payloadJson: String) {
        mainHandler.post {
            onResult(payloadJson)
        }
    }
}

@Composable
fun BangCaptchaDialog(
    challenge: CaptchaChallengeResponse?,
    selectedIndexes: Set<Int>,
    isLoading: Boolean,
    errorMessage: String?,
    isConfirmed: Boolean,
    onToggleIndex: (Int) -> Unit,
    onRefresh: () -> Unit,
    onConfirm: () -> Unit,
    onWebCaptchaResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var useWebFallback by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 380.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = BocchiPink
                        )
                        Text(
                            text = "安全验证",
                            color = Color(0xFF1E293B),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            onClick = { useWebFallback = !useWebFallback },
                            modifier = Modifier.testTag("toggle_web_captcha_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (useWebFallback) "原生验证" else "网页验证",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.05f))
                                .testTag("close_captcha_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "关闭验证",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))

                if (useWebFallback) {
                    BangCaptchaWebViewContent(
                        onCaptchaResult = onWebCaptchaResult,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(460.dp)
                    )
                } else {
                    NativeCaptchaContent(
                        challenge = challenge,
                        selectedIndexes = selectedIndexes,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        isConfirmed = isConfirmed,
                        onToggleIndex = onToggleIndex,
                        onRefresh = onRefresh,
                        onConfirm = onConfirm
                    )
                }
            }
        }
    }
}

@Composable
private fun NativeCaptchaContent(
    challenge: CaptchaChallengeResponse?,
    selectedIndexes: Set<Int>,
    isLoading: Boolean,
    errorMessage: String?,
    isConfirmed: Boolean,
    onToggleIndex: (Int) -> Unit,
    onRefresh: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        when {
            isLoading && challenge == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(color = Color(0xFFE91E63))
                    Text(
                        text = "正在召唤成员...",
                        color = Color(0xFF888888),
                        fontSize = 14.sp
                    )
                }
            }

            challenge == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "加载失败",
                        color = Color(0xFFFF4D4F),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Text("点击重试", color = Color.White)
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("请选出所有的 ")
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFFE91E63),
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(challenge.targetName)
                            }
                        },
                        color = Color(0xFF444444),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    val gridAlpha by animateFloatAsState(
                        targetValue = if (isLoading) 0.5f else 1f,
                        animationSpec = tween(200),
                        label = "captchaGridAlpha"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(gridAlpha),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val images = challenge.images
                        for (row in 0 until 3) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (col in 0 until 3) {
                                    val idx = row * 3 + col
                                    if (idx < images.size) {
                                        CaptchaTile(
                                            imageUrl = images[idx],
                                            isSelected = idx in selectedIndexes,
                                            enabled = !isLoading && !isConfirmed,
                                            onClick = { onToggleIndex(idx) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("captcha_tile_$idx")
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onRefresh,
                            enabled = !isLoading && !isConfirmed,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F7FA))
                                .testTag("captcha_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "刷新验证码",
                                tint = Color(0xFF666666)
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            enabled = selectedIndexes.isNotEmpty() && !isLoading && !isConfirmed,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE91E63),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE91E63).copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.85f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("captcha_confirm_button")
                        ) {
                            Text(
                                text = if (isConfirmed) "已确认" else "确认提交",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFFFFF2F0),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFFF4D4F),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isConfirmed,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color(0xFF52C41A),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "已确认",
                        color = Color(0xFF52C41A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "正在处理注册...",
                        color = Color(0xFF999999),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptchaTile(
    imageUrl: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = tween(180),
        label = "captchaTileScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF0F0F0))
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, Color(0xFFE91E63), RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(360, 360)
                .precision(Precision.INEXACT)
                .crossfade(true)
                .build(),
            contentDescription = "验证图片",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE91E63),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "加载失败",
                        color = Color(0xFF888888),
                        fontSize = 10.sp
                    )
                }
            }
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.6f),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.6f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33E91E63)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "已选择",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun BangCaptchaWebViewContent(
    onCaptchaResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPageLoading by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    addJavascriptInterface(
                        CaptchaJsBridge(onCaptchaResult),
                        "AndroidCaptchaBridge"
                    )
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isPageLoading = false
                            val shimJs = """
                                (function() {
                                  if (window.__bocchiCaptchaHooked) return;
                                  window.__bocchiCaptchaHooked = true;
                                  function forwardIfCaptcha(data) {
                                    if (data && data.type === 'CAPTCHA_RESULT' && data.payload) {
                                      AndroidCaptchaBridge.onCaptchaResult(JSON.stringify(data.payload));
                                    }
                                  }
                                  var origPost = window.postMessage.bind(window);
                                  window.postMessage = function(msg, targetOrigin, transfer) {
                                    forwardIfCaptcha(msg);
                                    return origPost(msg, targetOrigin, transfer);
                                  };
                                  if (window.parent && window.parent !== window) {
                                    var origParentPost = window.parent.postMessage.bind(window.parent);
                                    window.parent.postMessage = function(msg, targetOrigin, transfer) {
                                      forwardIfCaptcha(msg);
                                      return origParentPost(msg, targetOrigin, transfer);
                                    };
                                  }
                                  window.addEventListener('message', function(ev) {
                                    forwardIfCaptcha(ev.data);
                                  });
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(shimJs, null)
                        }
                    }
                    loadUrl(CloudflareApiService.CAPTCHA_URL)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isPageLoading) {
            CircularProgressIndicator(
                color = BocchiPink,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
