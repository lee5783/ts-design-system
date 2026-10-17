import Combine
import Foundation
import os

@MainActor
public final class TSDialogPresenter: ObservableObject {
    public static let shared = TSDialogPresenter()
    private static let logger = Logger(subsystem: "com.ts.ds", category: "TSDialogPresenter")

    @Published public private(set) var refusedCount = 0
    private weak var owner: AnyObject?

    public init() {}

    public var activeOwner: ObjectIdentifier? {
        owner.map(ObjectIdentifier.init)
    }

    public var isPresenting: Bool {
        owner != nil
    }

    public func isActive(_ owner: AnyObject) -> Bool {
        self.owner === owner
    }

    @discardableResult
    public func begin(_ owner: AnyObject) -> Bool {
        guard self.owner == nil || self.owner === owner else {
            refusedCount += 1
            #if DEBUG
            Self.logger.debug("Refused a dialog presentation: another dialog is open (\(String(describing: type(of: owner)), privacy: .public))")
            #endif
            return false
        }
        guard self.owner == nil else { return true }
        objectWillChange.send()
        self.owner = owner
        return true
    }

    public func end(_ owner: AnyObject) {
        guard self.owner === owner else { return }
        objectWillChange.send()
        self.owner = nil
    }
}
