import Combine
import Foundation

@MainActor
public final class DemoSettingsModel: ObservableObject {
    @Published public var theme: DemoTheme {
        didSet { themeStore?.save(theme) }
    }
    @Published public var textSize: DemoTextSize
    @Published public var isRightToLeft: Bool
    @Published public var reduceMotion: Bool

    private let themeStore: DemoThemeStore?

    public init(
        theme: DemoTheme = .system,
        textSize: DemoTextSize = .system,
        isRightToLeft: Bool = false,
        reduceMotion: Bool = false,
        themeStore: DemoThemeStore? = nil
    ) {
        self.themeStore = themeStore
        self.theme = theme
        self.textSize = textSize
        self.isRightToLeft = isRightToLeft
        self.reduceMotion = reduceMotion
    }

    public convenience init(launchOptions: DemoLaunchOptions, themeStore: DemoThemeStore? = nil) {
        self.init(
            theme: launchOptions.theme ?? themeStore?.load() ?? .system,
            textSize: launchOptions.textSize ?? .system,
            isRightToLeft: launchOptions.isRightToLeft ?? false,
            reduceMotion: launchOptions.reduceMotion ?? false,
            themeStore: themeStore
        )
        themeStore?.save(theme)
    }

    public var isDefault: Bool {
        theme == .system && textSize == .system && !isRightToLeft && !reduceMotion
    }

    public func reset() {
        theme = .system
        textSize = .system
        isRightToLeft = false
        reduceMotion = false
    }
}
