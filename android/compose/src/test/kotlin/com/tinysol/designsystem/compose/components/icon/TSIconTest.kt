package com.tinysol.designsystem.compose.components.icon

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.style.TSIconSize
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSIconTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sizesFollowTheIconTokens() {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                TSIconSize.entries.forEach { size ->
                    TSIcon(TSIcon.Home, size = size, modifier = Modifier.testTag(size.name))
                }
            }
        }

        listOf(TSIconSize.Sm to 16.dp, TSIconSize.Md to 20.dp, TSIconSize.Lg to 24.dp).forEach { (size, dp) ->
            composeRule.onNodeWithTag(size.name, useUnmergedTree = true).assertWidthIsEqualTo(dp).assertHeightIsEqualTo(dp)
        }
    }
}
