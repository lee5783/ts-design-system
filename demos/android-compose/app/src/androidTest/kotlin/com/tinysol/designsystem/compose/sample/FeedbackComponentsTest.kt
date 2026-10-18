package com.tinysol.designsystem.compose.sample

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tinysol.designsystem.compose.sample.components.AlertPageTags
import com.tinysol.designsystem.compose.sample.components.SpinnerPageTags
import com.tinysol.designsystem.core.style.TSSpinnerSize
import com.tinysol.designsystem.core.style.TSSpinnerTone
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.tinysol.designsystem.compose.sample.components.ToastPageTags
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.sample.support.sample.SampleData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedbackComponentsTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun ComposeTestRule.scrollTo(tag: String) {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
    }

    private fun ComposeTestRule.tap(tag: String) {
        scrollTo(tag)
        onNodeWithTag(tag).performClick()
        waitForIdle()
    }

    private fun ComposeTestRule.assertCount(tag: String, count: Int) {
        scrollTo(tag)
        onNodeWithTag(tag).assertTextEquals(resources.getQuantityString(DemoR.plurals.demo_component_tap_count, count, count))
    }

    @Test
    fun spinnerPageShowsEverySizeAndTone() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Spinner)
            TSSpinnerSize.entries.forEach { size ->
                composeRule.onNodeWithTag(SpinnerPageTags.size(size)).assertExists()
            }
            TSSpinnerTone.entries.forEach { tone ->
                composeRule.onNodeWithTag(SpinnerPageTags.tone(tone)).assertExists()
            }
            val messages = resources.getString(DemoR.string.demo_sample_loading_messages)
            composeRule.scrollTo(SpinnerPageTags.LABELLED)
            composeRule.onNodeWithTag(SpinnerPageTags.LABELLED).assertExists()
            composeRule.onNodeWithTag(SpinnerPageTags.IN_CONTEXT).assertTextEquals(messages)
        }
    }

    @Test
    fun alertPlaygroundActionIsSeparateFromTheMessage() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Alert)
            composeRule.tap(AlertPageTags.PLAYGROUND_ACTION)
            composeRule.scrollTo(AlertPageTags.PLAYGROUND_ALERT)
            composeRule.onNodeWithTag(AlertPageTags.PLAYGROUND_ALERT).assertIsDisplayed()

            composeRule.tap(AlertPageTags.PLAYGROUND_RETRY)
            composeRule.assertCount(AlertPageTags.PLAYGROUND_COUNT, 1)
        }
    }

    @Test
    fun alertDismissRemovesItAndShowAgainBringsItBack() {
        val dismiss = hasClickAction() and hasAnyAncestor(hasTestTag(AlertPageTags.PLAYGROUND_ALERT))
        launchDemo().use {
            composeRule.openPage(DemoPageId.Alert)
            composeRule.scrollTo(AlertPageTags.PLAYGROUND_ALERT)
            composeRule.onNodeWithTag(AlertPageTags.PLAYGROUND_SHOW).assertIsNotEnabled()

            composeRule.onNode(dismiss).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(AlertPageTags.PLAYGROUND_ALERT).assertDoesNotExist()

            composeRule.scrollTo(AlertPageTags.PLAYGROUND_SHOW)
            composeRule.onNodeWithTag(AlertPageTags.PLAYGROUND_SHOW).assertIsEnabled()
            composeRule.tap(AlertPageTags.PLAYGROUND_SHOW)
            composeRule.scrollTo(AlertPageTags.PLAYGROUND_ALERT)
            composeRule.onNodeWithTag(AlertPageTags.PLAYGROUND_ALERT).assertIsDisplayed()

            composeRule.tap(AlertPageTags.PLAYGROUND_DISMISSIBLE)
            assertEquals(0, composeRule.onAllNodes(dismiss).fetchSemanticsNodes().size)
        }
    }

    private fun ComposeTestRule.awaitToast(message: String) {
        waitUntil(TOAST_WAIT_MILLIS) { onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun ComposeTestRule.awaitNoToast(message: String) {
        waitUntil(TOAST_WAIT_MILLIS) { onAllNodesWithText(message).fetchSemanticsNodes().isEmpty() }
    }

    @Test
    fun queuedToastsShowOneAtATimeInOrder() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Toast)
            composeRule.tap(ToastPageTags.QUEUE_SHOW)

            val messages = listOf(
                DemoR.string.demo_sample_photo_uploaded,
                DemoR.string.demo_sample_album_shared,
                DemoR.string.demo_sample_backup_complete,
            ).map(resources::getString)
            messages.forEachIndexed { index, message ->
                composeRule.awaitToast(message)
                messages.filterIndexed { other, _ -> other > index }.forEach {
                    assertEquals(0, composeRule.onAllNodesWithText(it).fetchSemanticsNodes().size)
                }
            }
            messages.forEach { composeRule.awaitNoToast(it) }
        }
    }

    @Test
    fun toastUndoActionFiresAndDismisses() {
        val undo = resources.getString(DemoR.string.demo_sample_undo)
        val deleted = resources.getString(SampleData.message("deleted").titleRes)
        launchDemo().use {
            composeRule.openPage(DemoPageId.Toast)
            composeRule.tap(ToastPageTags.PLAYGROUND_SHOW)
            composeRule.awaitToast(deleted)

            composeRule.onNode(hasText(undo) and !hasAnyAncestor(hasScrollToNodeAction())).performClick()
            composeRule.awaitNoToast(deleted)
            composeRule.assertCount(ToastPageTags.PLAYGROUND_COUNT, 1)
        }
    }

    @Test
    fun toastIsShownInsideAnOpenSheetAndPassesTouchesThrough() {
        val title = resources.getString(DemoR.string.demo_sample_toast_sheet_title)
        launchDemo().use {
            composeRule.openPage(DemoPageId.Toast)
            composeRule.tap(ToastPageTags.SHEET_OPEN)
            composeRule.waitUntil(TOAST_WAIT_MILLIS) { composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }

            composeRule.onNodeWithText(resources.getString(DemoR.string.demo_sample_copy_link)).performClick()
            val linkCopied = resources.getString(DemoR.string.demo_sample_link_copied)
            composeRule.awaitToast(linkCopied)
            composeRule.onNode(hasText(linkCopied) and hasAnyAncestor(isDialog()))
                .assertIsDisplayed()

            composeRule.onNode(isDialog()).performTouchInput { click(Offset(centerX, SCRIM_TAP_Y)) }
            composeRule.waitUntil(TOAST_WAIT_MILLIS) { composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isEmpty() }
        }
    }

    private companion object {
        const val TOAST_WAIT_MILLIS = 8_000L
        const val SCRIM_TAP_Y = 200f
    }
}
