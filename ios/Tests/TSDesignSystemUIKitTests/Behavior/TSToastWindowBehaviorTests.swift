import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
final class AnnouncementLog {
    private(set) var messages: [String] = []

    func record(_ message: String) {
        messages.append(message)
    }
}

@MainActor
@Suite("TSToastWindow behaviour")
struct TSToastWindowBehaviorTests {
    private struct Fixture {
        let window: TSToastWindow
        let queue: TSToastQueue
        let queueClock: ManualToastScheduler
        let windowClock: ManualToastScheduler
        let log: AnnouncementLog
    }

    private func makeFixture(voiceOver: Bool = false) -> Fixture {
        let queueClock = ManualToastScheduler()
        let windowClock = ManualToastScheduler()
        let log = AnnouncementLog()
        let queue = TSToastQueue(scheduler: queueClock, isVoiceOverRunning: { voiceOver })
        let window = TSToastWindow(
            frame: CGRect(x: 0, y: 0, width: 390, height: 844),
            queue: queue,
            effects: TSToastShowEffects(announce: { log.record($0) }, haptics: TSHaptics(isEnabled: false)),
            scheduler: windowClock
        )
        return Fixture(window: window, queue: queue, queueClock: queueClock, windowClock: windowClock, log: log)
    }

    private func shownMessage(_ fixture: Fixture) -> String? {
        fixture.window.hostController.toastView?.message
    }

    @Test("The window sits above the app, stays hidden until a toast is queued and hides after the exit animation")
    func visibility() {
        let fixture = makeFixture()
        let window = fixture.window
        #expect(window.isHidden)
        #expect(window.windowLevel > .normal)
        #expect(!window.isKeyWindow)
        fixture.queue.enqueue(TSToastItem(message: "Saved", tone: .success))
        #expect(!window.isHidden)
        #expect(fixture.log.messages == ["Success, Saved"])
        fixture.queueClock.fire(after: 4)
        #expect(shownMessage(fixture) == nil)
        #expect(!window.isHidden)
        fixture.windowClock.fire(after: TSToastWindowing.hideDelay)
        #expect(window.isHidden)
    }

    @Test("Queued toasts are shown one at a time, in order, each announced once")
    func order() {
        let fixture = makeFixture()
        ["First", "Second", "Third"].forEach { fixture.queue.enqueue(TSToastItem(message: $0)) }
        #expect(shownMessage(fixture) == "First")
        fixture.queueClock.fire(after: 4)
        #expect(shownMessage(fixture) == "Second")
        fixture.queueClock.fire(after: 4)
        #expect(shownMessage(fixture) == "Third")
        #expect(fixture.log.messages == ["First", "Second", "Third"])
        #expect(fixture.windowClock.activeDelays.isEmpty)
        #expect(!fixture.window.isHidden)
    }

    @Test("A plain toast leaves after 4 s, an undo toast after 10 s, and stays with VoiceOver and an action")
    func autoDismissTiming() {
        let fixture = makeFixture()
        fixture.queue.enqueue(TSToastItem(message: "Project archived", action: TSToastAction(title: "Undo") {}))
        #expect(fixture.queueClock.activeDelays == [10])
        fixture.queueClock.fire(after: 4)
        #expect(shownMessage(fixture) == "Project archived")
        fixture.queueClock.fire(after: 10)
        #expect(shownMessage(fixture) == nil)

        let voiceOver = makeFixture(voiceOver: true)
        voiceOver.queue.enqueue(TSToastItem(message: "Project archived", action: TSToastAction(title: "Undo") {}))
        #expect(voiceOver.queueClock.activeDelays.isEmpty)
        #expect(shownMessage(voiceOver) == "Project archived")
        #expect(voiceOver.log.messages == ["Project archived, Undo"])
    }

    @Test("The action runs the handler, dismisses the toast and shows the next one")
    func action() throws {
        let fixture = makeFixture()
        let counter = TapCounter()
        fixture.queue.enqueue(TSToastItem(message: "Project archived", action: TSToastAction(title: "Undo") { counter.tap() }))
        fixture.queue.enqueue(TSToastItem(message: "Second"))
        let button = try #require(fixture.window.hostController.toastView?.actionButton)
        #expect(button.accessibilityLabel == "Undo")
        button.sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
        #expect(shownMessage(fixture) == "Second")
    }

    @Test("Touches outside the toast pass through the window; the toast and its action receive theirs")
    func passThrough() throws {
        let fixture = makeFixture()
        let window = fixture.window
        #expect(window.hitTest(CGPoint(x: 195, y: 400), with: nil) == nil)
        fixture.queue.enqueue(TSToastItem(message: "Project archived", action: TSToastAction(title: "Undo") {}))
        window.layoutIfNeeded()
        let toast = try #require(fixture.window.hostController.toastView)
        let frame = toast.convert(toast.bounds, to: window)
        #expect(frame.height >= 44)
        #expect(frame.maxY <= 844 - 16)
        #expect(window.hitTest(CGPoint(x: 195, y: 400), with: nil) == nil)
        #expect(window.hitTest(CGPoint(x: 195, y: frame.minY - 4), with: nil) == nil)
        #expect(window.hitTest(CGPoint(x: 4, y: frame.midY), with: nil) == nil)
        let messagePoint = CGPoint(x: frame.minX + 24, y: frame.midY)
        #expect(window.hitTest(messagePoint, with: nil)?.isDescendant(of: toast) == true)
        let action = toast.actionButton.convert(CGPoint(x: toast.actionButton.bounds.midX, y: toast.actionButton.bounds.midY), to: window)
        #expect(window.hitTest(action, with: nil) === toast.actionButton)
    }

    @Test("The bottom inset lifts the toast above a bottom bar")
    func bottomInset() throws {
        let fixture = makeFixture()
        fixture.window.bottomInset = 56
        fixture.queue.enqueue(TSToastItem(message: "Saved"))
        fixture.window.layoutIfNeeded()
        let host = fixture.window.hostController
        let toast = try #require(host.toastView)
        let keyboardTop = host.view.keyboardLayoutGuide.layoutFrame.minY
        #expect(abs(toast.frame.maxY - (keyboardTop - 16 - 56)) < 1)
    }

    @Test("applyTraits forwards the source's style, text size and layout direction")
    func traits() throws {
        let fixture = makeFixture()
        fixture.window.applyTraits(from: UITraitCollection(traitsFrom: [
            UITraitCollection(userInterfaceStyle: .dark),
            UITraitCollection(preferredContentSizeCategory: .accessibilityExtraExtraExtraLarge),
            UITraitCollection(layoutDirection: .rightToLeft)
        ]))
        fixture.queue.enqueue(TSToastItem(message: "Saved"))
        fixture.window.layoutIfNeeded()
        let host = fixture.window.hostController
        #expect(fixture.window.overrideUserInterfaceStyle == .dark)
        #expect(host.traitCollection.preferredContentSizeCategory == .accessibilityExtraExtraExtraLarge)
        #expect(host.traitCollection.layoutDirection == .rightToLeft)
        let toast = try #require(host.toastView)
        #expect(toast.effectiveUserInterfaceLayoutDirection == .rightToLeft)
    }
}
