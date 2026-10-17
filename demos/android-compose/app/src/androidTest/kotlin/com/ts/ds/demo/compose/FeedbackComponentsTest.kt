package com.ts.ds.demo.compose

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.compose.components.AlertPageTags
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.ts.ds.demo.compose.components.ToastPageTags
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedbackComponentsTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    private val offline = SampleData.message("offline")

    private val playgroundLabel: String
        get() = listOf(
            resources.getString(DemoR.string.demo_sample_information),
            resources.getString(offline.titleRes),
            resources.getString(offline.bodyRes),
        ).joinToString(", ")

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
    fun alertMessageIsOneElementWithSeparateAction() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Alert)
            composeRule.tap(AlertPageTags.PLAYGROUND_ACTION)
            composeRule.scrollTo(AlertPageTags.PLAYGROUND_ALERT)

            val message = composeRule.onNodeWithContentDescription(playgroundLabel).fetchSemanticsNode()
            assertNull(message.config.getOrNull(SemanticsProperties.Text))
            assertNull(message.config.getOrNull(SemanticsProperties.LiveRegion))

            composeRule.tap(AlertPageTags.PLAYGROUND_RETRY)
            composeRule.assertCount(AlertPageTags.PLAYGROUND_COUNT, 1)
        }
    }

    @Test
    fun alertDismissRemovesItAndShowAgainBringsItBack() {
        val dismiss = "Dismiss"
        launchDemo().use {
            composeRule.openPage(DemoPageId.Alert)
            composeRule.scrollTo(AlertPageTags.PLAYGROUND_ALERT)
            composeRule.onNodeWithTag(AlertPageTags.PLAYGROUND_SHOW).assertIsNotEnabled()

            composeRule.onNodeWithContentDescription(dismiss).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(AlertPageTags.PLAYGROUND_ALERT).assertDoesNotExist()

            composeRule.scrollTo(AlertPageTags.PLAYGROUND_SHOW)
            composeRule.onNodeWithTag(AlertPageTags.PLAYGROUND_SHOW).assertIsEnabled()
            composeRule.tap(AlertPageTags.PLAYGROUND_SHOW)
            composeRule.scrollTo(AlertPageTags.PLAYGROUND_ALERT)
            composeRule.onNodeWithContentDescription(playgroundLabel)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))

            composeRule.tap(AlertPageTags.PLAYGROUND_DISMISSIBLE)
            assertEquals(0, composeRule.onAllNodesWithContentDescription(dismiss).fetchSemanticsNodes().size)
        }
    }

    private val hostedToast = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite) and
        !hasTestTag(ToastPageTags.PLAYGROUND_COUNT)

    private fun successLabel(message: Int): String =
        "${resources.getString(DemoR.string.demo_sample_success)}, ${resources.getString(message)}"

    private fun ComposeTestRule.awaitToast(label: String) {
        waitUntil(TOAST_WAIT_MILLIS) {
            onAllNodes(hasContentDescription(label) and hasAnyAncestor(hostedToast)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun ComposeTestRule.awaitNoToast() {
        waitUntil(TOAST_WAIT_MILLIS) { onAllNodes(hostedToast).fetchSemanticsNodes().isEmpty() }
    }

    @Test
    fun queuedToastsShowOneAtATimeInOrder() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Toast)
            composeRule.tap(ToastPageTags.QUEUE_SHOW)

            listOf(
                DemoR.string.demo_sample_photo_uploaded,
                DemoR.string.demo_sample_album_shared,
                DemoR.string.demo_sample_backup_complete,
            ).forEach { message ->
                composeRule.awaitToast(successLabel(message))
                composeRule.waitUntil(TOAST_WAIT_MILLIS) { composeRule.onAllNodes(hostedToast).fetchSemanticsNodes().size == 1 }
            }
            composeRule.awaitNoToast()
        }
    }

    @Test
    fun toastUndoActionFiresAndDismisses() {
        val undo = resources.getString(DemoR.string.demo_sample_undo)
        launchDemo().use {
            composeRule.openPage(DemoPageId.Toast)
            composeRule.tap(ToastPageTags.PLAYGROUND_SHOW)
            composeRule.awaitToast(resources.getString(SampleData.message("deleted").titleRes))

            composeRule.onNode(hasText(undo) and hasAnyAncestor(hostedToast)).performClick()
            composeRule.awaitNoToast()
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
            composeRule.awaitToast(successLabel(DemoR.string.demo_sample_link_copied))
            composeRule.onNode(hasContentDescription(successLabel(DemoR.string.demo_sample_link_copied)) and hasAnyAncestor(isDialog()))
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
