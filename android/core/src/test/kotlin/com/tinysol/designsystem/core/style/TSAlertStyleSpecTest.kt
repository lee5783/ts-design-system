package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.core.icon.TSIcon
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSAlertStyleSpecTest(
    private val configuration: TSAlertStyleSpec.Configuration,
) {
    private val resolved = TSAlertStyleSpec.resolve(configuration)

    @Test
    fun toneTable() {
        val (fill, icon, iconColor) = when (configuration.tone) {
            TSAlertTone.Info -> Triple(TSColorRole.InfoSubtle, TSIcon.Info, TSColorRole.Info)
            TSAlertTone.Success -> Triple(TSColorRole.SuccessSubtle, TSIcon.CheckCircle, TSColorRole.Success)
            TSAlertTone.Warning -> Triple(TSColorRole.WarningSubtle, TSIcon.Alert, TSColorRole.Warning)
            TSAlertTone.Danger -> Triple(TSColorRole.DangerSubtle, TSIcon.XCircle, TSColorRole.Danger)
        }
        assertEquals(fill, resolved.fill)
        assertEquals(icon, resolved.icon)
        assertEquals(icon, TSAlertStyleSpec.icon(configuration.tone))
        assertEquals(iconColor, resolved.iconColor)
    }

    @Test
    fun titleAndDismissFollowTheConfiguration() {
        assertEquals(configuration.hasTitle, resolved.showsTitle)
        assertEquals(configuration.isDismissible, resolved.showsDismiss)
    }

    @Test
    fun anatomyComesFromTokens() {
        assertEquals(1f, resolved.borderWidthDp, 0f)
        assertEquals(12f, resolved.cornerRadiusDp, 0f)
        assertEquals(16f, resolved.horizontalPaddingDp, 0f)
        assertEquals(12f, resolved.verticalPaddingDp, 0f)
        assertEquals(17f, resolved.contentHorizontalInsetDp, 0f)
        assertEquals(13f, resolved.contentVerticalInsetDp, 0f)
        assertEquals(12f, resolved.spacingDp, 0f)
        assertEquals(TSIconSize.Md, resolved.iconSize)
        assertEquals(TSTypeRole.BodySm, resolved.titleTypeRole)
        assertEquals(600, resolved.titleFontWeight)
        assertEquals(TSColorRole.TextPrimary, resolved.titleColor)
        assertEquals(TSTypeRole.BodySm, resolved.messageTypeRole)
        assertEquals(TSColorRole.TextSecondary, resolved.messageColor)
        assertEquals(TSIcon.Close, resolved.dismissIcon)
        assertEquals(TSIconSize.Md, resolved.dismissIconSize)
        assertEquals(TSColorRole.TextSecondary, resolved.dismissColor)
        assertEquals(0.85f, resolved.dismissPressedOpacity, 0f)
        assertEquals(48f, resolved.minTouchTargetDp, 0f)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = TSAlertStyleSpec.configurations.map { arrayOf<Any>(it) }
    }
}

class TSAlertStyleSpecMatrixTest {
    @Test
    fun matrixHas16Configurations() {
        assertEquals(16, TSAlertStyleSpec.configurations.size)
        assertEquals(16, TSAlertStyleSpec.configurations.toSet().size)
    }

    @Test
    fun alertIsStaticSoStateDoesNotChangeIt() {
        val configuration = TSAlertStyleSpec.Configuration(TSAlertTone.Warning, isDismissible = true)
        listOf(TSControlState.Pressed, TSControlState.Disabled, TSControlState.Focused).forEach {
            assertEquals(TSAlertStyleSpec.resolve(configuration), TSAlertStyleSpec.resolve(configuration, it))
        }
    }
}
