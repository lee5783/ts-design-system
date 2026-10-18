package com.ts.ds.compose.components.iconbutton

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.components.icon.TSIconSource
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSIconButtonVariant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSIconButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var taps = 0

    private fun setIconButton(enabled: Boolean = true, round: Boolean = false) {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(Modifier.testTag("container")) {
                    TSIconButton(
                        icon = TSIcon.Search,
                        onClick = { taps++ },
                        variant = TSIconButtonVariant.Secondary,
                        round = round,
                        enabled = enabled,
                        modifier = Modifier.testTag("button"),
                    )
                }
            }
        }
    }

    @Test
    fun clickInvokesTheAction() {
        setIconButton()

        composeRule.onNodeWithTag("button").performClick()

        assertEquals(1, taps)
    }

    @Test
    fun disabledBlocksTheAction() {
        setIconButton(enabled = false)

        composeRule.onNodeWithTag("button").assertIsNotEnabled().performClick()

        assertEquals(0, taps)
    }

    @Test
    fun is44dpVisuallyWithA48dpTarget() {
        setIconButton(round = true)

        composeRule.onNodeWithTag("container").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag("button").assertTouchWidthIsEqualTo(48.dp).assertTouchHeightIsEqualTo(48.dp)
    }

    @Test
    fun consumerIconKeepsTheSize() {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(Modifier.testTag("container")) {
                    TSIconButton(
                        icon = TSIconSource.Painter(ColorPainter(Color.Red)),
                        onClick = { taps++ },
                        variant = TSIconButtonVariant.Secondary,
                        modifier = Modifier.testTag("button"),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("container").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag("button").performClick()
        assertEquals(1, taps)
    }
}
