package com.tinysol.designsystem.core.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TSBadgeCountFormatterTest {
    @Test
    fun zeroAndNegativeCountsHideTheBadge() {
        assertNull(TSBadgeCountFormatter.format(0))
        assertNull(TSBadgeCountFormatter.format(-3))
    }

    @Test
    fun countsUpToTheMaximumAreShownAsIs() {
        assertEquals("1", TSBadgeCountFormatter.format(1))
        assertEquals("99", TSBadgeCountFormatter.format(99))
    }

    @Test
    fun countsAboveTheMaximumOverflow() {
        assertEquals("99+", TSBadgeCountFormatter.format(100))
        assertEquals("99+", TSBadgeCountFormatter.format(Int.MAX_VALUE))
    }

    @Test
    fun customMaximumAndOverflowTemplate() {
        assertEquals("9", TSBadgeCountFormatter.format(9, maxCount = 9))
        assertEquals("plus de 9", TSBadgeCountFormatter.format(10, maxCount = 9) { "plus de $it" })
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveMaximumIsRejected() {
        TSBadgeCountFormatter.format(1, maxCount = 0)
    }
}
