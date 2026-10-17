import Foundation

public enum DemoTextSize: String, CaseIterable, Hashable, Sendable, Identifiable {
    case system
    case extraSmall
    case small
    case medium
    case large
    case extraLarge
    case extraExtraLarge
    case extraExtraExtraLarge
    case accessibility1
    case accessibility2
    case accessibility3
    case accessibility4
    case accessibility5

    public var id: String { rawValue }

    public var isAccessibilitySize: Bool {
        switch self {
        case .accessibility1, .accessibility2, .accessibility3, .accessibility4, .accessibility5:
            true
        default:
            false
        }
    }

    public var title: String {
        DemoStrings.textSizeTitle(self)
    }
}
