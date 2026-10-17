import TSDesignSystemUIKit
import UIKit

enum ListItemGalleryUIKind: String, CaseIterable {
    case contents
    case states
}

final class ListItemGalleryUIView: GalleryStackView {
    private static let longTitle = "Download large files over mobile data when Wi-Fi is unavailable"
    private static let longSubtitle = "A very long subtitle that wraps onto a second line at larger text sizes"

    init(kind: ListItemGalleryUIKind) {
        super.init(frame: .zero)
        stack.alignment = .fill
        switch kind {
        case .contents:
            stack.addArrangedSubview(TSListView(items: Self.contents.map { TSListItemView(configuration: $0) }))
        case .states:
            stack.addArrangedSubview(TSListView(items: Self.states()))
        }
    }

    private static let contents: [TSListItemConfiguration] = [
        TSListItemConfiguration(title: "Favorites"),
        TSListItemConfiguration(
            title: "Inbox",
            subtitle: "12 unread messages",
            leading: .icon(.mail),
            trailing: .badge("New", tone: .accent),
            showsChevron: true,
            isInteractive: true
        ),
        TSListItemConfiguration(title: "Ada Lovelace", subtitle: "Engineer", leading: .avatar(name: "Ada Lovelace"), trailing: .text("Online")),
        TSListItemConfiguration(title: "Wi-Fi", leading: .icon(.settings), trailing: .toggle(isOn: true)),
        TSListItemConfiguration(
            title: longTitle,
            subtitle: longSubtitle,
            leading: .icon(.download),
            trailing: .text("Today"),
            showsChevron: true,
            isInteractive: true
        )
    ]

    private static func states() -> [UIView] {
        let pressed = TSListItemView(configuration: TSListItemConfiguration(title: "Pressed", showsChevron: true, isInteractive: true))
        pressed.isHighlighted = true
        let staticPressed = TSListItemView(configuration: TSListItemConfiguration(title: "No action pressed", subtitle: "Not pressable"))
        staticPressed.isHighlighted = true
        return [
            TSListItemView(configuration: TSListItemConfiguration(title: "Default", showsChevron: true, isInteractive: true)),
            pressed,
            TSListItemView(configuration: TSListItemConfiguration(title: "Disabled", showsChevron: true, isInteractive: true, isEnabled: false)),
            staticPressed,
            TSListItemView(configuration: TSListItemConfiguration(title: "Switch off", trailing: .toggle(isOn: false))),
            TSListItemView(configuration: TSListItemConfiguration(title: "Switch disabled", trailing: .toggle(isOn: true), isEnabled: false))
        ]
    }
}
