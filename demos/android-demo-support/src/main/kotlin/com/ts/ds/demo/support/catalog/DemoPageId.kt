package com.ts.ds.demo.support.catalog

import com.ts.ds.demo.support.R

public enum class DemoPageKind {
    Foundation,
    Component,
}

public enum class DemoPageId(
    public val id: String,
    public val kind: DemoPageKind,
    public val titleRes: Int,
) {
    Colors("colors", DemoPageKind.Foundation, R.string.demo_page_colors),
    Typography("typography", DemoPageKind.Foundation, R.string.demo_page_typography),
    Spacing("spacing", DemoPageKind.Foundation, R.string.demo_page_spacing),
    Radius("radius", DemoPageKind.Foundation, R.string.demo_page_radius),
    Size("size", DemoPageKind.Foundation, R.string.demo_page_size),
    Opacity("opacity", DemoPageKind.Foundation, R.string.demo_page_opacity),
    Elevation("elevation", DemoPageKind.Foundation, R.string.demo_page_elevation),
    Icons("icons", DemoPageKind.Foundation, R.string.demo_page_icons),
    Button("button", DemoPageKind.Component, R.string.demo_page_button),
    IconButton("icon-button", DemoPageKind.Component, R.string.demo_page_icon_button),
    Badge("badge", DemoPageKind.Component, R.string.demo_page_badge),
    Avatar("avatar", DemoPageKind.Component, R.string.demo_page_avatar),
    TextField("text-field", DemoPageKind.Component, R.string.demo_page_text_field),
    Checkbox("checkbox", DemoPageKind.Component, R.string.demo_page_checkbox),
    Switch("switch", DemoPageKind.Component, R.string.demo_page_switch),
    Tabs("tabs", DemoPageKind.Component, R.string.demo_page_tabs),
    ListItem("list-item", DemoPageKind.Component, R.string.demo_page_list_item),
    Card("card", DemoPageKind.Component, R.string.demo_page_card),
    Alert("alert", DemoPageKind.Component, R.string.demo_page_alert),
    Dialog("dialog", DemoPageKind.Component, R.string.demo_page_dialog),
    Toast("toast", DemoPageKind.Component, R.string.demo_page_toast),
    BottomNav("bottom-nav", DemoPageKind.Component, R.string.demo_page_bottom_nav),
    Spinner("spinner", DemoPageKind.Component, R.string.demo_page_spinner),
    Text("text", DemoPageKind.Component, R.string.demo_page_text),
    ;

    public val testTag: String
        get() = "demo.page.$id"

    public companion object {
        public fun fromId(id: String): DemoPageId? = entries.firstOrNull { it.id == id }
    }
}
