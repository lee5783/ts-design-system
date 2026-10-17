package com.ts.ds.core.style

import com.ts.ds.tokens.TSTokenValues

public enum class TSAvatarSize(
    public val diameterDp: Float,
) {
    Xs(24f),
    Sm(32f),
    Md(40f),
    Lg(56f),
}

public enum class TSAvatarContent {
    Initials,
    Image,
    Placeholder,
    ;

    public companion object {
        public fun make(initials: String, hasImage: Boolean): TSAvatarContent = when {
            hasImage -> Image
            initials.isEmpty() -> Placeholder
            else -> Initials
        }
    }
}

public object TSAvatarStyleSpec : TSStyleSpec<TSAvatarStyleSpec.Configuration, TSAvatarStyleSpec.Resolved> {
    public data class Configuration(
        public val size: TSAvatarSize = TSAvatarSize.Md,
        public val content: TSAvatarContent = TSAvatarContent.Initials,
    )

    public data class Resolved(
        public val diameterDp: Float,
        public val cornerRadiusDp: Float,
        public val fill: TSColorRole,
        public val foreground: TSColorRole,
        public val initialsFontSizeDp: Float,
        public val initialsFontWeight: Int,
        public val initialsMinimumScaleFactor: Float,
        public val contentInsetDp: Float,
        public val showsInitials: Boolean,
        public val showsImage: Boolean,
        public val showsPlaceholder: Boolean,
        public val placeholderIconSizeDp: Float,
    )

    public val configurations: List<Configuration> = TSAvatarSize.entries.flatMap { size ->
        TSAvatarContent.entries.map { Configuration(size, it) }
    }

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved {
        val size = configuration.size
        val content = configuration.content
        return Resolved(
            diameterDp = size.diameterDp,
            cornerRadiusDp = size.diameterDp / 2f,
            fill = TSColorRole.AccentSubtle,
            foreground = TSColorRole.AccentText,
            initialsFontSizeDp = initialsFontSize(size),
            initialsFontWeight = INITIALS_FONT_WEIGHT,
            initialsMinimumScaleFactor = MINIMUM_SCALE_FACTOR,
            contentInsetDp = TSTokenValues.Spacing.space0_5,
            showsInitials = content == TSAvatarContent.Initials,
            showsImage = content == TSAvatarContent.Image,
            showsPlaceholder = content == TSAvatarContent.Placeholder,
            placeholderIconSizeDp = placeholderIconSize(size).dp,
        )
    }

    public fun initialsFontSize(size: TSAvatarSize): Float = when (size) {
        TSAvatarSize.Xs -> TSTokenValues.Component.avatarXsFontSize
        TSAvatarSize.Sm -> TSTokenValues.Component.avatarSmFontSize
        TSAvatarSize.Md -> TSTokenValues.Component.avatarMdFontSize
        TSAvatarSize.Lg -> TSTokenValues.Component.avatarLgFontSize
    }

    public fun placeholderIconSize(size: TSAvatarSize): TSIconSize = when (size) {
        TSAvatarSize.Xs, TSAvatarSize.Sm -> TSIconSize.Sm
        TSAvatarSize.Md -> TSIconSize.Md
        TSAvatarSize.Lg -> TSIconSize.Lg
    }

    private const val INITIALS_FONT_WEIGHT = 600
    private const val MINIMUM_SCALE_FACTOR = 0.5f
}
