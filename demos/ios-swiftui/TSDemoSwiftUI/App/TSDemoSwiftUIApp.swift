import SwiftUI
import TSDemoSupport

@main
struct TSDemoSwiftUIApp: App {
    @StateObject private var settings: DemoSettingsModel
    private let launchPage: DemoPageID?

    static let urlScheme = "tsds-swiftui"

    init() {
        let options = DemoLaunchOptions(arguments: ProcessInfo.processInfo.arguments)
        _settings = StateObject(wrappedValue: DemoSettingsModel(launchOptions: options, themeStore: DemoThemeStore()))
        launchPage = options.page
    }

    var body: some Scene {
        WindowGroup {
            DemoRootView(launchPage: launchPage)
                .environmentObject(settings)
                .onOpenURL { url in
                    if let theme = DemoLaunchOverrides.theme(url: url, scheme: Self.urlScheme) {
                        settings.theme = theme
                    }
                }
        }
    }
}
