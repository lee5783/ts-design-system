import Foundation

public enum DemoPageID: String, CaseIterable, Hashable, Sendable, Identifiable {
    case colors
    case typography
    case spacing
    case radius
    case size
    case opacity
    case elevation
    case icons
    case button
    case iconButton = "icon-button"
    case badge
    case avatar
    case textField = "text-field"
    case checkbox
    case `switch`
    case tabs
    case listItem = "list-item"
    case card
    case alert
    case dialog
    case toast
    case bottomNav = "bottom-nav"

    public var id: String { rawValue }

    public var kind: DemoPageKind {
        switch self {
        case .colors, .typography, .spacing, .radius, .size, .opacity, .elevation, .icons:
            .foundation
        default:
            .component
        }
    }

    public var title: String {
        DemoStrings.pageTitle(self)
    }

    public var accessibilityIdentifier: String {
        "demo.page.\(rawValue)"
    }
}

public enum DemoPageKind: String, Hashable, Sendable {
    case foundation
    case component
}
