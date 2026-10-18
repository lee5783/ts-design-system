package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSTokenValues

public enum class TSIconButtonVariant {
    Ghost,
    Secondary,
    Primary,
}

public object TSIconButtonStyleSpec : TSStyleSpec<TSIconButtonStyleSpec.Configuration, TSIconButtonStyleSpec.Resolved> {
    public data class Configuration(
        public val variant: TSIconButtonVariant = TSIconButtonVariant.Ghost,
        public val round: Boolean = false,
    )

    public data class Resolved(
        public val fill: TSColorRole?,
        public val icon: TSColorRole,
        public val border: TSColorRole?,
        public val borderWidthDp: Float,
        public val sizeDp: Float,
        public val cornerRadiusDp: Float,
        public val iconSizeDp: Float,
        public val opacity: Float,
        public val showsFocusRing: Boolean,
        public val minTouchTargetDp: Float,
    )

    public val states: List<TSControlState> = listOf(
        TSControlState.Default,
        TSControlState.Pressed,
        TSControlState.Disabled,
        TSControlState.Focused,
    )

    public val configurations: List<Configuration> = TSIconButtonVariant.entries.flatMap { variant ->
        listOf(false, true).map { Configuration(variant, it) }
    }

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val variant = configuration.variant
        val isPressed = state.pressed && state.interactive
        val size = TSTokenValues.Size.controlMd
        return Resolved(
            fill = fill(variant, isPressed),
            icon = if (variant == TSIconButtonVariant.Primary) TSColorRole.TextOnAccent else TSColorRole.TextPrimary,
            border = if (variant == TSIconButtonVariant.Secondary) TSColorRole.BorderStrong else null,
            borderWidthDp = if (variant == TSIconButtonVariant.Secondary) 1f else 0f,
            sizeDp = size,
            cornerRadiusDp = if (configuration.round) size / 2f else TSTokenValues.Radius.radiusMd,
            iconSizeDp = TSIconSize.Lg.dp,
            opacity = if (state.dimmed) TSTokenValues.Opacity.opacityDisabled else 1f,
            showsFocusRing = state.focused && state.interactive,
            minTouchTargetDp = TSButtonStyleSpec.MIN_TOUCH_TARGET_DP,
        )
    }

    private fun fill(variant: TSIconButtonVariant, isPressed: Boolean): TSColorRole? = when (variant) {
        TSIconButtonVariant.Ghost -> if (isPressed) TSColorRole.SurfacePressed else null
        TSIconButtonVariant.Secondary -> if (isPressed) TSColorRole.SurfacePressed else TSColorRole.SurfaceCard
        TSIconButtonVariant.Primary -> if (isPressed) TSColorRole.AccentPressed else TSColorRole.AccentPrimary
    }
}
