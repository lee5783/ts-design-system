package com.ts.ds.core.logic

import java.text.BreakIterator

public object TSInitials {
    public const val MAX_LETTERS: Int = 2

    private val whitespace = Regex("\\s+")

    public fun from(name: String): String {
        val words = name.trim().split(whitespace).filter { it.isNotEmpty() }
        if (words.isEmpty()) return ""
        val picked = if (words.size == 1) words else listOf(words.first(), words.last())
        return picked.take(MAX_LETTERS).joinToString(separator = "") { firstGrapheme(it).uppercase() }
    }

    private fun firstGrapheme(word: String): String {
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(word)
        val end = iterator.next()
        return if (end == BreakIterator.DONE) word else word.substring(0, end)
    }
}
