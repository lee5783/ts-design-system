package com.ts.ds.demo.views

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.views.components.AvatarFragment
import com.ts.ds.demo.views.components.BadgeFragment
import com.ts.ds.demo.views.components.CardFragment
import com.ts.ds.demo.views.components.ListItemFragment
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentComponentsTest {
    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun tap(tag: String) {
        onView(withTag(tag)).perform(scrollIntoView(), click())
    }

    private fun ActivityScenario<MainActivity>.nodeOf(tag: String): Pair<View, AccessibilityNodeInfoCompat> {
        onView(withTag(tag)).perform(scrollIntoView())
        var result: Pair<View, AccessibilityNodeInfoCompat>? = null
        onActivity { activity ->
            val view = activity.window.decorView.findViewWithTag<View>(tag)
            val info = AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(view, it) }
            result = view to info
        }
        return requireNotNull(result)
    }

    @Test
    fun badgeIsStaticTextLabelledByItsText() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Badge)
            val (plain, info) = scenario.nodeOf(BadgeFragment.PLAYGROUND_BADGE)
            val plainWidth = plain.width
            assertEquals(resources.getString(DemoR.string.demo_sample_draft), info.text.toString())
            assertEquals(TextView::class.java.name, info.className)
            assertFalse(info.isClickable)
            assertFalse(plain.isClickable)

            tap(BadgeFragment.PLAYGROUND_DOT)
            tap(BadgeFragment.PLAYGROUND_ICON)

            val (decorated, decoratedInfo) = scenario.nodeOf(BadgeFragment.PLAYGROUND_BADGE)
            assertEquals(resources.getString(DemoR.string.demo_sample_draft), decoratedInfo.text.toString())
            assertFalse(decoratedInfo.isClickable)
            assertTrue(decorated.width > plainWidth)
        }
    }

    @Test
    fun avatarIsAnImageLabelledByTheName() {
        val name = SampleData.people.first().name
        launchDemo().use { scenario ->
            openPage(DemoPageId.Avatar)
            val (_, info) = scenario.nodeOf(AvatarFragment.PLAYGROUND_AVATAR)
            assertEquals(name, info.contentDescription.toString())
            assertEquals(ImageView::class.java.name, info.className)
            assertFalse(info.isClickable)

            tap(AvatarFragment.PLAYGROUND_PHOTO)

            val (_, photoInfo) = scenario.nodeOf(AvatarFragment.PLAYGROUND_AVATAR)
            assertEquals(name, photoInfo.contentDescription.toString())
            assertEquals(ImageView::class.java.name, photoInfo.className)
        }
    }

    private fun tapAt(tag: String, x: (View) -> Float) {
        val coordinates = CoordinatesProvider { view ->
            val location = IntArray(2).also(view::getLocationOnScreen)
            floatArrayOf(location[0] + x(view), location[1] + view.height / 2f)
        }
        onView(withTag(tag)).perform(scrollIntoView(), GeneralClickAction(Tap.SINGLE, coordinates, Press.FINGER, 0, 0))
    }

    private fun tapSwitchOf(tag: String) {
        tapAt(tag) { view -> view.width - (16 + 22) * view.resources.displayMetrics.density }
    }

    private fun tapTitleOf(tag: String) {
        tapAt(tag) { view -> view.width * 0.3f }
    }

    private fun assertCount(tag: String, count: Int) {
        onView(withTag(tag)).perform(scrollIntoView()).check(matches(withText(resources.getQuantityString(DemoR.plurals.demo_component_tap_count, count, count))))
    }

    private fun assertText(tag: String, text: Int) {
        onView(withTag(tag)).perform(scrollIntoView()).check(matches(withText(text)))
    }

    @Test
    fun listItemRowsPressOnlyWithAnAction() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.ListItem)
            val (_, info) = scenario.nodeOf(ListItemFragment.STATE_DEFAULT)
            assertEquals(Button::class.java.name, info.className)
            assertTrue(info.isEnabled)

            tap(ListItemFragment.STATE_DEFAULT)
            assertCount(ListItemFragment.STATE_COUNT, 1)

            val (disabled, disabledInfo) = scenario.nodeOf(ListItemFragment.STATE_DISABLED)
            assertFalse(disabledInfo.isEnabled)
            assertFalse(disabled.isEnabled)
            tapTitleOf(ListItemFragment.STATE_DISABLED)
            val (static, staticInfo) = scenario.nodeOf(ListItemFragment.STATE_STATIC)
            assertFalse(static.isClickable)
            assertFalse(staticInfo.isClickable)
            tapTitleOf(ListItemFragment.STATE_STATIC)

            assertCount(ListItemFragment.STATE_COUNT, 1)
        }
    }

    @Test
    fun listItemSwitchRowExposesSwitchSemantics() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.ListItem)
            val (_, info) = scenario.nodeOf(ListItemFragment.WIFI)
            assertEquals(SwitchCompat::class.java.name, info.className)
            assertTrue(info.isCheckable)
            assertTrue(info.isChecked)
            assertEquals(resources.getString(DemoR.string.demo_sample_wifi), info.text.toString())
            assertEquals(resources.getString(DemoR.string.demo_state_on), info.stateDescription.toString())

            tapTitleOf(ListItemFragment.WIFI)
            assertTrue(scenario.nodeOf(ListItemFragment.WIFI).second.isChecked)

            tapSwitchOf(ListItemFragment.WIFI)
            assertFalse(scenario.nodeOf(ListItemFragment.WIFI).second.isChecked)
            assertText(ListItemFragment.WIFI_VALUE, DemoR.string.demo_state_off)
        }
    }

    @Test
    fun listItemPlaygroundTapCountsAndSwitchTrailing() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.ListItem)
            tap(ListItemFragment.PLAYGROUND_ROW)
            tap(ListItemFragment.PLAYGROUND_ROW)
            assertCount(ListItemFragment.PLAYGROUND_COUNT, 2)

            tap("demo.list-item.playground.trailing.switch")
            onView(withTag("demo.list-item.playground.trailing.switch")).check(matches(isChecked()))
            val (_, info) = scenario.nodeOf(ListItemFragment.PLAYGROUND_ROW)
            assertEquals(SwitchCompat::class.java.name, info.className)
            assertFalse(info.isChecked)
            tapSwitchOf(ListItemFragment.PLAYGROUND_ROW)
            assertTrue(scenario.nodeOf(ListItemFragment.PLAYGROUND_ROW).second.isChecked)
            assertText(ListItemFragment.PLAYGROUND_VALUE, DemoR.string.demo_state_on)

            tap(ListItemFragment.PLAYGROUND_DISABLED)
            tapSwitchOf(ListItemFragment.PLAYGROUND_ROW)
            val (_, disabledInfo) = scenario.nodeOf(ListItemFragment.PLAYGROUND_ROW)
            assertFalse(disabledInfo.isEnabled)
            assertTrue(disabledInfo.isChecked)
            assertCount(ListItemFragment.PLAYGROUND_COUNT, 2)
        }
    }

    private fun tapAtPoint(tag: String, x: (View) -> Float, y: (View) -> Float) {
        val coordinates = CoordinatesProvider { view ->
            val location = IntArray(2).also(view::getLocationOnScreen)
            floatArrayOf(location[0] + x(view), location[1] + y(view))
        }
        onView(withTag(tag)).perform(scrollIntoView(), GeneralClickAction(Tap.SINGLE, coordinates, Press.FINGER, 0, 0))
    }

    @Test
    fun interactiveCardIsOneElementAndPresses() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Card)
            val (card, info) = scenario.nodeOf(CardFragment.STATE_DEFAULT)
            assertEquals(Button::class.java.name, info.className)
            assertEquals(
                "${resources.getString(DemoR.string.demo_component_default)}, ${resources.getString(DemoR.string.demo_component_interactive)}",
                info.contentDescription.toString(),
            )
            assertEquals(
                listOf(resources.getString(DemoR.string.demo_sample_share), resources.getString(DemoR.string.demo_sample_open)),
                info.actionList.mapNotNull { it.label?.toString() },
            )
            assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS, (card as android.view.ViewGroup).getChildAt(0).importantForAccessibility)

            tapAtPoint(CardFragment.STATE_DEFAULT, { it.width * 0.3f }, { 40 * it.resources.displayMetrics.density })
            assertCount(CardFragment.STATE_COUNT, 1)

            val (disabled, disabledInfo) = scenario.nodeOf(CardFragment.STATE_DISABLED)
            assertFalse(disabled.isEnabled)
            assertFalse(disabledInfo.isEnabled)
            tapAtPoint(CardFragment.STATE_DISABLED, { it.width * 0.3f }, { it.height / 2f })
            assertCount(CardFragment.STATE_COUNT, 1)
        }
    }

    @Test
    fun cardFooterButtonsStayTappable() {
        launchDemo().use {
            openPage(DemoPageId.Card)
            val density = resources.displayMetrics.density
            tapAtPoint(CardFragment.STATE_DEFAULT, { it.width - 40 * density }, { it.height - 36 * density })

            onView(withTag(CardFragment.STATE_LAST_ACTION)).perform(scrollIntoView())
                .check(matches(withText(resources.getString(DemoR.string.demo_component_last_action, resources.getString(DemoR.string.demo_sample_open)))))
            assertCount(CardFragment.STATE_COUNT, 0)
        }
    }
}
