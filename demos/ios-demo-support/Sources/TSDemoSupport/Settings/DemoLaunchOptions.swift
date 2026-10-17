import Foundation

public struct DemoLaunchOptions: Hashable, Sendable {
    public static let themeArgument = "-TSDemoTheme"
    public static let textSizeArgument = "-TSDemoTextSize"
    public static let rightToLeftArgument = "-TSDemoRightToLeft"
    public static let reduceMotionArgument = "-TSDemoReduceMotion"
    public static let pageArgument = "-TSDemoPage"

    public var theme: DemoTheme?
    public var textSize: DemoTextSize?
    public var isRightToLeft: Bool?
    public var reduceMotion: Bool?
    public var page: DemoPageID?

    public init(
        theme: DemoTheme? = nil,
        textSize: DemoTextSize? = nil,
        isRightToLeft: Bool? = nil,
        reduceMotion: Bool? = nil,
        page: DemoPageID? = nil
    ) {
        self.theme = theme
        self.textSize = textSize
        self.isRightToLeft = isRightToLeft
        self.reduceMotion = reduceMotion
        self.page = page
    }

    public init(arguments: [String]) {
        func value(after flag: String) -> String? {
            guard let index = arguments.firstIndex(of: flag), arguments.indices.contains(index + 1) else { return nil }
            return arguments[index + 1]
        }
        func flag(_ name: String) -> Bool? {
            value(after: name).flatMap(Self.bool(from:))
        }
        self.init(
            theme: DemoLaunchOverrides.theme(arguments: arguments) ?? value(after: Self.themeArgument).flatMap(DemoTheme.init(rawValue:)),
            textSize: value(after: Self.textSizeArgument).flatMap(DemoTextSize.init(rawValue:)),
            isRightToLeft: flag(Self.rightToLeftArgument),
            reduceMotion: flag(Self.reduceMotionArgument),
            page: value(after: Self.pageArgument).flatMap(DemoPageID.init(rawValue:))
        )
    }

    public var arguments: [String] {
        var result: [String] = []
        if let theme { result += [Self.themeArgument, theme.rawValue] }
        if let textSize { result += [Self.textSizeArgument, textSize.rawValue] }
        if let isRightToLeft { result += [Self.rightToLeftArgument, isRightToLeft ? "YES" : "NO"] }
        if let reduceMotion { result += [Self.reduceMotionArgument, reduceMotion ? "YES" : "NO"] }
        if let page { result += [Self.pageArgument, page.rawValue] }
        return result
    }

    private static func bool(from value: String) -> Bool? {
        switch value.lowercased() {
        case "yes", "true", "1":
            true
        case "no", "false", "0":
            false
        default:
            nil
        }
    }
}
