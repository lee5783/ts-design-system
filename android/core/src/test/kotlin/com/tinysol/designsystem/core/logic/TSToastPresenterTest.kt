package com.tinysol.designsystem.core.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TSToastPresenterTest {
    private val scheduler = ManualToastScheduler()
    private val shown = mutableListOf<String>()
    private val changes = mutableListOf<String?>()

    private fun presenter(): TSToastPresenter {
        val queue = TSToastQueue(TSToastTimeouts.default)
        return TSToastPresenter(
            queue = queue,
            scheduler = scheduler,
            onShow = { shown += it.message.text },
            onChange = { changes += it?.message?.text },
        )
    }

    @Test
    fun showsQueuedToastsOneAtATimeInOrder() {
        val presenter = presenter().apply { start() }
        listOf("Project archived", "Link copied", "Saved").forEach { presenter.queue.enqueue(TSToastMessage(it)) }

        assertEquals("Project archived", presenter.current?.message?.text)
        scheduler.advanceBy(4_000)
        assertEquals("Link copied", presenter.current?.message?.text)
        scheduler.advanceBy(4_000)
        assertEquals("Saved", presenter.current?.message?.text)
        scheduler.advanceBy(4_000)

        assertNull(presenter.current)
        assertEquals(listOf("Project archived", "Link copied", "Saved"), shown)
        assertEquals(listOf(null, "Project archived", "Link copied", "Saved", null), changes)
    }

    @Test
    fun autoDismissWaitsForTheTimeout() {
        val presenter = presenter().apply { start() }
        presenter.queue.enqueue(TSToastMessage("Saved"))

        scheduler.advanceBy(3_999)
        assertEquals("Saved", presenter.current?.message?.text)
        scheduler.advanceBy(1)
        assertNull(presenter.current)
    }

    @Test
    fun actionRunsTheHandlerAndShowsTheNextToastAtOnce() {
        var undone = 0
        val presenter = presenter().apply { start() }
        val first = presenter.queue.enqueue(TSToastMessage("Message deleted", actionLabel = "Undo", onAction = { undone++ }))
        presenter.queue.enqueue(TSToastMessage("Saved"))

        assertTrue(presenter.performAction(first))

        assertEquals(1, undone)
        assertEquals("Saved", presenter.current?.message?.text)
        assertEquals(listOf(4_000L), scheduler.pendingDelays)
    }

    @Test
    fun aLateTimerCannotDismissTheNextToast() {
        val presenter = presenter().apply { start() }
        val first = presenter.queue.enqueue(TSToastMessage("First"))
        presenter.queue.enqueue(TSToastMessage("Second"))

        scheduler.advanceBy(3_000)
        presenter.dismiss(first)
        scheduler.advanceBy(1_000)

        assertEquals("Second", presenter.current?.message?.text)
        scheduler.advanceBy(3_000)
        assertNull(presenter.current)
    }

    @Test
    fun eachToastIsShownOnceAcrossRestarts() {
        val presenter = presenter().apply { start() }
        presenter.queue.enqueue(TSToastMessage("Saved"))
        presenter.stop()
        presenter.start()

        assertEquals(listOf("Saved"), shown)
    }

    @Test
    fun stopCancelsTheTimerAndIgnoresTheQueue() {
        val presenter = presenter().apply { start() }
        presenter.queue.enqueue(TSToastMessage("Saved"))
        presenter.stop()
        presenter.queue.enqueue(TSToastMessage("Later"))
        scheduler.advanceBy(10_000)

        assertEquals("Saved", presenter.current?.message?.text)
        assertEquals(listOf("Saved"), shown)
    }
}
