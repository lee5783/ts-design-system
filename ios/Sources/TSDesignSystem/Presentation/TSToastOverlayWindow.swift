import SwiftUI
#if canImport(UIKit)
import UIKit
#elseif canImport(AppKit)
import AppKit
#endif

#if canImport(UIKit)
final class TSToastOverlayWindow: UIWindow {
    var toastFrame: CGRect = .null

    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        guard !toastFrame.isNull, toastFrame.contains(point) else { return nil }
        return super.hitTest(point, with: event)
    }
}

final class TSToastHostingController: UIHostingController<AnyView> {
    weak var sourceWindow: UIWindow?
    private var keyboardFrame: CGRect = .null

    override var preferredStatusBarStyle: UIStatusBarStyle {
        TSToastWindowing.statusBarController(of: sourceWindow)?.preferredStatusBarStyle ?? super.preferredStatusBarStyle
    }

    override var prefersStatusBarHidden: Bool {
        TSToastWindowing.statusBarController(of: sourceWindow)?.prefersStatusBarHidden ?? super.prefersStatusBarHidden
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillChangeFrame(_:)), name: UIResponder.keyboardWillChangeFrameNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillChangeFrame(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
    }

    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        applyKeyboardInset(duration: nil)
    }

    func avoidKeyboard(endFrame: CGRect, duration: TimeInterval) {
        keyboardFrame = endFrame
        applyKeyboardInset(duration: duration)
    }

    private func applyKeyboardInset(duration: TimeInterval?) {
        guard let window = view.window else { return }
        let frame = keyboardFrame.isNull ? keyboardFrame : window.convert(keyboardFrame, from: window.screen.coordinateSpace)
        let inset = TSToastWindowing.keyboardInset(keyboardFrame: frame, windowBounds: window.bounds, safeAreaBottom: window.safeAreaInsets.bottom)
        guard additionalSafeAreaInsets.bottom != inset else { return }
        guard let duration else {
            additionalSafeAreaInsets.bottom = inset
            return
        }
        UIView.animate(withDuration: duration) {
            self.additionalSafeAreaInsets.bottom = inset
            self.view.layoutIfNeeded()
        }
    }

    @objc private func keyboardWillChangeFrame(_ notification: Notification) {
        let isHiding = notification.name == UIResponder.keyboardWillHideNotification
        let endFrame = isHiding ? CGRect.null : (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect) ?? .null
        let duration = (notification.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? TimeInterval) ?? 0
        avoidKeyboard(endFrame: endFrame, duration: duration)
    }
}
#elseif canImport(AppKit)
final class TSToastOverlayPanel: NSPanel {
    var onShow: (() -> Void)?
    private var wantsVisible = false
    weak var sourceWindow: NSWindow? {
        didSet {
            NotificationCenter.default.removeObserver(self, name: NSWindow.didResizeNotification, object: oldValue)
            NotificationCenter.default.removeObserver(self, name: NSWindow.didChangeOcclusionStateNotification, object: oldValue)
            if let sourceWindow {
                NotificationCenter.default.addObserver(self, selector: #selector(sourceWindowDidResize(_:)), name: NSWindow.didResizeNotification, object: sourceWindow)
                NotificationCenter.default.addObserver(self, selector: #selector(sourceWindowDidChangeOcclusionState(_:)), name: NSWindow.didChangeOcclusionStateNotification, object: sourceWindow)
            }
        }
    }

    init() {
        super.init(contentRect: .zero, styleMask: [.borderless, .nonactivatingPanel], backing: .buffered, defer: true)
        isOpaque = false
        backgroundColor = .clear
        hasShadow = false
        isReleasedWhenClosed = false
        hidesOnDeactivate = false
        animationBehavior = .none
        collectionBehavior = [.fullScreenAuxiliary, .transient, .ignoresCycle]
        setAccessibilityElement(false)
    }

    override var canBecomeKey: Bool {
        false
    }

    override var canBecomeMain: Bool {
        false
    }

    var isHidden: Bool {
        get { !isVisible }
        set {
            wantsVisible = !newValue
            newValue ? hide() : show()
        }
    }

    func show() {
        guard let sourceWindow, sourceWindow.isVisible else { return }
        matchSourceWindow()
        if parent !== sourceWindow {
            parent?.removeChildWindow(self)
            sourceWindow.addChildWindow(self, ordered: .above)
        }
        orderFront(nil)
        onShow?()
    }

    func hide() {
        parent?.removeChildWindow(self)
        orderOut(nil)
    }

    func matchSourceWindow() {
        guard let sourceWindow else { return }
        setFrame(sourceWindow.convertToScreen(sourceWindow.contentLayoutRect), display: false)
    }

    @objc private func sourceWindowDidResize(_ notification: Notification) {
        matchSourceWindow()
    }

    @objc private func sourceWindowDidChangeOcclusionState(_ notification: Notification) {
        guard wantsVisible, !isVisible else { return }
        show()
    }
}
#endif
