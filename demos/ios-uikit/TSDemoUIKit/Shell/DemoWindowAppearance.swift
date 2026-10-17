import Combine
import TSDemoSupport
import UIKit

@MainActor
final class DemoWindowAppearance {
    private weak var windowScene: UIWindowScene?
    private var style: UIUserInterfaceStyle = .unspecified
    private var subscriptions: Set<AnyCancellable> = []

    init(windowScene: UIWindowScene, settings: DemoSettingsModel) {
        self.windowScene = windowScene
        settings.$theme
            .sink { [weak self] theme in
                self?.style = theme.userInterfaceStyle
                self?.apply()
            }
            .store(in: &subscriptions)
        NotificationCenter.default.publisher(for: UIWindow.didBecomeVisibleNotification)
            .sink { [weak self] _ in self?.apply() }
            .store(in: &subscriptions)
    }

    func apply() {
        windowScene?.windows.forEach { $0.overrideUserInterfaceStyle = style }
    }
}
