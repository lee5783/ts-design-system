package com.tinysol.designsystem.compose.sample

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tinysol.designsystem.core.style.TSTextColor
import com.tinysol.designsystem.core.style.TSTypeRole
import com.tinysol.designsystem.compose.sample.components.AvatarPageTags
import com.tinysol.designsystem.compose.sample.components.BadgePageTags
import com.tinysol.designsystem.compose.sample.components.CardPageTags
import com.tinysol.designsystem.compose.sample.components.ListItemPageTags
import com.tinysol.designsystem.compose.sample.components.ListItemTrailingOption
import com.tinysol.designsystem.compose.sample.components.TextPageTags
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentComponentsTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun ComposeTestRule.scrollTo(tag: String) {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
    }

    private fun ComposeTestRule.tap(tag: String) {
        scrollTo(tag)
        onNodeWithTag(tag).performClick()
        waitForIdle()
    }

    private fun ComposeTestRule.width(tag: String): Float =
        onNodeWithTag(tag).getUnclippedBoundsInRoot().let { it.right - it.left }.value

    @Test
    fun listItemDisclosureRowExpandsItsBody() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.ListItem)
            val kotlin = ListItemPageTags.disclosure("kotlin")
            val snapshot = ListItemPageTags.disclosure("snapshot")
            val kotlinHeader = hasText(resources.getString(DemoR.string.demo_sample_licence_kotlin)) and hasClickAction()
            val snapshotHeader = hasText(resources.getString(DemoR.string.demo_sample_licence_snapshot)) and hasClickAction()
            composeRule.scrollTo(snapshot)
            composeRule.onNode(kotlinHeader).assertExists()
            composeRule.onNodeWithTag(ListItemPageTags.DISCLOSURE_BODY).assertDoesNotExist()
            composeRule.onNode(kotlinHeader).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(ListItemPageTags.DISCLOSURE_BODY)
                .assertTextEquals(resources.getString(DemoR.string.demo_sample_licence_apache_body))
            composeRule.scrollTo(snapshot)
            composeRule.onNode(snapshotHeader).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(ListItemPageTags.DISCLOSURE_BODY)
                .assertTextEquals(resources.getString(DemoR.string.demo_sample_licence_mit_body))
            composeRule.onNode(snapshotHeader).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(ListItemPageTags.DISCLOSURE_BODY).assertDoesNotExist()
        }
    }

    @Test
    fun listItemLazyListRowsAreDividedOutsideAList() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.ListItem)
            composeRule.scrollTo(ListItemPageTags.LAZY_DIVIDER)
            assertTrue(composeRule.onAllNodesWithTag(ListItemPageTags.LAZY_DIVIDER).fetchSemanticsNodes().isNotEmpty())
        }
    }

    @Test
    fun textPageShowsEveryTypeRoleAndTextColor() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Text)
            val pangram = resources.getString(DemoR.string.demo_sample_pangram)
            TSTypeRole.entries.forEach { role ->
                val tag = TextPageTags.role(role)
                composeRule.scrollTo(tag)
                composeRule.onNodeWithTag(tag).assertTextEquals(if (role == TSTypeRole.Overline) pangram.uppercase() else pangram)
            }
            TSTextColor.entries.forEach { color ->
                val tag = TextPageTags.color(color)
                composeRule.scrollTo(tag)
                composeRule.onNodeWithTag(tag).assertTextEquals(color.name.lowercase())
            }
            composeRule.scrollTo(TextPageTags.TRUNCATED)
            composeRule.onNodeWithTag(TextPageTags.TRUNCATED)
                .assertTextEquals(resources.getString(DemoR.string.demo_sample_long_setting))
        }
    }

    @Test
    fun badgeIsStaticText() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Badge)
            composeRule.scrollTo(BadgePageTags.PLAYGROUND_BADGE)
            val plain = composeRule.width(BadgePageTags.PLAYGROUND_BADGE)
            composeRule.onNodeWithTag(BadgePageTags.PLAYGROUND_BADGE)
                .assert(hasAnyDescendant(hasText(resources.getString(DemoR.string.demo_sample_draft))))
                .assert(!hasClickAction())

            composeRule.tap(BadgePageTags.PLAYGROUND_DOT)
            composeRule.tap(BadgePageTags.PLAYGROUND_ICON)
            composeRule.scrollTo(BadgePageTags.PLAYGROUND_BADGE)

            composeRule.onNodeWithTag(BadgePageTags.PLAYGROUND_BADGE)
                .assert(hasAnyDescendant(hasText(resources.getString(DemoR.string.demo_sample_draft))))
                .assert(!hasClickAction())
            assertTrue(composeRule.width(BadgePageTags.PLAYGROUND_BADGE) > plain)
        }
    }

    @Test
    fun avatarIsStaticAndSurvivesAPhoto() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Avatar)
            composeRule.scrollTo(AvatarPageTags.PLAYGROUND_AVATAR)
            composeRule.onNodeWithTag(AvatarPageTags.PLAYGROUND_AVATAR)
                .assertExists()
                .assert(!hasClickAction())

            composeRule.tap(AvatarPageTags.PLAYGROUND_PHOTO)
            composeRule.scrollTo(AvatarPageTags.PLAYGROUND_AVATAR)

            composeRule.onNodeWithTag(AvatarPageTags.PLAYGROUND_AVATAR).assertExists()
        }
    }

    private fun ComposeTestRule.assertCount(tag: String, count: Int) {
        scrollTo(tag)
        onNodeWithTag(tag).assertTextEquals(resources.getQuantityString(DemoR.plurals.demo_component_tap_count, count, count))
    }

    private fun ComposeTestRule.toggleOf(tag: String) =
        onNode(isToggleable() and hasAnyAncestor(hasTestTag(tag)), useUnmergedTree = true)

    private fun ComposeTestRule.tapSwitchOf(tag: String) {
        scrollTo(tag)
        onNodeWithTag(tag).performTouchInput { click(Offset(width - (16 + 22).dp.toPx(), centerY)) }
        waitForIdle()
    }

    private fun ComposeTestRule.tapTitleOf(tag: String) {
        scrollTo(tag)
        onNodeWithTag(tag).performTouchInput { click(Offset(width * 0.3f, centerY)) }
        waitForIdle()
    }

    @Test
    fun listItemRowsPressOnlyWithAnAction() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.ListItem)
            composeRule.scrollTo(ListItemPageTags.STATE_DEFAULT)
            composeRule.onNodeWithTag(ListItemPageTags.STATE_DEFAULT).assertIsEnabled()

            composeRule.tap(ListItemPageTags.STATE_DEFAULT)
            composeRule.assertCount(ListItemPageTags.STATE_COUNT, 1)

            composeRule.scrollTo(ListItemPageTags.STATE_DISABLED)
            composeRule.onNodeWithTag(ListItemPageTags.STATE_DISABLED).assertIsNotEnabled()
            composeRule.tap(ListItemPageTags.STATE_DISABLED)
            composeRule.scrollTo(ListItemPageTags.STATE_STATIC)
            composeRule.onNodeWithTag(ListItemPageTags.STATE_STATIC).assert(!hasClickAction())
            composeRule.tap(ListItemPageTags.STATE_STATIC)

            composeRule.assertCount(ListItemPageTags.STATE_COUNT, 1)
        }
    }

    @Test
    fun listItemSwitchRowTogglesFromTitleAndSwitch() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.ListItem)
            composeRule.scrollTo(ListItemPageTags.WIFI)
            composeRule.toggleOf(ListItemPageTags.WIFI).assertIsOn()

            composeRule.tapTitleOf(ListItemPageTags.WIFI)
            composeRule.toggleOf(ListItemPageTags.WIFI).assertIsOn()

            composeRule.tapSwitchOf(ListItemPageTags.WIFI)
            composeRule.toggleOf(ListItemPageTags.WIFI).assertIsOff()
            composeRule.scrollTo(ListItemPageTags.WIFI_VALUE)
            composeRule.onNodeWithTag(ListItemPageTags.WIFI_VALUE).assertTextEquals(resources.getString(DemoR.string.demo_state_off))
        }
    }

    @Test
    fun listItemPlaygroundTapCountsAndSwitchTrailing() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.ListItem)
            composeRule.tap(ListItemPageTags.PLAYGROUND_ROW)
            composeRule.tap(ListItemPageTags.PLAYGROUND_ROW)
            composeRule.assertCount(ListItemPageTags.PLAYGROUND_COUNT, 2)

            composeRule.tap(ListItemPageTags.trailing(ListItemTrailingOption.Switch))
            composeRule.scrollTo(ListItemPageTags.PLAYGROUND_ROW)
            composeRule.toggleOf(ListItemPageTags.PLAYGROUND_ROW).assertIsOff()
            composeRule.tapSwitchOf(ListItemPageTags.PLAYGROUND_ROW)
            composeRule.toggleOf(ListItemPageTags.PLAYGROUND_ROW).assertIsOn()
            composeRule.scrollTo(ListItemPageTags.PLAYGROUND_VALUE)
            composeRule.onNodeWithTag(ListItemPageTags.PLAYGROUND_VALUE).assertTextEquals(resources.getString(DemoR.string.demo_state_on))

            composeRule.tap(ListItemPageTags.PLAYGROUND_DISABLED)
            composeRule.tapSwitchOf(ListItemPageTags.PLAYGROUND_ROW)
            composeRule.toggleOf(ListItemPageTags.PLAYGROUND_ROW).assertIsNotEnabled().assertIsOn()
            composeRule.assertCount(ListItemPageTags.PLAYGROUND_COUNT, 2)
        }
    }

    private fun ComposeTestRule.tapAt(tag: String, x: (Float) -> Float, y: (Float) -> Float) {
        scrollTo(tag)
        onNodeWithTag(tag).performTouchInput { click(Offset(x(width.toFloat()), y(height.toFloat()))) }
        waitForIdle()
    }

    @Test
    fun interactiveCardPresses() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Card)
            composeRule.scrollTo(CardPageTags.STATE_DEFAULT)
            composeRule.onNodeWithTag(CardPageTags.STATE_DEFAULT).assertIsEnabled()

            composeRule.tapAt(CardPageTags.STATE_DEFAULT, { it * 0.3f }, { 40.dp.value * resources.displayMetrics.density })
            composeRule.assertCount(CardPageTags.STATE_COUNT, 1)

            composeRule.scrollTo(CardPageTags.STATE_DISABLED)
            composeRule.onNodeWithTag(CardPageTags.STATE_DISABLED).assertIsNotEnabled()
            composeRule.tap(CardPageTags.STATE_DISABLED)
            composeRule.assertCount(CardPageTags.STATE_COUNT, 1)
        }
    }

    @Test
    fun cardFooterButtonsStayTappable() {
        val density = resources.displayMetrics.density
        launchDemo().use {
            composeRule.openPage(DemoPageId.Card)
            composeRule.tapAt(CardPageTags.STATE_DEFAULT, { it - 40 * density }, { it - 36 * density })

            composeRule.scrollTo(CardPageTags.STATE_LAST_ACTION)
            composeRule.onNodeWithTag(CardPageTags.STATE_LAST_ACTION)
                .assertTextEquals(resources.getString(DemoR.string.demo_component_last_action, resources.getString(DemoR.string.demo_sample_open)))
            composeRule.assertCount(CardPageTags.STATE_COUNT, 0)
        }
    }
}
