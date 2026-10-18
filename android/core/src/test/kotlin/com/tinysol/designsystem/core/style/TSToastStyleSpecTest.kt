package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.core.haptics.DsHapticEvent
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.logic.TSToastTone
import com.tinysol.designsystem.tokens.TSTokenValues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSToastStyleSpecTest(
    private val configuration: TSToastStyleSpec.Configuration,
) {
    private val resolved = TSToastStyleSpec.resolve(configuration)

    @Test
    fun toneTable() {
        val (icon, iconColor, haptic) = when (configuration.tone) {
            TSToastTone.Neutral -> Triple(null, null, null)
            TSToastTone.Info -> Triple(TSIcon.Info, TSColorRole.InfoOnInverse, null)
            TSToastTone.Success -> Triple(TSIcon.CheckCircle, TSColorRole.SuccessOnInverse, DsHapticEvent.Confirm)
            TSToastTone.Warning -> Triple(TSIcon.Alert, TSColorRole.WarningOnInverse, DsHapticEvent.Reject)
            TSToastTone.Danger -> Triple(TSIcon.XCircle, TSColorRole.DangerOnInverse, DsHapticEvent.Reject)
        }
        assertEquals(icon, resolved.icon)
        assertEquals(iconColor, resolved.iconColor)
        assertEquals(haptic, TSToastStyleSpec.hapticEvent(configuration.tone))
    }

    @Test
    fun actionFollowsTheConfiguration() {
        assertEquals(configuration.hasAction, resolved.showsAction)
    }

    @Test
    fun anatomyComesFromTokens() {
        assertEquals(TSColorRole.SurfaceInverse, resolved.fill)
        assertEquals(12f, resolved.cornerRadiusDp, 0f)
        assertEquals(TSShadowRole.Lg, resolved.shadow)
        assertEquals(420f, resolved.maxWidthDp, 0f)
        assertEquals(16f, resolved.horizontalPaddingDp, 0f)
        assertEquals(12f, resolved.verticalPaddingDp, 0f)
        assertEquals(12f, resolved.spacingDp, 0f)
        assertEquals(16f, resolved.outerMarginDp, 0f)
        assertEquals(TSIconSize.Md, resolved.iconSize)
        assertEquals(TSTypeRole.BodySm, resolved.messageTypeRole)
        assertEquals(TSColorRole.TextInverse, resolved.messageColor)
        assertEquals(TSTypeRole.BodySm, resolved.actionTypeRole)
        assertEquals(600, resolved.actionFontWeight)
        assertEquals(TSColorRole.TextInverse, resolved.actionColor)
        assertEquals(true, resolved.actionIsUnderlined)
        assertEquals(8f, resolved.actionHorizontalPaddingDp, 0f)
        assertEquals(4f, resolved.actionVerticalPaddingDp, 0f)
        assertEquals(0.85f, resolved.actionPressedOpacity, 0f)
        assertEquals(48f, resolved.minTouchTargetDp, 0f)
        assertEquals(150, resolved.enterDurationMillis)
        assertEquals(120, resolved.exitDurationMillis)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = TSToastStyleSpec.configurations.map { arrayOf<Any>(it) }
    }
}

class TSToastStyleSpecMatrixTest {
    @Test
    fun matrixHas10Configurations() {
        assertEquals(10, TSToastStyleSpec.configurations.size)
        assertEquals(10, TSToastStyleSpec.configurations.toSet().size)
    }

    @Test
    fun onInverseIconColoursUseTheOppositeTheme() {
        listOf(
            TSColorRole.SuccessOnInverse to TSTokenValues.DarkColors.success,
            TSColorRole.WarningOnInverse to TSTokenValues.DarkColors.warning,
            TSColorRole.DangerOnInverse to TSTokenValues.DarkColors.danger,
            TSColorRole.InfoOnInverse to TSTokenValues.DarkColors.info,
        ).forEach { (role, darkStatus) ->
            assertEquals(darkStatus, role.light)
            assertNotEquals(role.light, role.dark)
        }
    }

    @Test
    fun toastIsStaticSoStateDoesNotChangeIt() {
        val configuration = TSToastStyleSpec.Configuration(TSToastTone.Danger, hasAction = true)
        listOf(TSControlState.Pressed, TSControlState.Disabled, TSControlState.Focused).forEach {
            assertEquals(TSToastStyleSpec.resolve(configuration), TSToastStyleSpec.resolve(configuration, it))
        }
    }
}
