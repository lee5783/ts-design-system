import Foundation
import TSTokens

public struct TSCardAction {
    public let label: String
    public let variant: TSButtonVariant
    public let handler: @MainActor () -> Void

    public init(_ label: String, variant: TSButtonVariant = .secondary, handler: @escaping @MainActor () -> Void) {
        self.label = label
        self.variant = variant
        self.handler = handler
    }
}

public enum TSCardAccessory {
    case iconButton(icon: TSIconName, label: String, action: @MainActor () -> Void)
}
