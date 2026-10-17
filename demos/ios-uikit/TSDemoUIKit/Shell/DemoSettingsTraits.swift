import TSDemoSupport
import UIKit

extension DemoTheme {
    var userInterfaceStyle: UIUserInterfaceStyle {
        switch self {
        case .system:
            .unspecified
        case .light:
            .light
        case .dark:
            .dark
        }
    }
}

extension DemoTextSize {
    var contentSizeCategory: UIContentSizeCategory? {
        switch self {
        case .system:
            nil
        case .extraSmall:
            .extraSmall
        case .small:
            .small
        case .medium:
            .medium
        case .large:
            .large
        case .extraLarge:
            .extraLarge
        case .extraExtraLarge:
            .extraExtraLarge
        case .extraExtraExtraLarge:
            .extraExtraExtraLarge
        case .accessibility1:
            .accessibilityMedium
        case .accessibility2:
            .accessibilityLarge
        case .accessibility3:
            .accessibilityExtraLarge
        case .accessibility4:
            .accessibilityExtraExtraLarge
        case .accessibility5:
            .accessibilityExtraExtraExtraLarge
        }
    }
}
