package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.core.icon.TSIcon
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSCheckboxStyleSpecTest(
    private val configuration: TSCheckboxStyleSpec.Configuration,
    private val state: TSControlState,
) {
    private val resolved = TSCheckboxStyleSpec.resolve(configuration, state)
    private val pressed = state == TSControlState.Pressed

    @Test
    fun markColorsFollowTheMatrix() {
        val (fill, border) = when {
            configuration.checked && pressed -> TSColorRole.AccentPressed to TSColorRole.AccentPressed
            configuration.checked -> TSColorRole.AccentPrimary to TSColorRole.AccentPrimary
            pressed -> TSColorRole.SurfacePressed to TSColorRole.BorderStrong
            else -> TSColorRole.SurfaceCard to TSColorRole.BorderStrong
        }
        assertEquals(fill, resolved.markFill)
        assertEquals(border, resolved.markBorder)
        assertEquals(1.5f, resolved.markBorderWidthDp, 0f)
    }

    @Test
    fun checkShowsOnlyWhenChecked() {
        assertEquals(configuration.checked, resolved.showsCheck)
        assertEquals(TSIcon.Check, resolved.checkIcon)
        assertEquals(TSColorRole.TextOnAccent, resolved.checkColor)
        assertEquals(16f, resolved.checkSizeDp, 0f)
    }

    @Test
    fun geometryLabelAndStates() {
        assertEquals(20f, resolved.markSizeDp, 0f)
        assertEquals(4f, resolved.markCornerRadiusDp, 0f)
        assertEquals(TSTypeRole.Body, resolved.labelTypeRole)
        assertEquals(TSColorRole.TextPrimary, resolved.labelColor)
        assertEquals(8f, resolved.spacingDp, 0f)
        assertEquals(48f, resolved.minTouchTargetDp, 0f)
        assertEquals(if (state == TSControlState.Disabled) 0.45f else 1f, resolved.opacity, 0f)
        assertEquals(state == TSControlState.Focused, resolved.showsFocusRing)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1}")
        fun parameters(): List<Array<Any>> = TSCheckboxStyleSpec.configurations.flatMap { configuration ->
            TSCheckboxStyleSpec.states.map { arrayOf<Any>(configuration, it) }
        }
    }
}

class TSCheckboxStyleSpecMatrixTest {
    @Test
    fun matrixIsOffOnTimesFourStates() {
        assertEquals(8, TSCheckboxStyleSpec.configurations.size * TSCheckboxStyleSpec.states.size)
    }

    @Test
    fun disabledPressIsNotPressed() {
        val resolved = TSCheckboxStyleSpec.resolve(TSCheckboxStyleSpec.Configuration(checked = true), TSControlState.make(pressed = true, enabled = false))
        assertEquals(TSColorRole.AccentPrimary, resolved.markFill)
        assertEquals(0.45f, resolved.opacity, 0f)
    }
}
