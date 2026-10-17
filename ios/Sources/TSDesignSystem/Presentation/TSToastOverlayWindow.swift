import SwiftUI
import UIKit

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
