package com.ts.ds.compose.components.tabs

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.components.shared.LocalTSReduceMotion
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.haptics.DsHaptics
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.core.style.TSTabsVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSTabsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ranges = listOf("day" to "Day", "week" to "Week", "month" to "Month", "year" to "Year").map { TSTabItem(it.first, it.second) }
    private val sections = listOf(
        "Overview", "Activity", "Members", "Billing", "Settings", "Integrations", "Security", "Notifications", "Reports",
    ).map { TSTabItem(it.lowercase(), it) }

    private val changes = mutableListOf<String>()
    private lateinit var hostView: View
    private var select: (String) -> Unit = {}

    private fun setTabs(
        items: List<TSTabItem> = ranges,
        initial: String = items.first().id,
        variant: TSTabsVariant = TSTabsVariant.Underline,
        block: Boolean = false,
        enabled: Boolean = true,
        width: Dp = 320.dp,
        direction: LayoutDirection = LayoutDirection.Ltr,
        reduceMotion: Boolean = false,
    ) {
        composeRule.setContent {
            hostView = LocalView.current
            CompositionLocalProvider(LocalLayoutDirection provides direction, LocalTSReduceMotion provides reduceMotion) {
                TSTheme(darkTheme = false) {
                    var selected by remember { mutableStateOf(initial) }
                    select = { selected = it }
                    Box(Modifier.width(width).testTag("host")) {
                        TSTabs(
                            items = items,
                            selectedId = selected,
                            onSelect = {
                                changes += it
                                selected = it
                            },
                            variant = variant,
                            block = block,
                            label = "Time range",
                            enabled = enabled,
                            modifier = Modifier.testTag("tabs"),
                        )
                    }
                }
            }
        }
    }

    private fun tab(id: String) = composeRule.onNodeWithTag(TSTabsTestTags.tab(id))

    private fun leftOf(tag: String): Float = composeRule.onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot().left.value

    @Test
    fun tapSelectsTheTabAndExposesTabSemantics() {
        setTabs()

        tab("week")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "2 of 4"))
            .assertIsNotSelected()
            .performClick()

        tab("week").assertIsSelected()
        tab("day").assertIsNotSelected().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "1 of 4"))
        assertEquals(listOf("week"), changes)
    }

    @Test
    fun theListIsASelectableGroupWithCollectionInfo() {
        setTabs()

        composeRule.onNodeWithTag("tabs")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("Time range")))
            .assert(
                SemanticsMatcher("collection of 1 x 4") {
                    val info = it.config[SemanticsProperties.CollectionInfo]
                    info.rowCount == 1 && info.columnCount == 4
                },
            )
        tab("month").assert(
            SemanticsMatcher("third column") {
                val info = it.config[SemanticsProperties.CollectionItemInfo]
                info.rowIndex == 0 && info.columnIndex == 2
            },
        )
    }

    @Test
    fun tappingTheSelectedTabIsANoOp() {
        setTabs()

        tab("day").performClick()

        assertEquals(emptyList<String>(), changes)
    }

    @Test
    fun disabledBlocksSelection() {
        setTabs(enabled = false)

        tab("week").assertIsNotEnabled().performClick()

        tab("day").assertIsSelected()
        assertEquals(emptyList<String>(), changes)
    }

    @Test
    fun segmentChangePlaysTheSelectionHaptic() {
        setTabs(variant = TSTabsVariant.Segmented)

        tab("week").performClick()

        assertEquals(DsHaptics.constantFor(DsHapticEvent.Selection), shadowOf(hostView).lastHapticFeedbackPerformed())
    }

    @Test
    fun underlineChangeHasNoHaptic() {
        setTabs()

        tab("week").performClick()

        assertEquals(-1, shadowOf(hostView).lastHapticFeedbackPerformed())
    }

    @Test
    fun segmentedIsA46TrackWith38SegmentsInA48TouchBox() {
        setTabs(variant = TSTabsVariant.Segmented, initial = "week")

        composeRule.onNodeWithTag("tabs").assertHeightIsEqualTo(48.dp)
        tab("day").assertHeightIsEqualTo(48.dp).assertTouchHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag(TSTabsTestTags.THUMB, useUnmergedTree = true).assertHeightIsEqualTo(38.dp)
        assertEquals(leftOf(TSTabsTestTags.tab("week")), leftOf(TSTabsTestTags.THUMB), 0.5f)
        val tabsTop = composeRule.onNodeWithTag("tabs").getBoundsInRoot().top
        val thumbTop = composeRule.onNodeWithTag(TSTabsTestTags.THUMB, useUnmergedTree = true).getBoundsInRoot().top
        assertEquals(5f, (thumbTop - tabsTop).value, 0.5f)
    }

    @Test
    fun segmentedNeverScrolls() {
        setTabs(variant = TSTabsVariant.Segmented)

        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)).assertCountEquals(0)
    }

    @Test
    fun blockSegmentsShareTheWidth() {
        setTabs(variant = TSTabsVariant.Segmented, block = true, width = 248.dp)

        val widths = ranges.map { tab(it.id).getBoundsInRoot().let { bounds -> (bounds.right - bounds.left).value } }

        widths.forEach { assertEquals(60f, it, 0.5f) }
    }

    @Test
    fun underlineTabsAre48TallWithTheIndicatorUnderTheSelection() {
        setTabs(initial = "month")

        tab("day").assertHeightIsEqualTo(48.dp).assertTouchHeightIsEqualTo(48.dp)
        val tabWidth = tab("month").getBoundsInRoot().let { it.right - it.left }
        composeRule.onNodeWithTag(TSTabsTestTags.INDICATOR, useUnmergedTree = true).assertHeightIsEqualTo(2.dp).assertWidthIsEqualTo(tabWidth).assertIsDisplayed()
        assertEquals(leftOf(TSTabsTestTags.tab("month")), leftOf(TSTabsTestTags.INDICATOR), 0.5f)
        val tabBottom = tab("month").getBoundsInRoot().bottom
        val indicatorBottom = composeRule.onNodeWithTag(TSTabsTestTags.INDICATOR, useUnmergedTree = true).getBoundsInRoot().bottom
        assertEquals(2f, (tabBottom - indicatorBottom).value, 0.5f)
    }

    @Test
    fun underlineOverflowScrollsAndSelectionScrollsIntoView() {
        setTabs(items = sections, width = 300.dp)

        tab("reports").assertIsNotDisplayed()
        composeRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange))
            .assert(SemanticsMatcher("overflows") { it.config[SemanticsProperties.HorizontalScrollAxisRange].maxValue() > 0f })

        composeRule.runOnIdle { select("reports") }
        composeRule.waitForIdle()

        tab("reports").assertIsDisplayed()
        assertTrue(tab("reports").getBoundsInRoot().right <= 300.dp + 0.5.dp)
        assertEquals(leftOf(TSTabsTestTags.tab("reports")), leftOf(TSTabsTestTags.INDICATOR), 0.5f)
    }

    @Test
    fun shortUnderlineListAlignsToTheStartInRightToLeft() {
        setTabs(direction = LayoutDirection.Rtl)

        val host = composeRule.onNodeWithTag("host").getBoundsInRoot()
        assertEquals(host.right.value, tab("day").getBoundsInRoot().right.value, 0.5f)
    }

    @Test
    fun indicatorSlidesOver150Milliseconds() {
        setTabs()
        val start = leftOf(TSTabsTestTags.INDICATOR)
        val end = leftOf(TSTabsTestTags.tab("year"))
        composeRule.mainClock.autoAdvance = false

        tab("year").performClick()
        composeRule.mainClock.advanceTimeBy(80)
        val midway = leftOf(TSTabsTestTags.INDICATOR)
        assertTrue("indicator at $midway is between $start and $end", midway > start + 1f && midway < end - 1f)

        composeRule.mainClock.advanceTimeBy(200)
        assertEquals(end, leftOf(TSTabsTestTags.INDICATOR), 0.5f)
    }

    @Test
    fun reduceMotionMovesTheIndicatorImmediately() {
        setTabs(reduceMotion = true)
        composeRule.mainClock.autoAdvance = false

        tab("year").performClick()
        composeRule.mainClock.advanceTimeBy(48)

        assertEquals(leftOf(TSTabsTestTags.tab("year")), leftOf(TSTabsTestTags.INDICATOR), 0.5f)
    }
}
