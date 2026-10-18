package com.ts.ds.views.components.iconbutton

import android.widget.ImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSIconButtonVariant
import com.ts.ds.views.R
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.shared.TSControlBackground
import com.ts.ds.views.components.shared.TSIconSlotDrawable
import com.ts.ds.views.components.tapCenter
import com.ts.ds.views.snapshots.sampleController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
class TSIconButtonViewTest {
    private var taps = 0

    private fun iconButton(configure: TSIconButtonView.() -> Unit = {}): TSIconButtonView = hostInActivity { context ->
        TSIconButtonView(context).apply {
            icon = TSIcon.Search
            label = "Search"
            setOnClickListener { taps++ }
            configure()
        }
    }

    @Test
    fun tapInvokesTheClickListener() {
        iconButton().tapCenter()

        assertEquals(1, taps)
    }

    @Test
    fun disabledBlocksTapsAndDims() {
        val view = iconButton { isEnabled = false }
        view.tapCenter()

        assertEquals(0, taps)
        assertEquals(0.45f, view.alpha, 0.001f)
    }

    @Test
    fun labelIsTheContentDescriptionOfAnImageButton() {
        val view = iconButton()
        val info = AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(view, it) }

        assertEquals("Search", info.contentDescription.toString())
        assertEquals(ImageButton::class.java.name, info.className)
    }

    @Test
    fun is44dpVisuallyInsideA48dpTarget() {
        val view = iconButton { isRound = true }

        assertEquals(48, view.width)
        assertEquals(48, view.height)
        val visual = (view.background as TSControlBackground).visualBounds
        assertEquals(44, visual.width())
        assertEquals(44, visual.height())
    }

    @Test
    fun pressedPrimaryResolvesTheAccentPressedFill() {
        val view = iconButton { variant = TSIconButtonVariant.Primary }
        view.isPressed = true

        assertEquals(TSColorRole.AccentPressed, view.resolved.fill)
        assertEquals(TSColorRole.TextOnAccent, view.resolved.icon)
    }

    @Test
    fun inflatesFromXmlAttributes() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.contentDescription, "More options")
            .addAttribute(R.attr.tsVariant, "secondary")
            .addAttribute(R.attr.tsIcon, "more")
            .addAttribute(R.attr.tsRound, "true")
            .build()
        val view = hostInActivity { TSIconButtonView(it, attrs) }

        assertEquals("More options", view.label.toString())
        assertEquals(TSIconButtonVariant.Secondary, view.variant)
        assertEquals(TSIcon.More, view.icon)
        assertTrue(view.isRound)
    }

    @Test
    fun consumerDrawableIsSizedToTheIconAndKeepsTheLabel() {
        val view = iconButton {
            iconDrawable = context.sampleController(48f)
            label = "Controller"
        }
        val info = AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(view, it) }

        assertEquals(24, view.drawable.intrinsicWidth)
        assertEquals(24, view.drawable.intrinsicHeight)
        assertEquals(24, view.drawable.bounds.width())
        assertEquals("Controller", info.contentDescription.toString())
        assertEquals(48, view.width)

        assertTrue(view.drawable is TSIconSlotDrawable)
        view.iconDrawable = null
        assertTrue(view.drawable != null && view.drawable !is TSIconSlotDrawable)
    }
}
