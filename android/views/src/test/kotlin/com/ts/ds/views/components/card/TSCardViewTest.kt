package com.ts.ds.views.components.card

import android.graphics.Rect
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.drawToBitmap
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSCardAccessory
import com.ts.ds.core.logic.TSCardAction
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.iconbutton.TSIconButtonView
import com.ts.ds.views.components.tapCenter
import com.ts.ds.views.theme.tsColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class TSCardViewTest {
    private val events = mutableListOf<String>()

    private val footer = listOf(
        TSCardAction("Share") { events += "share" },
        TSCardAction("Open") { events += "open" },
    )

    private fun card(configure: TSCardView.() -> Unit = {}): TSCardView = hostInActivity { context ->
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(
                TSCardView(context).apply {
                    title = "Plan"
                    subtitle = "Pro"
                    bodyText = "Billed monthly"
                    configure()
                },
                LinearLayout.LayoutParams(320, LinearLayout.LayoutParams.WRAP_CONTENT),
            )
        }
    }.getChildAt(0) as TSCardView

    private fun interactiveCard(configure: TSCardView.() -> Unit = {}): TSCardView = card {
        footerActions = footer
        accessibilityActions = listOf(TSCardAction("More options") { events += "more" })
        setOnClickListener { events += "card" }
        configure()
    }

    private fun TSCardView.node(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(this, it) }

    private fun View.boundsIn(ancestor: View): Rect {
        val location = IntArray(2).also(::getLocationInWindow)
        val origin = IntArray(2).also(ancestor::getLocationInWindow)
        return Rect(location[0] - origin[0], location[1] - origin[1], location[0] - origin[0] + width, location[1] - origin[1] + height)
    }

    private fun View.tapAt(x: Float, y: Float) {
        val time = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(time, time, action, x, y, 0)
            dispatchTouchEvent(event)
            event.recycle()
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun TSCardView.tapOn(child: View) {
        val bounds = child.boundsIn(this)
        tapAt(bounds.exactCenterX(), bounds.exactCenterY())
    }

    @Test
    fun interactiveCardIsOneButtonElement() {
        val view = interactiveCard()
        val node = view.node()

        assertEquals(Button::class.java.name, node.className)
        assertEquals("Plan, Pro", node.contentDescription)
        assertTrue(ViewCompat.isScreenReaderFocusable(view))
        view.footerButtons.forEach { assertFalse(it.isImportantForAccessibility) }
    }

    @Test
    fun tapAndActivationPerformTheAction() {
        val view = interactiveCard()

        view.tapCenter()
        ViewCompat.performAccessibilityAction(view, AccessibilityNodeInfoCompat.ACTION_CLICK, null)

        assertEquals(listOf("card", "card"), events)
    }

    @Test
    fun footerThenExtraActionsAreCustomActionsThatFire() {
        val view = interactiveCard()
        val custom = view.node().actionList.filter { it.label != null }

        assertEquals(listOf("Share", "Open", "More options"), custom.map { it.label.toString() })
        custom.forEach { ViewCompat.performAccessibilityAction(view, it.id, null) }
        assertEquals(listOf("share", "open", "more"), events)
    }

    @Test
    fun footerButtonsStayTappableWithoutTriggeringTheCard() {
        val view = interactiveCard()

        view.tapOn(view.footerButtons[1])
        view.tapOn(view.footerButtons[0])

        assertEquals(listOf("open", "share"), events)
    }

    @Test
    fun accessoryStaysTappable() {
        lateinit var accessory: TSIconButtonView
        val view = interactiveCard {
            accessory = TSIconButtonView(context).apply {
                icon = TSIcon.More
                label = "More options"
                setOnClickListener { events += "accessory" }
            }
            accessoryView = accessory
        }

        view.tapOn(accessory)
        view.tapAt(8f, view.height / 2f)

        assertEquals(listOf("accessory", "card"), events)
    }

    @Test
    fun staticCardIsAContainerWithAHeaderTitle() {
        val view = card { footerActions = footer }

        assertFalse(view.isClickable)
        assertFalse(view.node().className == Button::class.java.name)
        view.footerButtons.forEach { assertTrue(it.isImportantForAccessibility) }
        assertTrue(view.node().actionList.none { it.label != null })
        view.tapOn(view.footerButtons[1])
        assertEquals(listOf("open"), events)
        val title = (0 until view.childCount).flatMap { view.textViews(view.getChildAt(it)) }.first { it.text == "Plan" }
        assertTrue(ViewCompat.isAccessibilityHeading(title))
    }

    @Test
    fun disabledCardBlocksAndDropsCustomActions() {
        val view = interactiveCard { isEnabled = false }

        view.tapCenter()
        view.tapOn(view.footerButtons[1])

        assertEquals(emptyList<String>(), events)
        assertTrue(view.node().actionList.none { it.label != null })
        view.footerButtons.forEach { assertFalse(it.isEnabled) }
        assertEquals(0.45f, view.alpha, 0f)
    }

    @Test
    fun pressedFillOnlyOnAnInteractiveCard() {
        val interactive = interactiveCard { isPressed = true }
        val static = card { isPressed = true }

        assertEquals(interactive.tsColor(TSColorRole.SurfacePressed), interactive.drawToBitmap().getPixel(6, 40))
        assertEquals(static.tsColor(TSColorRole.SurfaceCard), static.drawToBitmap().getPixel(6, 40))
    }

    @Test
    fun raisedCastsAShadowAndClipsToTheShape() {
        val view = card { isRaised = true }

        assertTrue(view.elevation > 0f)
        assertTrue(view.clipToOutline)
        assertEquals(0f, card().elevation, 0f)
    }

    @Test
    fun heightsMatchTheDesignAtTheDefaultSize() {
        assertEquals(56, card { bodyText = null; subtitle = null }.height)
        assertEquals(92, card { subtitle = null }.height)
        assertEquals(165, card { subtitle = null; footerActions = footer }.height)
    }

    @Test
    fun footerStacksAtLargeFontSizes() {
        RuntimeEnvironment.setFontScale(2f)
        val view = interactiveCard()
        val share = view.footerButtons[0].boundsIn(view)
        val open = view.footerButtons[1].boundsIn(view)

        assertTrue(open.top >= share.bottom)
        assertEquals(share.right, open.right)
    }

    @Test
    fun disabledCardDimsItsFooterAndAccessoryOnce() {
        val view = interactiveCard {
            accessory = TSCardAccessory.IconButton(TSIcon.More, "Card options") {}
            isEnabled = false
        }

        assertEquals(0.45f, view.alpha, 0f)
        view.footerButtons.forEach { assertEquals(1f, it.alpha, 0f) }
        assertEquals(1f, requireNotNull(view.accessoryView).alpha, 0f)

        view.isEnabled = true
        view.footerButtons.forEach { assertEquals(1f, it.alpha, 0f) }
        assertEquals(1f, view.alpha, 0f)
    }

    @Test
    fun aReplacedAccessoryViewNoLongerFollowsTheDisabledCard() {
        lateinit var previous: TSIconButtonView
        val view = interactiveCard {
            previous = TSIconButtonView(context).apply {
                icon = TSIcon.Search
                label = "Search"
            }
            accessoryView = previous
            isEnabled = false
        }
        assertTrue(previous.isDimmedByContainer)
        assertFalse(previous.isEnabled)

        view.accessoryView = TSIconButtonView(view.context).apply {
            icon = TSIcon.More
            label = "More"
        }

        assertFalse(previous.isDimmedByContainer)
        assertTrue(previous.isEnabled)
        assertEquals(1f, previous.alpha, 0f)
        previous.isEnabled = false
        assertTrue(previous.alpha < 1f)
    }

    @Test
    fun bodyIsTheStateDescriptionBesideAHeadingAndTheLabelWithoutOne() {
        assertEquals("Billed monthly", interactiveCard().node().stateDescription)

        val untitled = interactiveCard {
            title = null
            subtitle = null
        }
        assertEquals("Billed monthly", untitled.node().contentDescription)
        assertEquals(null, untitled.node().stateDescription)
    }

    @Test
    fun typedAccessoryIsAnIconButtonAndACustomActionBetweenFooterAndExtra() {
        val view = interactiveCard {
            accessory = TSCardAccessory.IconButton(TSIcon.More, "Card options") { events += "accessory" }
        }
        val accessory = view.accessoryView as TSIconButtonView
        val custom = view.node().actionList.filter { it.label != null }

        assertEquals("Card options", accessory.contentDescription)
        assertEquals(listOf("Share", "Open", "Card options", "More options"), custom.map { it.label.toString() })
        ViewCompat.performAccessibilityAction(view, custom[2].id, null)
        view.tapOn(accessory)
        assertEquals(listOf("accessory", "accessory"), events)
    }

    @Test
    fun settingAnAccessoryViewClearsTheTypedAccessoryAndItsCustomAction() {
        val view = interactiveCard {
            accessory = TSCardAccessory.IconButton(TSIcon.More, "Card options") { events += "accessory" }
        }
        val replacement = TSIconButtonView(view.context).apply {
            icon = TSIcon.Search
            label = "Search"
        }

        view.accessoryView = replacement

        assertEquals(null, view.accessory)
        assertTrue(view.accessoryView === replacement)
        assertEquals(listOf("Share", "Open", "More options"), view.node().actionList.mapNotNull { it.label?.toString() })
    }

    @Test
    fun settingATypedAccessoryAfterAnAccessoryViewReplacesIt() {
        val view = interactiveCard {
            accessoryView = TSIconButtonView(context).apply {
                icon = TSIcon.Search
                label = "Search"
            }
        }

        view.accessory = TSCardAccessory.IconButton(TSIcon.More, "Card options") {}

        assertEquals("Card options", (view.accessoryView as TSIconButtonView).contentDescription)
        assertEquals(listOf("Share", "Open", "Card options", "More options"), view.node().actionList.mapNotNull { it.label?.toString() })
    }

    private fun TSCardView.textViews(root: View): List<android.widget.TextView> = when (root) {
        is android.widget.TextView -> listOf(root)
        is android.view.ViewGroup -> (0 until root.childCount).flatMap { textViews(root.getChildAt(it)) }
        else -> emptyList()
    }
}
