package com.ts.ds.demo.support.settings

import java.net.URI
import java.net.URLDecoder

public object DemoLaunchOverrides {
    public const val THEME_EXTRA: String = "demo.theme"
    public const val SETTINGS_HOST: String = "settings"
    public const val THEME_PARAMETER: String = "theme"
    public const val COMPOSE_SCHEME: String = "tsds-compose"
    public const val VIEWS_SCHEME: String = "tsds-views"

    public fun theme(extra: String?, link: String?, scheme: String): DemoTheme? =
        themeFromLink(link, scheme) ?: themeFromExtra(extra)

    public fun themeFromExtra(value: String?): DemoTheme? = value?.let(::parseTheme)

    public fun themeFromLink(link: String?, scheme: String): DemoTheme? {
        val uri = link?.let { runCatching { URI(it.trim()) }.getOrNull() } ?: return null
        if (!scheme.equals(uri.scheme, ignoreCase = true) || !SETTINGS_HOST.equals(uri.host, ignoreCase = true)) return null
        val value = uri.rawQuery.orEmpty()
            .split("&")
            .map { it.split("=", limit = 2) }
            .firstOrNull { it.first() == THEME_PARAMETER }
            ?: return null
        return parseTheme(URLDecoder.decode(value.getOrElse(1) { "" }, Charsets.UTF_8.name()))
    }

    public fun settingsLink(scheme: String, theme: DemoTheme): String = "$scheme://$SETTINGS_HOST?$THEME_PARAMETER=${theme.id}"

    private fun parseTheme(value: String): DemoTheme = DemoTheme.fromId(value.trim().lowercase())
}
