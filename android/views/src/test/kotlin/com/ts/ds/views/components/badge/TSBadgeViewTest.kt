package com.ts.ds.views.components.badge

import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.views.R
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.theme.tsColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class TSBadgeViewTest {
    private fun badge(configure: TSBadgeView.() -> Unit = {}): TSBadgeView = hostInActivity { context ->
        TSBadgeView(context).apply {
            text = "New"
            configure()
        }
    }

    private fun TSBadgeView.nodeInfo(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(this, it) }

    @Test
    fun labelIsTheTextAndItIsStaticText() {
        val view = badge { tone = TSBadgeTone.Success; showsDot = true; icon = TSIcon.Star }
        val info = view.nodeInfo()

        assertEquals("New", info.text.toString())
        assertEquals(TextView::class.java.name, info.className)
        assertFalse(info.isClickable)
        assertFalse(view.isClickable)
        assertFalse(view.isFocusable)
        assertFalse(view.hasOnClickListeners())
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_YES, view.importantForAccessibility)
        (0 until view.childCount).forEach {
            assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, view.getChildAt(it).importantForAccessibility)
        }
    }

    @Test
    fun toneColoursAndNeutralBorder() {
        val neutral = badge()
        assertEquals(TSColorRole.BackgroundSecondary, neutral.resolved.fill)
        assertEquals(TSColorRole.BorderSubtle, neutral.resolved.border)
        assertEquals(neutral.tsColor(TSColorRole.TextSecondary), neutral.label.currentTextColor)
        assertEquals(neutral.tsColor(TSColorRole.BackgroundSecondary), (neutral.background as GradientDrawable).color?.defaultColor)

        val danger = badge { tone = TSBadgeTone.Danger }
        assertEquals(TSColorRole.DangerSubtle, danger.resolved.fill)
        assertEquals(null, danger.resolved.border)
        assertEquals(danger.tsColor(TSColorRole.Danger), danger.label.currentTextColor)
    }

    @Test
    fun dotAndIconToggleAndUpdate() {
        val view = badge()
        val plainWidth = view.width
        assertEquals(View.GONE, view.getChildAt(0).visibility)
        assertEquals(View.GONE, view.getChildAt(1).visibility)

        view.showsDot = true
        view.icon = TSIcon.Star
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        assertEquals(View.VISIBLE, view.getChildAt(0).visibility)
        assertEquals(View.VISIBLE, view.getChildAt(1).visibility)
        assertEquals(6, view.getChildAt(0).measuredWidth)
        assertEquals(16, view.getChildAt(1).measuredWidth)
        assertEquals(plainWidth + 30, view.measuredWidth)

        view.showsDot = false
        view.icon = null
        assertEquals(View.GONE, view.getChildAt(0).visibility)
        assertEquals(View.GONE, view.getChildAt(1).visibility)
    }

    @Test
    fun heightIs22AtDefaultText() {
        assertEquals(22, badge().height)
    }

    @Test
    @Config(fontScale = 2f)
    fun heightGrowsAt200PercentText() {
        assertTrue(badge().height > 22)
    }

    @Test
    fun longTextTruncatesOnOneLineWithAnEllipsis() {
        val view = badge { text = "Awaiting review from the platform accessibility team" }
        view.measure(View.MeasureSpec.makeMeasureSpec(120, View.MeasureSpec.AT_MOST), View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        assertEquals(120, view.measuredWidth)
        assertEquals(22, view.measuredHeight)
        assertEquals(TextUtils.TruncateAt.END, view.label.ellipsize)
        assertEquals(1, view.label.lineCount)
        assertTrue(view.label.layout.getEllipsisCount(0) > 0)
    }

    @Test
    fun xmlAttributesConfigureTheBadge() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.text, "Beta")
            .addAttribute(R.attr.tsBadgeTone, "info")
            .addAttribute(R.attr.tsShowsDot, "true")
            .addAttribute(R.attr.tsIcon, "star")
            .build()
        val view = hostInActivity { TSBadgeView(it, attrs) }

        assertEquals("Beta", view.text.toString())
        assertEquals(TSBadgeTone.Info, view.tone)
        assertTrue(view.showsDot)
        assertEquals(TSIcon.Star, view.icon)
    }
}
