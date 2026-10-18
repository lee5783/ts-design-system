package com.ts.ds.core.style

import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSListItemRole
import com.ts.ds.tokens.TSTokenValues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSListItemStyleSpecTest(
    private val configuration: TSListItemStyleSpec.Configuration,
    private val state: TSControlState,
) {
    private val resolved = TSListItemStyleSpec.resolve(configuration, state)
    private val stacked = configuration.layout == TSListItemLayout.Stacked

    @Test
    fun backgroundAppearsOnlyForARowWithAnAction() {
        val expected = when {
            !configuration.isInteractive -> null
            state == TSControlState.Pressed -> TSColorRole.SurfacePressed
            state == TSControlState.Focused -> TSColorRole.SurfaceHover
            else -> null
        }
        assertEquals(expected, resolved.background)
    }

    @Test
    fun disabledDimsTheRow() {
        assertEquals(if (state == TSControlState.Disabled) 0.45f else 1f, resolved.opacity, 0f)
    }

    @Test
    fun textRolesAndLineLimitsFollowTheLayout() {
        assertEquals(TSTypeRole.Body, resolved.titleTypeRole)
        assertEquals(TSColorRole.TextPrimary, resolved.titleColor)
        assertEquals(TSTypeRole.BodySm, resolved.subtitleTypeRole)
        assertEquals(TSColorRole.TextMuted, resolved.subtitleColor)
        assertEquals(configuration.hasSubtitle, resolved.showsSubtitle)
        assertEquals(if (stacked) null else 1, resolved.titleMaxLines)
        assertEquals(if (stacked) null else 1, resolved.subtitleMaxLines)
        assertEquals(stacked, resolved.isStacked)
    }

    @Test
    fun slotsAndGeometryComeFromTokens() {
        assertEquals(TSColorRole.TextSecondary, resolved.leadingColor)
        assertEquals(TSTypeRole.BodySm, resolved.trailingTypeRole)
        assertEquals(TSColorRole.TextMuted, resolved.trailingColor)
        assertEquals(8f, resolved.trailingSpacingDp, 0f)
        assertEquals(TSIcon.ChevronRight, resolved.chevron)
        assertEquals(TSIconSize.Md, resolved.chevronSize)
        assertEquals(TSColorRole.TextMuted, resolved.chevronColor)
        assertEquals(16f, resolved.horizontalPaddingDp, 0f)
        assertEquals(12f, resolved.verticalPaddingDp, 0f)
        assertEquals(12f, resolved.spacingDp, 0f)
        assertEquals(8f, resolved.stackedSpacingDp, 0f)
        assertEquals(44f, resolved.minHeightDp, 0f)
        assertEquals(48f, resolved.minTouchTargetDp, 0f)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1}")
        fun parameters(): List<Array<Any>> = TSListItemStyleSpec.configurations.flatMap { configuration ->
            TSListItemStyleSpec.states.map { arrayOf<Any>(configuration, it) }
        }
    }
}

class TSListItemStyleSpecMatrixTest {
    private fun lineHeight(role: TSTypeRole): Float = role.values.lineHeight

    @Test
    fun matrixIsSubtitleTimesInteractiveTimesLayoutTimesFourStates() {
        assertEquals(32, TSListItemStyleSpec.configurations.size * TSListItemStyleSpec.states.size)
    }

    @Test
    fun heightsAre48WithATitleAnd68WithASubtitle() {
        val resolved = TSListItemStyleSpec.resolve(TSListItemStyleSpec.Configuration())
        val titleOnly = resolved.verticalPaddingDp * 2f + lineHeight(resolved.titleTypeRole)
        assertEquals(48f, titleOnly, 0f)
        assertEquals(68f, titleOnly + lineHeight(resolved.subtitleTypeRole), 0f)
        assertTrue(titleOnly >= resolved.minHeightDp)
    }

    @Test
    fun disabledPressDoesNotHighlight() {
        val state = TSControlState.make(pressed = true, enabled = false)
        val resolved = TSListItemStyleSpec.resolve(TSListItemStyleSpec.Configuration(isInteractive = true), state)
        assertEquals(null, resolved.background)
        assertEquals(0.45f, resolved.opacity, 0f)
    }

    @Test
    fun layoutStacksFromFontScaleOnePointFive() {
        assertEquals(TSListItemLayout.Horizontal, TSListItemLayout.make(1f))
        assertEquals(TSListItemLayout.Horizontal, TSListItemLayout.make(1.3f))
        assertEquals(TSListItemLayout.Stacked, TSListItemLayout.make(1.5f))
        assertEquals(TSListItemLayout.Stacked, TSListItemLayout.make(2f))
    }

    @Test
    fun tileIsA36AccentSquare() {
        val tile = TSListItemStyleSpec.tile
        assertEquals(TSTokenValues.Component.listItemTileSize, tile.sizeDp, 0f)
        assertEquals(36f, tile.sizeDp, 0f)
        assertEquals(8f, tile.cornerRadiusDp, 0f)
        assertEquals(TSColorRole.AccentSubtle, tile.fill)
        assertEquals(TSColorRole.AccentText, tile.foreground)
        assertEquals(TSIconSize.Md, tile.iconSize)
    }

    @Test
    fun groupIsACardWithFullWidthDividers() {
        val group = TSListItemStyleSpec.group
        assertEquals(TSColorRole.SurfaceCard, group.fill)
        assertEquals(TSColorRole.BorderSubtle, group.border)
        assertEquals(1f, group.borderWidthDp, 0f)
        assertEquals(12f, group.cornerRadiusDp, 0f)
        assertEquals(TSColorRole.BorderSubtle, group.divider)
        assertEquals(1f, group.dividerThicknessDp, 0f)
        assertEquals(0f, group.dividerInsetDp, 0f)
        assertEquals(TSTypeRole.Overline, group.headerTypeRole)
        assertEquals(TSColorRole.TextMuted, group.headerColor)
        assertEquals(8f, group.headerSpacingDp, 0f)
        assertEquals(16f, group.headerHorizontalPaddingDp, 0f)
        assertEquals(24f, group.sectionSpacingDp, 0f)
    }

    @Test
    fun disclosureRotatesTheChevronDownOverDurationBase() {
        val disclosure = TSListItemStyleSpec.disclosure
        assertEquals(0f, disclosure.rotationDegrees(expanded = false, rightToLeft = false), 0f)
        assertEquals(0f, disclosure.rotationDegrees(expanded = false, rightToLeft = true), 0f)
        assertEquals(90f, disclosure.rotationDegrees(expanded = true, rightToLeft = false), 0f)
        assertEquals(-90f, disclosure.rotationDegrees(expanded = true, rightToLeft = true), 0f)
        assertEquals(TSTokenValues.Duration.durationBase, disclosure.animationDurationMillis)
        assertEquals(150, disclosure.animationDurationMillis(reduceMotion = false))
        assertEquals(0, disclosure.animationDurationMillis(reduceMotion = true))
        assertEquals(TSTokenValues.Easing.easingStandard, disclosure.animationEasing)
        assertEquals(16f, disclosure.bodyHorizontalPaddingDp, 0f)
        assertEquals(12f, disclosure.bodyBottomPaddingDp, 0f)
    }

    @Test
    fun groupPositionsFollowTheIndex() {
        assertEquals(TSListGroupPosition.Single, TSListGroupPosition.make(0, 1))
        assertEquals(TSListGroupPosition.First, TSListGroupPosition.make(0, 3))
        assertEquals(TSListGroupPosition.Middle, TSListGroupPosition.make(1, 3))
        assertEquals(TSListGroupPosition.Last, TSListGroupPosition.make(2, 3))
        assertEquals(TSListGroupPosition.Last, TSListGroupPosition.make(1, 2))
    }

    @Test
    fun groupPositionsRoundTheirOuterCornersAndDivideFromTheRowAbove() {
        val expectations = mapOf(
            TSListGroupPosition.Single to Triple(true, true, false),
            TSListGroupPosition.First to Triple(true, false, false),
            TSListGroupPosition.Middle to Triple(false, false, true),
            TSListGroupPosition.Last to Triple(false, true, true),
        )
        expectations.forEach { (position, expected) ->
            assertEquals(expected, Triple(position.roundsTop, position.roundsBottom, position.drawsTopDivider))
        }
    }

    @Test
    fun roleFollowsActionChevronAndToggle() {
        assertEquals(TSListItemRole.Plain, TSListItemRole.make(hasAction = false, showsChevron = true, hasToggle = false))
        assertEquals(TSListItemRole.Action, TSListItemRole.make(hasAction = true, showsChevron = false, hasToggle = false))
        assertEquals(TSListItemRole.Navigation, TSListItemRole.make(hasAction = true, showsChevron = true, hasToggle = false))
        assertEquals(TSListItemRole.Toggle, TSListItemRole.make(hasAction = true, showsChevron = true, hasToggle = true))
        assertTrue(TSListItemRole.Action.isPressable)
        assertTrue(TSListItemRole.Navigation.isPressable)
        assertFalse(TSListItemRole.Plain.isPressable)
        assertFalse(TSListItemRole.Toggle.isPressable)
    }
}
