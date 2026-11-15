package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AssetImage
import com.example.ui.theme.StarryDark
import kotlinx.coroutines.launch

@Composable
fun HeroSection(
    onExploreCharacters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(1.8f) }
    val logoRotation = remember { Animatable(720f) }

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(900))
        }
        launch {
            logoScale.animateTo(1f, tween(1300, easing = FastOutSlowInEasing))
        }
        launch {
            logoRotation.animateTo(0f, tween(1300, easing = FastOutSlowInEasing))
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "heroArrow")
    val arrowOffset by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowBounce"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StarryDark)
    ) {
        AssetImage(
            assetPath = "videos/video_poster.webp",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.2f),
                            StarryDark.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        AssetImage(
            assetPath = "images/logo_movie_cn.png",
            contentDescription = "孤独摇滚剧场版 Logo",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .widthIn(max = 460.dp)
                .fillMaxWidth(0.84f)
                .graphicsLayer {
                    alpha = logoAlpha.value
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                    rotationZ = logoRotation.value
                },
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .clickable { onExploreCharacters() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "向上滑动探索",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "向下滚动",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .size(26.dp)
                    .offset(y = arrowOffset.dp)
            )
        }
    }
}
