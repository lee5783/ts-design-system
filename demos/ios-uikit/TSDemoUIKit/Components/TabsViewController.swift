import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class TabsViewController: DemoPageViewController {
    private var changeCount = 0
    private lazy var counter = DemoPlayground.counter(identifier: "demo.tabs.playground.count")
    private lazy var valueCaption: DemoLabel = {
        let caption = DemoPlayground.caption(TabsSample.label(for: TabsSample.ranges[0].id))
        caption.accessibilityIdentifier = "demo.tabs.playground.value"
        return caption
    }()
    private lazy var overflowCaption: DemoLabel = {
        let caption = DemoPlayground.caption(TabsSample.label(for: TabsSample.manySections[0].id))
        caption.accessibilityIdentifier = "demo.tabs.overflow.value"
        return caption
    }()
    private lazy var playgroundTabs: TSTabsView = {
        let tabs = TSTabsView(items: TabsSample.ranges, variant: .segmented, isBlock: true)
        tabs.accessibilityIdentifier = "demo.tabs.playground.tabs"
        tabs.addAction(UIAction { [weak self] _ in self?.tabsDidChange() }, for: .valueChanged)
        return tabs
    }()

    init() {
        super.init(page: .tabs)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [variantsSection(), blockSection(), statesSection(), overflowSection(), playgroundSection()]
    }

    private func variantsSection() -> UIView {
        let section = DemoComponentSectionView(.variants, views: [])
        section.addArrangedSubview(DemoPlayground.caption(DemoStrings.string(.underline)))
        section.addFullWidth(TSTabsView(items: TabsSample.sections))
        section.addArrangedSubview(DemoPlayground.caption(DemoStrings.string(.segmented)))
        section.addArrangedSubview(TSTabsView(
            items: Array(TabsSample.ranges.prefix(3)),
            selectedID: TabsSample.ranges[1].id,
            variant: .segmented
        ))
        return section
    }

    private func blockSection() -> UIView {
        let section = DemoComponentSectionView(.block, views: [])
        section.addFullWidth(TSTabsView(items: TabsSample.ranges, variant: .segmented, isBlock: true))
        section.addArrangedSubview(TSTabsView(items: TabsSample.layouts, variant: .segmented))
        return section
    }

    private func statesSection() -> UIView {
        let section = DemoComponentSectionView(.states, views: [])
        for variant in TSTabsVariant.allCases {
            let tabs = TSTabsView(items: Array(TabsSample.ranges.prefix(3)), selectedID: TabsSample.ranges[1].id, variant: variant, isBlock: true)
            tabs.isEnabled = false
            section.addArrangedSubview(DemoPlayground.caption(TabsSample.caption(variant)))
            section.addFullWidth(tabs)
        }
        return section
    }

    private func overflowSection() -> UIView {
        let tabs = TSTabsView(items: TabsSample.manySections)
        tabs.accessibilityIdentifier = "demo.tabs.overflow"
        tabs.addAction(UIAction { [weak self, unowned tabs] _ in
            self?.overflowCaption.string = TabsSample.label(for: tabs.selectedID)
        }, for: .valueChanged)
        let section = DemoComponentSectionView(.overflow, views: [])
        section.addFullWidth(tabs)
        section.addArrangedSubview(overflowCaption)
        return section
    }

    private func playgroundSection() -> UIView {
        let tabs = playgroundTabs
        let variants = TSTabsVariant.allCases
        let variantPicker = DemoPlayground.segmented(variants.map(\.rawValue), identifier: "demo.tabs.playground.variant") {
            tabs.variant = variants[$0]
        }
        variantPicker.selectedSegmentIndex = variants.firstIndex(of: .segmented) ?? 0
        let section = DemoComponentSectionView(.playground, views: [])
        [
            variantPicker,
            DemoPlayground.toggle(.stateDisabled, identifier: "demo.tabs.playground.disabled") { tabs.isEnabled = !$0 },
            blockToggle(tabs),
            tabs
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(valueCaption)
        section.addArrangedSubview(counter)
        return section
    }

    private func blockToggle(_ tabs: TSTabsView) -> UIView {
        let row = DemoPlayground.toggle(.block, identifier: "demo.tabs.playground.block") { tabs.isBlock = $0 }
        let toggle = row.subviews.compactMap { $0 as? UISwitch }.first
        toggle?.isOn = true
        return row
    }

    private func tabsDidChange() {
        changeCount += 1
        counter.string = DemoStrings.tapCount(changeCount)
        valueCaption.string = TabsSample.label(for: playgroundTabs.selectedID)
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
