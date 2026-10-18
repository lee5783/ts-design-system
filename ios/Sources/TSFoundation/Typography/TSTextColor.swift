import TSTokens

public enum TSTextColor: String, CaseIterable, Hashable, Sendable {
    case primary
    case secondary
    case muted
    case disabled
    case inverse
    case onAccent

    public var color: TSColor {
        switch self {
        case .primary: .textPrimary
        case .secondary: .textSecondary
        case .muted: .textMuted
        case .disabled: .textDisabled
        case .inverse: .textInverse
        case .onAccent: .textOnAccent
        }
    }
}
