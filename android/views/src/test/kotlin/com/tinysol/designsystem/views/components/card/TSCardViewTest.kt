package com.tinysol.designsystem.views.components.card

import android.graphics.Rect
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.drawToBitmap
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.logic.TSCardAccessory
import com.tinysol.designsystem.core.logic.TSCardAction
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.views.components.hostInActivity
import com.tinysol.designsystem.views.components.iconbutton.TSIconButtonView
import com.tinysol.designsystem.views.components.tapCenter
import com.tinysol.designsystem.views.theme.tsColor
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
        setOnClickListener { events += "card" }
        configure()
    }

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
    fun tapPerformsTheAction() {
        val view = interactiveCard()

        view.tapCenter()

        assertEquals(listOf("card"), events)
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
    fun staticCardIsNotClickable() {
        val view = card { footerActions = footer }

        assertFalse(view.isClickable)
        view.tapOn(view.footerButtons[1])
        assertEquals(listOf("open"), events)
    }

    @Test
    fun disabledCardBlocksTaps() {
        val view = interactiveCard { isEnabled = false }

        view.tapCenter()
        view.tapOn(view.footerButtons[1])

        assertEquals(emptyList<String>(), events)
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
    fun typedAccessoryIsAnIconButtonThatFires() {
        val view = interactiveCard {
            accessory = TSCardAccessory.IconButton(TSIcon.More, "Card options") { events += "accessory" }
        }
        val accessory = view.accessoryView as TSIconButtonView

        view.tapOn(accessory)
        assertEquals(listOf("accessory"), events)
    }

    @Test
    fun settingAnAccessoryViewClearsTheTypedAccessory() {
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

        assertTrue(view.accessoryView is TSIconButtonView)
    }
}
