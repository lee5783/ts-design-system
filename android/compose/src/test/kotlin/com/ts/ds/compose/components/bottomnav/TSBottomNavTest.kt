package com.ts.ds.compose.components.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.components.shared.LocalTSReduceMotion
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBottomNavItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSBottomNavTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val five = listOf(
        TSBottomNavItem("home", "Home", TSIcon.Home),
        TSBottomNavItem("inbox", "Inbox", TSIcon.Mail, badge = 3),
        TSBottomNavItem("search", "Search", TSIcon.Search),
        TSBottomNavItem("alerts", "Alerts", TSIcon.Bell, badge = 120),
        TSBottomNavItem("profile", "Profile", TSIcon.User),
    )

    private val changes = mutableListOf<String>()
    private var setItems: (List<TSBottomNavItem>) -> Unit = {}

    private fun setNav(
        items: List<TSBottomNavItem> = five,
        initial: String = items.first().id,
        enabled: Boolean = true,
        fontScale: Float = 1f,
        direction: LayoutDirection = LayoutDirection.Ltr,
        insets: WindowInsets = WindowInsets(0, 0, 0, 0),
        width: Dp = 320.dp,
    ) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalLayoutDirection provides direction,
                LocalTSReduceMotion provides true,
            ) {
                TSTheme(darkTheme = false) {
                    var current by remember { mutableStateOf(items) }
                    var selected by remember { mutableStateOf(initial) }
                    setItems = { current = it }
                    Box(Modifier.width(width)) {
                        TSBottomNav(
                            items = current,
                            selectedId = selected,
                            onSelect = {
                                changes += it
                                selected = it
                            },
                            enabled = enabled,
                            windowInsets = insets,
                            modifier = Modifier.testTag("nav"),
                        )
                    }
                }
            }
        }
    }

    private fun item(id: String) = composeRule.onNodeWithTag(TSBottomNavTestTags.item(id))

    private fun bounds(tag: String): DpRect {
        val node = composeRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
        return with(composeRule.density) {
            val position = node.positionInRoot
            DpRect(
                left = position.x.toDp(),
                top = position.y.toDp(),
                right = (position.x + node.size.width).toDp(),
                bottom = (position.y + node.size.height).toDp(),
            )
        }
    }

    @Test
    fun tapSelectsTheItem() {
        setNav()

        item("search").assertIsNotSelected().performClick()

        item("search").assertIsSelected()
        item("home").assertIsNotSelected()
        assertEquals(listOf("search"), changes)
    }

    @Test
    fun reselectingTheSelectedItemIsANoOp() {
        setNav()

        item("home").performClick()

        item("home").assertIsSelected()
        assertTrue(changes.isEmpty())
    }

    @Test
    fun disabledBlocksSelection() {
        setNav(enabled = false)

        item("inbox").performClick()

        item("home").assertIsSelected()
        assertTrue(changes.isEmpty())
    }

    @Test
    fun badgesShowFormattedCounts() {
        setNav()

        composeRule.onNodeWithTag(TSBottomNavTestTags.badge("inbox"), useUnmergedTree = true).onChild().assertTextEquals("3")
        composeRule.onNodeWithTag(TSBottomNavTestTags.badge("alerts"), useUnmergedTree = true).onChild().assertTextEquals("99+")
        composeRule.onNodeWithTag(TSBottomNavTestTags.badge("home"), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun badgeUpdatesWithTheItems() {
        setNav()

        setItems(five.map { if (it.id == "inbox") it.badged(null) else it })

        composeRule.onNodeWithTag(TSBottomNavTestTags.badge("inbox"), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun itemsShareTheWidthAndTheBarIs64WithA56By28Pill() {
        setNav()

        composeRule.onNodeWithTag("nav").assertHeightIsEqualTo(64.dp)
        five.forEach { item(it.id).assertWidthIsEqualTo(64.dp).assertHeightIsEqualTo(64.dp) }
        composeRule.onNodeWithTag(TSBottomNavTestTags.pill("home"), useUnmergedTree = true)
            .assertWidthIsEqualTo(56.dp)
            .assertHeightIsEqualTo(28.dp)
    }

    @Test
    fun columnIsCentredWithThePillNineFromTheTopAtTheDefaultSize() {
        setNav()

        val pill = bounds(TSBottomNavTestTags.pill("home"))
        assertEquals(9f, pill.top.value, 0.5f)
    }

    @Test
    fun navigationBarInsetIsAddedBelowThe64Band() {
        setNav(insets = WindowInsets(left = 0.dp, top = 0.dp, right = 0.dp, bottom = 24.dp))

        composeRule.onNodeWithTag("nav").assertHeightIsEqualTo(88.dp)
        item("home").assertHeightIsEqualTo(64.dp)
        assertEquals(0f, composeRule.onNodeWithTag(TSBottomNavTestTags.item("home")).getBoundsInRoot().top.value, 0.5f)
    }

    @Test
    fun labelScaleIsClampedAtFontScaleTwoSoTheBarStays64() {
        setNav(fontScale = 2f)

        composeRule.onNodeWithTag("nav").assertHeightIsEqualTo(64.dp)
        five.forEach { item(it.id).assertHeightIsEqualTo(64.dp) }
        val label = bounds(TSBottomNavTestTags.label("home")).let { it.bottom - it.top }
        assertEquals(16f * 1.3f, label.value, 1f)
        val badge = bounds(TSBottomNavTestTags.badge("inbox"))
        assertEquals(16f, (badge.bottom - badge.top).value, 0.5f)
    }

    @Test
    fun badgeSitsThreeAboveThePillAndSixPastTheCentre() {
        setNav()

        val itemBounds = composeRule.onNodeWithTag(TSBottomNavTestTags.item("inbox")).getBoundsInRoot()
        val pill = bounds(TSBottomNavTestTags.pill("inbox"))
        val badge = bounds(TSBottomNavTestTags.badge("inbox"))
        val centre = (itemBounds.left + itemBounds.right) / 2
        assertEquals((pill.top - 3.dp).value, badge.top.value, 0.5f)
        assertEquals((centre + 6.dp).value, badge.left.value, 0.5f)
    }

    @Test
    fun badgeMirrorsInRtl() {
        setNav(direction = LayoutDirection.Rtl)

        val itemBounds = composeRule.onNodeWithTag(TSBottomNavTestTags.item("inbox")).getBoundsInRoot()
        val badge = bounds(TSBottomNavTestTags.badge("inbox"))
        val centre = (itemBounds.left + itemBounds.right) / 2
        assertEquals((centre - 6.dp).value, badge.right.value, 0.5f)
        assertFalse(badge.left > centre)
    }
}
