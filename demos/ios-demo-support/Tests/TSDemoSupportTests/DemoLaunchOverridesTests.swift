import Foundation
import Testing
import TSDemoSupport

@Suite("DemoLaunchOverrides")
struct DemoLaunchOverridesTests {
    @Test("Reads -demoTheme", arguments: [
        (["App", "-demoTheme", "dark"], DemoTheme.dark),
        (["App", "-demoTheme", "LIGHT"], .light),
        (["App", "-demoTheme", "system"], .system),
    ])
    func argument(arguments: [String], expected: DemoTheme) {
        #expect(DemoLaunchOverrides.theme(arguments: arguments) == expected)
    }

    @Test("An unknown or missing -demoTheme value falls back to System", arguments: [
        ["-demoTheme", "sepia"],
        ["-demoTheme", ""],
        ["-demoTheme"],
    ])
    func argumentFallback(arguments: [String]) {
        #expect(DemoLaunchOverrides.theme(arguments: arguments) == .system)
    }

    @Test("No -demoTheme argument means no override")
    func argumentAbsent() {
        #expect(DemoLaunchOverrides.theme(arguments: ["App", "-TSDemoPage", "button"]) == nil)
    }

    @Test("-demoTheme wins over -TSDemoTheme in the launch options")
    func launchOptionsPrecedence() {
        let options = DemoLaunchOptions(arguments: ["-TSDemoTheme", "light", "-demoTheme", "dark"])
        #expect(options.theme == .dark)
        #expect(DemoLaunchOptions(arguments: ["-TSDemoTheme", "light"]).theme == .light)
    }

    @Test("Reads the settings deep link", arguments: [
        ("tsds-swiftui://settings?theme=dark", DemoTheme.dark),
        ("TSDS-SWIFTUI://Settings?theme=Light", .light),
        ("tsds-swiftui://settings?other=1&theme=system", .system),
        ("tsds-swiftui://settings?theme=sepia", .system),
        ("tsds-swiftui://settings?theme=", .system),
    ])
    func deepLink(string: String, expected: DemoTheme) throws {
        let url = try #require(URL(string: string))
        #expect(DemoLaunchOverrides.theme(url: url, scheme: "tsds-swiftui") == expected)
    }

    @Test("Ignores links for another scheme, host, or without a theme", arguments: [
        "tsds-uikit://settings?theme=dark",
        "tsds-swiftui://about?theme=dark",
        "tsds-swiftui://settings",
        "https://settings?theme=dark",
    ])
    func deepLinkIgnored(string: String) throws {
        let url = try #require(URL(string: string))
        #expect(DemoLaunchOverrides.theme(url: url, scheme: "tsds-swiftui") == nil)
    }

    @Test("Builds a settings link that parses back", arguments: DemoTheme.allCases)
    func settingsURLRoundTrip(theme: DemoTheme) throws {
        let url = try #require(DemoLaunchOverrides.settingsURL(scheme: "tsds-uikit", theme: theme))
        #expect(url.absoluteString == "tsds-uikit://settings?theme=\(theme.rawValue)")
        #expect(DemoLaunchOverrides.theme(url: url, scheme: "tsds-uikit") == theme)
    }
}

@Suite("DemoTheme")
struct DemoThemeTests {
    @Test("The quick toggle cycles System, Light, Dark")
    func cycle() {
        #expect(DemoTheme.system.next == .light)
        #expect(DemoTheme.light.next == .dark)
        #expect(DemoTheme.dark.next == .system)
    }

    @Test("Each mode has its own icon and a Theme label", arguments: DemoTheme.allCases)
    func iconAndLabel(theme: DemoTheme) {
        #expect(!theme.symbolName.isEmpty)
        #expect(DemoStrings.themeToggleLabel(theme) == "Theme: \(theme.title)")
        #expect(Set(DemoTheme.allCases.map(\.symbolName)).count == DemoTheme.allCases.count)
    }
}

@Suite("DemoThemeStore")
@MainActor
struct DemoThemeStoreTests {
    private func makeStore() -> (DemoThemeStore, UserDefaults) {
        let name = "demo-theme-store-\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: name) ?? .standard
        defaults.removePersistentDomain(forName: name)
        return (DemoThemeStore(defaults: defaults), defaults)
    }

    @Test("A theme change persists, and the next launch starts from it")
    func persists() {
        let (store, _) = makeStore()
        let model = DemoSettingsModel(launchOptions: DemoLaunchOptions(), themeStore: store)
        #expect(model.theme == .system)
        model.theme = model.theme.next
        #expect(store.load() == .light)
        let relaunched = DemoSettingsModel(launchOptions: DemoLaunchOptions(), themeStore: store)
        #expect(relaunched.theme == .light)
    }

    @Test("An external override wins over the stored theme and persists like an in-app change")
    func overridePersists() {
        let (store, _) = makeStore()
        store.save(.light)
        let model = DemoSettingsModel(launchOptions: DemoLaunchOptions(arguments: ["-demoTheme", "dark"]), themeStore: store)
        #expect(model.theme == .dark)
        #expect(store.load() == .dark)
    }

    @Test("Reset returns the stored theme to System")
    func resetPersists() {
        let (store, _) = makeStore()
        let model = DemoSettingsModel(launchOptions: DemoLaunchOptions(theme: .dark), themeStore: store)
        model.reset()
        #expect(store.load() == .system)
    }

    @Test("An unreadable stored value falls back to System")
    func corruptValue() {
        let (store, defaults) = makeStore()
        defaults.set("sepia", forKey: DemoThemeStore.key)
        #expect(store.load() == nil)
        #expect(DemoSettingsModel(launchOptions: DemoLaunchOptions(), themeStore: store).theme == .system)
    }
}
