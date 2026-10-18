package com.ts.ds.core.style

import com.ts.ds.tokens.TSTokenValues
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSSpinnerStyleSpecTest(
    private val configuration: TSSpinnerStyleSpec.Configuration,
) {
    private val resolved = TSSpinnerStyleSpec.resolve(configuration)

    @Test
    fun diameterIsTheIconSizeToken() {
        val expected = when (configuration.size) {
            TSSpinnerSize.Sm -> TSTokenValues.Size.iconSm
            TSSpinnerSize.Md -> TSTokenValues.Size.iconMd
            TSSpinnerSize.Lg -> TSTokenValues.Size.iconLg
        }
        assertEquals(expected, resolved.sizeDp, 0f)
    }

    @Test
    fun strokeKeepsTheButtonSpinnerRatio() {
        val ratio = TSTokenValues.Component.spinnerStrokeWidth / TSTokenValues.Component.spinnerSize
        assertEquals(resolved.sizeDp * ratio, resolved.strokeWidthDp, 0.0001f)
    }

    @Test
    fun toneMapsToTextOrAccentRole() {
        val expected = when (configuration.tone) {
            TSSpinnerTone.Primary -> TSColorRole.TextPrimary
            TSSpinnerTone.Secondary -> TSColorRole.TextSecondary
            TSSpinnerTone.Accent -> TSColorRole.AccentPrimary
        }
        assertEquals(expected, resolved.color)
    }

    @Test
    fun rotatesOnceEverySpinnerRotationDuration() {
        assertEquals(TSTokenValues.Component.spinnerRotationDuration, resolved.rotationMillis)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = TSSpinnerStyleSpec.configurations.map { arrayOf(it) }
    }
}

class TSSpinnerStyleSpecMatrixTest {
    @Test
    fun matrixIsThreeSizesTimesThreeTones() {
        assertEquals(9, TSSpinnerStyleSpec.configurations.size)
        assertEquals(9, TSSpinnerStyleSpec.configurations.toSet().size)
    }

    @Test
    fun sizesAre16To24AndSmallMatchesTheButtonSpinner() {
        val sizes = TSSpinnerSize.entries.map { TSSpinnerStyleSpec.resolve(TSSpinnerStyleSpec.Configuration(it)) }
        assertEquals(listOf(16f, 20f, 24f), sizes.map { it.sizeDp })
        assertEquals(listOf(2f, 2.5f, 3f), sizes.map { it.strokeWidthDp })
        val button = TSButtonStyleSpec.resolve(TSButtonStyleSpec.Configuration(), TSControlState.Loading)
        assertEquals(button.spinnerSizeDp, sizes.first().sizeDp, 0f)
        assertEquals(button.spinnerStrokeWidthDp, sizes.first().strokeWidthDp, 0f)
        assertEquals(button.spinnerRotationMillis, sizes.first().rotationMillis)
    }

    @Test
    fun defaultsAreMediumPrimary() {
        assertEquals(TSSpinnerSize.Md, TSSpinnerStyleSpec.Configuration().size)
        assertEquals(TSSpinnerTone.Primary, TSSpinnerStyleSpec.Configuration().tone)
    }
}
