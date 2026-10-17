package com.ts.ds.demo.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.compose.components.AvatarPageTags
import com.ts.ds.demo.compose.components.BadgePageTags
import com.ts.ds.demo.compose.components.CardPageTags
import com.ts.ds.demo.compose.components.ListItemPageTags
import com.ts.ds.demo.compose.components.ListItemTrailingOption
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun badgeIsStaticTextLabelledByItsText() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Badge)
            composeRule.scrollTo(BadgePageTags.PLAYGROUND_BADGE)
            val plain = composeRule.width(BadgePageTags.PLAYGROUND_BADGE)
            composeRule.onNodeWithTag(BadgePageTags.PLAYGROUND_BADGE)
                .assertTextEquals(resources.getString(DemoR.string.demo_sample_draft))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
                .assert(!hasClickAction())

            composeRule.tap(BadgePageTags.PLAYGROUND_DOT)
            composeRule.tap(BadgePageTags.PLAYGROUND_ICON)
            composeRule.scrollTo(BadgePageTags.PLAYGROUND_BADGE)

            composeRule.onNodeWithTag(BadgePageTags.PLAYGROUND_BADGE)
                .assertTextEquals(resources.getString(DemoR.string.demo_sample_draft))
                .assert(!hasClickAction())
            assertTrue(composeRule.width(BadgePageTags.PLAYGROUND_BADGE) > plain)
        }
    }

    @Test
    fun avatarIsAnImageLabelledByTheName() {
        val name = SampleData.people.first().name
        launchDemo().use {
            composeRule.openPage(DemoPageId.Avatar)
            composeRule.scrollTo(AvatarPageTags.PLAYGROUND_AVATAR)
            composeRule.onNodeWithTag(AvatarPageTags.PLAYGROUND_AVATAR)
                .assertContentDescriptionEquals(name)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
                .assert(!hasClickAction())

            composeRule.tap(AvatarPageTags.PLAYGROUND_PHOTO)
            composeRule.scrollTo(AvatarPageTags.PLAYGROUND_AVATAR)

            composeRule.onNodeWithTag(AvatarPageTags.PLAYGROUND_AVATAR)
                .assertContentDescriptionEquals(name)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
        }
    }

    private fun ComposeTestRule.assertCount(tag: String, count: Int) {
        scrollTo(tag)
        onNodeWithTag(tag).assertTextEquals(resources.getQuantityString(DemoR.plurals.demo_component_tap_count, count, count))
    }

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
            composeRule.onNodeWithTag(ListItemPageTags.STATE_DEFAULT)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assertIsEnabled()

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
    fun listItemSwitchRowExposesSwitchSemantics() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.ListItem)
            composeRule.scrollTo(ListItemPageTags.WIFI)
            composeRule.onNodeWithTag(ListItemPageTags.WIFI)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, resources.getString(DemoR.string.demo_state_on)))
                .assertContentDescriptionEquals(resources.getString(DemoR.string.demo_sample_wifi))
                .assertIsOn()

            composeRule.tapTitleOf(ListItemPageTags.WIFI)
            composeRule.onNodeWithTag(ListItemPageTags.WIFI).assertIsOn()

            composeRule.tapSwitchOf(ListItemPageTags.WIFI)
            composeRule.onNodeWithTag(ListItemPageTags.WIFI).assertIsOff()
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
            composeRule.onNodeWithTag(ListItemPageTags.PLAYGROUND_ROW)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
                .assertIsOff()
            composeRule.tapSwitchOf(ListItemPageTags.PLAYGROUND_ROW)
            composeRule.onNodeWithTag(ListItemPageTags.PLAYGROUND_ROW).assertIsOn()
            composeRule.scrollTo(ListItemPageTags.PLAYGROUND_VALUE)
            composeRule.onNodeWithTag(ListItemPageTags.PLAYGROUND_VALUE).assertTextEquals(resources.getString(DemoR.string.demo_state_on))

            composeRule.tap(ListItemPageTags.PLAYGROUND_DISABLED)
            composeRule.tapSwitchOf(ListItemPageTags.PLAYGROUND_ROW)
            composeRule.onNodeWithTag(ListItemPageTags.PLAYGROUND_ROW).assertIsNotEnabled().assertIsOn()
            composeRule.assertCount(ListItemPageTags.PLAYGROUND_COUNT, 2)
        }
    }

    private fun ComposeTestRule.tapAt(tag: String, x: (Float) -> Float, y: (Float) -> Float) {
        scrollTo(tag)
        onNodeWithTag(tag).performTouchInput { click(Offset(x(width.toFloat()), y(height.toFloat()))) }
        waitForIdle()
    }

    @Test
    fun interactiveCardIsOneElementAndPresses() {
        val share = resources.getString(DemoR.string.demo_sample_share)
        val open = resources.getString(DemoR.string.demo_sample_open)
        launchDemo().use {
            composeRule.openPage(DemoPageId.Card)
            composeRule.scrollTo(CardPageTags.STATE_DEFAULT)
            val node = composeRule.onNodeWithTag(CardPageTags.STATE_DEFAULT)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assertContentDescriptionEquals(
                    "${resources.getString(DemoR.string.demo_component_default)}, ${resources.getString(DemoR.string.demo_component_interactive)}",
                )
                .assertIsEnabled()
                .fetchSemanticsNode()
            assertEquals(listOf(share, open), node.config[SemanticsActions.CustomActions].map { it.label })

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
