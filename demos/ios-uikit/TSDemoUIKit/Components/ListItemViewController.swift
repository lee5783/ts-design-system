import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class ListItemViewController: DemoPageViewController {
    private var stateTapCount = 0
    private var tapCount = 0
    private var showsSubtitle = false
    private var showsChevron = false
    private var isDisabled = false
    private var leading = ListItemSample.Leading.none
    private var trailing = ListItemSample.Trailing.none
    private var isPlaygroundOn = false

    private lazy var wifiCaption: DemoLabel = {
        let caption = DemoPlayground.caption(DemoStrings.string(.stateOn))
        caption.accessibilityIdentifier = "demo.list-item.wifi.value"
        return caption
    }()
    private lazy var stateCounter = DemoPlayground.counter(identifier: "demo.list-item.state.count")
    private lazy var counter = DemoPlayground.counter(identifier: "demo.list-item.playground.count")
    private lazy var valueCaption: DemoLabel = {
        let caption = DemoPlayground.caption(DemoStrings.string(.stateOff))
        caption.accessibilityIdentifier = "demo.list-item.playground.value"
        return caption
    }()
    private lazy var playgroundRow: TSListItemView = {
        let row = TSListItemView(configuration: playgroundConfiguration)
        row.accessibilityIdentifier = "demo.list-item.playground.row"
        row.addAction(UIAction { [weak self] _ in self?.playgroundDidTap() }, for: .primaryActionTriggered)
        return row
    }()

    init() {
        super.init(page: .listItem)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [contentsSection(), statesSection(), dividerSection(), disclosureSection(), truncationSection(), playgroundSection()]
    }

    private func contentsSection() -> UIView {
        let inbox = ListItemSample.inbox
        let calendar = ListItemSample.calendar
        let person = ListItemSample.person
        let inboxRow = TSListItemView(configuration: TSListItemConfiguration(
            title: inbox.title,
            subtitle: inbox.subtitle,
            leading: .icon(.mail),
            trailing: .badge(DemoStrings.string(.sampleNew), tone: .accent),
            showsChevron: true,
            isInteractive: true
        ))
        inboxRow.accessibilityIdentifier = "demo.list-item.inbox"
        let wifiRow = TSListItemView(configuration: TSListItemConfiguration(
            title: DemoStrings.string(.sampleWifi),
            leading: .icon(.settings),
            trailing: .toggle(isOn: true),
            onToggle: { [weak self] isOn in self?.wifiCaption.string = DemoStrings.string(isOn ? .stateOn : .stateOff) }
        ))
        wifiRow.accessibilityIdentifier = "demo.list-item.wifi"
        let list = TSListView(items: [
            TSListItemView(configuration: TSListItemConfiguration(title: ListItemSample.favorites.title)),
            inboxRow,
            TSListItemView(configuration: TSListItemConfiguration(
                title: calendar.title,
                subtitle: calendar.subtitle,
                leading: .icon(.calendar),
                trailing: .text(DemoStrings.string(.sampleToday))
            )),
            TSListItemView(configuration: TSListItemConfiguration(
                title: person.name,
                subtitle: person.role,
                leading: .avatar(name: person.name, image: DemoSamplePhoto.image),
                trailing: .text(DemoStrings.string(.sampleOnline))
            )),
            wifiRow,
            TSListItemView(configuration: TSListItemConfiguration(title: ListItemSample.settings.title, showsChevron: true, isInteractive: true))
        ])
        let section = DemoComponentSectionView(.contents, views: [])
        section.addFullWidth(list)
        section.addArrangedSubview(wifiCaption)
        return section
    }

    private func statesSection() -> UIView {
        let defaultRow = TSListItemView(configuration: TSListItemConfiguration(title: DemoStrings.string(.stateDefault), showsChevron: true, isInteractive: true))
        defaultRow.accessibilityIdentifier = "demo.list-item.state.default"
        defaultRow.addAction(UIAction { [weak self] _ in self?.stateDidTap() }, for: .primaryActionTriggered)
        let disabledRow = TSListItemView(configuration: TSListItemConfiguration(
            title: DemoStrings.string(.stateDisabled),
            showsChevron: true,
            isInteractive: true,
            isEnabled: false
        ))
        disabledRow.accessibilityIdentifier = "demo.list-item.state.disabled"
        disabledRow.addAction(UIAction { [weak self] _ in self?.stateDidTap() }, for: .primaryActionTriggered)
        let staticRow = TSListItemView(configuration: TSListItemConfiguration(
            title: DemoStrings.string(.noAction),
            subtitle: ListItemSample.favorites.title
        ))
        staticRow.accessibilityIdentifier = "demo.list-item.state.static"
        let list = TSListView(items: [
            defaultRow,
            disabledRow,
            staticRow,
            TSListItemView(configuration: TSListItemConfiguration(
                title: DemoStrings.string(.sampleDarkMode),
                trailing: .toggle(isOn: false),
                isEnabled: false
            ))
        ])
        let section = DemoComponentSectionView(.states, views: [])
        section.addFullWidth(list)
        section.addArrangedSubview(stateCounter)
        return section
    }

    private func dividerSection() -> UIView {
        var rows: [UIView] = []
        for (index, row) in SampleListRows.all.enumerated() {
            if index > 0 {
                rows.append(TSDividerView())
            }
            rows.append(TSListItemView(configuration: TSListItemConfiguration(title: row.title, subtitle: row.subtitle)))
        }
        let stack = UIStackView(arrangedSubviews: rows)
        stack.axis = .vertical
        stack.backgroundColor = TSColor.surfaceCard.uiColor
        stack.accessibilityIdentifier = "demo.list-item.lazy-divider"
        let section = DemoComponentSectionView(.lazyDivider, views: [])
        section.addFullWidth(stack)
        return section
    }

    private func disclosureSection() -> UIView {
        let rows = ListItemSample.licences.map { licence in
            let body = DemoLabel(licence.body, font: TSTypeRole.bodySm.font, color: TSTextColor.secondary.color)
            body.accessibilityIdentifier = "demo.list-item.disclosure.body"
            let row = TSDisclosureListItemView(title: licence.title, subtitle: licence.licence, bodyView: body)
            row.accessibilityIdentifier = "demo.list-item.disclosure.\(licence.id)"
            return row
        }
        rows.forEach { row in
            row.addAction(UIAction { _ in
                guard row.isExpanded else { return }
                rows.filter { $0 !== row }.forEach { $0.isExpanded = false }
            }, for: .valueChanged)
        }
        let section = DemoComponentSectionView(.disclosure, views: [])
        section.addFullWidth(TSListView(items: rows))
        return section
    }

    private func truncationSection() -> UIView {
        let list = TSListView(items: [
            TSListItemView(configuration: TSListItemConfiguration(
                title: DemoStrings.string(.sampleLongSetting),
                subtitle: ListItemSample.downloads.subtitle,
                leading: .icon(.download),
                trailing: .text(DemoStrings.string(.sampleToday)),
                showsChevron: true,
                isInteractive: true
            ))
        ])
        let section = DemoComponentSectionView(.truncation, views: [])
        section.addFullWidth(list)
        return section
    }

    private func playgroundSection() -> UIView {
        let section = DemoComponentSectionView(.playground, views: [])
        [
            DemoPlayground.toggle(.subtitle, identifier: "demo.list-item.playground.subtitle") { [weak self] in
                self?.showsSubtitle = $0
                self?.updatePlayground()
            },
            DemoPlayground.toggle(.chevron, identifier: "demo.list-item.playground.chevron") { [weak self] in
                self?.showsChevron = $0
                self?.updatePlayground()
            },
            DemoPlayground.toggle(.stateDisabled, identifier: "demo.list-item.playground.disabled") { [weak self] in
                self?.isDisabled = $0
                self?.updatePlayground()
            },
            DemoPlayground.segmented(ListItemSample.Leading.allCases.map(\.title), identifier: "demo.list-item.playground.leading") { [weak self] in
                self?.leading = ListItemSample.Leading.allCases[$0]
                self?.updatePlayground()
            },
            DemoPlayground.segmented(ListItemSample.Trailing.allCases.map(\.title), identifier: "demo.list-item.playground.trailing") { [weak self] in
                self?.trailing = ListItemSample.Trailing.allCases[$0]
                self?.updatePlayground()
            },
            TSListView(items: [playgroundRow])
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(valueCaption)
        section.addArrangedSubview(counter)
        return section
    }

    private var playgroundConfiguration: TSListItemConfiguration {
        let inbox = ListItemSample.inbox
        let leadingContent: TSListItemLeading = switch leading {
        case .none: .none
        case .icon: .icon(.mail)
        case .avatar: .avatar(name: ListItemSample.person.name)
        }
        let trailingContent: TSListItemTrailing = switch trailing {
        case .none: .none
        case .text: .text(DemoStrings.string(.sampleToday))
        case .badge: .badge(DemoStrings.string(.sampleNew), tone: .accent)
        case .switch: .toggle(isOn: isPlaygroundOn)
        }
        let isToggle = trailing == .switch
        return TSListItemConfiguration(
            title: inbox.title,
            subtitle: showsSubtitle ? inbox.subtitle : nil,
            leading: leadingContent,
            trailing: trailingContent,
            showsChevron: showsChevron && !isToggle,
            isInteractive: !isToggle,
            isEnabled: !isDisabled,
            onToggle: { [weak self] isOn in self?.playgroundDidToggle(isOn) }
        )
    }

    private func updatePlayground() {
        playgroundRow.configuration = playgroundConfiguration
    }

    private func playgroundDidTap() {
        tapCount += 1
        counter.string = DemoStrings.tapCount(tapCount)
    }

    private func playgroundDidToggle(_ isOn: Bool) {
        isPlaygroundOn = isOn
        valueCaption.string = DemoStrings.string(isOn ? .stateOn : .stateOff)
    }

    private func stateDidTap() {
        stateTapCount += 1
        stateCounter.string = DemoStrings.tapCount(stateTapCount)
    }
}

enum ListItemSample {
    enum Leading: CaseIterable {
        case none
        case icon
        case avatar

        var title: String {
            switch self {
            case .none: DemoStrings.string(.none)
            case .icon: DemoStrings.string(.icon)
            case .avatar: DemoStrings.string(.person)
            }
        }
    }

    enum Trailing: CaseIterable {
        case none
        case text
        case badge
        case `switch`

        var title: String {
            switch self {
            case .none: DemoStrings.string(.none)
            case .text: DemoStrings.string(.text)
            case .badge: DemoStrings.string(.badge)
            case .switch: DemoStrings.string(.switchControl)
            }
        }
    }

    static let inbox = row("inbox")
    static let calendar = row("calendar")
    static let favorites = row("favorites")
    static let downloads = row("downloads")
    static let settings = row("settings")
    static let person = SamplePeople.all[0]
    static let licences = [
        Licence(id: "kotlin", title: DemoStrings.string(.sampleLicenceKotlin), licence: DemoStrings.string(.sampleLicenceApache), body: DemoStrings.string(.sampleLicenceApacheBody)),
        Licence(id: "snapshot", title: DemoStrings.string(.sampleLicenceSnapshot), licence: DemoStrings.string(.sampleLicenceMIT), body: DemoStrings.string(.sampleLicenceMITBody))
    ]

    struct Licence {
        let id: String
        let title: String
        let licence: String
        let body: String
    }

    private static func row(_ id: String) -> SampleListRow {
        SampleListRows.all.first { $0.id == id } ?? SampleListRows.all[0]
    }
}
