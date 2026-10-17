import Foundation

public enum DemoCatalog {
    public static let sections: [DemoSection] = [
        DemoSection(id: .foundations, pages: [.colors, .typography, .spacing, .radius, .size, .opacity, .elevation, .icons]),
        DemoSection(id: .actions, pages: [.button, .iconButton]),
        DemoSection(id: .forms, pages: [.textField, .checkbox, .switch]),
        DemoSection(id: .navigation, pages: [.tabs, .bottomNav]),
        DemoSection(id: .content, pages: [.badge, .avatar, .listItem, .card]),
        DemoSection(id: .feedback, pages: [.alert, .dialog, .toast])
    ]

    public static var allPages: [DemoPageID] {
        sections.flatMap(\.pages)
    }

    public static var foundationPages: [DemoPageID] {
        allPages.filter { $0.kind == .foundation }
    }

    public static var componentPages: [DemoPageID] {
        allPages.filter { $0.kind == .component }
    }

    public static func section(containing page: DemoPageID) -> DemoSection? {
        sections.first { $0.pages.contains(page) }
    }
}
