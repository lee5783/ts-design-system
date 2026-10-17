package com.ts.ds.testsupport.snapshot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotMatrixTest {
    @Test
    fun standardMatrixCoversThemeFontScaleAndDirection() {
        val variants = SnapshotMatrix.standard
        assertEquals(8, variants.size)
        assertEquals(8, variants.map { it.name }.toSet().size)
        assertEquals(setOf(false, true), variants.map { it.dark }.toSet())
        assertEquals(setOf(1f, 2f), variants.map { it.fontScale }.toSet())
        assertEquals(setOf(false, true), variants.map { it.rtl }.toSet())
    }

    @Test
    fun variantNamesAreStableAndFileSafe() {
        assertEquals("light_font100_ltr", SnapshotVariant(dark = false, fontScale = 1f, rtl = false).name)
        assertEquals("dark_font200_rtl", SnapshotVariant(dark = true, fontScale = 2f, rtl = true).name)
        assertTrue(SnapshotMatrix.standard.all { it.name.matches(Regex("[a-z0-9_]+")) })
    }

    @Test
    fun sdkSubsetIsPartOfTheStandardMatrix() {
        assertTrue(SnapshotMatrix.standard.containsAll(SnapshotMatrix.sdkSubset))
        assertTrue(SnapshotMatrix.sdkSubset.isNotEmpty())
    }

    @Test
    fun parametersWrapEachVariant() {
        val parameters = SnapshotMatrix.parameters(SnapshotMatrix.standard)
        assertEquals(SnapshotMatrix.standard, parameters.map { it.single() })
    }
}
