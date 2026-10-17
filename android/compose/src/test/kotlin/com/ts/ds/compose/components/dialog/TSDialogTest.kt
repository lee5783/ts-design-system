package com.ts.ds.compose.components.dialog

import androidx.activity.BackEventCompat
import androidx.activity.ComponentDialog
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSDialogPresenter
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSDialogVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val presenter = TSDialogPresenter()
    private var presented by mutableStateOf(true)
    private var dismissals = 0
    private val events = mutableListOf<String>()

    private val actions = listOf(
        TSDialogAction("Cancel") { events += "cancel" },
        TSDialogAction("Delete", TSButtonVariant.Danger) { events += "delete" },
        TSDialogAction("Keep open", dismisses = false) { events += "keep" },
    )

    private fun setDialog(variant: TSDialogVariant = TSDialogVariant.Center, extra: @Composable () -> Unit = {}) {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                TSDialog(
                    isPresented = presented,
                    onPresentedChange = { presented = it },
                    title = "Delete this project?",
                    message = "This cannot be undone.",
                    variant = variant,
                    actions = actions,
                    onDismiss = { dismissals++ },
                    presenter = presenter,
                )
                extra()
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun centerShowsInADialogWindowWithPaneTitleAndHeading() {
        setDialog()
        composeRule.onNode(isDialog()).assertExists()
        composeRule.onNodeWithTag(TSDialogTestTags.PANEL)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Delete this project?"))
        composeRule.onNodeWithText("Delete this project?").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule.onNodeWithText("This cannot be undone.").assertExists()
        composeRule.onAllNodesWithTag(TSDialogTestTags.GRABBER).assertCountEquals(0)
        assertTrue(presenter.isPresenting)
    }

    @Test
    fun centerActionsSitInATrailingRowPrimaryLast() {
        setDialog()
        val cancel = composeRule.onNodeWithText("Cancel").getBoundsInRoot()
        val delete = composeRule.onNodeWithText("Delete").getBoundsInRoot()
        assertEquals(cancel.top, delete.top)
        assertTrue(cancel.left < delete.left)
    }

    @Test
    fun sheetShowsTheGrabberAndStacksPrimaryFirst() {
        setDialog(TSDialogVariant.Sheet)
        composeRule.onNodeWithTag(TSDialogTestTags.GRABBER, useUnmergedTree = true).assertExists()
        val cancel = composeRule.onNodeWithText("Cancel").getBoundsInRoot()
        val delete = composeRule.onNodeWithText("Delete").getBoundsInRoot()
        assertTrue(delete.top < cancel.top)
    }

    @Test
    fun dialogWindowInheritsTheHostDensity() {
        composeRule.setContent {
            val host = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(host.density, fontScale = 2f)) {
                TSTheme(darkTheme = false) {
                    BasicText("Delete", style = TSTheme.typography.title2, modifier = Modifier.testTag(REFERENCE))
                    TSDialog(
                        isPresented = presented,
                        onPresentedChange = { presented = it },
                        title = "Delete",
                        message = "Body",
                        presenter = presenter,
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val reference = composeRule.onNodeWithTag(REFERENCE).getBoundsInRoot().height
        val title = composeRule.onNodeWithTag(TSDialogTestTags.TITLE, useUnmergedTree = true).getBoundsInRoot().height
        assertEquals(reference.value, title.value, 1f)
    }

    @Test
    fun actionClosesThenRunsWithoutOnDismiss() {
        setDialog()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitForIdle()
        assertFalse(presented)
        assertEquals(listOf("delete"), events)
        assertEquals(0, dismissals)
        composeRule.onAllNodesWithText("Delete this project?").assertCountEquals(0)
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun nonDismissingActionKeepsTheDialogOpen() {
        setDialog()
        composeRule.onNodeWithText("Keep open").performClick()
        composeRule.waitForIdle()
        assertTrue(presented)
        assertEquals(listOf("keep"), events)
        composeRule.onNodeWithText("Delete this project?").assertExists()
    }

    @Test
    fun scrimTapDismisses() {
        setDialog()
        composeRule.onNodeWithTag(TSDialogTestTags.SCRIM).performTouchInput { click(Offset(10f, 10f)) }
        composeRule.waitForIdle()
        assertFalse(presented)
        assertEquals(1, dismissals)
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun panelTapDoesNotDismiss() {
        setDialog()
        composeRule.onNodeWithText("This cannot be undone.").performClick()
        composeRule.waitForIdle()
        assertTrue(presented)
        assertEquals(0, dismissals)
    }

    @Test
    fun backDismissesTheCenterDialog() {
        setDialog()
        Espresso.pressBack()
        composeRule.waitForIdle()
        assertFalse(presented)
        assertEquals(1, dismissals)
    }

    @Test
    fun backDismissesTheSheet() {
        setDialog(TSDialogVariant.Sheet)
        Espresso.pressBack()
        composeRule.waitForIdle()
        assertFalse(presented)
        assertEquals(1, dismissals)
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun predictiveBackScalesThePanelCancelRestoresAndCommitDismisses() {
        setDialog()
        val dispatcher = (ShadowDialog.getLatestDialog() as ComponentDialog).onBackPressedDispatcher
        val panel = composeRule.onNodeWithTag(TSDialogTestTags.PANEL)
        val width = panel.getBoundsInRoot().width
        composeRule.runOnUiThread {
            dispatcher.dispatchOnBackStarted(BackEventCompat(0f, 0f, 0f, BackEventCompat.EDGE_LEFT))
            dispatcher.dispatchOnBackProgressed(BackEventCompat(10f, 0f, 1f, BackEventCompat.EDGE_LEFT))
        }
        composeRule.waitForIdle()
        assertEquals(width.value * 0.9f, panel.getBoundsInRoot().width.value, 1f)
        composeRule.runOnUiThread { dispatcher.dispatchOnBackCancelled() }
        composeRule.waitForIdle()
        assertEquals(width.value, panel.getBoundsInRoot().width.value, 0.5f)
        assertTrue(presented)
        assertEquals(0, dismissals)
        composeRule.runOnUiThread {
            dispatcher.dispatchOnBackStarted(BackEventCompat(0f, 0f, 0f, BackEventCompat.EDGE_LEFT))
            dispatcher.dispatchOnBackProgressed(BackEventCompat(10f, 0f, 0.5f, BackEventCompat.EDGE_LEFT))
            dispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        assertFalse(presented)
        assertEquals(1, dismissals)
    }

    @Test
    fun accessibilityDismissAndCloseActionDismiss() {
        setDialog()
        val panel = composeRule.onNodeWithTag(TSDialogTestTags.PANEL)
        val close = panel.fetchSemanticsNode().config.getOrNull(SemanticsActions.CustomActions).orEmpty()
        assertEquals(listOf("Close"), close.map { it.label })
        panel.performSemanticsAction(SemanticsActions.Dismiss)
        composeRule.waitForIdle()
        assertFalse(presented)
        assertEquals(1, dismissals)
    }

    @Test
    fun secondDialogIsRefusedAndItsBindingReset() {
        var second by mutableStateOf(true)
        setDialog {
            TSDialog(
                isPresented = second,
                onPresentedChange = { second = it },
                title = "Second",
                presenter = presenter,
            ) { BasicText("Body") }
        }
        assertFalse(second)
        assertEquals(1, presenter.refusedCount)
        composeRule.onAllNodesWithText("Second").assertCountEquals(0)
        composeRule.onNodeWithText("Delete this project?").assertExists()
    }

    @Test
    fun presenterIsFreedSoTheNextDialogIsGranted() {
        var second by mutableStateOf(false)
        setDialog {
            TSDialog(
                isPresented = second,
                onPresentedChange = { second = it },
                title = "Second",
                presenter = presenter,
            ) { BasicText("Body") }
        }
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.waitForIdle()
        second = true
        composeRule.waitForIdle()
        assertTrue(second)
        composeRule.onNodeWithText("Second").assertExists()
        assertEquals(0, presenter.refusedCount)
    }

    @Test
    fun actionHandlerCanPresentAFollowUpDialog() {
        var second by mutableStateOf(false)
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                TSDialog(
                    isPresented = presented,
                    onPresentedChange = { presented = it },
                    title = "Delete this project?",
                    message = "This cannot be undone.",
                    actions = listOf(TSDialogAction("Delete", TSButtonVariant.Danger) { second = true }),
                    presenter = presenter,
                )
                TSDialog(
                    isPresented = second,
                    onPresentedChange = { second = it },
                    title = "Project deleted",
                    message = "Undo?",
                    presenter = presenter,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitForIdle()
        assertTrue(second)
        assertEquals(0, presenter.refusedCount)
        composeRule.onNodeWithText("Project deleted").assertExists()
        composeRule.onAllNodesWithText("Delete this project?").assertCountEquals(0)
    }

    @Test
    fun sheetActionHandlerCanPresentAFollowUpDialog() {
        var second by mutableStateOf(false)
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                TSDialog(
                    isPresented = presented,
                    onPresentedChange = { presented = it },
                    title = "Delete this project?",
                    message = "This cannot be undone.",
                    variant = TSDialogVariant.Sheet,
                    actions = listOf(TSDialogAction("Delete", TSButtonVariant.Danger) { second = true }),
                    presenter = presenter,
                )
                TSDialog(
                    isPresented = second,
                    onPresentedChange = { second = it },
                    title = "Project deleted",
                    message = "Undo?",
                    presenter = presenter,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitForIdle()
        assertTrue(second)
        assertEquals(0, presenter.refusedCount)
        composeRule.onNodeWithText("Project deleted").assertExists()
    }

    @Test
    fun sheetReRequestedDuringItsHideEndsExpandedAndStillFreesLater() {
        setDialog(TSDialogVariant.Sheet)
        val expandedTop = composeRule.onNodeWithTag(TSDialogTestTags.PANEL).getBoundsInRoot().top
        composeRule.mainClock.autoAdvance = false
        presented = false
        val panel = composeRule.onNodeWithTag(TSDialogTestTags.PANEL)
        val hidingTops = List(5) {
            composeRule.mainClock.advanceTimeByFrame()
            panel.getBoundsInRoot().top
        }
        assertTrue(hidingTops.last() > expandedTop)
        presented = true
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        assertEquals(expandedTop.value, panel.getBoundsInRoot().top.value, 0.5f)
        assertTrue(presenter.isPresenting)
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.waitForIdle()
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun refusedPresentationCallsOnRefused() {
        var second by mutableStateOf(true)
        var refusals = 0
        setDialog {
            TSDialog(
                isPresented = second,
                onPresentedChange = { second = it },
                title = "Second",
                message = "Body",
                presenter = presenter,
                onRefused = { refusals++ },
            )
        }
        assertFalse(second)
        assertEquals(1, refusals)
        assertEquals(1, presenter.refusedCount)
    }

    @Test
    fun grantedPresentationDoesNotCallOnRefused() {
        var refusals = 0
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                TSDialog(
                    isPresented = presented,
                    onPresentedChange = { presented = it },
                    title = "Only",
                    message = "Body",
                    presenter = presenter,
                    onRefused = { refusals++ },
                )
            }
        }
        composeRule.waitForIdle()
        assertEquals(0, refusals)
        assertTrue(presenter.isPresenting)
    }

    @Test
    fun changingThePresenterWhileShownClaimsTheNewOne() {
        val other = TSDialogPresenter()
        var current by mutableStateOf(presenter)
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                TSDialog(
                    isPresented = presented,
                    onPresentedChange = { presented = it },
                    title = "Moving",
                    message = "Body",
                    presenter = current,
                )
            }
        }
        composeRule.waitForIdle()
        assertTrue(presenter.isPresenting)
        current = other
        composeRule.waitForIdle()
        assertFalse(presenter.isPresenting)
        assertTrue(other.isPresenting)
        assertTrue(presented)
    }

    private companion object {
        const val REFERENCE = "reference"
    }
}
