import Combine
import Foundation
import os

public enum TSDialogPolicy: Sendable {
    case queue
    case refuse
}

public enum TSDialogRequestResult: Sendable {
    case granted
    case queued
    case refused
}

@MainActor
public final class TSDialogPresenter: ObservableObject {
    public static let shared = TSDialogPresenter()
    private static let logger = Logger(subsystem: "com.ts.ds", category: "TSDialogPresenter")

    public var policy: TSDialogPolicy
    @Published public private(set) var refusedCount = 0
    private weak var owner: AnyObject?
    private var pending: [PendingRequest] = []

    public init() {
        policy = .queue
    }

    public init(policy: TSDialogPolicy) {
        self.policy = policy
    }

    public var activeOwner: ObjectIdentifier? {
        owner.map(ObjectIdentifier.init)
    }

    public var isPresenting: Bool {
        owner != nil
    }

    public var pendingCount: Int {
        pending.filter { $0.owner != nil }.count
    }

    public func isActive(_ owner: AnyObject) -> Bool {
        self.owner === owner
    }

    public func isPending(_ owner: AnyObject) -> Bool {
        pending.contains { $0.owner === owner }
    }

    @discardableResult
    public func begin(_ owner: AnyObject) -> Bool {
        grantNext()
        guard self.owner == nil || self.owner === owner else {
            refuse(owner)
            return false
        }
        guard self.owner == nil else { return true }
        objectWillChange.send()
        self.owner = owner
        return true
    }

    @discardableResult
    public func request(_ owner: AnyObject, onGranted: @escaping @MainActor () -> Void) -> TSDialogRequestResult {
        prunePending()
        grantNext()
        if self.owner == nil || self.owner === owner {
            begin(owner)
            return .granted
        }
        guard policy == .queue else {
            refuse(owner)
            return .refused
        }
        if let existing = pending.first(where: { $0.owner === owner }) {
            existing.onGranted = onGranted
        } else {
            objectWillChange.send()
            pending.append(PendingRequest(owner: owner, onGranted: onGranted))
        }
        return .queued
    }

    @discardableResult
    public func handOver(from: AnyObject, to: AnyObject) -> Bool {
        guard owner === from else { return false }
        objectWillChange.send()
        pending.removeAll { $0.owner === to }
        owner = to
        return true
    }

    public func end(_ owner: AnyObject) {
        prunePending()
        if self.owner === owner {
            objectWillChange.send()
            self.owner = nil
            grantNext()
            return
        }
        guard let index = pending.firstIndex(where: { $0.owner === owner }) else { return }
        objectWillChange.send()
        pending.remove(at: index)
    }

    private func refuse(_ owner: AnyObject) {
        refusedCount += 1
        #if DEBUG
        Self.logger.debug("Refused a dialog presentation: another dialog is open (\(String(describing: type(of: owner)), privacy: .public))")
        #endif
    }

    private func prunePending() {
        pending.removeAll { $0.owner == nil }
    }

    private func grantNext() {
        guard owner == nil else { return }
        while !pending.isEmpty {
            let next = pending.removeFirst()
            guard let nextOwner = next.owner else { continue }
            objectWillChange.send()
            owner = nextOwner
            next.onGranted()
            return
        }
    }

    private final class PendingRequest {
        weak var owner: AnyObject?
        var onGranted: @MainActor () -> Void

        init(owner: AnyObject, onGranted: @escaping @MainActor () -> Void) {
            self.owner = owner
            self.onGranted = onGranted
        }
    }
}
