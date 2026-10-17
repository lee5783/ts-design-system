package com.ts.ds.demo.views

import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Checkable
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.components.CheckboxFragment
import com.ts.ds.demo.views.components.DemoToggleViewSpec
import com.ts.ds.demo.views.components.SwitchFragment
import com.ts.ds.demo.views.components.TextFieldFragment
import com.ts.ds.views.components.textfield.TSTextFieldView
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormComponentsTest {
    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun tap(tag: String) {
        onView(withTag(tag)).perform(scrollIntoView(), click())
    }

    private fun input() = onView(allOf(isDescendantOfA(withTag(TextFieldFragment.PLAYGROUND_FIELD)), withClassName(endsWith("TSTextFieldInput"))))

    private fun toggle(label: String) = onView(allOf(isDescendantOfA(withTag(TextFieldFragment.PLAYGROUND_FIELD)), withContentDescription(label)))

    private fun <T> ActivityScenario<MainActivity>.read(block: (TSTextFieldView) -> T): T {
        var result: Result<T>? = null
        onActivity { result = runCatching { block(it.window.decorView.findViewWithTag(TextFieldFragment.PLAYGROUND_FIELD)) } }
        return requireNotNull(result).getOrThrow()
    }

    private fun TSTextFieldView.inputNode(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(editText, it) }

    private fun TSTextFieldView.masked(): Boolean = editText.transformationMethod is PasswordTransformationMethod

    @Test
    fun textFieldTypingErrorAndPasswordToggle() {
        val error = resources.getString(DemoR.string.demo_sample_email_error)
        launchDemo().use { scenario ->
            openPage(DemoPageId.TextField)
            input().perform(closeSoftKeyboard(), scrollIntoView(), click(), typeText("ada"), closeSoftKeyboard())
            assertEquals("ada", scenario.read { it.text.toString() })
            assertTrue(scenario.read { it.editText.isFocused })
            assertEquals(null, scenario.read { it.inputNode().error })

            tap(TextFieldFragment.PLAYGROUND_ERROR)
            assertEquals(error, scenario.read { it.inputNode().error.toString() })
            assertTrue(scenario.read { it.inputNode().isContentInvalid })

            tap(TextFieldFragment.PLAYGROUND_SECURE)
            assertTrue(scenario.read { it.masked() })
            toggle("Show password").perform(scrollIntoView(), click())
            assertFalse(scenario.read { it.masked() })
            toggle("Hide password").perform(scrollIntoView(), click())
            assertTrue(scenario.read { it.masked() })
            assertEquals("ada", scenario.read { it.text.toString() })
        }
    }

    @Test
    fun disabledAndReadOnlyBlockEditing() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.TextField)
            tap(TextFieldFragment.PLAYGROUND_DISABLED)
            assertFalse(scenario.read { it.editText.isEnabled })

            tap(TextFieldFragment.PLAYGROUND_DISABLED)
            assertTrue(scenario.read { it.editText.isEnabled })
            tap(TextFieldFragment.PLAYGROUND_READ_ONLY)
            assertFalse(scenario.read { it.editText.onCheckIsTextEditor() })
            assertTrue(scenario.read { it.editText.isEnabled })
        }
    }

    private fun clickNearEnd() = GeneralClickAction(
        Tap.SINGLE,
        CoordinatesProvider { view ->
            val location = IntArray(2).also(view::getLocationOnScreen)
            val endX = if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL) location[0] + 4f else location[0] + view.width - 4f
            floatArrayOf(endX, location[1] + view.height / 2f)
        },
        Press.FINGER,
        0,
        0,
    )

    private fun <T> ActivityScenario<MainActivity>.readTag(tag: String, block: (View) -> T): T {
        var result: Result<T>? = null
        onActivity { result = runCatching { block(it.window.decorView.findViewWithTag(tag)) } }
        return requireNotNull(result).getOrThrow()
    }

    private fun <T : View> toggleTogglesFromTheLabelAndDisabledBlocks(page: DemoPageId, spec: DemoToggleViewSpec<T>, onState: String, offState: String) {
        val on = resources.getString(spec.onRes)
        val off = resources.getString(spec.offRes)
        launchDemo().use { scenario ->
            openPage(page)
            val checked = { scenario.readTag(spec.playgroundControlTag) { (it as Checkable).isChecked } }
            val state = { scenario.readTag(spec.playgroundControlTag) { ViewCompat.getStateDescription(it).toString() } }
            val value = { scenario.readTag(spec.playgroundValueTag) { (it as TextView).text.toString() } }
            val count = { scenario.readTag(spec.playgroundCountTag) { (it as TextView).text.toString() } }
            assertEquals(offState, state())

            onView(withTag(spec.playgroundControlTag)).perform(scrollIntoView(), clickNearEnd())
            assertTrue(checked())
            assertEquals(onState, state())
            assertEquals(on, value())
            onView(withTag(spec.playgroundControlTag)).perform(scrollIntoView(), click())
            assertFalse(checked())
            assertEquals(off, value())
            assertEquals("Tapped 2 times", count())

            tap(spec.playgroundDisabledTag)
            onView(withTag(spec.playgroundControlTag)).perform(scrollIntoView(), click())
            assertFalse(checked())
            assertEquals("Tapped 2 times", count())
        }
    }

    @Test
    fun checkboxTogglesFromTheLabelAndDisabledBlocks() {
        toggleTogglesFromTheLabelAndDisabledBlocks(DemoPageId.Checkbox, CheckboxFragment.SPEC, "Checked", "Unchecked")
    }

    @Test
    fun switchTogglesFromTheLabelAndDisabledBlocks() {
        toggleTogglesFromTheLabelAndDisabledBlocks(DemoPageId.Switch, SwitchFragment.SPEC, "On", "Off")
    }
}
