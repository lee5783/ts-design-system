package com.ts.ds.core.style

import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSCardAccessibility
import com.ts.ds.core.logic.TSCardAccessory
import com.ts.ds.core.logic.TSCardAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSCardStyleSpecTest(
    private val configuration: TSCardStyleSpec.Configuration,
    private val state: TSControlState,
) {
    private val resolved = TSCardStyleSpec.resolve(configuration, state)

    @Test
    fun pressedFillOnlyOnAnInteractiveCard() {
        val pressed = configuration.isInteractive && state == TSControlState.Pressed
        assertEquals(if (pressed) TSColorRole.SurfacePressed else TSColorRole.SurfaceCard, resolved.fill)
    }

    @Test
    fun focusRingOnlyOnAFocusedInteractiveCard() {
        assertEquals(configuration.isInteractive && state == TSControlState.Focused, resolved.showsFocusRing)
    }

    @Test
    fun disabledDimsTheCard() {
        assertEquals(if (state == TSControlState.Disabled) 0.45f else 1f, resolved.opacity, 0f)
    }

    @Test
    fun raisedCastsTheSmallShadow() {
        assertEquals(if (configuration.isRaised) TSShadowRole.Sm else null, resolved.shadow)
    }

    @Test
    fun anatomyComesFromTokens() {
        assertEquals(TSColorRole.BorderSubtle, resolved.border)
        assertEquals(1f, resolved.borderWidthDp, 0f)
        assertEquals(12f, resolved.cornerRadiusDp, 0f)
        assertEquals(TSTypeRole.Title3, resolved.titleTypeRole)
        assertEquals(TSColorRole.TextPrimary, resolved.titleColor)
        assertEquals(TSTypeRole.BodySm, resolved.subtitleTypeRole)
        assertEquals(TSColorRole.TextMuted, resolved.subtitleColor)
        assertEquals(2f, resolved.subtitleSpacingDp, 0f)
        assertEquals(TSTypeRole.BodySm, resolved.bodyTypeRole)
        assertEquals(TSColorRole.TextSecondary, resolved.bodyColor)
        assertEquals(16f, resolved.paddingDp, 0f)
        assertEquals(12f, resolved.headerSpacingDp, 0f)
        assertEquals(12f, resolved.footerVerticalPaddingDp, 0f)
        assertEquals(16f, resolved.footerHorizontalPaddingDp, 0f)
        assertEquals(8f, resolved.footerSpacingDp, 0f)
        assertEquals(TSButtonVariant.Secondary, resolved.footerButtonVariant)
        assertEquals(TSButtonSize.Md, resolved.footerButtonSize)
        assertEquals(TSColorRole.BorderSubtle, resolved.divider)
        assertEquals(1f, resolved.dividerThicknessDp, 0f)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1}")
        fun parameters(): List<Array<Any>> = TSCardStyleSpec.configurations.flatMap { configuration ->
            TSCardStyleSpec.states.map { arrayOf<Any>(configuration, it) }
        }
    }
}

class TSCardStyleSpecMatrixTest {
    @Test
    fun matrixHas16Cases() {
        assertEquals(4, TSCardStyleSpec.configurations.toSet().size)
        assertEquals(4, TSCardStyleSpec.states.toSet().size)
    }

    @Test
    fun cardsStack16Apart() {
        assertEquals(16f, TSCardStyleSpec.STACK_SPACING_DP, 0f)
    }

    @Test
    fun footerStacksFromTheLargeFontThreshold() {
        assertFalse(TSCardStyleSpec.stacksFooter(1f))
        assertFalse(TSCardStyleSpec.stacksFooter(1.3f))
        assertTrue(TSCardStyleSpec.stacksFooter(1.5f))
        assertTrue(TSCardStyleSpec.stacksFooter(2f))
    }

    @Test
    fun loadingDoesNotDimOrPressTheCard() {
        val resolved = TSCardStyleSpec.resolve(TSCardStyleSpec.Configuration(isInteractive = true), TSControlState.Loading)
        assertEquals(TSColorRole.SurfaceCard, resolved.fill)
        assertEquals(1f, resolved.opacity, 0f)
    }

    @Test
    fun labelJoinsTitleAndSubtitle() {
        assertEquals("Plan, Pro", TSCardAccessibility.label("Plan", "Pro"))
        assertEquals("Plan", TSCardAccessibility.label("Plan", null))
        assertEquals("Plan", TSCardAccessibility.label("Plan", ""))
        assertEquals("Pro", TSCardAccessibility.label(null, "Pro"))
        assertEquals("", TSCardAccessibility.label(null, null))
    }

    @Test
    fun customActionsListFooterFirstAndAreDroppedWhileDisabled() {
        val share = TSCardAction("Share") {}
        val open = TSCardAction("Open", TSButtonVariant.Primary) {}
        val more = TSCardAction("More options") {}

        assertEquals(
            listOf("Share", "Open", "More options"),
            TSCardAccessibility.customActions(listOf(share, open), listOf(more)).map { it.label },
        )
        assertEquals(emptyList<TSCardAction>(), TSCardAccessibility.customActions(listOf(share), listOf(more), enabled = false))
        assertEquals(TSButtonVariant.Secondary, share.variant)
    }

    @Test
    fun labelFallsBackToTheBodyAndTheValueCarriesTheBodyBesideAHeading() {
        assertEquals("Billed monthly", TSCardAccessibility.label(null, null, "Billed monthly"))
        assertEquals("Plan", TSCardAccessibility.label("Plan", null, "Billed monthly"))
        assertEquals("", TSCardAccessibility.label("", "", ""))
        assertEquals("Billed monthly", TSCardAccessibility.value("Plan", null, "Billed monthly"))
        assertEquals(null, TSCardAccessibility.value(null, null, "Billed monthly"))
        assertEquals(null, TSCardAccessibility.value("Plan", "Pro", ""))
    }

    @Test
    fun accessoryIsACustomActionBetweenFooterAndAdditional() {
        var fired = 0
        val accessory = TSCardAccessory.IconButton(TSIcon.More, "More options") { fired++ }
        val actions = TSCardAccessibility.customActions(
            footer = listOf(TSCardAction("Share") {}),
            additional = listOf(TSCardAction("Pin") {}),
            accessory = accessory,
        )

        assertEquals(listOf("Share", "More options", "Pin"), actions.map { it.label })
        actions[1].onClick()
        assertEquals(1, fired)
        assertEquals(emptyList<TSCardAction>(), TSCardAccessibility.customActions(emptyList(), emptyList(), enabled = false, accessory = accessory))
    }
}
