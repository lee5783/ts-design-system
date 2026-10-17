package com.ts.ds.core.style

import com.ts.ds.core.motion.TSReduceMotion
import com.ts.ds.tokens.TSTokenValues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSSwitchStyleSpecTest(
    private val configuration: TSSwitchStyleSpec.Configuration,
    private val state: TSControlState,
) {
    private val resolved = TSSwitchStyleSpec.resolve(configuration, state)
    private val pressed = state == TSControlState.Pressed

    @Test
    fun trackFollowsTheMatrix() {
        val (track, opacity) = when {
            configuration.checked && pressed -> TSColorRole.AccentPressed to 1f
            configuration.checked -> TSColorRole.AccentPrimary to 1f
            pressed -> TSColorRole.BorderStrong to 0.85f
            else -> TSColorRole.BorderStrong to 1f
        }
        assertEquals(track, resolved.trackColor)
        assertEquals(opacity, resolved.trackOpacity, 0f)
    }

    @Test
    fun thumbIsTheSwitchThumbTokenWithSmallShadow() {
        assertEquals(TSColorRole.SwitchThumb, resolved.thumbColor)
        assertEquals(TSShadowRole.Sm, resolved.thumbShadow)
        assertEquals(20f, resolved.thumbSizeDp, 0f)
        assertEquals(3f, resolved.thumbInsetDp, 0f)
        assertEquals(if (configuration.checked) 21f else 3f, resolved.thumbStartOffsetDp, 0f)
    }

    @Test
    fun thumbOnSideIsTrailingInBothDirections() {
        assertEquals(if (configuration.checked) 21f else 3f, resolved.thumbLeftDp(rightToLeft = false), 0f)
        assertEquals(if (configuration.checked) 3f else 21f, resolved.thumbLeftDp(rightToLeft = true), 0f)
    }

    @Test
    fun geometryMotionAndStates() {
        assertEquals(44f, resolved.trackWidthDp, 0f)
        assertEquals(26f, resolved.trackHeightDp, 0f)
        assertEquals(13f, resolved.trackCornerRadiusDp, 0f)
        assertEquals(150, resolved.animationDurationMillis)
        assertSame(TSTokenValues.Easing.easingStandard, resolved.animationEasing)
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
        fun parameters(): List<Array<Any>> = TSSwitchStyleSpec.configurations.flatMap { configuration ->
            TSSwitchStyleSpec.states.map { arrayOf<Any>(configuration, it) }
        }
    }
}

class TSSwitchStyleSpecMatrixTest {
    @Test
    fun matrixIsOffOnTimesFourStates() {
        assertEquals(8, TSSwitchStyleSpec.configurations.size * TSSwitchStyleSpec.states.size)
    }

    @Test
    fun reduceMotionMakesTheDurationZero() {
        assertEquals(0, TSReduceMotion.durationMillis(150, reduceMotion = true))
        assertEquals(150, TSReduceMotion.durationMillis(150, reduceMotion = false))
    }
}
