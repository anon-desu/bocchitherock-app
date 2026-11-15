package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.data.AuthRepository
import com.example.data.BandCatalog
import com.example.data.local.SessionPreferences
import com.example.data.remote.CaptchaChallengeResponse
import com.example.data.remote.CloudflareApiService
import com.example.playback.MusicPlayerController
import com.example.ui.navigation.BocchiNavHost
import com.example.ui.screens.AboutSection
import com.example.ui.screens.BangCaptchaDialog
import com.example.ui.screens.CharacterInfoSection
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PhotosSection
import com.example.ui.theme.BocchiTheme
import com.example.viewmodel.LoginViewModel
import com.example.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BocchiUiBehaviorTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun navHostLoginScreenStaysOpenDuringInteraction() {
        composeRule.mainClock.autoAdvance = false
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val repo = AuthRepository(CloudflareApiService.create(), SessionPreferences(context))
        val controller = MusicPlayerController(context)
        val mainViewModel = MainViewModel(repo, controller, connectPlayer = false)
        val loginFactory = LoginViewModel.Factory(repo)

        composeRule.setContent {
            BocchiTheme {
                BocchiNavHost(
                    mainViewModel = mainViewModel,
                    loginViewModelFactory = loginFactory
                )
            }
        }

        repeat(20) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithTag("header_login_button").performClick()
        repeat(60) { composeRule.mainClock.advanceTimeByFrame() }

        composeRule.onNodeWithTag("auth_submit_button").assertIsDisplayed()
        composeRule.onNodeWithText("Log In").assertIsDisplayed()

        // Interact with username & switch auth mode & background
        composeRule.onNodeWithTag("username_input").performClick()
        composeRule.onNodeWithTag("username_input").performTextInput("bocchi")
        composeRule.onNodeWithTag("login_bg_next").performClick()
        composeRule.onNodeWithTag("switch_auth_mode_text").performClick()
        repeat(120) { composeRule.mainClock.advanceTimeByFrame() }

        // Should still remain on LoginScreen
        composeRule.onNodeWithTag("repassword_input").assertIsDisplayed()
        composeRule.onNodeWithTag("auth_submit_button").assertIsDisplayed()

        // Explicit close returns cleanly to HomeScreen
        composeRule.onNodeWithTag("close_login_button").performClick()
        repeat(90) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithTag("header_login_button").assertIsDisplayed()
    }

    @Test
    fun guitarSvgTogglesLoginAndRegisterMode() {
        composeRule.mainClock.autoAdvance = false
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val repo = AuthRepository(CloudflareApiService.create(), SessionPreferences(context))
        val loginFactory = LoginViewModel.Factory(repo)

        composeRule.setContent {
            BocchiTheme {
                LoginScreen(factory = loginFactory, onNavigateBack = {})
            }
        }

        repeat(30) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithText("Log In").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("repassword_input").fetchSemanticsNodes().isEmpty())

        // Click electric guitar button -> Register mode
        composeRule.onNodeWithTag("guitar_switch_mode_button").performClick()
        repeat(30) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithText("Join Us").assertIsDisplayed()
        composeRule.onNodeWithTag("repassword_input").assertIsDisplayed()

        // Click electric guitar button again -> back to Login mode
        composeRule.onNodeWithTag("guitar_switch_mode_button").performClick()
        repeat(30) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithText("Log In").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("repassword_input").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun photosSectionFlipPersistsUntilTappedAgain() {
        composeRule.mainClock.autoAdvance = false
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val repo = AuthRepository(CloudflareApiService.create(), SessionPreferences(context))
        val controller = MusicPlayerController(context)
        val mainViewModel = MainViewModel(repo, controller, connectPlayer = false)

        composeRule.setContent {
            BocchiTheme {
                val activePhotoKey by mainViewModel.activePhotoKey.collectAsStateWithLifecycle()
                PhotosSection(
                    galleryRows = mainViewModel.galleryRows,
                    activePhotoKey = activePhotoKey,
                    onTogglePhoto = mainViewModel::toggleGalleryPhoto
                )
            }
        }

        repeat(10) { composeRule.mainClock.advanceTimeByFrame() }
        val photoNode = composeRule.onNodeWithTag("gallery_photo_0_1")
        photoNode.assertIsNotSelected()
        val initialWidth = photoNode.fetchSemanticsNode().size.width

        // Tap once: should flip + expand width (~1.6x) and STAY flipped over 2+ seconds (150 frames)
        photoNode.performClick()
        repeat(150) { composeRule.mainClock.advanceTimeByFrame() }
        photoNode.assertIsSelected()
        val expandedWidth = photoNode.fetchSemanticsNode().size.width
        assertTrue(
            "Expected photo card width to expand when flipped (was $initialWidth, became $expandedWidth)",
            expandedWidth > initialWidth
        )

        // Tap same card again: should unflip and collapse back
        photoNode.performClick()
        repeat(45) { composeRule.mainClock.advanceTimeByFrame() }
        photoNode.assertIsNotSelected()
        val collapsedWidth = photoNode.fetchSemanticsNode().size.width
        assertEquals(initialWidth, collapsedWidth)
    }

    @Test
    fun characterInfoShowsCompactPosterOnlyWhenSpacePermitsAndSwitches() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            BocchiTheme {
                var selectedIndex by remember { mutableIntStateOf(0) }
                Box(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    CharacterInfoSection(
                        selectedIndex = selectedIndex,
                        onSelectCharacter = { selectedIndex = it }
                    )
                }
            }
        }

        repeat(20) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithTag("compact_character_poster").assertIsDisplayed()
        composeRule.onNodeWithText(BandCatalog.characters[0].name).assertIsDisplayed()

        composeRule.onNodeWithTag("character_select_2").performClick()
        repeat(40) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithText(BandCatalog.characters[2].name).assertIsDisplayed()
    }

    @Test
    fun characterInfoOmitsCompactPosterWhenSpaceIsTight() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            BocchiTheme {
                Box(modifier = Modifier.size(width = 340.dp, height = 520.dp)) {
                    CharacterInfoSection(
                        selectedIndex = 0,
                        onSelectCharacter = {}
                    )
                }
            }
        }

        repeat(20) { composeRule.mainClock.advanceTimeByFrame() }
        assertTrue(composeRule.onAllNodesWithTag("compact_character_poster").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun aboutSectionRendersGitHubAndSpinnableProducerAvatars() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            BocchiTheme {
                AboutSection()
            }
        }

        repeat(15) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithTag("github_repo_button").assertIsDisplayed()
        composeRule.onNodeWithTag("producer_avatar_Sudoria").performScrollTo().performClick()
        composeRule.onNodeWithTag("producer_avatar_Rquars").performScrollTo().performClick()
        repeat(50) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithTag("producer_avatar_Rquars").assertIsDisplayed()
    }

    @Test
    fun nativeCaptchaDialogDisplaysTilesAndConfirms() {
        composeRule.mainClock.autoAdvance = false
        var confirmed = false
        val mockChallenge = CaptchaChallengeResponse(
            id = "cap-123",
            targetName = "后藤一里",
            images = List(9) { idx -> "https://band.53113007.xyz/img/$idx.jpg" }
        )

        composeRule.setContent {
            BocchiTheme {
                var selected by remember { mutableStateOf(emptySet<Int>()) }
                BangCaptchaDialog(
                    challenge = mockChallenge,
                    selectedIndexes = selected,
                    isLoading = false,
                    errorMessage = null,
                    isConfirmed = false,
                    onToggleIndex = { idx ->
                        selected = if (idx in selected) selected - idx else selected + idx
                    },
                    onRefresh = {},
                    onConfirm = { confirmed = true },
                    onWebCaptchaResult = {},
                    onDismiss = {}
                )
            }
        }

        repeat(15) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithTag("captcha_confirm_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("captcha_tile_0").performClick()
        composeRule.onNodeWithTag("captcha_tile_4").performClick()
        repeat(15) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithTag("captcha_confirm_button").assertIsEnabled()
        composeRule.onNodeWithTag("captcha_confirm_button").performClick()
        assertTrue(confirmed)
        assertEquals("cap-123", mockChallenge.id)
    }
}
