package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.core.logic.TSDialogAction
import com.tinysol.designsystem.core.logic.TSDialogLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSDialogStyleSpecTest(
    private val configuration: TSDialogStyleSpec.Configuration,
    private val expected: Expectation,
) {
    data class Expectation(
        val cornerRadius: Float,
        val roundsBottom: Boolean,
        val shadow: TSShadowRole?,
        val maxWidth: Float?,
        val outerMargin: Float,
        val paddingTop: Float,
        val paddingHorizontal: Float,
        val showsGrabber: Boolean,
        val contentTopInset: Float,
        val axis: TSDialogActionsAxis,
        val primaryFirst: Boolean,
        val block: Boolean,
    )

    private val resolved = TSDialogStyleSpec.resolve(configuration)

    @Test
    fun scrimAndFillAreTokenRoles() {
        assertEquals(TSColorRole.Overlay, resolved.scrim)
        assertEquals(TSColorRole.SurfaceElevated, resolved.fill)
    }

    @Test
    fun panelShapeAndShadow() {
        assertEquals(expected.cornerRadius, resolved.cornerRadiusDp, 0f)
        assertEquals(expected.roundsBottom, resolved.roundsBottomCorners)
        assertEquals(expected.shadow, resolved.shadow)
        assertEquals(expected.maxWidth, resolved.maxWidthDp)
        assertEquals(expected.outerMargin, resolved.outerMarginDp, 0f)
    }

    @Test
    fun paddingAndGrabber() {
        assertEquals(expected.paddingTop, resolved.paddingTopDp, 0f)
        assertEquals(expected.paddingHorizontal, resolved.paddingHorizontalDp, 0f)
        assertEquals(24f, resolved.paddingBottomDp, 0f)
        assertEquals(expected.showsGrabber, resolved.showsGrabber)
        assertEquals(36f, resolved.grabberWidthDp, 0f)
        assertEquals(4f, resolved.grabberHeightDp, 0f)
        assertEquals(TSColorRole.BorderStrong, resolved.grabberColor)
        assertEquals(16f, resolved.grabberSpacingDp, 0f)
        assertEquals(expected.contentTopInset, resolved.contentTopInsetDp, 0f)
    }

    @Test
    fun textRoles() {
        assertEquals(TSTypeRole.Title2, resolved.titleTypeRole)
        assertEquals(TSColorRole.TextPrimary, resolved.titleColor)
        assertEquals(8f, resolved.titleSpacingDp, 0f)
        assertEquals(TSTypeRole.Body, resolved.bodyTypeRole)
        assertEquals(TSColorRole.TextSecondary, resolved.bodyColor)
    }

    @Test
    fun actionsLayout() {
        assertEquals(24f, resolved.actionsTopSpacingDp, 0f)
        assertEquals(8f, resolved.actionsSpacingDp, 0f)
        assertEquals(expected.axis, resolved.actionsAxis)
        assertEquals(expected.primaryFirst, resolved.primaryActionFirst)
        assertEquals(expected.block, resolved.blockActions)
    }

    companion object {
        private val center = Expectation(16f, true, TSShadowRole.Lg, 440f, 16f, 24f, 24f, false, 24f, TSDialogActionsAxis.Horizontal, false, false)
        private val centerLarge = center.copy(axis = TSDialogActionsAxis.Vertical, primaryFirst = true, block = true)
        private val sheet = Expectation(16f, false, null, null, 0f, 12f, 16f, true, 32f, TSDialogActionsAxis.Vertical, true, true)

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = listOf(
            arrayOf(TSDialogStyleSpec.Configuration(TSDialogVariant.Center, false), center),
            arrayOf(TSDialogStyleSpec.Configuration(TSDialogVariant.Center, true), centerLarge),
            arrayOf(TSDialogStyleSpec.Configuration(TSDialogVariant.Sheet, false), sheet),
            arrayOf(TSDialogStyleSpec.Configuration(TSDialogVariant.Sheet, true), sheet),
        )
    }
}

class TSDialogStyleSpecMatrixTest {
    @Test
    fun matrixHas4Configurations() {
        assertEquals(4, TSDialogStyleSpec.configurations.toSet().size)
    }

    @Test
    fun accessibilitySizeStartsAtTheStackedFontScale() {
        assertFalse(TSDialogStyleSpec.isAccessibilitySize(1.3f))
        assertTrue(TSDialogStyleSpec.isAccessibilitySize(1.5f))
        assertTrue(TSDialogStyleSpec.isAccessibilitySize(2f))
        assertEquals(TSDialogStyleSpec.Configuration(TSDialogVariant.Sheet, true), TSDialogStyleSpec.configuration(TSDialogVariant.Sheet, 2f))
    }

    @Test
    fun predictiveBackScaleShrinksToNinetyPercent() {
        assertEquals(1f, TSDialogStyleSpec.predictiveBackScale(0f), 0f)
        assertEquals(0.95f, TSDialogStyleSpec.predictiveBackScale(0.5f), 0.0001f)
        assertEquals(0.9f, TSDialogStyleSpec.predictiveBackScale(1f), 0.0001f)
        assertEquals(0.9f, TSDialogStyleSpec.predictiveBackScale(3f), 0.0001f)
        assertEquals(1f, TSDialogStyleSpec.predictiveBackScale(-1f), 0f)
    }

    @Test
    fun primaryActionMovesFirstWhenStacked() {
        val cancel = TSDialogAction("Cancel")
        val delete = TSDialogAction("Delete", TSButtonVariant.Danger)
        assertEquals(listOf("Cancel", "Delete"), TSDialogLayout.orderedActions(listOf(cancel, delete), primaryFirst = false).map { it.label })
        assertEquals(listOf("Delete", "Cancel"), TSDialogLayout.orderedActions(listOf(cancel, delete), primaryFirst = true).map { it.label })
    }

    @Test
    fun actionDefaults() {
        val action = TSDialogAction("Close")
        assertEquals(TSButtonVariant.Secondary, action.variant)
        assertTrue(action.dismisses)
        assertNull(TSDialogStyleSpec.resolve(TSDialogStyleSpec.Configuration(TSDialogVariant.Sheet)).maxWidthDp)
    }
}
