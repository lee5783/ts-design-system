import UIKit

public enum TSToastAccessibility {
    public static func label(tone: TSToastTone, message: String) -> String {
        [TSStrings.toastTone(tone), message]
            .compactMap { $0 }
            .filter { !$0.isEmpty }
            .joined(separator: ", ")
    }

    public static func announcement(for item: TSToastItem) -> String {
        [label(tone: item.tone, message: item.message), item.action?.title]
            .compactMap { $0 }
            .filter { !$0.isEmpty }
            .joined(separator: ", ")
    }

    @MainActor
    public static func post(_ announcement: String) {
        UIAccessibility.post(notification: .announcement, argument: announcement)
    }
}

@MainActor
package final class TSToastShowEffects {
    private let announce: @MainActor (String) -> Void
    private let haptics: TSHaptics
    private var lastShownID: TSToastItem.ID?

    package init(
        announce: @escaping @MainActor (String) -> Void = { TSToastAccessibility.post($0) },
        haptics: TSHaptics = .shared
    ) {
        self.announce = announce
        self.haptics = haptics
    }

    package func didChange(to item: TSToastItem?) {
        guard let item else {
            lastShownID = nil
            return
        }
        guard item.id != lastShownID else { return }
        lastShownID = item.id
        announce(TSToastAccessibility.announcement(for: item))
        if let event = item.tone.hapticEvent {
            haptics.play(event)
        }
    }
}
