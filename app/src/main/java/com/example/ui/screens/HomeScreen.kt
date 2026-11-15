package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Section
import com.example.ui.components.BandHeader
import com.example.ui.theme.StarryDark
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToLogin: () -> Unit
) {
    val activeSection by viewModel.activeSection.collectAsStateWithLifecycle()
    val currentUsername by viewModel.currentUsername.collectAsStateWithLifecycle()
    val selectedCharacterIndex by viewModel.selectedCharacterIndex.collectAsStateWithLifecycle()
    val activeMvTrack by viewModel.activeMvTrack.collectAsStateWithLifecycle()

    val sections = Section.entries
    val pagerState = rememberPagerState(
        initialPage = activeSection.ordinal,
        pageCount = { sections.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.setActiveSection(sections[page])
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    val scrollToSection: (Section) -> Unit = { target ->
        coroutineScope.launch {
            pagerState.animateScrollToPage(target.ordinal)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 680.dp

        Scaffold(
            containerColor = StarryDark,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            },
            bottomBar = {
                if (!isWide) {
                    NavigationBar(
                        containerColor = Color(0xEE141418),
                        contentColor = Color.White,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        sections.forEach { section ->
                            val selected = section == activeSection
                            val icon = when (section) {
                                Section.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                                Section.CHARACTERS -> if (selected) Icons.Filled.Groups else Icons.Outlined.Groups
                                Section.MUSIC -> if (selected) Icons.Filled.MusicNote else Icons.Outlined.MusicNote
                                Section.PHOTOS -> if (selected) Icons.Filled.PhotoLibrary else Icons.Outlined.PhotoLibrary
                                Section.ABOUT -> if (selected) Icons.Filled.Info else Icons.Outlined.Info
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = { scrollToSection(section) },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = section.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = section.title,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = section.accentColor,
                                    selectedTextColor = section.accentColor,
                                    indicatorColor = section.accentColor.copy(alpha = 0.16f),
                                    unselectedIconColor = Color.White.copy(alpha = 0.65f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier.testTag("bottom_nav_${section.routeKey}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                VerticalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (sections[page]) {
                        Section.HOME -> HeroSection(
                            onExploreCharacters = { scrollToSection(Section.CHARACTERS) }
                        )

                        Section.CHARACTERS -> CharacterInfoSection(
                            selectedIndex = selectedCharacterIndex,
                            onSelectCharacter = viewModel::selectCharacter
                        )

                        Section.MUSIC -> {
                            val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
                            val likedSongs by viewModel.likedSongs.collectAsStateWithLifecycle()
                            MusicPlayerSection(
                                playbackState = playbackState,
                                likedSongs = likedSongs,
                                onSwitchPlaylist = viewModel::switchPlaylist,
                                onSelectTrack = viewModel::selectTrack,
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onNextTrack = viewModel::nextTrack,
                                onPreviousTrack = viewModel::previousTrack,
                                onWeightedRandom = viewModel::playWeightedRandom,
                                onToggleLike = viewModel::toggleLikeActiveTrack,
                                onOpenMv = viewModel::openMvForActiveTrack,
                                onSeekToFraction = viewModel::seekToFraction,
                                onVolumeChange = viewModel::setVolume,
                                onAdjustVolume = viewModel::adjustVolume
                            )
                        }

                        Section.PHOTOS -> {
                            val activePhotoKey by viewModel.activePhotoKey.collectAsStateWithLifecycle()
                            val onTogglePhoto = remember(viewModel) { viewModel::toggleGalleryPhoto }
                            PhotosSection(
                                galleryRows = viewModel.galleryRows,
                                activePhotoKey = activePhotoKey,
                                onTogglePhoto = onTogglePhoto
                            )
                        }

                        Section.ABOUT -> AboutSection()
                    }
                }

                BandHeader(
                    activeSection = activeSection,
                    showInlineNav = isWide,
                    username = currentUsername,
                    onSectionClick = scrollToSection,
                    onLoginClick = onNavigateToLogin,
                    onLogoutClick = viewModel::logout,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    activeMvTrack?.let { track ->
        MvPlayerDialog(
            track = track,
            onDismiss = viewModel::closeMv
        )
    }
}
