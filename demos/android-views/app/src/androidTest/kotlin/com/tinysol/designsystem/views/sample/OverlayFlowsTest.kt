package com.tinysol.designsystem.views.sample

import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.view.InputDevice
import android.view.View
import android.view.MotionEvent
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.sample.support.sample.SampleData
import com.tinysol.designsystem.views.sample.components.BottomNavFragment
import com.tinysol.designsystem.views.sample.components.BottomNavShellFragment
import com.tinysol.designsystem.views.sample.components.DialogPageFragment
import com.tinysol.designsystem.views.sample.shell.DemoTags
import com.tinysol.designsystem.views.components.textfield.TSTextFieldView
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OverlayFlowsTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val resources = instrumentation.targetContext.resources
    private val confirmTitle = resources.getString(SampleData.message("confirm-delete").titleRes)

    @After
    fun resetSettings() {
        instrumentation.runOnMainSync { settingsStore.reset() }
    }

    private fun tap(tag: String) {
        onView(withTag(tag)).perform(scrollIntoView(), click())
    }

    private fun assertDialogShown(title: String) {
        onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    private fun assertDialogGone(title: String) {
        instrumentation.waitForIdleSync()
        onView(withText(title)).check(doesNotExist())
    }

    private fun assertResult(value: String) {
        onView(withTag(DialogPageFragment.RESULT))
            .perform(scrollIntoView())
            .check(matches(withText(resources.getString(DemoR.string.demo_component_dialog_result, value))))
    }

    private fun ActivityScenario<MainActivity>.refusedCount(): Int {
        var count = 0
        onActivity { activity ->
            val caption = activity.window.decorView.findViewWithTag<TextView>(DialogPageFragment.REFUSED)
            count = caption.text.filter(Char::isDigit).toString().toInt()
        }
        return count
    }

    private fun tapScrim() {
        onView(isRoot()).inRoot(isDialog()).perform(
            GeneralClickAction(
                Tap.SINGLE,
                { view ->
                    val location = IntArray(2).also(view::getLocationOnScreen)
                    floatArrayOf(location[0] + SCRIM_INSET, location[1] + view.height * SCRIM_HEIGHT_FRACTION)
                },
                Press.FINGER,
                InputDevice.SOURCE_UNKNOWN,
                MotionEvent.BUTTON_PRIMARY,
            ),
        )
    }

    private fun edgeSwipe() {
        Thread.sleep(SETTLE_MILLIS)
        val y = resources.displayMetrics.heightPixels / 2
        val script = buildString {
            appendLine("input motionevent DOWN 2 $y")
            listOf(40, 80, 140, 200, 260, 320, 380, 440, 500).forEach { appendLine("input motionevent MOVE $it $y") }
            appendLine("input motionevent UP 500 $y")
        }
        val file = File(requireNotNull(instrumentation.targetContext.getExternalFilesDir(null)), "edge-swipe.sh")
        file.writeText(script)
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("sh ${file.absolutePath}")).use { it.readBytes() }
        Thread.sleep(SETTLE_MILLIS)
    }

    private val gestureNavigation: Boolean
        get() = Settings.Secure.getInt(instrumentation.targetContext.contentResolver, "navigation_mode", 0) == GESTURAL_NAVIGATION

    @Test
    fun centerDialogOpensAndScrimDismisses() {
        launchDemo().use {
            openPage(DemoPageId.Dialog)
            tap(DialogPageFragment.OPEN_CENTER)
            assertDialogShown(confirmTitle)
            tapScrim()
            assertDialogGone(confirmTitle)
            assertResult(resources.getString(DemoR.string.demo_component_dismissed))
        }
    }

    @Test
    fun sheetOpensActionClosesAndBackDismisses() {
        launchDemo().use {
            openPage(DemoPageId.Dialog)
            tap(DialogPageFragment.OPEN_SHEET)
            assertDialogShown(confirmTitle)
            onView(withText(resources.getString(DemoR.string.demo_sample_cancel))).inRoot(isDialog()).perform(click())
            assertDialogGone(confirmTitle)
            assertResult(resources.getString(DemoR.string.demo_sample_cancel))

            tap(DialogPageFragment.OPEN_SHEET)
            assertDialogShown(confirmTitle)
            Espresso.pressBack()
            assertDialogGone(confirmTitle)
            assertResult(resources.getString(DemoR.string.demo_component_dismissed))
        }
    }

    @Test
    fun backDismissesTheCenterDialog() {
        launchDemo().use {
            openPage(DemoPageId.Dialog)
            tap(DialogPageFragment.OPEN_CENTER)
            assertDialogShown(confirmTitle)
            Espresso.pressBack()
            assertDialogGone(confirmTitle)
            assertResult(resources.getString(DemoR.string.demo_component_dismissed))
        }
    }

    @Test
    fun predictiveBackGestureDismissesCenterAndSheet() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && gestureNavigation)
        launchDemo().use {
            openPage(DemoPageId.Dialog)
            listOf(DialogPageFragment.OPEN_CENTER, DialogPageFragment.OPEN_SHEET).forEach { opener ->
                tap(opener)
                assertDialogShown(confirmTitle)
                edgeSwipe()
                assertDialogGone(confirmTitle)
                assertResult(resources.getString(DemoR.string.demo_component_dismissed))
            }
        }
    }

    @Test
    fun secondDialogIsRefusedWhileOneIsOpen() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.Dialog)
            val before = scenario.refusedCount()
            val stackingTitle = resources.getString(DemoR.string.demo_sample_stacking_title)
            tap(DialogPageFragment.OPEN_STACKING)
            assertDialogShown(stackingTitle)
            onView(withText(resources.getString(DemoR.string.demo_component_open_another))).inRoot(isDialog()).perform(click())
            instrumentation.waitForIdleSync()
            assertDialogShown(stackingTitle)
            onView(withText(confirmTitle)).check(doesNotExist())

            onView(withText(resources.getString(DemoR.string.demo_sample_close))).inRoot(isDialog()).perform(click())
            assertDialogGone(stackingTitle)
            assertEquals(before + 1, scenario.refusedCount())
        }
    }

    @Test
    fun deleteActionChainsAnUndoDialog() {
        val deletedTitle = resources.getString(DemoR.string.demo_sample_project_deleted_title)
        launchDemo().use {
            openPage(DemoPageId.Dialog)
            tap(DialogPageFragment.OPEN_CHAIN)
            assertDialogShown(confirmTitle)
            onView(withText(resources.getString(DemoR.string.demo_sample_delete))).inRoot(isDialog()).perform(click())
            assertDialogShown(deletedTitle)
            onView(withText(confirmTitle)).check(doesNotExist())

            onView(withText(resources.getString(DemoR.string.demo_sample_undo))).inRoot(isDialog()).perform(click())
            assertDialogGone(deletedTitle)
            onView(withTag(DialogPageFragment.CHAIN_RESULT))
                .perform(scrollIntoView())
                .check(matches(withText(resources.getString(DemoR.string.demo_component_dialog_result, resources.getString(DemoR.string.demo_sample_undo)))))
        }
    }

    @Test
    fun droppedDialogIsPresentedAgainAfterRecreationWithItsDraft() {
        val renameTitle = resources.getString(DemoR.string.demo_sample_rename_title)
        launchDemo().use { scenario ->
            openPage(DemoPageId.Dialog)
            tap(DialogPageFragment.OPEN_INPUT)
            assertDialogShown(renameTitle)
            scenario.onActivity { activity ->
                activity.findDialogView<TSTextFieldView>(DialogPageFragment.INPUT_TAG, DialogPageFragment.INPUT_FIELD).text = DRAFT
            }

            scenario.recreate()
            assertDialogShown(renameTitle)
            scenario.onActivity { activity ->
                assertEquals(DRAFT, activity.findDialogView<TSTextFieldView>(DialogPageFragment.INPUT_TAG, DialogPageFragment.INPUT_FIELD).text.toString())
            }
            onView(withText(resources.getString(DemoR.string.demo_sample_rename))).inRoot(isDialog()).perform(click())
            assertDialogGone(renameTitle)
            onView(withText(resources.getString(DemoR.string.demo_component_dialog_result, DRAFT))).perform(scrollIntoView()).check(matches(isDisplayed()))
        }
    }

    @Test
    fun bottomNavShellOpensFromItsPageAndBackReturns() {
        launchDemo().use {
            openPage(DemoPageId.BottomNav)
            tap(BottomNavFragment.OPEN_SHELL)
            onView(withTag(BottomNavShellFragment.SHELL)).check(matches(isDisplayed()))
            onView(withTag(DemoTags.ACTION_THEME)).check(matches(isDisplayed()))
            Espresso.pressBack()
            onView(withTag(DemoTags.screen(DemoPageId.BottomNav))).check(matches(isDisplayed()))
        }
    }

    private fun <T : View> MainActivity.findDialogView(dialogTag: String, viewTag: String): T {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val page = requireNotNull(navHost.childFragmentManager.primaryNavigationFragment)
        val dialog = (page.childFragmentManager.findFragmentByTag(dialogTag) as DialogFragment).requireDialog()
        return requireNotNull(dialog.window).decorView.findViewWithTag(viewTag)
    }

    private companion object {
        const val DRAFT = "Atlas v2"
        const val GESTURAL_NAVIGATION = 2
        const val SETTLE_MILLIS = 1_000L
        const val SCRIM_INSET = 20f
        const val SCRIM_HEIGHT_FRACTION = 0.15f
    }
}
