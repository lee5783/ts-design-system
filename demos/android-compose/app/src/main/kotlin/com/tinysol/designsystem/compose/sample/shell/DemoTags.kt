package com.tinysol.designsystem.compose.sample.shell

import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.sample.support.settings.DemoTheme

object DemoTags {
    const val CATALOG = "demo.catalog"
    const val ABOUT = "demo.about"
    const val SETTINGS = "demo.settings"
    const val ACTION_BACK = "demo.action.back"
    const val ACTION_SETTINGS = "demo.action.settings"
    const val ACTION_ABOUT = "demo.action.about"
    const val ACTION_THEME = "demo.action.theme"
    const val ACTION_CLOSE_SETTINGS = "demo.settings.close"
    const val ACTION_RESET_SETTINGS = "demo.settings.reset"
    const val SETTING_RTL = "demo.settings.rtl"
    const val SETTING_REDUCE_MOTION = "demo.settings.reduceMotion"
    const val SCREEN_TITLE = "demo.screen.title"

    fun screen(page: DemoPageId): String = "demo.screen.${page.id}"

    fun theme(theme: DemoTheme): String = "demo.settings.theme.${theme.id}"

    fun fontScale(scale: Float): String = "demo.settings.fontScale.${(scale * 100).toInt()}"
}
