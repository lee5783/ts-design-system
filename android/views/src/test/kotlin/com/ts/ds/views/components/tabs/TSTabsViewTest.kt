package com.ts.ds.views.components.tabs

import android.os.Looper
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.haptics.DsHaptics
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.core.style.TSTabsVariant
import com.ts.ds.views.R
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.tapCenter
import com.ts.ds.views.foundation.TSTouchDelegateGroup
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSTabsViewTest {
    private val ranges = listOf("Day", "Week", "Month", "Year").map { TSTabItem(it.lowercase(), it) }
    private val sections = listOf(
        "Overview", "Activity", "Members", "Billing", "Settings", "Integrations", "Security", "Notifications", "Reports",
    ).map { TSTabItem(it.lowercase(), it) }
    private val changes = mutableListOf<String>()

    private fun tabs(widthPx: Int = 300, configure: TSTabsView.() -> Unit = {}): TSTabsView {
        val frame = hostInActivity { context ->
            FrameLayout(context).apply {
                addView(
                    TSTabsView(context).apply {
                        items = ranges
                        reduceMotion = { false }
                        setOnTabSelectedListener { _, id -> changes += id }
                        configure()
                    },
                    FrameLayout.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT),
                )
            }
        }
        idle()
        return frame.getChildAt(0) as TSTabsView
    }

    private fun idle() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun TSTabsView.tab(id: String): TSTabView = tabs.first { it.item.id == id }

    private fun TSTabsView.leftInContent(id: String): Int = row.left + tab(id).left

    @Test
    fun shortUnderlineTabsGetA48dpTouchTargetWithoutChangingTheirLayout() {
        val view = tabs()
        val day = view.tab("day")
        val bounds = (view.row.touchDelegate as TSTouchDelegateGroup).bounds.getValue(day)

        assertTrue(day.width < 48)
        assertEquals(48, bounds.width())
        assertEquals(day.left + day.width / 2, bounds.centerX())
    }

    @Test
    fun tapSelectsAndNotifiesOnce() {
        val view = tabs()

        view.tab("week").tapCenter()

        assertEquals("week", view.selectedId)
        assertEquals(listOf("week"), changes)
        assertTrue(view.tab("week").isSelected)
        assertFalse(view.tab("day").isSelected)
    }

    @Test
    fun tappingTheSelectedTabIsANoOp() {
        val view = tabs()

        view.tab("day").tapCenter()

        assertEquals(emptyList<String>(), changes)
    }

    @Test
    fun programmaticSelectionIsSilent() {
        val view = tabs()

        view.selectedId = "year"
        idle()

        assertTrue(view.tab("year").isSelected)
        assertEquals(emptyList<String>(), changes)
    }

    @Test
    fun disabledBlocksAndDimsOnce() {
        val view = tabs { isEnabled = false }

        view.tab("week").tapCenter()

        assertTrue(view.tab("day").isSelected)
        assertEquals(emptyList<String>(), changes)
        assertEquals(0.45f, view.alpha, 0.001f)
        assertEquals(1f, view.tab("week").alpha, 0f)
        assertFalse(view.tab("week").isEnabled)
    }

    @Test
    fun selectedTabIsMarkedSelected() {
        val view = tabs { selectedId = "week" }

        assertTrue(view.tab("week").isSelected)
        assertFalse(view.tab("day").isSelected)
    }

    @Test
    fun segmentedIsA46TrackWith38SegmentsInA48TouchBox() {
        val view = tabs { variant = TSTabsVariant.Segmented; selectedId = "week" }

        assertEquals(48, view.height)
        assertEquals(46, view.trackBounds.height())
        assertEquals(1, view.trackBounds.top)
        assertEquals(48, view.tab("day").height)
        assertEquals(38, view.thumbView.height)
        assertEquals(view.leftInContent("week"), view.thumbView.left)
        assertEquals(view.tab("week").width, view.thumbView.width)
        assertEquals(view.scrollView.width, view.trackBounds.width())
    }

    @Test
    fun segmentChangePlaysTheSelectionHaptic() {
        val view = tabs { variant = TSTabsVariant.Segmented }

        view.tab("week").tapCenter()

        assertEquals(DsHaptics.constantFor(DsHapticEvent.Selection), shadowOf(view).lastHapticFeedbackPerformed())
    }

    @Test
    fun underlineChangeHasNoHaptic() {
        val view = tabs()

        view.tab("week").tapCenter()

        assertEquals(-1, shadowOf(view).lastHapticFeedbackPerformed())
    }

    @Test
    fun blockSegmentsShareTheWidth() {
        val view = tabs(widthPx = 248) { variant = TSTabsVariant.Segmented; isBlock = true }

        view.tabs.forEach { assertEquals(60, it.width) }
    }

    @Test
    fun underlineTabsAre48TallWithTheIndicatorUnderTheSelection() {
        val view = tabs { selectedId = "month" }
        val tab = view.tab("month")

        assertEquals(48, tab.height)
        assertEquals(view.leftInContent("month"), view.indicatorView.left)
        assertEquals(tab.width, view.indicatorView.width)
        assertEquals(2, view.indicatorView.height)
        assertEquals(view.row.top + tab.bottom - 2, view.indicatorView.bottom)
    }

    @Test
    fun underlineOverflowScrollsAndSelectionScrollsIntoView() {
        val view = tabs { items = sections }
        val content = view.scrollView.getChildAt(0)
        assertTrue(content.width > view.scrollView.width)
        assertEquals(0, view.scrollView.scrollX)

        view.selectedId = "reports"
        idle()

        val reports = view.tab("reports")
        assertTrue(view.scrollView.scrollX > 0)
        assertTrue(view.row.left + reports.right <= view.scrollView.scrollX + view.scrollView.width)
        assertEquals(view.leftInContent("reports"), view.indicatorView.left)
    }

    @Test
    fun userSelectionSlidesTheIndicatorOver150Milliseconds() {
        val view = tabs()

        view.tab("year").performClick()
        val animator = requireNotNull(view.runningAnimator)
        assertEquals(150L, animator.duration)
        assertTrue(animator.interpolator is PathInterpolator)

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300))
        assertEquals(view.leftInContent("year"), view.indicatorView.left)
    }

    @Test
    fun reduceMotionMovesTheIndicatorImmediately() {
        val view = tabs { reduceMotion = { true } }

        view.tab("year").performClick()
        idle()

        assertNull(view.runningAnimator)
        assertEquals(view.leftInContent("year"), view.indicatorView.left)
    }

    @Test
    fun replacingItemsRebuildsAndFallsBackToTheFirst() {
        val view = tabs { selectedId = "week" }

        view.items = sections
        idle()

        assertEquals(9, view.tabs.size)
        assertTrue(view.tab("overview").isSelected)
    }

    @Test
    fun inflatesFromXmlAttributes() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(R.attr.tsTabsVariant, "segmented")
            .addAttribute(R.attr.tsBlock, "true")
            .build()
        val view = hostInActivity { TSTabsView(it, attrs) }

        assertEquals(TSTabsVariant.Segmented, view.variant)
        assertTrue(view.isBlock)
    }
}
