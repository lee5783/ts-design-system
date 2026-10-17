package com.ts.ds.demo.support.catalog

public object DemoCatalog {
    public val sections: List<DemoSection> = listOf(
        DemoSection(
            DemoSectionId.Foundations,
            listOf(
                DemoPageId.Colors,
                DemoPageId.Typography,
                DemoPageId.Spacing,
                DemoPageId.Radius,
                DemoPageId.Size,
                DemoPageId.Opacity,
                DemoPageId.Elevation,
                DemoPageId.Icons,
            ),
        ),
        DemoSection(DemoSectionId.Actions, listOf(DemoPageId.Button, DemoPageId.IconButton)),
        DemoSection(DemoSectionId.Forms, listOf(DemoPageId.TextField, DemoPageId.Checkbox, DemoPageId.Switch)),
        DemoSection(DemoSectionId.Navigation, listOf(DemoPageId.Tabs, DemoPageId.BottomNav)),
        DemoSection(DemoSectionId.Content, listOf(DemoPageId.Badge, DemoPageId.Avatar, DemoPageId.ListItem, DemoPageId.Card)),
        DemoSection(DemoSectionId.Feedback, listOf(DemoPageId.Alert, DemoPageId.Dialog, DemoPageId.Toast)),
    )

    public val allPages: List<DemoPageId>
        get() = sections.flatMap { it.pages }

    public val foundationPages: List<DemoPageId>
        get() = allPages.filter { it.kind == DemoPageKind.Foundation }

    public val componentPages: List<DemoPageId>
        get() = allPages.filter { it.kind == DemoPageKind.Component }

    public fun sectionContaining(page: DemoPageId): DemoSection? = sections.firstOrNull { page in it.pages }
}
