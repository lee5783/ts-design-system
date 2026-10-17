package com.ts.ds.core.style

import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSBadgeCountFormatter
import com.ts.ds.tokens.TSCubicBezierValues
import com.ts.ds.tokens.TSTokenValues

public data class TSBottomNavItem(
    public val id: String,
    public val label: String,
    public val icon: TSIcon,
    public val badge: Int? = null,
) {
    public val badgeText: String?
        get() = badge?.let { TSBadgeCountFormatter.format(it) }

    public fun badged(badge: Int?): TSBottomNavItem = copy(badge = badge)
}

public object TSBottomNavStyleSpec : TSStyleSpec<TSBottomNavStyleSpec.Configuration, TSBottomNavStyleSpec.Resolved> {
    public const val LABEL_MAX_FONT_SCALE: Float = 1.3f
    public const val BORDER_WIDTH_DP: Float = 1f

    public val itemCountRange: IntRange = 3..5

    public data class Configuration(
        public val selected: Boolean = false,
    )

    public data class Resolved(
        public val labelTypeRole: TSTypeRole,
        public val labelFontWeight: Int,
        public val foreground: TSColorRole,
        public val pillFill: TSColorRole?,
        public val pillWidthDp: Float,
        public val pillHeightDp: Float,
        public val pillCornerRadiusDp: Float,
        public val iconSizeDp: Float,
        public val spacingDp: Float,
        public val opacity: Float,
        public val showsFocusRing: Boolean,
        public val animationDurationMillis: Int,
        public val animationEasing: TSCubicBezierValues,
    )

    public data class Container(
        public val heightDp: Float,
        public val fill: TSColorRole,
        public val border: TSColorRole,
        public val borderWidthDp: Float,
    )

    public data class Badge(
        public val fill: TSColorRole,
        public val foreground: TSColorRole,
        public val fontSizeDp: Float,
        public val fontWeight: Int,
        public val minWidthDp: Float,
        public val heightDp: Float,
        public val horizontalPaddingDp: Float,
        public val cornerRadiusDp: Float,
        public val offsetTopDp: Float,
        public val offsetStartDp: Float,
        public val topFromPillDp: Float,
    )

    public val states: List<TSControlState> = listOf(
        TSControlState.Default,
        TSControlState.Pressed,
        TSControlState.Disabled,
        TSControlState.Focused,
    )

    public val configurations: List<Configuration> = listOf(false, true).map(::Configuration)

    public val container: Container = Container(
        heightDp = TSTokenValues.Size.tabBar,
        fill = TSColorRole.SurfaceCard,
        border = TSColorRole.BorderSubtle,
        borderWidthDp = BORDER_WIDTH_DP,
    )

    public val defaultContentInsetDp: Float
        get() = (
            TSTokenValues.Size.tabBar -
                TSTokenValues.Component.bottomNavPillHeight -
                TSTokenValues.Spacing.space0_5 -
                TSTypeRole.Caption.values.lineHeight
            ) / 2f

    public val badge: Badge = Badge(
        fill = TSColorRole.DangerSolid,
        foreground = TSColorRole.TextOnAccent,
        fontSizeDp = TSTokenValues.Component.bottomNavBadgeFontSize,
        fontWeight = TSTokenValues.Component.bottomNavBadgeFontWeight,
        minWidthDp = TSTokenValues.Component.bottomNavBadgeSize,
        heightDp = TSTokenValues.Component.bottomNavBadgeSize,
        horizontalPaddingDp = TSTokenValues.Spacing.space1,
        cornerRadiusDp = TSTokenValues.Component.bottomNavBadgeSize / 2f,
        offsetTopDp = TSTokenValues.Component.bottomNavBadgeOffsetTop,
        offsetStartDp = TSTokenValues.Component.bottomNavBadgeOffsetStart,
        topFromPillDp = TSTokenValues.Component.bottomNavBadgeOffsetTop - defaultContentInsetDp,
    )

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val selected = configuration.selected
        val isPressed = state.pressed && state.interactive
        return Resolved(
            labelTypeRole = TSTypeRole.Caption,
            labelFontWeight = TSTokenValues.Component.bottomNavLabelFontWeight,
            foreground = if (selected) TSColorRole.AccentText else TSColorRole.TextMuted,
            pillFill = when {
                selected -> TSColorRole.AccentSubtle
                isPressed -> TSColorRole.SurfacePressed
                else -> null
            },
            pillWidthDp = TSTokenValues.Component.bottomNavPillWidth,
            pillHeightDp = TSTokenValues.Component.bottomNavPillHeight,
            pillCornerRadiusDp = TSTokenValues.Component.bottomNavPillHeight / 2f,
            iconSizeDp = TSTokenValues.Size.iconLg,
            spacingDp = TSTokenValues.Spacing.space0_5,
            opacity = if (state.dimmed) TSTokenValues.Opacity.opacityDisabled else 1f,
            showsFocusRing = state.focused && state.interactive,
            animationDurationMillis = TSTokenValues.Duration.durationBase,
            animationEasing = TSTokenValues.Easing.easingStandard,
        )
    }

    public fun labelFontScale(fontScale: Float): Float = fontScale.coerceAtMost(LABEL_MAX_FONT_SCALE)

    public fun selectedIndex(items: List<TSBottomNavItem>, selectedId: String): Int =
        items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
}
