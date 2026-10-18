package com.tinysol.designsystem.views.components.toast

import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.core.logic.TSDialogPresenter
import com.tinysol.designsystem.core.logic.TSToastMessage
import com.tinysol.designsystem.core.logic.TSToastQueue
import com.tinysol.designsystem.core.logic.TSToastTimeouts
import com.tinysol.designsystem.core.logic.TSToastTone
import com.tinysol.designsystem.views.components.dialog.DialogHostActivity
import com.tinysol.designsystem.views.components.dialog.TSBottomSheetDialogFragment
import com.tinysol.designsystem.views.components.dialog.TSDialogFragment
import com.tinysol.designsystem.views.components.tapCenter
import java.lang.ref.WeakReference
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36], qualifiers = "w360dp-h640dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSToastHostTest {
    private val activity = Robolectric.buildActivity(DialogHostActivity::class.java).setup().get()
    private val scheduler = ManualToastScheduler()
    private val queue = TSToastQueue(TSToastTimeouts.default)
    private var actions = 0

    private val content: ViewGroup
        get() = activity.findViewById(android.R.id.content)

    private fun idle() = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))

    private fun install(): TSToastHost = TSToastHost.install(activity, queue, scheduler).also {
        it.reduceMotion = { true }
        idle()
    }

    private fun advance(millis: Long) {
        scheduler.advanceBy(millis)
        idle()
    }

    private fun TSToastHost.shownMessage(): String? =
        topLayer?.toastView?.takeIf { it.visibility == View.VISIBLE }?.message?.toString()

    @Test
    fun installAttachesOneHostPerActivity() {
        val host = install()

        assertSame(host, TSToastHost.install(activity, queue, scheduler))
        assertSame(host, TSToastHost.from(activity))
        assertTrue(host.isAttached)
        host.detach()
        assertNull(TSToastHost.from(activity))
    }

    @Test
    fun aPendingTimeoutOfTheDefaultHostDoesNotRetainADestroyedActivity() {
        val destroyed = showFromADefaultHostThenDestroyItsActivity()
        awaitCollection(destroyed)
        assertNull(destroyed.get())
    }

    @Test
    fun hostShowsQueuedToastsInOrder() {
        val host = install()
        listOf("Project archived", "Link copied", "Saved").forEach { host.show(it) }
        idle()

        val seen = mutableListOf<String?>()
        repeat(3) {
            seen += host.shownMessage()
            advance(4_000)
        }
        seen += host.shownMessage()

        assertEquals(listOf("Project archived", "Link copied", "Saved", null), seen)
    }

    @Test
    fun autoDismissTimingUsesTheInjectedClock() {
        val host = install()
        host.show("Saved")
        idle()

        advance(3_999)
        assertEquals("Saved", host.shownMessage())
        advance(1)
        assertNull(host.shownMessage())
        assertNull(host.topLayer?.toastView)
    }

    @Test
    fun hostActionRunsTheHandlerAndShowsTheNextToast() {
        val host = install()
        host.show("Message deleted", actionLabel = "Undo", onAction = { actions++ })
        host.show("Saved")
        idle()

        requireNotNull(host.topLayer?.toastView).actionView.tapCenter()
        idle()

        assertEquals(1, actions)
        assertEquals("Saved", host.shownMessage())
    }

    @Test
    fun attachingAnOverlayMovesTheToastToANewView() {
        val host = install()
        host.show(TSToastMessage("Saved", TSToastTone.Success))
        idle()
        val toast = requireNotNull(host.topLayer?.toastView)

        val overlay = FrameLayout(activity)
        content.addView(overlay, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        host.attachOverlay(overlay)
        idle()
        val moved = requireNotNull(host.topLayer?.toastView)

        assertNotSame(toast, moved)
    }

    @Test
    @Suppress("DEPRECATION")
    fun systemWindowBottomInsetLiftsTheToast() {
        val host = install()
        host.show("Saved")
        idle()
        val layer = requireNotNull(host.topLayer)
        val bottomWith = { inset: Int ->
            ViewCompat.dispatchApplyWindowInsets(layer, WindowInsetsCompat.Builder().setSystemWindowInsets(Insets.of(0, 0, 0, inset)).build())
            layer.requestLayout()
            idle()
            requireNotNull(layer.toastView).bottom
        }
        val resting = bottomWith(0)

        assertEquals(resting - 24, bottomWith(24))
    }

    @Test
    fun layerPassesTouchesThroughOutsideTheToast() {
        val host = install()
        host.show("Saved")
        idle()
        val layer = requireNotNull(host.topLayer)

        val toast = requireNotNull(layer.toastView)
        val down = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 1f, 1f, 0)

        assertTrue(!layer.isClickable)
        assertTrue(toast.onTouchEvent(down))
        assertFalse(toast.isClickable)
        down.recycle()
    }

    @Test
    fun anchorKeepsTheToastAboveIt() {
        val anchor = View(activity)
        content.addView(anchor, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 56, Gravity.BOTTOM))
        val host = install()
        host.anchorView = anchor
        host.show("Saved")
        idle()

        val toast = requireNotNull(host.topLayer?.toastView)
        val toastLocation = IntArray(2).also(toast::getLocationInWindow)
        val anchorLocation = IntArray(2).also(anchor::getLocationInWindow)
        assertEquals(anchorLocation[1] - 16, toastLocation[1] + toast.height)
        assertEquals(content.width / 2, toastLocation[0] + toast.width / 2)
    }

    @Test
    fun toastIsShownInsideAnOpenSheet() {
        val host = install()
        val sheet = TSBottomSheetDialogFragment().apply {
            dialogTitle = "Share"
            message = "Anyone with the link can view."
            presenter = TSDialogPresenter()
        }
        sheet.present(activity.supportFragmentManager, "sheet")
        idle()
        host.show("Link copied", actionLabel = "Undo", onAction = { actions++ })
        idle()

        val toast = requireNotNull(host.topLayer?.toastView)
        assertSame(sheet.requireDialog().window?.decorView, toast.rootView)
        assertEquals(View.VISIBLE, toast.visibility)
        toast.actionView.tapCenter()
        idle()
        assertEquals(1, actions)

        host.show("Saved")
        idle()
        sheet.dismiss()
        idle()
        assertSame(activity.window.decorView, requireNotNull(host.topLayer?.toastView).rootView)
        assertEquals("Saved", host.shownMessage())
    }

    @Test
    fun toastIsShownInsideAnOpenCenterDialog() {
        val host = install()
        val dialog = TSDialogFragment().apply {
            dialogTitle = "Share"
            message = "Anyone with the link can view."
            presenter = TSDialogPresenter()
        }
        dialog.present(activity.supportFragmentManager, "dialog")
        idle()
        host.show("Link copied")
        idle()

        assertSame(dialog.requireDialog().window?.decorView, requireNotNull(host.topLayer?.toastView).rootView)
    }

    private fun showFromADefaultHostThenDestroyItsActivity(): WeakReference<Any> {
        val controller = Robolectric.buildActivity(DialogHostActivity::class.java).setup()
        val destroyed = controller.get()
        TSToastHost.install(destroyed).show("Saved")
        idle()
        controller.pause().stop().destroy()
        idle()
        return WeakReference<Any>(destroyed)
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
