package com.example.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.BandCatalog
import com.example.model.Track
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val activePlaylistName: String = "結束バンド",
    val playingPlaylistName: String = "結束バンド",
    val activeTrack: Track = BandCatalog.playlists.getValue("結束バンド").first(),
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 0.7f
) {
    val progress: Float
        get() = if (durationMs > 0L) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

class MusicPlayerController(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var progressJob: Job? = null

    private val availableMusicAssets: Set<String> by lazy {
        context.assets.list("musics")?.toSet() ?: emptySet()
    }

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startProgressTicker()
            } else {
                stopProgressTicker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val ctrl = mediaController ?: return
            if (playbackState == Player.STATE_READY) {
                _uiState.update {
                    it.copy(
                        durationMs = ctrl.duration.coerceAtLeast(0L),
                        currentPositionMs = ctrl.currentPosition.coerceAtLeast(0L)
                    )
                }
            } else if (playbackState == Player.STATE_ENDED) {
                nextTrack(autoPlay = true)
            }
        }
    }

    fun connect() {
        if (controllerFuture != null) return
        runCatching {
            val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val future = MediaController.Builder(context, token).buildAsync()
            controllerFuture = future
            future.addListener({
                runCatching {
                    val controller = future.get()
                    mediaController = controller
                    controller.volume = _uiState.value.volume
                    controller.addListener(playerListener)
                    if (controller.mediaItemCount == 0) {
                        prepareTrack(_uiState.value.activeTrack, autoPlay = false)
                    } else {
                        _uiState.update {
                            it.copy(
                                isPlaying = controller.isPlaying,
                                durationMs = controller.duration.coerceAtLeast(0L),
                                currentPositionMs = controller.currentPosition.coerceAtLeast(0L)
                            )
                        }
                        if (controller.isPlaying) startProgressTicker()
                    }
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    fun switchBrowsedPlaylist(playlistName: String) {
        if (playlistName !in BandCatalog.playlists) return
        _uiState.update { it.copy(activePlaylistName = playlistName) }
    }

    fun selectTrack(track: Track, autoPlay: Boolean = true) {
        _uiState.update {
            it.copy(
                activeTrack = track,
                playingPlaylistName = track.playlistName,
                currentPositionMs = 0L,
                durationMs = 0L
            )
        }
        prepareTrack(track, autoPlay = autoPlay)
    }

    fun togglePlayPause() {
        val ctrl = mediaController
        val track = _uiState.value.activeTrack
        if (ctrl == null) {
            connect()
            return
        }
        if (ctrl.mediaItemCount == 0) {
            prepareTrack(track, autoPlay = true)
            return
        }
        if (ctrl.isPlaying) {
            ctrl.pause()
        } else {
            ctrl.play()
        }
    }

    fun nextTrack(autoPlay: Boolean = _uiState.value.isPlaying) {
        val list = currentPlayingList()
        if (list.isEmpty()) return
        val currentIdx = list.indexOfFirst { it.name == _uiState.value.activeTrack.name }
        val nextIdx = if (currentIdx == -1 || currentIdx + 1 >= list.size) 0 else currentIdx + 1
        selectTrack(list[nextIdx], autoPlay = autoPlay)
    }

    fun previousTrack(autoPlay: Boolean = _uiState.value.isPlaying) {
        val list = currentPlayingList()
        if (list.isEmpty()) return
        val currentIdx = list.indexOfFirst { it.name == _uiState.value.activeTrack.name }
        val prevIdx = if (currentIdx <= 0) list.lastIndex else currentIdx - 1
        selectTrack(list[prevIdx], autoPlay = autoPlay)
    }

    fun playWeightedRandom(likedSongs: Set<String>) {
        val list = currentPlayingList()
        val currentName = _uiState.value.activeTrack.name
        val weightedPool = buildList {
            list.forEach { song ->
                if (song.name != currentName) {
                    val weight = if (song.name in likedSongs) 5 else 1
                    repeat(weight) { add(song) }
                }
            }
        }
        if (weightedPool.isEmpty()) {
            nextTrack(autoPlay = true)
        } else {
            selectTrack(weightedPool.random(), autoPlay = true)
        }
    }

    fun seekToFraction(fraction: Float) {
        val ctrl = mediaController ?: return
        val duration = ctrl.duration.coerceAtLeast(0L)
        if (duration > 0L) {
            val targetMs = (fraction.coerceIn(0f, 1f) * duration).toLong()
            ctrl.seekTo(targetMs)
            _uiState.update { it.copy(currentPositionMs = targetMs) }
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        mediaController?.volume = clamped
        _uiState.update { it.copy(volume = clamped) }
    }

    fun adjustVolume(delta: Float) {
        setVolume(_uiState.value.volume + delta)
    }

    private fun currentPlayingList(): List<Track> {
        val playing = _uiState.value.playingPlaylistName
        return BandCatalog.playlists[playing]
            ?: BandCatalog.playlists.values.first()
    }

    private fun resolveTrackUri(track: Track): Uri {
        return if (track.audioAssetFileName in availableMusicAssets) {
            Uri.parse("asset:///musics/${track.audioAssetFileName}")
        } else {
            Uri.Builder()
                .scheme("https")
                .authority("band.bocchi.us.kg")
                .appendPath("assets")
                .appendPath("musics")
                .appendPath(track.audioAssetFileName)
                .build()
        }
    }

    private fun prepareTrack(track: Track, autoPlay: Boolean) {
        val ctrl = mediaController ?: return
        val mediaItem = MediaItem.Builder()
            .setUri(resolveTrackUri(track))
            .setMediaId(track.name)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.name)
                    .setArtist(track.singer)
                    .setAlbumTitle(track.playlistName)
                    .build()
            )
            .build()
        ctrl.setMediaItem(mediaItem)
        ctrl.prepare()
        ctrl.playWhenReady = autoPlay
    }

    private fun startProgressTicker() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (isActive) {
                val ctrl = mediaController
                if (ctrl != null) {
                    _uiState.update {
                        it.copy(
                            currentPositionMs = ctrl.currentPosition.coerceAtLeast(0L),
                            durationMs = ctrl.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(250L)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTicker()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        mediaController = null
    }
}
