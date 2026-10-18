package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSCubicBezierValues
import com.tinysol.designsystem.tokens.TSTokenValues

public object TSSwitchStyleSpec : TSStyleSpec<TSSwitchStyleSpec.Configuration, TSSwitchStyleSpec.Resolved> {
    public const val MIN_TOUCH_TARGET_DP: Float = 48f

    public data class Configuration(
        public val checked: Boolean = false,
    )

    public data class Resolved(
        public val trackWidthDp: Float,
        public val trackHeightDp: Float,
        public val trackColor: TSColorRole,
        public val trackOpacity: Float,
        public val thumbSizeDp: Float,
        public val thumbInsetDp: Float,
        public val thumbStartOffsetDp: Float,
        public val thumbColor: TSColorRole,
        public val thumbShadow: TSShadowRole,
        public val labelTypeRole: TSTypeRole,
        public val labelColor: TSColorRole,
        public val spacingDp: Float,
        public val minTouchTargetDp: Float,
        public val opacity: Float,
        public val showsFocusRing: Boolean,
        public val animationDurationMillis: Int,
        public val animationEasing: TSCubicBezierValues,
    ) {
        public val trackCornerRadiusDp: Float
            get() = trackHeightDp / 2f

        public fun thumbLeftDp(rightToLeft: Boolean): Float =
            if (rightToLeft) trackWidthDp - thumbStartOffsetDp - thumbSizeDp else thumbStartOffsetDp
    }

    public val states: List<TSControlState> = listOf(
        TSControlState.Default,
        TSControlState.Pressed,
        TSControlState.Disabled,
        TSControlState.Focused,
    )

    public val configurations: List<Configuration> = listOf(Configuration(checked = false), Configuration(checked = true))

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val checked = configuration.checked
        val isPressed = state.pressed && state.interactive
        val inset = TSTokenValues.Component.switchThumbInset
        return Resolved(
            trackWidthDp = TSTokenValues.Component.switchTrackWidth,
            trackHeightDp = TSTokenValues.Component.switchTrackHeight,
            trackColor = when {
                !checked -> TSColorRole.BorderStrong
                isPressed -> TSColorRole.AccentPressed
                else -> TSColorRole.AccentPrimary
            },
            trackOpacity = if (isPressed && !checked) TSTokenValues.Opacity.opacityPressed else 1f,
            thumbSizeDp = TSTokenValues.Component.switchThumbSize,
            thumbInsetDp = inset,
            thumbStartOffsetDp = if (checked) inset + TSTokenValues.Component.switchThumbTravel else inset,
            thumbColor = TSColorRole.SwitchThumb,
            thumbShadow = TSShadowRole.Sm,
            labelTypeRole = TSTypeRole.Body,
            labelColor = TSColorRole.TextPrimary,
            spacingDp = TSTokenValues.Spacing.space2,
            minTouchTargetDp = MIN_TOUCH_TARGET_DP,
            opacity = if (state.dimmed) TSTokenValues.Opacity.opacityDisabled else 1f,
            showsFocusRing = state.focused && state.interactive,
            animationDurationMillis = TSTokenValues.Duration.durationBase,
            animationEasing = TSTokenValues.Easing.easingStandard,
        )
    }
}
