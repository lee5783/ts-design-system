import Foundation

public struct DemoThemeStore: @unchecked Sendable {
    public static let key = "demo.settings.theme"

    private let defaults: UserDefaults

    public init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    public func load() -> DemoTheme? {
        defaults.string(forKey: Self.key).flatMap(DemoTheme.init(rawValue:))
    }

    public func save(_ theme: DemoTheme) {
        defaults.set(theme.rawValue, forKey: Self.key)
    }
}
