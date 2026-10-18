package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSTokenValues

public enum class TSBadgeTone {
    Neutral,
    Accent,
    Success,
    Warning,
    Danger,
    Info,
}

public object TSBadgeStyleSpec : TSStyleSpec<TSBadgeStyleSpec.Configuration, TSBadgeStyleSpec.Resolved> {
    public data class Configuration(
        public val tone: TSBadgeTone = TSBadgeTone.Neutral,
        public val hasDot: Boolean = false,
        public val hasIcon: Boolean = false,
    )

    public data class Resolved(
        public val fill: TSColorRole,
        public val foreground: TSColorRole,
        public val border: TSColorRole?,
        public val borderWidthDp: Float,
        public val typeRole: TSTypeRole,
        public val fontWeight: Int,
        public val maxLines: Int,
        public val minHeightDp: Float,
        public val horizontalPaddingDp: Float,
        public val verticalPaddingDp: Float,
        public val spacingDp: Float,
        public val cornerRadiusDp: Float,
        public val showsDot: Boolean,
        public val dotSizeDp: Float,
        public val showsIcon: Boolean,
        public val iconSizeDp: Float,
    )

    public val configurations: List<Configuration> = TSBadgeTone.entries.flatMap { tone ->
        listOf(false, true).flatMap { hasDot ->
            listOf(false, true).map { Configuration(tone, hasDot, it) }
        }
    }

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val isNeutral = configuration.tone == TSBadgeTone.Neutral
        return Resolved(
            fill = fill(configuration.tone),
            foreground = foreground(configuration.tone),
            border = if (isNeutral) TSColorRole.BorderSubtle else null,
            borderWidthDp = if (isNeutral) 1f else 0f,
            typeRole = TSTypeRole.Caption,
            fontWeight = FONT_WEIGHT,
            maxLines = 1,
            minHeightDp = TSTokenValues.Component.badgeHeight,
            horizontalPaddingDp = TSTokenValues.Spacing.space2,
            verticalPaddingDp = TSTokenValues.Spacing.space0_5,
            spacingDp = TSTokenValues.Spacing.space1,
            cornerRadiusDp = TSTokenValues.Radius.radiusSm,
            showsDot = configuration.hasDot,
            dotSizeDp = TSTokenValues.Component.badgeDotSize,
            showsIcon = configuration.hasIcon,
            iconSizeDp = TSIconSize.Sm.dp,
        )
    }

    public fun fill(tone: TSBadgeTone): TSColorRole = when (tone) {
        TSBadgeTone.Neutral -> TSColorRole.BackgroundSecondary
        TSBadgeTone.Accent -> TSColorRole.AccentSubtle
        TSBadgeTone.Success -> TSColorRole.SuccessSubtle
        TSBadgeTone.Warning -> TSColorRole.WarningSubtle
        TSBadgeTone.Danger -> TSColorRole.DangerSubtle
        TSBadgeTone.Info -> TSColorRole.InfoSubtle
    }

    public fun foreground(tone: TSBadgeTone): TSColorRole = when (tone) {
        TSBadgeTone.Neutral -> TSColorRole.TextSecondary
        TSBadgeTone.Accent -> TSColorRole.AccentText
        TSBadgeTone.Success -> TSColorRole.Success
        TSBadgeTone.Warning -> TSColorRole.Warning
        TSBadgeTone.Danger -> TSColorRole.Danger
        TSBadgeTone.Info -> TSColorRole.Info
    }

    private const val FONT_WEIGHT = 500
}
