package com.ts.ds.core.style

import com.ts.ds.core.icon.TSIcon
import com.ts.ds.tokens.TSTokenValues

public object TSTextFieldStyleSpec : TSStyleSpec<TSTextFieldStyleSpec.Configuration, TSTextFieldStyleSpec.Resolved> {
    public const val MIN_TOUCH_TARGET_DP: Float = 48f
    public const val MINIMUM_LINES: Int = 3
    public const val MAXIMUM_LINES: Int = 6
    public const val UNFOCUSED_BORDER_WIDTH_DP: Float = 1f
    public const val FOCUSED_BORDER_WIDTH_DP: Float = 2f
    public const val MASK_CHARACTER: Char = '•'

    public data class Configuration(
        public val readOnly: Boolean = false,
    )

    public data class Resolved(
        public val fill: TSColorRole,
        public val border: TSColorRole,
        public val borderWidthDp: Float,
        public val shadow: TSShadowRole?,
        public val labelTypeRole: TSTypeRole,
        public val labelFontWeight: Int,
        public val labelColor: TSColorRole,
        public val inputTypeRole: TSTypeRole,
        public val inputColor: TSColorRole,
        public val placeholderColor: TSColorRole,
        public val messageTypeRole: TSTypeRole,
        public val messageColor: TSColorRole,
        public val showsErrorIcon: Boolean,
        public val messageIconSizeDp: Float,
        public val leadingIconSizeDp: Float,
        public val leadingIconColor: TSColorRole,
        public val minHeightDp: Float,
        public val horizontalPaddingDp: Float,
        public val verticalPaddingDp: Float,
        public val spacingDp: Float,
        public val labelSpacingDp: Float,
        public val messageSpacingDp: Float,
        public val cornerRadiusDp: Float,
        public val opacity: Float,
        public val showsFocusRing: Boolean,
        public val editable: Boolean,
        public val selectable: Boolean,
    )

    public data class PasswordToggle(
        public val icon: TSIcon,
        public val iconColor: TSColorRole,
        public val fill: TSColorRole?,
        public val sizeDp: Float,
        public val iconSizeDp: Float,
        public val cornerRadiusDp: Float,
        public val minTouchTargetDp: Float,
        public val selected: Boolean,
    )

    public val states: List<TSControlState> = listOf(
        TSControlState.Default,
        TSControlState.Focused,
        TSControlState(error = true),
        TSControlState(focused = true, error = true),
        TSControlState.Disabled,
    )

    public val configurations: List<Configuration> = listOf(Configuration(readOnly = false), Configuration(readOnly = true))

    public fun state(enabled: Boolean = true, focused: Boolean = false, error: Boolean = false): TSControlState =
        TSControlState.make(enabled = enabled, focused = focused).copy(error = error)

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val disabled = !state.enabled
        val readOnly = configuration.readOnly
        val showsFocusRing = state.focused && !disabled && !readOnly
        return Resolved(
            fill = if (disabled || readOnly) TSColorRole.BackgroundSecondary else TSColorRole.SurfaceCard,
            border = border(state.error, showsFocusRing),
            borderWidthDp = if (showsFocusRing) FOCUSED_BORDER_WIDTH_DP else UNFOCUSED_BORDER_WIDTH_DP,
            shadow = if (readOnly) null else TSShadowRole.Xs,
            labelTypeRole = TSTypeRole.Label,
            labelFontWeight = TSTypeRole.Label.values.fontWeight,
            labelColor = TSColorRole.TextPrimary,
            inputTypeRole = TSTypeRole.Body,
            inputColor = TSColorRole.TextPrimary,
            placeholderColor = TSColorRole.TextMuted,
            messageTypeRole = TSTypeRole.Caption,
            messageColor = if (state.error) TSColorRole.Danger else TSColorRole.TextMuted,
            showsErrorIcon = state.error,
            messageIconSizeDp = TSIconSize.Sm.dp,
            leadingIconSizeDp = TSIconSize.Md.dp,
            leadingIconColor = TSColorRole.TextMuted,
            minHeightDp = TSTokenValues.Size.controlMd,
            horizontalPaddingDp = TSTokenValues.Spacing.space3,
            verticalPaddingDp = TSTokenValues.Component.textFieldPaddingVertical,
            spacingDp = TSTokenValues.Spacing.space2,
            labelSpacingDp = TSTokenValues.Spacing.space1,
            messageSpacingDp = TSTokenValues.Spacing.space1,
            cornerRadiusDp = TSTokenValues.Radius.radiusMd,
            opacity = if (disabled) TSTokenValues.Opacity.opacityDisabled else 1f,
            showsFocusRing = showsFocusRing,
            editable = !disabled && !readOnly,
            selectable = !disabled,
        )
    }

    public fun passwordToggle(revealed: Boolean): PasswordToggle = PasswordToggle(
        icon = TSIcon.Eye,
        iconColor = if (revealed) TSColorRole.AccentText else TSColorRole.TextMuted,
        fill = if (revealed) TSColorRole.AccentSubtle else null,
        sizeDp = TSTokenValues.Size.controlSm,
        iconSizeDp = TSIconSize.Md.dp,
        cornerRadiusDp = TSTokenValues.Radius.radiusSm,
        minTouchTargetDp = MIN_TOUCH_TARGET_DP,
        selected = revealed,
    )

    public fun maskedText(text: String, secure: Boolean): String =
        if (secure) MASK_CHARACTER.toString().repeat(text.length) else text

    private fun border(error: Boolean, showsFocusRing: Boolean): TSColorRole = when {
        error -> TSColorRole.Danger
        showsFocusRing -> TSColorRole.Focus
        else -> TSColorRole.BorderStrong
    }
}
