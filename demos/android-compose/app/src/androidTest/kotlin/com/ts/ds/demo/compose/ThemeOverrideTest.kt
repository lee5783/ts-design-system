package com.ts.ds.demo.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.core.view.WindowCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.compose.components.DialogPageTags
import com.ts.ds.demo.compose.components.ToastPageTags
import com.ts.ds.demo.compose.shell.DemoTags
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.support.settings.DemoLaunchOverrides
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class ThemeOverrideTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val resources = context.resources

    @After
    fun restoreDevice() {
        setDeviceDark(false)
        instrumentation.runOnMainSync { settingsStore.reset() }
    }

    private fun shell(command: String) {
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    }

    private fun setDeviceDark(dark: Boolean) {
        shell("cmd uimode night ${if (dark) "yes" else "no"}")
        instrumentation.waitForIdleSync()
    }

    private fun launchWithTheme(theme: DemoTheme): ActivityScenario<MainActivity> {
        instrumentation.runOnMainSync { settingsStore.update { DemoSettings() } }
        val intent = Intent(context, MainActivity::class.java).putExtra(DemoLaunchOverrides.THEME_EXTRA, theme.id)
        return ActivityScenario.launch(intent)
    }

    private fun ActivityScenario<MainActivity>.openLink(theme: DemoTheme) {
        val link = Intent(Intent.ACTION_VIEW, Uri.parse(DemoLaunchOverrides.settingsLink(DemoLaunchOverrides.COMPOSE_SCHEME, theme)))
            .setPackage(context.packageName)
        val handler = requireNotNull(context.packageManager.resolveActivity(link, 0)).activityInfo
        assertEquals(MainActivity::class.java.name, handler.name)
        onActivity { activity -> instrumentation.callActivityOnNewIntent(activity, link) }
        composeRule.waitUntil(WAIT_MILLIS) { settingsStore.settings.theme == theme }
        composeRule.waitForIdle()
    }

    private fun SemanticsNodeInteraction.isDark(): Boolean {
        val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            captureToImage().toPixelMap()[SAMPLE_OFFSET, SAMPLE_OFFSET]
        } else {
            screenPixel(fetchSemanticsNode().positionOnScreen)
        }
        return color.luminance() < 0.5f
    }

    private fun screenPixel(origin: Offset): Color {
        val screenshot = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        try {
            return Color(screenshot.getPixel(origin.x.roundToInt() + SAMPLE_OFFSET, origin.y.roundToInt() + SAMPLE_OFFSET))
        } finally {
            screenshot.recycle()
        }
    }

    private fun ActivityScenario<MainActivity>.lightStatusBars(): Boolean {
        var light = false
        onActivity { light = WindowCompat.getInsetsController(it.window, it.window.decorView).isAppearanceLightStatusBars }
        return light
    }

    private fun themeLabel(theme: DemoTheme): String {
        val mode = resources.getString(
            when (theme) {
                DemoTheme.System -> DemoR.string.demo_settings_theme_system
                DemoTheme.Light -> DemoR.string.demo_settings_theme_light
                DemoTheme.Dark -> DemoR.string.demo_settings_theme_dark
            },
        )
        return resources.getString(DemoR.string.demo_action_theme, mode)
    }

    private fun ComposeTestRule.assertThemeAction(theme: DemoTheme) {
        onNodeWithTag(DemoTags.ACTION_THEME)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(themeLabel(theme))))
    }

    private fun ComposeTestRule.tap(tag: String) {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
        onNodeWithTag(tag).performClick()
        waitForIdle()
    }

    private fun ComposeTestRule.awaitText(text: String) {
        waitUntil(WAIT_MILLIS) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun ComposeTestRule.awaitDescription(label: String) {
        waitUntil(WAIT_MILLIS) { onAllNodes(hasContentDescription(label), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun deviceLightAppLaunchedDarkRendersDark() {
        setDeviceDark(false)
        launchWithTheme(DemoTheme.Dark).use { scenario ->
            composeRule.waitForIdle()
            assertEquals(DemoTheme.Dark, settingsStore.settings.theme)
            composeRule.onNodeWithTag(DemoTags.CATALOG).assertExistsAndIs(dark = true)
            assertEquals(false, scenario.lightStatusBars())
            composeRule.assertThemeAction(DemoTheme.Dark)

            composeRule.openPage(DemoPageId.Button)
            composeRule.onNodeWithTag(DemoTags.screen(DemoPageId.Button)).assertExistsAndIs(dark = true)
            composeRule.assertThemeAction(DemoTheme.Dark)
        }
    }

    @Test
    fun deviceDarkAppLaunchedLightRendersLight() {
        setDeviceDark(true)
        launchWithTheme(DemoTheme.Light).use { scenario ->
            composeRule.waitForIdle()
            assertEquals(DemoTheme.Light, settingsStore.settings.theme)
            composeRule.onNodeWithTag(DemoTags.CATALOG).assertExistsAndIs(dark = false)
            assertEquals(true, scenario.lightStatusBars())

            composeRule.openPage(DemoPageId.Card)
            composeRule.onNodeWithTag(DemoTags.screen(DemoPageId.Card)).assertExistsAndIs(dark = false)
        }
    }

    @Test
    fun systemFollowsTheDeviceAgain() {
        setDeviceDark(true)
        launchWithTheme(DemoTheme.System).use { scenario ->
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(DemoTags.CATALOG).assertExistsAndIs(dark = true)
            setDeviceDark(false)
            composeRule.onNodeWithTag(DemoTags.CATALOG).assertExistsAndIs(dark = false)
            assertEquals(true, scenario.lightStatusBars())
        }
    }

    @Test
    fun toggleCyclesOnEveryScreenAndPersistsAcrossRelaunch() {
        setDeviceDark(false)
        launchDemo().use {
            composeRule.assertThemeAction(DemoTheme.System)
            composeRule.onNodeWithTag(DemoTags.ACTION_THEME).performClick()
            composeRule.assertThemeAction(DemoTheme.Light)
            assertEquals(DemoTheme.Light, settingsStore.settings.theme)

            composeRule.openPage(DemoPageId.Toast)
            composeRule.onNodeWithTag(DemoTags.ACTION_THEME).performClick()
            composeRule.assertThemeAction(DemoTheme.Dark)
            composeRule.onNodeWithTag(DemoTags.screen(DemoPageId.Toast)).assertExistsAndIs(dark = true)

            composeRule.onNodeWithTag(DemoTags.ACTION_SETTINGS).performClick()
            composeRule.onNodeWithTag(DemoTags.theme(DemoTheme.Dark)).assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        }
        val persisted = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(PREFERENCES_KEY, null)
        assertEquals(DemoTheme.Dark, DemoSettings.decode(persisted).theme)
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.assertThemeAction(DemoTheme.Dark)
            composeRule.onNodeWithTag(DemoTags.CATALOG).assertExistsAndIs(dark = true)
            composeRule.onNodeWithTag(DemoTags.ACTION_THEME).performClick()
            composeRule.assertThemeAction(DemoTheme.System)
            assertEquals(DemoTheme.System, settingsStore.settings.theme)
        }
    }

    @Test
    fun openCenterDialogFollowsALiveFlipWithoutClosing() {
        setDeviceDark(false)
        val title = resources.getString(SampleData.message("confirm-delete").titleRes)
        launchWithTheme(DemoTheme.Light).use { scenario ->
            composeRule.openPage(DemoPageId.Dialog)
            composeRule.tap(DialogPageTags.OPEN_CENTER)
            composeRule.awaitText(title)
            composeRule.onNodeWithText(title).assertExistsAndIs(dark = false)

            scenario.openLink(DemoTheme.Dark)
            composeRule.onNodeWithText(title).assertExistsAndIs(dark = true)
            assertEquals(false, scenario.lightStatusBars())
        }
    }

    @Test
    fun openSheetAndToastFollowALiveFlipWithoutClosing() {
        setDeviceDark(false)
        val title = resources.getString(DemoR.string.demo_sample_toast_sheet_title)
        val toast = "${resources.getString(DemoR.string.demo_sample_success)}, ${resources.getString(DemoR.string.demo_sample_link_copied)}"
        launchWithTheme(DemoTheme.Dark).use { scenario ->
            composeRule.openPage(DemoPageId.Toast)
            composeRule.tap(ToastPageTags.SHEET_OPEN)
            composeRule.awaitText(title)
            composeRule.onNodeWithText(resources.getString(DemoR.string.demo_sample_copy_link)).performClick()
            composeRule.awaitDescription(toast)
            composeRule.onNodeWithText(title).assertExistsAndIs(dark = true)

            scenario.openLink(DemoTheme.Light)
            composeRule.onNodeWithText(title).assertExistsAndIs(dark = false)
            composeRule.onNode(hasContentDescription(toast), useUnmergedTree = true).assertExists()
        }
    }

    @Test
    fun toastKeepsItsQueuePositionAcrossALiveFlip() {
        setDeviceDark(false)
        val first = "${resources.getString(DemoR.string.demo_sample_success)}, ${resources.getString(DemoR.string.demo_sample_photo_uploaded)}"
        val second = "${resources.getString(DemoR.string.demo_sample_success)}, ${resources.getString(DemoR.string.demo_sample_album_shared)}"
        launchWithTheme(DemoTheme.Light).use { scenario ->
            composeRule.openPage(DemoPageId.Toast)
            composeRule.tap(ToastPageTags.QUEUE_SHOW)
            composeRule.awaitDescription(first)

            scenario.openLink(DemoTheme.Dark)
            composeRule.onNode(hasContentDescription(first), useUnmergedTree = true).assertExists()
            assertEquals(0, composeRule.onAllNodes(hasContentDescription(second), useUnmergedTree = true).fetchSemanticsNodes().size)
            composeRule.awaitDescription(second)
        }
    }

    private fun SemanticsNodeInteraction.assertExistsAndIs(dark: Boolean) {
        assertExists()
        composeRule.waitUntil(WAIT_MILLIS) { isDark() == dark }
    }

    private companion object {
        const val WAIT_MILLIS = 8_000L
        const val SAMPLE_OFFSET = 2
        const val PREFERENCES = "demo_settings"
        const val PREFERENCES_KEY = "settings"
    }
}
