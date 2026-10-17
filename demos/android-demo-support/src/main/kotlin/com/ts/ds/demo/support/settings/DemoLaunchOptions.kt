package com.ts.ds.demo.support.settings

import com.ts.ds.demo.support.catalog.DemoPageId

public data class DemoLaunchOptions(
    val theme: DemoTheme? = null,
    val fontScale: Float? = null,
    val rtl: Boolean? = null,
    val reduceMotion: Boolean? = null,
    val page: DemoPageId? = null,
) {
    public fun applyTo(settings: DemoSettings): DemoSettings = settings.copy(
        theme = theme ?: settings.theme,
        fontScale = fontScale ?: settings.fontScale,
        rtl = rtl ?: settings.rtl,
        reduceMotion = reduceMotion ?: settings.reduceMotion,
    )

    public val hasSettings: Boolean
        get() = theme != null || fontScale != null || rtl != null || reduceMotion != null

    public companion object {
        public const val THEME: String = "TSDemoTheme"
        public const val FONT_SCALE: String = "TSDemoFontScale"
        public const val RIGHT_TO_LEFT: String = "TSDemoRightToLeft"
        public const val REDUCE_MOTION: String = "TSDemoReduceMotion"
        public const val PAGE: String = "TSDemoPage"

        public fun from(value: (String) -> String?): DemoLaunchOptions = DemoLaunchOptions(
            theme = value(THEME)?.let { id -> DemoTheme.entries.firstOrNull { it.id == id } },
            fontScale = value(FONT_SCALE)?.toFloatOrNull()?.takeIf { it in DemoSettings.fontScales },
            rtl = value(RIGHT_TO_LEFT)?.let(::parseFlag),
            reduceMotion = value(REDUCE_MOTION)?.let(::parseFlag),
            page = value(PAGE)?.let(DemoPageId::fromId),
        )

        private fun parseFlag(value: String): Boolean? = when (value.lowercase()) {
            "yes", "true", "1" -> true
            "no", "false", "0" -> false
            else -> null
        }
    }
}
