import UIKit

public enum TSTestAppearance: String, CaseIterable, Sendable {
    case light
    case dark

    public var userInterfaceStyle: UIUserInterfaceStyle {
        switch self {
        case .light: .light
        case .dark: .dark
        }
    }

    public var traitCollection: UITraitCollection {
        UITraitCollection(userInterfaceStyle: userInterfaceStyle)
    }
}
