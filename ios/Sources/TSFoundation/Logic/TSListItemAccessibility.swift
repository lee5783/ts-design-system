import Foundation

public enum TSListItemRole: String, CaseIterable, Hashable, Sendable {
    case plain
    case action
    case navigation
    case toggle

    public static func make(hasAction: Bool, showsChevron: Bool, hasToggle: Bool) -> TSListItemRole {
        if hasToggle {
            return .toggle
        }
        guard hasAction else { return .plain }
        return showsChevron ? .navigation : .action
    }

    public var isPressable: Bool {
        self == .action || self == .navigation
    }
}

public enum TSListItemAccessibility {
    public static func label(title: String, subtitle: String?) -> String {
        guard let subtitle, !subtitle.isEmpty else { return title }
        return "\(title), \(subtitle)"
    }

    public static func hint(for role: TSListItemRole) -> String? {
        role == .navigation ? TSStrings.listItemNavigates : nil
    }
}
