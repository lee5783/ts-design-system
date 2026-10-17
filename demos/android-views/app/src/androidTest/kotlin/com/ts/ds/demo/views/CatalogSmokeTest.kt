package com.ts.ds.demo.views

import android.content.Intent
import android.content.res.Configuration
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.support.catalog.DemoCatalog
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.settings.DemoLaunchOptions
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoTheme
import com.ts.ds.demo.views.shell.DemoTags
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import com.ts.ds.demo.support.R as DemoR

@RunWith(AndroidJUnit4::class)
class CatalogSmokeTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    @Test
    fun launchListsEveryCatalogPage() {
        launchDemo().use {
            onView(withTag(DemoTags.CATALOG)).check(matches(isDisplayed()))
            DemoCatalog.allPages.forEach { page ->
                onView(withId(R.id.catalog_list))
                    .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(withTag(page.testTag)))
                onView(withTag(page.testTag)).check(matches(isDisplayed()))
            }
        }
    }

    @Test
    fun everyFoundationPageOpensAndReturnsToTheCatalog() {
        launchDemo().use {
            DemoCatalog.foundationPages.forEach { page ->
                openPage(page).check(matches(isDisplayed()))
                onView(withId(R.id.demo_toolbar)).check(matches(hasDescendant(withText(page.titleRes))))
                Espresso.pressBack()
                onView(withTag(DemoTags.CATALOG)).check(matches(isDisplayed()))
            }
        }
    }

    @Test
    fun everyComponentPageOpensFromTheCatalog() {
        launchDemo().use {
            DemoCatalog.componentPages.forEach { page ->
                openPage(page).check(matches(isDisplayed()))
                onView(withContentDescription(DemoR.string.demo_action_back)).perform(click())
                onView(withTag(DemoTags.CATALOG)).check(matches(isDisplayed()))
            }
        }
    }

    @Test
    fun themeToggleSwitchesBetweenDarkAndLight() {
        launchDemo().use {
            onView(withId(R.id.action_settings)).perform(click())
            onView(withTag(DemoTags.SETTINGS)).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withTag(DemoTags.theme(DemoTheme.System))).inRoot(isDialog()).check(matches(isChecked()))

            onView(withTag(DemoTags.theme(DemoTheme.Dark))).inRoot(isDialog()).perform(click())
            onView(withTag(DemoTags.theme(DemoTheme.Dark))).inRoot(isDialog()).check(matches(isChecked()))
            onView(withTag(DemoTags.theme(DemoTheme.System))).inRoot(isDialog()).check(matches(isNotChecked()))
            assertEquals(DemoTheme.Dark, settingsStore.settings.theme)

            onView(withTag(DemoTags.theme(DemoTheme.Light))).inRoot(isDialog()).perform(click())
            onView(withTag(DemoTags.theme(DemoTheme.Light))).inRoot(isDialog()).check(matches(isChecked()))
            assertEquals(DemoTheme.Light, settingsStore.settings.theme)

            onView(withId(R.id.settings_close)).inRoot(isDialog()).perform(click())
            onView(withTag(DemoTags.SETTINGS)).check(doesNotExist())
        }
    }

    @Test
    fun settingsApplyFontScaleRtlAndReduceMotion() {
        launchDemo(DemoSettings()).use { scenario ->
            onView(withId(R.id.action_settings)).perform(click())
            onView(withTag(DemoTags.fontScale(2f))).inRoot(isDialog()).perform(click())
            onView(withTag(DemoTags.fontScale(2f))).inRoot(isDialog()).check(matches(isChecked()))
            scenario.onActivity { activity -> assertEquals(2f, activity.resources.configuration.fontScale / systemFontScale(), 0.01f) }

            onView(withId(R.id.settings_rtl)).inRoot(isDialog()).perform(scrollIntoView()).check(matches(isNotChecked())).perform(click())
            onView(withId(R.id.settings_rtl)).inRoot(isDialog()).check(matches(isChecked()))
            scenario.onActivity { activity -> assertEquals(View.LAYOUT_DIRECTION_RTL, activity.window.decorView.layoutDirection) }

            onView(withId(R.id.settings_reduce_motion)).inRoot(isDialog()).perform(scrollIntoView(), click())
            onView(withId(R.id.settings_reduce_motion)).inRoot(isDialog()).check(matches(isChecked()))
            assertEquals(DemoSettings(fontScale = 2f, rtl = true, reduceMotion = true), settingsStore.settings)

            onView(withId(R.id.settings_reset)).inRoot(isDialog()).perform(scrollIntoView(), click())
            assertEquals(DemoSettings(), settingsStore.settings)
        }
    }

    @Test
    fun backClosesTheSettingsSheetBeforeLeavingThePage() {
        launchDemo().use {
            val page = DemoCatalog.foundationPages.first()
            openPage(page)
            onView(withId(R.id.action_settings)).perform(click())
            onView(withTag(DemoTags.SETTINGS)).inRoot(isDialog()).check(matches(isDisplayed()))
            Espresso.pressBack()
            onView(withTag(DemoTags.SETTINGS)).check(doesNotExist())
            onView(withTag(DemoTags.screen(page))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun launchOptionsOpenAPageWithTheGivenSettings() {
        launchDemo().close()
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(DemoLaunchOptions.PAGE, DemoPageId.Typography.id)
            .putExtra(DemoLaunchOptions.THEME, DemoTheme.Dark.id)
            .putExtra(DemoLaunchOptions.FONT_SCALE, "2")
            .putExtra(DemoLaunchOptions.RIGHT_TO_LEFT, "YES")
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            onView(withTag(DemoTags.screen(DemoPageId.Typography))).check(matches(isDisplayed()))
            assertEquals(DemoSettings(theme = DemoTheme.Dark, fontScale = 2f, rtl = true, reduceMotion = true), settingsStore.settings)
            scenario.onActivity { activity -> assertEquals(true, activity.isNightMode) }
        }
    }

    @Test
    fun aboutShowsTheLibraryAndDesignVersions() {
        launchDemo().use {
            onView(withId(R.id.action_about)).perform(click())
            onView(withTag(DemoTags.ABOUT)).check(matches(isDisplayed()))
            onView(withText(BuildConfig.DS_DESIGN_VERSION)).check(matches(isDisplayed()))
            onView(withText(context.getString(R.string.toolkit_name))).check(matches(isDisplayed()))
            onView(withTag(DemoTags.ABOUT)).check(matches(hasDescendant(withText(BuildConfig.DS_LIBRARY_VERSION))))
        }
    }

    private fun systemFontScale(): Float = context.applicationContext.resources.configuration.fontScale

    private val MainActivity.isNightMode: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}
