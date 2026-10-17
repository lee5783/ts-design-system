package com.ts.ds.compose.components.alert

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSAlertTone
import com.ts.ds.core.style.TSButtonSize
import com.ts.ds.core.style.TSButtonVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSAlertTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var dismissals = 0
    private var retries = 0

    private fun setContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(Modifier.width(320.dp)) { content() }
            }
        }
    }

    @Test
    fun messageIsOneElementPerTone() {
        val names = mapOf(
            TSAlertTone.Info to "Information",
            TSAlertTone.Success to "Success",
            TSAlertTone.Warning to "Warning",
            TSAlertTone.Danger to "Error",
        )
        setContent {
            Column {
                names.keys.forEach { tone ->
                    TSAlert("Free up space", tone = tone, title = "Storage almost full", modifier = Modifier.testTag(tone.name))
                }
            }
        }

        names.forEach { (tone, name) ->
            val message = composeRule.onAllNodesWithTag(TSAlertTestTags.MESSAGE)[tone.ordinal]
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("$name, Storage almost full, Free up space")))
                .fetchSemanticsNode()
            assertNull(message.config.getOrNull(SemanticsProperties.Text))
        }
    }

    @Test
    fun dismissFiresFromTheButtonAndTheAccessibilityAction() {
        setContent { TSAlert("Saved", title = "Done", onDismiss = { dismissals++ }) }

        composeRule.onNodeWithContentDescription("Dismiss")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()
        composeRule.onNodeWithTag(TSAlertTestTags.MESSAGE).performSemanticsAction(SemanticsActions.Dismiss)

        assertEquals(2, dismissals)
    }

    @Test
    fun noDismissWithoutACallback() {
        setContent { TSAlert("Saved", title = "Done") }

        composeRule.onAllNodesWithTag(TSAlertTestTags.DISMISS, useUnmergedTree = true).assertCountEquals(0)
        assertNull(composeRule.onNodeWithTag(TSAlertTestTags.MESSAGE).fetchSemanticsNode().config.getOrNull(SemanticsActions.Dismiss))
    }

    @Test
    fun dismissHitAreaReachesBeyondTheGlyph() {
        setContent { TSAlert("Saved", onDismiss = { dismissals++ }, modifier = Modifier.testTag("alert")) }
        val alert = composeRule.onNodeWithTag("alert").getBoundsInRoot()
        val glyph = composeRule.onNodeWithTag(TSAlertTestTags.DISMISS, useUnmergedTree = true).getBoundsInRoot()

        composeRule.onNodeWithTag("alert").performTouchInput {
            click(Offset((glyph.right - alert.left + 10.dp).toPx(), ((glyph.top + glyph.bottom) / 2f - alert.top).toPx()))
        }

        assertEquals(1, dismissals)
        assertEquals(20.dp, glyph.right - glyph.left)
    }

    @Test
    fun actionStaysASeparateElement() {
        setContent {
            TSAlert("Upload failed", tone = TSAlertTone.Danger) {
                TSButton("Retry", onClick = { retries++ }, variant = TSButtonVariant.Ghost, size = TSButtonSize.Md)
            }
        }

        composeRule.onNodeWithTag(TSAlertTestTags.MESSAGE)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("Error, Upload failed")))
        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun liveRegionOnlyWhenShownDynamically() {
        setContent {
            Column {
                TSAlert("Static", modifier = Modifier.testTag("static"))
                TSAlert("Dynamic", announcesWhenShown = true, modifier = Modifier.testTag("dynamic"))
            }
        }

        val messages = composeRule.onAllNodesWithTag(TSAlertTestTags.MESSAGE, useUnmergedTree = true)
        assertNull(messages[0].fetchSemanticsNode().config.getOrNull(SemanticsProperties.LiveRegion))
        messages[1].assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test
    fun heightsMatchTheDesignAtTheDefaultSize() {
        setContent {
            Column {
                TSAlert("Saved", modifier = Modifier.testTag("one"), onDismiss = {})
                TSAlert("Saved", title = "Done", modifier = Modifier.testTag("two"))
            }
        }

        composeRule.onNodeWithTag("one").assertHeightIsEqualTo(46.dp)
        composeRule.onNodeWithTag("two").assertHeightIsEqualTo(66.dp)
    }

    @Test
    fun customIconReplacesTheToneIcon() {
        setContent { TSAlert("New feature", icon = TSIcon.Star) }

        composeRule.onNodeWithTag(TSAlertTestTags.ICON, useUnmergedTree = true).assertHeightIsEqualTo(20.dp)
    }
}
