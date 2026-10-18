package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.tokens.TSTokenValues

public enum class TSAlertTone {
    Info,
    Success,
    Warning,
    Danger,
}

public object TSAlertStyleSpec : TSStyleSpec<TSAlertStyleSpec.Configuration, TSAlertStyleSpec.Resolved> {
    public const val MIN_TOUCH_TARGET_DP: Float = 48f

    public data class Configuration(
        public val tone: TSAlertTone = TSAlertTone.Info,
        public val hasTitle: Boolean = true,
        public val isDismissible: Boolean = false,
    )

    public data class Resolved(
        public val fill: TSColorRole,
        public val borderWidthDp: Float,
        public val cornerRadiusDp: Float,
        public val horizontalPaddingDp: Float,
        public val verticalPaddingDp: Float,
        public val spacingDp: Float,
        public val icon: TSIcon,
        public val iconSize: TSIconSize,
        public val iconColor: TSColorRole,
        public val showsTitle: Boolean,
        public val titleTypeRole: TSTypeRole,
        public val titleFontWeight: Int,
        public val titleColor: TSColorRole,
        public val messageTypeRole: TSTypeRole,
        public val messageColor: TSColorRole,
        public val showsDismiss: Boolean,
        public val dismissIcon: TSIcon,
        public val dismissIconSize: TSIconSize,
        public val dismissColor: TSColorRole,
        public val dismissPressedOpacity: Float,
        public val minTouchTargetDp: Float,
    ) {
        public val contentHorizontalInsetDp: Float
            get() = horizontalPaddingDp + borderWidthDp

        public val contentVerticalInsetDp: Float
            get() = verticalPaddingDp + borderWidthDp
    }

    public val configurations: List<Configuration> = TSAlertTone.entries.flatMap { tone ->
        listOf(false, true).flatMap { hasTitle ->
            listOf(false, true).map { Configuration(tone, hasTitle, it) }
        }
    }

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val tone = configuration.tone
        return Resolved(
            fill = fill(tone),
            borderWidthDp = 1f,
            cornerRadiusDp = TSTokenValues.Radius.radiusLg,
            horizontalPaddingDp = TSTokenValues.Spacing.space4,
            verticalPaddingDp = TSTokenValues.Spacing.space3,
            spacingDp = TSTokenValues.Spacing.space3,
            icon = icon(tone),
            iconSize = TSIconSize.Md,
            iconColor = iconColor(tone),
            showsTitle = configuration.hasTitle,
            titleTypeRole = TSTypeRole.BodySm,
            titleFontWeight = TSTokenValues.Component.alertTitleFontWeight,
            titleColor = TSColorRole.TextPrimary,
            messageTypeRole = TSTypeRole.BodySm,
            messageColor = TSColorRole.TextSecondary,
            showsDismiss = configuration.isDismissible,
            dismissIcon = TSIcon.Close,
            dismissIconSize = TSIconSize.Md,
            dismissColor = TSColorRole.TextSecondary,
            dismissPressedOpacity = TSTokenValues.Opacity.opacityPressed,
            minTouchTargetDp = MIN_TOUCH_TARGET_DP,
        )
    }

    public fun icon(tone: TSAlertTone): TSIcon = when (tone) {
        TSAlertTone.Info -> TSIcon.Info
        TSAlertTone.Success -> TSIcon.CheckCircle
        TSAlertTone.Warning -> TSIcon.Alert
        TSAlertTone.Danger -> TSIcon.XCircle
    }

    public fun fill(tone: TSAlertTone): TSColorRole = when (tone) {
        TSAlertTone.Info -> TSColorRole.InfoSubtle
        TSAlertTone.Success -> TSColorRole.SuccessSubtle
        TSAlertTone.Warning -> TSColorRole.WarningSubtle
        TSAlertTone.Danger -> TSColorRole.DangerSubtle
    }

    public fun iconColor(tone: TSAlertTone): TSColorRole = when (tone) {
        TSAlertTone.Info -> TSColorRole.Info
        TSAlertTone.Success -> TSColorRole.Success
        TSAlertTone.Warning -> TSColorRole.Warning
        TSAlertTone.Danger -> TSColorRole.Danger
    }
}
