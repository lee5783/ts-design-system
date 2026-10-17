package com.ts.ds.views.components.bottomnav

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBottomNavItem
import com.ts.ds.views.R
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.tapCenter
import com.ts.ds.views.theme.tsColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSBottomNavViewTest {
    private val five = listOf(
        TSBottomNavItem("home", "Home", TSIcon.Home),
        TSBottomNavItem("inbox", "Inbox", TSIcon.Mail, badge = 3),
        TSBottomNavItem("search", "Search", TSIcon.Search),
        TSBottomNavItem("alerts", "Alerts", TSIcon.Bell, badge = 120),
        TSBottomNavItem("profile", "Profile", TSIcon.User),
    )
    private val changes = mutableListOf<String>()

    private fun nav(widthPx: Int = 320, rtl: Boolean = false, configure: TSBottomNavView.() -> Unit = {}): TSBottomNavView {
        val frame = hostInActivity { context ->
            FrameLayout(context).apply {
                if (rtl) {
                    context.applicationInfo.flags = context.applicationInfo.flags or ApplicationInfo.FLAG_SUPPORTS_RTL
                    layoutDirection = View.LAYOUT_DIRECTION_RTL
                }
                addView(
                    TSBottomNavView(context).apply {
                        items = five
                        label = "Main navigation"
                        reduceMotion = { true }
                        setOnItemSelectedListener { _, id -> changes += id }
                        configure()
                    },
                    FrameLayout.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT),
                )
            }
        }
        idle()
        return frame.getChildAt(0) as TSBottomNavView
    }

    private fun idle() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun TSBottomNavView.item(id: String): TSBottomNavItemView = itemViewList.first { it.item.id == id }

    private fun View.node(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(this, it) }

    @Test
    fun tapSelectsAndNotifiesOnce() {
        val view = nav()

        view.item("search").tapCenter()

        assertEquals("search", view.selectedId)
        assertEquals(listOf("search"), changes)
        assertTrue(view.item("search").isItemSelected)
        assertFalse(view.item("home").isItemSelected)
    }

    @Test
    fun tappingTheSelectedItemIsANoOp() {
        val view = nav()

        view.item("home").tapCenter()

        assertEquals(emptyList<String>(), changes)
    }

    @Test
    fun programmaticSelectionIsSilentAndUnknownFallsBackToTheFirst() {
        val view = nav()

        view.selectedId = "alerts"
        assertTrue(view.item("alerts").isItemSelected)
        view.selectedId = "missing"
        assertTrue(view.item("home").isItemSelected)
        assertEquals("home", view.selectedId)
        assertEquals(emptyList<String>(), changes)
    }

    @Test
    fun disabledBlocksAndDimsEachItem() {
        val view = nav { isEnabled = false }

        view.item("inbox").tapCenter()

        assertEquals(emptyList<String>(), changes)
        view.itemViewList.forEach { assertEquals(0.45f, it.alpha, 0f) }
        assertEquals(1f, view.alpha, 0f)
    }

    @Test
    fun itemsAreTabsWithPositionAndBadgeInTheValue() {
        val view = nav { selectedId = "inbox" }

        val inbox = view.item("inbox").node()
        assertEquals(View::class.java.name, inbox.className)
        assertTrue(inbox.isSelected)
        assertEquals("Inbox", inbox.contentDescription)
        assertEquals("Tab, 2 of 5, 3 new", inbox.stateDescription)
        assertEquals(1, inbox.collectionItemInfo.columnIndex)
        assertEquals("Tab, 4 of 5, 99+ new", view.item("alerts").node().stateDescription)
        assertEquals("Tab, 1 of 5", view.item("home").node().stateDescription)
        assertFalse(view.item("home").node().isSelected)
        listOf(view.item("inbox").badgeView, view.item("inbox").labelView, view.item("inbox").pillView).forEach {
            assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, it.importantForAccessibility)
        }
    }

    @Test
    fun barIsALabelledCollection() {
        val view = nav()

        val node = view.node()
        assertEquals("Main navigation", node.contentDescription)
        assertEquals(5, node.collectionInfo.columnCount)
        assertEquals(1, node.collectionInfo.rowCount)
    }

    @Test
    fun badgesShowFormattedCountsAndUpdateInPlace() {
        val view = nav()
        val inbox = view.item("inbox")

        assertEquals("3", inbox.badgeView.text.toString())
        assertEquals("99+", view.item("alerts").badgeView.text.toString())
        assertEquals(View.GONE, view.item("home").badgeView.visibility)

        view.items = five.map { if (it.id == "inbox") it.badged(null) else it }
        idle()

        assertSame(inbox, view.item("inbox"))
        assertEquals(View.GONE, inbox.badgeView.visibility)
        assertEquals("Tab, 2 of 5", inbox.node().stateDescription)
    }

    @Test
    fun replacingTheIdsRebuildsTheItems() {
        val view = nav()

        view.items = five.take(3)
        idle()

        assertEquals(3, view.itemViewList.size)
        assertEquals("Tab, 3 of 3", view.item("search").node().stateDescription)
    }

    @Test
    fun itemsShareTheWidthAndTheBarIs64WithA56By28Pill() {
        val view = nav()

        assertEquals(64, view.height)
        view.itemViewList.forEach {
            assertEquals(64, it.width)
            assertEquals(64, it.height)
        }
        val pill = view.item("home").pillBounds
        assertEquals(56, pill.width())
        assertEquals(28, pill.height())
        assertEquals(9, pill.top)
        assertEquals(view.tsColor(R.attr.tsColorAccentSubtle), view.item("home").pillFillColor)
        assertEquals(0, view.item("inbox").pillFillColor)
    }

    @Test
    fun pressedUnselectedItemGetsTheSurfacePressedPill() {
        val view = nav()

        view.item("inbox").isPressed = true

        assertEquals(view.tsColor(R.attr.tsColorSurfacePressed), view.item("inbox").pillFillColor)
    }

    @Test
    @Config(sdk = [36])
    fun navigationBarInsetIsAddedBelowThe64Band() {
        val view = nav()

        ViewCompat.dispatchApplyWindowInsets(
            view,
            WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, 24)).build(),
        )
        view.requestLayout()
        idle()

        assertEquals(24, view.paddingBottom)
        assertEquals(88, view.height)
        assertEquals(64, view.item("home").height)
        assertEquals(0, view.item("home").top)
    }

    @Test
    @Config(sdk = [26])
    @Suppress("DEPRECATION")
    fun systemWindowNavigationInsetIsAddedBelowThe64BandAt26() {
        val view = TSBottomNavView(ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_TS)).apply { items = five }

        ViewCompat.dispatchApplyWindowInsets(
            view,
            WindowInsetsCompat.Builder().setSystemWindowInsets(Insets.of(0, 0, 0, 24)).build(),
        )
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        assertEquals(24, view.paddingBottom)
        assertEquals(88, view.height)
        assertEquals(64, view.item("home").height)
        assertEquals(0, view.item("home").top)
    }

    @Test
    fun focusRingIsDrawnOutsideThePillWithAGap() {
        val item = nav().item("home")
        item.draw(Canvas(Bitmap.createBitmap(item.width, item.height, Bitmap.Config.ARGB_8888)))
        val pill = item.pillBounds
        val ring = item.pillRingBounds

        assertEquals(pill.left - 4, ring.left)
        assertEquals(pill.top - 4, ring.top)
        assertEquals(pill.right + 4, ring.right)
        assertEquals(pill.bottom + 4, ring.bottom)
    }

    @Test
    fun badgeSitsThreeAboveThePillAndSixPastTheCentre() {
        val item = nav().item("inbox")

        assertEquals(item.pillBounds.top - 3, item.badgeView.top)
        assertEquals(16, item.badgeView.height)
        assertEquals(item.width / 2 + 6, item.badgeView.left)
    }

    @Test
    fun badgeMirrorsInRtl() {
        val item = nav(rtl = true).item("inbox")

        assertEquals(item.width / 2 - 6, item.badgeView.right)
    }

    @Test
    @Config(fontScale = 2f)
    fun labelScaleIsClampedAtFontScaleTwoSoTheBarStays64() {
        val view = nav()
        val item = view.item("home")

        assertEquals(12f * 1.3f, item.labelView.textSize, 0.01f)
        assertEquals(10f, item.badgeView.textSize, 0.01f)
        assertEquals(64, view.height)
        assertTrue(item.labelView.bottom <= item.height)
        assertNotEquals(9, item.pillBounds.top)
    }
}
