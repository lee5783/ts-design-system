import Foundation

public struct TSDialogAction {
    public let label: String
    public let variant: TSButtonVariant
    public let dismisses: Bool
    public let handler: @MainActor () -> Void
    public let afterDismissal: (@MainActor () -> Void)?

    public init(
        _ label: String,
        variant: TSButtonVariant = .secondary,
        dismisses: Bool = true,
        handler: @escaping @MainActor () -> Void = {}
    ) {
        self.label = label
        self.variant = variant
        self.dismisses = dismisses
        self.handler = handler
        self.afterDismissal = nil
    }

    public init(
        _ label: String,
        variant: TSButtonVariant = .secondary,
        handler: @escaping @MainActor () -> Void,
        afterDismissal: @escaping @MainActor () -> Void
    ) {
        self.label = label
        self.variant = variant
        self.dismisses = true
        self.handler = handler
        self.afterDismissal = afterDismissal
    }
}

public enum TSDialogLayout {
    public static func orderedActions(_ actions: [TSDialogAction], primaryFirst: Bool) -> [TSDialogAction] {
        primaryFirst ? actions.reversed() : actions
    }
}
