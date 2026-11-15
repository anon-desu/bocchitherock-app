package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BandCatalog
import com.example.model.Track
import com.example.playback.PlaybackUiState
import com.example.ui.components.AssetImage
import com.example.ui.components.formatDurationMs
import com.example.ui.theme.BocchiPink
import kotlinx.coroutines.isActive

@Composable
fun MusicPlayerSection(
    playbackState: PlaybackUiState,
    likedSongs: Set<String>,
    onSwitchPlaylist: (String) -> Unit,
    onSelectTrack: (Track) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onWeightedRandom: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenMv: () -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onAdjustVolume: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val tracklist = BandCatalog.playlists[playbackState.activePlaylistName]
        ?: BandCatalog.playlists.getValue("結束バンド")

    // Smooth gradient shift
    val bgTransition = rememberInfiniteTransition(label = "musicBg")
    val bgShift by bgTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgShift"
    )

    // Continuous vinyl disc rotation that pauses smoothly in-place when paused
    var vinylRotation by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(playbackState.isPlaying) {
        if (playbackState.isPlaying) {
            var lastNanos = 0L
            while (isActive) {
                withFrameNanos { frameNanos ->
                    if (lastNanos != 0L) {
                        val dtSec = (frameNanos - lastNanos) / 1_000_000_000f
                        vinylRotation = (vinylRotation + dtSec * 24f) % 360f
                    }
                    lastNanos = frameNanos
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF86BE),
                        Color(0xFFFFD859),
                        Color(0xFF5AD0FF),
                        Color(0xFFFF5656)
                    ),
                    start = Offset(x = -200f + bgShift * 400f, y = 0f),
                    end = Offset(x = 1200f - bgShift * 300f, y = 1800f)
                )
            )
            .padding(top = 66.dp, bottom = 10.dp, start = 12.dp, end = 12.dp)
    ) {
        FloatingMusicNotesOverlay(isPlaying = playbackState.isPlaying)

        val isWide = maxWidth >= 760.dp

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 1140.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.96f),
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.78f),
            shadowElevation = 8.dp
        ) {
            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    PlaylistPanel(
                        activePlaylistName = playbackState.activePlaylistName,
                        activeTrackName = playbackState.activeTrack.name,
                        likedSongs = likedSongs,
                        tracks = tracklist,
                        onSwitchPlaylist = onSwitchPlaylist,
                        onSelectTrack = onSelectTrack,
                        modifier = Modifier
                            .weight(0.38f)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.55f))
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFE0E0E0))
                    )
                    PlayerTurntablePane(
                        playbackState = playbackState,
                        isLiked = playbackState.activeTrack.name in likedSongs,
                        vinylRotation = vinylRotation,
                        discSize = 240.dp,
                        coverSize = 166.dp,
                        onTogglePlayPause = onTogglePlayPause,
                        onNextTrack = onNextTrack,
                        onPreviousTrack = onPreviousTrack,
                        onWeightedRandom = onWeightedRandom,
                        onToggleLike = onToggleLike,
                        onOpenMv = onOpenMv,
                        onSeekToFraction = onSeekToFraction,
                        onVolumeChange = onVolumeChange,
                        onAdjustVolume = onAdjustVolume,
                        modifier = Modifier
                            .weight(0.62f)
                            .fillMaxHeight()
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    PlaylistPanel(
                        activePlaylistName = playbackState.activePlaylistName,
                        activeTrackName = playbackState.activeTrack.name,
                        likedSongs = likedSongs,
                        tracks = tracklist,
                        onSwitchPlaylist = onSwitchPlaylist,
                        onSelectTrack = onSelectTrack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.34f)
                            .background(Color.White.copy(alpha = 0.55f))
                    )
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(Color(0xFFE0E0E0))
                    )
                    PlayerTurntablePane(
                        playbackState = playbackState,
                        isLiked = playbackState.activeTrack.name in likedSongs,
                        vinylRotation = vinylRotation,
                        discSize = 152.dp,
                        coverSize = 104.dp,
                        onTogglePlayPause = onTogglePlayPause,
                        onNextTrack = onNextTrack,
                        onPreviousTrack = onPreviousTrack,
                        onWeightedRandom = onWeightedRandom,
                        onToggleLike = onToggleLike,
                        onOpenMv = onOpenMv,
                        onSeekToFraction = onSeekToFraction,
                        onVolumeChange = onVolumeChange,
                        onAdjustVolume = onAdjustVolume,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.66f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingMusicNotesOverlay(isPlaying: Boolean) {
    val noteAnim = rememberInfiniteTransition(label = "notes")
    val progress by noteAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "noteCycle"
    )

    val notes = listOf("♪", "♫", "♩", "♬", "♪", "♫")
    val xFractions = listOf(0.14f, 0.28f, 0.45f, 0.65f, 0.80f, 0.90f)
    val yOffsets = listOf(0.25f, 0.72f, 0.42f, 0.78f, 0.22f, 0.62f)

    if (isPlaying) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            notes.forEachIndexed { idx, symbol ->
                val phase = (progress + idx * 0.17f) % 1f
                val alpha = when {
                    phase < 0.15f -> phase / 0.15f
                    phase > 0.85f -> (1f - phase) / 0.15f
                    else -> 1f
                } * 0.65f
                Text(
                    text = symbol,
                    color = Color.White.copy(alpha = alpha),
                    fontSize = 36.sp,
                    modifier = Modifier
                        .offset(
                            x = maxWidth * xFractions[idx],
                            y = maxHeight * yOffsets[idx] - (80.dp * phase)
                        )
                        .graphicsLayer { rotationZ = phase * 360f }
                )
            }
        }
    }
}

@Composable
private fun PlaylistPanel(
    activePlaylistName: String,
    activeTrackName: String,
    likedSongs: Set<String>,
    tracks: List<Track>,
    onSwitchPlaylist: (String) -> Unit,
    onSelectTrack: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.05f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BandCatalog.playlistNames.forEach { name ->
                val isActive = name == activePlaylistName
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) BocchiPink else Color.White.copy(alpha = 0.6f))
                        .border(
                            width = 1.dp,
                            color = if (isActive) Color.Transparent else Color.Black.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSwitchPlaylist(name) }
                        .padding(vertical = 8.dp)
                        .testTag("playlist_tab_$name"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = if (isActive) Color.White else Color(0xFF444444),
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(tracks, key = { "${it.playlistName}_${it.name}" }) { track ->
                val isSelected = track.name == activeTrackName
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) Color(0xFFE6E6EA) else Color.Transparent)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 0.dp,
                                    color = Color.Transparent
                                )
                            } else Modifier
                        )
                        .clickable { onSelectTrack(track) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("track_item_${track.index}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(42.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(BocchiPink)
                        )
                    }
                    AssetImage(
                        assetPath = track.albumAssetPath,
                        contentDescription = track.name,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.name,
                            color = Color(0xFF222222),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.singer,
                            color = Color(0xFF666666),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (track.name in likedSongs) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = "已收藏",
                            tint = BocchiPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerTurntablePane(
    playbackState: PlaybackUiState,
    isLiked: Boolean,
    vinylRotation: Float,
    discSize: Dp,
    coverSize: Dp,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onWeightedRandom: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenMv: () -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onAdjustVolume: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Spinning Vinyl Disc
        Box(
            modifier = Modifier
                .size(discSize)
                .graphicsLayer { rotationZ = vinylRotation }
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF4A4A4A), Color(0xFF1F1F1F), Color(0xFF333333))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AssetImage(
                assetPath = playbackState.activeTrack.albumAssetPath,
                contentDescription = playbackState.activeTrack.name,
                modifier = Modifier
                    .size(coverSize)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // Song title & singer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = playbackState.activeTrack.name,
                color = Color(0xFF1F1F23),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = playbackState.activeTrack.singer,
                color = Color(0xFF55555E),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Volume Control
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.84f)
        ) {
            IconButton(
                onClick = { onAdjustVolume(-0.1f) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("volume_down_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "减小音量",
                    tint = Color(0xFF444444)
                )
            }
            Slider(
                value = playbackState.volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = BocchiPink,
                    activeTrackColor = BocchiPink,
                    inactiveTrackColor = Color.Black.copy(alpha = 0.14f)
                )
            )
            IconButton(
                onClick = { onAdjustVolume(0.1f) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("volume_up_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "增大音量",
                    tint = Color(0xFF444444)
                )
            }
        }

        // Like, Weighted Shuffle, MV buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleLike,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("like_song_button")
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isLiked) "取消收藏" else "收藏歌曲",
                    tint = if (isLiked) BocchiPink else Color(0xFF4A4A4A),
                    modifier = Modifier.size(26.dp)
                )
            }

            IconButton(
                onClick = onWeightedRandom,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("weighted_random_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "加权随机播放",
                    tint = Color(0xFF4A4A4A),
                    modifier = Modifier.size(26.dp)
                )
            }

            val hasMv = playbackState.activeTrack.bvid.isNotBlank()
            IconButton(
                onClick = onOpenMv,
                enabled = hasMv,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("open_mv_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.OndemandVideo,
                    contentDescription = "观看 MV",
                    tint = if (hasMv) Color(0xFF4A4A4A) else Color(0xFFAAAAAA),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Progress Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formatDurationMs(playbackState.currentPositionMs),
                color = Color(0xFF444444),
                fontSize = 11.sp,
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.Center
            )
            Slider(
                value = playbackState.progress,
                onValueChange = onSeekToFraction,
                modifier = Modifier
                    .weight(1f)
                    .testTag("music_seek_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF5252),
                    activeTrackColor = Color(0xFFFF8A00),
                    inactiveTrackColor = Color.Black.copy(alpha = 0.14f)
                )
            )
            val totalLabel = if (playbackState.durationMs > 0L) {
                formatDurationMs(playbackState.durationMs)
            } else {
                playbackState.activeTrack.durationLabel
            }
            Text(
                text = totalLabel,
                color = Color(0xFF444444),
                fontSize = 11.sp,
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.Center
            )
        }

        // Transport Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousTrack,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("prev_track_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "上一首",
                    tint = Color(0xFF2E2E34),
                    modifier = Modifier.size(34.dp)
                )
            }

            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (playbackState.isPlaying) {
                        Icons.Rounded.PauseCircleFilled
                    } else {
                        Icons.Rounded.PlayCircleFilled
                    },
                    contentDescription = if (playbackState.isPlaying) "暂停" else "播放",
                    tint = BocchiPink,
                    modifier = Modifier.size(56.dp)
                )
            }

            IconButton(
                onClick = onNextTrack,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("next_track_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "下一首",
                    tint = Color(0xFF2E2E34),
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MvPlayerDialog(
    track: Track,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val playerUrl = "https://player.bilibili.com/player.html?isOutside=true&bvid=${track.bvid}&page=1&autoplay=1"
    val externalUrl = "https://www.bilibili.com/video/${track.bvid}"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 780.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF16161A)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = BocchiPink
                        )
                        Text(
                            text = "${track.name} - MV",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl)))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFF99D6F1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("浏览器打开", color = Color(0xFF99D6F1), fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_mv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "关闭 MV",
                                tint = Color.White
                            )
                        }
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            loadUrl(playerUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                )
            }
        }
    }
}
