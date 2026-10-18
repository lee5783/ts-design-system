package com.tinysol.designsystem.compose.components.dialog

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.core.logic.TSDialogPresenter
import com.tinysol.designsystem.core.style.TSDialogVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class TSDialogSystemBarsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun centerDialogKeepsTheHostsHiddenSystemBars() {
        hideHostBars()
        showDialog(TSDialogVariant.Center)
        assertBarsHidden(dialogWindow())
    }

    @Test
    fun sheetDialogKeepsTheHostsHiddenSystemBars() {
        hideHostBars()
        showDialog(TSDialogVariant.Sheet)
        assertBarsHidden(dialogWindow())
    }

    @Test
    @Config(sdk = [26])
    fun aHostHiddenWithFlagFullscreenHidesTheDialogStatusBar() {
        composeRule.runOnUiThread { composeRule.activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) }
        composeRule.waitForIdle()
        showDialog(TSDialogVariant.Center)
        assertFalse(dialogWindow().decorView.showsBar(WindowInsetsCompat.Type.statusBars(), View.SYSTEM_UI_FLAG_FULLSCREEN))
    }

    @Test
    fun dialogOverAHostWithVisibleBarsKeepsThemVisible() {
        showDialog(TSDialogVariant.Center)
        val window = dialogWindow()
        assertTrue(window.decorView.showsBar(WindowInsetsCompat.Type.statusBars(), View.SYSTEM_UI_FLAG_FULLSCREEN))
        assertTrue(window.decorView.showsBar(WindowInsetsCompat.Type.navigationBars(), View.SYSTEM_UI_FLAG_HIDE_NAVIGATION))
    }

    private fun hideHostBars() {
        composeRule.runOnUiThread {
            val window = composeRule.activity.window
            WindowCompat.getInsetsController(window, window.decorView).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        composeRule.waitForIdle()
        val decor = composeRule.activity.window.decorView
        assertFalse(decor.showsBar(WindowInsetsCompat.Type.statusBars(), View.SYSTEM_UI_FLAG_FULLSCREEN))
    }

    private fun showDialog(variant: TSDialogVariant) {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                TSDialog(
                    isPresented = true,
                    onPresentedChange = {},
                    title = "Immersive",
                    message = "Body",
                    variant = variant,
                    presenter = TSDialogPresenter(),
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Immersive").assertExists()
    }

    private fun dialogWindow(): Window {
        val window = requireNotNull(ShadowDialog.getLatestDialog()?.window)
        assertNotSame(composeRule.activity.window, window)
        return window
    }

    private fun assertBarsHidden(window: Window) {
        val decor = window.decorView
        assertFalse(decor.showsBar(WindowInsetsCompat.Type.statusBars(), View.SYSTEM_UI_FLAG_FULLSCREEN))
        assertFalse(decor.showsBar(WindowInsetsCompat.Type.navigationBars(), View.SYSTEM_UI_FLAG_HIDE_NAVIGATION))
        assertEquals(
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
            WindowCompat.getInsetsController(window, decor).systemBarsBehavior,
        )
    }

    @Suppress("DEPRECATION")
    private fun View.showsBar(type: Int, legacyFlag: Int): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ViewCompat.getRootWindowInsets(this)?.isVisible(type) ?: true
        } else {
            systemUiVisibility and legacyFlag == 0
        }
}
