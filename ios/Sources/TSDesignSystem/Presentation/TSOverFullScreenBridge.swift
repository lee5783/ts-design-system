import SwiftUI
import UIKit

struct TSOverFullScreenBridge: UIViewControllerRepresentable {
    @Binding var isPresented: Bool
    let environment: TSPresentationEnvironment
    let onDidDismiss: () -> Void
    var onRefused: () -> Void = {}
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
            configure: Self.configure
        )
    }

    static func dismantleUIViewController(_ anchor: TSPresentationAnchorViewController, coordinator: TSPresentationCoordinator) {
        coordinator.dismiss(animated: false)
    }

    static func configure(_ hosting: UIHostingController<AnyView>) {
        hosting.modalPresentationStyle = .overFullScreen
        hosting.modalTransitionStyle = .crossDissolve
        hosting.view.backgroundColor = .clear
    }
}
