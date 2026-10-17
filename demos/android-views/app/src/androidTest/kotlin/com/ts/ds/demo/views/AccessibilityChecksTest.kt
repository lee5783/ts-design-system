package com.ts.ds.demo.views

import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.allViews
import androidx.core.view.ancestors
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.Parameters
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement
import com.ts.ds.demo.support.catalog.DemoCatalog
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoTheme
import com.ts.ds.views.components.bottomnav.TSBottomNavView
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class AccessibilityChecksTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val checks = AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)
    private val suppressions = listOf(
        Suppression(
            screen = "bottom-nav",
            check = "TextSizeCheck",
            reason = "Badge counts keep a fixed size inside the 16dp badge until the pending font-scale decision for icons, dots and badges",
        ) { element ->
            element?.text?.toString()?.matches(BADGE_COUNT) == true &&
                element.boundsInScreen.let { bounds -> bottomNavBounds.any { it.contains(bounds.left, bounds.top, bounds.right, bounds.bottom) } }
        },
        Suppression(
            screen = null,
            check = "TouchTargetSizeCheck",
            reason = "Before API 29 AccessibilityNodeInfo cannot report TouchDelegate bounds, so ATF sees only the visual size of controls whose 48dp hit area comes from a TouchDelegate; ATF itself downgrades these to a warning when it can see the delegate, and the API 29+ run verifies the delegate size",
        ) { element ->
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                element?.boundsInScreen?.let { bounds -> touchDelegatedBounds.any { it.contains(bounds.left, bounds.top, bounds.right, bounds.bottom) } } == true
        },
    )
    private lateinit var scenario: ActivityScenario<MainActivity>
    private var bottomNavBounds: List<Rect> = emptyList()
    private var touchDelegatedBounds: List<Rect> = emptyList()

    @After
    fun resetSettings() {
        instrumentation.runOnMainSync { settingsStore.reset() }
    }

    @Test
    fun lightThemeScreensPassAccessibilityChecks() {
        checkEveryScreen(DemoSettings(theme = DemoTheme.Light, reduceMotion = true))
    }

    @Test
    fun darkThemeScreensPassAccessibilityChecks() {
        checkEveryScreen(DemoSettings(theme = DemoTheme.Dark, reduceMotion = true))
    }

    @Test
    fun largeTextRtlScreensPassAccessibilityChecks() {
        checkEveryScreen(DemoSettings(fontScale = 2f, rtl = true, reduceMotion = true))
    }

    private fun checkEveryScreen(settings: DemoSettings) {
        val failures = mutableListOf<String>()
        launchDemo(settings).use { launched ->
            scenario = launched
            failures += checkWhileScrolling("catalog")
            (DemoCatalog.foundationPages + DemoCatalog.componentPages).forEach { page ->
                openPage(page)
                failures += checkWhileScrolling(page.id)
                Espresso.pressBack()
            }
            onView(withId(R.id.action_settings)).perform(click())
            failures += checkWhileScrolling("settings")
            Espresso.pressBack()
            onView(withId(R.id.action_about)).perform(click())
            failures += checkWhileScrolling("about")
        }
        assertTrue(failures.joinToString(separator = "\n"), failures.isEmpty())
    }

    private fun checkWhileScrolling(screen: String): List<String> {
        val failures = LinkedHashMap<String, String>()
        repeat(MAX_SCROLL_STEPS) {
            check(screen).forEach { (key, message) -> failures.putIfAbsent(key, message) }
            if (!scrollOneViewport(screen)) return failures.values.toList()
        }
        error("$screen did not reach its end within $MAX_SCROLL_STEPS viewports")
    }

    private fun scrollOneViewport(screen: String): Boolean {
        val scroller = activeWindowRoot(screen).selfAndDescendants()
            .filter { it.isScrollable }
            .maxByOrNull { node -> Rect().also(node::getBoundsInScreen).let { it.width() * it.height() } }
            ?: return false
        if (AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD !in scroller.actionList) return false
        val scrolled = scroller.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        instrumentation.waitForIdleSync()
        SystemClock.sleep(SCROLL_SETTLE_MS)
        return scrolled
    }

    private fun AccessibilityNodeInfo.selfAndDescendants(): Sequence<AccessibilityNodeInfo> = sequence {
        yield(this@selfAndDescendants)
        (0 until childCount).forEach { index -> getChild(index)?.let { yieldAll(it.selfAndDescendants()) } }
    }

    private fun currentBottomNavBounds(): List<Rect> {
        val bounds = mutableListOf<Rect>()
        scenario.onActivity { activity ->
            activity.window.decorView.allViews.filterIsInstance<TSBottomNavView>().filter(View::isShown).forEach { nav ->
                val location = IntArray(2).also(nav::getLocationOnScreen)
                bounds += Rect(location[0], location[1], location[0] + nav.width, location[1] + nav.height)
            }
        }
        return bounds
    }

    private fun currentTouchDelegatedBounds(): List<Rect> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return emptyList()
        val bounds = mutableListOf<Rect>()
        scenario.onActivity { activity ->
            activity.window.decorView.allViews
                .filter { view -> view.isShown && view.isClickable && view.ancestors.any { (it as? View)?.touchDelegate != null } }
                .forEach { view ->
                    val location = IntArray(2).also(view::getLocationOnScreen)
                    bounds += Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
                }
        }
        return bounds
    }

    private fun check(screen: String): List<Pair<String, String>> {
        instrumentation.waitForIdleSync()
        bottomNavBounds = currentBottomNavBounds()
        touchDelegatedBounds = currentTouchDelegatedBounds()
        val root = activeWindowRoot(screen)
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
        const val MAX_SCROLL_STEPS = 60
        const val SCROLL_SETTLE_MS = 300L
        val BADGE_COUNT = Regex("\\d+\\+?")
    }
}
