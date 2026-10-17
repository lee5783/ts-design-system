package com.ts.ds.views.components.listitem

import android.content.pm.ApplicationInfo
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.haptics.DsHaptics
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSListItemRole
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSListGroupPosition
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.tapCenter
import com.ts.ds.views.theme.tsColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "w360dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSListItemViewTest {
    private var taps = 0
    private val changes = mutableListOf<Boolean>()

    private fun row(configure: TSListItemView.() -> Unit = {}): TSListItemView = hostInActivity { context ->
        LinearLayout(context).apply {
            addView(
                TSListItemView(context).apply {
                    title = "Wi-Fi"
                    setOnCheckedChangeListener { _, isChecked -> changes += isChecked }
                    configure()
                },
                LinearLayout.LayoutParams(360, LinearLayout.LayoutParams.WRAP_CONTENT),
            )
        }
    }.getChildAt(0) as TSListItemView

    private fun TSListItemView.node(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(this, it) }

    private fun View.tapAt(x: Float, y: Float) {
        val time = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(time, time, action, x, y, 0)
            dispatchTouchEvent(event)
            event.recycle()
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun View.boundsIn(ancestor: View): android.graphics.Rect {
        val location = IntArray(2).also(::getLocationInWindow)
        val origin = IntArray(2).also(ancestor::getLocationInWindow)
        return android.graphics.Rect(location[0] - origin[0], location[1] - origin[1], location[0] - origin[0] + width, location[1] - origin[1] + height)
    }

    @Test
    fun actionRowIsAButtonThatFires() {
        val view = row { setOnClickListener { taps++ } }

        view.tapCenter()

        assertEquals(1, taps)
        assertEquals(TSListItemRole.Action, view.role)
        assertEquals(Button::class.java.name, view.node().className)
        assertEquals("Wi-Fi", view.node().text)
    }

    @Test
    fun disabledRowBlocksItsAction() {
        val view = row {
            setOnClickListener { taps++ }
            isEnabled = false
        }

        view.tapCenter()

        assertEquals(0, taps)
        assertFalse(view.node().isEnabled)
    }

    @Test
    fun plainRowIsNotClickableAndNeverPresses() {
        val view = row {
            subtitle = "Home"
            trailing = TSListItemTrailing.Text("Connected")
        }

        view.isPressed = true

        assertFalse(view.isClickable)
        assertFalse(view.isPressed)
        assertNull(view.resolved.background)
        assertEquals(View::class.java.name, view.node().className)
        assertEquals("Wi-Fi, Home", view.node().text)
        assertEquals("Connected", view.node().stateDescription)
    }

    @Test
    fun pressedAndFocusedHighlightOnlyWithAnAction() {
        val view = row { setOnClickListener {} }

        view.isPressed = true
        assertEquals(TSColorRole.SurfacePressed, view.resolved.background)

        view.isPressed = false
        assertNull(view.resolved.background)
    }

    @Test
    fun chevronRowCarriesTheNavigatesHint() {
        val view = row {
            setOnClickListener {}
            showsChevron = true
        }

        assertEquals(TSListItemRole.Navigation, view.role)
        assertEquals("Opens another screen", view.node().hintText)
        assertEquals(View.VISIBLE, view.chevronView.visibility)
        assertTrue(view.chevronView.drawable.isAutoMirrored)
    }

    @Test
    fun rowWithoutChevronHasNoHint() {
        val view = row { setOnClickListener {} }

        assertNull(view.node().hintText)
    }

    @Test
    fun switchRowIsOneSwitchElement() {
        val view = row {
            subtitle = "Home"
            trailing = TSListItemTrailing.Switch
            setOnClickListener { taps++ }
        }

        val node = view.node()
        assertEquals(SwitchCompat::class.java.name, node.className)
        assertTrue(node.isCheckable)
        assertFalse(node.isChecked)
        assertEquals("Wi-Fi, Home", node.text)
        assertEquals("Off", node.stateDescription)
        assertFalse(view.isClickable)
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS, (view.getChildAt(0)).importantForAccessibility)

        assertTrue(ViewCompat.performAccessibilityAction(view, AccessibilityNodeInfoCompat.ACTION_CLICK, null))

        assertTrue(view.isChecked)
        assertEquals(listOf(true), changes)
        assertEquals("On", view.node().stateDescription)
        assertEquals(DsHaptics.constantFor(DsHapticEvent.ToggleOn), shadowOf(view.switchView).lastHapticFeedbackPerformed())
        assertEquals(0, taps)
    }

    @Test
    fun onlyTheSwitchTakesTapsOnASwitchRow() {
        val view = row { trailing = TSListItemTrailing.Switch }

        view.tapAt(40f, view.height / 2f)
        assertEquals(emptyList<Boolean>(), changes)

        view.switchView.tapCenter()
        assertEquals(listOf(true), changes)
    }

    @Test
    fun switchHitAreaReachesTheTouchTargetOutsideTheTrack() {
        val view = row { trailing = TSListItemTrailing.Switch }
        val track = view.switchView.boundsIn(view)

        assertEquals(26, track.height())
        assertEquals(48, view.switchTouchArea.height())
        assertEquals(48, view.switchTouchArea.width())
        view.tapAt(track.exactCenterX(), track.top - 8f)

        assertEquals(listOf(true), changes)
    }

    @Test
    fun disabledSwitchRowBlocksTheSwitch() {
        val view = row {
            trailing = TSListItemTrailing.Switch
            isEnabled = false
        }

        view.switchView.tapCenter()

        assertEquals(emptyList<Boolean>(), changes)
        assertFalse(ViewCompat.performAccessibilityAction(view, AccessibilityNodeInfoCompat.ACTION_CLICK, null))
        assertFalse(view.isChecked)
        assertEquals(0.45f, view.switchView.alpha, 0.001f)
    }

    @Test
    fun heightsMatchTheDesignAtTheDefaultSize() {
        assertEquals(48, row().height)
        assertEquals(68, row { subtitle = "Home" }.height)
        assertEquals(60, row { leading = TSListItemLeading.Icon(TSIcon.Mail) }.height)
        assertEquals(64, row { leading = TSListItemLeading.Avatar("Ada Lovelace") }.height)
        assertEquals(50, row { trailing = TSListItemTrailing.Switch }.height)
        assertEquals(48, row { trailing = TSListItemTrailing.Badge("New", TSBadgeTone.Accent) }.height)
    }

    @Test
    fun largeFontStacksLeadingTextAndTrailing() {
        RuntimeEnvironment.setFontScale(2f)
        val view = row {
            title = "A long title that has to wrap at large text sizes"
            leading = TSListItemLeading.Icon(TSIcon.Mail)
            trailing = TSListItemTrailing.Text("Meta")
            showsChevron = true
            setOnClickListener {}
        }

        assertTrue(view.resolved.isStacked)
        val content = view.getChildAt(0) as LinearLayout
        assertEquals(LinearLayout.VERTICAL, content.orientation)
        val leading = content.getChildAt(0)
        val text = content.getChildAt(1)
        val trailing = content.getChildAt(2)
        assertTrue(text.top >= leading.bottom)
        assertTrue(trailing.top >= text.bottom)
        assertTrue(view.chevronView.left >= content.right)
    }

    @Test
    fun chevronSitsOnTheLeadingEdgeInRightToLeft() {
        val application = RuntimeEnvironment.getApplication()
        application.applicationInfo.flags = application.applicationInfo.flags or ApplicationInfo.FLAG_SUPPORTS_RTL
        val view = row {
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            showsChevron = true
            setOnClickListener {}
        }

        assertTrue(view.chevronView.right <= view.getChildAt(0).left)
    }

    @Test
    fun groupPositionRoundsThePressedBackground() {
        val view = row {
            setOnClickListener {}
            groupPosition = TSListGroupPosition.First
        }
        view.isPressed = true

        val drawable = view.background as com.google.android.material.shape.MaterialShapeDrawable
        assertEquals(view.tsColor(TSColorRole.SurfacePressed), drawable.fillColor?.defaultColor)
        assertTrue(drawable.topLeftCornerResolvedSize > 0f)
        assertEquals(0f, drawable.bottomLeftCornerResolvedSize, 0f)
    }
}
