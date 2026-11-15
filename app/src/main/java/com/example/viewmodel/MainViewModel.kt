package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.BandCatalog
import com.example.model.Section
import com.example.model.Track
import com.example.playback.MusicPlayerController
import com.example.playback.PlaybackUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val playerController: MusicPlayerController,
    connectPlayer: Boolean = true
) : ViewModel() {

    val currentUsername: StateFlow<String?> = authRepository.currentUsername
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val likedSongs: StateFlow<Set<String>> = authRepository.likedSongs

    val playbackState: StateFlow<PlaybackUiState> = playerController.uiState

    private val _activeSection = MutableStateFlow(Section.HOME)
    val activeSection: StateFlow<Section> = _activeSection.asStateFlow()

    private val _selectedCharacterIndex = MutableStateFlow(0)
    val selectedCharacterIndex: StateFlow<Int> = _selectedCharacterIndex.asStateFlow()

    private val _activeMvTrack = MutableStateFlow<Track?>(null)
    val activeMvTrack: StateFlow<Track?> = _activeMvTrack.asStateFlow()

    private val _activePhotoKey = MutableStateFlow<Pair<Int, Int>?>(null)
    val activePhotoKey: StateFlow<Pair<Int, Int>?> = _activePhotoKey.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    val galleryRows: List<List<String>> = BandCatalog.buildGalleryRows()

    init {
        if (connectPlayer) {
            playerController.connect()
        }
        viewModelScope.launch {
            authRepository.currentUsername.collect { username ->
                if (!username.isNullOrBlank()) {
                    authRepository.refreshLikes()
                }
            }
        }
    }

    fun setActiveSection(section: Section) {
        _activeSection.value = section
    }

    fun selectCharacter(index: Int) {
        if (index in BandCatalog.characters.indices) {
            _selectedCharacterIndex.value = index
        }
    }

    fun toggleGalleryPhoto(rowIndex: Int, itemIndex: Int) {
        val key = rowIndex to itemIndex
        _activePhotoKey.value = if (_activePhotoKey.value == key) null else key
    }

    fun switchPlaylist(name: String) {
        playerController.switchBrowsedPlaylist(name)
    }

    fun selectTrack(track: Track) {
        playerController.selectTrack(track, autoPlay = true)
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun nextTrack() {
        playerController.nextTrack()
    }

    fun previousTrack() {
        playerController.previousTrack()
    }

    fun playWeightedRandom() {
        playerController.playWeightedRandom(likedSongs.value)
    }

    fun seekToFraction(fraction: Float) {
        playerController.seekToFraction(fraction)
    }

    fun setVolume(volume: Float) {
        playerController.setVolume(volume)
    }

    fun adjustVolume(delta: Float) {
        playerController.adjustVolume(delta)
    }

    fun toggleLikeActiveTrack() {
        val username = currentUsername.value
        if (username.isNullOrBlank()) {
            _toastEvents.tryEmit("请先登录才能收藏歌曲哦！")
            return
        }
        val songName = playbackState.value.activeTrack.name
        viewModelScope.launch {
            val result = authRepository.toggleLike(songName)
            result.fold(
                onSuccess = { isLiked ->
                    _toastEvents.tryEmit(if (isLiked) "已收藏《$songName》" else "已取消收藏《$songName》")
                },
                onFailure = { err ->
                    _toastEvents.tryEmit(err.message ?: "收藏操作失败")
                }
            )
        }
    }

    fun openMvForActiveTrack() {
        val track = playbackState.value.activeTrack
        if (track.bvid.isNotBlank()) {
            _activeMvTrack.value = track
        }
    }

    fun closeMv() {
        _activeMvTrack.value = null
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _toastEvents.tryEmit("已退出登录")
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val playerController: MusicPlayerController
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(authRepository, playerController) as T
        }
    }
}
