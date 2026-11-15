package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ui.components.AssetImage
import kotlinx.coroutines.isActive

@Composable
fun PhotosSection(
    galleryRows: List<List<String>>,
    activePhotoKey: Pair<Int, Int>?,
    onTogglePhoto: (rowIndex: Int, itemIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgTransition = rememberInfiniteTransition(label = "photosBg")
    val shift by bgTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "photoBgShift"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF7D99),
                        Color(0xFFFFC766),
                        Color(0xFF5CB6FF),
                        Color(0xFFFF6363)
                    ),
                    start = Offset(0f, shift * 300f),
                    end = Offset(1200f, 1800f - shift * 300f)
                )
            )
            .padding(top = 68.dp, bottom = 14.dp)
    ) {
        val cardSize = when {
            maxWidth >= 900.dp -> 190.dp
            maxWidth >= 600.dp -> 150.dp
            else -> 124.dp
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            galleryRows.forEachIndexed { rowIndex, rowPhotos ->
                val isForward = rowIndex % 2 == 0
                val isRowPaused = activePhotoKey?.first == rowIndex
                PhotoMarqueeRow(
                    photos = rowPhotos,
                    forward = isForward,
                    paused = isRowPaused,
                    cardSize = cardSize,
                    baseTiltDegrees = if (isForward) -18f else 18f,
                    rowIndex = rowIndex,
                    activeItemIndex = if (isRowPaused) activePhotoKey?.second else null,
                    onPhotoClick = { itemIdx ->
                        onTogglePhoto(rowIndex, itemIdx)
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoMarqueeRow(
    photos: List<String>,
    forward: Boolean,
    paused: Boolean,
    cardSize: Dp,
    baseTiltDegrees: Float,
    rowIndex: Int,
    activeItemIndex: Int?,
    onPhotoClick: (Int) -> Unit
) {
    val repeatedPhotos = remember(photos) {
        photos + photos + photos + photos
    }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (forward) 0 else photos.size * 2
    )

    LaunchedEffect(paused, forward) {
        if (!paused) {
            var lastNanos = 0L
            while (isActive) {
                withFrameNanos { frameNanos ->
                    if (lastNanos != 0L) {
                        val dtSec = (frameNanos - lastNanos) / 1_000_000_000f
                        val speedPx = if (forward) 64f else -64f
                        listState.dispatchRawDelta(speedPx * dtSec)
                    }
                    lastNanos = frameNanos
                }
                val idx = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset
                if (forward && idx >= photos.size * 3) {
                    listState.scrollToItem(photos.size, offset)
                } else if (!forward && idx <= photos.size / 2) {
                    listState.scrollToItem(photos.size * 2 + photos.size / 2, offset)
                }
            }
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(cardSize * 1.24f),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(
            items = repeatedPhotos,
            key = { itemIdx, _ -> "${rowIndex}_$itemIdx" }
        ) { itemIdx, photo ->
            val isFlipped = activeItemIndex == itemIdx

            val rotationYAnim by animateFloatAsState(
                targetValue = if (isFlipped) 0f else baseTiltDegrees,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
                label = "photoFlip"
            )
            val cardWidthAnim by animateDpAsState(
                targetValue = if (isFlipped) cardSize * 1.6f else cardSize,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
                label = "photoWidthExpand"
            )
            val scaleAnim by animateFloatAsState(
                targetValue = if (isFlipped) 1.08f else 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                label = "photoScale"
            )
            val alphaAnim by animateFloatAsState(
                targetValue = if (isFlipped) 1f else 0.8f,
                animationSpec = tween(durationMillis = 320),
                label = "photoAlpha"
            )

            Box(
                modifier = Modifier
                    .zIndex(if (isFlipped) 100f else 0f)
                    .size(width = cardWidthAnim, height = cardSize)
                    .graphicsLayer {
                        rotationY = rotationYAnim
                        scaleX = scaleAnim
                        scaleY = scaleAnim
                        alpha = alphaAnim
                        // Compose cameraDistance is already density-scaled internally;
                        // small value (~3.4f) produces crisp 3D trapezoid perspective matching CSS.
                        cameraDistance = 3.4f
                    }
                    .shadow(if (isFlipped) 12.dp else 4.dp, RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(15.dp))
                    .then(
                        if (isFlipped) {
                            Modifier.border(2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(15.dp))
                        } else {
                            Modifier
                        }
                    )
                    .semantics { selected = isFlipped }
                    .clickable { onPhotoClick(itemIdx) }
                    .testTag("gallery_photo_${rowIndex}_$itemIdx")
            ) {
                AssetImage(
                    assetPath = photo,
                    contentDescription = "孤独摇滚相册图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
