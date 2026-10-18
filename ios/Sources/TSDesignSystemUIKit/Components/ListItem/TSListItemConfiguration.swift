import UIKit

public enum TSListItemLeading: Equatable {
    case none
    case icon(TSIconName)
    case avatar(name: String, image: UIImage? = nil)
}

public enum TSListItemTrailing: Equatable {
    case none
    case text(String)
    case badge(String, tone: TSBadgeTone = .neutral)
    case toggle(isOn: Bool)

    var isOn: Bool? {
        guard case let .toggle(isOn) = self else { return nil }
        return isOn
    }
}

public struct TSListItemConfiguration: UIContentConfiguration {
    public var title: String
    public var subtitle: String?
    public var leading: TSListItemLeading
    public var trailing: TSListItemTrailing
    public var showsChevron: Bool
    public var isInteractive: Bool
    public var isEnabled: Bool
    public var onToggle: (@MainActor (Bool) -> Void)?
    public internal(set) var isHighlighted = false
    public internal(set) var isFocused = false
    var expansionState: Bool?

    public init(
        title: String,
        subtitle: String? = nil,
        leading: TSListItemLeading = .none,
        trailing: TSListItemTrailing = .none,
        showsChevron: Bool = false,
        isInteractive: Bool = false,
        isEnabled: Bool = true,
        onToggle: (@MainActor (Bool) -> Void)? = nil
    ) {
        self.title = title
        self.subtitle = subtitle
        self.leading = leading
        self.trailing = trailing
        self.showsChevron = showsChevron
        self.isInteractive = isInteractive
        self.isEnabled = isEnabled
        self.onToggle = onToggle
    }

    public var role: TSListItemRole {
        .make(hasAction: isInteractive, showsChevron: showsChevron, hasToggle: trailing.isOn != nil)
    }

    public var controlState: TSControlState {
        .make(isPressed: isHighlighted, isEnabled: isEnabled, isFocused: isFocused)
    }

    public func makeContentView() -> UIView & UIContentView {
        TSListItemContentView(configuration: self)
    }

    public func updated(for state: UIConfigurationState) -> TSListItemConfiguration {
        guard let state = state as? UICellConfigurationState else { return self }
        return updated(isHighlighted: state.isHighlighted, isFocused: state.isFocused)
    }

    func updated(isHighlighted: Bool, isFocused: Bool) -> TSListItemConfiguration {
        var configuration = self
        let isPressable = role.isPressable
        configuration.isHighlighted = isPressable && isHighlighted
        configuration.isFocused = isPressable && isFocused
        return configuration
    }
}
