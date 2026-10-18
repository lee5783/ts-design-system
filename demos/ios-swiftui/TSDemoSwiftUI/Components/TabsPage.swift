import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct TabsPage: View {
    @State private var section = TabsSample.sections[0].id
    @State private var range = TabsSample.ranges[1].id
    @State private var blockRange = TabsSample.ranges[0].id
    @State private var layout = TabsSample.layouts[0].id
    @State private var overflowSection = TabsSample.manySections[0].id
    @State private var variant = TSTabsVariant.segmented
    @State private var isDisabled = false
    @State private var isBlock = true
    @State private var selection = TabsSample.ranges[0].id
    @State private var changeCount = 0

    var body: some View {
        DemoPage(page: .tabs) {
            DemoComponentSection(.variants) {
                DemoCaption(text: DemoStrings.string(.underline))
                TSTabs(items: TabsSample.sections, selection: $section)
                DemoCaption(text: DemoStrings.string(.segmented))
                TSTabs(items: Array(TabsSample.ranges.prefix(3)), selection: $range, variant: .segmented)
            }
            DemoComponentSection(.block) {
                TSTabs(items: TabsSample.ranges, selection: $blockRange, variant: .segmented, isBlock: true)
                TSTabs(items: TabsSample.layouts, selection: $layout, variant: .segmented)
            }
            DemoComponentSection(.states) {
                ForEach(TSTabsVariant.allCases, id: \.self) { variant in
                    DemoCaption(text: TabsSample.caption(variant))
                    TSTabs(items: Array(TabsSample.ranges.prefix(3)), selection: .constant(TabsSample.ranges[1].id), variant: variant, isBlock: true)
                        .disabled(true)
                }
            }
            DemoComponentSection(.overflow) {
                TSTabs(items: TabsSample.manySections, selection: $overflowSection)
                    .accessibilityIdentifier("demo.tabs.overflow")
                DemoCaption(text: TabsSample.label(for: overflowSection))
                    .accessibilityIdentifier("demo.tabs.overflow.value")
            }
            DemoComponentSection(.playground) {
                Picker(DemoStrings.string(.variant), selection: $variant) {
                    ForEach(TSTabsVariant.allCases, id: \.self) { Text($0.rawValue).tag($0) }
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("demo.tabs.playground.variant")
                Toggle(DemoStrings.string(.stateDisabled), isOn: $isDisabled)
                    .accessibilityIdentifier("demo.tabs.playground.disabled")
                Toggle(DemoStrings.string(.block), isOn: $isBlock)
                    .accessibilityIdentifier("demo.tabs.playground.block")
                TSTabs(items: TabsSample.ranges, selection: countedSelection, variant: variant, isBlock: isBlock)
                    .disabled(isDisabled)
                    .accessibilityIdentifier("demo.tabs.playground.tabs")
                DemoCaption(text: TabsSample.label(for: selection))
                    .accessibilityIdentifier("demo.tabs.playground.value")
                DemoTapCounter(count: changeCount, identifier: "demo.tabs.playground.count")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }

    private var countedSelection: Binding<String> {
        Binding(
            get: { selection },
            set: { newValue in
                selection = newValue
                changeCount += 1
            }
        )
    }
}

enum TabsSample {
    static let sections = items([.sampleOverview, .sampleActivity, .sampleSettings])
    static let ranges = items([.sampleDay, .sampleWeek, .sampleMonth, .sampleYear])
    static let layouts = items([.sampleGrid, .sampleList])
    static let manySections = items([
        .sampleOverview, .sampleActivity, .sampleMembers, .sampleBilling, .sampleSettings,
        .sampleIntegrations, .sampleSecurity, .sampleNotifications, .sampleReports
    ])

    static func label(for id: String) -> String {
        let all = sections + ranges + layouts + manySections
        let label = all.first { $0.id == id }?.label ?? id
        return "\(DemoStrings.string(.selected)): \(label)"
    }

    static func caption(_ variant: TSTabsVariant) -> String {
        "\(DemoStrings.string(variant == .underline ? .underline : .segmented)), \(DemoStrings.string(.stateDisabled))"
    }

    private static func items(_ keys: [DemoStringKey]) -> [TSTabItem] {
        keys.map { TSTabItem(id: $0.rawValue, label: DemoStrings.string($0)) }
    }
}
