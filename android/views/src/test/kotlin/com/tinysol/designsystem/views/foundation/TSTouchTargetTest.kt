package com.tinysol.designsystem.views.foundation

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
class TSTouchTargetTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun layout(vararg children: Pair<View, Rect>): FrameLayout = FrameLayout(context).apply {
        children.forEach { (child, _) -> addView(child) }
        measure(View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY))
        layout(0, 0, 200, 200)
        children.forEach { (child, frame) ->
            child.measure(
                View.MeasureSpec.makeMeasureSpec(frame.width(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(frame.height(), View.MeasureSpec.EXACTLY),
            )
            child.layout(frame.left, frame.top, frame.right, frame.bottom)
        }
    }

    private fun FrameLayout.tap(x: Float, y: Float) {
        val time = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(time, time, action, x, y, 0)
            dispatchTouchEvent(event)
            event.recycle()
        }
    }

    private val FrameLayout.group: TSTouchDelegateGroup
        get() = touchDelegate as TSTouchDelegateGroup

    @Test
    fun expandsSmallBoundsToTheMinimumAroundTheirCentre() {
        assertEquals(Rect(88, 88, 136, 136), expandedTouchBounds(Rect(100, 100, 124, 124), 48))
        assertEquals(Rect(88, 100, 136, 148), expandedTouchBounds(Rect(100, 100, 123, 148), 48))
        assertEquals(Rect(0, 0, 60, 60), expandedTouchBounds(Rect(0, 0, 60, 60), 48))
    }

    @Test
    fun registersA48dpDelegateOnTheParentAfterLayout() {
        val child = View(context)
        child.ensureMinTouchTarget()
        val parent = layout(child to Rect(100, 100, 124, 124))
        assertEquals(Rect(88, 88, 136, 136), parent.group.bounds.getValue(child))
    }

    @Test
    fun tapInsideTheExpandedAreaClicksTheSmallView() {
        var clicks = 0
        val child = View(context).apply { setOnClickListener { clicks++ } }
        child.ensureMinTouchTarget()
        val parent = layout(child to Rect(100, 100, 124, 124))
        Robolectric.buildActivity(Activity::class.java).setup().get().setContentView(FrameLayout(context).apply { addView(parent, 200, 200) })
        parent.layout(0, 0, 200, 200)
        child.layout(100, 100, 124, 124)

        parent.tap(90f, 90f)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, clicks)
        parent.tap(60f, 60f)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, clicks)
    }

    @Test
    fun aDelegateThatDeclinesTheDownLeavesTheEventUnchangedForTheNextOne() {
        val received = mutableListOf<Int>()
        val declining = View(context)
        val accepting = View(context).apply {
            setOnTouchListener { _, event ->
                received += event.actionMasked
                true
            }
        }
        val host = layout(accepting to Rect(70, 70, 100, 100), declining to Rect(55, 55, 95, 95))
        val group = TSTouchDelegateGroup(host).apply {
            put(accepting, Rect(60, 60, 110, 110))
            put(declining, Rect(50, 50, 100, 100))
        }
        val time = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, 80f, 80f, 0)
        val up = MotionEvent.obtain(time, time, MotionEvent.ACTION_UP, 80f, 80f, 0)

        assertTrue(group.onTouchEvent(down))
        assertEquals(80f, down.x, 0f)
        assertEquals(80f, down.y, 0f)
        assertTrue(group.onTouchEvent(up))
        assertEquals(80f, up.x, 0f)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP), received)
        down.recycle()
        up.recycle()
    }

    @Test
    fun severalSiblingsShareOneDelegateGroup() {
        val first = View(context).apply { ensureMinTouchTarget() }
        val second = View(context).apply { ensureMinTouchTarget() }
        val parent = layout(first to Rect(10, 10, 34, 34), second to Rect(150, 150, 174, 174))
        assertEquals(setOf(first, second), parent.group.bounds.keys)
    }

    @Test
    fun followsRelayoutAndCanBeCleared() {
        val child = View(context)
        child.ensureMinTouchTarget()
        val parent = layout(child to Rect(100, 100, 124, 124))
        child.layout(20, 20, 44, 44)
        assertEquals(Rect(8, 8, 56, 56), parent.group.bounds.getValue(child))

        child.clearMinTouchTarget()
        assertFalse(parent.group.bounds.containsKey(child))
        child.layout(30, 30, 54, 54)
        assertFalse(parent.group.bounds.containsKey(child))
    }

    @Test
    fun goneViewsAreNotTouchable() {
        val child = View(context).apply { ensureMinTouchTarget() }
        val parent = layout(child to Rect(100, 100, 124, 124))
        child.visibility = View.GONE
        child.layout(100, 100, 100, 100)
        assertTrue(parent.group.bounds.isEmpty())
        assertNull(parent.group.bounds[child])
    }

    @Test
    fun detachedViewsLeaveTheGroup() {
        val child = View(context).apply { ensureMinTouchTarget() }
        val parent = layout(child to Rect(100, 100, 124, 124))
        Robolectric.buildActivity(Activity::class.java).setup().get().setContentView(parent)
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(parent.group.bounds.containsKey(child))

        parent.removeView(child)
        assertFalse(parent.group.bounds.containsKey(child))
    }

    @Test
    @Config(sdk = [36])
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun accessibilityServicesSeeTheExpandedBounds() {
        val first = View(context).apply { ensureMinTouchTarget() }
        val second = View(context).apply { ensureMinTouchTarget() }
        val parent = layout(first to Rect(10, 10, 34, 34), second to Rect(150, 150, 174, 174))
        val info = parent.group.touchDelegateInfo
        val regions = (0 until info.regionCount).map { info.getRegionAt(it).bounds }.toSet()
        assertEquals(setOf(Rect(-2, -2, 46, 46), Rect(138, 138, 186, 186)), regions)
    }
}
