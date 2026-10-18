import SwiftUI
import UIKit

struct TSSheetBridge: UIViewControllerRepresentable {
    @Binding var isPresented: Bool
    let environment: TSPresentationEnvironment
    let cornerRadius: CGFloat
    let background: UIColor
    var reduceMotion = false
    var detentPath: TSCompatPath = TSCompat.customSheetDetents
    let onDidDismiss: () -> Void
    var onRefused: () -> Void = {}
    let fitHeight: @MainActor (CGFloat) -> CGFloat
    let content: AnyView

    func makeCoordinator() -> TSPresentationCoordinator {
        TSPresentationCoordinator()
    }

    func makeUIViewController(context: Context) -> TSPresentationAnchorViewController {
        TSPresentationAnchorViewController()
    }

    func updateUIViewController(_ anchor: TSPresentationAnchorViewController, context: Context) {
        let coordinator = context.coordinator
        coordinator.onUserDismiss = { isPresented = false }
        coordinator.onDidDismiss = onDidDismiss
        coordinator.onRefused = onRefused
        coordinator.update(
            isPresented: isPresented,
            rootView: AnyView(content.tsPresentationEnvironment(environment)),
            environment: environment,
            anchor: anchor,
            animated: true,
            configure: configure
        )
        if let sheet = coordinator.hosting?.sheetPresentationController {
            TSSheetCompat.invalidateDetents(of: sheet)
        }
    }

    static func dismantleUIViewController(_ anchor: TSPresentationAnchorViewController, coordinator: TSPresentationCoordinator) {
        coordinator.dismiss(animated: false)
    }

    func configure(_ hosting: UIHostingController<AnyView>) {
        hosting.modalPresentationStyle = .pageSheet
        hosting.modalTransitionStyle = reduceMotion ? .crossDissolve : .coverVertical
        hosting.view.backgroundColor = background
        guard let sheet = hosting.sheetPresentationController else { return }
        let fitHeight = fitHeight
        TSSheetCompat.configure(sheet, cornerRadius: cornerRadius, path: detentPath) { [weak hosting] in
            let width = hosting?.sheetPresentationController?.containerView?.bounds.width ?? hosting?.view.bounds.width ?? 0
            return fitHeight(width)
        }
    }
}
