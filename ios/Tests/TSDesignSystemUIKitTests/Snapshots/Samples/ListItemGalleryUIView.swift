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

enum ListItemExtrasGalleryUIKind: String, CaseIterable {
    case disclosure
    case divider
}

final class ListItemExtrasGalleryUIView: GalleryStackView {
    init(kind: ListItemExtrasGalleryUIKind) {
        super.init(frame: .zero)
        stack.alignment = .fill
        stack.spacing = TSSpacing.space6
        switch kind {
        case .disclosure:
            stack.addArrangedSubview(TSListView(items: Self.disclosures()))
            stack.addArrangedSubview(TSDisclosureListItemView(title: "Standalone", bodyView: Self.body("Outside a list"), isExpanded: true))
        case .divider:
            let plain = UIStackView(arrangedSubviews: [
                TSListItemView(configuration: TSListItemConfiguration(title: "First row")),
                TSDividerView(),
                TSListItemView(configuration: TSListItemConfiguration(title: "Second row", subtitle: "Divided with TSDividerView")),
                TSDividerView(),
                TSListItemView(configuration: TSListItemConfiguration(title: "Third row"))
            ])
            plain.axis = .vertical
            plain.backgroundColor = TSColor.surfaceCard.uiColor
            stack.addArrangedSubview(plain)
            stack.addArrangedSubview(TSListView(items: [
                TSListItemView(configuration: TSListItemConfiguration(title: "First row")),
                TSListItemView(configuration: TSListItemConfiguration(title: "Second row", subtitle: "Divided inside TSListView")),
                TSListItemView(configuration: TSListItemConfiguration(title: "Third row"))
            ]))
        }
    }

    static func body(_ text: String) -> UILabel {
        let label = UILabel()
        label.numberOfLines = 0
        label.attributedText = TSTypeRole.bodySm.font.attributedString(
            text,
            sizeCategory: .large,
            color: TSTextColor.secondary.color.uiColor
        )
        label.adjustsFontForContentSizeCategory = false
        return label
    }

    private static func disclosures() -> [UIView] {
        let pressed = TSDisclosureListItemView(title: "Pressed", bodyView: body("Hidden body"))
        pressed.isHighlighted = true
        let disabled = TSDisclosureListItemView(title: "Disabled", bodyView: body("Dimmed body"), isExpanded: true)
        disabled.isEnabled = false
        return [
            TSDisclosureListItemView(title: "Collapsed", bodyView: body("Hidden body")),
            TSDisclosureListItemView(
                title: "Kotlin",
                subtitle: "Apache License 2.0",
                leading: .icon(.info),
                bodyView: body("A very long subtitle that wraps onto a second line at larger text sizes"),
                isExpanded: true
            ),
            pressed,
            disabled
        ]
    }
}
