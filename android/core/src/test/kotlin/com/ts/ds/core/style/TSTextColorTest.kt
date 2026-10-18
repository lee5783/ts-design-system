package com.ts.ds.core.style

import org.junit.Assert.assertEquals
import org.junit.Test

class TSTextColorTest {
    @Test
    fun everyTextColorIsATextRole() {
        val expected = mapOf(
            TSTextColor.Primary to TSColorRole.TextPrimary,
            TSTextColor.Secondary to TSColorRole.TextSecondary,
            TSTextColor.Muted to TSColorRole.TextMuted,
            TSTextColor.Disabled to TSColorRole.TextDisabled,
            TSTextColor.Inverse to TSColorRole.TextInverse,
            TSTextColor.OnAccent to TSColorRole.TextOnAccent,
        )
        assertEquals(expected.keys, TSTextColor.entries.toSet())
        expected.forEach { (color, role) -> assertEquals(role, color.role) }
    }

    @Test
    fun textColorsCoverEveryTextRoleOfThePalette() {
        val textRoles = TSColorRole.entries.filter { it.name.startsWith("Text") }.toSet()
        assertEquals(textRoles, TSTextColor.entries.map { it.role }.toSet())
    }
}
