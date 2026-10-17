import Combine
import Foundation
import UIKit

public enum TSToastTone: String, CaseIterable, Hashable, Sendable {
    case neutral
    case success
    case warning
    case danger
    case info

    public var hapticEvent: TSHapticEvent? {
        switch self {
        case .neutral, .info: nil
        case .success: .success
        case .warning: .warning
        case .danger: .error
        }
    }
}

public struct TSToastAction {
    public let title: String
    public let handler: @MainActor () -> Void

    public init(title: String, handler: @escaping @MainActor () -> Void) {
        self.title = title
        self.handler = handler
    }
}

public struct TSToastItem: Identifiable {
    public let id: UUID
    public let message: String
    public let tone: TSToastTone
    public let action: TSToastAction?
    public let duration: TimeInterval?

    public init(
        id: UUID = UUID(),
        message: String,
        tone: TSToastTone = .neutral,
        action: TSToastAction? = nil,
        duration: TimeInterval? = nil
    ) {
        self.id = id
        self.message = message
        self.tone = tone
        self.action = action
        self.duration = duration
    }
}

public enum TSToastTimeout {
    public static let standard: TimeInterval = 4
    public static let extended: TimeInterval = 10

    public static func duration(
        hasAction: Bool,
        isVoiceOverRunning: Bool,
        standard: TimeInterval = standard
    ) -> TimeInterval? {
        switch (hasAction, isVoiceOverRunning) {
        case (true, true): nil
        case (false, false): standard
        default: max(standard, extended)
        }
    }
}

public protocol TSToastCancellable {
    func cancel()
}

@MainActor
public protocol TSToastScheduling {
    func schedule(after delay: TimeInterval, _ action: @escaping @MainActor () -> Void) -> any TSToastCancellable
}

public struct TSToastTaskScheduler: TSToastScheduling {
    public init() {}

    public func schedule(after delay: TimeInterval, _ action: @escaping @MainActor () -> Void) -> any TSToastCancellable {
        TSToastTask(task: Task { @MainActor in
            try? await Task.sleep(nanoseconds: UInt64(max(0, delay) * 1_000_000_000))
            guard !Task.isCancelled else { return }
            action()
        })
    }
}

struct TSToastTask: TSToastCancellable {
    let task: Task<Void, Never>

    func cancel() {
        task.cancel()
    }
}

@MainActor
public final class TSToastQueue: ObservableObject {
    public static let shared = TSToastQueue()

    @Published public private(set) var current: TSToastItem?
    @Published public private(set) var pending: [TSToastItem] = []

    private let scheduler: any TSToastScheduling
    private let isVoiceOverRunning: @MainActor () -> Bool
    private var timer: (any TSToastCancellable)?

    public convenience init() {
        self.init(scheduler: TSToastTaskScheduler(), isVoiceOverRunning: { UIAccessibility.isVoiceOverRunning })
    }

    public init(scheduler: any TSToastScheduling, isVoiceOverRunning: @escaping @MainActor () -> Bool) {
        self.scheduler = scheduler
        self.isVoiceOverRunning = isVoiceOverRunning
    }

    @discardableResult
    public func enqueue(_ item: TSToastItem) -> TSToastItem.ID {
        pending.append(item)
        showNextIfIdle()
        return item.id
    }

    public func timeout(for item: TSToastItem) -> TimeInterval? {
        TSToastTimeout.duration(
            hasAction: item.action != nil,
            isVoiceOverRunning: isVoiceOverRunning(),
            standard: item.duration ?? TSToastTimeout.standard
        )
    }

    public func dismiss(_ id: TSToastItem.ID) {
        if current?.id == id {
            timer?.cancel()
            timer = nil
            current = nil
            showNextIfIdle()
        } else {
            pending.removeAll { $0.id == id }
        }
    }

    public func dismissCurrent() {
        guard let current else { return }
        dismiss(current.id)
    }

    public func performAction() {
        guard let item = current, let action = item.action else { return }
        dismiss(item.id)
        action.handler()
    }

    public func cancelAll() {
        timer?.cancel()
        timer = nil
        pending.removeAll()
        current = nil
    }

    private func showNextIfIdle() {
        guard current == nil, !pending.isEmpty else { return }
        let next = pending.removeFirst()
        current = next
        guard let timeout = timeout(for: next) else { return }
        let id = next.id
        timer = scheduler.schedule(after: timeout) { [weak self] in
            self?.dismiss(id)
        }
    }
}
