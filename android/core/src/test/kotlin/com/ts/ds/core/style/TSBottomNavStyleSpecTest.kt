package com.ts.ds.core.style

import com.ts.ds.core.icon.TSIcon
import com.ts.ds.tokens.TSTokenValues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSBottomNavStyleSpecTest(
    private val configuration: TSBottomNavStyleSpec.Configuration,
    private val state: TSControlState,
) {
    private val resolved = TSBottomNavStyleSpec.resolve(configuration, state)
    private val selected = configuration.selected

    @Test
    fun foregroundFollowsTheSelection() {
        assertEquals(if (selected) TSColorRole.AccentText else TSColorRole.TextMuted, resolved.foreground)
        assertEquals(TSTypeRole.Caption, resolved.labelTypeRole)
        assertEquals(500, resolved.labelFontWeight)
    }

    @Test
    fun pillFollowsTheMatrix() {
        val expected = when {
            selected -> TSColorRole.AccentSubtle
            state == TSControlState.Pressed -> TSColorRole.SurfacePressed
            else -> null
        }
        assertEquals(expected, resolved.pillFill)
        assertEquals(56f, resolved.pillWidthDp, 0f)
        assertEquals(28f, resolved.pillHeightDp, 0f)
        assertEquals(14f, resolved.pillCornerRadiusDp, 0f)
        assertEquals(24f, resolved.iconSizeDp, 0f)
        assertEquals(2f, resolved.spacingDp, 0f)
    }

    @Test
    fun statesDimEachItemAndRingOnFocus() {
        assertEquals(if (state == TSControlState.Disabled) 0.45f else 1f, resolved.opacity, 0f)
        assertEquals(state == TSControlState.Focused, resolved.showsFocusRing)
        assertEquals(150, resolved.animationDurationMillis)
        assertSame(TSTokenValues.Easing.easingStandard, resolved.animationEasing)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1}")
        fun parameters(): List<Array<Any>> = TSBottomNavStyleSpec.configurations.flatMap { configuration ->
            TSBottomNavStyleSpec.states.map { arrayOf<Any>(configuration, it) }
        }
    }
}

class TSBottomNavStyleSpecMatrixTest {
    private val items = listOf(
        TSBottomNavItem("home", "Home", TSIcon.Home),
        TSBottomNavItem("inbox", "Inbox", TSIcon.Mail, badge = 3),
    )

    @Test
    fun matrixIsSelectionTimesFourStates() {
        assertEquals(8, TSBottomNavStyleSpec.configurations.size * TSBottomNavStyleSpec.states.size)
    }

    @Test
    fun disabledIgnoresPressed() {
        val resolved = TSBottomNavStyleSpec.resolve(TSBottomNavStyleSpec.Configuration(), TSControlState(enabled = false, pressed = true))
        assertNull(resolved.pillFill)
        assertEquals(0.45f, resolved.opacity, 0f)
    }

    @Test
    fun containerIsA64SurfaceCardBandWithAHairline() {
        val container = TSBottomNavStyleSpec.container
        assertEquals(64f, container.heightDp, 0f)
        assertEquals(TSColorRole.SurfaceCard, container.fill)
        assertEquals(TSColorRole.BorderSubtle, container.border)
        assertEquals(1f, container.borderWidthDp, 0f)
    }

    @Test
    fun badgeUsesTheBottomNavTokensAndSitsThreeAboveThePill() {
        val badge = TSBottomNavStyleSpec.badge
        assertEquals(TSColorRole.DangerSolid, badge.fill)
        assertEquals(TSColorRole.TextOnAccent, badge.foreground)
        assertEquals(10f, badge.fontSizeDp, 0f)
        assertEquals(600, badge.fontWeight)
        assertEquals(16f, badge.minWidthDp, 0f)
        assertEquals(16f, badge.heightDp, 0f)
        assertEquals(4f, badge.horizontalPaddingDp, 0f)
        assertEquals(8f, badge.cornerRadiusDp, 0f)
        assertEquals(6f, badge.offsetTopDp, 0f)
        assertEquals(6f, badge.offsetStartDp, 0f)
        assertEquals(9f, TSBottomNavStyleSpec.defaultContentInsetDp, 0f)
        assertEquals(-3f, badge.topFromPillDp, 0f)
    }

    @Test
    fun labelScaleIsClampedSoTheBarStays64() {
        assertEquals(1f, TSBottomNavStyleSpec.labelFontScale(1f), 0f)
        assertEquals(1.3f, TSBottomNavStyleSpec.labelFontScale(1.3f), 0f)
        assertEquals(1.3f, TSBottomNavStyleSpec.labelFontScale(2f), 0f)
        assertEquals(0.85f, TSBottomNavStyleSpec.labelFontScale(0.85f), 0f)
        val clampedLine = TSTypeRole.Caption.values.lineHeight * TSBottomNavStyleSpec.labelFontScale(2f)
        val resolved = TSBottomNavStyleSpec.resolve(TSBottomNavStyleSpec.Configuration())
        assertTrue(resolved.pillHeightDp + resolved.spacingDp + clampedLine <= TSBottomNavStyleSpec.container.heightDp)
    }

    @Test
    fun badgeTextFormatsCountsAndHidesEmptyOnes() {
        assertNull(items[0].badgeText)
        assertEquals("3", items[1].badgeText)
        assertNull(items[1].badged(0).badgeText)
        assertNull(items[1].badged(-2).badgeText)
        assertEquals("99", items[1].badged(99).badgeText)
        assertEquals("99+", items[1].badged(100).badgeText)
        assertEquals(items[1].copy(badge = 7), items[1].badged(7))
    }

    @Test
    fun itemCountRangeIsThreeToFive() {
        assertEquals(3..5, TSBottomNavStyleSpec.itemCountRange)
    }

    @Test
    fun unknownSelectionFallsBackToTheFirstItem() {
        assertEquals(1, TSBottomNavStyleSpec.selectedIndex(items, "inbox"))
        assertEquals(0, TSBottomNavStyleSpec.selectedIndex(items, "missing"))
    }
}
