import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSToast behaviour")
struct TSToastBehaviorTests {
    private func makeQueue() -> (TSToastQueue, ManualToastScheduler) {
        let clock = ManualToastScheduler()
        return (TSToastQueue(scheduler: clock), clock)
    }

    private func size(of view: some View, width: CGFloat = 358) -> CGSize {
        UIHostingController(rootView: view).sizeThatFits(in: CGSize(width: width, height: CGFloat.greatestFiniteMagnitude))
    }

    private func shownMessage(in host: TSToastHostView) throws -> String {
        try host.inspect().find(TSToast.self).actualView().message
    }

    @Test("The message shows the tone icon", arguments: TSToastTone.allCases)
    func toneIcon(tone: TSToastTone) throws {
        let sut = TSToast("Project archived", tone: tone)
        let message = try sut.inspect().find(TSToastMessage.self)
        #expect(try message.actualView().resolved.icon == TSToastStyleSpec.icon(tone))
        #expect(throws: Never.self) { try message.find(text: "Project archived") }
    }

    @Test("The action is a separate button with its title that fires")
    func actionFires() throws {
        let counter = TapCounter()
        let sut = TSToast("Project archived", actionTitle: "Undo") { counter.tap() }
        let button = try sut.inspect().find(TSToastActionButton.self).find(ViewType.Button.self)
        #expect(throws: Never.self) { try button.find(text: "Undo") }
        try button.tap()
        #expect(counter.count == 1)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(TSToastMessage.self).find(ViewType.Button.self)
        }
    }

    @Test("Without an action handler or title there is no action button")
    func noAction() throws {
        #expect(throws: (any Error).self) { try TSToast("Saved").inspect().find(TSToastActionButton.self) }
        #expect(throws: (any Error).self) { try TSToast("Saved", actionTitle: "Undo").inspect().find(TSToastActionButton.self) }
    }

    @Test("One line is 12 + 20 + 12 tall, with or without an action, and the width stops at 420")
    func geometry() {
        #expect(abs(size(of: TSToast("Saved")).height - 44) < 1)
        #expect(abs(size(of: TSToast("Project archived", actionTitle: "Undo", onAction: {})).height - 44) < 1)
        #expect(size(of: TSToast("Saved"), width: 800).width <= 420)
    }

    @Test("The host shows queued toasts one at a time, in order")
    func hostOrder() throws {
        let (queue, clock) = makeQueue()
        queue.enqueue(TSToastItem(message: "First"))
        queue.enqueue(TSToastItem(message: "Second"))
        let host = TSToastHostView(queue: queue, bottomInset: 0) { _ in }
        #expect(try shownMessage(in: host) == "First")
        clock.fire(after: TSToastTimeout.standard)
        #expect(try shownMessage(in: host) == "Second")
        clock.fire(after: TSToastTimeout.standard)
        #expect(throws: (any Error).self) { try host.inspect().find(TSToast.self) }
    }

    @Test("A plain toast and an undo toast both leave after 4 s")
    func autoDismissTiming() throws {
        let (queue, clock) = makeQueue()
        let host = TSToastHostView(queue: queue, bottomInset: 0) { _ in }
        queue.enqueue(TSToastItem(message: "Saved"))
        #expect(clock.activeDelays == [4])
        clock.fire(after: 4)
        #expect(throws: (any Error).self) { try host.inspect().find(TSToast.self) }

        queue.enqueue(TSToastItem(message: "Project archived", action: TSToastAction(title: "Undo") {}))
        #expect(clock.activeDelays == [4])
        clock.fire(after: 4)
        #expect(throws: (any Error).self) { try host.inspect().find(TSToast.self) }
    }

    @Test("The host's action runs the handler, dismisses the toast and shows the next one")
    func hostAction() throws {
        let (queue, _) = makeQueue()
        let counter = TapCounter()
        queue.enqueue(TSToastItem(message: "Project archived", action: TSToastAction(title: "Undo") { counter.tap() }))
        queue.enqueue(TSToastItem(message: "Second"))
        let host = TSToastHostView(queue: queue, bottomInset: 0) { _ in }
        try host.inspect().find(TSToastActionButton.self).find(ViewType.Button.self).tap()
        #expect(counter.count == 1)
        #expect(try shownMessage(in: host) == "Second")
    }

    @Test("The overlay window appears above the app for a toast and hides after the exit animation")
    func overlayWindowVisibility() {
        let (queue, queueClock) = makeQueue()
        let windowClock = ManualToastScheduler()
        let coordinator = TSToastWindowCoordinator(
            queue: queue,
            effects: TSToastShowEffects(haptics: TSHaptics(isEnabled: false)),
            scheduler: windowClock
        )
        let window = TSToastOverlayWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        coordinator.install(window, sourceWindow: nil)
        #expect(window.isHidden)
        #expect(window.windowLevel > .normal)
        #expect(window.rootViewController === coordinator.hosting)

        queue.enqueue(TSToastItem(message: "Saved", tone: .success))
        #expect(!window.isHidden)

        queueClock.fire(after: 4)
        #expect(!window.isHidden)
        windowClock.fire(after: TSToastWindowing.hideDelay)
        #expect(window.isHidden)

        coordinator.detach()
        #expect(window.rootViewController == nil)
    }

    @Test("The overlay host lifts the toast above a keyboard that was already up, and drops it back when the keyboard hides")
    func overlayAvoidsKeyboard() throws {
        let (queue, _) = makeQueue()
        let coordinator = TSToastWindowCoordinator(queue: queue, effects: TSToastShowEffects(haptics: TSHaptics(isEnabled: false)))
        let window = TSToastOverlayWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        coordinator.install(window, sourceWindow: nil)
        let hosting = try #require(coordinator.hosting)
        hosting.avoidKeyboard(endFrame: CGRect(x: 0, y: 544, width: 390, height: 300), duration: 0)
        queue.enqueue(TSToastItem(message: "Saved", tone: .success))
        hosting.view.setNeedsLayout()
        window.layoutIfNeeded()
        let safeBottom = window.safeAreaInsets.bottom
        #expect(hosting.additionalSafeAreaInsets.bottom == 300 - safeBottom)

        hosting.avoidKeyboard(endFrame: .null, duration: 0)
        #expect(hosting.additionalSafeAreaInsets.bottom == 0)
        coordinator.detach()
    }

    @Test("The overlay window passes touches through everywhere except the toast")
    func overlayPassThrough() {
        let (queue, _) = makeQueue()
        let coordinator = TSToastWindowCoordinator(queue: queue, effects: TSToastShowEffects(haptics: TSHaptics(isEnabled: false)))
        let window = TSToastOverlayWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        coordinator.install(window, sourceWindow: nil)
        queue.enqueue(TSToastItem(message: "Project archived", action: TSToastAction(title: "Undo") {}))
        window.layoutIfNeeded()
        coordinator.hosting?.view.layoutIfNeeded()
        #expect(window.hitTest(CGPoint(x: 195, y: 400), with: nil) == nil)
        window.toastFrame = CGRect(x: 16, y: 784, width: 358, height: 44)
        #expect(window.hitTest(CGPoint(x: 195, y: 400), with: nil) == nil)
        #expect(window.hitTest(CGPoint(x: 8, y: 806), with: nil) == nil)
        #expect(window.hitTest(CGPoint(x: 195, y: 806), with: nil) != nil)
        window.toastFrame = .null
        #expect(window.hitTest(CGPoint(x: 195, y: 806), with: nil) == nil)
    }
}
