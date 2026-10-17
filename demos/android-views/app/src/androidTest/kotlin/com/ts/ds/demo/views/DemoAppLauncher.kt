package com.ts.ds.demo.views

import android.graphics.Rect
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoSettingsStore
import com.ts.ds.demo.views.shell.DemoTags
import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo

val settingsStore: DemoSettingsStore
    get() = DemoSettingsStore.from(InstrumentationRegistry.getInstrumentation().targetContext)

fun launchDemo(settings: DemoSettings = DemoSettings(reduceMotion = true)): ActivityScenario<MainActivity> {
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        settingsStore.update { settings }
    }
    return ActivityScenario.launch(MainActivity::class.java)
}

fun withTag(tag: String): Matcher<View> = withTagValue(equalTo(tag))

fun openPage(page: DemoPageId): ViewInteraction {
    onView(withId(R.id.catalog_list))
        .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(withTag(page.testTag)))
    onView(withTag(page.testTag)).perform(click())
    return onView(withTag(DemoTags.screen(page)))
}

fun scrollIntoView(): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isDescendantOfA(isAssignableFrom(NestedScrollView::class.java))

    override fun getDescription(): String = "scroll the view inside the padded viewport of its NestedScrollView"

    override fun perform(uiController: UiController, view: View) {
        val scroll = generateSequence(view.parent) { it.parent }.filterIsInstance<NestedScrollView>().first()
        val rect = Rect(0, 0, view.width, view.height).also { scroll.offsetDescendantRectToMyCoords(view, it) }
        val top = scroll.scrollY + scroll.paddingTop
        val bottom = scroll.scrollY + scroll.height - scroll.paddingBottom
        val delta = when {
            rect.top < top -> rect.top - top
            rect.bottom > bottom -> minOf(rect.bottom - bottom, rect.top - top)
            else -> 0
        }
        if (delta != 0) scroll.scrollBy(0, delta)
        uiController.loopMainThreadUntilIdle()
    }
}
