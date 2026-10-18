import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSBottomNav behaviour")
struct TSBottomNavBehaviorTests {
    private let items = [
        TSBottomNavItem(id: "home", label: "Home", icon: .home),
        TSBottomNavItem(id: "inbox", label: "Inbox", icon: .mail, badge: 3),
        TSBottomNavItem(id: "search", label: "Search", icon: .search),
        TSBottomNavItem(id: "profile", label: "Profile", icon: .user, badge: 120)
    ]

    @Test("Tapping an item moves the selection binding; tapping the selected item is a no-op")
    func tapSelects() throws {
        let box = SelectionBox("home")
        try TSBottomNav(items: items, selection: box.binding).inspect().findAll(ViewType.Button.self)[2].tap()
        #expect(box.value == "search")
        var writes = 0
        let counted = Binding(get: { box.value }, set: { box.value = $0; writes += 1 })
        try TSBottomNav(items: items, selection: counted).inspect().findAll(ViewType.Button.self)[2].tap()
        #expect(box.value == "search")
        #expect(writes == 0)
    }

    @Test("A disabled bar does not change the selection")
    func disabledBlocks() throws {
        let box = SelectionBox("home")
        let sut = TSBottomNav(items: items, selection: box.binding).disabled(true)
        #expect(throws: (any Error).self) {
            try sut.inspect().findAll(ViewType.Button.self)[1].tap()
        }
        #expect(box.value == "home")
    }

    @Test("The badge shows the formatted count and is hidden from VoiceOver; no badge without a count")
    func badge() throws {
        let sut = TSBottomNav(items: items, selection: .constant("home"))
        let buttons = try sut.inspect().findAll(ViewType.Button.self)
        let inboxStyle = try #require(try buttons[1].buttonStyle() as? TSBottomNavItemStyle)
        let inboxBadge = try inboxStyle.inspect(isPressed: false).find(TSBottomNavBadge.self)
        #expect(try inboxBadge.actualView().text == "3")
        #expect(try inboxBadge.actualView().body.inspect().text().accessibilityHidden())
        let profileStyle = try #require(try buttons[3].buttonStyle() as? TSBottomNavItemStyle)
        #expect(try profileStyle.inspect(isPressed: false).find(TSBottomNavBadge.self).actualView().text == "99+")
        let homeStyle = try #require(try buttons[0].buttonStyle() as? TSBottomNavItemStyle)
        #expect(throws: (any Error).self) {
            try homeStyle.inspect(isPressed: false).find(TSBottomNavBadge.self)
        }
    }

    @Test("Items share the width equally and the bar is 64 pt high")
    func equalWidths() throws {
        let sut = TSBottomNav(items: items, selection: .constant("home"))
        for button in try sut.inspect().findAll(ViewType.Button.self) {
            #expect(try button.flexFrame().maxWidth == .infinity)
        }
        let host = UIHostingController(rootView: sut)
        let size = host.sizeThatFits(in: CGSize(width: 390, height: CGFloat.greatestFiniteMagnitude))
        #expect(abs(size.height - 64) < 0.5)
        #expect(abs(size.width - 390) < 0.5)
    }

    @Test("The bar height stays 64 at AX5 because labels are clamped")
    func clampedAtAccessibilitySizes() {
        let sut = TSBottomNav(items: items, selection: .constant("home"))
            .environment(\.dynamicTypeSize, .accessibility5)
        let host = UIHostingController(rootView: sut)
        let size = host.sizeThatFits(in: CGSize(width: 390, height: CGFloat.greatestFiniteMagnitude))
        #expect(abs(size.height - 64) < 0.5)
    }

    @Test("The shell has no system TabView and shows only the selected destination")
    func shell() throws {
        let box = SelectionBox("inbox")
        let sut = TSBottomNavShell(items: items, selection: box.binding) { item in
            Text(item.label)
        }
        let inspected = try sut.inspect()
        #expect(throws: (any Error).self) {
            try inspected.find(ViewType.TabView.self)
        }
        #expect(try inspected.find(TSBottomNav.self).actualView().items == items)
        let pages = try inspected.zStack().forEach(0)
        #expect(pages.count == 4)
        for (index, page) in pages.enumerated() {
            let isSelected = index == 1
            #expect(try page.text().opacity() == (isSelected ? 1 : 0))
            #expect(try page.text().allowsHitTesting() == isSelected)
            #expect(try page.text().accessibilityHidden() == !isSelected)
        }
        try inspected.find(TSBottomNav.self).findAll(ViewType.Button.self)[3].tap()
        #expect(box.value == "profile")
    }
}
