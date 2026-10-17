package com.ts.ds.demo.compose

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.compose.shell.DemoTags
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoCatalog
import com.ts.ds.demo.support.catalog.DemoPageKind
import com.ts.ds.demo.support.settings.DemoSettings
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageWalkTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val resources = instrumentation.targetContext.resources

    @After
    fun resetSettings() {
        instrumentation.runOnMainSync { settingsStore.reset() }
    }

    private class Frame(val heightDp: Dp, val statusBarDp: Dp, val navigationBarDp: Dp)

    private fun ActivityScenario<MainActivity>.frame(): Frame {
        lateinit var frame: Frame
        onActivity { activity ->
            val decor = activity.window.decorView
            val density = activity.resources.displayMetrics.density
            val insets = requireNotNull(ViewCompat.getRootWindowInsets(decor)).getInsets(WindowInsetsCompat.Type.systemBars())
            frame = Frame((decor.height / density).dp, (insets.top / density).dp, (insets.bottom / density).dp)
        }
        return frame
    }

    private fun walk(settings: DemoSettings) {
        val playground = resources.getString(DemoR.string.demo_component_playground).uppercase()
        launchDemo(settings).use { scenario ->
            val frame = scenario.frame()
            DemoCatalog.allPages.forEach { page ->
                val screen = composeRule.openPage(page).assertIsDisplayed()
                val bounds = screen.getBoundsInRoot()
                assertEquals("${page.id} draws under the status bar", 0.dp, bounds.top)
                assertEquals("${page.id} draws under the navigation bar", frame.heightDp.value, bounds.bottom.value, 1f)
                val title = composeRule.onNodeWithTag(DemoTags.SCREEN_TITLE).getBoundsInRoot()
                assertTrue("${page.id} title clears the status bar", title.top >= frame.statusBarDp)
                composeRule.onNodeWithTag(DemoTags.ACTION_THEME).assertIsDisplayed()

                val list = composeRule.onNode(hasScrollToNodeAction())
                if (page.kind == DemoPageKind.Component) {
                    list.performScrollToNode(hasText(playground))
                    composeRule.onNodeWithText(playground).assertIsDisplayed()
                }
                list.performSemanticsAction(SemanticsActions.ScrollBy) { scrollBy -> scrollBy(0f, END_OF_PAGE_PX) }
                val last = list.onChildren().onLast().getBoundsInRoot()
                assertTrue("${page.id} scrolls its end above the navigation bar", last.bottom <= frame.heightDp - frame.navigationBarDp)
                Espresso.pressBack()
                composeRule.onNodeWithTag(DemoTags.CATALOG).assertIsDisplayed()
            }
        }
    }

    @Test
    fun everyPageIsEdgeToEdgeWithThemeActionAndPlayground() {
        walk(DemoSettings())
    }

    @Test
    fun everyPageWalksAt200PercentFontInRtl() {
        walk(DemoSettings(fontScale = 2f, rtl = true))
    }

    private companion object {
        const val END_OF_PAGE_PX = 1_000_000f
    }
}
