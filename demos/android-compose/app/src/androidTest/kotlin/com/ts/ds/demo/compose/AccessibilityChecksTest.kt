package com.ts.ds.demo.compose

import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.Parameters
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement
import com.ts.ds.demo.compose.shell.DemoTags
import com.ts.ds.demo.support.catalog.DemoCatalog
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoTheme
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class AccessibilityChecksTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val checks = AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)
    private val density = instrumentation.targetContext.resources.displayMetrics.density
    private val suppressions = listOf(
        Suppression(
            screen = "tabs",
            check = "TouchTargetSizeCheck",
            reason = "Short underline labels 16dp apart cannot each own a 48dp area; Compose trims the overlap to at least 44dp until the pending tab minimum-width decision",
        ) { element ->
            element != null &&
                element.boundsInScreen.let { bounds -> tabBounds.any { it.contains((bounds.left + bounds.right) / 2, (bounds.top + bounds.bottom) / 2) } } &&
                element.boundsInScreen.width / density >= TRIMMED_TAB_MIN_DP &&
                element.boundsInScreen.height / density >= MIN_TOUCH_TARGET_DP
        },
    ) + listOf("TouchTargetSizeCheck", "SpeakableTextPresentCheck").map { check ->
        Suppression(
            screen = null,
            check = check,
            reason = "After a scroll step Compose reports the touch bounds of list content scrolled under the top bar without clipping them to the list, so ATF sees the top bar actions shrunk and the straddling item without its scrolled-away label; the chrome is checked unscrolled at step 0 and hit testing is unaffected",
        ) { element -> element != null && scrolledViewportTop?.let { top -> element.boundsInScreen.top < top } == true }
    }
    private var scrolledViewportTop: Int? = null
    private var tabBounds: List<Rect> = emptyList()
    private val isTab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
    private val verticallyScrollable = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)

    @After
    fun resetSettings() {
        instrumentation.runOnMainSync { settingsStore.reset() }
    }

    @Test
    fun lightThemeScreensPassAccessibilityChecks() {
        checkEveryScreen(DemoSettings(theme = DemoTheme.Light))
    }

    @Test
    fun darkThemeScreensPassAccessibilityChecks() {
        checkEveryScreen(DemoSettings(theme = DemoTheme.Dark))
    }

    @Test
    fun largeTextRtlScreensPassAccessibilityChecks() {
        checkEveryScreen(DemoSettings(fontScale = 2f, rtl = true))
    }

    private fun checkEveryScreen(settings: DemoSettings) {
        val failures = mutableListOf<String>()
        launchDemo(settings).use {
            failures += checkWhileScrolling("catalog")
            (DemoCatalog.foundationPages + DemoCatalog.componentPages).forEach { page ->
                composeRule.openPage(page)
                failures += checkWhileScrolling(page.id)
                Espresso.pressBack()
            }
            composeRule.onNodeWithTag(DemoTags.ACTION_SETTINGS).performClick()
            failures += checkWhileScrolling("settings")
            Espresso.pressBack()
            composeRule.onNodeWithTag(DemoTags.ACTION_ABOUT).performClick()
            failures += checkWhileScrolling("about")
        }
        assertTrue(failures.joinToString(separator = "\n"), failures.isEmpty())
    }

    private fun checkWhileScrolling(screen: String): List<String> {
        val failures = LinkedHashMap<String, String>()
        repeat(MAX_SCROLL_STEPS) { step ->
            check(screen, scrolled = step > 0).forEach { (key, message) -> failures.putIfAbsent(key, message) }
            if (!scrollOneViewport()) return failures.values.toList()
        }
        error("$screen did not reach its end within $MAX_SCROLL_STEPS viewports")
    }

    private fun scrollOneViewport(): Boolean {
        val node = composeRule.onAllNodes(verticallyScrollable).fetchSemanticsNodes().firstOrNull() ?: return false
        val range = node.config[SemanticsProperties.VerticalScrollAxisRange]
        if (range.value() >= range.maxValue()) return false
        composeRule.onAllNodes(verticallyScrollable).onFirst().performSemanticsAction(SemanticsActions.ScrollBy) { scrollBy ->
            scrollBy(0f, node.size.height * VIEWPORT_STEP)
        }
        composeRule.waitForIdle()
        SystemClock.sleep(SCROLL_SETTLE_MS)
        return true
    }

    private fun check(screen: String, scrolled: Boolean): List<Pair<String, String>> {
        composeRule.waitForIdle()
        val root = activeWindowRoot(screen)
        scrolledViewportTop = if (scrolled) viewportTop(root) else null
        tabBounds = currentTabBounds()
        val hierarchy = AccessibilityHierarchyAndroid.newBuilder(root, instrumentation.targetContext).build()
        return checks
            .flatMap { it.runCheckOnHierarchy(hierarchy, null, Parameters()) }
            .filter { it.type == AccessibilityCheckResultType.ERROR }
            .filterNot { result -> suppressions.any { it.matches(screen, result) } }
            .map { result ->
                val element = result.element
                val label = element?.let { listOfNotNull(it.text, it.contentDescription, it.className).joinToString(" | ") }
                val key = "$screen: ${result.sourceCheckClass.simpleName}: [$label]"
                key to "$screen: ${result.sourceCheckClass.simpleName}: ${result.getMessage(Locale.ENGLISH)} [$label ${element?.boundsInScreen}]"
            }
    }

    private fun currentTabBounds(): List<Rect> = composeRule.onAllNodes(isTab, useUnmergedTree = true).fetchSemanticsNodes().map { node ->
        val origin = node.positionOnScreen
        Rect(origin.x.roundToInt(), origin.y.roundToInt(), (origin.x + node.size.width).roundToInt(), (origin.y + node.size.height).roundToInt())
    }

    private fun viewportTop(root: AccessibilityNodeInfo): Int? = root.selfAndDescendants()
        .filter { it.isScrollable }
        .map { node -> Rect().also(node::getBoundsInScreen) }
        .maxByOrNull { it.width() * it.height() }
        ?.top

    private fun AccessibilityNodeInfo.selfAndDescendants(): Sequence<AccessibilityNodeInfo> = sequence {
        yield(this@selfAndDescendants)
        (0 until childCount).forEach { index -> getChild(index)?.let { yieldAll(it.selfAndDescendants()) } }
    }

    private fun activeWindowRoot(screen: String): AccessibilityNodeInfo {
        val deadline = SystemClock.uptimeMillis() + WINDOW_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            instrumentation.uiAutomation.rootInActiveWindow
                ?.takeIf { it.packageName == instrumentation.targetContext.packageName }
                ?.let { return it }
            SystemClock.sleep(WINDOW_POLL_MS)
        }
        error("No active window of the demo app for $screen")
    }

    private class Suppression(
        val screen: String?,
        val check: String,
        val reason: String,
        val applies: (ViewHierarchyElement?) -> Boolean,
    ) {
        fun matches(screen: String, result: AccessibilityHierarchyCheckResult): Boolean =
            (this.screen == null || screen == this.screen) && result.sourceCheckClass.simpleName == check && applies(result.element)
    }

    private companion object {
        const val WINDOW_TIMEOUT_MS = 5_000L
        const val WINDOW_POLL_MS = 100L
        const val TRIMMED_TAB_MIN_DP = 44f
        const val MIN_TOUCH_TARGET_DP = 48f
        const val MAX_SCROLL_STEPS = 60
        const val VIEWPORT_STEP = 0.8f
        const val SCROLL_SETTLE_MS = 300L
    }
}
