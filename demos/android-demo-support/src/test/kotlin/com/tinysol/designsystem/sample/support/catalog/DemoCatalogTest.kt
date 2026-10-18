package com.tinysol.designsystem.sample.support.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoCatalogTest {
    @Test
    fun everyPageIsListedExactlyOnce() {
        val pages = DemoCatalog.allPages
        assertEquals(DemoPageId.entries.toSet(), pages.toSet())
        assertEquals(pages.size, pages.distinct().size)
    }

    @Test
    fun everySectionIsListedOnceInDeclarationOrder() {
        assertEquals(DemoSectionId.entries.toList(), DemoCatalog.sections.map { it.id })
    }

    @Test
    fun foundationsSectionHoldsExactlyTheFoundationPages() {
        val foundations = DemoCatalog.sections.first { it.id == DemoSectionId.Foundations }
        assertEquals(DemoCatalog.foundationPages, foundations.pages)
        assertEquals(
            listOf("colors", "typography", "spacing", "radius", "size", "opacity", "elevation", "icons"),
            DemoCatalog.foundationPages.map { it.id },
        )
    }

    @Test
    fun componentPagesMatchTheIosCatalogIds() {
        assertEquals(
            setOf(
                "button", "icon-button", "badge", "avatar", "text-field", "checkbox", "switch",
                "tabs", "list-item", "card", "alert", "dialog", "toast", "bottom-nav", "spinner", "text",
            ),
            DemoCatalog.componentPages.map { it.id }.toSet(),
        )
        assertTrue(DemoCatalog.sections.drop(1).all { section -> section.pages.all { it.kind == DemoPageKind.Component } })
    }

    @Test
    fun pageIdsAreKebabCaseAndUnique() {
        val ids = DemoPageId.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it.matches(Regex("[a-z]+(-[a-z]+)*")) })
    }

    @Test
    fun testTagsFollowTheSharedAccessibilityIdentifierScheme() {
        assertEquals("demo.page.icon-button", DemoPageId.IconButton.testTag)
    }

    @Test
    fun pagesResolveFromTheirIds() {
        DemoPageId.entries.forEach { assertEquals(it, DemoPageId.fromId(it.id)) }
        assertNull(DemoPageId.fromId("unknown"))
    }

    @Test
    fun everyPageBelongsToOneSection() {
        DemoPageId.entries.forEach { page ->
            assertEquals(1, DemoCatalog.sections.count { page in it.pages })
        }
        assertEquals(DemoSectionId.Foundations, DemoCatalog.sectionContaining(DemoPageId.Colors)?.id)
        assertEquals(DemoSectionId.Forms, DemoCatalog.sectionContaining(DemoPageId.Switch)?.id)
        assertEquals(DemoSectionId.Navigation, DemoCatalog.sectionContaining(DemoPageId.Tabs)?.id)
        assertEquals(DemoSectionId.Content, DemoCatalog.sectionContaining(DemoPageId.ListItem)?.id)
        assertEquals(DemoSectionId.Feedback, DemoCatalog.sectionContaining(DemoPageId.Toast)?.id)
    }
}
