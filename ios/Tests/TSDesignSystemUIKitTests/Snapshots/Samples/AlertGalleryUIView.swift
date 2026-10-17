import TSDesignSystemUIKit
import UIKit

enum AlertGalleryUIKind: String, CaseIterable {
    case tones
    case contents
}

final class AlertGalleryUIView: GalleryStackView {
    init(kind: AlertGalleryUIKind) {
        super.init(frame: .zero)
        stack.alignment = .fill
        let alerts = switch kind {
        case .tones: Self.tones()
        case .contents: Self.contents()
        }
        alerts.forEach(stack.addArrangedSubview)
    }

    private static func tones() -> [UIView] {
        [
            TSAlertView(tone: .info, title: "New sign-in", message: "We noticed a sign-in from a new device."),
            TSAlertView(tone: .success, title: "Payment received", message: "Your invoice has been paid."),
            TSAlertView(tone: .warning, title: "Storage almost full", message: "Free up space to keep syncing."),
            TSAlertView(tone: .danger, title: "Payment failed", message: "Update your card to continue.")
        ]
    }

    private static func contents() -> [UIView] {
        let action = TSAlertView(tone: .danger, title: "With an action", message: "Retry the upload.")
        action.actionView = TSButtonView(title: "Retry", variant: .link)
        return [
            TSAlertView(tone: .info, message: "Message only, without a title."),
            TSAlertView(tone: .warning, title: "Dismissible", message: "The close button sits at the trailing edge.", onDismiss: {}),
            action,
            TSAlertView(tone: .success, title: "Custom icon", message: "A tone can override its icon.", icon: .star, onDismiss: {}),
            TSAlertView(
                tone: .info,
                title: "A long title that wraps onto a second line at larger text sizes",
                message: "A long message that explains what happened and what to do next, wrapping across several lines.",
                onDismiss: {}
            )
        ]
    }
}
