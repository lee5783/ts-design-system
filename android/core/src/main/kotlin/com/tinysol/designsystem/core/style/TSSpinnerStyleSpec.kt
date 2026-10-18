package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSTokenValues

public enum class TSSpinnerSize(
    public val iconSize: TSIconSize,
) {
    Sm(TSIconSize.Sm),
    Md(TSIconSize.Md),
    Lg(TSIconSize.Lg),
}

public enum class TSSpinnerTone {
    Primary,
    Secondary,
    Accent,
}

public object TSSpinnerStyleSpec : TSStyleSpec<TSSpinnerStyleSpec.Configuration, TSSpinnerStyleSpec.Resolved> {
    public const val START_ANGLE_DEGREES: Float = 45f
    public const val SWEEP_ANGLE_DEGREES: Float = 270f

    public data class Configuration(
        public val size: TSSpinnerSize = TSSpinnerSize.Md,
        public val tone: TSSpinnerTone = TSSpinnerTone.Primary,
    )

    public data class Resolved(
        public val sizeDp: Float,
        public val strokeWidthDp: Float,
        public val color: TSColorRole,
        public val rotationMillis: Int,
    )

    public val configurations: List<Configuration> = TSSpinnerSize.entries.flatMap { size ->
        TSSpinnerTone.entries.map { Configuration(size, it) }
    }

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val sizeDp = configuration.size.iconSize.dp
        return Resolved(
            sizeDp = sizeDp,
            strokeWidthDp = strokeWidth(sizeDp),
            color = color(configuration.tone),
            rotationMillis = TSTokenValues.Component.spinnerRotationDuration,
        )
    }

    public fun strokeWidth(sizeDp: Float): Float =
        sizeDp * TSTokenValues.Component.spinnerStrokeWidth / TSTokenValues.Component.spinnerSize

    public fun color(tone: TSSpinnerTone): TSColorRole = when (tone) {
        TSSpinnerTone.Primary -> TSColorRole.TextPrimary
        TSSpinnerTone.Secondary -> TSColorRole.TextSecondary
        TSSpinnerTone.Accent -> TSColorRole.AccentPrimary
    }
}
