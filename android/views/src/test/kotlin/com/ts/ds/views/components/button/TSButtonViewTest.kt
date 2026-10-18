package com.ts.ds.views.components.button

import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.drawToBitmap
import androidx.core.widget.ImageViewCompat
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSButtonSize
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.views.R
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.shared.TSSpinnerDrawable
import com.ts.ds.views.components.tapCenter
import com.ts.ds.views.snapshots.sampleController
import com.ts.ds.views.theme.tsColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSButtonViewTest {
    private var taps = 0

    private fun button(configure: TSButtonView.() -> Unit = {}): TSButtonView = hostInActivity { context ->
        TSButtonView(context).apply {
            text = "Save"
            setOnClickListener { taps++ }
            configure()
        }
    }

    private fun TSButtonView.nodeInfo(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(this, it) }

    @Test
    fun tapInvokesTheClickListener() {
        button().tapCenter()

        assertEquals(1, taps)
    }

    @Test
    fun disabledBlocksTaps() {
        val view = button { isEnabled = false }
        view.tapCenter()

        assertEquals(0, taps)
        assertEquals(0.45f, view.alpha, 0.001f)
    }

    @Test
    fun loadingBlocksTapsWithoutDimmingAndReportsLoading() {
        val view = button { isLoading = true }
        view.tapCenter()

        assertEquals(0, taps)
        assertEquals(1f, view.alpha, 0f)
        assertEquals("Loading", ViewCompat.getStateDescription(view))
        assertFalse(view.nodeInfo().isEnabled)
    }

    @Test
    fun loadingClearsWhenFinishedAndRestoresTheUserEnabledState() {
        val view = button { isLoading = true }
        view.isLoading = false

        assertTrue(view.isEnabled)
        assertNull(ViewCompat.getStateDescription(view))
        view.tapCenter()
        assertEquals(1, taps)
    }

    @Test
    fun accessibilityNodeIsAButtonNamedByItsLabel() {
        val info = button { leadingIcon = TSIcon.Check }.nodeInfo()

        assertEquals(Button::class.java.name, info.className)
        assertEquals("Save", info.text.toString())
        assertTrue(info.isEnabled)
    }

    @Test
    fun spinnerReplacesTheLeadingIconWhileLoading() {
        val view = button { leadingIcon = TSIcon.Plus }
        val leading = view.getChildAt(0) as ImageView
        assertFalse(leading.drawable is TSSpinnerDrawable)

        view.isLoading = true
        assertTrue(leading.drawable is TSSpinnerDrawable)
        assertEquals(ImageView.IMPORTANT_FOR_ACCESSIBILITY_NO, leading.importantForAccessibility)
    }

    @Test
    fun mediumIs44dpVisuallyInsideA48dpTarget() {
        val view = button()

        assertEquals(48, view.height)
        assertEquals(44, (view.background as com.ts.ds.views.components.shared.TSControlBackground).visualBounds.height())
    }

    @Test
    fun largeIs52dpTall() {
        assertEquals(52, button { size = TSButtonSize.Lg }.height)
    }

    @Test
    fun linkHasA48dpTouchTargetAndIsUnderlined() {
        val view = button { variant = TSButtonVariant.Link }
        val label = view.getChildAt(1) as TextView

        assertTrue(view.height >= 48 && view.width >= 48)
        assertTrue(label.paintFlags and android.graphics.Paint.UNDERLINE_TEXT_FLAG != 0)
        view.tapCenter()
        assertEquals(1, taps)
    }

    @Test
    fun pressedStateResolvesThePressedFill() {
        val view = button { variant = TSButtonVariant.Danger }
        view.isPressed = true

        assertEquals(TSColorRole.DangerPressed, view.resolved.fill)
    }

    @Test
    fun pressedLinkDropsToThePressedOpacity() {
        val view = button { variant = TSButtonVariant.Link }
        view.isPressed = true

        assertEquals(0.85f, view.alpha, 0.001f)
    }

    @Test
    fun labelUsesTheMediumFamily() {
        val label = button { size = TSButtonSize.Lg }.getChildAt(1) as TextView

        assertSame(Typeface.create("sans-serif-medium", Typeface.NORMAL), label.typeface)
    }

    @Test
    fun inflatesFromXmlAttributes() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.text, "Delete")
            .addAttribute(R.attr.tsVariant, "danger")
            .addAttribute(R.attr.tsSize, "lg")
            .addAttribute(R.attr.tsLeadingIcon, "trash")
            .addAttribute(R.attr.tsTrailingIcon, "arrow-right")
            .addAttribute(R.attr.tsLoading, "true")
            .build()
        val view = hostInActivity { TSButtonView(it, attrs) }

        assertEquals("Delete", view.text.toString())
        assertEquals(TSButtonVariant.Danger, view.variant)
        assertEquals(TSButtonSize.Lg, view.size)
        assertEquals(TSIcon.Trash, view.leadingIcon)
        assertEquals(TSIcon.ArrowRight, view.trailingIcon)
        assertTrue(view.isLoading)
    }

    @Test
    fun consumerDrawablesFillTheIconSlotsAndAreTinted() {
        val view = button {
            leadingIconDrawable = context.sampleController(48f)
            trailingIconDrawable = context.sampleController()
        }
        val reference = button { leadingIcon = TSIcon.Plus; trailingIcon = TSIcon.Plus }
        val leading = view.getChildAt(0) as ImageView
        val trailing = view.getChildAt(2) as ImageView

        listOf(leading, trailing).forEach {
            assertEquals(View.VISIBLE, it.visibility)
            assertEquals(20, it.width)
            assertEquals(20, it.height)
            assertEquals(view.tsColor(view.resolved.label), ImageViewCompat.getImageTintList(it)?.defaultColor)
            assertEquals(ImageView.IMPORTANT_FOR_ACCESSIBILITY_NO, it.importantForAccessibility)
        }
        assertEquals(reference.width, view.width)
        assertEquals(reference.height, view.height)
        val labelRgb = view.tsColor(view.resolved.label) and 0xFFFFFF
        val inked = leading.drawToBitmap().let { bitmap ->
            (0 until bitmap.width).flatMap { x -> (0 until bitmap.height).map { y -> bitmap.getPixel(x, y) } }
        }.filter { Color.alpha(it) == 255 }
        assertTrue(inked.isNotEmpty())
        assertTrue(inked.all { it and 0xFFFFFF == labelRgb })
    }

    @Test
    fun spinnerReplacesOnlyTheLeadingConsumerDrawable() {
        val view = button {
            leadingIconDrawable = context.sampleController()
            trailingIconDrawable = context.sampleController()
            isLoading = true
        }

        assertTrue((view.getChildAt(0) as ImageView).drawable is TSSpinnerDrawable)
        assertEquals(View.VISIBLE, view.getChildAt(2).visibility)
    }

    @Test
    fun maxLinesKeepsTheLabelOnOneLineWithAnEllipsis() {
        val title = "Join the session with a very long label"
        val limited = hostInActivity { context ->
            android.widget.FrameLayout(context).apply {
                addView(
                    TSButtonView(context).apply {
                        text = title
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    },
                    android.widget.FrameLayout.LayoutParams(160, android.widget.FrameLayout.LayoutParams.WRAP_CONTENT),
                )
            }
        }.getChildAt(0) as TSButtonView
        val label = limited.getChildAt(1) as TextView

        assertEquals(1, label.lineCount)
        assertTrue(label.layout.getEllipsisCount(0) > 0)
        assertEquals(48, limited.height)
    }

    @Test
    fun inflatesLineControlFromXml() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.text, "Join")
            .addAttribute(android.R.attr.maxLines, "1")
            .addAttribute(android.R.attr.ellipsize, "end")
            .build()
        val view = hostInActivity { TSButtonView(it, attrs) }

        assertEquals(1, view.maxLines)
        assertEquals(TextUtils.TruncateAt.END, view.ellipsize)
        assertEquals(Int.MAX_VALUE, button().maxLines)
        assertNull(button().ellipsize)
    }
}
