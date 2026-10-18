import Combine
import Foundation
import Testing
@testable import TSFoundation

@MainActor
final class ManualToastScheduler: TSToastScheduling {
    final class Entry: TSToastCancellable {
        let delay: TimeInterval
        let action: @MainActor () -> Void
        var isCancelled = false

        init(delay: TimeInterval, action: @escaping @MainActor () -> Void) {
            self.delay = delay
            self.action = action
        }

        func cancel() {
            isCancelled = true
        }
    }

    private(set) var entries: [Entry] = []

    var activeDelays: [TimeInterval] {
        entries.filter { !$0.isCancelled }.map(\.delay)
    }

    func schedule(after delay: TimeInterval, _ action: @escaping @MainActor () -> Void) -> any TSToastCancellable {
        let entry = Entry(delay: delay, action: action)
        entries.append(entry)
        return entry
    }

    func fireActive() {
        for entry in entries where !entry.isCancelled {
            entry.isCancelled = true
            entry.action()
        }
    }
}

@MainActor
@Suite("TSToastQueue")
struct TSToastQueueTests {
    private func makeQueue() -> (TSToastQueue, ManualToastScheduler) {
        let scheduler = ManualToastScheduler()
        let queue = TSToastQueue(scheduler: scheduler)
        return (queue, scheduler)
    }

    @Test("Toasts are shown one at a time in FIFO order")
    func fifo() {
        let (queue, scheduler) = makeQueue()
        let first = queue.enqueue(TSToastItem(message: "First"))
        queue.enqueue(TSToastItem(message: "Second"))
        queue.enqueue(TSToastItem(message: "Third"))

        #expect(queue.current?.id == first)
        #expect(queue.pending.map(\.message) == ["Second", "Third"])

        scheduler.fireActive()
        #expect(queue.current?.message == "Second")
        scheduler.fireActive()
        #expect(queue.current?.message == "Third")
        scheduler.fireActive()
        #expect(queue.current == nil)
        #expect(queue.pending.isEmpty)
    }

    @Test("A plain toast auto-dismisses after 4 seconds")
    func defaultTimeout() {
        let (queue, scheduler) = makeQueue()
        queue.enqueue(TSToastItem(message: "Saved"))
        #expect(scheduler.activeDelays == [4])
    }

    @Test("A custom duration replaces the 4 second default")
    func customDuration() {
        let (queue, scheduler) = makeQueue()
        queue.enqueue(TSToastItem(message: "Saved", duration: 6))
        #expect(scheduler.activeDelays == [6])
    }

    @Test("An action extends the timeout")
    func actionExtends() {
        let (queue, scheduler) = makeQueue()
        queue.enqueue(TSToastItem(message: "Deleted", action: TSToastAction(title: "Undo") {}))
        #expect(scheduler.activeDelays == [TSToastTimeout.extended])
        #expect(TSToastTimeout.extended > TSToastTimeout.standard)
    }

    @Test("Cancelling the current toast stops its timer and shows the next one")
    func cancelCurrent() {
        let (queue, scheduler) = makeQueue()
        let first = queue.enqueue(TSToastItem(message: "First"))
        queue.enqueue(TSToastItem(message: "Second"))

        queue.dismiss(first)

        #expect(queue.current?.message == "Second")
        #expect(scheduler.entries.first?.isCancelled == true)
        #expect(scheduler.activeDelays == [4])
    }

    @Test("Cancelling a pending toast removes it without touching the current one")
    func cancelPending() {
        let (queue, scheduler) = makeQueue()
        queue.enqueue(TSToastItem(message: "First"))
        let second = queue.enqueue(TSToastItem(message: "Second"))
        queue.enqueue(TSToastItem(message: "Third"))

        queue.dismiss(second)

        #expect(queue.current?.message == "First")
        #expect(queue.pending.map(\.message) == ["Third"])
        #expect(scheduler.activeDelays == [4])
    }

    @Test("cancelAll clears the queue and the timer")
    func cancelAll() {
        let (queue, scheduler) = makeQueue()
        queue.enqueue(TSToastItem(message: "First"))
        queue.enqueue(TSToastItem(message: "Second"))

        queue.cancelAll()

        #expect(queue.current == nil)
        #expect(queue.pending.isEmpty)
        #expect(scheduler.activeDelays.isEmpty)
    }

    @Test("A fired timer for a dismissed toast does not dismiss the next one")
    func staleTimer() {
        let (queue, scheduler) = makeQueue()
        let first = queue.enqueue(TSToastItem(message: "First"))
        queue.enqueue(TSToastItem(message: "Second"))
        let staleTimer = scheduler.entries[0]

        queue.dismiss(first)
        staleTimer.action()

        #expect(queue.current?.message == "Second")
    }

    @Test("performAction dismisses the toast and runs the handler")
    func performAction() {
        let (queue, _) = makeQueue()
        var undone = false
        queue.enqueue(TSToastItem(message: "Deleted", action: TSToastAction(title: "Undo") { undone = true }))

        queue.performAction()

        #expect(undone)
        #expect(queue.current == nil)
    }

    @Test("The queue publishes changes to the current toast")
    func publishes() {
        let (queue, _) = makeQueue()
        var changes = 0
        let subscription = queue.objectWillChange.sink { changes += 1 }
        queue.enqueue(TSToastItem(message: "Saved"))
        subscription.cancel()
        #expect(changes > 0)
    }

    @Test(
        "Tones map to notification haptics",
        arguments: [
            (TSToastTone.neutral, nil),
            (.info, nil),
            (.success, TSHapticEvent.success),
            (.warning, .warning),
            (.danger, .error),
        ]
    )
    func toneHaptics(tone: TSToastTone, expected: TSHapticEvent?) {
        #expect(tone.hapticEvent == expected)
    }
}
