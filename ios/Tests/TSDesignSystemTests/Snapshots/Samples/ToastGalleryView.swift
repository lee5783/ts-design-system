import SwiftUI
import TSDesignSystem

enum ToastGalleryKind: String, CaseIterable {
    case tones
    case contents
}

struct ToastGalleryView: View {
    let kind: ToastGalleryKind

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
        TSToast("Project archived")
        TSToast("Changes saved", tone: .success)
        TSToast("Storage almost full", tone: .warning)
        TSToast("Upload failed", tone: .danger)
        TSToast("New version available", tone: .info)
    }

    @ViewBuilder
    private var contents: some View {
        TSToast("Project archived", actionTitle: "Undo", onAction: {})
        TSToast("Message deleted", tone: .success, actionTitle: "Undo", onAction: {})
        TSToast("A longer message that wraps onto a second line at larger text sizes", tone: .info, actionTitle: "View", onAction: {})
    }
}
