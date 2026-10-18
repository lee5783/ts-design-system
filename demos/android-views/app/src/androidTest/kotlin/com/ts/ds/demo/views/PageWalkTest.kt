package com.ts.ds.demo.views

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.appbar.MaterialToolbar
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoCatalog
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.catalog.DemoPageKind
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.views.shell.DemoTags
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageWalkTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @After
    fun resetSettings() {
        instrumentation.runOnMainSync { settingsStore.reset() }
    }

    private fun ActivityScenario<MainActivity>.assertEdgeToEdge(page: DemoPageId) {
        onActivity { activity ->
            val decor = activity.window.decorView
            val bars = requireNotNull(ViewCompat.getRootWindowInsets(decor)).getInsets(WindowInsetsCompat.Type.systemBars())
            val screen = decor.findViewWithTag<View>(DemoTags.screen(page))
            val location = IntArray(2).also(screen::getLocationOnScreen)
            assertEquals("${page.id} draws under the status bar", 0, location[1])
            assertEquals("${page.id} draws under the navigation bar", decor.height, location[1] + screen.height)
            val toolbar = screen.findViewById<MaterialToolbar>(R.id.demo_toolbar)
            assertTrue("${page.id} title clears the status bar", toolbar.paddingTop >= bars.top)

            val scroll = screen.findViewById<NestedScrollView>(R.id.page_scroll)
            val rows = scroll.getChildAt(0) as ViewGroup
            scroll.scrollTo(0, rows.height)
            val last = rows.getChildAt(rows.childCount - 1)
            val lastLocation = IntArray(2).also(last::getLocationOnScreen)
            assertTrue("${page.id} scrolls its end above the navigation bar", lastLocation[1] + last.height <= decor.height - bars.bottom)
        }
    }

    private fun walk(settings: DemoSettings) {
        launchDemo(settings).use { scenario ->
            DemoCatalog.allPages.forEach { page ->
                openPage(page).check(matches(isDisplayed()))
                onView(withTag(DemoTags.ACTION_THEME)).check(matches(isDisplayed()))
                if (page.kind == DemoPageKind.Component) {
                    onView(allOf(withText(DemoR.string.demo_component_playground), isDescendantOfA(withTag(DemoTags.screen(page)))))
                        .perform(scrollIntoView())
                        .check(matches(isDisplayed()))
                }
                scenario.assertEdgeToEdge(page)
                Espresso.pressBack()
                onView(withTag(DemoTags.CATALOG)).check(matches(isDisplayed()))
            }
        }
    }

    @Test
    fun everyPageIsEdgeToEdgeWithThemeActionAndPlayground() {
        walk(DemoSettings(reduceMotion = true))
    }

    @Test
    fun everyPageWalksInRtl() {
        walk(DemoSettings(rtl = true, reduceMotion = true))
    }
}
