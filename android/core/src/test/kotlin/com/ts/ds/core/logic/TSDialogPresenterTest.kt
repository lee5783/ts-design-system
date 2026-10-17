package com.ts.ds.core.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.lang.ref.WeakReference

class TSDialogPresenterTest {
    private val presenter = TSDialogPresenter()
    private val first = Any()
    private val second = Any()

    @Test
    fun firstPresentationIsGranted() {
        assertTrue(presenter.begin(first))
        assertTrue(presenter.isPresenting)
        assertTrue(presenter.isActive(first))
        assertEquals(0, presenter.refusedCount)
    }

    @Test
    fun secondPresentationIsRefusedNotQueued() {
        presenter.begin(first)
        assertFalse(presenter.begin(second))
        assertEquals(1, presenter.refusedCount)
        presenter.end(first)
        assertFalse(presenter.isPresenting)
        assertFalse(presenter.isActive(second))
    }

    @Test
    fun sameOwnerIsIdempotent() {
        assertTrue(presenter.begin(first))
        assertTrue(presenter.begin(first))
        assertEquals(0, presenter.refusedCount)
    }

    @Test
    fun onlyTheOwnerEnds() {
        presenter.begin(first)
        presenter.end(second)
        assertTrue(presenter.isActive(first))
        presenter.end(first)
        assertTrue(presenter.begin(second))
    }

    @Test
    fun ownersAreComparedByIdentity() {
        presenter.begin("owner")
        assertFalse(presenter.begin(String("owner".toCharArray())))
    }

    @Test
    fun listenersHearGrantsRefusalsAndEnds() {
        val events = mutableListOf<Pair<Boolean, Int>>()
        val listener: (TSDialogPresenter) -> Unit = { events += it.isPresenting to it.refusedCount }
        presenter.addListener(listener)
        presenter.begin(first)
        presenter.begin(first)
        presenter.begin(second)
        presenter.end(first)
        presenter.removeListener(listener)
        presenter.begin(second)
        assertEquals(listOf(true to 0, true to 1, false to 1), events)
    }

    @Test
    fun collectedOwnerFreesThePresenter() {
        val collected = claimWithUnreachableOwner()
        awaitCollection(collected)
        assertFalse(presenter.isPresenting)
        assertTrue(presenter.begin(second))
        assertEquals(0, presenter.refusedCount)
    }

    @Test
    fun presenterDoesNotRetainItsOwner() {
        val collected = claimWithUnreachableOwner()
        awaitCollection(collected)
        assertEquals(null, collected.get())
    }

    private fun claimWithUnreachableOwner(): WeakReference<Any> {
        val owner = Any()
        assertTrue(presenter.begin(owner))
        return WeakReference(owner)
    }

    private fun awaitCollection(reference: WeakReference<*>) {
        repeat(50) {
            if (reference.get() == null) return
            System.gc()
            Thread.sleep(10)
        }
        fail("The reference was not collected")
    }

    @Test
    fun sharedIsOneInstance() {
        assertSame(TSDialogPresenter.shared, TSDialogPresenter.shared)
    }
}
