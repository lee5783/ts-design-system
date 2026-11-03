import TSTokens
#if canImport(UIKit)
import UIKit
#elseif canImport(AppKit)
import AppKit
#endif

package enum TSToastWindowing {
    #if canImport(UIKit)
    @MainActor
    package static var windowLevel: UIWindow.Level {
        .alert + 1
    }
    #endif

    package static var hideDelay: TimeInterval {
        TSMotion.durationBase + TSMotion.durationFast
    }

    package static func keyboardInset(keyboardFrame: CGRect, windowBounds: CGRect, safeAreaBottom: CGFloat) -> CGFloat {
        guard !keyboardFrame.isNull, !keyboardFrame.isEmpty, keyboardFrame.intersects(windowBounds) else { return 0 }
        return max(0, windowBounds.maxY - keyboardFrame.minY - safeAreaBottom)
    }

    #if canImport(UIKit)
    @MainActor
    package static func statusBarController(of window: UIWindow?) -> UIViewController? {
        guard var controller = window?.rootViewController else { return nil }
        while let presented = controller.presentedViewController, presented.modalPresentationCapturesStatusBarAppearance {
            controller = presented
        }
        while let child = controller.childForStatusBarStyle {
            controller = child
        }
        return controller
    }

    @MainActor
    package static func sourceWindow(in scene: UIWindowScene?, excluding window: UIWindow) -> UIWindow? {
        scene?.windows.first { $0 !== window && $0.isKeyWindow } ?? scene?.windows.first { $0 !== window && $0.windowLevel == .normal }
    }
    #endif
}
