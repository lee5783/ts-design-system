package com.ts.ds.views.components.dialog

import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.activity.BackEventCompat
import androidx.activity.ComponentDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSDialogPresenter
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.views.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.lang.ref.WeakReference

class DialogHostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TS)
        super.onCreate(savedInstanceState)
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSDialogFragmentTest {
    private val hostActivity = Robolectric.buildActivity(DialogHostActivity::class.java).setup().get()
    private val presenter = TSDialogPresenter()
    private var dismissals = 0
    private val events = mutableListOf<String>()

    private val actions = listOf(
        TSDialogAction("Cancel") { events += "cancel" },
        TSDialogAction("Delete", TSButtonVariant.Danger) { events += "delete" },
    )

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun center(title: String = "Delete this project?"): TSDialogFragment = TSDialogFragment().apply {
        dialogTitle = title
        message = "This cannot be undone."
        actions = this@TSDialogFragmentTest.actions
        onUserDismiss = { dismissals++ }
        presenter = this@TSDialogFragmentTest.presenter
    }

    private fun sheet(): TSBottomSheetDialogFragment = TSBottomSheetDialogFragment().apply {
        dialogTitle = "Delete this project?"
        message = "This cannot be undone."
        actions = this@TSDialogFragmentTest.actions
        onUserDismiss = { dismissals++ }
        presenter = this@TSDialogFragmentTest.presenter
    }

    private fun TSDialogFragment.presentAndIdle(): Boolean = present(hostActivity.supportFragmentManager, "dialog").also { idle() }

    private fun TSBottomSheetDialogFragment.presentAndIdle(): Boolean = present(hostActivity.supportFragmentManager, "sheet").also { idle() }

    private fun View.tapAt(x: Float, y: Float) {
        val time = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(time, time, action, x, y, 0)
            dispatchTouchEvent(event)
            event.recycle()
        }
        idle()
    }

    private fun nodeOf(view: View): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(view, it) }

    @Test
    fun centerPresentsAModalPanelWithPaneTitleAndHeading() {
        val fragment = center()
        assertTrue(fragment.presentAndIdle())
        assertTrue(fragment.requireDialog().isShowing)
        val panel = requireNotNull(fragment.panel)
        assertEquals("Delete this project?", ViewCompat.getAccessibilityPaneTitle(panel))
        assertTrue(nodeOf(panel.titleView).isHeading)
        assertEquals("This cannot be undone.", panel.messageView.text.toString())
        assertEquals(View.GONE, panel.grabberView.visibility)
        assertTrue(presenter.isActive(fragment))
    }

    @Test
    fun centerPanelFillsTheWindowOverTheScrim() {
        val fragment = center().also { it.presentAndIdle() }
        val scrim = fragment.requireView() as TSDialogScrimLayout
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, scrim.importantForAccessibility)
        assertTrue(requireNotNull(scrim.panel).width <= (440 * hostActivity.resources.displayMetrics.density).toInt())
    }

    @Test
    fun centerActionsAreARowPrimaryLast() {
        val fragment = center().also { it.presentAndIdle() }
        val panel = requireNotNull(fragment.panel)
        assertEquals(listOf("Cancel", "Delete"), panel.actionButtons.map { it.text.toString() })
        assertEquals(android.widget.LinearLayout.HORIZONTAL, panel.actionsView.orientation)
    }

    @Test
    fun sheetShowsTheGrabberAndStacksPrimaryFirst() {
        val fragment = sheet()
        assertTrue(fragment.presentAndIdle())
        val panel = requireNotNull(fragment.panel)
        assertEquals(View.VISIBLE, panel.grabberView.visibility)
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, panel.grabberView.importantForAccessibility)
        assertEquals(listOf("Delete", "Cancel"), panel.actionButtons.map { it.text.toString() })
        assertEquals(android.widget.LinearLayout.VERTICAL, panel.actionsView.orientation)
        assertEquals(panel.actionsView.width, panel.actionButtons.first().width)
    }

    @Test
    fun actionRunsThenDismissesWithoutUserDismiss() {
        val fragment = center().also { it.presentAndIdle() }
        requireNotNull(fragment.panel).actionButtons.last().performClick()
        idle()
        assertEquals(listOf("delete"), events)
        assertEquals(0, dismissals)
        assertFalse(presenter.isPresenting)
        assertNull(hostActivity.supportFragmentManager.findFragmentByTag("dialog"))
    }

    @Test
    fun centerActionHandlerRunsWhileTheDialogIsStillShowing() = assertHandlerRunsWhileShowing { actions ->
        center().apply { this.actions = actions }.also { it.presentAndIdle() }.let { it to it.panel }
    }

    @Test
    fun sheetActionHandlerRunsWhileTheDialogIsStillShowing() = assertHandlerRunsWhileShowing { actions ->
        sheet().apply { this.actions = actions }.also { it.presentAndIdle() }.let { it to it.panel }
    }

    private fun assertHandlerRunsWhileShowing(show: (List<TSDialogAction>) -> Pair<DialogFragment, TSDialogPanelView?>) {
        var showingInHandler = false
        var addedInHandler = false
        var fragment: DialogFragment? = null
        val (shown, panel) = show(
            listOf(
                TSDialogAction("Remove") {
                    showingInHandler = requireNotNull(fragment).requireDialog().isShowing
                    addedInHandler = requireNotNull(fragment).isAdded
                },
            ),
        )
        fragment = shown
        requireNotNull(panel).actionButtons.single().performClick()
        idle()
        assertTrue(showingInHandler)
        assertTrue(addedInHandler)
        assertFalse(presenter.isPresenting)
        assertFalse(shown.isAdded)
    }

    @Test
    fun aHandlerThatRemovesItsDialogDoesNotCrashAndFreesThePresenter() {
        val fragment = center()
        fragment.actions = listOf(
            TSDialogAction("Remove") { hostActivity.supportFragmentManager.beginTransaction().remove(fragment).commitNow() },
        )
        fragment.presentAndIdle()
        requireNotNull(fragment.panel).actionButtons.single().performClick()
        idle()
        assertFalse(fragment.isAdded)
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun aQueuedRequestWaitsUntilTheViewsDialogHasRunItsHandlerAndDismissed() {
        val queuedOwner = Any()
        var granted = 0
        var heldInHandler = false
        val fragment = center()
        fragment.actions = listOf(TSDialogAction("Done") { heldInHandler = presenter.isActive(fragment) && granted == 0 })
        fragment.presentAndIdle()
        presenter.request(queuedOwner) { granted += 1 }
        requireNotNull(fragment.panel).actionButtons.single().performClick()
        idle()
        assertTrue(heldInHandler)
        assertEquals(1, granted)
        assertTrue(presenter.isActive(queuedOwner))
    }

    @Test
    fun aChainedDialogTakesTheSlotAheadOfAQueuedRequest() {
        val queuedOwner = Any()
        var granted = 0
        var followUp: TSDialogFragment? = null
        val fragment = center()
        fragment.actions = listOf(
            TSDialogAction("Delete") {
                followUp = center("Project deleted").also { assertTrue(it.present(hostActivity.supportFragmentManager, "followUp")) }
            },
        )
        fragment.presentAndIdle()
        presenter.request(queuedOwner) { granted += 1 }
        requireNotNull(fragment.panel).actionButtons.single().performClick()
        idle()
        assertTrue(presenter.isActive(requireNotNull(followUp)))
        assertEquals(0, granted)
        assertEquals(0, presenter.refusedCount)
    }

    @Test
    fun centerAndSheetKeepTheHostsHiddenSystemBars() {
        WindowCompat.getInsetsController(hostActivity.window, hostActivity.window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        idle()
        assertFalse(hostActivity.window.decorView.showsBar(WindowInsetsCompat.Type.statusBars(), View.SYSTEM_UI_FLAG_FULLSCREEN))
        val centerFragment = center().also { it.presentAndIdle() }
        assertBarsHidden(requireNotNull(centerFragment.requireDialog().window))
        centerFragment.dismiss()
        idle()
        val sheetWindow = requireNotNull(sheet().also { it.presentAndIdle() }.requireDialog().window)
        assertBarsHidden(sheetWindow)
    }

    @Test
    fun dialogOverAHostWithVisibleBarsKeepsThemVisible() {
        val window = requireNotNull(center().also { it.presentAndIdle() }.requireDialog().window)
        assertTrue(window.decorView.showsBar(WindowInsetsCompat.Type.statusBars(), View.SYSTEM_UI_FLAG_FULLSCREEN))
        assertTrue(window.decorView.showsBar(WindowInsetsCompat.Type.navigationBars(), View.SYSTEM_UI_FLAG_HIDE_NAVIGATION))
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

    @Test
    fun scrimTapDismissesAndFreesThePresenter() {
        val fragment = center().also { it.presentAndIdle() }
        fragment.requireView().tapAt(4f, 4f)
        assertEquals(1, dismissals)
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun panelTapDoesNotDismiss() {
        val fragment = center().also { it.presentAndIdle() }
        val scrim = fragment.requireView() as TSDialogScrimLayout
        val panel = requireNotNull(scrim.panel)
        scrim.tapAt(panel.left + panel.width / 2f, panel.top + panel.height / 4f)
        assertEquals(0, dismissals)
        assertTrue(presenter.isActive(fragment))
    }

    @Test
    fun backDismissesTheCenterDialog() {
        val fragment = center().also { it.presentAndIdle() }
        (fragment.requireDialog() as ComponentDialog).onBackPressedDispatcher.onBackPressed()
        idle()
        assertEquals(1, dismissals)
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun predictiveBackScalesThePanelAndCancelRestoresIt() {
        val fragment = center().also { it.presentAndIdle() }
        val dispatcher = (fragment.requireDialog() as ComponentDialog).onBackPressedDispatcher
        val panel = requireNotNull(fragment.panel)
        dispatcher.dispatchOnBackStarted(BackEventCompat(0f, 0f, 0f, BackEventCompat.EDGE_LEFT))
        dispatcher.dispatchOnBackProgressed(BackEventCompat(10f, 0f, 0.5f, BackEventCompat.EDGE_LEFT))
        assertEquals(0.95f, panel.scaleX, 0.0001f)
        dispatcher.dispatchOnBackCancelled()
        assertEquals(1f, panel.scaleX, 0f)
        assertEquals(0, dismissals)
        dispatcher.dispatchOnBackStarted(BackEventCompat(0f, 0f, 0f, BackEventCompat.EDGE_LEFT))
        dispatcher.dispatchOnBackProgressed(BackEventCompat(10f, 0f, 1f, BackEventCompat.EDGE_LEFT))
        dispatcher.onBackPressed()
        idle()
        assertEquals(1, dismissals)
    }

    @Test
    fun backDismissesTheSheet() {
        val fragment = sheet().also { it.presentAndIdle() }
        fragment.requireDialog().onBackPressed()
        idle()
        assertEquals(1, dismissals)
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun accessibilityCloseAndDismissActionsDismiss() {
        val fragment = center().also { it.presentAndIdle() }
        val panel = requireNotNull(fragment.panel)
        val labels = nodeOf(panel).actionList.mapNotNull { it.label?.toString() }
        assertTrue(labels.contains("Close"))
        assertTrue(ViewCompat.performAccessibilityAction(panel, AccessibilityNodeInfoCompat.ACTION_DISMISS, null))
        idle()
        assertEquals(1, dismissals)
    }

    @Test
    fun secondPresentationIsRefused() {
        val first = center().also { it.presentAndIdle() }
        val second = center("Second")
        assertFalse(second.present(hostActivity.supportFragmentManager, "second"))
        idle()
        assertEquals(1, presenter.refusedCount)
        assertNull(hostActivity.supportFragmentManager.findFragmentByTag("second"))
        assertTrue(presenter.isActive(first))
    }

    @Test
    fun showIsRoutedThroughThePresenter() {
        center().also { it.presentAndIdle() }
        val second = sheet()
        second.show(hostActivity.supportFragmentManager, "second")
        idle()
        assertEquals(1, presenter.refusedCount)
        assertNull(hostActivity.supportFragmentManager.findFragmentByTag("second"))
    }

    @Test
    fun aRawTransactionCannotBypassTheRefusal() {
        center().also { it.presentAndIdle() }
        val second = center("Second")
        hostActivity.supportFragmentManager.beginTransaction().add(second, "second").commitNow()
        idle()
        assertEquals(1, presenter.refusedCount)
        assertNull(hostActivity.supportFragmentManager.findFragmentByTag("second"))
    }

    @Test
    fun presenterIsFreedSoTheNextDialogIsGranted() {
        val first = center().also { it.presentAndIdle() }
        first.dismiss()
        idle()
        assertTrue(sheet().presentAndIdle())
        assertEquals(0, presenter.refusedCount)
    }

    @Test
    fun presentAfterTheStateIsSavedReturnsFalseAndLeavesThePresenterFree() {
        val controller = Robolectric.buildActivity(DialogHostActivity::class.java).setup()
        controller.pause().stop()
        val fragment = center()
        assertFalse(fragment.present(controller.get().supportFragmentManager, "dialog"))
        assertFalse(presenter.isPresenting)
        assertEquals(0, presenter.refusedCount)
        assertTrue(sheet().presentAndIdle())
    }

    @Test
    fun showAfterTheStateIsSavedLeavesThePresenterFree() {
        val controller = Robolectric.buildActivity(DialogHostActivity::class.java).setup()
        controller.pause().stop()
        center().show(controller.get().supportFragmentManager, "dialog")
        assertFalse(presenter.isPresenting)
        assertTrue(sheet().presentAndIdle())
    }

    @Test
    fun aThrowingTransactionCommitRollsTheClaimBack() {
        val controller = Robolectric.buildActivity(DialogHostActivity::class.java).setup()
        controller.pause().stop()
        val transaction = controller.get().supportFragmentManager.beginTransaction()
        assertThrows(IllegalStateException::class.java) { center().show(transaction, "dialog") }
        assertFalse(presenter.isPresenting)
        assertTrue(center("Later").presentAndIdle())
    }

    @Test
    fun showNowOnADestroyedManagerLeavesThePresenterFree() {
        val controller = Robolectric.buildActivity(DialogHostActivity::class.java).setup()
        val manager = controller.get().supportFragmentManager
        controller.pause().stop().destroy()
        assertThrows(IllegalStateException::class.java) { sheet().showNow(manager, "sheet") }
        assertFalse(presenter.isPresenting)
        assertTrue(center("Later").presentAndIdle())
    }

    @Test
    fun anAbandonedClaimDoesNotRetainTheFragmentOrItsActivity() {
        val (fragment, activity) = claimFromAnActivityThatIsThenDestroyed()
        awaitCollection(activity)
        assertNull(fragment.get())
        assertNull(activity.get())
        assertFalse(presenter.isPresenting)
        assertTrue(center("Later").presentAndIdle())
    }

    @Test
    fun actionHandlerCanPresentAFollowUpDialog() {
        var followUp: TSDialogFragment? = null
        val fragment = center().apply {
            actions = listOf(
                TSDialogAction("Delete", TSButtonVariant.Danger) {
                    followUp = center("Project deleted").also { assertTrue(it.present(hostActivity.supportFragmentManager, "followUp")) }
                },
            )
        }
        fragment.presentAndIdle()
        requireNotNull(fragment.panel).actionButtons.single().performClick()
        idle()
        assertEquals(0, presenter.refusedCount)
        assertTrue(presenter.isActive(requireNotNull(followUp)))
        assertTrue(requireNotNull(followUp).requireDialog().isShowing)
    }

    @Test
    fun sheetActionHandlerCanPresentAFollowUpDialog() {
        var granted = false
        val fragment = sheet().apply {
            actions = listOf(TSDialogAction("Delete") { granted = center("Project deleted").present(hostActivity.supportFragmentManager, "followUp") })
        }
        fragment.presentAndIdle()
        requireNotNull(fragment.panel).actionButtons.single().performClick()
        idle()
        assertTrue(granted)
        assertEquals(0, presenter.refusedCount)
    }

    @Test
    fun aRestoredCenterDialogRemovesItselfAndReportsTheDrop() {
        assertDroppedAfterRecreation("dialog") { manager -> center().present(manager, "dialog") }
    }

    @Test
    fun aRestoredSheetRemovesItselfAndReportsTheDrop() {
        assertDroppedAfterRecreation("sheet") { manager -> sheet().present(manager, "sheet") }
    }

    @Test
    fun aRefusalOnFirstShowDoesNotReportADrop() {
        val dropped = mutableListOf<String?>()
        hostActivity.supportFragmentManager.setFragmentResultListener(TSDialogFragmentResults.DROPPED, hostActivity) { _, bundle ->
            dropped += bundle.getString(TSDialogFragmentResults.KEY_TAG)
        }
        center().also { it.presentAndIdle() }
        hostActivity.supportFragmentManager.beginTransaction().add(center("Second"), "second").commitNow()
        idle()
        assertNull(hostActivity.supportFragmentManager.findFragmentByTag("second"))
        assertEquals(emptyList<String?>(), dropped)
    }

    private fun assertDroppedAfterRecreation(tag: String, show: (FragmentManager) -> Boolean) {
        val controller = Robolectric.buildActivity(DialogHostActivity::class.java).setup()
        assertTrue(show(controller.get().supportFragmentManager))
        idle()
        controller.recreate()
        val recreated = controller.get()
        val dropped = mutableListOf<String?>()
        recreated.supportFragmentManager.setFragmentResultListener(TSDialogFragmentResults.DROPPED, recreated) { _, bundle ->
            dropped += bundle.getString(TSDialogFragmentResults.KEY_TAG)
        }
        idle()
        assertEquals(listOf<String?>(tag), dropped)
        assertNull(recreated.supportFragmentManager.findFragmentByTag(tag))
        assertFalse(presenter.isPresenting)
    }

    private fun claimFromAnActivityThatIsThenDestroyed(): Pair<WeakReference<Any>, WeakReference<Any>> {
        val controller = Robolectric.buildActivity(DialogHostActivity::class.java).setup()
        val activity = controller.get()
        val fragment = center().apply { bodyView = TextView(activity) }
        assertTrue(presenter.begin(fragment))
        controller.pause().stop().destroy()
        idle()
        return WeakReference<Any>(fragment) to WeakReference<Any>(activity)
    }

    private fun awaitCollection(reference: WeakReference<*>) {
        repeat(50) {
            if (reference.get() == null) return
            System.gc()
            Thread.sleep(10)
        }
        fail("The reference was not collected")
    }
}
