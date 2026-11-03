#if canImport(UIKit)
import UIKit
#elseif canImport(AppKit)
import AppKit
#endif

public enum TSReturnKey: String, CaseIterable, Hashable, Sendable {
    case `default`
    case done
    case go
    case next
    case search
    case send
    case `continue`

    #if canImport(UIKit)
    public var returnKeyType: UIReturnKeyType {
        switch self {
        case .default: .default
        case .done: .done
        case .go: .go
        case .next: .next
        case .search: .search
        case .send: .send
        case .continue: .continue
        }
    }
    #endif
}

#if canImport(UIKit)
public struct TSTextInputTraits: Hashable, Sendable {
    public var keyboardType: UIKeyboardType
    public var textContentType: UITextContentType?
    public var autocapitalization: UITextAutocapitalizationType
    public var isAutocorrectionEnabled: Bool
    public var returnKey: TSReturnKey

    public init(
        keyboardType: UIKeyboardType = .default,
        textContentType: UITextContentType? = nil,
        autocapitalization: UITextAutocapitalizationType = .sentences,
        isAutocorrectionEnabled: Bool = true,
        returnKey: TSReturnKey = .default
    ) {
        self.keyboardType = keyboardType
        self.textContentType = textContentType
        self.autocapitalization = autocapitalization
        self.isAutocorrectionEnabled = isAutocorrectionEnabled
        self.returnKey = returnKey
    }

    public static let `default` = TSTextInputTraits()
    public static let name = TSTextInputTraits(textContentType: .name, autocapitalization: .words, isAutocorrectionEnabled: false)
    public static let email = TSTextInputTraits(keyboardType: .emailAddress, textContentType: .emailAddress, autocapitalization: .none, isAutocorrectionEnabled: false)
    public static let username = TSTextInputTraits(textContentType: .username, autocapitalization: .none, isAutocorrectionEnabled: false)
    public static let password = TSTextInputTraits(textContentType: .password, autocapitalization: .none, isAutocorrectionEnabled: false)
    public static let newPassword = TSTextInputTraits(textContentType: .newPassword, autocapitalization: .none, isAutocorrectionEnabled: false)
    public static let oneTimeCode = TSTextInputTraits(keyboardType: .numberPad, textContentType: .oneTimeCode, autocapitalization: .none, isAutocorrectionEnabled: false)
    public static let phone = TSTextInputTraits(keyboardType: .phonePad, textContentType: .telephoneNumber, autocapitalization: .none, isAutocorrectionEnabled: false)
    public static let number = TSTextInputTraits(keyboardType: .numberPad, autocapitalization: .none, isAutocorrectionEnabled: false)
    public static let decimal = TSTextInputTraits(keyboardType: .decimalPad, autocapitalization: .none, isAutocorrectionEnabled: false)
    public static let url = TSTextInputTraits(keyboardType: .URL, textContentType: .URL, autocapitalization: .none, isAutocorrectionEnabled: false)
    public static let code = TSTextInputTraits(keyboardType: .asciiCapable, autocapitalization: .allCharacters, isAutocorrectionEnabled: false)
    public static let search = TSTextInputTraits(keyboardType: .webSearch, autocapitalization: .none, isAutocorrectionEnabled: false, returnKey: .search)

    @MainActor
    public func apply(to textField: UITextField) {
        textField.keyboardType = keyboardType
        textField.textContentType = textContentType
        textField.autocapitalizationType = autocapitalization
        textField.autocorrectionType = isAutocorrectionEnabled ? .default : .no
        textField.returnKeyType = returnKey.returnKeyType
    }

    @MainActor
    public func apply(to textView: UITextView) {
        textView.keyboardType = keyboardType
        textView.textContentType = textContentType
        textView.autocapitalizationType = autocapitalization
        textView.autocorrectionType = isAutocorrectionEnabled ? .default : .no
        textView.returnKeyType = returnKey.returnKeyType
    }
}
#elseif canImport(AppKit)
public struct TSTextInputTraits: Hashable, Sendable {
    public var textContentType: NSTextContentType?
    public var isAutocorrectionEnabled: Bool
    public var returnKey: TSReturnKey

    public init(
        textContentType: NSTextContentType? = nil,
        isAutocorrectionEnabled: Bool = true,
        returnKey: TSReturnKey = .default
    ) {
        self.textContentType = textContentType
        self.isAutocorrectionEnabled = isAutocorrectionEnabled
        self.returnKey = returnKey
    }

    public static let `default` = TSTextInputTraits()
    public static let name = TSTextInputTraits(isAutocorrectionEnabled: false)
    public static let email = TSTextInputTraits(isAutocorrectionEnabled: false)
    public static let username = TSTextInputTraits(textContentType: .username, isAutocorrectionEnabled: false)
    public static let password = TSTextInputTraits(textContentType: .password, isAutocorrectionEnabled: false)
    public static let newPassword = TSTextInputTraits(textContentType: .password, isAutocorrectionEnabled: false)
    public static let oneTimeCode = TSTextInputTraits(textContentType: .oneTimeCode, isAutocorrectionEnabled: false)
    public static let phone = TSTextInputTraits(isAutocorrectionEnabled: false)
    public static let number = TSTextInputTraits(isAutocorrectionEnabled: false)
    public static let decimal = TSTextInputTraits(isAutocorrectionEnabled: false)
    public static let url = TSTextInputTraits(isAutocorrectionEnabled: false)
    public static let code = TSTextInputTraits(isAutocorrectionEnabled: false)
    public static let search = TSTextInputTraits(isAutocorrectionEnabled: false, returnKey: .search)

    @MainActor
    public func apply(to textField: NSTextField) {
        textField.contentType = textContentType
    }

    @MainActor
    public func apply(to textView: NSTextView) {
        guard !isAutocorrectionEnabled else { return }
        textView.isAutomaticSpellingCorrectionEnabled = false
        textView.isAutomaticTextReplacementEnabled = false
    }
}
#endif
