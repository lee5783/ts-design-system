package com.ts.ds.demo.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.compose.components.BottomNavPageTags
import com.ts.ds.demo.compose.components.TabsPageTags
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import org.junit.Assert.assertTrue
import com.ts.ds.demo.support.catalog.DemoPageId
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationComponentsTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun ComposeTestRule.scrollTo(tag: String) {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
    }

    private fun ComposeTestRule.tab(list: String, label: String) = onNode(hasText(label) and hasAnyAncestor(hasTestTag(list)))

    @Test
    fun tabsSelectionUpdatesTheValueAndDisabledBlocks() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Tabs)
            composeRule.scrollTo(TabsPageTags.PLAYGROUND_COUNT)
            composeRule.tab(TabsPageTags.PLAYGROUND_TABS, "Week")
                .assertIsNotSelected()
                .performTouchInput { click(Offset(centerX, 2.dp.toPx())) }
            composeRule.tab(TabsPageTags.PLAYGROUND_TABS, "Week").assertIsSelected()
            composeRule.tab(TabsPageTags.PLAYGROUND_TABS, "Day").assertIsNotSelected()
            composeRule.onNodeWithTag(TabsPageTags.PLAYGROUND_VALUE).assertTextEquals("Selected: Week")
            composeRule.onNodeWithTag(TabsPageTags.PLAYGROUND_COUNT).assertTextEquals("Tapped 1 time")

            composeRule.tab(TabsPageTags.PLAYGROUND_TABS, "Week").performClick()
            composeRule.onNodeWithTag(TabsPageTags.PLAYGROUND_COUNT).assertTextEquals("Tapped 1 time")

            composeRule.scrollTo(TabsPageTags.PLAYGROUND_DISABLED)
            composeRule.onNodeWithTag(TabsPageTags.PLAYGROUND_DISABLED).performClick()
            composeRule.scrollTo(TabsPageTags.PLAYGROUND_COUNT)
            composeRule.tab(TabsPageTags.PLAYGROUND_TABS, "Month").performClick().assertIsNotSelected()
            composeRule.onNodeWithTag(TabsPageTags.PLAYGROUND_COUNT).assertTextEquals("Tapped 1 time")
        }
    }

    @Test
    fun underlineTabsScrollWhenTheyOverflow() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Tabs)
            composeRule.scrollTo(TabsPageTags.OVERFLOW_VALUE)
            composeRule.tab(TabsPageTags.OVERFLOW, "Reports").assertIsNotDisplayed()

            repeat(3) { composeRule.onNodeWithTag(TabsPageTags.OVERFLOW).performTouchInput { swipeLeft() } }
            composeRule.waitForIdle()

            composeRule.tab(TabsPageTags.OVERFLOW, "Reports").assertIsDisplayed().performClick().assertIsSelected()
            composeRule.onNodeWithTag(TabsPageTags.OVERFLOW_VALUE).assertTextEquals("Selected: Reports")
        }
    }

    private fun ComposeTestRule.navBadge(nav: String, label: String, badge: String) =
        tab(nav, label).assert(hasText(badge))

    private fun ComposeTestRule.navWithoutBadge(nav: String, label: String, badge: String) =
        tab(nav, label).assert(!hasText(badge))

    @Test
    fun bottomNavSelectionBadgeAndDisabled() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.BottomNav)
            composeRule.scrollTo(BottomNavPageTags.PLAYGROUND_CHANGES)
            composeRule.tab(BottomNavPageTags.PLAYGROUND_NAV, "Search")
                .assertIsNotSelected()
                .performClick()
            composeRule.tab(BottomNavPageTags.PLAYGROUND_NAV, "Search").assertIsSelected()
            composeRule.tab(BottomNavPageTags.PLAYGROUND_NAV, "Home").assertIsNotSelected()
            composeRule.onNodeWithTag(BottomNavPageTags.PLAYGROUND_VALUE).assertTextEquals("Selected: Search")
            composeRule.onNodeWithTag(BottomNavPageTags.PLAYGROUND_CHANGES).assertTextEquals("Tapped 1 time")

            composeRule.tab(BottomNavPageTags.PLAYGROUND_NAV, "Search").performClick()
            composeRule.onNodeWithTag(BottomNavPageTags.PLAYGROUND_CHANGES).assertTextEquals("Tapped 1 time")

            composeRule.navBadge(BottomNavPageTags.PLAYGROUND_NAV, "Inbox", "3")
            composeRule.scrollTo(BottomNavPageTags.PLAYGROUND_ADD_HUNDRED)
            composeRule.onNodeWithTag(BottomNavPageTags.PLAYGROUND_ADD_HUNDRED).performClick()
            composeRule.scrollTo(BottomNavPageTags.PLAYGROUND_CHANGES)
            composeRule.navBadge(BottomNavPageTags.PLAYGROUND_NAV, "Inbox", "99+")
            composeRule.scrollTo(BottomNavPageTags.PLAYGROUND_CLEAR)
            composeRule.onNodeWithTag(BottomNavPageTags.PLAYGROUND_CLEAR).performClick()
            composeRule.scrollTo(BottomNavPageTags.PLAYGROUND_CHANGES)
            composeRule.navWithoutBadge(BottomNavPageTags.PLAYGROUND_NAV, "Inbox", "99+")

            composeRule.scrollTo(BottomNavPageTags.PLAYGROUND_DISABLED)
            composeRule.onNodeWithTag(BottomNavPageTags.PLAYGROUND_DISABLED).performClick()
            composeRule.scrollTo(BottomNavPageTags.PLAYGROUND_CHANGES)
            composeRule.tab(BottomNavPageTags.PLAYGROUND_NAV, "Alerts").performClick().assertIsNotSelected()
            composeRule.onNodeWithTag(BottomNavPageTags.PLAYGROUND_CHANGES).assertTextEquals("Tapped 1 time")
        }
    }

    @Test
    fun bottomNavShellSwitchesTabsAndShowsAToastAboveTheBar() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.BottomNav)
            composeRule.scrollTo(BottomNavPageTags.OPEN_SHELL)
            composeRule.onNodeWithTag(BottomNavPageTags.OPEN_SHELL).performClick()
            composeRule.onNodeWithTag(BottomNavPageTags.SHELL).assertIsDisplayed()
            composeRule.onNodeWithTag(BottomNavPageTags.shellTitle("home")).assertIsDisplayed()

            composeRule.tab(BottomNavPageTags.SHELL_NAV, "Inbox").performClick().assertIsSelected()
            composeRule.onNodeWithTag(BottomNavPageTags.shellTitle("inbox")).assertIsDisplayed()
            composeRule.onNodeWithTag(BottomNavPageTags.shellAddOne("inbox")).performClick()
            composeRule.navBadge(BottomNavPageTags.SHELL_NAV, "Inbox", "4")

            composeRule.onNodeWithTag(BottomNavPageTags.shellToast("inbox")).performClick()
            val sentToast = hasText("Message sent", substring = true)
            composeRule.waitUntil(5_000) { composeRule.onAllNodes(sentToast).fetchSemanticsNodes().isNotEmpty() }
            val toast = composeRule.onNode(sentToast).getBoundsInRoot()
            val nav = composeRule.onNodeWithTag(BottomNavPageTags.SHELL_NAV).getBoundsInRoot()
            assertTrue("toast ${toast.bottom} should sit above the bar ${nav.top}", toast.bottom <= nav.top)
        }
    }
}
