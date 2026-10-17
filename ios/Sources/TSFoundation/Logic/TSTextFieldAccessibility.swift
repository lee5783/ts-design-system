import UIKit

public enum TSTextFieldAccessibility {
    public static func value(text: String, error: String?, isSecure: Bool) -> String? {
        guard let error, !error.isEmpty else { return nil }
        let message = TSStrings.textFieldError(error)
        guard !isSecure, !text.isEmpty else { return message }
        return "\(text), \(message)"
    }

    public static func hint(_ hint: String?, error: String?) -> String? {
        guard error?.isEmpty ?? true, let hint, !hint.isEmpty else { return nil }
        return hint
    }

    public static func announcement(from oldError: String?, to newError: String?) -> String? {
        guard let newError, !newError.isEmpty, newError != oldError else { return nil }
        return TSStrings.textFieldError(newError)
    }

    public static func toggleLabel(isRevealed: Bool) -> String {
        isRevealed ? TSStrings.hidePassword : TSStrings.showPassword
    }

    @MainActor
    public static func post(_ announcement: String) {
        UIAccessibility.post(notification: .announcement, argument: announcement)
    }
}
