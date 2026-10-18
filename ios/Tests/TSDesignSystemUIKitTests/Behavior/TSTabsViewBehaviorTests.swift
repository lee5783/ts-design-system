import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSTabsView behaviour")
struct TSTabsViewBehaviorTests {
    private let items = [
        TSTabItem(id: "day", label: "Day"),
        TSTabItem(id: "week", label: "Week"),
        TSTabItem(id: "month", label: "Month")
    ]

    private let manyItems = [
        "Overview", "Activity", "Members", "Billing", "Settings", "Integrations", "Security", "Notifications"
    ].map { TSTabItem(id: $0.lowercased(), label: $0) }

    private func layout(_ view: TSTabsView, width: CGFloat = 320) -> TSTabsView {
        let size = view.systemLayoutSizeFitting(
            CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .required,
            verticalFittingPriority: .fittingSizeLevel
        )
        view.frame = CGRect(origin: .zero, size: size)
        view.layoutIfNeeded()
        return view
    }

    @Test("A tap selects the tab and fires .valueChanged once; the selected tab is a no-op", arguments: TSTabsVariant.allCases)
    func tapSelects(variant: TSTabsVariant) {
        let counter = TapCounter()
        var haptics: [TSHapticEvent] = []
        let tabs = TSTabsView(items: items, variant: variant)
        tabs.playHaptic = { haptics.append($0) }
        tabs.reduceMotion = { true }
        tabs.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        #expect(tabs.selectedID == "day")
        tabs.tabViews[1].sendActions(for: .touchUpInside)
        #expect(tabs.selectedID == "week")
        #expect(tabs.tabViews.map(\.isSelected) == [false, true, false])
        #expect(counter.count == 1)
        #expect(haptics == (variant == .segmented ? [.selection] : []))
        tabs.tabViews[1].sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
    }

    @Test("Setting selectedID programmatically is silent and updates the selected tab")
    func programmaticSelectionIsSilent() {
        let counter = TapCounter()
        let tabs = TSTabsView(items: items)
        tabs.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        tabs.selectedID = "month"
        #expect(counter.count == 0)
        #expect(tabs.selectedTab?.item.id == "month")
    }

    @Test("isEnabled = false blocks selection and dims once", arguments: TSTabsVariant.allCases)
    func disabledBlocks(variant: TSTabsVariant) {
        let counter = TapCounter()
        let tabs = TSTabsView(items: items, variant: variant)
        tabs.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        tabs.isEnabled = false
        tabs.tabViews[2].sendActions(for: .touchUpInside)
        #expect(tabs.selectedID == "day")
        #expect(counter.count == 0)
        #expect(tabs.tabViews.allSatisfy { abs($0.alpha - 0.45) < 0.001 })
        #expect(abs(tabs.trackView.alpha - 0.45) < 0.001)
    }

    @Test("Replacing items rebuilds the tabs and keeps a valid selection")
    func replacingItems() {
        let tabs = TSTabsView(items: items, selectedID: "month")
        tabs.items = Array(manyItems.prefix(2))
        #expect(tabs.tabViews.count == 2)
        #expect(tabs.selectedID == "overview")
    }

    @Test("Underline tabs scroll horizontally when they overflow, and selecting a far tab scrolls it into view")
    func underlineOverflowScrolls() {
        let tabs = layout(TSTabsView(items: manyItems))
        tabs.reduceMotion = { true }
        #expect(tabs.bounds.width == 320)
        #expect(tabs.scrollView.isScrollEnabled)
        #expect(tabs.scrollView.contentSize.width > tabs.scrollView.bounds.width)
        #expect(tabs.scrollView.contentOffset.x == 0)
        let last = tabs.tabViews[manyItems.count - 1]
        last.sendActions(for: .touchUpInside)
        tabs.layoutIfNeeded()
        #expect(tabs.selectedID == "notifications")
        let visible = CGRect(origin: tabs.scrollView.contentOffset, size: tabs.scrollView.bounds.size)
        #expect(visible.contains(last.convert(last.bounds, to: tabs.scrollView)))
        #expect(abs(tabs.indicatorView.frame.minX - last.convert(last.bounds, to: tabs.scrollView).minX) < 0.5)
    }

    @Test("Segmented tabs do not scroll; block segments share the width equally")
    func segmentedBlock() {
        let tabs = layout(TSTabsView(items: items, variant: .segmented, isBlock: true))
        #expect(!tabs.scrollView.isScrollEnabled)
        #expect(tabs.bounds.width == 320)
        let widths = tabs.tabViews.map(\.bounds.width)
        #expect(widths.allSatisfy { abs($0 - widths[0]) < 0.5 })
        #expect(tabs.thumbView.frame == tabs.tabViews[0].convert(tabs.tabViews[0].bounds, to: tabs.scrollView))
        #expect(!tabs.thumbView.isHidden)
        #expect(tabs.indicatorView.isHidden)
    }

    @Test("The segmented track is 46 pt high and each segment's hit area reaches 44 pt")
    func segmentedHitArea() {
        let tabs = layout(TSTabsView(items: items, variant: .segmented, isBlock: true))
        #expect(abs(tabs.bounds.height - 46) < 0.5)
        let segment = tabs.tabViews[1]
        #expect(abs(segment.bounds.height - 38) < 0.5)
        let hit = TSHitArea.expandedRect(segment.bounds)
        #expect(hit.height >= 44)
        #expect(segment.point(inside: CGPoint(x: segment.bounds.midX, y: -2.5), with: nil))
        let above = segment.convert(CGPoint(x: segment.bounds.midX, y: -2.5), to: tabs)
        #expect(tabs.hitTest(above, with: nil) === segment)
    }

    @Test("Underline tabs are at least 44 pt high and grow at AX5")
    func dynamicType() {
        let tabs = TSTabsView(items: items)
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        tabs.translatesAutoresizingMaskIntoConstraints = false
        host.content.addSubview(tabs)
        NSLayoutConstraint.activate([
            tabs.leadingAnchor.constraint(equalTo: host.content.leadingAnchor),
            tabs.trailingAnchor.constraint(equalTo: host.content.trailingAnchor),
            tabs.topAnchor.constraint(equalTo: host.content.topAnchor)
        ])
        host.window.layoutIfNeeded()
        #expect(tabs.bounds.height > 44)
        #expect(layout(TSTabsView(items: items)).bounds.height >= 44)
    }
}
