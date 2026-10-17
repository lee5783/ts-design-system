package com.ts.ds.core.style

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSButtonStyleSpecTest(
    private val configuration: TSButtonStyleSpec.Configuration,
    private val state: TSControlState,
) {
    private val resolved = TSButtonStyleSpec.resolve(configuration, state)
    private val variant = configuration.variant
    private val isLink = variant == TSButtonVariant.Link

    @Test
    fun fillFollowsThePressedTable() {
        val expected = mapOf(
            TSButtonVariant.Primary to (TSColorRole.AccentPrimary to TSColorRole.AccentPressed),
            TSButtonVariant.Secondary to (TSColorRole.SurfaceCard to TSColorRole.SurfacePressed),
            TSButtonVariant.Ghost to (null to TSColorRole.SurfacePressed),
            TSButtonVariant.Danger to (TSColorRole.DangerSolid to TSColorRole.DangerPressed),
            TSButtonVariant.Link to (null to null),
        ).getValue(variant)
        assertEquals(if (state == TSControlState.Pressed) expected.second else expected.first, resolved.fill)
    }

    @Test
    fun labelColorIsStateIndependent() {
        val expected = when (variant) {
            TSButtonVariant.Primary, TSButtonVariant.Danger -> TSColorRole.TextOnAccent
            TSButtonVariant.Secondary, TSButtonVariant.Ghost -> TSColorRole.TextPrimary
            TSButtonVariant.Link -> TSColorRole.AccentText
        }
        assertEquals(expected, resolved.label)
    }

    @Test
    fun onlySecondaryHasBorderAndShadow() {
        val secondary = variant == TSButtonVariant.Secondary
        assertEquals(if (secondary) TSColorRole.BorderStrong else null, resolved.border)
        assertEquals(if (secondary) 1f else 0f, resolved.borderWidthDp, 0f)
        assertEquals(if (secondary) TSShadowRole.Xs else null, resolved.shadow)
    }

    @Test
    fun opacityDimsOnlyDisabledAndPressedLinks() {
        val expected = when {
            state == TSControlState.Disabled -> 0.45f
            isLink && state == TSControlState.Pressed -> 0.85f
            else -> 1f
        }
        assertEquals(expected, resolved.opacity, 0f)
    }

    @Test
    fun loadingShowsTheSpinnerAndIsNotDimmed() {
        assertEquals(state == TSControlState.Loading, resolved.showsSpinner)
        if (state == TSControlState.Loading) assertEquals(1f, resolved.opacity, 0f)
        assertEquals(16f, resolved.spinnerSizeDp, 0f)
        assertEquals(2f, resolved.spinnerStrokeWidthDp, 0f)
        assertEquals(700, resolved.spinnerRotationMillis)
    }

    @Test
    fun focusRingOnlyForTheFocusedState() {
        assertEquals(state == TSControlState.Focused, resolved.showsFocusRing)
    }

    @Test
    fun geometryFollowsTheSize() {
        val md = configuration.size == TSButtonSize.Md
        if (isLink) {
            assertEquals(0f, resolved.minHeightDp, 0f)
            assertEquals(0f, resolved.horizontalPaddingDp, 0f)
            assertEquals(0f, resolved.verticalPaddingDp, 0f)
            assertEquals(0f, resolved.cornerRadiusDp, 0f)
            assertTrue(resolved.underlined)
        } else {
            assertEquals(if (md) 44f else 52f, resolved.minHeightDp, 0f)
            assertEquals(if (md) 16f else 20f, resolved.horizontalPaddingDp, 0f)
            assertEquals(8f, resolved.verticalPaddingDp, 0f)
            assertEquals(8f, resolved.cornerRadiusDp, 0f)
            assertFalse(resolved.underlined)
        }
        assertEquals(if (md) TSTypeRole.Label else TSTypeRole.Body, resolved.typeRole)
        assertEquals(500, resolved.fontWeight)
        assertEquals(20f, resolved.iconSizeDp, 0f)
        assertEquals(8f, resolved.iconSpacingDp, 0f)
        assertEquals(48f, resolved.minTouchTargetDp, 0f)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1}")
        fun parameters(): List<Array<Any>> = TSButtonStyleSpec.configurations.flatMap { configuration ->
            TSButtonStyleSpec.states.map { arrayOf<Any>(configuration, it) }
        }
    }
}

class TSButtonStyleSpecMatrixTest {
    @Test
    fun matrixCoversFiftyConfigurations() {
        assertEquals(10, TSButtonStyleSpec.configurations.size)
        assertEquals(50, TSButtonStyleSpec.configurations.size * TSButtonStyleSpec.states.size)
    }

    @Test
    fun rawLoadingAndDisabledStillReadsAsLoading() {
        val resolved = TSButtonStyleSpec.resolve(TSButtonStyleSpec.Configuration(), TSControlState(enabled = false, loading = true))

        assertEquals(1f, resolved.opacity, 0f)
        assertTrue(resolved.showsSpinner)
    }

    @Test
    fun pressWhileDisabledIsIgnored() {
        val resolved = TSButtonStyleSpec.resolve(TSButtonStyleSpec.Configuration(), TSControlState(enabled = false, pressed = true))

        assertEquals(TSColorRole.AccentPrimary, resolved.fill)
        assertNull(TSButtonStyleSpec.resolve(TSButtonStyleSpec.Configuration(TSButtonVariant.Link), TSControlState.Pressed).fill)
    }
}
