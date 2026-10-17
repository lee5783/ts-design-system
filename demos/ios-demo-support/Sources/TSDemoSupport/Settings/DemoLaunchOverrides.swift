import Foundation

public enum DemoLaunchOverrides {
    public static let themeArgument = "-demoTheme"
    public static let settingsHost = "settings"
    public static let themeQueryItem = "theme"

    public static func theme(arguments: [String]) -> DemoTheme? {
        guard let index = arguments.firstIndex(of: themeArgument) else { return nil }
        let value = arguments.indices.contains(index + 1) ? arguments[index + 1] : ""
        return theme(named: value)
    }

    public static func theme(url: URL, scheme: String) -> DemoTheme? {
        guard url.scheme?.lowercased() == scheme.lowercased(),
              url.host?.lowercased() == settingsHost,
              let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
              let item = components.queryItems?.first(where: { $0.name == themeQueryItem })
        else { return nil }
        return theme(named: item.value ?? "")
    }

    public static func settingsURL(scheme: String, theme: DemoTheme) -> URL? {
        var components = URLComponents()
        components.scheme = scheme
        components.host = settingsHost
        components.queryItems = [URLQueryItem(name: themeQueryItem, value: theme.rawValue)]
        return components.url
    }

    static func theme(named value: String) -> DemoTheme {
        DemoTheme(rawValue: value.trimmingCharacters(in: .whitespaces).lowercased()) ?? .system
    }
}
