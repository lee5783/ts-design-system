import UIKit

public enum TSAlertAccessibility {
    public static func label(tone: TSAlertTone, title: String?, message: String?) -> String {
        [TSStrings.alertTone(tone), title, message]
            .compactMap { $0 }
            .filter { !$0.isEmpty }
            .joined(separator: ", ")
    }

    @MainActor
    public static func post(_ announcement: String) {
        UIAccessibility.post(notification: .announcement, argument: announcement)
    }
}
