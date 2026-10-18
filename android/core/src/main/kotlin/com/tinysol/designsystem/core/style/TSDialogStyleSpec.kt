package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSTokenValues

public enum class TSDialogVariant {
    Center,
    Sheet,
}

public enum class TSDialogActionsAxis {
    Horizontal,
    Vertical,
}

public object TSDialogStyleSpec : TSStyleSpec<TSDialogStyleSpec.Configuration, TSDialogStyleSpec.Resolved> {
    public const val ACCESSIBILITY_FONT_SCALE: Float = TSListItemLayout.STACKED_FONT_SCALE
    public const val PREDICTIVE_BACK_MIN_SCALE: Float = 0.9f

    public data class Configuration(
        public val variant: TSDialogVariant = TSDialogVariant.Center,
        public val isAccessibilitySize: Boolean = false,
    )

    public data class Resolved(
        public val scrim: TSColorRole,
        public val fill: TSColorRole,
        public val cornerRadiusDp: Float,
        public val roundsBottomCorners: Boolean,
        public val shadow: TSShadowRole?,
        public val maxWidthDp: Float?,
        public val outerMarginDp: Float,
        public val paddingTopDp: Float,
        public val paddingHorizontalDp: Float,
        public val paddingBottomDp: Float,
        public val showsGrabber: Boolean,
        public val grabberWidthDp: Float,
        public val grabberHeightDp: Float,
        public val grabberColor: TSColorRole,
        public val grabberSpacingDp: Float,
        public val contentTopInsetDp: Float,
        public val titleTypeRole: TSTypeRole,
        public val titleColor: TSColorRole,
        public val titleSpacingDp: Float,
        public val bodyTypeRole: TSTypeRole,
        public val bodyColor: TSColorRole,
        public val actionsTopSpacingDp: Float,
        public val actionsSpacingDp: Float,
        public val actionsAxis: TSDialogActionsAxis,
        public val primaryActionFirst: Boolean,
        public val blockActions: Boolean,
    )

    public val configurations: List<Configuration> = TSDialogVariant.entries.flatMap { variant ->
        listOf(false, true).map { Configuration(variant, it) }
    }

    public fun isAccessibilitySize(fontScale: Float): Boolean = fontScale >= ACCESSIBILITY_FONT_SCALE

    public fun configuration(variant: TSDialogVariant, fontScale: Float): Configuration =
        Configuration(variant, isAccessibilitySize(fontScale))

    public fun predictiveBackScale(progress: Float): Float =
        1f - (1f - PREDICTIVE_BACK_MIN_SCALE) * progress.coerceIn(0f, 1f)

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val isSheet = configuration.variant == TSDialogVariant.Sheet
        val stacksActions = isSheet || configuration.isAccessibilitySize
        val paddingTop = if (isSheet) TSTokenValues.Spacing.space3 else TSTokenValues.Spacing.space6
        val grabberHeight = TSTokenValues.Component.sheetGrabberHeight
        val grabberSpacing = TSTokenValues.Spacing.space4
        return Resolved(
            scrim = TSColorRole.Overlay,
            fill = TSColorRole.SurfaceElevated,
            cornerRadiusDp = TSTokenValues.Radius.radiusXl,
            roundsBottomCorners = !isSheet,
            shadow = if (isSheet) null else TSShadowRole.Lg,
            maxWidthDp = if (isSheet) null else TSTokenValues.Component.dialogMaxWidth,
            outerMarginDp = if (isSheet) 0f else TSTokenValues.Spacing.space4,
            paddingTopDp = paddingTop,
            paddingHorizontalDp = if (isSheet) TSTokenValues.Spacing.space4 else TSTokenValues.Spacing.space6,
            paddingBottomDp = TSTokenValues.Spacing.space6,
            showsGrabber = isSheet,
            grabberWidthDp = TSTokenValues.Component.sheetGrabberWidth,
            grabberHeightDp = grabberHeight,
            grabberColor = TSColorRole.BorderStrong,
            grabberSpacingDp = grabberSpacing,
            contentTopInsetDp = if (isSheet) paddingTop + grabberHeight + grabberSpacing else paddingTop,
            titleTypeRole = TSTypeRole.Title2,
            titleColor = TSColorRole.TextPrimary,
            titleSpacingDp = TSTokenValues.Spacing.space2,
            bodyTypeRole = TSTypeRole.Body,
            bodyColor = TSColorRole.TextSecondary,
            actionsTopSpacingDp = TSTokenValues.Spacing.space6,
            actionsSpacingDp = TSTokenValues.Spacing.space2,
            actionsAxis = if (stacksActions) TSDialogActionsAxis.Vertical else TSDialogActionsAxis.Horizontal,
            primaryActionFirst = stacksActions,
            blockActions = stacksActions,
        )
    }
}
