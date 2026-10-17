import SwiftUI
import Testing
import UIKit
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSDialog behaviour")
struct TSDialogBehaviorTests {
    private final class ActionLog {
        private(set) var labels: [String] = []
        private(set) var closes = 0

        func record(_ action: TSDialogAction) {
            labels.append(action.label)
        }

        func close() {
            closes += 1
        }
    }

    private func panel(
        variant: TSDialogVariant = .center,
        log: ActionLog = ActionLog(),
        actions: [TSDialogAction] = [TSDialogAction("Cancel"), TSDialogAction("Delete", variant: .danger)]
    ) -> TSDialogPanel<Text> {
        TSDialogPanel(
            title: "Delete this project?",
            variant: variant,
            actions: actions,
            hasBody: true,
            onAction: { log.record($0) },
            onClose: { log.close() },
            content: Text("This cannot be undone."),
            focusesTitle: false
        )
    }

    private func height(of view: some View, width: CGFloat = 358) -> CGFloat {
        UIHostingController(rootView: view).sizeThatFits(in: CGSize(width: width, height: CGFloat.greatestFiniteMagnitude)).height
    }

    @Test("A center dialog always presents over full screen, on every OS path and with reduce motion")
    func centerRoute() {
        for path in [TSCompatPath.native, .fallback] {
            for reduceMotion in [false, true] {
                #expect(TSDialogRoute.resolve(variant: .center, sheetPath: path, reduceMotion: reduceMotion) == .overFullScreen)
            }
        }
    }

    @Test("A sheet uses TSSheetBridge below iOS 16.4 and the native modifiers on 16.4+")
    func sheetRoute() {
        #expect(TSDialogRoute.resolve(variant: .sheet, sheetPath: .fallback, reduceMotion: false) == .sheetBridge)
        #expect(TSDialogRoute.resolve(variant: .sheet, sheetPath: .native, reduceMotion: false) == .nativeSheet)
        if #available(iOS 16.4, *) {
            #expect(TSDialogRoute.resolve(variant: .sheet, sheetPath: TSCompat.sheetPresentation, reduceMotion: false) == .nativeSheet)
        } else {
            #expect(TSDialogRoute.resolve(variant: .sheet, sheetPath: TSCompat.sheetPresentation, reduceMotion: false) == .sheetBridge)
        }
    }

    @Test("With reduce motion a sheet goes through the bridge, which fades instead of sliding")
    func reduceMotionRoute() {
        #expect(TSDialogRoute.resolve(variant: .sheet, sheetPath: .native, reduceMotion: true) == .sheetBridge)
        let bridge = TSSheetBridge(
            isPresented: .constant(false),
            environment: TSPresentationEnvironment(),
            cornerRadius: 16,
            background: .white,
            reduceMotion: true,
            onDidDismiss: {},
            fitHeight: { _ in 200 },
            content: AnyView(EmptyView())
        )
        let hosting = UIHostingController(rootView: AnyView(EmptyView()))
        bridge.configure(hosting)
        #expect(hosting.modalTransitionStyle == .crossDissolve)
    }

    @Test("The iOS 15 sheet bridge is a page sheet with medium and large detents, a 16 pt radius and the grabber")
    func sheetBridgeConfiguration() throws {
        let bridge = TSSheetBridge(
            isPresented: .constant(false),
            environment: TSPresentationEnvironment(),
            cornerRadius: 16,
            background: TSColor.surfaceElevated.uiColor,
            reduceMotion: false,
            detentPath: .fallback,
            onDidDismiss: {},
            fitHeight: { _ in 200 },
            content: AnyView(EmptyView())
        )
        let hosting = UIHostingController(rootView: AnyView(EmptyView()))
        bridge.configure(hosting)
        let sheet = try #require(hosting.sheetPresentationController)
        #expect(hosting.modalPresentationStyle == .pageSheet)
        #expect(hosting.modalTransitionStyle == .coverVertical)
        #expect(detentIdentifiers(sheet) == [.medium, .large])
        #expect(sheet.preferredCornerRadius == 16)
        #expect(sheet.prefersGrabberVisible)
        let traits = UITraitCollection(userInterfaceStyle: .dark)
        #expect(hosting.view.backgroundColor?.resolvedColor(with: traits) == TSColor.surfaceElevated.uiColor.resolvedColor(with: traits))
    }

    @Test("The center bridge presents over full screen with a cross-dissolve on a clear background")
    func overFullScreenConfiguration() {
        let hosting = UIHostingController(rootView: AnyView(EmptyView()))
        TSOverFullScreenBridge.configure(hosting)
        #expect(hosting.modalPresentationStyle == .overFullScreen)
        #expect(hosting.modalTransitionStyle == .crossDissolve)
        #expect(hosting.view.backgroundColor == .clear)
    }

    @Test("The bridge coordinator presents a modal hosting controller from its anchor and drops it on dismiss")
    func coordinatorPresents() {
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        let root = UIViewController()
        let anchor = TSPresentationAnchorViewController()
        root.addChild(anchor)
        root.view.addSubview(anchor.view)
        anchor.didMove(toParent: root)
        window.rootViewController = root
        window.isHidden = false
        let coordinator = TSPresentationCoordinator()
        let environment = TSPresentationEnvironment(colorScheme: .dark)
        coordinator.update(isPresented: true, rootView: AnyView(Text("Panel")), environment: environment, anchor: anchor, animated: false, configure: TSOverFullScreenBridge.configure)
        let hosting = coordinator.hosting
        #expect(hosting?.modalPresentationStyle == .overFullScreen)
        #expect(hosting?.overrideUserInterfaceStyle == .dark)
        #expect(hosting?.view.accessibilityViewIsModal == true)
        #expect(hosting?.presentingViewController === root)
        #expect(hosting?.presentationController?.delegate === coordinator)
        coordinator.update(isPresented: true, rootView: AnyView(Text("Panel")), environment: environment, anchor: anchor, animated: false, configure: TSOverFullScreenBridge.configure)
        #expect(coordinator.hosting === hosting)
        var dismissals = 0
        coordinator.onDidDismiss = { dismissals += 1 }
        coordinator.update(isPresented: false, rootView: AnyView(Text("Panel")), environment: environment, anchor: anchor, animated: false, configure: TSOverFullScreenBridge.configure)
        #expect(coordinator.hosting == nil || coordinator.isDismissing)
        if coordinator.isDismissing {
            coordinator.didFinishDismissal()
        }
        #expect(coordinator.hosting == nil)
        #expect(dismissals == 1)
    }

    @Test("Without a window the anchor defers the presentation; cancelling it reports one dismissal so the presenter is released")
    func coordinatorWaitsForWindow() {
        let coordinator = TSPresentationCoordinator()
        let anchor = TSPresentationAnchorViewController()
        coordinator.update(isPresented: true, rootView: AnyView(Text("Panel")), environment: TSPresentationEnvironment(), anchor: anchor, animated: false, configure: TSOverFullScreenBridge.configure)
        #expect(coordinator.hosting == nil)
        var dismissals = 0
        coordinator.onDidDismiss = { dismissals += 1 }
        coordinator.update(isPresented: false, rootView: AnyView(Text("Panel")), environment: TSPresentationEnvironment(), anchor: anchor, animated: false, configure: TSOverFullScreenBridge.configure)
        #expect(dismissals == 1)
        coordinator.update(isPresented: false, rootView: AnyView(Text("Panel")), environment: TSPresentationEnvironment(), anchor: anchor, animated: false, configure: TSOverFullScreenBridge.configure)
        #expect(dismissals == 1)
    }

    @Test("A dialog requested while the anchor's chain is already presenting is refused: nothing is held, the binding resets and the presenter is free")
    func coordinatorRefusesWhileAnchorPresents() {
        let presenter = TSDialogPresenter()
        let session = TSDialogSession()
        let (window, root, anchor) = anchoredWindow()
        root.present(UIViewController(), animated: false)
        var isPresented = true
        var refusals = 0
        var dismissals = 0
        let coordinator = TSPresentationCoordinator()
        wire(coordinator, session: session, presenter: presenter, isPresented: { isPresented = $0 }, refusals: { refusals += 1 }, dismissals: { dismissals += 1 })
        #expect(session.request(true, presenter: presenter))
        coordinator.update(isPresented: session.isGranted, rootView: AnyView(Text("Panel")), environment: TSPresentationEnvironment(), anchor: anchor, animated: false, configure: TSOverFullScreenBridge.configure)
        #expect(coordinator.hosting == nil)
        #expect(refusals == 1)
        #expect(!isPresented)
        #expect(!session.isGranted)
        #expect(!presenter.isPresenting)
        coordinator.update(isPresented: false, rootView: AnyView(Text("Panel")), environment: TSPresentationEnvironment(), anchor: anchor, animated: false, configure: TSOverFullScreenBridge.configure)
        #expect(dismissals == 0)
        let later = TSDialogSession()
        #expect(later.request(true, presenter: presenter))
        _ = window
    }

    @Test("Re-requesting a dialog during its dismissal is refused; once the dismissal finishes the presenter is free")
    func coordinatorRefusesDuringDismissal() throws {
        let presenter = TSDialogPresenter()
        let session = TSDialogSession()
        let (window, _, anchor) = anchoredWindow()
        var isPresented = true
        var refusals = 0
        var dismissals = 0
        let coordinator = TSPresentationCoordinator()
        wire(coordinator, session: session, presenter: presenter, isPresented: { isPresented = $0 }, refusals: { refusals += 1 }, dismissals: { dismissals += 1 })
        #expect(session.request(true, presenter: presenter))
        coordinator.update(isPresented: true, rootView: AnyView(Text("Panel")), environment: TSPresentationEnvironment(), anchor: anchor, animated: false, configure: TSOverFullScreenBridge.configure)
        let hosting = try #require(coordinator.hosting)
        #expect(hosting.presentingViewController != nil)
        session.withdraw()
        coordinator.update(isPresented: false, rootView: AnyView(Text("Panel")), environment: TSPresentationEnvironment(), anchor: anchor, animated: true, configure: TSOverFullScreenBridge.configure)
        try #require(coordinator.isDismissing)
        #expect(coordinator.hosting === hosting)
        #expect(session.request(true, presenter: presenter))
        coordinator.update(isPresented: session.isGranted, rootView: AnyView(Text("Panel")), environment: TSPresentationEnvironment(), anchor: anchor, animated: true, configure: TSOverFullScreenBridge.configure)
        #expect(refusals == 1)
        #expect(!isPresented)
        #expect(!presenter.isPresenting)
        coordinator.didFinishDismissal()
        #expect(coordinator.hosting == nil)
        #expect(dismissals == 1)
        #expect(!presenter.isPresenting)
        _ = window
    }

    @Test("A dismissing action's handler runs only after the dismissal, when the presenter is free for a follow-up dialog")
    func handlerRunsAfterDismissal() {
        let presenter = TSDialogPresenter()
        let first = TSDialogSession()
        let followUp = TSDialogSession()
        var followUpGranted: Bool?
        #expect(first.request(true, presenter: presenter))
        first.withdraw(then: { followUpGranted = followUp.request(true, presenter: presenter) })
        #expect(followUpGranted == nil)
        first.didDismiss(presenter: presenter)
        #expect(followUpGranted == true)
        #expect(presenter.refusedCount == 0)
        first.didDismiss(presenter: presenter)
        #expect(presenter.refusedCount == 0)
    }

    @Test("A session refused because another dialog is open reports the refusal and stays ungranted")
    func refusedSessionStaysClosed() {
        let presenter = TSDialogPresenter()
        let other = TSDialogSession()
        let session = TSDialogSession()
        #expect(other.request(true, presenter: presenter))
        #expect(!session.request(true, presenter: presenter))
        #expect(!session.isGranted)
        #expect(presenter.refusedCount == 1)
    }

    @Test("A drag-to-dismiss tells the owner and releases the presenter")
    func userDismissReported() {
        let coordinator = TSPresentationCoordinator()
        var userDismissals = 0
        var dismissals = 0
        coordinator.onUserDismiss = { userDismissals += 1 }
        coordinator.onDidDismiss = { dismissals += 1 }
        let controller = UIViewController()
        controller.modalPresentationStyle = .pageSheet
        coordinator.presentationControllerDidDismiss(controller.presentationController!)
        #expect(userDismissals == 1)
        #expect(dismissals == 1)
    }

    @Test("A session is refused while another dialog is open, and is granted once it has dismissed")
    func sessionRefusal() {
        let presenter = TSDialogPresenter()
        let first = TSDialogSession()
        let second = TSDialogSession()
        #expect(first.request(true, presenter: presenter))
        #expect(first.isGranted)
        #expect(!second.request(true, presenter: presenter))
        #expect(!second.isGranted)
        #expect(presenter.refusedCount == 1)
        first.withdraw()
        #expect(!second.request(true, presenter: presenter))
        first.didDismiss(presenter: presenter)
        #expect(!presenter.isPresenting)
        #expect(second.request(true, presenter: presenter))
        second.invalidate(presenter: presenter)
        #expect(!presenter.isPresenting)
    }

    @Test("The title is a header and the message follows it")
    func title() throws {
        let sut = panel()
        let title = try sut.inspect().find(text: "Delete this project?")
        #expect(try title.accessibilityTraits().contains(.isHeader))
        #expect(try sut.inspect().find(text: "This cannot be undone.").string() == "This cannot be undone.")
    }

    @Test("Center actions keep primary last; sheet actions put it first")
    func actionOrder() throws {
        let center = try panel(variant: .center).inspect().find(TSDialogActions.self).actualView()
        #expect(center.actions.map(\.label) == ["Cancel", "Delete"])
        #expect(!center.resolved.blockActions)
        let sheet = try panel(variant: .sheet).inspect().find(TSDialogActions.self).actualView()
        #expect(sheet.actions.map(\.label) == ["Delete", "Cancel"])
        #expect(sheet.resolved.blockActions)
    }

    @Test("Tapping an action reports that action")
    func actionFires() throws {
        let log = ActionLog()
        let sut = panel(log: log)
        try sut.inspect().find(button: "Delete").tap()
        try sut.inspect().find(button: "Cancel").tap()
        #expect(log.labels == ["Delete", "Cancel"])
    }

    @Test("Escape and the Close action both close the dialog")
    func escapeCloses() throws {
        let log = ActionLog()
        let sut = panel(log: log)
        let escape = try sut.inspect().vStack().modifier(TSDialogEscape.self).actualView()
        escape.onClose()
        #expect(log.closes == 1)
        #expect(TSStrings.close == "Close")
    }

    @Test("A hidden cancel-action button maps the hardware Escape key to close")
    func escapeKeyCloses() throws {
        let log = ActionLog()
        let sut = panel(log: log)
        let key = try sut.inspect().find(TSDialogEscapeKey.self)
        let button = try key.button()
        #expect(try button.accessibilityHidden())
        try button.tap()
        #expect(log.closes == 1)
    }

    @Test("Without actions there is no action row")
    func noActions() {
        let sut = panel(actions: [])
        #expect(throws: (any Error).self) {
            try sut.inspect().find(TSDialogActions.self)
        }
    }

    @Test("Center: 24 padding, 28 title, 8 gap, 24 body, 24 gap, 44 buttons, 24 padding")
    func centerHeight() {
        #expect(abs(height(of: panel(variant: .center)) - (24 + 28 + 8 + 24 + 24 + 44 + 24)) < 1)
    }

    @Test("The center panel never grows past 440 pt")
    func maxWidth() {
        let size = UIHostingController(rootView: panel(variant: .center)).sizeThatFits(in: CGSize(width: 800, height: 1000))
        #expect(size.width <= 440)
    }

    @Test("The sheet measures its content, so the fit detent hugs it")
    func sheetHeight() {
        let height = height(of: panel(variant: .sheet), width: 390)
        #expect(abs(height - (32 + 28 + 8 + 24 + 24 + 44 + 8 + 44 + 24)) < 1)
    }

    @Test("Presenting forwards the host's colour scheme, text size, direction and locale")
    func environmentForwarding() {
        let environment = TSPresentationEnvironment(colorScheme: .dark, dynamicTypeSize: .accessibility5, layoutDirection: .rightToLeft, locale: Locale(identifier: "ar"))
        #expect(environment.userInterfaceStyle == .dark)
        #expect(TSPresentationEnvironment().userInterfaceStyle == .light)
    }
}

@MainActor
private func anchoredWindow() -> (UIWindow, UIViewController, TSPresentationAnchorViewController) {
    let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
    let root = UIViewController()
    let anchor = TSPresentationAnchorViewController()
    root.addChild(anchor)
    root.view.addSubview(anchor.view)
    anchor.didMove(toParent: root)
    window.rootViewController = root
    window.isHidden = false
    return (window, root, anchor)
}

@MainActor
private func wire(
    _ coordinator: TSPresentationCoordinator,
    session: TSDialogSession,
    presenter: TSDialogPresenter,
    isPresented: @escaping (Bool) -> Void,
    refusals: @escaping () -> Void,
    dismissals: @escaping () -> Void
) {
    coordinator.onRefused = {
        session.invalidate(presenter: presenter)
        isPresented(false)
        refusals()
    }
    coordinator.onDidDismiss = {
        session.didDismiss(presenter: presenter)
        dismissals()
    }
}

@MainActor
private func detentIdentifiers(_ sheet: UISheetPresentationController) -> [UISheetPresentationController.Detent.Identifier] {
    if #available(iOS 16, *) {
        return sheet.detents.map { $0.identifier }
    }
    return sheet.detents.isEmpty ? [] : [.medium, .large]
}
