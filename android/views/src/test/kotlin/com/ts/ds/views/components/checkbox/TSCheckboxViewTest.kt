package com.ts.ds.views.components.checkbox

import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.widget.CheckBox
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.haptics.DsHaptics
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.views.R
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.tapCenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
class TSCheckboxViewTest {
    private val changes = mutableListOf<Boolean>()

    private fun checkbox(configure: TSCheckboxView.() -> Unit = {}): TSCheckboxView = hostInActivity { context ->
        TSCheckboxView(context).apply {
            label = "Remember me"
            setOnCheckedChangeListener { _, isChecked -> changes += isChecked }
            configure()
        }
    }

    private fun TSCheckboxView.node(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(this, it) }

    private fun TSCheckboxView.tapAt(x: Float) {
        val time = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            MotionEvent.obtain(time, time, action, x, height / 2f, 0).also(::dispatchTouchEvent).recycle()
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun tapTogglesAndNotifiesTheListener() {
        val view = checkbox()

        view.tapCenter()
        assertTrue(view.isChecked)
        view.tapCenter()

        assertFalse(view.isChecked)
        assertEquals(listOf(true, false), changes)
    }

    @Test
    fun programmaticChangesAreSilent() {
        val view = checkbox()

        view.isChecked = true
        view.toggle()

        assertFalse(view.isChecked)
        assertEquals(emptyList<Boolean>(), changes)
    }

    @Test
    fun disabledBlocksTapsAndDims() {
        val view = checkbox { isEnabled = false }

        view.tapCenter()

        assertFalse(view.isChecked)
        assertEquals(emptyList<Boolean>(), changes)
        assertEquals(0.45f, view.alpha, 0.001f)
    }

    @Test
    fun nodeIsACheckableCheckBoxWithTheLabelAndStateDescription() {
        val view = checkbox()
        val off = view.node()

        assertEquals(CheckBox::class.java.name, off.className)
        assertEquals("Remember me", off.text.toString())
        assertTrue(off.isCheckable)
        assertFalse(off.isChecked)
        assertEquals("Unchecked", ViewCompat.getStateDescription(view).toString())

        view.tapCenter()

        assertTrue(view.node().isChecked)
        assertEquals("Checked", ViewCompat.getStateDescription(view).toString())
    }

    @Test
    fun wholeRowIsTheHitAreaAndAtLeast48dpTall() {
        val view = checkbox()

        assertEquals(48, view.height)
        view.tapAt(view.width - 1f)

        assertTrue(view.isChecked)
    }

    @Test
    fun hiddenLabelKeepsTheAccessibleLabelAndA48dpTarget() {
        val view = checkbox { isLabelHidden = true }

        assertEquals(48, view.width)
        assertEquals(48, view.height)
        assertEquals(20, view.markView.width)
        assertEquals("Remember me", view.node().text.toString())
    }

    @Test
    fun pressedResolvesThePressedFills() {
        val view = checkbox()
        view.isPressed = true
        assertEquals(TSColorRole.SurfacePressed, view.resolved.markFill)

        view.isChecked = true
        assertEquals(TSColorRole.AccentPressed, view.resolved.markFill)
        assertTrue(view.markView.drawable != null)
    }

    @Test
    fun togglingPlaysTheToggleHaptic() {
        val view = checkbox()

        view.tapCenter()
        assertEquals(DsHaptics.constantFor(DsHapticEvent.ToggleOn), shadowOf(view).lastHapticFeedbackPerformed())
        view.tapCenter()
        assertEquals(DsHaptics.constantFor(DsHapticEvent.ToggleOff), shadowOf(view).lastHapticFeedbackPerformed())
    }

    @Test
    fun inflatesFromXmlAttributes() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.text, "Accept terms")
            .addAttribute(android.R.attr.checked, "true")
            .addAttribute(R.attr.tsLabelHidden, "true")
            .build()
        val view = hostInActivity { TSCheckboxView(it, attrs) }

        assertEquals("Accept terms", view.label.toString())
        assertTrue(view.isChecked)
        assertTrue(view.isLabelHidden)
    }
}
