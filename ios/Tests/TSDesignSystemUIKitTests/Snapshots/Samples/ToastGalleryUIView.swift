import TSDesignSystemUIKit
import UIKit

enum ToastGalleryUIKind: String, CaseIterable {
    case tones
    case contents
}

final class ToastGalleryUIView: GalleryStackView {
    init(kind: ToastGalleryUIKind) {
        super.init(frame: .zero)
        stack.alignment = .fill
        let toasts = switch kind {
        case .tones: Self.tones()
        case .contents: Self.contents()
        }
        toasts.forEach(stack.addArrangedSubview)
    }

    private static func tones() -> [UIView] {
        [
            TSToastView("Project archived"),
            TSToastView("Changes saved", tone: .success),
            TSToastView("Storage almost full", tone: .warning),
            TSToastView("Upload failed", tone: .danger),
            TSToastView("New version available", tone: .info)
        ]
    }

    private static func contents() -> [UIView] {
        [
            TSToastView("Project archived", actionTitle: "Undo", onAction: {}),
            TSToastView("Message deleted", tone: .success, actionTitle: "Undo", onAction: {}),
            TSToastView("A longer message that wraps onto a second line at larger text sizes", tone: .info, actionTitle: "View", onAction: {})
        ]
    }
}
