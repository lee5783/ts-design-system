package com.ts.ds.demo.views

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.views.components.AlertFragment
import com.ts.ds.demo.views.components.ToastFragment
import com.ts.ds.views.components.toast.TSToastHost
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedbackComponentsTest {
    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    private val offline = SampleData.message("offline")

    private val playgroundLabel: String
        get() = listOf(
            resources.getString(DemoR.string.demo_sample_information),
            resources.getString(offline.titleRes),
            resources.getString(offline.bodyRes),
        ).joinToString(", ")

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun tap(tag: String) {
        onView(withTag(tag)).perform(scrollIntoView(), click())
    }

    private fun assertCount(tag: String, count: Int) {
        onView(withTag(tag)).perform(scrollIntoView()).check(matches(withText(resources.getQuantityString(DemoR.plurals.demo_component_tap_count, count, count))))
    }

    private fun View.findByDescription(description: String): View? {
        if (contentDescription?.toString() == description) return this
        if (this is ViewGroup) {
            (0 until childCount).forEach { index -> getChildAt(index).findByDescription(description)?.let { return it } }
        }
        return null
    }

    private fun ActivityScenario<MainActivity>.alertState(): Triple<Int, AccessibilityNodeInfoCompat?, Int?> {
        var result: Triple<Int, AccessibilityNodeInfoCompat?, Int?>? = null
        onActivity { activity ->
            val alert = activity.window.decorView.findViewWithTag<View>(AlertFragment.PLAYGROUND_ALERT)
            val message = alert.findByDescription(playgroundLabel)
            val info = message?.let { view -> AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(view, it) } }
            result = Triple(alert.visibility, info, message?.let(ViewCompat::getAccessibilityLiveRegion))
        }
        return requireNotNull(result)
    }

    @Test
    fun alertMessageIsOneElementWithSeparateAction() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Alert)
            tap(AlertFragment.PLAYGROUND_ACTION)
            onView(withTag(AlertFragment.PLAYGROUND_ALERT)).perform(scrollIntoView())

            val (_, info, liveRegion) = scenario.alertState()
            assertNotNull(info)
            assertEquals(TextView::class.java.name, info?.className)
            assertEquals(ViewCompat.ACCESSIBILITY_LIVE_REGION_NONE, liveRegion)

            tap(AlertFragment.PLAYGROUND_RETRY)
            assertCount(AlertFragment.PLAYGROUND_COUNT, 1)
        }
    }

    @Test
    fun alertDismissRemovesItAndShowAgainBringsItBack() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Alert)
            onView(withTag(AlertFragment.PLAYGROUND_ALERT)).perform(scrollIntoView())
            onView(allOf(withContentDescription(DISMISS), isDisplayed())).perform(click())
            assertEquals(View.GONE, scenario.alertState().first)

            tap(AlertFragment.PLAYGROUND_SHOW)
            val (visibility, _, liveRegion) = scenario.alertState()
            assertEquals(View.VISIBLE, visibility)
            assertEquals(ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE, liveRegion)

            tap(AlertFragment.PLAYGROUND_DISMISSIBLE)
            var dismissVisible: Boolean? = null
            scenario.onActivity { activity ->
                val alert = activity.window.decorView.findViewWithTag<View>(AlertFragment.PLAYGROUND_ALERT)
                dismissVisible = alert.findByDescription(DISMISS)?.isShown
            }
            assertEquals(false, dismissVisible ?: false)
        }
    }


    private fun successLabel(message: Int): String =
        "${resources.getString(DemoR.string.demo_sample_success)}, ${resources.getString(message)}"

    private fun ActivityScenario<MainActivity>.currentToast(): String? {
        var text: String? = null
        onActivity { text = TSToastHost.from(it)?.current?.message?.text }
        return text
    }

    private fun ActivityScenario<MainActivity>.awaitToast(expected: String?) {
        val deadline = SystemClock.uptimeMillis() + TOAST_WAIT_MILLIS
        while (currentToast() != expected && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(POLL_MILLIS)
        }
        assertEquals(expected, currentToast())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        SystemClock.sleep(SETTLE_MILLIS)
    }

    @Test
    fun queuedToastsShowOneAtATimeInOrder() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Toast)
            tap(ToastFragment.QUEUE_SHOW)

            listOf(
                DemoR.string.demo_sample_photo_uploaded,
                DemoR.string.demo_sample_album_shared,
                DemoR.string.demo_sample_backup_complete,
            ).forEach { message ->
                scenario.awaitToast(resources.getString(message))
                onView(allOf(withContentDescription(successLabel(message)), isDisplayed())).check(matches(isDisplayed()))
            }
            scenario.awaitToast(null)
            onView(withContentDescription(successLabel(DemoR.string.demo_sample_backup_complete))).check(doesNotExist())
        }
    }

    @Test
    fun toastUndoActionFiresAndDismisses() {
        val deleted = resources.getString(SampleData.message("deleted").titleRes)
        launchDemo().use { scenario ->
            openPage(DemoPageId.Toast)
            tap(ToastFragment.PLAYGROUND_SHOW)
            scenario.awaitToast(deleted)
            onView(withContentDescription(deleted)).check(matches(isDisplayed()))

            onView(allOf(withText(resources.getString(DemoR.string.demo_sample_undo)), isDisplayed(), hasSibling(withContentDescription(deleted))))
                .perform(click())
            scenario.awaitToast(null)
            assertCount(ToastFragment.PLAYGROUND_COUNT, 1)
        }
    }

    @Test
    fun toastIsShownInsideAnOpenSheetAndPassesTouchesThrough() {
        val title = resources.getString(DemoR.string.demo_sample_toast_sheet_title)
        val label = successLabel(DemoR.string.demo_sample_link_copied)
        launchDemo().use { scenario ->
            openPage(DemoPageId.Toast)
            tap(ToastFragment.SHEET_OPEN)
            onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))

            onView(withText(resources.getString(DemoR.string.demo_sample_copy_link))).inRoot(isDialog()).perform(click())
            scenario.awaitToast(resources.getString(DemoR.string.demo_sample_link_copied))
            onView(withContentDescription(label)).inRoot(isDialog()).check(matches(isDisplayed()))

            onView(isRoot()).inRoot(isDialog()).perform(
                GeneralClickAction(
                    Tap.SINGLE,
                    { view ->
                        val location = IntArray(2).also(view::getLocationOnScreen)
                        floatArrayOf(location[0] + view.width / 2f, location[1] + SCRIM_TAP_Y)
                    },
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY,
                ),
            )
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            SystemClock.sleep(SETTLE_MILLIS)
            onView(withText(title)).check(doesNotExist())
        }
    }

    private companion object {
        const val DISMISS = "Dismiss"
        const val TOAST_WAIT_MILLIS = 8_000L
        const val POLL_MILLIS = 100L
        const val SETTLE_MILLIS = 400L
        const val SCRIM_TAP_Y = 200f
    }
}
