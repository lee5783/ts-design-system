package com.tinysol.designsystem.compose.sample

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tinysol.designsystem.compose.sample.components.ButtonPageTags
import com.tinysol.designsystem.compose.sample.components.IconButtonPageTags
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActionComponentsTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun tapCount(count: Int): String = resources.getQuantityString(DemoR.plurals.demo_component_tap_count, count, count)

    private fun ComposeTestRule.tap(tag: String) {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
        onNodeWithTag(tag).performClick()
        waitForIdle()
    }

    private fun ComposeTestRule.assertCount(tag: String, count: Int) {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
        onNodeWithTag(tag).assertTextEquals(tapCount(count))
    }

    @Test
    fun buttonPlaygroundFiresAndLoadingAndDisabledBlock() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Button)
            composeRule.tap(ButtonPageTags.PLAYGROUND_BUTTON)
            composeRule.assertCount(ButtonPageTags.PLAYGROUND_COUNT, 1)

            composeRule.tap(ButtonPageTags.PLAYGROUND_LOADING)
            composeRule.onNodeWithTag(ButtonPageTags.PLAYGROUND_BUTTON).assertIsNotEnabled()
            composeRule.tap(ButtonPageTags.PLAYGROUND_BUTTON)
            composeRule.assertCount(ButtonPageTags.PLAYGROUND_COUNT, 1)
            composeRule.tap(ButtonPageTags.PLAYGROUND_LOADING)

            composeRule.tap(ButtonPageTags.PLAYGROUND_DISABLED)
            composeRule.tap(ButtonPageTags.PLAYGROUND_BUTTON)
            composeRule.assertCount(ButtonPageTags.PLAYGROUND_COUNT, 1)
            composeRule.tap(ButtonPageTags.PLAYGROUND_DISABLED)

            composeRule.tap(ButtonPageTags.PLAYGROUND_BUTTON)
            composeRule.assertCount(ButtonPageTags.PLAYGROUND_COUNT, 2)
        }
    }

    @Test
    fun linkButtonHasA48dpTouchTarget() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.Button)
            composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(ButtonPageTags.LINK))
            composeRule.onNodeWithTag(ButtonPageTags.LINK).assertTouchHeightIsEqualTo(48.dp)
        }
    }

    @Test
    fun iconButtonPlaygroundFiresAndDisabledBlocks() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.IconButton)
            composeRule.tap(IconButtonPageTags.PLAYGROUND_BUTTON)
            composeRule.assertCount(IconButtonPageTags.PLAYGROUND_COUNT, 1)

            composeRule.tap(IconButtonPageTags.PLAYGROUND_DISABLED)
            composeRule.onNodeWithTag(IconButtonPageTags.PLAYGROUND_BUTTON).assertIsNotEnabled()
            composeRule.tap(IconButtonPageTags.PLAYGROUND_BUTTON)
            composeRule.assertCount(IconButtonPageTags.PLAYGROUND_COUNT, 1)
        }
    }
}
