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

    public var customAction: TSCardAction {
        switch self {
        case let .iconButton(_, label, action):
            TSCardAction(label, handler: action)
        }
    }
}

public enum TSCardAccessibility {
    public static func label(title: String?, subtitle: String?, body: String? = nil) -> String {
        let heading = [title, subtitle]
            .compactMap { $0 }
            .filter { !$0.isEmpty }
            .joined(separator: ", ")
        return heading.isEmpty ? body ?? "" : heading
    }

    public static func value(title: String?, subtitle: String?, body: String?) -> String? {
        let hasHeading = [title, subtitle].contains { !($0 ?? "").isEmpty }
        guard hasHeading, let body, !body.isEmpty else { return nil }
        return body
    }

    public static func customActions(footer: [TSCardAction], accessory: TSCardAccessory? = nil, additional: [TSCardAction]) -> [TSCardAction] {
        footer + [accessory?.customAction].compactMap { $0 } + additional
    }
}
