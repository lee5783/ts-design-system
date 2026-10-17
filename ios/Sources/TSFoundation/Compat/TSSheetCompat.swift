import UIKit

package enum TSSheetDetent: String, Hashable, Sendable {
    case fit
    case medium
    case large
}

package enum TSSheetCompat {
    package static let fitDetentIdentifier = "ts.dialog.fit"

    package static func detents(path: TSCompatPath = TSCompat.customSheetDetents) -> [TSSheetDetent] {
        path == .native ? [.fit, .large] : [.medium, .large]
    }

    @MainActor
    package static func configure(
        _ sheet: UISheetPresentationController,
        cornerRadius: CGFloat,
        path: TSCompatPath = TSCompat.customSheetDetents,
        fitHeight: @escaping @MainActor () -> CGFloat
    ) {
        let detents = detents(path: path)
        sheet.detents = detents.map { uiDetent($0, fitHeight: fitHeight) }
        sheet.selectedDetentIdentifier = detents.first.map(identifier(for:))
        sheet.preferredCornerRadius = cornerRadius
        sheet.prefersGrabberVisible = true
        sheet.prefersScrollingExpandsWhenScrolledToEdge = false
    }

    @MainActor
    package static func invalidateDetents(of sheet: UISheetPresentationController) {
        if #available(iOS 16, *) {
            sheet.animateChanges {
                sheet.invalidateDetents()
            }
        }
    }

    package static func identifier(for detent: TSSheetDetent) -> UISheetPresentationController.Detent.Identifier {
        switch detent {
        case .fit:
            if #available(iOS 16, *) { UISheetPresentationController.Detent.Identifier(fitDetentIdentifier) } else { .medium }
        case .medium:
            .medium
        case .large:
            .large
        }
    }

    @MainActor
    static func uiDetent(_ detent: TSSheetDetent, fitHeight: @escaping @MainActor () -> CGFloat) -> UISheetPresentationController.Detent {
        switch detent {
        case .medium:
            return .medium()
        case .large:
            return .large()
        case .fit:
            if #available(iOS 16, *) {
                return .custom(identifier: identifier(for: .fit)) { context in
                    let height = MainActor.assumeIsolated { fitHeight() }
                    return min(height, context.maximumDetentValue)
                }
            }
            return .medium()
        }
    }
}
