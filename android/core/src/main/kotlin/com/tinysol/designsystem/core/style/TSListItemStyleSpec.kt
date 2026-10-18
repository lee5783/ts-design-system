package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.tokens.TSCubicBezierValues
import com.tinysol.designsystem.tokens.TSTokenValues

public enum class TSListItemLayout {
    Horizontal,
    Stacked,
    ;

    public companion object {
        public const val STACKED_FONT_SCALE: Float = 1.5f

        public fun make(fontScale: Float): TSListItemLayout = if (fontScale >= STACKED_FONT_SCALE) Stacked else Horizontal
    }
}

public enum class TSListGroupPosition {
    Single,
    First,
    Middle,
    Last,
    ;

    public val roundsTop: Boolean
        get() = this == Single || this == First

    public val roundsBottom: Boolean
        get() = this == Single || this == Last

    public val drawsTopDivider: Boolean
        get() = this == Middle || this == Last

    public companion object {
        public fun make(index: Int, count: Int): TSListGroupPosition = when {
            count <= 1 -> Single
            index <= 0 -> First
            index >= count - 1 -> Last
            else -> Middle
        }
    }
}

public object TSListItemStyleSpec : TSStyleSpec<TSListItemStyleSpec.Configuration, TSListItemStyleSpec.Resolved> {
    public const val MIN_TOUCH_TARGET_DP: Float = 48f

    public data class Configuration(
        public val hasSubtitle: Boolean = false,
        public val isInteractive: Boolean = false,
        public val layout: TSListItemLayout = TSListItemLayout.Horizontal,
    )

    public data class Resolved(
        public val background: TSColorRole?,
        public val titleTypeRole: TSTypeRole,
        public val titleColor: TSColorRole,
        public val titleMaxLines: Int?,
        public val showsSubtitle: Boolean,
        public val subtitleTypeRole: TSTypeRole,
        public val subtitleColor: TSColorRole,
        public val subtitleMaxLines: Int?,
        public val leadingColor: TSColorRole,
        public val trailingTypeRole: TSTypeRole,
        public val trailingColor: TSColorRole,
        public val trailingSpacingDp: Float,
        public val chevron: TSIcon,
        public val chevronSize: TSIconSize,
        public val chevronColor: TSColorRole,
        public val horizontalPaddingDp: Float,
        public val verticalPaddingDp: Float,
        public val spacingDp: Float,
        public val isStacked: Boolean,
        public val stackedSpacingDp: Float,
        public val minHeightDp: Float,
        public val minTouchTargetDp: Float,
        public val opacity: Float,
    )

    public data class Tile(
        public val sizeDp: Float,
        public val cornerRadiusDp: Float,
        public val fill: TSColorRole,
        public val foreground: TSColorRole,
        public val iconSize: TSIconSize,
    )

    public data class Group(
        public val fill: TSColorRole,
        public val border: TSColorRole,
        public val borderWidthDp: Float,
        public val cornerRadiusDp: Float,
        public val divider: TSColorRole,
        public val dividerThicknessDp: Float,
        public val dividerInsetDp: Float,
        public val headerTypeRole: TSTypeRole,
        public val headerColor: TSColorRole,
        public val headerSpacingDp: Float,
        public val headerHorizontalPaddingDp: Float,
        public val sectionSpacingDp: Float,
    )

    public data class Disclosure(
        public val collapsedRotationDegrees: Float,
        public val expandedRotationDegrees: Float,
        public val animationDurationMillis: Int,
        public val animationEasing: TSCubicBezierValues,
        public val bodyHorizontalPaddingDp: Float,
        public val bodyBottomPaddingDp: Float,
    ) {
        public fun rotationDegrees(expanded: Boolean, rightToLeft: Boolean): Float = when {
            !expanded -> collapsedRotationDegrees
            rightToLeft -> -expandedRotationDegrees
            else -> expandedRotationDegrees
        }

        public fun animationDurationMillis(reduceMotion: Boolean): Int = if (reduceMotion) 0 else animationDurationMillis
    }

    public val states: List<TSControlState> = listOf(
        TSControlState.Default,
        TSControlState.Pressed,
        TSControlState.Disabled,
        TSControlState.Focused,
    )

    public val configurations: List<Configuration> = listOf(false, true).flatMap { hasSubtitle ->
        listOf(false, true).flatMap { isInteractive ->
            TSListItemLayout.entries.map { Configuration(hasSubtitle, isInteractive, it) }
        }
    }

    public val tile: Tile = Tile(
        sizeDp = TSTokenValues.Component.listItemTileSize,
        cornerRadiusDp = TSTokenValues.Radius.radiusMd,
        fill = TSColorRole.AccentSubtle,
        foreground = TSColorRole.AccentText,
        iconSize = TSIconSize.Md,
    )

    public val group: Group = Group(
        fill = TSColorRole.SurfaceCard,
        border = TSColorRole.BorderSubtle,
        borderWidthDp = 1f,
        cornerRadiusDp = TSTokenValues.Radius.radiusLg,
        divider = TSColorRole.BorderSubtle,
        dividerThicknessDp = 1f,
        dividerInsetDp = 0f,
        headerTypeRole = TSTypeRole.Overline,
        headerColor = TSColorRole.TextMuted,
        headerSpacingDp = TSTokenValues.Spacing.space2,
        headerHorizontalPaddingDp = TSTokenValues.Spacing.space4,
        sectionSpacingDp = TSTokenValues.Spacing.space6,
    )

    public val disclosure: Disclosure = Disclosure(
        collapsedRotationDegrees = 0f,
        expandedRotationDegrees = 90f,
        animationDurationMillis = TSTokenValues.Duration.durationBase,
        animationEasing = TSTokenValues.Easing.easingStandard,
        bodyHorizontalPaddingDp = TSTokenValues.Spacing.space4,
        bodyBottomPaddingDp = TSTokenValues.Spacing.space3,
    )

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val isStacked = configuration.layout == TSListItemLayout.Stacked
        return Resolved(
            background = background(configuration.isInteractive, state),
            titleTypeRole = TSTypeRole.Body,
            titleColor = TSColorRole.TextPrimary,
            titleMaxLines = if (isStacked) null else 1,
            showsSubtitle = configuration.hasSubtitle,
            subtitleTypeRole = TSTypeRole.BodySm,
            subtitleColor = TSColorRole.TextMuted,
            subtitleMaxLines = if (isStacked) null else 1,
            leadingColor = TSColorRole.TextSecondary,
            trailingTypeRole = TSTypeRole.BodySm,
            trailingColor = TSColorRole.TextMuted,
            trailingSpacingDp = TSTokenValues.Spacing.space2,
            chevron = TSIcon.ChevronRight,
            chevronSize = TSIconSize.Md,
            chevronColor = TSColorRole.TextMuted,
            horizontalPaddingDp = TSTokenValues.Spacing.space4,
            verticalPaddingDp = TSTokenValues.Spacing.space3,
            spacingDp = TSTokenValues.Spacing.space3,
            isStacked = isStacked,
            stackedSpacingDp = TSTokenValues.Spacing.space2,
            minHeightDp = TSTokenValues.Size.controlMd,
            minTouchTargetDp = MIN_TOUCH_TARGET_DP,
            opacity = if (state.dimmed) TSTokenValues.Opacity.opacityDisabled else 1f,
        )
    }

    private fun background(isInteractive: Boolean, state: TSControlState): TSColorRole? = when {
        !isInteractive || !state.interactive -> null
        state.pressed -> TSColorRole.SurfacePressed
        state.focused -> TSColorRole.SurfaceHover
        else -> null
    }
}
