package com.tinysol.designsystem.core.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TSToastQueueTest {
    private fun queue(): TSToastQueue = TSToastQueue(TSToastTimeouts.default)

    @Test
    fun toastsAreShownInFifoOrder() {
        val queue = queue()
        val first = queue.enqueue(TSToastMessage("Project archived"))
        val second = queue.enqueue(TSToastMessage("Link copied"))
        val third = queue.enqueue(TSToastMessage("Saved"))

        assertEquals(first.id, queue.current?.id)
        assertEquals(listOf(second.id, third.id), queue.pending.map { it.id })

        assertTrue(queue.dismiss(first.id))
        assertEquals(second.id, queue.current?.id)
        assertTrue(queue.dismissCurrent())
        assertEquals(third.id, queue.current?.id)
        assertTrue(queue.dismissCurrent())
        assertNull(queue.current)
        assertFalse(queue.dismissCurrent())
    }

    @Test
    fun idsAreUniqueAndIncreasing() {
        val queue = queue()
        val ids = (1..5).map { queue.enqueue(TSToastMessage("t$it")).id }

        assertEquals(ids.sorted(), ids)
        assertEquals(5, ids.toSet().size)
    }

    @Test
    fun dismissingAPendingToastKeepsTheCurrentOne() {
        val queue = queue()
        val first = queue.enqueue(TSToastMessage("a"))
        val second = queue.enqueue(TSToastMessage("b"))
        val third = queue.enqueue(TSToastMessage("c"))

        assertTrue(queue.dismiss(second.id))
        assertEquals(first.id, queue.current?.id)
        assertEquals(listOf(third.id), queue.pending.map { it.id })
        assertFalse(queue.dismiss(second.id))
    }

    @Test
    fun listenersHearOnlyHeadChanges() {
        val queue = queue()
        val heads = mutableListOf<Long?>()
        queue.addListener { heads += it?.id }

        val first = queue.enqueue(TSToastMessage("a"))
        val second = queue.enqueue(TSToastMessage("b"))
        queue.dismiss(first.id)
        queue.clear()

        assertEquals(listOf(first.id, second.id, null), heads)
    }

    @Test
    fun actionFiresAndDismisses() {
        val queue = queue()
        var undone = 0
        val entry = queue.enqueue(TSToastMessage("Project archived", actionLabel = "Undo", onAction = { undone++ }))

        assertTrue(queue.performAction(entry.id))
        assertEquals(1, undone)
        assertNull(queue.current)
        assertFalse(queue.performAction(entry.id))
    }

    @Test
    fun defaultTimeoutIsFourSecondsAndTenWithAnAction() {
        val queue = queue()
        val entry = queue.enqueue(TSToastMessage("Saved"))

        assertEquals(4_000L, entry.timeoutMillis)
        assertTrue(entry.autoDismisses)
        assertEquals(10_000L, queue.enqueue(TSToastMessage("Archived", actionLabel = "Undo")).timeoutMillis)
    }
}
