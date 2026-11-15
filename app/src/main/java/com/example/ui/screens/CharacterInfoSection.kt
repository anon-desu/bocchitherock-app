package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BandCatalog
import com.example.model.CharacterProfile
import com.example.ui.components.AssetImage
import com.example.ui.theme.AaPixelFontFamily
import com.example.ui.theme.NotoSerifScFontFamily
import com.example.ui.theme.StarryDark

@Composable
fun CharacterInfoSection(
    selectedIndex: Int,
    onSelectCharacter: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val characters = BandCatalog.characters
    val activeCharacter = characters[selectedIndex.coerceIn(characters.indices)]
    val accentColor by animateColorAsState(
        targetValue = activeCharacter.accentColor,
        animationSpec = tween(500),
        label = "charAccent"
    )

    val marqueeTransition = rememberInfiniteTransition(label = "romajiMarquee")
    val marqueeOffset by marqueeTransition.animateFloat(
        initialValue = 0f,
        targetValue = -500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marqueeOffset"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(StarryDark)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(size.width * 0.52f, size.height * 0.36f),
                        radius = size.maxDimension * 0.65f
                    )
                )
            }
    ) {
        val isWide = maxWidth >= 760.dp
        val showCompactPoster = !isWide && maxHeight >= 640.dp && maxWidth >= 360.dp

        // Animated instrument watermark
        AnimatedContent(
            targetState = activeCharacter.instrument,
            transitionSpec = {
                (fadeIn(tween(380)) + scaleIn(initialScale = 0.92f)) togetherWith
                    (fadeOut(tween(220)) + scaleOut(targetScale = 1.06f))
            },
            label = "instrumentWatermark",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 72.dp, end = 20.dp)
        ) { instrument ->
            Text(
                text = instrument,
                fontSize = if (isWide) 180.sp else 110.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.05f)
            )
        }

        if (isWide) {
            WideCharacterLayout(
                activeCharacter = activeCharacter,
                accentColor = accentColor,
                marqueeOffset = marqueeOffset,
                characters = characters,
                selectedIndex = selectedIndex,
                onSelectCharacter = onSelectCharacter
            )
        } else {
            CompactCharacterLayout(
                activeCharacter = activeCharacter,
                accentColor = accentColor,
                marqueeOffset = marqueeOffset,
                characters = characters,
                selectedIndex = selectedIndex,
                showPoster = showCompactPoster,
                onSelectCharacter = onSelectCharacter
            )
        }
    }
}

@Composable
private fun WideCharacterLayout(
    activeCharacter: CharacterProfile,
    accentColor: Color,
    marqueeOffset: Float,
    characters: List<CharacterProfile>,
    selectedIndex: Int,
    onSelectCharacter: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = activeCharacter,
            transitionSpec = {
                (fadeIn(tween(420)) + slideInHorizontally { -it / 5 }) togetherWith
                    (fadeOut(tween(260)) + slideOutHorizontally { -it / 5 })
            },
            label = "widePoster",
            modifier = Modifier
                .fillMaxHeight()
                .width(260.dp)
        ) { char ->
            AssetImage(
                assetPath = char.posterAssetPath,
                contentDescription = "${char.name} 海报",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            AnimatedContent(
                targetState = activeCharacter,
                transitionSpec = {
                    (fadeIn(tween(450)) + slideInHorizontally { it / 4 } + scaleIn(initialScale = 0.94f)) togetherWith
                        (fadeOut(tween(260)) + slideOutHorizontally { -it / 6 })
                },
                label = "wideArt",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 32.dp, top = 56.dp)
                    .fillMaxHeight(0.9f)
            ) { char ->
                AssetImage(
                    assetPath = char.artAssetPath,
                    contentDescription = "${char.name} 立绘",
                    modifier = Modifier.fillMaxHeight(),
                    contentScale = ContentScale.Fit
                )
            }

            AnimatedContent(
                targetState = activeCharacter,
                transitionSpec = {
                    (fadeIn(tween(360)) + slideInVertically { -it / 4 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically { it / 4 })
                },
                label = "wideQuote",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 28.dp, top = 96.dp)
            ) { char ->
                CharacterQuoteBox(
                    lines = char.lines,
                    accentColor = accentColor
                )
            }

            NameMarqueeStrip(
                character = activeCharacter,
                accentColor = accentColor,
                marqueeOffset = marqueeOffset,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
            )

            AnimatedContent(
                targetState = activeCharacter,
                transitionSpec = {
                    (fadeIn(tween(380)) + slideInVertically { it / 4 }) togetherWith
                        (fadeOut(tween(220)) + slideOutVertically { -it / 4 })
                },
                label = "wideBio",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, bottom = 112.dp)
                    .fillMaxWidth(0.55f)
            ) { char ->
                CharacterBioCard(
                    character = char,
                    accentColor = accentColor
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, bottom = 20.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        CharacterSelectorRow(
            characters = characters,
            selectedIndex = selectedIndex,
            onSelectCharacter = onSelectCharacter,
            compact = false
        )
    }
}

@Composable
private fun CompactCharacterLayout(
    activeCharacter: CharacterProfile,
    accentColor: Color,
    marqueeOffset: Float,
    characters: List<CharacterProfile>,
    selectedIndex: Int,
    showPoster: Boolean,
    onSelectCharacter: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp)
    ) {
        // Standing Art behind info cards
        AnimatedContent(
            targetState = activeCharacter,
            transitionSpec = {
                (fadeIn(tween(450)) + slideInHorizontally { it / 4 } + scaleIn(initialScale = 0.94f)) togetherWith
                    (fadeOut(tween(260)) + slideOutHorizontally { -it / 5 })
            },
            label = "compactArt",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight(0.68f)
        ) { char ->
            AssetImage(
                assetPath = char.artAssetPath,
                contentDescription = "${char.name} 立绘",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Quote pill top-left
        AnimatedContent(
            targetState = activeCharacter,
            transitionSpec = {
                (fadeIn(tween(350)) + slideInHorizontally { -it / 4 }) togetherWith
                    (fadeOut(tween(200)) + slideOutHorizontally { -it / 4 })
            },
            label = "compactQuote",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 8.dp)
        ) { char ->
            CharacterQuoteBox(
                lines = char.lines.take(2),
                accentColor = accentColor
            )
        }

        // Framed poster card on top-right only when vertical & horizontal space allows
        if (showPoster) {
            AnimatedContent(
                targetState = activeCharacter,
                transitionSpec = {
                    (fadeIn(tween(420)) + scaleIn(initialScale = 0.88f)) togetherWith
                        (fadeOut(tween(240)) + scaleOut(targetScale = 0.92f))
                },
                label = "compactPoster",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 8.dp)
            ) { char ->
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .aspectRatio(1180f / 1530f)
                        .graphicsLayer { rotationZ = 3f }
                        .shadow(8.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, accentColor.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                        .testTag("compact_character_poster")
                ) {
                    AssetImage(
                        assetPath = char.posterAssetPath,
                        contentDescription = "${char.name} 海报",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Bottom info stack
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedContent(
                targetState = activeCharacter,
                transitionSpec = {
                    (fadeIn(tween(380)) + slideInVertically { it / 4 }) togetherWith
                        (fadeOut(tween(220)) + slideOutVertically { -it / 4 })
                },
                label = "compactBio",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) { char ->
                CharacterBioCard(
                    character = char,
                    accentColor = accentColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            NameMarqueeStrip(
                character = activeCharacter,
                accentColor = accentColor,
                marqueeOffset = marqueeOffset,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CharacterSelectorRow(
                    characters = characters,
                    selectedIndex = selectedIndex,
                    onSelectCharacter = onSelectCharacter,
                    compact = true
                )
            }
        }
    }
}

@Composable
private fun CharacterQuoteBox(
    lines: List<String>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .drawBehind {
                val stroke = 2.dp.toPx()
                val corner = 18.dp.toPx()
                drawLine(accentColor, Offset(0f, 0f), Offset(corner, 0f), strokeWidth = stroke)
                drawLine(accentColor, Offset(0f, 0f), Offset(0f, corner), strokeWidth = stroke)
                drawLine(accentColor, Offset(size.width, size.height), Offset(size.width - corner, size.height), strokeWidth = stroke)
                drawLine(accentColor, Offset(size.width, size.height), Offset(size.width, size.height - corner), strokeWidth = stroke)
            }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            lines.forEach { line ->
                Text(
                    text = line,
                    color = accentColor,
                    fontFamily = NotoSerifScFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NameMarqueeStrip(
    character: CharacterProfile,
    accentColor: Color,
    marqueeOffset: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(0.dp))
        ) {
            Text(
                text = character.romaji.repeat(12),
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.graphicsLayer { translationX = marqueeOffset }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor)
                .padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
            AnimatedContent(
                targetState = character.name,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically { it / 2 }) togetherWith
                        (fadeOut(tween(180)) + slideOutVertically { -it / 2 })
                },
                label = "characterName"
            ) { charName ->
                Text(
                    text = charName,
                    color = Color.Black,
                    fontFamily = AaPixelFontFamily,
                    fontSize = 26.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CharacterBioCard(
    character: CharacterProfile,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.68f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accentColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "CV",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = character.cv,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = character.instrument,
                color = accentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${character.romaji.trim()} / CV ${character.cvRomaji}",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.4f))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = character.description,
            color = Color.White.copy(alpha = 0.92f),
            fontFamily = NotoSerifScFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun CharacterSelectorRow(
    characters: List<CharacterProfile>,
    selectedIndex: Int,
    onSelectCharacter: (Int) -> Unit,
    compact: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        characters.forEachIndexed { index, character ->
            val isSelected = index == selectedIndex
            val boxSize by animateDpAsState(
                targetValue = when {
                    compact && isSelected -> 66.dp
                    compact -> 54.dp
                    isSelected -> 92.dp
                    else -> 76.dp
                },
                animationSpec = tween(250),
                label = "thumbSize"
            )

            Box(
                modifier = Modifier
                    .size(boxSize)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) character.accentColor else Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectCharacter(index) }
                    .testTag("character_select_$index")
            ) {
                AssetImage(
                    assetPath = character.posterAssetPath,
                    contentDescription = "选择角色 ${character.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
