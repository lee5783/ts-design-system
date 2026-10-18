package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.core.haptics.DsHapticEvent
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.logic.TSToastTone
import com.tinysol.designsystem.tokens.TSTokenValues

public object TSToastStyleSpec : TSStyleSpec<TSToastStyleSpec.Configuration, TSToastStyleSpec.Resolved> {
    public const val MIN_TOUCH_TARGET_DP: Float = 48f

    public data class Configuration(
        public val tone: TSToastTone = TSToastTone.Neutral,
        public val hasAction: Boolean = false,
    )

    public data class Resolved(
        public val fill: TSColorRole,
        public val cornerRadiusDp: Float,
        public val shadow: TSShadowRole,
        public val maxWidthDp: Float,
        public val horizontalPaddingDp: Float,
        public val verticalPaddingDp: Float,
        public val spacingDp: Float,
        public val outerMarginDp: Float,
        public val icon: TSIcon?,
        public val iconSize: TSIconSize,
        public val iconColor: TSColorRole?,
        public val messageTypeRole: TSTypeRole,
        public val messageColor: TSColorRole,
        public val showsAction: Boolean,
        public val actionTypeRole: TSTypeRole,
        public val actionFontWeight: Int,
        public val actionColor: TSColorRole,
        public val actionIsUnderlined: Boolean,
        public val actionHorizontalPaddingDp: Float,
        public val actionVerticalPaddingDp: Float,
        public val actionPressedOpacity: Float,
        public val minTouchTargetDp: Float,
        public val enterDurationMillis: Int,
        public val exitDurationMillis: Int,
    )

    public val configurations: List<Configuration> = TSToastTone.entries.flatMap { tone ->
        listOf(false, true).map { Configuration(tone, it) }
    }

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val tone = configuration.tone
        return Resolved(
            fill = TSColorRole.SurfaceInverse,
            cornerRadiusDp = TSTokenValues.Radius.radiusLg,
            shadow = TSShadowRole.Lg,
            maxWidthDp = TSTokenValues.Component.toastMaxWidth,
            horizontalPaddingDp = TSTokenValues.Spacing.space4,
            verticalPaddingDp = TSTokenValues.Spacing.space3,
            spacingDp = TSTokenValues.Spacing.space3,
            outerMarginDp = TSTokenValues.Spacing.space4,
            icon = icon(tone),
            iconSize = TSIconSize.Md,
            iconColor = iconColor(tone),
            messageTypeRole = TSTypeRole.BodySm,
            messageColor = TSColorRole.TextInverse,
            showsAction = configuration.hasAction,
            actionTypeRole = TSTypeRole.BodySm,
            actionFontWeight = TSTokenValues.Component.toastActionFontWeight,
            actionColor = TSColorRole.TextInverse,
            actionIsUnderlined = true,
            actionHorizontalPaddingDp = TSTokenValues.Spacing.space2,
            actionVerticalPaddingDp = TSTokenValues.Spacing.space1,
            actionPressedOpacity = TSTokenValues.Opacity.opacityPressed,
            minTouchTargetDp = MIN_TOUCH_TARGET_DP,
            enterDurationMillis = TSTokenValues.Duration.durationBase,
            exitDurationMillis = TSTokenValues.Duration.durationFast,
        )
    }

    public fun icon(tone: TSToastTone): TSIcon? = when (tone) {
        TSToastTone.Neutral -> null
        TSToastTone.Info -> TSIcon.Info
        TSToastTone.Success -> TSIcon.CheckCircle
        TSToastTone.Warning -> TSIcon.Alert
        TSToastTone.Danger -> TSIcon.XCircle
    }

    public fun iconColor(tone: TSToastTone): TSColorRole? = when (tone) {
        TSToastTone.Neutral -> null
        TSToastTone.Info -> TSColorRole.InfoOnInverse
        TSToastTone.Success -> TSColorRole.SuccessOnInverse
        TSToastTone.Warning -> TSColorRole.WarningOnInverse
        TSToastTone.Danger -> TSColorRole.DangerOnInverse
    }

    public fun hapticEvent(tone: TSToastTone): DsHapticEvent? = when (tone) {
        TSToastTone.Neutral, TSToastTone.Info -> null
        TSToastTone.Success -> DsHapticEvent.Confirm
        TSToastTone.Warning, TSToastTone.Danger -> DsHapticEvent.Reject
    }
}
