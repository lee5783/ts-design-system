package com.ts.ds.core.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class TSInitialsTest {
    @Test
    fun oneWordUsesItsFirstLetter() {
        assertEquals("C", TSInitials.from("cher"))
    }

    @Test
    fun twoWordsUseBothFirstLetters() {
        assertEquals("AL", TSInitials.from("Ada Lovelace"))
    }

    @Test
    fun threeWordsUseFirstAndLastWords() {
        assertEquals("MW", TSInitials.from("Mary Jane Watson"))
    }

    @Test
    fun extraWhitespaceIsIgnored() {
        assertEquals("AL", TSInitials.from("  ada \t\n  lovelace  "))
    }

    @Test
    fun emptyAndBlankNamesGiveNoInitials() {
        assertEquals("", TSInitials.from(""))
        assertEquals("", TSInitials.from("   "))
    }

    @Test
    fun emojiKeepsTheWholeGraphemeCluster() {
        assertEquals("👩‍💻S", TSInitials.from("👩‍💻 Smith"))
        assertEquals("😀", TSInitials.from("😀"))
    }

    @Test
    fun combiningMarksStayWithTheirBaseLetter() {
        assertEquals("ÉZ", TSInitials.from("émile zola"))
    }

    @Test
    fun nonLatinScriptsAreSupported() {
        assertEquals("ИП", TSInitials.from("иван петров"))
        assertEquals("山太", TSInitials.from("山田 太郎"))
        assertEquals("مع", TSInitials.from("محمد علي"))
    }
}
