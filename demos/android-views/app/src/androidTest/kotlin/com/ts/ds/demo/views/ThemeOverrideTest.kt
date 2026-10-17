package com.ts.ds.demo.views

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.drawToBitmap
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.materialswitch.MaterialSwitch
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.support.settings.DemoLaunchOverrides
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoTheme
import com.ts.ds.demo.views.components.DialogPageFragment
import com.ts.ds.demo.views.components.ToastFragment
import com.ts.ds.demo.views.shell.DemoTags
import com.ts.ds.demo.views.shell.isNightConfiguration
import com.ts.ds.demo.views.shell.labelRes
import com.ts.ds.views.components.toast.TSToastHost
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeOverrideTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val resources = context.resources

    @After
    fun restoreDevice() {
        setDeviceDark(false)
        instrumentation.runOnMainSync { settingsStore.reset() }
    }

    private fun shell(command: String) {
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    }

    private fun setDeviceDark(dark: Boolean) {
        shell("cmd uimode night ${if (dark) "yes" else "no"}")
        instrumentation.waitForIdleSync()
    }

    private fun launchWithTheme(theme: DemoTheme, settings: DemoSettings = DemoSettings(reduceMotion = true)): ActivityScenario<MainActivity> {
        instrumentation.runOnMainSync { settingsStore.update { settings } }
        val intent = Intent(context, MainActivity::class.java).putExtra(DemoLaunchOverrides.THEME_EXTRA, theme.id)
        return ActivityScenario.launch(intent)
    }

    private fun ActivityScenario<MainActivity>.activityId(): Int {
        var id = 0
        onActivity { id = System.identityHashCode(it) }
        return id
    }

    private fun ActivityScenario<MainActivity>.awaitRecreation(action: () -> Unit) {
        val before = activityId()
        action()
        val deadline = SystemClock.uptimeMillis() + WAIT_MILLIS
        while (activityId() == before && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(POLL_MILLIS)
        assertNotEquals("the activity was recreated", before, activityId())
        instrumentation.waitForIdleSync()
        SystemClock.sleep(SETTLE_MILLIS)
        instrumentation.waitForIdleSync()
    }

    private fun ActivityScenario<MainActivity>.openLink(theme: DemoTheme) {
        val link = Intent(Intent.ACTION_VIEW, Uri.parse(DemoLaunchOverrides.settingsLink(DemoLaunchOverrides.VIEWS_SCHEME, theme)))
            .setPackage(context.packageName)
        val handler = requireNotNull(context.packageManager.resolveActivity(link, 0)).activityInfo
        assertEquals(MainActivity::class.java.name, handler.name)
        awaitRecreation { onActivity { activity -> instrumentation.callActivityOnNewIntent(activity, link) } }
        assertEquals(theme, settingsStore.settings.theme)
    }

    private fun View.isDarkAt(x: Int, y: Int): Boolean = Color.luminance(drawToBitmap().getPixel(x, y)) < DARK_LUMINANCE

    private fun ActivityScenario<MainActivity>.screenIsDark(): Boolean {
        var dark = false
        onActivity { activity ->
            val decor = activity.window.decorView
            dark = decor.isDarkAt(EDGE_SAMPLE_PX, decor.height / 2)
            assertEquals(dark, activity.isNightConfiguration())
        }
        return dark
    }

    private fun ActivityScenario<MainActivity>.lightStatusBars(): Boolean {
        var light = false
        onActivity { light = WindowCompat.getInsetsController(it.window, it.window.decorView).isAppearanceLightStatusBars }
        return light
    }

    private fun MainActivity.currentPage(): Fragment {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        return requireNotNull(navHost.childFragmentManager.primaryNavigationFragment)
    }

    private fun ActivityScenario<MainActivity>.dialogIsDark(tag: String, title: String): Boolean {
        var dark = false
        onActivity { activity ->
            val dialog = (activity.currentPage().childFragmentManager.findFragmentByTag(tag) as DialogFragment).requireDialog()
            val decor = requireNotNull(dialog.window).decorView
            val titleView = ArrayList<View>().also { decor.findViewsWithText(it, title, View.FIND_VIEWS_WITH_TEXT) }.first()
            val location = IntArray(2).also(titleView::getLocationInWindow)
            dark = decor.isDarkAt((location[0] - PANEL_SAMPLE_PX).coerceAtLeast(0), location[1] + titleView.height / 2)
        }
        return dark
    }

    private fun ActivityScenario<MainActivity>.pageScrollY(): Int {
        var scrollY = 0
        onActivity { scrollY = it.currentPage().requireView().findViewById<NestedScrollView>(R.id.page_scroll).scrollY }
        return scrollY
    }

    private fun ActivityScenario<MainActivity>.toggleChecked(tag: String): Boolean {
        var checked = false
        onActivity { checked = it.currentPage().requireView().findViewWithTag<MaterialSwitch>(tag).isChecked }
        return checked
    }

    private fun ActivityScenario<MainActivity>.toastHost(read: (TSToastHost) -> Unit) {
        onActivity { read(requireNotNull(TSToastHost.from(it))) }
    }

    private fun ActivityScenario<MainActivity>.awaitToast(expected: String?) {
        val deadline = SystemClock.uptimeMillis() + WAIT_MILLIS
        var current: String? = null
        do {
            toastHost { current = it.current?.message?.text }
            if (current == expected) break
            SystemClock.sleep(POLL_MILLIS)
        } while (SystemClock.uptimeMillis() < deadline)
        assertEquals(expected, current)
    }

    private fun themeLabel(theme: DemoTheme): String = resources.getString(DemoR.string.demo_action_theme, resources.getString(theme.labelRes))

    private fun assertThemeAction(theme: DemoTheme) {
        onView(withId(R.id.action_theme)).check(matches(withContentDescription(themeLabel(theme))))
        onView(withTag(DemoTags.ACTION_THEME)).check(matches(isDisplayed()))
    }

    private fun tap(tag: String) {
        onView(withTag(tag)).perform(scrollIntoView(), click())
    }

    @Test
    fun deviceLightAppLaunchedDarkRendersDark() {
        setDeviceDark(false)
        launchWithTheme(DemoTheme.Dark).use { scenario ->
            assertEquals(DemoTheme.Dark, settingsStore.settings.theme)
            assertTrue(scenario.screenIsDark())
            assertEquals(false, scenario.lightStatusBars())
            assertThemeAction(DemoTheme.Dark)

            openPage(DemoPageId.Button).check(matches(isDisplayed()))
            assertTrue(scenario.screenIsDark())
            assertThemeAction(DemoTheme.Dark)
        }
    }

    @Test
    fun deviceDarkAppLaunchedLightRendersLight() {
        setDeviceDark(true)
        launchWithTheme(DemoTheme.Light).use { scenario ->
            assertEquals(DemoTheme.Light, settingsStore.settings.theme)
            assertTrue(!scenario.screenIsDark())
            assertEquals(true, scenario.lightStatusBars())

            openPage(DemoPageId.Card).check(matches(isDisplayed()))
            assertTrue(!scenario.screenIsDark())
        }
    }

    @Test
    fun systemFollowsTheDeviceAgain() {
        setDeviceDark(true)
        launchWithTheme(DemoTheme.System).use { scenario ->
            assertTrue(scenario.screenIsDark())
            scenario.awaitRecreation { setDeviceDark(false) }
            assertTrue(!scenario.screenIsDark())
            assertEquals(true, scenario.lightStatusBars())
        }
    }

    @Test
    fun toggleCyclesOnEveryScreenAndPersistsAcrossRelaunch() {
        setDeviceDark(false)
        launchDemo().use { scenario ->
            assertThemeAction(DemoTheme.System)
            onView(withId(R.id.action_theme)).perform(click())
            assertThemeAction(DemoTheme.Light)
            assertEquals(DemoTheme.Light, settingsStore.settings.theme)

            openPage(DemoPageId.Toast)
            scenario.awaitRecreation { onView(withId(R.id.action_theme)).perform(click()) }
            onView(withTag(DemoTags.screen(DemoPageId.Toast))).check(matches(isDisplayed()))
            assertThemeAction(DemoTheme.Dark)
            assertTrue(scenario.screenIsDark())

            onView(withId(R.id.action_settings)).perform(click())
            onView(withTag(DemoTags.theme(DemoTheme.Dark))).inRoot(isDialog()).check(matches(isChecked()))
        }
        val persisted = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(PREFERENCES_KEY, null)
        assertEquals(DemoTheme.Dark, DemoSettings.decode(persisted).theme)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertThemeAction(DemoTheme.Dark)
            assertTrue(scenario.screenIsDark())
            scenario.awaitRecreation { onView(withId(R.id.action_theme)).perform(click()) }
            assertThemeAction(DemoTheme.System)
            assertEquals(DemoTheme.System, settingsStore.settings.theme)
        }
    }

    @Test
    fun openCenterDialogFollowsALiveFlipWithoutClosing() {
        setDeviceDark(false)
        val title = resources.getString(DemoR.string.demo_sample_archive_title)
        launchWithTheme(DemoTheme.Light, DemoSettings(fontScale = 2f, reduceMotion = true)).use { scenario ->
            openPage(DemoPageId.Dialog)
            tap(DialogPageFragment.PLAYGROUND_LONG)
            tap(DialogPageFragment.PLAYGROUND_OPEN)
            onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))
            assertTrue(!scenario.dialogIsDark(DialogPageFragment.PLAYGROUND_TAG, title))
            val scrollY = scenario.pageScrollY()
            assertTrue("the page is scrolled", scrollY > 0)

            scenario.openLink(DemoTheme.Dark)
            onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))
            assertTrue(scenario.dialogIsDark(DialogPageFragment.PLAYGROUND_TAG, title))
            assertEquals(false, scenario.lightStatusBars())
            assertEquals(scrollY, scenario.pageScrollY())
            assertTrue(scenario.toggleChecked(DialogPageFragment.PLAYGROUND_LONG))
        }
    }

    @Test
    fun openSheetAndToastFollowALiveFlipWithoutClosing() {
        setDeviceDark(false)
        val title = resources.getString(DemoR.string.demo_sample_toast_sheet_title)
        val linkCopied = resources.getString(DemoR.string.demo_sample_link_copied)
        val toast = "${resources.getString(DemoR.string.demo_sample_success)}, $linkCopied"
        launchWithTheme(DemoTheme.Dark).use { scenario ->
            openPage(DemoPageId.Toast)
            tap(ToastFragment.SHEET_OPEN)
            onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText(resources.getString(DemoR.string.demo_sample_copy_link))).inRoot(isDialog()).perform(click())
            scenario.awaitToast(linkCopied)
            assertTrue(scenario.dialogIsDark(ToastFragment.SHEET_TAG, title))

            scenario.openLink(DemoTheme.Light)
            onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))
            assertTrue(!scenario.dialogIsDark(ToastFragment.SHEET_TAG, title))
            scenario.awaitToast(linkCopied)
            onView(withContentDescription(toast)).inRoot(isDialog()).check(matches(isDisplayed()))
        }
    }

    @Test
    fun toastKeepsItsQueuePositionAcrossALiveFlip() {
        setDeviceDark(false)
        val first = resources.getString(DemoR.string.demo_sample_photo_uploaded)
        val second = resources.getString(DemoR.string.demo_sample_album_shared)
        launchWithTheme(DemoTheme.Light).use { scenario ->
            openPage(DemoPageId.Toast)
            tap(ToastFragment.QUEUE_SHOW)
            scenario.awaitToast(first)
            var firstId = 0L
            scenario.toastHost { firstId = requireNotNull(it.current).id }

            scenario.openLink(DemoTheme.Dark)
            scenario.toastHost { host ->
                assertEquals(firstId, host.current?.id)
                assertEquals(listOf(second, resources.getString(DemoR.string.demo_sample_backup_complete)), host.queue.pending.map { it.message.text })
            }
            onView(withContentDescription("${resources.getString(DemoR.string.demo_sample_success)}, $first")).check(matches(isDisplayed()))
            scenario.awaitToast(second)
        }
    }

    private companion object {
        const val WAIT_MILLIS = 8_000L
        const val POLL_MILLIS = 100L
        const val SETTLE_MILLIS = 500L
        const val DARK_LUMINANCE = 0.5f
        const val EDGE_SAMPLE_PX = 2
        const val PANEL_SAMPLE_PX = 8
        const val PREFERENCES = "demo_settings"
        const val PREFERENCES_KEY = "settings"
    }
}
