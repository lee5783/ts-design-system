package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSTokenValues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSTabsStyleSpecTest(
    private val configuration: TSTabsStyleSpec.Configuration,
    private val state: TSControlState,
) {
    private val resolved = TSTabsStyleSpec.resolve(configuration, state)
    private val pressed = state == TSControlState.Pressed
    private val selected = configuration.selected
    private val underline = configuration.variant == TSTabsVariant.Underline

    @Test
    fun labelFollowsTheMatrix() {
        val expected = when {
            underline && selected -> TSColorRole.AccentText
            underline && pressed -> TSColorRole.TextPrimary
            underline -> TSColorRole.TextMuted
            selected -> TSColorRole.TextPrimary
            else -> TSColorRole.TextSecondary
        }
        assertEquals(expected, resolved.labelColor)
        assertEquals(TSTypeRole.Label, resolved.labelTypeRole)
    }

    @Test
    fun fillShadowAndIndicatorFollowTheMatrix() {
        val fill = when {
            underline -> null
            selected -> TSColorRole.SurfaceCard
            pressed -> TSColorRole.SurfacePressed
            else -> null
        }
        assertEquals(fill, resolved.fill)
        assertEquals(if (!underline && selected) TSShadowRole.Sm else null, resolved.shadow)
        assertEquals(if (underline && selected) TSColorRole.AccentText else null, resolved.indicatorColor)
        assertEquals(if (underline) 2f else 0f, resolved.indicatorHeightDp, 0f)
    }

    @Test
    fun geometryFollowsTheVariant() {
        if (underline) {
            assertEquals(6f, resolved.cornerRadiusDp, 0f)
            assertEquals(12f, resolved.paddingVerticalDp, 0f)
            assertEquals(0f, resolved.paddingHorizontalDp, 0f)
            assertEquals(44f, resolved.minHeightDp, 0f)
            assertEquals(2f, resolved.touchOutsetDp, 0f)
        } else {
            assertEquals(8f, resolved.cornerRadiusDp, 0f)
            assertEquals(6f, resolved.paddingVerticalDp, 0f)
            assertEquals(16f, resolved.paddingHorizontalDp, 0f)
            assertEquals(38f, resolved.minHeightDp, 0f)
            assertEquals(5f, resolved.touchOutsetDp, 0f)
        }
        assertEquals(48f, resolved.minTouchTargetDp, 0f)
    }

    @Test
    fun statesDimOnceAndRingOnFocus() {
        assertEquals(if (state == TSControlState.Disabled) 0.45f else 1f, resolved.opacity, 0f)
        assertEquals(state == TSControlState.Focused, resolved.showsFocusRing)
        assertEquals(150, resolved.animationDurationMillis)
        assertSame(TSTokenValues.Easing.easingStandard, resolved.animationEasing)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1}")
        fun parameters(): List<Array<Any>> = TSTabsStyleSpec.configurations.flatMap { configuration ->
            TSTabsStyleSpec.states.map { arrayOf<Any>(configuration, it) }
        }
    }
}

class TSTabsStyleSpecMatrixTest {
    @Test
    fun matrixIsTwoVariantsTimesSelectionTimesFourStates() {
        assertEquals(16, TSTabsStyleSpec.configurations.size * TSTabsStyleSpec.states.size)
    }

    @Test
    fun underlineContainerIsAScrollingHairline() {
        val container = TSTabsStyleSpec.container(TSTabsVariant.Underline)
        assertEquals(null, container.fill)
        assertEquals(TSColorRole.BorderSubtle, container.border)
        assertEquals(1f, container.borderWidthDp, 0f)
        assertEquals(16f, container.spacingDp, 0f)
        assertEquals(44f, container.minHeightDp, 0f)
        assertEquals(2f, container.touchOutsetDp, 0f)
        assertEquals(0f, container.contentInsetDp, 0f)
        assertEquals(true, container.scrollable)
        assertEquals(1f, container.opacity, 0f)
    }

    @Test
    fun segmentedTrackIs46WithTheSegmentsInsetByPaddingAndBorder() {
        val container = TSTabsStyleSpec.container(TSTabsVariant.Segmented)
        val segment = TSTabsStyleSpec.resolve(TSTabsStyleSpec.Configuration(TSTabsVariant.Segmented))
        assertEquals(TSColorRole.BackgroundSecondary, container.fill)
        assertEquals(12f, container.cornerRadiusDp, 0f)
        assertEquals(3f, container.paddingDp, 0f)
        assertEquals(4f, container.contentInsetDp, 0f)
        assertEquals(46f, container.minHeightDp, 0f)
        assertEquals(1f, container.touchOutsetDp, 0f)
        assertEquals(false, container.scrollable)
        assertEquals(container.minHeightDp, segment.minHeightDp + container.contentInsetDp * 2f, 0f)
        assertEquals(48f, segment.minHeightDp + segment.touchOutsetDp * 2f, 0f)
    }

    @Test
    fun disabledContainerDimsOnce() {
        TSTabsVariant.entries.forEach { assertEquals(0.45f, TSTabsStyleSpec.container(it, enabled = false).opacity, 0f) }
    }

    @Test
    fun unknownSelectionFallsBackToTheFirstItem() {
        val items = listOf(TSTabItem("day", "Day"), TSTabItem("week", "Week"))
        assertEquals(1, TSTabsStyleSpec.selectedIndex(items, "week"))
        assertEquals(0, TSTabsStyleSpec.selectedIndex(items, "missing"))
    }
}
