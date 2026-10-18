package com.tinysol.designsystem.views.sample

import android.view.View
import android.widget.TextView
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
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.components.AvatarFragment
import com.tinysol.designsystem.views.sample.components.BadgeFragment
import com.tinysol.designsystem.views.sample.components.CardFragment
import com.tinysol.designsystem.views.sample.components.ListItemFragment
import com.tinysol.designsystem.views.sample.components.TextFragment
import com.tinysol.designsystem.core.style.TSTextColor
import com.tinysol.designsystem.core.style.TSTypeRole
import com.tinysol.designsystem.views.components.badge.TSBadgeView
import com.tinysol.designsystem.views.components.listitem.TSListItemView
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

    private fun ActivityScenario<MainActivity>.viewOf(tag: String): View {
        onView(withTag(tag)).perform(scrollIntoView())
        var result: View? = null
        onActivity { activity -> result = activity.window.decorView.findViewWithTag<View>(tag) }
        return requireNotNull(result)
    }

    private fun tapHeaderOf(tag: String) {
        val coordinates = CoordinatesProvider { view ->
            val location = IntArray(2).also(view::getLocationOnScreen)
            floatArrayOf(location[0] + view.width * 0.3f, location[1] + 24 * view.resources.displayMetrics.density)
        }
        onView(withTag(tag)).perform(scrollIntoView(), GeneralClickAction(Tap.SINGLE, coordinates, Press.FINGER, 0, 0))
    }

    private fun ActivityScenario<MainActivity>.bodyIsShown(tag: String): Boolean {
        var shown = false
        onActivity { activity ->
            shown = activity.window.decorView.findViewWithTag<View>(tag)?.findViewWithTag<View>(ListItemFragment.DISCLOSURE_BODY)?.isShown == true
        }
        return shown
    }

    @Test
    fun listItemDisclosureRowExpandsItsBody() {
        val kotlin = ListItemFragment.disclosure("kotlin")
        val snapshot = ListItemFragment.disclosure("snapshot")
        launchDemo().use { scenario ->
            openPage(DemoPageId.ListItem)
            scenario.viewOf(kotlin)
            assertFalse(scenario.bodyIsShown(kotlin))

            tapHeaderOf(kotlin)

            assertTrue(scenario.bodyIsShown(kotlin))

            tapHeaderOf(snapshot)

            assertTrue(scenario.bodyIsShown(snapshot))
            assertFalse(scenario.bodyIsShown(kotlin))

            tapHeaderOf(snapshot)

            assertFalse(scenario.bodyIsShown(snapshot))
        }
    }

    @Test
    fun textPageShowsEveryTypeRoleAndTextColor() {
        val pangram = resources.getString(DemoR.string.demo_sample_pangram)
        launchDemo().use { scenario ->
            openPage(DemoPageId.Text)
            TSTypeRole.entries.forEach { role ->
                val view = scenario.viewOf(TextFragment.role(role))
                assertEquals(pangram, (view as TextView).text.toString())
            }
            TSTextColor.entries.forEach { color ->
                val view = scenario.viewOf(TextFragment.color(color))
                assertEquals(color.name.lowercase(), (view as TextView).text.toString())
            }
            val truncated = scenario.viewOf(TextFragment.TRUNCATED)
            assertEquals(1, (truncated as TextView).lineCount)
        }
    }

    @Test
    fun badgeIsStaticText() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Badge)
            val plain = scenario.viewOf(BadgeFragment.PLAYGROUND_BADGE) as TSBadgeView
            val plainWidth = plain.width
            assertEquals(resources.getString(DemoR.string.demo_sample_draft), plain.text.toString())
            assertFalse(plain.isClickable)

            tap(BadgeFragment.PLAYGROUND_DOT)
            tap(BadgeFragment.PLAYGROUND_ICON)

            val decorated = scenario.viewOf(BadgeFragment.PLAYGROUND_BADGE) as TSBadgeView
            assertEquals(resources.getString(DemoR.string.demo_sample_draft), decorated.text.toString())
            assertFalse(decorated.isClickable)
            assertTrue(decorated.width > plainWidth)
        }
    }

    @Test
    fun avatarIsStaticAndSurvivesAPhoto() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Avatar)
            assertFalse(scenario.viewOf(AvatarFragment.PLAYGROUND_AVATAR).isClickable)

            tap(AvatarFragment.PLAYGROUND_PHOTO)

            assertFalse(scenario.viewOf(AvatarFragment.PLAYGROUND_AVATAR).isClickable)
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
            assertTrue(scenario.viewOf(ListItemFragment.STATE_DEFAULT).isEnabled)

            tap(ListItemFragment.STATE_DEFAULT)
            assertCount(ListItemFragment.STATE_COUNT, 1)

            assertFalse(scenario.viewOf(ListItemFragment.STATE_DISABLED).isEnabled)
            tapTitleOf(ListItemFragment.STATE_DISABLED)
            assertFalse(scenario.viewOf(ListItemFragment.STATE_STATIC).isClickable)
            tapTitleOf(ListItemFragment.STATE_STATIC)

            assertCount(ListItemFragment.STATE_COUNT, 1)
        }
    }

    @Test
    fun listItemSwitchRowTogglesFromTitleAndSwitch() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.ListItem)
            assertTrue((scenario.viewOf(ListItemFragment.WIFI) as TSListItemView).isChecked)

            tapTitleOf(ListItemFragment.WIFI)
            assertTrue((scenario.viewOf(ListItemFragment.WIFI) as TSListItemView).isChecked)

            tapSwitchOf(ListItemFragment.WIFI)
            assertFalse((scenario.viewOf(ListItemFragment.WIFI) as TSListItemView).isChecked)
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
            assertFalse((scenario.viewOf(ListItemFragment.PLAYGROUND_ROW) as TSListItemView).isChecked)
            tapSwitchOf(ListItemFragment.PLAYGROUND_ROW)
            assertTrue((scenario.viewOf(ListItemFragment.PLAYGROUND_ROW) as TSListItemView).isChecked)
            assertText(ListItemFragment.PLAYGROUND_VALUE, DemoR.string.demo_state_on)

            tap(ListItemFragment.PLAYGROUND_DISABLED)
            tapSwitchOf(ListItemFragment.PLAYGROUND_ROW)
            val disabled = scenario.viewOf(ListItemFragment.PLAYGROUND_ROW) as TSListItemView
            assertFalse(disabled.isEnabled)
            assertTrue(disabled.isChecked)
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
    fun interactiveCardPresses() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Card)
            assertTrue(scenario.viewOf(CardFragment.STATE_DEFAULT).isEnabled)

            tapAtPoint(CardFragment.STATE_DEFAULT, { it.width * 0.3f }, { 40 * it.resources.displayMetrics.density })
            assertCount(CardFragment.STATE_COUNT, 1)

            assertFalse(scenario.viewOf(CardFragment.STATE_DISABLED).isEnabled)
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
