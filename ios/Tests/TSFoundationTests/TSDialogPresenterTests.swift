import Testing
import TSFoundation

@MainActor
@Suite("TSDialogPresenter")
struct TSDialogPresenterTests {
    private final class Owner {}

    @Test("The first dialog is granted and a second one is refused while it is open")
    func refusesSecond() {
        let presenter = TSDialogPresenter()
        let first = Owner()
        let second = Owner()
        #expect(presenter.begin(first))
        #expect(presenter.isPresenting)
        #expect(presenter.isActive(first))
        #expect(!presenter.begin(second))
        #expect(presenter.refusedCount == 1)
        #expect(presenter.isActive(first))
        #expect(!presenter.isActive(second))
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
