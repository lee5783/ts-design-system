package com.tinysol.designsystem.compose.components.toast

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.compose.components.dialog.TSDialog
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.core.logic.TSDialogPresenter
import com.tinysol.designsystem.core.logic.TSToastQueue
import com.tinysol.designsystem.core.logic.TSToastTimeouts
import com.tinysol.designsystem.core.logic.TSToastTone
import com.tinysol.designsystem.core.style.TSDialogVariant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSToastTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val scheduler = ManualToastScheduler()
    private val queue = TSToastQueue(TSToastTimeouts.default)
    private val state = TSToastState(queue, scheduler)
    private var actions = 0

    private fun setContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            TSTheme(darkTheme = false) { content() }
        }
    }

    private fun setHost(bottomInset: Dp = 0.dp, content: @Composable () -> Unit = {}) {
        setContent {
            TSToastHost(state, Modifier.fillMaxSize().testTag("host"), bottomInset = bottomInset) { content() }
        }
    }

    private fun advance(millis: Long) {
        composeRule.runOnIdle { scheduler.advanceBy(millis) }
        composeRule.waitForIdle()
    }

    private fun shownMessages(): List<String> =
        composeRule.onAllNodesWithTag(TSToastTestTags.MESSAGE, useUnmergedTree = true).fetchSemanticsNodes().map {
            it.children.mapNotNull { child -> child.config.getOrNull(SemanticsProperties.Text)?.single()?.text }.single()
        }

    @Test
    fun iconShowsForEveryToneExceptNeutral() {
        setContent {
            Column {
                TSToastTone.entries.forEach { TSToast("Saved", tone = it) }
            }
        }

        composeRule.onAllNodesWithTag(TSToastTestTags.ICON, useUnmergedTree = true).assertCountEquals(4)
    }

    @Test
    fun actionIsAButtonThatFires() {
        setContent { TSToast("Message deleted", actionLabel = "Undo", onAction = { actions++ }) }

        composeRule.onNodeWithText("Undo").performClick()

        assertEquals(1, actions)
    }

    @Test
    fun noActionWithoutAHandler() {
        setContent { TSToast("Message deleted", actionLabel = "Undo") }

        composeRule.onAllNodesWithTag(TSToastTestTags.ACTION, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "w600dp-h800dp")
    fun geometryMatchesTheSpec() {
        setContent {
            Column {
                TSToast("Saved", modifier = Modifier.testTag("plain"))
                TSToast("Message deleted", actionLabel = "Undo", onAction = {}, modifier = Modifier.testTag("action"))
                Box(Modifier.width(600.dp)) {
                    TSToast("A".repeat(200), modifier = Modifier.testTag("long"))
                }
            }
        }

        composeRule.onNodeWithTag("plain").assertHeightIsEqualTo(44.dp)
        composeRule.onNodeWithTag("action").assertHeightIsEqualTo(44.dp)
        composeRule.onNodeWithTag(TSToastTestTags.ACTION, useUnmergedTree = true).assertTouchHeightIsEqualTo(48.dp)
        assertEquals(420.dp, composeRule.onNodeWithTag("long").getBoundsInRoot().width)
    }

    @Test
    fun hostShowsQueuedToastsInOrder() {
        setHost()
        composeRule.runOnIdle {
            listOf("Project archived", "Link copied", "Saved").forEach { state.show(it) }
        }
        composeRule.waitForIdle()

        val seen = mutableListOf<List<String>>()
        repeat(3) {
            seen += shownMessages()
            advance(4_000)
        }
        seen += shownMessages()

        assertEquals(listOf(listOf("Project archived"), listOf("Link copied"), listOf("Saved"), emptyList()), seen)
    }

    @Test
    fun autoDismissTimingUsesTheInjectedClock() {
        setHost()
        composeRule.runOnIdle { state.show("Saved") }
        composeRule.waitForIdle()

        advance(3_999)
        assertEquals(listOf("Saved"), shownMessages())
        advance(1)
        assertEquals(emptyList<String>(), shownMessages())
    }

    @Test
    fun hostActionRunsTheHandlerAndShowsTheNextToast() {
        setHost()
        composeRule.runOnIdle {
            state.show("Message deleted", actionLabel = "Undo", onAction = { actions++ })
            state.show("Saved")
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Undo").performClick()
        composeRule.waitForIdle()

        assertEquals(1, actions)
        assertEquals(listOf("Saved"), shownMessages())
    }

    @Test
    fun hostProvidesItsStateAndKeepsContentInteractive() {
        var provided: TSToastState? = null
        setHost {
            provided = LocalTSToastState.current
            BasicText("Content", Modifier.testTag("content"))
        }
        composeRule.runOnIdle { state.show("Saved") }
        composeRule.waitForIdle()

        assertEquals(state, provided)
        composeRule.onNodeWithTag("content").assertIsDisplayed()
    }

    @Test
    fun bottomInsetLiftsTheToast() {
        setHost(bottomInset = 56.dp)
        composeRule.runOnIdle { state.show("Saved") }
        composeRule.waitForIdle()

        val host = composeRule.onNodeWithTag("host").getBoundsInRoot()
        val toast = composeRule.onNodeWithTag(TSToastTestTags.TOAST, useUnmergedTree = true).getBoundsInRoot()
        assertEquals(host.bottom - 56.dp - 16.dp, toast.bottom)
        assertEquals((host.left + host.right) / 2, (toast.left + toast.right) / 2)
    }

    @Test
    fun toastIsShownInsideAnOpenSheet() {
        assertToastInsideDialog(TSDialogVariant.Sheet)
    }

    @Test
    fun toastIsShownInsideAnOpenCenterDialog() {
        assertToastInsideDialog(TSDialogVariant.Center)
    }

    private fun assertToastInsideDialog(variant: TSDialogVariant) {
        setHost {
            TSDialog(
                isPresented = true,
                onPresentedChange = {},
                title = "Share",
                message = "Anyone with the link can view.",
                variant = variant,
                presenter = TSDialogPresenter(),
            )
        }
        composeRule.runOnIdle { state.show("Link copied", actionLabel = "Undo", onAction = { actions++ }) }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TSToastTestTags.TOAST, useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag(TSToastHostTestTags.LAYER, useUnmergedTree = true).assertCountEquals(2)
        composeRule.onNodeWithTag(TSToastTestTags.TOAST, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Undo").performClick()
        composeRule.waitForIdle()
        assertEquals(1, actions)
        composeRule.onAllNodesWithTag(TSToastTestTags.TOAST, useUnmergedTree = true).assertCountEquals(0)
    }
}
