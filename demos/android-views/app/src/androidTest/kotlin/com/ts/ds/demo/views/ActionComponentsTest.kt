package com.ts.ds.demo.views

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.components.ButtonFragment
import com.ts.ds.demo.views.components.IconButtonFragment
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActionComponentsTest {
    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun tapCount(count: Int): String = resources.getQuantityString(DemoR.plurals.demo_component_tap_count, count, count)

    private fun tap(tag: String) {
        onView(withTag(tag)).perform(scrollIntoView(), click())
    }

    private fun assertCount(tag: String, count: Int) {
        onView(withTag(tag)).perform(scrollIntoView()).check(matches(withText(tapCount(count))))
    }

    @Test
    fun buttonPlaygroundFiresAndLoadingAndDisabledBlock() {
        launchDemo().use {
            openPage(DemoPageId.Button)
            tap(ButtonFragment.PLAYGROUND_BUTTON)
            assertCount(ButtonFragment.PLAYGROUND_COUNT, 1)

            tap(ButtonFragment.PLAYGROUND_LOADING)
            onView(withTag(ButtonFragment.PLAYGROUND_BUTTON)).check(matches(isNotEnabled()))
            tap(ButtonFragment.PLAYGROUND_BUTTON)
            assertCount(ButtonFragment.PLAYGROUND_COUNT, 1)
            tap(ButtonFragment.PLAYGROUND_LOADING)

            tap(ButtonFragment.PLAYGROUND_DISABLED)
            tap(ButtonFragment.PLAYGROUND_BUTTON)
            assertCount(ButtonFragment.PLAYGROUND_COUNT, 1)
            tap(ButtonFragment.PLAYGROUND_DISABLED)

            tap(ButtonFragment.PLAYGROUND_BUTTON)
            assertCount(ButtonFragment.PLAYGROUND_COUNT, 2)
        }
    }

    @Test
    fun linkButtonHasA48dpTouchTarget() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Button)
            onView(withId(R.id.button_link)).perform(scrollIntoView())
            scenario.onActivity { activity ->
                val link = activity.findViewById<View>(R.id.button_link)
                val minPx = 48 * activity.resources.displayMetrics.density
                assertTrue(link.height >= minPx - 1 && link.width >= minPx - 1)
            }
        }
    }

    @Test
    fun iconButtonPlaygroundFiresAndDisabledBlocks() {
        launchDemo().use {
            openPage(DemoPageId.IconButton)
            tap(IconButtonFragment.PLAYGROUND_BUTTON)
            assertCount(IconButtonFragment.PLAYGROUND_COUNT, 1)

            tap(IconButtonFragment.PLAYGROUND_DISABLED)
            onView(withTag(IconButtonFragment.PLAYGROUND_BUTTON)).check(matches(isNotEnabled()))
            tap(IconButtonFragment.PLAYGROUND_BUTTON)
            assertCount(IconButtonFragment.PLAYGROUND_COUNT, 1)
        }
    }
}
