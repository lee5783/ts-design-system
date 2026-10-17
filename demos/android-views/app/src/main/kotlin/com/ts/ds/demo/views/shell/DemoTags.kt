package com.ts.ds.demo.views.shell

import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.settings.DemoTheme

object DemoTags {
    const val CATALOG = "demo.catalog"
    const val ABOUT = "demo.about"
    const val SETTINGS = "demo.settings"
    const val ACTION_THEME = "demo.action.theme"

    fun screen(page: DemoPageId): String = "demo.screen.${page.id}"

    fun theme(theme: DemoTheme): String = "demo.settings.theme.${theme.id}"

    fun fontScale(scale: Float): String = "demo.settings.fontScale.${(scale * 100).toInt()}"
}
