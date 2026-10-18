package com.tinysol.designsystem.compose.sample

import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tinysol.designsystem.compose.sample.components.BottomNavPageTags
import com.tinysol.designsystem.compose.sample.components.DialogPageTags
import com.tinysol.designsystem.compose.sample.shell.DemoTags
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.sample.support.sample.SampleData
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OverlayFlowsTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val resources = instrumentation.targetContext.resources
    private val confirmTitle = resources.getString(SampleData.message("confirm-delete").titleRes)

    @After
    fun resetSettings() {
        instrumentation.runOnMainSync { settingsStore.reset() }
    }

    private fun result(value: String): String = resources.getString(DemoR.string.demo_component_dialog_result, value)

    private fun ComposeTestRule.tap(tag: String) {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
        onNodeWithTag(tag).performClick()
        waitForIdle()
    }

    private fun ComposeTestRule.assertDialogShown(title: String) {
        waitUntil(5_000) { onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun ComposeTestRule.assertDialogGone(title: String) {
        waitUntil(5_000) { onAllNodesWithText(title).fetchSemanticsNodes().isEmpty() }
    }

    private fun ComposeTestRule.assertResult(value: String) {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(DialogPageTags.RESULT))
        onNodeWithTag(DialogPageTags.RESULT).assertTextEquals(result(value))
    }

    private fun ComposeTestRule.count(tag: String): Int {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
        val text = onNodeWithTag(tag).fetchSemanticsNode()
            .config[SemanticsProperties.Text].joinToString { it.text }
        return text.filter(Char::isDigit).toInt()
    }

    private fun shell(command: String) {
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    }

    private fun edgeSwipe() {
        Thread.sleep(SETTLE_MILLIS)
        val y = instrumentation.targetContext.resources.displayMetrics.heightPixels / 2
        val script = buildString {
            appendLine("input motionevent DOWN 2 $y")
            listOf(40, 80, 140, 200, 260, 320, 380, 440, 500).forEach { appendLine("input motionevent MOVE $it $y") }
            appendLine("input motionevent UP 500 $y")
        }
        val file = File(requireNotNull(instrumentation.targetContext.getExternalFilesDir(null)), "edge-swipe.sh")
        file.writeText(script)
        shell("sh ${file.absolutePath}")
        Thread.sleep(SETTLE_MILLIS)
    }

    private val gestureNavigation: Boolean
        get() = Settings.Secure.getInt(instrumentation.targetContext.contentResolver, "navigation_mode", 0) == GESTURAL_NAVIGATION

    @Test
    fun centerDialogOpensAndScrimDismisses() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Dialog)
            composeRule.tap(DialogPageTags.OPEN_CENTER)
            composeRule.assertDialogShown(confirmTitle)
            composeRule.onNode(isDialog()).performTouchInput { click(Offset(20f, 20f)) }
            composeRule.assertDialogGone(confirmTitle)
            composeRule.assertResult(resources.getString(DemoR.string.demo_component_dismissed))
        }
    }

    @Test
    fun sheetOpensActionClosesAndBackDismisses() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Dialog)
            composeRule.tap(DialogPageTags.OPEN_SHEET)
            composeRule.assertDialogShown(confirmTitle)
            composeRule.onNodeWithText(resources.getString(DemoR.string.demo_sample_cancel)).performClick()
            composeRule.assertDialogGone(confirmTitle)
            composeRule.assertResult(resources.getString(DemoR.string.demo_sample_cancel))

            composeRule.tap(DialogPageTags.OPEN_SHEET)
            composeRule.assertDialogShown(confirmTitle)
            Espresso.pressBack()
            composeRule.assertDialogGone(confirmTitle)
            composeRule.assertResult(resources.getString(DemoR.string.demo_component_dismissed))
        }
    }

    @Test
    fun backDismissesTheCenterDialog() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Dialog)
            composeRule.tap(DialogPageTags.OPEN_CENTER)
            composeRule.assertDialogShown(confirmTitle)
            Espresso.pressBack()
            composeRule.assertDialogGone(confirmTitle)
            composeRule.assertResult(resources.getString(DemoR.string.demo_component_dismissed))
        }
    }

    @Test
    fun predictiveBackGestureDismissesCenterAndSheet() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && gestureNavigation)
        launchDemo().use {
            composeRule.openPage(DemoPageId.Dialog)
            listOf(DialogPageTags.OPEN_CENTER, DialogPageTags.OPEN_SHEET).forEach { opener ->
                composeRule.tap(opener)
                composeRule.assertDialogShown(confirmTitle)
                edgeSwipe()
                composeRule.assertDialogGone(confirmTitle)
                composeRule.assertResult(resources.getString(DemoR.string.demo_component_dismissed))
            }
        }
    }

    @Test
    fun secondDialogIsQueuedWhileOneIsOpenAndShownAfterItCloses() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Dialog)
            val refusedBefore = composeRule.count(DialogPageTags.REFUSED)
            val stackingTitle = resources.getString(DemoR.string.demo_sample_stacking_title)
            composeRule.tap(DialogPageTags.OPEN_STACKING)
            composeRule.assertDialogShown(stackingTitle)
            composeRule.onNodeWithText(resources.getString(DemoR.string.demo_component_open_another)).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText(stackingTitle).assertExists()
            assertEquals(0, composeRule.onAllNodesWithText(confirmTitle).fetchSemanticsNodes().size)

            composeRule.onNodeWithText(resources.getString(DemoR.string.demo_sample_close)).performClick()
            composeRule.assertDialogGone(stackingTitle)
            composeRule.assertDialogShown(confirmTitle)
            composeRule.onNodeWithText(resources.getString(DemoR.string.demo_sample_cancel)).performClick()
            composeRule.assertDialogGone(confirmTitle)
            assertEquals(refusedBefore, composeRule.count(DialogPageTags.REFUSED))
            assertEquals(0, composeRule.count(DialogPageTags.QUEUED))
        }
    }

    @Test
    fun deleteActionChainsAnUndoDialog() {
        val deletedTitle = resources.getString(DemoR.string.demo_sample_project_deleted_title)
        launchDemo().use {
            composeRule.openPage(DemoPageId.Dialog)
            composeRule.tap(DialogPageTags.OPEN_CHAIN)
            composeRule.assertDialogShown(confirmTitle)
            composeRule.onNodeWithText(resources.getString(DemoR.string.demo_sample_delete)).performClick()
            composeRule.assertDialogShown(deletedTitle)
            composeRule.assertDialogGone(confirmTitle)

            composeRule.onNodeWithText(resources.getString(DemoR.string.demo_sample_undo)).performClick()
            composeRule.assertDialogGone(deletedTitle)
            composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(DialogPageTags.CHAIN_RESULT))
            composeRule.onNodeWithTag(DialogPageTags.CHAIN_RESULT).assertTextEquals(result(resources.getString(DemoR.string.demo_sample_undo)))
        }
    }

    @Test
    fun bottomNavShellOpensFromItsPageAndBackReturns() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.BottomNav)
            composeRule.tap(BottomNavPageTags.OPEN_SHELL)
            composeRule.onNodeWithText(resources.getString(DemoR.string.demo_component_shell)).assertExists()
            Espresso.pressBack()
            composeRule.onNodeWithTag(DemoTags.screen(DemoPageId.BottomNav)).assertExists()
        }
    }

    private companion object {
        const val GESTURAL_NAVIGATION = 2
        const val SETTLE_MILLIS = 1_000L
    }
}
