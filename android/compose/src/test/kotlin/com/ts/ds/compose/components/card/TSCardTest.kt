package com.ts.ds.compose.components.card

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.components.badge.TSBadge
import com.ts.ds.compose.components.iconbutton.TSIconButton
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.lightTSColors
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSCardAccessory
import com.ts.ds.core.logic.TSCardAction
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSCardStyleSpec
import com.ts.ds.core.style.TSControlState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<String>()
    private lateinit var hostView: View

    private val footer = listOf(
        TSCardAction("Share") { events += "share" },
        TSCardAction("Open") { events += "open" },
    )

    private fun setContent(fontScale: Float = 1f, forced: TSControlState? = null, content: @Composable () -> Unit) {
        composeRule.setContent {
            hostView = LocalView.current
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalTSForcedControlState provides forced,
            ) {
                TSTheme(darkTheme = false) {
                    Box(Modifier.width(320.dp)) { content() }
                }
            }
        }
    }

    @Composable
    private fun InteractiveCard(enabled: Boolean = true, footerActions: List<TSCardAction> = footer) {
        TSCard(
            text = "Billed monthly",
            title = "Plan",
            subtitle = "Pro",
            footerActions = footerActions,
            onClick = { events += "card" },
            enabled = enabled,
            modifier = Modifier.testTag("card"),
        )
    }

    private fun pixelAt(tag: String, x: Dp, y: Dp): Color {
        val bounds = composeRule.onNodeWithTag(tag).getBoundsInRoot()
        val density = hostView.resources.displayMetrics.density
        val bitmap = hostView.drawToBitmap()
        return Color(bitmap.getPixel(((bounds.left + x).value * density).toInt(), ((bounds.top + y).value * density).toInt()))
    }

    private fun tapInside(tag: String, inner: String) {
        val card = composeRule.onNodeWithTag(tag).getBoundsInRoot()
        val target = composeRule.onNodeWithText(inner, useUnmergedTree = true).getBoundsInRoot()
        composeRule.onNodeWithTag(tag).performTouchInput {
            click(Offset(((target.left + target.right) / 2f - card.left).toPx(), ((target.top + target.bottom) / 2f - card.top).toPx()))
        }
    }

    @Test
    fun tapPerformsTheAction() {
        setContent { InteractiveCard() }

        composeRule.onNodeWithTag("card").performClick()

        assertEquals(listOf("card"), events)
    }

    @Test
    fun footerButtonsStayTappableWithoutTriggeringTheCard() {
        setContent { InteractiveCard() }

        tapInside("card", "Open")
        tapInside("card", "Share")

        assertEquals(listOf("open", "share"), events)
    }

    @Test
    fun accessoryStaysTappable() {
        setContent {
            TSCard(
                text = "Body",
                title = "Plan",
                onClick = { events += "card" },
                modifier = Modifier.testTag("card"),
                accessory = { TSIconButton(TSIcon.More, onClick = { events += "accessory" }) },
            )
        }
        val card = composeRule.onNodeWithTag("card").getBoundsInRoot()
        val accessory = composeRule.onNodeWithTag(TSCardTestTags.ACCESSORY, useUnmergedTree = true).getBoundsInRoot()

        composeRule.onNodeWithTag("card").performTouchInput {
            click(Offset(((accessory.left + accessory.right) / 2f - card.left).toPx(), ((accessory.top + accessory.bottom) / 2f - card.top).toPx()))
        }
        composeRule.onNodeWithTag("card").performTouchInput { click(Offset(24.dp.toPx(), centerY)) }

        assertEquals(listOf("accessory", "card"), events)
    }

    @Test
    fun staticCardHasNoClickAction() {
        setContent {
            TSCard(text = "Billed monthly", title = "Plan", subtitle = "Pro", footerActions = footer, modifier = Modifier.testTag("card")) {
            }
        }

        val node = composeRule.onNodeWithTag("card").fetchSemanticsNode()
        assertNull(node.config.getOrNull(SemanticsActions.OnClick))
        composeRule.onNodeWithText("Open").performClick()
        assertEquals(listOf("open"), events)
    }

    @Test
    fun disabledCardBlocksTaps() {
        setContent { InteractiveCard(enabled = false) }

        composeRule.onNodeWithTag("card").assertIsNotEnabled().performClick()
        tapInside("card", "Open")

        assertEquals(emptyList<String>(), events)
    }

    @Test
    fun pressedFillOnlyOnAnInteractiveCard() {
        setContent(forced = TSControlState.Pressed) {
            Column {
                TSCard(title = "Interactive", onClick = {}, modifier = Modifier.testTag("interactive"))
                TSCard(title = "Static", modifier = Modifier.testTag("static"))
            }
        }

        assertEquals(lightTSColors().surfacePressed, pixelAt("interactive", 6.dp, 40.dp))
        assertEquals(lightTSColors().surfaceCard, pixelAt("static", 6.dp, 40.dp))
    }

    @Test
    fun heightsMatchTheDesignAtTheDefaultSize() {
        setContent {
            Column {
                TSCard(title = "Plan", modifier = Modifier.testTag("title"))
                TSCard(text = "Billed monthly", title = "Plan", modifier = Modifier.testTag("body"))
                TSCard(text = "Billed monthly", title = "Plan", footerActions = footer, modifier = Modifier.testTag("footer"))
            }
        }

        composeRule.onNodeWithTag("title").assertHeightIsEqualTo(56.dp)
        composeRule.onNodeWithTag("body").assertHeightIsEqualTo(92.dp)
        composeRule.onNodeWithTag("footer").assertHeightIsEqualTo(165.dp)
    }

    @Test
    fun footerStacksAtLargeFontSizes() {
        setContent(fontScale = 2f) { InteractiveCard() }

        val share = composeRule.onNodeWithText("Share", useUnmergedTree = true).getBoundsInRoot()
        val open = composeRule.onNodeWithText("Open", useUnmergedTree = true).getBoundsInRoot()
        assertTrue(open.top > share.bottom)
    }

    @Test
    fun badgeAccessoryRendersInTheHeader() {
        setContent {
            TSCard(text = "Body", title = "Plan", modifier = Modifier.testTag("card"), accessory = { TSBadge("New") })
        }

        val card = composeRule.onNodeWithTag("card").getBoundsInRoot()
        val badge = composeRule.onNodeWithTag(TSCardTestTags.ACCESSORY, useUnmergedTree = true).getBoundsInRoot()
        assertEquals(card.right - 16.dp, badge.right)
        assertEquals(card.top + 16.dp, badge.top)
    }

    @Test
    fun typedAccessoryRendersAndFires() {
        setContent {
            TSCard(
                text = "Billed monthly",
                accessory = TSCardAccessory.IconButton(TSIcon.More, "Card options") { events += "accessory" },
                title = "Plan",
                footerActions = footer,
                onClick = { events += "card" },
                modifier = Modifier.testTag("card"),
            )
        }

        val card = composeRule.onNodeWithTag("card").getBoundsInRoot()
        val accessory = composeRule.onNodeWithTag(TSCardTestTags.ACCESSORY, useUnmergedTree = true).getBoundsInRoot()
        composeRule.onNodeWithTag("card").performTouchInput {
            click(Offset(((accessory.left + accessory.right) / 2f - card.left).toPx(), ((accessory.top + accessory.bottom) / 2f - card.top).toPx()))
        }

        assertEquals(listOf("accessory"), events)
    }

    @Test
    fun disabledCardDimsItsFooterButtonsOnce() {
        val primary = listOf(TSCardAction("Open", TSButtonVariant.Primary) {})
        val opacity = TSCardStyleSpec.resolve(TSCardStyleSpec.Configuration(isInteractive = true), TSControlState.Disabled).opacity
        setContent {
            Column {
                TSCard(title = "Plan", footerActions = primary, onClick = {}, enabled = false, modifier = Modifier.testTag("disabled"))
                Box(Modifier.alpha(opacity)) {
                    TSCard(title = "Plan", footerActions = primary, onClick = {}, modifier = Modifier.testTag("dimmedOnce"))
                }
            }
        }

        val card = composeRule.onNodeWithTag("disabled").getBoundsInRoot()
        val label = composeRule.onAllNodesWithText("Open", useUnmergedTree = true)[0].getBoundsInRoot()
        val x = label.left - card.left - 6.dp
        val y = (label.top + label.bottom) / 2f - card.top
        val disabled = pixelAt("disabled", x, y)
        assertEquals(pixelAt("dimmedOnce", x, y), disabled)
        assertTrue(disabled != pixelAt("disabled", 6.dp, 20.dp))
    }
}
