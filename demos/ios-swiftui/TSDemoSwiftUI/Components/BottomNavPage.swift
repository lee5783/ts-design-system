import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct BottomNavPage: View {
    @State private var threeSelection = BottomNavSample.three[0].id
    @State private var fourSelection = BottomNavSample.four[1].id
    @State private var fiveSelection = BottomNavSample.five[0].id
    @State private var itemCount = 4
    @State private var badge = 3
    @State private var isDisabled = false
    @State private var selection = BottomNavSample.five[0].id
    @State private var changeCount = 0
    @State private var isShowingShell = false

    var body: some View {
        DemoPage(page: .bottomNav) {
            DemoComponentSection(.variants) {
                DemoCaption(text: DemoStrings.string(.threeItems))
                bar(BottomNavSample.three, selection: $threeSelection)
                DemoCaption(text: DemoStrings.string(.fourItemsWithBadges))
                bar(BottomNavSample.four, selection: $fourSelection)
                DemoCaption(text: DemoStrings.string(.fiveItems))
                bar(BottomNavSample.five, selection: $fiveSelection)
            }
            DemoComponentSection(.states) {
                DemoCaption(text: DemoStrings.string(.stateDisabled))
                bar(BottomNavSample.four, selection: .constant(BottomNavSample.four[0].id))
                    .disabled(true)
            }
            DemoComponentSection(.shell) {
                TSButton(DemoStrings.string(.openShell), variant: .secondary) { isShowingShell = true }
                    .accessibilityIdentifier("demo.bottom-nav.open-shell")
                DemoCaption(text: DemoStrings.string(.shellCaption))
                NavigationLink(isActive: $isShowingShell) {
                    BottomNavShellDemo()
                } label: {
                    EmptyView()
                }
                .hidden()
            }
            DemoComponentSection(.playground) {
                Picker(DemoStrings.string(.itemCount), selection: $itemCount) {
                    ForEach(BottomNavSample.counts, id: \.self) { Text("\($0)").tag($0) }
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("demo.bottom-nav.playground.count")
                Toggle(DemoStrings.string(.stateDisabled), isOn: $isDisabled)
                    .accessibilityIdentifier("demo.bottom-nav.playground.disabled")
                DemoComponentRow {
                    TSButton(DemoStrings.string(.sampleNewMessage), variant: .secondary) { badge += 1 }
                        .accessibilityIdentifier("demo.bottom-nav.playground.add-one")
                    TSButton(DemoStrings.string(.sampleAddHundred), variant: .secondary) { badge += 100 }
                        .accessibilityIdentifier("demo.bottom-nav.playground.add-hundred")
                }
                TSButton(DemoStrings.string(.sampleMarkAllRead), variant: .ghost) { badge = 0 }
                    .accessibilityIdentifier("demo.bottom-nav.playground.clear")
                bar(playgroundItems, selection: countedSelection)
                    .disabled(isDisabled)
                    .accessibilityIdentifier("demo.bottom-nav.playground.nav")
                DemoCaption(text: BottomNavSample.label(for: selection))
                    .accessibilityIdentifier("demo.bottom-nav.playground.value")
                DemoTapCounter(count: changeCount, identifier: "demo.bottom-nav.playground.changes")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }

    private var playgroundItems: [TSBottomNavItem] {
        BottomNavSample.items(count: itemCount, inboxBadge: badge)
    }

    private var countedSelection: Binding<String> {
        Binding(
            get: { playgroundItems.contains { $0.id == selection } ? selection : playgroundItems[0].id },
            set: { newValue in
                selection = newValue
                changeCount += 1
            }
        )
    }

    private func bar(_ items: [TSBottomNavItem], selection: Binding<String>) -> some View {
        TSBottomNav(items: items, selection: selection)
    }
}

struct BottomNavShellDemo: View {
    @State private var selection = BottomNavSample.four[0].id
    @State private var inboxBadge = 3

    var body: some View {
        TSBottomNavShell(
            items: BottomNavSample.items(count: 4, inboxBadge: inboxBadge),
            selection: $selection
        ) { item in
            BottomNavShellTab(item: item, inboxBadge: $inboxBadge)
        }
        .background(TSColor.backgroundPrimary.color.edgesIgnoringSafeArea(.all))
        .navigationTitle(DemoStrings.string(.shell))
        .navigationBarTitleDisplayMode(.inline)
        .demoThemeToolbar()
        .accessibilityIdentifier("demo.bottom-nav.shell")
    }
}

struct BottomNavShellTab: View {
    let item: TSBottomNavItem
    @Binding var inboxBadge: Int

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: TSSpacing.space4) {
                Text(item.label)
                    .foregroundColor(TSColor.textPrimary.color)
                    .tsFont(.title2)
                    .accessibilityIdentifier("demo.bottom-nav.shell.title.\(item.id)")
                Text(DemoStrings.string(.sampleShellBody))
                    .foregroundColor(TSColor.textSecondary.color)
                    .tsFont(.body)
                TSButton(DemoStrings.string(.sampleNewMessage), variant: .secondary) { inboxBadge += 1 }
                    .accessibilityIdentifier("demo.bottom-nav.shell.add-one.\(item.id)")
                TSButton(DemoStrings.string(.sampleMarkAllRead), variant: .ghost) { inboxBadge = 0 }
                    .accessibilityIdentifier("demo.bottom-nav.shell.clear.\(item.id)")
            }
            .padding(TSSpacing.space4)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

enum BottomNavSample {
    static let counts = [3, 4, 5]
    static let three = items(count: 3, inboxBadge: 0)
    static let four = items(count: 4, inboxBadge: 3, alertsBadge: 120)
    static let five = items(count: 5, inboxBadge: 3, alertsBadge: 120)

    static func items(count: Int, inboxBadge: Int, alertsBadge: Int? = nil) -> [TSBottomNavItem] {
        let all = [
            TSBottomNavItem(id: DemoStringKey.sampleHome.rawValue, label: DemoStrings.string(.sampleHome), icon: .home),
            TSBottomNavItem(id: DemoStringKey.sampleInbox.rawValue, label: DemoStrings.string(.sampleInbox), icon: .mail, badge: inboxBadge),
            TSBottomNavItem(id: DemoStringKey.sampleSearch.rawValue, label: DemoStrings.string(.sampleSearch), icon: .search),
            TSBottomNavItem(id: DemoStringKey.sampleAlerts.rawValue, label: DemoStrings.string(.sampleAlerts), icon: .bell, badge: alertsBadge),
            TSBottomNavItem(id: DemoStringKey.sampleProfile.rawValue, label: DemoStrings.string(.sampleProfile), icon: .user)
        ]
        switch count {
        case 3:
            return [all[0], all[1], all[4]]
        case 4:
            return Array(all.prefix(4))
        default:
            return all
        }
    }

    static func label(for id: String) -> String {
        let label = items(count: 5, inboxBadge: 0).first { $0.id == id }?.label ?? id
        return "\(DemoStrings.string(.selected)): \(label)"
    }
}
