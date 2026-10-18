package com.ts.ds.views.components.switchcontrol

import android.content.pm.ApplicationInfo
import android.os.Looper
import android.view.View
import android.view.animation.PathInterpolator
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
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
class TSSwitchViewTest {
    private val changes = mutableListOf<Boolean>()

    private fun switch(configure: TSSwitchView.() -> Unit = {}): TSSwitchView = hostInActivity { context ->
        TSSwitchView(context).apply {
            label = "Wi-Fi"
            reduceMotion = { false }
            setOnCheckedChangeListener { _, isChecked -> changes += isChecked }
            configure()
        }
    }

    private fun TSSwitchView.thumbLeft(): Float = thumbView.left + thumbView.translationX

    private fun idleFor(millis: Long) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis))
    }

    @Test
    fun tapTogglesNotifiesAndPlaysTheHaptic() {
        val view = switch()

        view.tapCenter()

        assertTrue(view.isChecked)
        assertEquals(listOf(true), changes)
        assertEquals(DsHaptics.constantFor(DsHapticEvent.ToggleOn), shadowOf(view).lastHapticFeedbackPerformed())
    }

    @Test
    fun programmaticChangesAreSilentAndImmediate() {
        val view = switch()

        view.isChecked = true

        assertEquals(emptyList<Boolean>(), changes)
        assertEquals(1f, view.thumbProgress, 0f)
        assertEquals(21f, view.thumbLeft(), 0.5f)
    }

    @Test
    fun disabledBlocksTapsAndDims() {
        val view = switch { isEnabled = false }

        view.tapCenter()

        assertFalse(view.isChecked)
        assertEquals(0.45f, view.alpha, 0.001f)
    }

    @Test
    fun checkedStateFollowsTheSetter() {
        val view = switch()

        assertFalse(view.isChecked)

        view.isChecked = true

        assertTrue(view.isChecked)
    }

    @Test
    fun trackIs44By26InsideA48dpRow() {
        val view = switch()

        assertEquals(44, view.trackView.width)
        assertEquals(26, view.trackView.height)
        assertEquals(20, view.thumbView.width)
        assertEquals(48, view.height)
    }

    @Test
    fun hiddenLabelIsA48dpTarget() {
        val view = switch { isLabelHidden = true }

        assertEquals(48, view.width)
        assertEquals(48, view.height)
    }

    @Test
    fun onIsTheTrailingSideInLeftToRight() {
        val view = switch()
        assertEquals(3f, view.thumbLeft(), 0.5f)

        view.isChecked = true

        assertEquals(21f, view.thumbLeft(), 0.5f)
    }

    @Test
    fun onIsTheTrailingSideInRightToLeft() {
        RuntimeEnvironment.getApplication().applicationInfo.flags =
            RuntimeEnvironment.getApplication().applicationInfo.flags or ApplicationInfo.FLAG_SUPPORTS_RTL
        val view = switch { layoutDirection = View.LAYOUT_DIRECTION_RTL }
        assertEquals(21f, view.thumbLeft(), 0.5f)
        assertEquals(view.width, view.trackView.right)

        view.isChecked = true

        assertEquals(3f, view.thumbLeft(), 0.5f)
    }

    @Test
    fun userToggleAnimatesOver150Milliseconds() {
        val view = switch()

        view.performClick()
        val animator = requireNotNull(view.runningAnimator)
        assertEquals(150L, animator.duration)
        assertTrue(animator.interpolator is PathInterpolator)
        assertEquals(0f, view.thumbProgress, 0f)

        idleFor(300)
        assertEquals(1f, view.thumbProgress, 0f)
        assertEquals(21f, view.thumbLeft(), 0.5f)
    }

    @Test
    fun reduceMotionMakesTheUserToggleImmediate() {
        val view = switch { reduceMotion = { true } }

        view.performClick()

        assertEquals(null, view.runningAnimator)
        assertEquals(1f, view.thumbProgress, 0f)
    }

    @Test
    fun pressedOffTrackUsesThePressedOpacity() {
        val view = switch()
        view.isPressed = true

        assertEquals(TSColorRole.BorderStrong, view.resolved.trackColor)
        assertEquals(0.85f, view.resolved.trackOpacity, 0f)
        view.isChecked = true
        assertEquals(TSColorRole.AccentPressed, view.resolved.trackColor)
    }

    @Test
    fun inflatesFromXmlAttributes() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.text, "Dark mode")
            .addAttribute(android.R.attr.checked, "true")
            .addAttribute(R.attr.tsLabelHidden, "true")
            .build()
        val view = hostInActivity { TSSwitchView(it, attrs) }

        assertEquals("Dark mode", view.label.toString())
        assertTrue(view.isChecked)
        assertTrue(view.isLabelHidden)
        assertEquals(1f, view.thumbProgress, 0f)
    }
}
