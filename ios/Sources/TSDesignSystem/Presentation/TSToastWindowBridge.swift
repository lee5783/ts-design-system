import Combine
import SwiftUI
#if canImport(UIKit)
import UIKit
#elseif canImport(AppKit)
import AppKit
#endif

#if canImport(UIKit)
struct TSToastWindowBridge: UIViewRepresentable {
    let queue: TSToastQueue
    let bottomInset: CGFloat
    let environment: TSPresentationEnvironment

    func makeCoordinator() -> TSToastWindowCoordinator {
        TSToastWindowCoordinator(queue: queue)
    }

    func makeUIView(context: Context) -> TSToastAnchorView {
        let anchor = TSToastAnchorView()
        let coordinator = context.coordinator
        anchor.onWindowChange = { [weak coordinator] window in
            coordinator?.attach(to: window)
        }
        return anchor
    }

    func updateUIView(_ anchor: TSToastAnchorView, context: Context) {
        context.coordinator.update(queue: queue, bottomInset: bottomInset, environment: environment)
    }

    static func dismantleUIView(_ anchor: TSToastAnchorView, coordinator: TSToastWindowCoordinator) {
        anchor.onWindowChange = nil
        coordinator.detach()
    }
}

final class TSToastAnchorView: UIView {
    var onWindowChange: ((UIWindow?) -> Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        isUserInteractionEnabled = false
        isAccessibilityElement = false
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func didMoveToWindow() {
        super.didMoveToWindow()
        onWindowChange?(window)
    }
}
#elseif canImport(AppKit)
struct TSToastWindowBridge: NSViewRepresentable {
    let queue: TSToastQueue
    let bottomInset: CGFloat
    let environment: TSPresentationEnvironment

    func makeCoordinator() -> TSToastWindowCoordinator {
        TSToastWindowCoordinator(queue: queue)
    }

    func makeNSView(context: Context) -> TSToastAnchorView {
        let anchor = TSToastAnchorView()
        let coordinator = context.coordinator
        anchor.onWindowChange = { [weak coordinator] window in
            coordinator?.attach(to: window)
        }
        return anchor
    }

    func updateNSView(_ anchor: TSToastAnchorView, context: Context) {
        context.coordinator.update(queue: queue, bottomInset: bottomInset, environment: environment)
    }

    static func dismantleNSView(_ anchor: TSToastAnchorView, coordinator: TSToastWindowCoordinator) {
        anchor.onWindowChange = nil
        coordinator.detach()
    }
}

final class TSToastAnchorView: NSView {
    var onWindowChange: ((NSWindow?) -> Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        setAccessibilityElement(false)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func hitTest(_ point: NSPoint) -> NSView? {
        nil
    }

    override func viewDidMoveToWindow() {
        super.viewDidMoveToWindow()
        onWindowChange?(window)
    }
}
#endif

@MainActor
final class TSToastWindowCoordinator {
    private(set) var queue: TSToastQueue
    #if canImport(UIKit)
    private(set) var window: TSToastOverlayWindow?
    private(set) var hosting: TSToastHostingController?
    #elseif canImport(AppKit)
    private(set) var window: TSToastOverlayPanel?
    private(set) var hosting: NSHostingView<AnyView>?
    #endif
    private var bottomInset: CGFloat = 0
    private var environment = TSPresentationEnvironment()
    private let effects: TSToastShowEffects
    private let scheduler: any TSToastScheduling
    private var hideTimer: (any TSToastCancellable)?
    private var subscription: AnyCancellable?

    init(
        queue: TSToastQueue,
        effects: TSToastShowEffects = TSToastShowEffects(),
        scheduler: any TSToastScheduling = TSToastTaskScheduler()
    ) {
        self.queue = queue
        self.effects = effects
        self.scheduler = scheduler
    }

    func update(queue: TSToastQueue, bottomInset: CGFloat, environment: TSPresentationEnvironment) {
        self.bottomInset = bottomInset
        self.environment = environment
        if queue !== self.queue {
            self.queue = queue
            subscribe()
        }
        render()
    }

    #if canImport(UIKit)
    func attach(to sourceWindow: UIWindow?) {
        guard let sourceWindow, let scene = sourceWindow.windowScene else { return }
        if let window, window.windowScene === scene {
            hosting?.sourceWindow = sourceWindow
            return
        }
        install(TSToastOverlayWindow(windowScene: scene), sourceWindow: sourceWindow)
    }

    func install(_ window: TSToastOverlayWindow, sourceWindow: UIWindow?) {
        detach()
        let hosting = TSToastHostingController(rootView: AnyView(EmptyView()))
        hosting.sourceWindow = sourceWindow
        hosting.view.backgroundColor = .clear
        window.windowLevel = TSToastWindowing.windowLevel
        window.backgroundColor = .clear
        window.rootViewController = hosting
        window.isHidden = true
        self.window = window
        self.hosting = hosting
        render()
        subscribe()
    }

    func detach() {
        subscription = nil
        hideTimer?.cancel()
        hideTimer = nil
        window?.isHidden = true
        window?.rootViewController = nil
        window = nil
        hosting = nil
    }
    #elseif canImport(AppKit)
    func attach(to sourceWindow: NSWindow?) {
        guard let sourceWindow, !(sourceWindow is TSToastOverlayPanel) else { return }
        if let window, window.sourceWindow === sourceWindow {
            return
        }
        install(TSToastOverlayPanel(), sourceWindow: sourceWindow)
    }

    func install(_ window: TSToastOverlayPanel, sourceWindow: NSWindow?) {
        detach()
        let hosting = NSHostingView(rootView: AnyView(EmptyView()))
        hosting.sizingOptions = []
        window.sourceWindow = sourceWindow
        window.contentView = hosting
        window.isHidden = true
        window.onShow = { [weak self] in
            self?.render()
        }
        self.window = window
        self.hosting = hosting
        render()
        subscribe()
    }

    func detach() {
        subscription = nil
        hideTimer?.cancel()
        hideTimer = nil
        window?.onShow = nil
        window?.isHidden = true
        window?.contentView = nil
        window = nil
        hosting = nil
    }
    #endif

    private func subscribe() {
        subscription = queue.$current.sink { [weak self] item in
            self?.currentDidChange(item)
        }
    }

    private func currentDidChange(_ item: TSToastItem?) {
        effects.didChange(to: item)
        hideTimer?.cancel()
        hideTimer = nil
        if item != nil {
            window?.isHidden = false
        } else {
            hideTimer = scheduler.schedule(after: TSToastWindowing.hideDelay) { [weak self] in
                self?.hideIfIdle()
            }
        }
    }

    private func hideIfIdle() {
        guard queue.current == nil, let window else { return }
        window.isHidden = true
        #if canImport(UIKit)
        window.toastFrame = .null
        #endif
    }

    private func render() {
        guard let hosting, let window else { return }
        #if canImport(UIKit)
        window.overrideUserInterfaceStyle = environment.userInterfaceStyle
        let onToastFrameChange: (CGRect) -> Void = { [weak window] frame in
            window?.toastFrame = frame
        }
        #elseif canImport(AppKit)
        guard window.isVisible else { return }
        window.appearance = environment.appearance
        let onToastFrameChange: (CGRect) -> Void = { _ in }
        #endif
        hosting.rootView = AnyView(
            TSToastHostView(queue: queue, bottomInset: bottomInset, onToastFrameChange: onToastFrameChange)
            .tsPresentationEnvironment(environment)
        )
    }
}
