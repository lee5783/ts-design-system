package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSCubicBezierValues
import com.tinysol.designsystem.tokens.TSTokenValues

public enum class TSTabsVariant {
    Underline,
    Segmented,
}

public data class TSTabItem(
    public val id: String,
    public val label: String,
)

public object TSTabsStyleSpec : TSStyleSpec<TSTabsStyleSpec.Configuration, TSTabsStyleSpec.Resolved> {
    public const val MIN_TOUCH_TARGET_DP: Float = 48f
    public const val SEGMENTED_HEIGHT_DP: Float = 46f
    public const val BORDER_WIDTH_DP: Float = 1f
    public const val UNDERLINE_HEIGHT_DP: Float = 44f

    public data class Configuration(
        public val variant: TSTabsVariant = TSTabsVariant.Underline,
        public val selected: Boolean = false,
    )

    public data class Resolved(
        public val labelTypeRole: TSTypeRole,
        public val labelColor: TSColorRole,
        public val fill: TSColorRole?,
        public val shadow: TSShadowRole?,
        public val indicatorColor: TSColorRole?,
        public val indicatorHeightDp: Float,
        public val cornerRadiusDp: Float,
        public val paddingVerticalDp: Float,
        public val paddingHorizontalDp: Float,
        public val minHeightDp: Float,
        public val minTouchTargetDp: Float,
        public val opacity: Float,
        public val showsFocusRing: Boolean,
        public val animationDurationMillis: Int,
        public val animationEasing: TSCubicBezierValues,
    ) {
        public val touchOutsetDp: Float
            get() = ((minTouchTargetDp - minHeightDp) / 2f).coerceAtLeast(0f)
    }

    public data class Container(
        public val fill: TSColorRole?,
        public val border: TSColorRole,
        public val borderWidthDp: Float,
        public val cornerRadiusDp: Float,
        public val paddingDp: Float,
        public val spacingDp: Float,
        public val minHeightDp: Float,
        public val minTouchTargetDp: Float,
        public val scrollable: Boolean,
        public val opacity: Float,
    ) {
        public val touchOutsetDp: Float
            get() = ((minTouchTargetDp - minHeightDp) / 2f).coerceAtLeast(0f)

        public val contentInsetDp: Float
            get() = paddingDp + if (fill != null) borderWidthDp else 0f
    }

    public val states: List<TSControlState> = listOf(
        TSControlState.Default,
        TSControlState.Pressed,
        TSControlState.Disabled,
        TSControlState.Focused,
    )

    public val configurations: List<Configuration> = TSTabsVariant.entries.flatMap { variant ->
        listOf(false, true).map { Configuration(variant, it) }
    }

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val selected = configuration.selected
        val isPressed = state.pressed && state.interactive
        val opacity = if (state.dimmed) TSTokenValues.Opacity.opacityDisabled else 1f
        val showsFocusRing = state.focused && state.interactive
        return when (configuration.variant) {
            TSTabsVariant.Underline -> Resolved(
                labelTypeRole = TSTypeRole.Label,
                labelColor = when {
                    selected -> TSColorRole.AccentText
                    isPressed -> TSColorRole.TextPrimary
                    else -> TSColorRole.TextMuted
                },
                fill = null,
                shadow = null,
                indicatorColor = if (selected) TSColorRole.AccentText else null,
                indicatorHeightDp = 2f,
                cornerRadiusDp = TSTokenValues.Radius.radiusSm,
                paddingVerticalDp = TSTokenValues.Spacing.space3,
                paddingHorizontalDp = 0f,
                minHeightDp = UNDERLINE_HEIGHT_DP,
                minTouchTargetDp = MIN_TOUCH_TARGET_DP,
                opacity = opacity,
                showsFocusRing = showsFocusRing,
                animationDurationMillis = TSTokenValues.Duration.durationBase,
                animationEasing = TSTokenValues.Easing.easingStandard,
            )
            TSTabsVariant.Segmented -> Resolved(
                labelTypeRole = TSTypeRole.Label,
                labelColor = if (selected) TSColorRole.TextPrimary else TSColorRole.TextSecondary,
                fill = when {
                    selected -> TSColorRole.SurfaceCard
                    isPressed -> TSColorRole.SurfacePressed
                    else -> null
                },
                shadow = if (selected) TSShadowRole.Sm else null,
                indicatorColor = null,
                indicatorHeightDp = 0f,
                cornerRadiusDp = TSTokenValues.Radius.radiusMd,
                paddingVerticalDp = TSTokenValues.Component.tabsSegmentPaddingVertical,
                paddingHorizontalDp = TSTokenValues.Component.tabsSegmentPaddingHorizontal,
                minHeightDp = SEGMENTED_HEIGHT_DP - (TSTokenValues.Component.tabsSegmentedTrackPadding + BORDER_WIDTH_DP) * 2f,
                minTouchTargetDp = MIN_TOUCH_TARGET_DP,
                opacity = opacity,
                showsFocusRing = showsFocusRing,
                animationDurationMillis = TSTokenValues.Duration.durationBase,
                animationEasing = TSTokenValues.Easing.easingStandard,
            )
        }
    }

    public fun container(variant: TSTabsVariant, enabled: Boolean = true): Container {
        val opacity = if (enabled) 1f else TSTokenValues.Opacity.opacityDisabled
        return when (variant) {
            TSTabsVariant.Underline -> Container(
                fill = null,
                border = TSColorRole.BorderSubtle,
                borderWidthDp = BORDER_WIDTH_DP,
                cornerRadiusDp = 0f,
                paddingDp = 0f,
                spacingDp = TSTokenValues.Spacing.space4,
                minHeightDp = UNDERLINE_HEIGHT_DP,
                minTouchTargetDp = MIN_TOUCH_TARGET_DP,
                scrollable = true,
                opacity = opacity,
            )
            TSTabsVariant.Segmented -> Container(
                fill = TSColorRole.BackgroundSecondary,
                border = TSColorRole.BorderSubtle,
                borderWidthDp = BORDER_WIDTH_DP,
                cornerRadiusDp = TSTokenValues.Radius.radiusLg,
                paddingDp = TSTokenValues.Component.tabsSegmentedTrackPadding,
                spacingDp = 0f,
                minHeightDp = SEGMENTED_HEIGHT_DP,
                minTouchTargetDp = MIN_TOUCH_TARGET_DP,
                scrollable = false,
                opacity = opacity,
            )
        }
    }

    public fun selectedIndex(items: List<TSTabItem>, selectedId: String): Int =
        items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
}
