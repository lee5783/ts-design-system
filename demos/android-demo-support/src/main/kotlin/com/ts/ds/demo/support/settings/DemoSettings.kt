package com.ts.ds.demo.support.settings

public enum class DemoTheme(public val id: String) {
    System("system"),
    Light("light"),
    Dark("dark"),
    ;

    public fun isDark(systemIsDark: Boolean): Boolean = when (this) {
        System -> systemIsDark
        Light -> false
        Dark -> true
    }

    public fun next(): DemoTheme = entries[(ordinal + 1) % entries.size]

    public companion object {
        public fun fromId(id: String?): DemoTheme = entries.firstOrNull { it.id == id } ?: System
    }
}

public data class DemoSettings(
    val theme: DemoTheme = DemoTheme.System,
    val fontScale: Float = DEFAULT_FONT_SCALE,
    val rtl: Boolean = false,
    val reduceMotion: Boolean = false,
) {
    public fun encode(): String = listOf(
        "$KEY_THEME=${theme.id}",
        "$KEY_FONT_SCALE=$fontScale",
        "$KEY_RTL=$rtl",
        "$KEY_REDUCE_MOTION=$reduceMotion",
    ).joinToString(SEPARATOR)

    public companion object {
        public const val DEFAULT_FONT_SCALE: Float = 1f
        public val fontScales: List<Float> = listOf(0.85f, 1f, 1.3f, 1.5f, 2f)

        private const val SEPARATOR = ";"
        private const val KEY_THEME = "theme"
        private const val KEY_FONT_SCALE = "fontScale"
        private const val KEY_RTL = "rtl"
        private const val KEY_REDUCE_MOTION = "reduceMotion"

        public fun decode(encoded: String?): DemoSettings {
            val values = encoded.orEmpty()
                .split(SEPARATOR)
                .mapNotNull { entry -> entry.split("=", limit = 2).takeIf { it.size == 2 } }
                .associate { (key, value) -> key to value }
            return DemoSettings(
                theme = DemoTheme.fromId(values[KEY_THEME]),
                fontScale = values[KEY_FONT_SCALE]?.toFloatOrNull()?.takeIf { it in fontScales } ?: DEFAULT_FONT_SCALE,
                rtl = values[KEY_RTL].toBoolean(),
                reduceMotion = values[KEY_REDUCE_MOTION].toBoolean(),
            )
        }
    }
}
