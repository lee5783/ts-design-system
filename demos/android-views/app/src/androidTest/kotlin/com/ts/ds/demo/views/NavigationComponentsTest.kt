package com.ts.ds.demo.views

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.components.BottomNavFragment
import com.ts.ds.demo.views.components.BottomNavShellFragment
import com.ts.ds.demo.views.components.TabsFragment
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationComponentsTest {
    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun tap(tag: String) {
        onView(withTag(tag)).perform(scrollIntoView(), click())
    }

    private fun tab(list: String, label: String) = onView(allOf(withText(label), isDescendantOfA(withTag(list))))

    private fun <T> ActivityScenario<MainActivity>.readTag(tag: String, block: (View) -> T): T {
        var result: Result<T>? = null
        onActivity { result = runCatching { block(it.window.decorView.findViewWithTag(tag)) } }
        return requireNotNull(result).getOrThrow()
    }

    private fun View.findText(label: String): TextView? = when {
        this is TextView && text.toString() == label -> this
        this is ViewGroup -> (0 until childCount).firstNotNullOfOrNull { getChildAt(it).findText(label) }
        else -> null
    }

    private fun ActivityScenario<MainActivity>.tabSelected(list: String, label: String): Boolean =
        readTag(list) { requireNotNull(it.findText(label)).isSelected }

    private fun ActivityScenario<MainActivity>.text(tag: String): String = readTag(tag) { (it as TextView).text.toString() }

    private fun clickNearTop() = GeneralClickAction(
        Tap.SINGLE,
        CoordinatesProvider { view ->
            val location = IntArray(2).also(view::getLocationOnScreen)
            floatArrayOf(location[0] + view.width / 2f, location[1] + 2f * view.resources.displayMetrics.density)
        },
        Press.FINGER,
        0,
        0,
    )

    @Test
    fun tabsSelectionUpdatesTheValueAndDisabledBlocks() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Tabs)
            onView(withTag(TabsFragment.PLAYGROUND_COUNT)).perform(scrollIntoView())
            assertFalse(scenario.tabSelected(TabsFragment.PLAYGROUND_TABS, "Week"))

            tab(TabsFragment.PLAYGROUND_TABS, "Week").perform(clickNearTop())
            assertTrue(scenario.tabSelected(TabsFragment.PLAYGROUND_TABS, "Week"))
            assertFalse(scenario.tabSelected(TabsFragment.PLAYGROUND_TABS, "Day"))
            assertEquals("Selected: Week", scenario.text(TabsFragment.PLAYGROUND_VALUE))
            assertEquals("Tapped 1 time", scenario.text(TabsFragment.PLAYGROUND_COUNT))

            tab(TabsFragment.PLAYGROUND_TABS, "Week").perform(click())
            assertEquals("Tapped 1 time", scenario.text(TabsFragment.PLAYGROUND_COUNT))

            tap(TabsFragment.PLAYGROUND_DISABLED)
            tab(TabsFragment.PLAYGROUND_TABS, "Month").perform(scrollIntoView(), click())
            assertFalse(scenario.tabSelected(TabsFragment.PLAYGROUND_TABS, "Month"))
            assertEquals("Tapped 1 time", scenario.text(TabsFragment.PLAYGROUND_COUNT))
        }
    }

    @Test
    fun underlineTabsScrollWhenTheyOverflow() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Tabs)
            onView(withTag(TabsFragment.OVERFLOW_VALUE)).perform(scrollIntoView())
            val reportsVisible = {
                scenario.readTag(TabsFragment.OVERFLOW) { list ->
                    val reports = requireNotNull(list.findText("Reports"))
                    val bounds = Rect()
                    reports.getGlobalVisibleRect(bounds) && bounds.width() == reports.width
                }
            }
            assertFalse(reportsVisible())

            repeat(3) { onView(withTag(TabsFragment.OVERFLOW)).perform(swipeLeft()) }

            assertTrue(reportsVisible())
            tab(TabsFragment.OVERFLOW, "Reports").perform(click())
            assertTrue(scenario.tabSelected(TabsFragment.OVERFLOW, "Reports"))
            assertEquals("Selected: Reports", scenario.text(TabsFragment.OVERFLOW_VALUE))
        }
    }

    private fun View.findItem(label: String): View? {
        var view: View? = findText(label)
        while (view != null && !view.isClickable) view = view.parent as? View
        return view
    }

    private fun navItem(nav: String, label: String) = onView(allOf(withText(label), isDescendantOfA(withTag(nav))))

    private fun ActivityScenario<MainActivity>.navShows(nav: String, label: String, text: String): Boolean =
        readTag(nav) { requireNotNull(it.findItem(label)).findText(text) != null }

    private fun ActivityScenario<MainActivity>.navSelected(nav: String, label: String): Boolean =
        readTag(nav) { requireNotNull(it.findItem(label)).isSelected }

    @Test
    fun bottomNavSelectionBadgeAndDisabled() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.BottomNav)
            onView(withTag(BottomNavFragment.PLAYGROUND_CHANGES)).perform(scrollIntoView())
            val nav = BottomNavFragment.PLAYGROUND_NAV
            assertFalse(scenario.navSelected(nav, "Search"))

            navItem(nav, "Search").perform(click())
            assertTrue(scenario.navSelected(nav, "Search"))
            assertFalse(scenario.navSelected(nav, "Home"))
            assertEquals("Selected: Search", scenario.text(BottomNavFragment.PLAYGROUND_VALUE))
            assertEquals("Tapped 1 time", scenario.text(BottomNavFragment.PLAYGROUND_CHANGES))

            navItem(nav, "Search").perform(click())
            assertEquals("Tapped 1 time", scenario.text(BottomNavFragment.PLAYGROUND_CHANGES))

            assertTrue(scenario.navShows(nav, "Inbox", "3"))
            tap(BottomNavFragment.PLAYGROUND_ADD_HUNDRED)
            assertTrue(scenario.navShows(nav, "Inbox", "99+"))
            tap(BottomNavFragment.PLAYGROUND_CLEAR)
            assertFalse(scenario.navShows(nav, "Inbox", "99+"))

            tap(BottomNavFragment.PLAYGROUND_DISABLED)
            navItem(nav, "Alerts").perform(scrollIntoView(), click())
            assertFalse(scenario.navSelected(nav, "Alerts"))
            assertEquals("Tapped 1 time", scenario.text(BottomNavFragment.PLAYGROUND_CHANGES))
        }
    }

    @Test
    fun bottomNavShellSwitchesTabsAndShowsAToastAboveTheBar() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.BottomNav)
            tap(BottomNavFragment.OPEN_SHELL)
            onView(withTag(BottomNavShellFragment.SHELL)).check(matches(isDisplayed()))
            onView(withTag("demo.bottom-nav.shell.title.home")).check(matches(isDisplayed()))
            val nav = "demo.bottom-nav.shell.nav"

            navItem(nav, "Inbox").perform(click())
            assertTrue(scenario.navSelected(nav, "Inbox"))
            onView(withTag("demo.bottom-nav.shell.title.inbox")).check(matches(isDisplayed()))
            onView(withTag("demo.bottom-nav.shell.add-one.inbox")).perform(click())
            assertTrue(scenario.navShows(nav, "Inbox", "4"))

            onView(withTag("demo.bottom-nav.shell.toast.inbox")).perform(click())
            onView(withText("Message sent")).check(matches(isDisplayed()))
            val (toastBottom, navTop) = scenario.readTag(nav) { bar ->
                val toast = requireNotNull(bar.rootView.findText("Message sent"))
                val toastLocation = IntArray(2).also(toast::getLocationOnScreen)
                val barLocation = IntArray(2).also(bar::getLocationOnScreen)
                (toastLocation[1] + toast.height) to barLocation[1]
            }
            assertTrue("toast $toastBottom should sit above the bar $navTop", toastBottom <= navTop)
        }
    }
}
