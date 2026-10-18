package com.ts.ds.compose.components.badge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBadgeTone
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
class TSBadgeTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun setBadges(fontScale: Float = 1f, content: @Composable () -> Unit) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                TSTheme(darkTheme = false) { content() }
            }
        }
    }

    @Test
    fun badgeIsNotAButton() {
        setBadges { TSBadge("New", tone = TSBadgeTone.Accent, showsDot = true, icon = TSIcon.Star) }

        composeRule.onNodeWithText("New").assert(!hasClickAction())
        assertEquals(0, composeRule.onAllNodesCount(hasClickAction()))
    }

    @Test
    fun minimumHeightIs22AndGrowsWithLargeText() {
        setBadges { TSBadge("Default", modifier = Modifier.testTag("badge")) }
        composeRule.onNodeWithTag("badge").assertHeightIsEqualTo(22.dp)
    }

    @Test
    fun heightGrowsAt200PercentText() {
        setBadges(fontScale = 2f) { TSBadge("Large", modifier = Modifier.testTag("badge")) }
        composeRule.onNodeWithTag("badge").assertHeightIsAtLeast(30.dp)
    }

    @Test
    fun dotAndIconAreDecorativeAndAddWidth() {
        setBadges {
            Box {
                TSBadge("Plain", modifier = Modifier.testTag("plain"))
            }
            Box {
                TSBadge("Plain", modifier = Modifier.testTag("dot"), showsDot = true)
            }
            Box {
                TSBadge("Plain", modifier = Modifier.testTag("icon"), icon = TSIcon.Star)
            }
        }
        val plain = composeRule.onNodeWithTag("plain").getUnclippedBoundsInRoot().let { it.right - it.left }
        val dot = composeRule.onNodeWithTag("dot").getUnclippedBoundsInRoot().let { it.right - it.left }
        val icon = composeRule.onNodeWithTag("icon").getUnclippedBoundsInRoot().let { it.right - it.left }

        assertEquals((plain + 10.dp).value, dot.value, 0.5f)
        assertEquals((plain + 20.dp).value, icon.value, 0.5f)
    }

    @Test
    fun longTextTruncatesOnOneLineWithAnEllipsis() {
        val long = "Awaiting review from the platform accessibility team"
        setBadges {
            Box(Modifier.width(120.dp)) {
                TSBadge(long, modifier = Modifier.testTag("badge"), tone = TSBadgeTone.Warning)
            }
        }

        composeRule.onNodeWithTag("badge").assertWidthIsEqualTo(120.dp).assertHeightIsEqualTo(22.dp)
        val layouts = mutableListOf<TextLayoutResult>()
        composeRule.onNode(hasText(long), useUnmergedTree = true)
            .fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        assertEquals(1, layouts.single().lineCount)
        assertTrue(layouts.single().isLineEllipsized(0))
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesCount(
        matcher: SemanticsMatcher,
    ): Int = onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().size
}
