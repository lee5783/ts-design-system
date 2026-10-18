package com.tinysol.designsystem.core.style

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSIconButtonStyleSpecTest(
    private val configuration: TSIconButtonStyleSpec.Configuration,
    private val state: TSControlState,
) {
    private val resolved = TSIconButtonStyleSpec.resolve(configuration, state)
    private val pressed = state == TSControlState.Pressed

    @Test
    fun colorsFollowTheVariantTable() {
        val (fill, icon) = when (configuration.variant) {
            TSIconButtonVariant.Ghost -> (if (pressed) TSColorRole.SurfacePressed else null) to TSColorRole.TextPrimary
            TSIconButtonVariant.Secondary -> (if (pressed) TSColorRole.SurfacePressed else TSColorRole.SurfaceCard) to TSColorRole.TextPrimary
            TSIconButtonVariant.Primary -> (if (pressed) TSColorRole.AccentPressed else TSColorRole.AccentPrimary) to TSColorRole.TextOnAccent
        }
        assertEquals(fill, resolved.fill)
        assertEquals(icon, resolved.icon)
    }

    @Test
    fun secondaryHasABorderButNoShadowField() {
        val secondary = configuration.variant == TSIconButtonVariant.Secondary
        assertEquals(if (secondary) TSColorRole.BorderStrong else null, resolved.border)
        assertEquals(if (secondary) 1f else 0f, resolved.borderWidthDp, 0f)
    }

    @Test
    fun geometryAndStates() {
        assertEquals(44f, resolved.sizeDp, 0f)
        assertEquals(if (configuration.round) 22f else 8f, resolved.cornerRadiusDp, 0f)
        assertEquals(24f, resolved.iconSizeDp, 0f)
        assertEquals(48f, resolved.minTouchTargetDp, 0f)
        assertEquals(if (state == TSControlState.Disabled) 0.45f else 1f, resolved.opacity, 0f)
        assertEquals(state == TSControlState.Focused, resolved.showsFocusRing)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1}")
        fun parameters(): List<Array<Any>> = TSIconButtonStyleSpec.configurations.flatMap { configuration ->
            TSIconButtonStyleSpec.states.map { arrayOf<Any>(configuration, it) }
        }
    }
}

class TSIconStyleSpecTest {
    @Test
    fun sizesMapToIconTokens() {
        assertEquals(listOf(16f, 20f, 24f), TSIconSize.entries.map { TSIconStyleSpec.resolve(TSIconStyleSpec.Configuration(it)).sizeDp })
        assertEquals(24, TSIconButtonStyleSpec.configurations.size * TSIconButtonStyleSpec.states.size)
    }
}
