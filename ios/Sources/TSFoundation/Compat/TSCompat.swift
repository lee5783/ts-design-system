import Foundation

package enum TSCompatPath: String, Hashable, Sendable {
    case native
    case fallback
}

package enum TSCompat {
    package static var onChange: TSCompatPath {
        if #available(iOS 17, *) { .native } else { .fallback }
    }

    package static var focusEffect: TSCompatPath {
        if #available(iOS 17, *) { .native } else { .fallback }
    }

    package static var multilineTextField: TSCompatPath {
        if #available(iOS 16, *) { .native } else { .fallback }
    }

    package static var sheetPresentation: TSCompatPath {
        if #available(iOS 16.4, *) { .native } else { .fallback }
    }

    package static var customSheetDetents: TSCompatPath {
        if #available(iOS 16, *) { .native } else { .fallback }
    }

    package static var traitOverrides: TSCompatPath {
        if #available(iOS 17, *) { .native } else { .fallback }
    }

    package static var tabBarHidden: TSCompatPath {
        if #available(iOS 18, *) { .native } else { .fallback }
    }
}
