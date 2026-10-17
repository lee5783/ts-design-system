import Combine
import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    static let urlScheme = "tsds-uikit"

    var window: UIWindow?
    private var toastWindow: TSToastWindow?
    private var settings: DemoSettingsModel?
    private var windowAppearance: DemoWindowAppearance?
    private var subscription: AnyCancellable?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let options = DemoLaunchOptions(arguments: ProcessInfo.processInfo.arguments)
        let settings = DemoSettingsModel(launchOptions: options, themeStore: DemoThemeStore())
        self.settings = settings
        let root = options.page.map(DemoPageFactory.makeViewController(for:)) ?? CatalogViewController(settings: settings)
        let navigationController = UINavigationController(rootViewController: root)
        navigationController.navigationBar.prefersLargeTitles = true
        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = DemoSettingsContainerViewController(settings: settings, content: navigationController)
        self.window = window
        windowAppearance = DemoWindowAppearance(windowScene: windowScene, settings: settings)
        window.makeKeyAndVisible()
        installToastWindow(in: windowScene, settings: settings)
        windowAppearance?.apply()
        handle(connectionOptions.urlContexts)
    }

    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        handle(URLContexts)
    }

    private func handle(_ contexts: Set<UIOpenURLContext>) {
        for context in contexts {
            if let theme = DemoLaunchOverrides.theme(url: context.url, scheme: Self.urlScheme) {
                settings?.theme = theme
            }
        }
    }

    private func installToastWindow(in windowScene: UIWindowScene, settings: DemoSettingsModel) {
        let toastWindow = TSToastWindow(windowScene: windowScene)
        self.toastWindow = toastWindow
        subscription = Publishers.CombineLatest3(settings.$theme, settings.$textSize, settings.$isRightToLeft)
            .sink { [weak toastWindow] theme, textSize, isRightToLeft in
                toastWindow?.applyTraits(from: UITraitCollection(traitsFrom: [
                    UITraitCollection(userInterfaceStyle: theme.userInterfaceStyle),
                    UITraitCollection(preferredContentSizeCategory: textSize.contentSizeCategory ?? .unspecified),
                    UITraitCollection(layoutDirection: isRightToLeft ? .rightToLeft : .unspecified)
                ]))
            }
    }
}
