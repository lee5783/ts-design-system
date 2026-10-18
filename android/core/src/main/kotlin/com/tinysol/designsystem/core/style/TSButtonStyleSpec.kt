package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSTokenValues

public enum class TSButtonVariant {
    Primary,
    Secondary,
    Ghost,
    Danger,
    Link,
}

public enum class TSButtonSize {
    Md,
    Lg,
}

public object TSButtonStyleSpec : TSStyleSpec<TSButtonStyleSpec.Configuration, TSButtonStyleSpec.Resolved> {
    public const val MIN_TOUCH_TARGET_DP: Float = 48f

    public data class Configuration(
        public val variant: TSButtonVariant = TSButtonVariant.Primary,
        public val size: TSButtonSize = TSButtonSize.Md,
    )

    public data class Resolved(
        public val fill: TSColorRole?,
        public val label: TSColorRole,
        public val border: TSColorRole?,
        public val borderWidthDp: Float,
        public val shadow: TSShadowRole?,
        public val typeRole: TSTypeRole,
        public val fontWeight: Int,
        public val minHeightDp: Float,
        public val horizontalPaddingDp: Float,
        public val verticalPaddingDp: Float,
        public val cornerRadiusDp: Float,
        public val iconSizeDp: Float,
        public val iconSpacingDp: Float,
        public val spinnerSizeDp: Float,
        public val spinnerStrokeWidthDp: Float,
        public val spinnerRotationMillis: Int,
        public val opacity: Float,
        public val underlined: Boolean,
        public val showsSpinner: Boolean,
        public val showsFocusRing: Boolean,
        public val minTouchTargetDp: Float,
    )

    public val states: List<TSControlState> = listOf(
        TSControlState.Default,
        TSControlState.Pressed,
        TSControlState.Disabled,
        TSControlState.Loading,
        TSControlState.Focused,
    )

    public val configurations: List<Configuration> = TSButtonVariant.entries.flatMap { variant ->
        TSButtonSize.entries.map { Configuration(variant, it) }
    }

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val variant = configuration.variant
        val isLink = variant == TSButtonVariant.Link
        val isPressed = state.pressed && state.interactive
        val isSecondary = variant == TSButtonVariant.Secondary
        return Resolved(
            fill = fill(variant, isPressed),
            label = label(variant),
            border = if (isSecondary) TSColorRole.BorderStrong else null,
            borderWidthDp = if (isSecondary) 1f else 0f,
            shadow = if (isSecondary) TSShadowRole.Xs else null,
            typeRole = typeRole(configuration.size),
            fontWeight = TSTypeRole.Label.values.fontWeight,
            minHeightDp = if (isLink) 0f else minHeight(configuration.size),
            horizontalPaddingDp = if (isLink) 0f else horizontalPadding(configuration.size),
            verticalPaddingDp = if (isLink) 0f else TSTokenValues.Spacing.space2,
            cornerRadiusDp = if (isLink) 0f else TSTokenValues.Radius.radiusMd,
            iconSizeDp = TSIconSize.Md.dp,
            iconSpacingDp = TSTokenValues.Spacing.space2,
            spinnerSizeDp = TSTokenValues.Component.spinnerSize,
            spinnerStrokeWidthDp = TSTokenValues.Component.spinnerStrokeWidth,
            spinnerRotationMillis = TSTokenValues.Component.spinnerRotationDuration,
            opacity = opacity(isLink, state, isPressed),
            underlined = isLink,
            showsSpinner = state.loading,
            showsFocusRing = state.focused && state.interactive,
            minTouchTargetDp = MIN_TOUCH_TARGET_DP,
        )
    }

    public fun typeRole(size: TSButtonSize): TSTypeRole = when (size) {
        TSButtonSize.Md -> TSTypeRole.Label
        TSButtonSize.Lg -> TSTypeRole.Body
    }

    public fun minHeight(size: TSButtonSize): Float = when (size) {
        TSButtonSize.Md -> TSTokenValues.Size.controlMd
        TSButtonSize.Lg -> TSTokenValues.Size.controlLg
    }

    public fun horizontalPadding(size: TSButtonSize): Float = when (size) {
        TSButtonSize.Md -> TSTokenValues.Spacing.space4
        TSButtonSize.Lg -> TSTokenValues.Spacing.space5
    }

    private fun fill(variant: TSButtonVariant, isPressed: Boolean): TSColorRole? = when (variant) {
        TSButtonVariant.Primary -> if (isPressed) TSColorRole.AccentPressed else TSColorRole.AccentPrimary
        TSButtonVariant.Secondary -> if (isPressed) TSColorRole.SurfacePressed else TSColorRole.SurfaceCard
        TSButtonVariant.Ghost -> if (isPressed) TSColorRole.SurfacePressed else null
        TSButtonVariant.Danger -> if (isPressed) TSColorRole.DangerPressed else TSColorRole.DangerSolid
        TSButtonVariant.Link -> null
    }

    private fun label(variant: TSButtonVariant): TSColorRole = when (variant) {
        TSButtonVariant.Primary, TSButtonVariant.Danger -> TSColorRole.TextOnAccent
        TSButtonVariant.Secondary, TSButtonVariant.Ghost -> TSColorRole.TextPrimary
        TSButtonVariant.Link -> TSColorRole.AccentText
    }

    private fun opacity(isLink: Boolean, state: TSControlState, isPressed: Boolean): Float = when {
        state.dimmed -> TSTokenValues.Opacity.opacityDisabled
        isLink && isPressed -> TSTokenValues.Opacity.opacityPressed
        else -> 1f
    }
}
