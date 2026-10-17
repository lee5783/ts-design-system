import SwiftUI
import TSDesignSystem

enum AlertGalleryKind: String, CaseIterable {
    case tones
    case contents
}

struct AlertGalleryView: View {
    let kind: AlertGalleryKind

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            switch kind {
            case .tones:
                tones
            case .contents:
                contents
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private var tones: some View {
        TSAlert(tone: .info, title: "New sign-in", message: "We noticed a sign-in from a new device.")
        TSAlert(tone: .success, title: "Payment received", message: "Your invoice has been paid.")
        TSAlert(tone: .warning, title: "Storage almost full", message: "Free up space to keep syncing.")
        TSAlert(tone: .danger, title: "Payment failed", message: "Update your card to continue.")
    }

    @ViewBuilder
    private var contents: some View {
        TSAlert(tone: .info, message: "Message only, without a title.")
        TSAlert(tone: .warning, title: "Dismissible", message: "The close button sits at the trailing edge.", onDismiss: {})
        TSAlert(tone: .danger, title: "With an action", message: "Retry the upload.") {
            TSButton("Retry", variant: .link) {}
        }
        TSAlert(tone: .success, title: "Custom icon", message: "A tone can override its icon.", icon: .star, onDismiss: {})
        TSAlert(
            tone: .info,
            title: "A long title that wraps onto a second line at larger text sizes",
            message: "A long message that explains what happened and what to do next, wrapping across several lines.",
            onDismiss: {}
        )
    }
}
