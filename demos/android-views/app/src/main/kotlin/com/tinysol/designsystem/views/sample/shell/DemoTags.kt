package com.tinysol.designsystem.views.sample.shell

import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.sample.support.settings.DemoTheme

object DemoTags {
    const val CATALOG = "demo.catalog"
    const val ABOUT = "demo.about"
    const val SETTINGS = "demo.settings"
    const val ACTION_THEME = "demo.action.theme"

    fun screen(page: DemoPageId): String = "demo.screen.${page.id}"

    fun theme(theme: DemoTheme): String = "demo.settings.theme.${theme.id}"

    fun fontScale(scale: Float): String = "demo.settings.fontScale.${(scale * 100).toInt()}"
}
