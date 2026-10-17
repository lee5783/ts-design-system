import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSBottomNavView behaviour")
struct TSBottomNavViewBehaviorTests {
    private let items = [
        TSBottomNavItem(id: "home", label: "Home", icon: .home),
        TSBottomNavItem(id: "inbox", label: "Inbox", icon: .mail, badge: 3),
        TSBottomNavItem(id: "search", label: "Search", icon: .search),
        TSBottomNavItem(id: "profile", label: "Profile", icon: .user, badge: 120)
    ]

    private func host(_ nav: TSBottomNavView, sizeCategory: UIContentSizeCategory = .large, bottomInset: CGFloat = 0) -> TraitHost {
        let host = TraitHost(sizeCategory: sizeCategory)
        host.child.additionalSafeAreaInsets.bottom = bottomInset
        nav.translatesAutoresizingMaskIntoConstraints = false
        host.content.addSubview(nav)
        NSLayoutConstraint.activate([
            nav.leadingAnchor.constraint(equalTo: host.content.leadingAnchor),
            nav.trailingAnchor.constraint(equalTo: host.content.trailingAnchor),
            nav.bottomAnchor.constraint(equalTo: host.content.bottomAnchor)
        ])
        host.window.layoutIfNeeded()
        return host
    }

    @Test("A tap selects the item and fires .valueChanged once; the selected item is a no-op")
    func tapSelects() {
        let counter = TapCounter()
        let nav = TSBottomNavView(items: items)
        nav.reduceMotion = { true }
        nav.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        #expect(nav.selectedID == "home")
        nav.itemViews[2].sendActions(for: .touchUpInside)
        #expect(nav.selectedID == "search")
        #expect(nav.itemViews.map(\.isSelected) == [false, false, true, false])
        #expect(counter.count == 1)
        nav.itemViews[2].sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
    }

    @Test("Setting selectedID programmatically is silent")
    func programmaticSelectionIsSilent() {
        let counter = TapCounter()
        let nav = TSBottomNavView(items: items)
        nav.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        nav.selectedID = "profile"
        #expect(counter.count == 0)
        #expect(nav.selectedItemView?.item.id == "profile")
        #expect(nav.itemViews[3].accessibilityTraits.contains(.selected))
    }

    @Test("isEnabled = false blocks selection, adds the not-enabled trait and dims the items")
    func disabledBlocks() {
        let counter = TapCounter()
        let nav = TSBottomNavView(items: items)
        nav.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        nav.isEnabled = false
        nav.itemViews[1].sendActions(for: .touchUpInside)
        #expect(!nav.itemViews[1].accessibilityActivate())
        #expect(nav.selectedID == "home")
        #expect(counter.count == 0)
        #expect(nav.itemViews.allSatisfy { $0.accessibilityTraits.contains(.notEnabled) })
        #expect(nav.itemViews.allSatisfy { abs($0.alpha - 0.45) < 0.001 })
    }

    @Test("VoiceOver activation selects like a tap")
    func accessibilityActivate() {
        let nav = TSBottomNavView(items: items)
        nav.reduceMotion = { true }
        #expect(nav.itemViews[3].accessibilityActivate())
        #expect(nav.selectedID == "profile")
    }

    @Test("Each item is one element: label, \"Tab, n of N\" plus the badge, the selected trait only when selected, no button trait")
    func accessibility() {
        let nav = TSBottomNavView(items: items, selectedID: "inbox", label: "Main")
        #expect(!nav.isAccessibilityElement)
        #expect(nav.accessibilityLabel == "Main")
        #expect(nav.accessibilityContainerType == .semanticGroup)
        let values = ["Tab, 1 of 4", "Tab, 2 of 4, 3 new", "Tab, 3 of 4", "Tab, 4 of 4, 99+ new"]
        for (index, itemView) in nav.itemViews.enumerated() {
            #expect(itemView.isAccessibilityElement)
            #expect(itemView.accessibilityLabel == items[index].label)
            #expect(itemView.accessibilityValue == values[index])
            #expect(itemView.accessibilityTraits.contains(.selected) == (index == 1))
            #expect(!itemView.accessibilityTraits.contains(.button))
            #expect(!itemView.labelView.isAccessibilityElement)
            #expect(!itemView.badgeLabel.isAccessibilityElement)
        }
    }

    @Test("Badges show the formatted count and hide without one; updating a badge keeps the item views")
    func badges() {
        let nav = TSBottomNavView(items: items)
        let views = nav.itemViews
        #expect(nav.itemViews.map(\.badgeView.isHidden) == [true, false, true, false])
        #expect(nav.itemViews[1].badgeLabel.text == "3")
        #expect(nav.itemViews[3].badgeLabel.text == "99+")
        nav.items = items.map { $0.id == "inbox" ? $0.badged(nil) : $0 }
        #expect(nav.itemViews.count == views.count)
        #expect(zip(nav.itemViews, views).allSatisfy { $0 === $1 })
        #expect(nav.itemViews[1].badgeView.isHidden)
        #expect(nav.itemViews[1].accessibilityValue == "Tab, 2 of 4")
    }

    @Test("Replacing the items rebuilds them and keeps a valid selection")
    func replacingItems() {
        let nav = TSBottomNavView(items: items, selectedID: "profile")
        nav.items = Array(items.prefix(3))
        #expect(nav.itemViews.count == 3)
        #expect(nav.selectedID == "home")
        #expect(nav.itemViews[2].accessibilityValue == "Tab, 3 of 3")
    }

    @Test("Five items share the width equally; the bar is 64 plus the bottom safe area")
    func layoutAndSafeArea() {
        let five = items + [TSBottomNavItem(id: "settings", label: "Settings", icon: .settings)]
        let nav = TSBottomNavView(items: five)
        let host = host(nav, bottomInset: 34)
        let bottomInset = host.window.safeAreaInsets.bottom + 34
        #expect(abs(nav.safeAreaInsets.bottom - bottomInset) < 0.5)
        #expect(abs(nav.bounds.width - 390) < 0.5)
        #expect(abs(nav.bounds.height - (64 + bottomInset)) < 0.5)
        #expect(abs(nav.stackView.frame.height - 64) < 0.5)
        #expect(nav.stackView.frame.minY == 0)
        let widths = nav.itemViews.map(\.bounds.width)
        #expect(widths.allSatisfy { abs($0 - 78) < 0.5 })
        let pill = nav.itemViews[0].pillView.frame
        #expect(pill.size == CGSize(width: 56, height: 28))
        #expect(abs(nav.hairlineView.frame.height - 1) < 0.001)
    }

    @Test("The badge sits 6 pt below the item top and starts 6 pt past the centre, mirrored in RTL")
    func badgePosition() {
        let nav = TSBottomNavView(items: items)
        let host = host(nav)
        let inbox = nav.itemViews[1]
        inbox.layoutIfNeeded()
        #expect(abs(inbox.badgeView.frame.minY - 6) < 0.5)
        #expect(abs(inbox.badgeView.frame.minX - (inbox.bounds.midX + 6)) < 0.5)
        #expect(inbox.badgeView.frame.height == 16)
        nav.semanticContentAttribute = .forceRightToLeft
        nav.itemViews.forEach { $0.semanticContentAttribute = .forceRightToLeft }
        inbox.setNeedsLayout()
        host.window.layoutIfNeeded()
        inbox.layoutIfNeeded()
        #expect(abs(inbox.badgeView.frame.maxX - (inbox.bounds.midX - 6)) < 0.5)
    }

    @Test("Labels are clamped at AX5, the bar stays 64 and the Large Content Viewer is offered")
    func clampedLabelsAndLargeContent() {
        let nav = TSBottomNavView(items: items)
        let host = host(nav, sizeCategory: .accessibilityExtraExtraExtraLarge)
        let font = nav.itemViews[0].labelView.attributedText?.attribute(.font, at: 0, effectiveRange: nil) as? UIFont
        let expected = TSFont.caption.weighted(500).uiFont(for: .extraExtraExtraLarge)
        #expect(font?.pointSize == expected.pointSize)
        #expect(abs(nav.bounds.height - (64 + host.window.safeAreaInsets.bottom)) < 0.5)
        #expect(abs(nav.stackView.frame.height - 64) < 0.5)
        #expect(nav.interactions.contains { $0 is UILargeContentViewerInteraction })
        for (index, itemView) in nav.itemViews.enumerated() {
            #expect(itemView.showsLargeContentViewer)
            #expect(itemView.largeContentTitle == items[index].label)
            #expect(itemView.largeContentImage != nil)
        }
    }
}

@MainActor
@Suite("TSTabBarController behaviour")
struct TSTabBarControllerBehaviorTests {
    private let items = [
        TSBottomNavItem(id: "home", label: "Home", icon: .home),
        TSBottomNavItem(id: "inbox", label: "Inbox", icon: .mail, badge: 3),
        TSBottomNavItem(id: "profile", label: "Profile", icon: .user)
    ]

    private func makeController() -> (TSTabBarController, UIWindow) {
        let controller = TSTabBarController(
            items: items,
            viewControllers: items.map { _ in UIViewController() },
            label: "Main"
        )
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        window.rootViewController = controller
        window.isHidden = false
        window.layoutIfNeeded()
        return (controller, window)
    }

    private func visibleTabBars(in view: UIView) -> [UITabBar] {
        var bars: [UITabBar] = []
        var queue = [view]
        while let next = queue.popLast() {
            guard !next.isHidden, next.alpha > 0.01 else { continue }
            if let bar = next as? UITabBar {
                bars.append(bar)
            }
            queue.append(contentsOf: next.subviews)
        }
        return bars
    }

    @Test("The system tab bar is hidden and out of VoiceOver; the TS bottom nav is on top")
    func systemTabBarIsHidden() {
        let (controller, window) = makeController()
        _ = window
        #expect(controller.tabBar.isHidden)
        #expect(controller.tabBar.accessibilityElementsHidden)
        if #available(iOS 18, *) {
            #expect(controller.isTabBarHidden)
        }
        #expect(visibleTabBars(in: window).isEmpty)
        #expect(controller.bottomNav.superview === controller.view)
        #expect(controller.view.subviews.last === controller.bottomNav)
        #expect(controller.bottomNav.accessibilityLabel == "Main")
    }

    @Test("The system tab bar stays hidden after the selection changes and after a relayout")
    func staysHidden() {
        let (controller, window) = makeController()
        controller.selectedIndex = 2
        controller.view.setNeedsLayout()
        window.layoutIfNeeded()
        #expect(controller.tabBar.isHidden)
        #expect(visibleTabBars(in: window).isEmpty)
    }

    @Test("Children get a 64 pt bottom safe-area inset for the bottom nav")
    func childInsets() {
        let (controller, window) = makeController()
        #expect(controller.viewControllers?.allSatisfy { $0.additionalSafeAreaInsets.bottom == 64 } == true)
        #expect(abs(controller.bottomNav.frame.height - (64 + window.safeAreaInsets.bottom)) < 0.5)
        #expect(abs(controller.bottomNav.stackView.frame.height - 64) < 0.5)
        #expect(abs(controller.bottomNav.frame.maxY - 844) < 0.5)
    }

    @Test("Selecting in the bottom nav switches the child and calls the delegate; programmatic selection syncs the nav")
    func selection() {
        let (controller, window) = makeController()
        _ = window
        let delegate = TabBarDelegateSpy()
        controller.delegate = delegate
        controller.bottomNav.reduceMotion = { true }
        controller.bottomNav.itemViews[1].sendActions(for: .touchUpInside)
        #expect(controller.selectedIndex == 1)
        #expect(delegate.selected == [1])
        controller.selectedIndex = 2
        #expect(controller.bottomNav.selectedID == "profile")
        controller.selectedViewController = controller.viewControllers?[0]
        #expect(controller.bottomNav.selectedID == "home")
    }

    @Test("A delegate that refuses the selection keeps the current tab and resets the nav")
    func delegateRefuses() {
        let (controller, window) = makeController()
        _ = window
        let delegate = TabBarDelegateSpy()
        delegate.allows = false
        controller.delegate = delegate
        controller.bottomNav.reduceMotion = { true }
        controller.bottomNav.itemViews[2].sendActions(for: .touchUpInside)
        #expect(controller.selectedIndex == 0)
        #expect(controller.bottomNav.selectedID == "home")
        #expect(delegate.selected.isEmpty)
    }

    @Test("Updating items updates the badge in place")
    func badgeUpdate() {
        let (controller, window) = makeController()
        _ = window
        controller.items = items.map { $0.id == "profile" ? $0.badged(7) : $0 }
        #expect(controller.bottomNav.itemViews[2].badgeLabel.text == "7")
        #expect(!controller.bottomNav.itemViews[2].badgeView.isHidden)
    }
}

@MainActor
final class TabBarDelegateSpy: NSObject, UITabBarControllerDelegate {
    var allows = true
    private(set) var selected: [Int] = []

    func tabBarController(_ tabBarController: UITabBarController, shouldSelect viewController: UIViewController) -> Bool {
        allows
    }

    func tabBarController(_ tabBarController: UITabBarController, didSelect viewController: UIViewController) {
        selected.append(tabBarController.viewControllers?.firstIndex(of: viewController) ?? -1)
    }
}
