import Testing
import TSFoundation

@MainActor
@Suite("TSDialogPresenter")
struct TSDialogPresenterTests {
    private final class Owner {}

    @Test("Queue is the default policy, also for the shared presenter")
    func queueIsDefault() {
        #expect(TSDialogPresenter().policy == .queue)
        #expect(TSDialogPresenter.shared.policy == .queue)
    }

    @Test("The first dialog is granted and a second imperative begin is refused under either policy")
    func refusesSecondBegin() {
        for policy in [TSDialogPolicy.queue, .refuse] {
            let presenter = TSDialogPresenter(policy: policy)
            let first = Owner()
            let second = Owner()
            #expect(presenter.begin(first))
            #expect(presenter.isPresenting)
            #expect(presenter.isActive(first))
            #expect(!presenter.begin(second))
            #expect(presenter.refusedCount == 1)
            #expect(presenter.pendingCount == 0)
            #expect(presenter.isActive(first))
            #expect(!presenter.isActive(second))
        }
    }

    @Test("A request on a free presenter is granted at once without calling back")
    func requestGranted() {
        let presenter = TSDialogPresenter()
        let owner = Owner()
        var granted = 0
        #expect(presenter.request(owner) { granted += 1 } == .granted)
        #expect(presenter.isActive(owner))
        #expect(presenter.request(owner) { granted += 1 } == .granted)
        #expect(granted == 0)
    }

    @Test("A conflicting request is queued and granted after the open dialog ends")
    func queuedThenGranted() {
        let presenter = TSDialogPresenter()
        let first = Owner()
        let second = Owner()
        var granted = 0
        presenter.begin(first)
        #expect(presenter.request(second) { granted += 1 } == .queued)
        #expect(presenter.pendingCount == 1)
        #expect(presenter.isPending(second))
        #expect(presenter.refusedCount == 0)
        #expect(granted == 0)
        presenter.end(first)
        #expect(granted == 1)
        #expect(presenter.isActive(second))
        #expect(presenter.pendingCount == 0)
    }

    @Test("The refuse policy refuses a conflicting request and never grants it")
    func refusePolicy() {
        let presenter = TSDialogPresenter(policy: .refuse)
        let first = Owner()
        let second = Owner()
        var granted = 0
        presenter.begin(first)
        #expect(presenter.request(second) { granted += 1 } == .refused)
        #expect(presenter.refusedCount == 1)
        #expect(presenter.pendingCount == 0)
        presenter.end(first)
        #expect(granted == 0)
        #expect(!presenter.isPresenting)
    }

    @Test("Pending requests are granted first in, first out, one at a time")
    func fifo() {
        let presenter = TSDialogPresenter()
        let first = Owner()
        let second = Owner()
        let third = Owner()
        var order: [String] = []
        presenter.begin(first)
        presenter.request(second) { order.append("second") }
        presenter.request(third) { order.append("third") }
        #expect(presenter.pendingCount == 2)
        presenter.end(first)
        #expect(order == ["second"])
        presenter.end(second)
        #expect(order == ["second", "third"])
        #expect(presenter.isActive(third))
    }

    @Test("End withdraws a pending request")
    func endWithdraws() {
        let presenter = TSDialogPresenter()
        let first = Owner()
        let second = Owner()
        var granted = 0
        presenter.begin(first)
        presenter.request(second) { granted += 1 }
        presenter.end(second)
        #expect(presenter.pendingCount == 0)
        #expect(presenter.isActive(first))
        presenter.end(first)
        #expect(granted == 0)
        #expect(!presenter.isPresenting)
    }

    @Test("A repeated request keeps one place in the queue and the latest callback")
    func noDuplicates() {
        let presenter = TSDialogPresenter()
        let first = Owner()
        let second = Owner()
        let third = Owner()
        var calls: [String] = []
        presenter.begin(first)
        presenter.request(second) { calls.append("old") }
        presenter.request(third) { calls.append("third") }
        presenter.request(second) { calls.append("new") }
        #expect(presenter.pendingCount == 2)
        presenter.end(first)
        #expect(calls == ["new"])
    }

    @Test("Handing over moves the slot to the chained owner without granting the queue; only the owner can hand over")
    func handOver() {
        let presenter = TSDialogPresenter()
        let first = Owner()
        let chained = Owner()
        let queued = Owner()
        var granted = 0
        presenter.begin(first)
        presenter.request(queued) { granted += 1 }
        #expect(!presenter.handOver(from: chained, to: first))
        #expect(presenter.handOver(from: first, to: chained))
        #expect(presenter.isActive(chained))
        #expect(granted == 0)
        presenter.end(first)
        #expect(presenter.isActive(chained))
        presenter.end(chained)
        #expect(granted == 1)
        #expect(presenter.isActive(queued))
    }

    @Test("A deallocated pending owner is skipped")
    func deallocatedPendingOwner() {
        let presenter = TSDialogPresenter()
        let first = Owner()
        let second = Owner()
        var gone: Owner? = Owner()
        var granted: [String] = []
        presenter.begin(first)
        presenter.request(gone!) { granted.append("gone") }
        presenter.request(second) { granted.append("second") }
        gone = nil
        #expect(presenter.pendingCount == 1)
        presenter.end(first)
        #expect(granted == ["second"])
        #expect(presenter.isActive(second))
    }

    @Test("The open dialog can ask again without being refused")
    func sameOwnerIsIdempotent() {
        let presenter = TSDialogPresenter()
        let owner = Owner()
        #expect(presenter.begin(owner))
        #expect(presenter.begin(owner))
        #expect(presenter.refusedCount == 0)
    }

    @Test("Only the open dialog can end the presentation; afterwards another is granted")
    func endReleases() {
        let presenter = TSDialogPresenter()
        let first = Owner()
        let second = Owner()
        presenter.begin(first)
        presenter.end(second)
        #expect(presenter.isActive(first))
        presenter.end(first)
        #expect(!presenter.isPresenting)
        #expect(presenter.begin(second))
        #expect(presenter.isActive(second))
    }

    @Test("The presenter holds its owner weakly, so a deallocated owner frees it for the next dialog")
    func weakOwner() {
        let presenter = TSDialogPresenter()
        var owner: Owner? = Owner()
        #expect(presenter.begin(owner!))
        #expect(presenter.isPresenting)
        owner = nil
        #expect(!presenter.isPresenting)
        #expect(presenter.activeOwner == nil)
        let next = Owner()
        #expect(presenter.begin(next))
        #expect(presenter.isActive(next))
        #expect(presenter.refusedCount == 0)
    }

    @Test("The shared presenter is one instance")
    func shared() {
        #expect(TSDialogPresenter.shared === TSDialogPresenter.shared)
    }
}
