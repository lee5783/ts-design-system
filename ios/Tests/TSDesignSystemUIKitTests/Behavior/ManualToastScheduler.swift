import Foundation
import TSFoundation

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

    func fire(after delay: TimeInterval) {
        for entry in entries where !entry.isCancelled && entry.delay == delay {
            entry.isCancelled = true
            entry.action()
        }
    }
}
