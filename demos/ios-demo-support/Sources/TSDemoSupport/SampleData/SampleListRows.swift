import Foundation

public struct SampleListRow: Hashable, Sendable, Identifiable {
    public let id: String
    public let title: String
    public let subtitle: String?
    public let iconAssetName: String

    public init(id: String, title: String, subtitle: String?, iconAssetName: String) {
        self.id = id
        self.title = title
        self.subtitle = subtitle
        self.iconAssetName = iconAssetName
    }
}

public enum SampleListRows {
    public static let all: [SampleListRow] = [
        SampleListRow(id: "inbox", title: "Inbox", subtitle: "12 unread messages", iconAssetName: "mail"),
        SampleListRow(id: "calendar", title: "Calendar", subtitle: "Design review at 3 PM", iconAssetName: "calendar"),
        SampleListRow(id: "favorites", title: "Favorites", subtitle: nil, iconAssetName: "star"),
        SampleListRow(id: "downloads", title: "Downloads", subtitle: "A very long subtitle that wraps onto a second line at larger text sizes", iconAssetName: "download"),
        SampleListRow(id: "settings", title: "Settings", subtitle: nil, iconAssetName: "settings")
    ]
}
