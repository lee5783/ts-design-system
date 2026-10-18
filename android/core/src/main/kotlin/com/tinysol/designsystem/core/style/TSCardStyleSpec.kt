package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSTokenValues

public object TSCardStyleSpec : TSStyleSpec<TSCardStyleSpec.Configuration, TSCardStyleSpec.Resolved> {
    public const val STACK_SPACING_DP: Float = TSTokenValues.Spacing.space4
    public const val STACKED_FOOTER_FONT_SCALE: Float = TSListItemLayout.STACKED_FONT_SCALE

    public data class Configuration(
        public val isRaised: Boolean = false,
        public val isInteractive: Boolean = false,
    )

    public data class Resolved(
        public val fill: TSColorRole,
        public val border: TSColorRole,
        public val borderWidthDp: Float,
        public val cornerRadiusDp: Float,
        public val shadow: TSShadowRole?,
        public val showsFocusRing: Boolean,
        public val opacity: Float,
        public val titleTypeRole: TSTypeRole,
        public val titleColor: TSColorRole,
        public val subtitleTypeRole: TSTypeRole,
        public val subtitleColor: TSColorRole,
        public val subtitleSpacingDp: Float,
        public val bodyTypeRole: TSTypeRole,
        public val bodyColor: TSColorRole,
        public val paddingDp: Float,
        public val headerSpacingDp: Float,
        public val footerVerticalPaddingDp: Float,
        public val footerHorizontalPaddingDp: Float,
        public val footerSpacingDp: Float,
        public val footerButtonVariant: TSButtonVariant,
        public val footerButtonSize: TSButtonSize,
        public val divider: TSColorRole,
        public val dividerThicknessDp: Float,
    )

    public val states: List<TSControlState> = listOf(
        TSControlState.Default,
        TSControlState.Pressed,
        TSControlState.Disabled,
        TSControlState.Focused,
    )

    public val configurations: List<Configuration> = listOf(false, true).flatMap { isRaised ->
        listOf(false, true).map { Configuration(isRaised, it) }
    }

    public fun stacksFooter(fontScale: Float): Boolean = fontScale >= STACKED_FOOTER_FONT_SCALE

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val reactive = configuration.isInteractive && state.interactive
        return Resolved(
            fill = if (reactive && state.pressed) TSColorRole.SurfacePressed else TSColorRole.SurfaceCard,
            border = TSColorRole.BorderSubtle,
            borderWidthDp = 1f,
            cornerRadiusDp = TSTokenValues.Radius.radiusLg,
            shadow = if (configuration.isRaised) TSShadowRole.Sm else null,
            showsFocusRing = reactive && state.focused,
            opacity = if (state.dimmed) TSTokenValues.Opacity.opacityDisabled else 1f,
            titleTypeRole = TSTypeRole.Title3,
            titleColor = TSColorRole.TextPrimary,
            subtitleTypeRole = TSTypeRole.BodySm,
            subtitleColor = TSColorRole.TextMuted,
            subtitleSpacingDp = TSTokenValues.Component.cardSubtitleSpacing,
            bodyTypeRole = TSTypeRole.BodySm,
            bodyColor = TSColorRole.TextSecondary,
            paddingDp = TSTokenValues.Spacing.space4,
            headerSpacingDp = TSTokenValues.Spacing.space3,
            footerVerticalPaddingDp = TSTokenValues.Spacing.space3,
            footerHorizontalPaddingDp = TSTokenValues.Spacing.space4,
            footerSpacingDp = TSTokenValues.Spacing.space2,
            footerButtonVariant = TSButtonVariant.Secondary,
            footerButtonSize = TSButtonSize.Md,
            divider = TSColorRole.BorderSubtle,
            dividerThicknessDp = 1f,
        )
    }
}
