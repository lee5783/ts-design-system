import SwiftUI
import UIKit

struct TSPresentationEnvironment: Equatable {
    var colorScheme: ColorScheme = .light
    var dynamicTypeSize: DynamicTypeSize = .large
    var layoutDirection: LayoutDirection = .leftToRight
    var locale: Locale = .current

    var userInterfaceStyle: UIUserInterfaceStyle {
        colorScheme == .dark ? .dark : .light
    }
}

extension View {
    func tsPresentationEnvironment(_ environment: TSPresentationEnvironment) -> some View {
        self
            .environment(\.colorScheme, environment.colorScheme)
            .environment(\.dynamicTypeSize, environment.dynamicTypeSize)
            .environment(\.layoutDirection, environment.layoutDirection)
            .environment(\.locale, environment.locale)
    }
}

@MainActor
final class TSPresentationCoordinator: NSObject, UIAdaptivePresentationControllerDelegate {
    var onUserDismiss: () -> Void = {}
    var onDidDismiss: () -> Void = {}
    var onRefused: () -> Void = {}
    private(set) var hosting: UIHostingController<AnyView>?
    private(set) var isDismissing = false
    private var pendingPresentation = false
    private var owesDismissal = false

    func update(
        isPresented: Bool,
        rootView: AnyView,
        environment: TSPresentationEnvironment,
        anchor: UIViewController,
        animated: Bool,
        configure: @escaping @MainActor (UIHostingController<AnyView>) -> Void
    ) {
        if let hosting {
            hosting.rootView = rootView
            hosting.overrideUserInterfaceStyle = environment.userInterfaceStyle
        }
        if isPresented, isDismissing {
            refuse()
        } else if isPresented, hosting == nil {
            owesDismissal = true
            present(rootView: rootView, environment: environment, anchor: anchor, animated: animated, configure: configure)
        } else if !isPresented {
            pendingPresentation = false
            dismiss(animated: animated)
        }
    }

    func dismiss(animated: Bool) {
        guard !isDismissing else { return }
        guard let hosting, hosting.presentingViewController != nil else {
            if owesDismissal {
                didFinishDismissal()
            }
            return
        }
        isDismissing = true
        hosting.dismiss(animated: animated) { [weak self] in
            self?.didFinishDismissal()
        }
    }

    func didFinishDismissal() {
        hosting = nil
        isDismissing = false
        owesDismissal = false
        onDidDismiss()
    }

    func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
        onUserDismiss()
        didFinishDismissal()
    }

    private func refuse() {
        owesDismissal = false
        onRefused()
    }

    private func present(
        rootView: AnyView,
        environment: TSPresentationEnvironment,
        anchor: UIViewController,
        animated: Bool,
        configure: @escaping @MainActor (UIHostingController<AnyView>) -> Void
    ) {
        guard anchor.view.window != nil else {
            guard !pendingPresentation else { return }
            pendingPresentation = true
            DispatchQueue.main.async { [weak self, weak anchor] in
                guard let self, let anchor, self.pendingPresentation else { return }
                self.pendingPresentation = false
                self.present(rootView: rootView, environment: environment, anchor: anchor, animated: animated, configure: configure)
            }
            return
        }
        guard anchor.presentedViewController == nil else {
            refuse()
            return
        }
        let hosting = UIHostingController(rootView: rootView)
        hosting.overrideUserInterfaceStyle = environment.userInterfaceStyle
        hosting.view.accessibilityViewIsModal = true
        configure(hosting)
        hosting.presentationController?.delegate = self
        anchor.present(hosting, animated: animated)
        guard hosting.presentingViewController != nil else {
            refuse()
            return
        }
        self.hosting = hosting
    }
}

final class TSPresentationAnchorViewController: UIViewController {
    override func loadView() {
        let view = UIView()
        view.backgroundColor = .clear
        view.isUserInteractionEnabled = false
        view.isAccessibilityElement = false
        self.view = view
    }
}
