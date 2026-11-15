package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BandCatalog
import com.example.model.Producer
import com.example.ui.components.AssetImage
import com.example.ui.theme.NoteScriptFontFamily
import com.example.ui.theme.NotoSerifScFontFamily
import com.example.ui.theme.ProducerPurple

@Composable
fun AboutSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val staffAnim = rememberInfiniteTransition(label = "onpuAnim")
    val item1Offset by staffAnim.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "onpu1"
    )
    val item2Offset by staffAnim.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "onpu2"
    )
    val lineScaleY by staffAnim.animateFloat(
        initialValue = 1f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(tween(4500, easing = LinearEasing), RepeatMode.Reverse),
        label = "onpuLine"
    )

    Box(modifier = modifier.fillMaxSize()) {
        AssetImage(
            assetPath = "images/pagebg.png",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Subtle animated musical staff layers
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(170.dp)
                .padding(top = 60.dp)
        ) {
            AssetImage(
                assetPath = "images/onpu_line.png",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleY = lineScaleY
                        alpha = 0.5f
                    },
                contentScale = ContentScale.Fit
            )
            AssetImage(
                assetPath = "images/onpu_item1.png",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = item1Offset.dp),
                contentScale = ContentScale.Fit
            )
            AssetImage(
                assetPath = "images/onpu_item2.png",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = item2Offset.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 84.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Title + GitHub Link + Ciallo Subtitle
            Column(
                modifier = Modifier
                    .widthIn(max = 820.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_title),
                        color = ProducerPurple.copy(alpha = 0.9f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/anon-desu/Bocchi-the-Rock"))
                            )
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .testTag("github_repo_button")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_github_cat),
                            contentDescription = "查看 GitHub 源码",
                            tint = Color(0xFFE0E0E8),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.about_subtitle),
                    color = Color.Yellow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // PRODUCER Section
            BoxWithConstraints(
                modifier = Modifier
                    .widthIn(max = 820.dp)
                    .fillMaxWidth()
            ) {
                val isTablet = maxWidth >= 600.dp
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.producer_header),
                        color = ProducerPurple,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = if (isTablet) TextAlign.Start else TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isTablet) {
                        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            BandCatalog.producers.forEach { producer ->
                                ProducerRowCard(producer = producer)
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BandCatalog.producers.forEach { producer ->
                                ProducerColumnCard(producer = producer)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Moe ICP Footer
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://icp.gov.moe/?keyword=20261129"))
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("icp_footer_link")
            ) {
                Text(
                    text = stringResource(R.string.icp_text),
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ProducerRowCard(producer: Producer) {
    var spinTarget by remember(producer.name) { mutableFloatStateOf(0f) }
    val avatarRotation by animateFloatAsState(
        targetValue = spinTarget,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "producerRowAvatarSpin"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AssetImage(
            assetPath = producer.avatarAssetPath,
            contentDescription = "${producer.name} 头像，点击旋转",
            modifier = Modifier
                .size(92.dp)
                .graphicsLayer { rotationZ = avatarRotation }
                .clip(CircleShape)
                .border(2.dp, producer.accentColor.copy(alpha = 0.8f), CircleShape)
                .clickable { spinTarget += 360f }
                .testTag("producer_avatar_${producer.name}"),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = producer.name,
                color = producer.accentColor,
                fontFamily = NoteScriptFontFamily,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = producer.description,
                color = producer.accentColor,
                fontFamily = NotoSerifScFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ProducerColumnCard(producer: Producer) {
    var spinTarget by remember(producer.name) { mutableFloatStateOf(0f) }
    val avatarRotation by animateFloatAsState(
        targetValue = spinTarget,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "producerColAvatarSpin"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AssetImage(
            assetPath = producer.avatarAssetPath,
            contentDescription = "${producer.name} 头像，点击旋转",
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer { rotationZ = avatarRotation }
                .clip(CircleShape)
                .border(2.dp, producer.accentColor.copy(alpha = 0.8f), CircleShape)
                .clickable { spinTarget += 360f }
                .testTag("producer_avatar_${producer.name}"),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = producer.name,
            color = producer.accentColor,
            fontFamily = NoteScriptFontFamily,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = producer.description,
            color = producer.accentColor,
            fontFamily = NotoSerifScFontFamily,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
    }
}
