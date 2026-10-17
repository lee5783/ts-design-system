import Foundation

public enum DemoTheme: String, CaseIterable, Hashable, Sendable, Identifiable {
    case system
    case light
    case dark

    public var id: String { rawValue }

    public var title: String {
        DemoStrings.themeTitle(self)
    }

    public var next: DemoTheme {
        switch self {
        case .system: .light
        case .light: .dark
        case .dark: .system
        }
    }

    public var symbolName: String {
        switch self {
        case .system: "circle.lefthalf.filled"
        case .light: "sun.max"
        case .dark: "moon"
        }
    }
}
