package com.ts.ds.core.style

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSBadgeStyleSpecTest(
    private val configuration: TSBadgeStyleSpec.Configuration,
) {
    private val resolved = TSBadgeStyleSpec.resolve(configuration)

    @Test
    fun colorsFollowTheToneTable() {
        val (fill, foreground) = when (configuration.tone) {
            TSBadgeTone.Neutral -> TSColorRole.BackgroundSecondary to TSColorRole.TextSecondary
            TSBadgeTone.Accent -> TSColorRole.AccentSubtle to TSColorRole.AccentText
            TSBadgeTone.Success -> TSColorRole.SuccessSubtle to TSColorRole.Success
            TSBadgeTone.Warning -> TSColorRole.WarningSubtle to TSColorRole.Warning
            TSBadgeTone.Danger -> TSColorRole.DangerSubtle to TSColorRole.Danger
            TSBadgeTone.Info -> TSColorRole.InfoSubtle to TSColorRole.Info
        }
        assertEquals(fill, resolved.fill)
        assertEquals(foreground, resolved.foreground)
    }

    @Test
    fun onlyNeutralHasABorder() {
        val neutral = configuration.tone == TSBadgeTone.Neutral
        assertEquals(if (neutral) TSColorRole.BorderSubtle else null, resolved.border)
        assertEquals(if (neutral) 1f else 0f, resolved.borderWidthDp, 0f)
    }

    @Test
    fun dotAndIconFollowTheConfiguration() {
        assertEquals(configuration.hasDot, resolved.showsDot)
        assertEquals(configuration.hasIcon, resolved.showsIcon)
        assertEquals(6f, resolved.dotSizeDp, 0f)
        assertEquals(16f, resolved.iconSizeDp, 0f)
    }

    @Test
    fun anatomy() {
        assertEquals(22f, resolved.minHeightDp, 0f)
        assertEquals(8f, resolved.horizontalPaddingDp, 0f)
        assertEquals(2f, resolved.verticalPaddingDp, 0f)
        assertEquals(4f, resolved.spacingDp, 0f)
        assertEquals(6f, resolved.cornerRadiusDp, 0f)
        assertEquals(TSTypeRole.Caption, resolved.typeRole)
        assertEquals(500, resolved.fontWeight)
        assertEquals(1, resolved.maxLines)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = TSBadgeStyleSpec.configurations.map { arrayOf<Any>(it) }
    }
}

class TSBadgeStyleSpecMatrixTest {
    @Test
    fun matrixHas24Configurations() {
        assertEquals(24, TSBadgeStyleSpec.configurations.size)
        assertEquals(24, TSBadgeStyleSpec.configurations.toSet().size)
    }

    @Test
    fun badgeIsStaticSoStateDoesNotChangeIt() {
        val configuration = TSBadgeStyleSpec.Configuration(TSBadgeTone.Danger, hasDot = true)
        listOf(TSControlState.Pressed, TSControlState.Disabled, TSControlState.Focused).forEach {
            assertEquals(TSBadgeStyleSpec.resolve(configuration), TSBadgeStyleSpec.resolve(configuration, it))
        }
    }
}
