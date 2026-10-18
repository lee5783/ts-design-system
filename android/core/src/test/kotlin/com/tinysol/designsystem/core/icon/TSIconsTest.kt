package com.tinysol.designsystem.core.icon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TSIconsTest {
    @Test
    fun dsNamesAreKebabCase() {
        assertEquals("chevron-left", TSIcon.ChevronLeft.dsName)
        assertEquals("check-circle", TSIcon.CheckCircle.dsName)
        assertEquals("home", TSIcon.Home.dsName)
    }

    @Test
    fun everyIconRoundTripsThroughItsDsName() {
        TSIcon.entries.forEach { icon ->
            assertEquals(icon, TSIcons.fromDsName(icon.dsName))
        }
    }

    @Test
    fun lookupIsLenientOnCaseAndWhitespaceAndRejectsUnknownNames() {
        assertEquals(TSIcon.ArrowRight, TSIcons.fromDsName(" Arrow-Right "))
        assertNull(TSIcons.fromDsName("eye-off"))
        assertNull(TSIcons.fromDsName(""))
    }
}
