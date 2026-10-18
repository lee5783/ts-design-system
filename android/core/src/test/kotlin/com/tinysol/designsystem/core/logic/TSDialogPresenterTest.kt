package com.tinysol.designsystem.core.logic

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
    fun queueIsTheDefaultPolicy() {
        assertEquals(TSDialogPolicy.Queue, presenter.policy)
        assertEquals(TSDialogPolicy.Queue, TSDialogPresenter.shared.policy)
    }

    @Test
    fun secondBeginIsRefusedUnderEitherPolicy() {
        TSDialogPolicy.entries.forEach { policy ->
            val presenter = TSDialogPresenter(policy)
            presenter.begin(first)
            assertFalse(presenter.begin(second))
            assertEquals(1, presenter.refusedCount)
            assertEquals(0, presenter.pendingCount)
            presenter.end(first)
            assertFalse(presenter.isPresenting)
            assertFalse(presenter.isActive(second))
        }
    }

    @Test
    fun requestOnAFreePresenterIsGranted() {
        var granted = 0
        assertEquals(TSDialogRequestResult.Granted, presenter.request(first) { granted += 1 })
        assertTrue(presenter.isActive(first))
        assertEquals(TSDialogRequestResult.Granted, presenter.request(first) { granted += 1 })
        assertEquals(0, granted)
    }

    @Test
    fun conflictingRequestIsQueuedAndGrantedAfterTheCurrentEnds() {
        var granted = 0
        presenter.begin(first)
        assertEquals(TSDialogRequestResult.Queued, presenter.request(second) { granted += 1 })
        assertEquals(1, presenter.pendingCount)
        assertTrue(presenter.isPending(second))
        assertEquals(0, presenter.refusedCount)
        assertEquals(0, granted)
        presenter.end(first)
        assertEquals(1, granted)
        assertTrue(presenter.isActive(second))
        assertEquals(0, presenter.pendingCount)
    }

    @Test
    fun refusePolicyRefusesAConflictingRequest() {
        val presenter = TSDialogPresenter(TSDialogPolicy.Refuse)
        var granted = 0
        presenter.begin(first)
        assertEquals(TSDialogRequestResult.Refused, presenter.request(second) { granted += 1 })
        assertEquals(1, presenter.refusedCount)
        assertEquals(0, presenter.pendingCount)
        presenter.end(first)
        assertEquals(0, granted)
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun pendingRequestsAreGrantedInOrderOneAtATime() {
        val third = Any()
        val order = mutableListOf<Any>()
        presenter.begin(first)
        presenter.request(second) { order += second }
        presenter.request(third) { order += third }
        assertEquals(2, presenter.pendingCount)
        presenter.end(first)
        assertEquals(listOf(second), order)
        assertTrue(presenter.isActive(second))
        presenter.end(second)
        assertEquals(listOf(second, third), order)
        assertTrue(presenter.isActive(third))
    }

    @Test
    fun endWithdrawsAPendingRequest() {
        var granted = 0
        presenter.begin(first)
        presenter.request(second) { granted += 1 }
        presenter.end(second)
        assertEquals(0, presenter.pendingCount)
        assertTrue(presenter.isActive(first))
        presenter.end(first)
        assertEquals(0, granted)
        assertFalse(presenter.isPresenting)
    }

    @Test
    fun repeatedRequestKeepsOnePlaceAndTheLatestCallback() {
        val third = Any()
        val calls = mutableListOf<String>()
        presenter.begin(first)
        presenter.request(second) { calls += "old" }
        presenter.request(third) { calls += "third" }
        presenter.request(second) { calls += "new" }
        assertEquals(2, presenter.pendingCount)
        presenter.end(first)
        assertEquals(listOf("new"), calls)
    }

    @Test
    fun collectedPendingOwnerIsSkipped() {
        var granted = 0
        presenter.begin(first)
        val collected = queueUnreachableOwner()
        presenter.request(second) { granted += 1 }
        awaitCollection(collected)
        assertEquals(1, presenter.pendingCount)
        presenter.end(first)
        assertEquals(1, granted)
        assertTrue(presenter.isActive(second))
    }

    @Test
    fun collectedActiveOwnerLetsTheNextRequestIn() {
        var granted = 0
        val collected = claimWithUnreachableOwner()
        awaitCollection(collected)
        assertEquals(TSDialogRequestResult.Granted, presenter.request(second) { granted += 1 })
        assertTrue(presenter.isActive(second))
        assertEquals(0, granted)
    }

    @Test
    fun handOverMovesTheSlotWithoutGrantingTheQueue() {
        val third = Any()
        var granted = 0
        presenter.begin(first)
        presenter.request(third) { granted += 1 }
        assertTrue(presenter.handOver(first, second))
        assertTrue(presenter.isActive(second))
        assertEquals(0, granted)
        assertEquals(1, presenter.pendingCount)
        presenter.end(first)
        assertTrue(presenter.isActive(second))
        presenter.end(second)
        assertEquals(1, granted)
        assertTrue(presenter.isActive(third))
    }

    @Test
    fun handOverFromANonOwnerIsIgnored() {
        presenter.begin(first)
        assertFalse(presenter.handOver(second, Any()))
        assertTrue(presenter.isActive(first))
    }

    @Test
    fun listenersHearEnqueueGrantAndWithdrawal() {
        val events = mutableListOf<Pair<Boolean, Int>>()
        presenter.begin(first)
        presenter.addListener { events += it.isActive(second) to it.pendingCount }
        presenter.request(second) {}
        presenter.end(first)
        presenter.end(second)
        assertEquals(listOf(false to 1, false to 1, true to 0, false to 0), events)
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

    private fun queueUnreachableOwner(): WeakReference<Any> {
        val owner = Any()
        assertEquals(TSDialogRequestResult.Queued, presenter.request(owner) {})
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
