package com.tinysol.designsystem.views.sample

import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Checkable
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.components.CheckboxFragment
import com.tinysol.designsystem.views.sample.components.DemoToggleViewSpec
import com.tinysol.designsystem.views.sample.components.SwitchFragment
import com.tinysol.designsystem.views.sample.components.TextFieldFragment
import com.tinysol.designsystem.views.components.textfield.TSTextFieldView
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

    private fun toggle() = onView(allOf(isDescendantOfA(withTag(TextFieldFragment.PLAYGROUND_FIELD)), withClassName(endsWith("AppCompatImageButton"))))

    private fun <T> ActivityScenario<MainActivity>.read(block: (TSTextFieldView) -> T): T {
        var result: Result<T>? = null
        onActivity { result = runCatching { block(it.window.decorView.findViewWithTag(TextFieldFragment.PLAYGROUND_FIELD)) } }
        return requireNotNull(result).getOrThrow()
    }

    private fun TSTextFieldView.masked(): Boolean = editText.transformationMethod is PasswordTransformationMethod

    @Test
    fun textFieldTypingErrorAndPasswordToggle() {
        val error = resources.getString(DemoR.string.demo_sample_email_error)
        launchDemo().use { scenario ->
            openPage(DemoPageId.TextField)
            input().perform(closeSoftKeyboard(), scrollIntoView(), click(), typeText("ada"), closeSoftKeyboard())
            assertEquals("ada", scenario.read { it.text.toString() })
            assertTrue(scenario.read { it.editText.isFocused })
            onView(withText(error)).check(doesNotExist())

            tap(TextFieldFragment.PLAYGROUND_ERROR)
            onView(withText(error)).perform(scrollIntoView()).check(matches(isDisplayed()))

            tap(TextFieldFragment.PLAYGROUND_SECURE)
            assertTrue(scenario.read { it.masked() })
            toggle().perform(scrollIntoView(), click())
            assertFalse(scenario.read { it.masked() })
            toggle().perform(scrollIntoView(), click())
            assertTrue(scenario.read { it.masked() })
            assertEquals("ada", scenario.read { it.text.toString() })
        }
    }

    @Test
    fun codeFieldShowsTypedCodeUnmaskedWithACapitalsKeyboard() {
        launchDemo().use { scenario ->
            openPage(DemoPageId.TextField)
            onView(allOf(isDescendantOfA(withTag(TextFieldFragment.CODE_FIELD)), withClassName(endsWith("TSTextFieldInput"))))
                .perform(closeSoftKeyboard(), scrollIntoView(), click(), typeText("AB12CD"), closeSoftKeyboard())
            var text = ""
            var masked = true
            var inputType = 0
            scenario.onActivity {
                val code = it.window.decorView.findViewWithTag<TSTextFieldView>(TextFieldFragment.CODE_FIELD)
                text = code.text.toString()
                masked = code.masked()
                inputType = code.editText.inputType
            }
            assertEquals("AB12CD", text)
            assertFalse(masked)
            assertTrue(inputType and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS != 0)
            assertTrue(inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0)
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

    private fun <T : View> toggleTogglesFromTheLabelAndDisabledBlocks(page: DemoPageId, spec: DemoToggleViewSpec<T>) {
        val on = resources.getString(spec.onRes)
        val off = resources.getString(spec.offRes)
        launchDemo().use { scenario ->
            openPage(page)
            val checked = { scenario.readTag(spec.playgroundControlTag) { (it as Checkable).isChecked } }
            val value = { scenario.readTag(spec.playgroundValueTag) { (it as TextView).text.toString() } }
            val count = { scenario.readTag(spec.playgroundCountTag) { (it as TextView).text.toString() } }

            onView(withTag(spec.playgroundControlTag)).perform(scrollIntoView(), clickNearEnd())
            assertTrue(checked())
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
        toggleTogglesFromTheLabelAndDisabledBlocks(DemoPageId.Checkbox, CheckboxFragment.SPEC)
    }

    @Test
    fun switchTogglesFromTheLabelAndDisabledBlocks() {
        toggleTogglesFromTheLabelAndDisabledBlocks(DemoPageId.Switch, SwitchFragment.SPEC)
    }
}
