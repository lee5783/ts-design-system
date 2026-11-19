import Foundation

package enum TSCompatPath: String, Hashable, Sendable {
    case native
    case fallback
}

package enum TSCompat {
    package static var onChange: TSCompatPath {
        if #available(iOS 17, macOS 14, *) { .native } else { .fallback }
    }

    package static var focusEffect: TSCompatPath {
        if #available(iOS 17, macOS 14, *) { .native } else { .fallback }
    }

    package static var multilineTextField: TSCompatPath {
        if #available(iOS 16, macOS 13, *) { .native } else { .fallback }
    }

    package static var sheetPresentation: TSCompatPath {
        if #available(iOS 16.4, macOS 13.3, *) { .native } else { .fallback }
    }

    package static var customSheetDetents: TSCompatPath {
        if #available(iOS 16, macOS 13, *) { .native } else { .fallback }
    }

    package static var traitOverrides: TSCompatPath {
        if #available(iOS 17, macOS 14, *) { .native } else { .fallback }
    }

    package static var tabBarHidden: TSCompatPath {
        if #available(iOS 18, macOS 15, *) { .native } else { .fallback }
    }
}
