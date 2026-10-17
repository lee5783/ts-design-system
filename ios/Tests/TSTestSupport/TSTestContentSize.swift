import UIKit

public enum TSTestContentSize: String, CaseIterable, Sendable {
    case large
    case accessibility5

    public var sizeCategory: UIContentSizeCategory {
        switch self {
        case .large: .large
        case .accessibility5: .accessibilityExtraExtraExtraLarge
        }
    }

    public var traitCollection: UITraitCollection {
        UITraitCollection(preferredContentSizeCategory: sizeCategory)
    }
}
