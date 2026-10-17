package com.ts.ds.core.style

import com.ts.ds.tokens.TSTokenValues

public enum class TSIconSize(
    public val dp: Float,
) {
    Sm(TSTokenValues.Size.iconSm),
    Md(TSTokenValues.Size.iconMd),
    Lg(TSTokenValues.Size.iconLg),
}

public object TSIconStyleSpec : TSStyleSpec<TSIconStyleSpec.Configuration, TSIconStyleSpec.Resolved> {
    public data class Configuration(
        public val size: TSIconSize = TSIconSize.Md,
        public val hasLabel: Boolean = false,
    )

    public data class Resolved(
        public val sizeDp: Float,
        public val importantForAccessibility: Boolean,
    )

    public val configurations: List<Configuration> = TSIconSize.entries.flatMap { size ->
        listOf(false, true).map { Configuration(size, it) }
    }

    override fun resolve(configuration: Configuration, state: TSControlState): Resolved =
        Resolved(sizeDp = configuration.size.dp, importantForAccessibility = configuration.hasLabel)
}
