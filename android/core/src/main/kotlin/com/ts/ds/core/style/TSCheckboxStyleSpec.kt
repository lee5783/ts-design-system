package com.ts.ds.core.style

import com.ts.ds.core.icon.TSIcon
import com.ts.ds.tokens.TSTokenValues

public object TSCheckboxStyleSpec : TSStyleSpec<TSCheckboxStyleSpec.Configuration, TSCheckboxStyleSpec.Resolved> {
    public const val MIN_TOUCH_TARGET_DP: Float = 48f

    public data class Configuration(
        public val checked: Boolean = false,
    )

    public data class Resolved(
        public val markSizeDp: Float,
        public val markCornerRadiusDp: Float,
        public val markFill: TSColorRole,
        public val markBorder: TSColorRole,
        public val markBorderWidthDp: Float,
        public val showsCheck: Boolean,
        public val checkIcon: TSIcon,
        public val checkColor: TSColorRole,
        public val checkSizeDp: Float,
        public val labelTypeRole: TSTypeRole,
        public val labelColor: TSColorRole,
        public val spacingDp: Float,
        public val minTouchTargetDp: Float,
        public val opacity: Float,
        public val showsFocusRing: Boolean,
    )

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
        val accent = if (isPressed) TSColorRole.AccentPressed else TSColorRole.AccentPrimary
        return Resolved(
            markSizeDp = TSTokenValues.Component.checkboxSize,
            markCornerRadiusDp = TSTokenValues.Radius.radiusXs,
            markFill = when {
                checked -> accent
                isPressed -> TSColorRole.SurfacePressed
                else -> TSColorRole.SurfaceCard
            },
            markBorder = if (checked) accent else TSColorRole.BorderStrong,
            markBorderWidthDp = TSTokenValues.Component.checkboxBorderWidth,
            showsCheck = checked,
            checkIcon = TSIcon.Check,
            checkColor = TSColorRole.TextOnAccent,
            checkSizeDp = TSIconSize.Sm.dp,
            labelTypeRole = TSTypeRole.Body,
            labelColor = TSColorRole.TextPrimary,
            spacingDp = TSTokenValues.Spacing.space2,
            minTouchTargetDp = MIN_TOUCH_TARGET_DP,
            opacity = if (state.dimmed) TSTokenValues.Opacity.opacityDisabled else 1f,
            showsFocusRing = state.focused && state.interactive,
        )
    }
}
