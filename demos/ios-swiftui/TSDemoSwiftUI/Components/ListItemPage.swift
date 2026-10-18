import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct ListItemPage: View {
    @State private var isWifiOn = true
    @State private var isDarkModeOn = false
    @State private var stateTapCount = 0
    @State private var showsSubtitle = false
    @State private var showsChevron = false
    @State private var isDisabled = false
    @State private var leading: ListItemSample.Leading = .none
    @State private var trailing: ListItemSample.Trailing = .none
    @State private var isPlaygroundOn = false
    @State private var tapCount = 0
    @State private var expandedLicence: String?

    var body: some View {
        DemoPage(page: .listItem) {
            DemoComponentSection(.contents) {
                TSList {
                    TSListItem(ListItemSample.favorites.title)
                    TSListItem(ListItemSample.inbox.title, subtitle: ListItemSample.inbox.subtitle, showsChevron: true, action: {}) {
                        TSListItemIcon(.mail)
                    } trailing: {
                        TSBadge(DemoStrings.string(.sampleNew), tone: .accent)
                    }
                    .accessibilityIdentifier("demo.list-item.inbox")
                    TSListItem(ListItemSample.calendar.title, subtitle: ListItemSample.calendar.subtitle) {
                        TSListItemIcon(.calendar)
                    } trailing: {
                        Text(DemoStrings.string(.sampleToday))
                    }
                    TSListItem(ListItemSample.person.name, subtitle: ListItemSample.person.role) {
                        TSAvatar(name: ListItemSample.person.name, image: DemoSamplePhoto.image)
                    } trailing: {
                        Text(DemoStrings.string(.sampleOnline))
                    }
                    TSListItem(DemoStrings.string(.sampleWifi), isOn: $isWifiOn) {
                        TSListItemIcon(.settings)
                    }
                    .accessibilityIdentifier("demo.list-item.wifi")
                    TSListItem(ListItemSample.settings.title, showsChevron: true, action: {})
                }
                DemoCaption(text: DemoStrings.string(isWifiOn ? .stateOn : .stateOff))
                    .accessibilityIdentifier("demo.list-item.wifi.value")
            }
            DemoComponentSection(.states) {
                TSList {
                    TSListItem(DemoStrings.string(.stateDefault), showsChevron: true, action: { stateTapCount += 1 })
                        .accessibilityIdentifier("demo.list-item.state.default")
                    TSListItem(DemoStrings.string(.stateDisabled), showsChevron: true, action: { stateTapCount += 1 })
                        .disabled(true)
                        .accessibilityIdentifier("demo.list-item.state.disabled")
                    TSListItem(DemoStrings.string(.noAction), subtitle: ListItemSample.favorites.title)
                        .accessibilityIdentifier("demo.list-item.state.static")
                    TSListItem(DemoStrings.string(.sampleDarkMode), isOn: $isDarkModeOn)
                        .disabled(true)
                }
                DemoTapCounter(count: stateTapCount, identifier: "demo.list-item.state.count")
            }
            DemoComponentSection(.lazyDivider) {
                LazyVStack(spacing: 0) {
                    ForEach(Array(SampleListRows.all.enumerated()), id: \.element.id) { index, row in
                        if index > 0 {
                            TSDivider()
                        }
                        TSListItem(row.title, subtitle: row.subtitle)
                    }
                }
                .background(TSColor.surfaceCard.color)
                .accessibilityIdentifier("demo.list-item.lazy-divider")
            }
            DemoComponentSection(.disclosure) {
                TSList {
                    ForEach(ListItemSample.licences, id: \.title) { licence in
                        TSDisclosureListItem(licence.title, subtitle: licence.licence, isExpanded: expansion(licence.title)) {
                            TSText(licence.body, role: .bodySm, color: .secondary)
                                .accessibilityIdentifier("demo.list-item.disclosure.body")
                        }
                        .accessibilityIdentifier("demo.list-item.disclosure.\(licence.id)")
                    }
                }
            }
            DemoComponentSection(.truncation) {
                TSList {
                    TSListItem(DemoStrings.string(.sampleLongSetting), subtitle: ListItemSample.downloads.subtitle, showsChevron: true, action: {}) {
                        TSListItemIcon(.download)
                    } trailing: {
                        Text(DemoStrings.string(.sampleToday))
                    }
                }
            }
            DemoComponentSection(.playground) {
                Toggle(DemoStrings.string(.subtitle), isOn: $showsSubtitle)
                    .accessibilityIdentifier("demo.list-item.playground.subtitle")
                Toggle(DemoStrings.string(.chevron), isOn: $showsChevron)
                    .accessibilityIdentifier("demo.list-item.playground.chevron")
                Toggle(DemoStrings.string(.stateDisabled), isOn: $isDisabled)
                    .accessibilityIdentifier("demo.list-item.playground.disabled")
                Picker(DemoStrings.string(.leading), selection: $leading) {
                    ForEach(ListItemSample.Leading.allCases, id: \.self) { Text($0.title).tag($0) }
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("demo.list-item.playground.leading")
                Picker(DemoStrings.string(.trailing), selection: $trailing) {
                    ForEach(ListItemSample.Trailing.allCases, id: \.self) { Text($0.title).tag($0) }
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("demo.list-item.playground.trailing")
                TSList {
                    playgroundRow
                        .disabled(isDisabled)
                        .accessibilityIdentifier("demo.list-item.playground.row")
                }
                DemoCaption(text: DemoStrings.string(isPlaygroundOn ? .stateOn : .stateOff))
                    .accessibilityIdentifier("demo.list-item.playground.value")
                DemoTapCounter(count: tapCount, identifier: "demo.list-item.playground.count")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }

    private func expansion(_ title: String) -> Binding<Bool> {
        Binding(
            get: { expandedLicence == title },
            set: { expandedLicence = $0 ? title : nil }
        )
    }

    @ViewBuilder
    private var playgroundRow: some View {
        let title = ListItemSample.inbox.title
        let subtitle = showsSubtitle ? ListItemSample.inbox.subtitle : nil
        switch trailing {
        case .switch:
            TSListItem(title, subtitle: subtitle, isOn: $isPlaygroundOn) {
                playgroundLeading
            }
        case .none, .text, .badge:
            TSListItem(title, subtitle: subtitle, showsChevron: showsChevron, action: { tapCount += 1 }) {
                playgroundLeading
            } trailing: {
                playgroundTrailing
            }
        }
    }

    @ViewBuilder
    private var playgroundLeading: some View {
        switch leading {
        case .none:
            EmptyView()
        case .icon:
            TSListItemIcon(.mail)
        case .avatar:
            TSAvatar(name: ListItemSample.person.name)
        }
    }

    @ViewBuilder
    private var playgroundTrailing: some View {
        switch trailing {
        case .text:
            Text(DemoStrings.string(.sampleToday))
        case .badge:
            TSBadge(DemoStrings.string(.sampleNew), tone: .accent)
        case .none, .switch:
            EmptyView()
        }
    }
}

enum ListItemSample {
    enum Leading: String, CaseIterable {
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

    enum Trailing: String, CaseIterable {
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
