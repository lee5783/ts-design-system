import UIKit
@testable import TSDesignSystemUIKit

final class BottomNavGalleryUIView: GalleryStackView {
    private static let home = TSBottomNavItem(id: "home", label: "Home", icon: .home)
    private static let inbox = TSBottomNavItem(id: "inbox", label: "Inbox", icon: .mail, badge: 3)
    private static let search = TSBottomNavItem(id: "search", label: "Search", icon: .search)
    private static let alerts = TSBottomNavItem(id: "alerts", label: "Alerts", icon: .bell, badge: 120)
    private static let profile = TSBottomNavItem(id: "profile", label: "Profile", icon: .user)

    private static let three = [home, search, profile]
    private static let four = [home, inbox, search, alerts]
    private static let five = [home, inbox, search, alerts, profile]

    override init(frame: CGRect) {
        super.init(frame: frame)
        stack.alignment = .fill
        stack.directionalLayoutMargins.leading = 0
        stack.directionalLayoutMargins.trailing = 0
        stack.addArrangedSubview(TSBottomNavView(items: Self.three))
        stack.addArrangedSubview(TSBottomNavView(items: Self.four, selectedID: "inbox"))
        let pressed = TSBottomNavView(items: Self.five)
        pressed.itemViews[2].isHighlighted = true
        stack.addArrangedSubview(pressed)
        stack.addArrangedSubview(TSBottomNavView(items: Self.five, selectedID: "alerts"))
        let disabled = TSBottomNavView(items: Self.four)
        disabled.isEnabled = false
        stack.addArrangedSubview(disabled)
    }
}
