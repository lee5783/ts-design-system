import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSTabs behaviour")
struct TSTabsBehaviorTests {
    private let items = [
        TSTabItem(id: "day", label: "Day"),
        TSTabItem(id: "week", label: "Week"),
        TSTabItem(id: "month", label: "Month")
    ]

    private let manyItems = [
        "Overview", "Activity", "Members", "Billing", "Settings", "Integrations", "Security", "Notifications"
    ].map { TSTabItem(id: $0.lowercased(), label: $0) }

    @Test("Tapping a tab moves the selection binding; tapping the selected tab is a no-op", arguments: TSTabsVariant.allCases)
    func tapSelects(variant: TSTabsVariant) throws {
        let box = SelectionBox("day")
        var haptics: [TSHapticEvent] = []
        let sut = TSTabs(items: items, selection: box.binding, variant: variant).playingHaptics { haptics.append($0) }
        try sut.inspect().findAll(ViewType.Button.self)[1].tap()
        #expect(box.value == "week")
        #expect(haptics == (variant == .segmented ? [.selection] : []))
        try TSTabs(items: items, selection: box.binding, variant: variant).playingHaptics { haptics.append($0) }
            .inspect().findAll(ViewType.Button.self)[1].tap()
        #expect(box.value == "week")
        #expect(haptics.count == (variant == .segmented ? 1 : 0))
    }

    @Test("A disabled tab list does not change the selection", arguments: TSTabsVariant.allCases)
    func disabledBlocks(variant: TSTabsVariant) throws {
        let box = SelectionBox("day")
        let sut = TSTabs(items: items, selection: box.binding, variant: variant).disabled(true)
        #expect(throws: (any Error).self) {
            try sut.inspect().findAll(ViewType.Button.self)[2].tap()
        }
        #expect(box.value == "day")
    }

    @Test("The selection indicator is decorative", arguments: TSTabsVariant.allCases)
    func selectionIndicator(variant: TSTabsVariant) throws {
        let sut = TSTabs(items: items, selection: .constant("week"), variant: variant)
        let buttons = try sut.inspect().findAll(ViewType.Button.self)
        #expect(buttons.count == 3)
        let style = try #require(try buttons[1].buttonStyle() as? TSTabStyle)
        let selection = try style.inspect(isPressed: false).find(TSTabSelection.self)
        #expect(try selection.actualView().body.inspect().accessibilityHidden())
    }

    @Test("Underline tabs scroll horizontally when they overflow instead of squeezing")
    func underlineOverflowScrolls() throws {
        let sut = TSTabs(items: manyItems, selection: .constant("overview"))
        let scrollView = try sut.inspect().find(ViewType.ScrollView.self)
        #expect(try scrollView.axes() == .horizontal)
        let host = UIHostingController(rootView: sut)
        let ideal = host.sizeThatFits(in: CGSize(width: CGFloat.greatestFiniteMagnitude, height: CGFloat.greatestFiniteMagnitude))
        let constrained = host.sizeThatFits(in: CGSize(width: 320, height: CGFloat.greatestFiniteMagnitude))
        #expect(ideal.width > 320)
        #expect(constrained.width <= 320)
        #expect(constrained.height >= 44)
    }

    @Test("Segmented tabs never scroll")
    func segmentedDoesNotScroll() throws {
        let sut = TSTabs(items: items, selection: .constant("day"), variant: .segmented, isBlock: true)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.ScrollView.self)
        }
    }

    @Test("The segmented track is 46 pt high and each segment's hit area reaches 44 pt")
    func segmentedHitArea() throws {
        let sut = TSTabs(items: items, selection: .constant("day"), variant: .segmented, isBlock: true)
        let host = UIHostingController(rootView: sut)
        let size = host.sizeThatFits(in: CGSize(width: 320, height: CGFloat.greatestFiniteMagnitude))
        #expect(abs(size.height - 46) < 0.5)
        #expect(abs(size.width - 320) < 0.5)
        let style = try #require(try sut.inspect().findAll(ViewType.Button.self)[0].buttonStyle() as? TSTabStyle)
        let hitArea = try style.inspect(isPressed: false)
            .find(where: { (try? $0.modifier(TSMinimumHitAreaModifier.self)) != nil })
            .modifier(TSMinimumHitAreaModifier.self)
            .actualView()
        #expect(hitArea.edges == .vertical)
        #expect(38 + hitArea.outset * 2 >= 44)
    }

    @Test("Underline tabs are at least 44 pt high")
    func underlineHeight() {
        let host = UIHostingController(rootView: TSTabs(items: items, selection: .constant("day")))
        let size = host.sizeThatFits(in: CGSize(width: 320, height: CGFloat.greatestFiniteMagnitude))
        #expect(size.height >= 44)
    }
}
