import SwiftUI
import UIKit

public enum TSTestLayoutDirection: String, CaseIterable, Sendable {
    case ltr
    case rtl

    public var traitLayoutDirection: UITraitEnvironmentLayoutDirection {
        switch self {
        case .ltr: .leftToRight
        case .rtl: .rightToLeft
        }
    }

    public var layoutDirection: LayoutDirection {
        switch self {
        case .ltr: .leftToRight
        case .rtl: .rightToLeft
        }
    }

    public var semanticContentAttribute: UISemanticContentAttribute {
        switch self {
        case .ltr: .forceLeftToRight
        case .rtl: .forceRightToLeft
        }
    }

    public var traitCollection: UITraitCollection {
        UITraitCollection(layoutDirection: traitLayoutDirection)
    }
}
