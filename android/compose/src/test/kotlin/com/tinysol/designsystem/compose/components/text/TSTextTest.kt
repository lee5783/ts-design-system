package com.tinysol.designsystem.compose.components.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.compose.theme.lightTSColors
import com.tinysol.designsystem.core.style.TSTextColor
import com.tinysol.designsystem.core.style.TSTypeRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun layout(tag: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        composeRule.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single()
    }

    @Test
    fun everyRoleHasItsLineHeight() {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Column {
                    TSTypeRole.entries.forEach { TSText("Text", Modifier.testTag(it.name), role = it) }
                }
            }
        }
        TSTypeRole.entries.forEach { role ->
            composeRule.onNodeWithTag(role.name).assertHeightIsEqualTo(role.values.lineHeight.dp)
        }
    }

    @Test
    fun colorComesFromTheTextRole() {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Column {
                    TSTextColor.entries.forEach { TSText("Text", Modifier.testTag(it.name), color = it) }
                }
            }
        }
        val colors = lightTSColors()
        TSTextColor.entries.forEach { color ->
            assertEquals(colors.color(color.role), layout(color.name).layoutInput.style.color)
        }
    }

    @Test
    fun defaultsAreBodyPrimaryAndOverlineIsUppercased() {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Column {
                    TSText("Body", Modifier.testTag("body"))
                    TSText("Section", Modifier.testTag("overline"), role = TSTypeRole.Overline)
                }
            }
        }
        val body = layout("body").layoutInput.style
        assertEquals(TSTypeRole.Body.values.fontSize, body.fontSize.value, 0f)
        assertEquals(lightTSColors().textPrimary, body.color)
        composeRule.onNodeWithTag("overline").assertTextEquals("SECTION")
    }

    @Test
    fun maxLinesAndOverflowTruncate() {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(Modifier.testTag("box")) {
                    TSText(
                        "A long line of text that cannot fit in a narrow column and has to be cut",
                        Modifier.testTag("text").then(Modifier.width(120.dp)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        val result = layout("text")
        assertEquals(1, result.lineCount)
        assertTrue(result.isLineEllipsized(0))
    }
}
