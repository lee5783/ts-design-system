import SwiftUI
@testable import TSDesignSystem

enum BottomNavSample {
    static let home = TSBottomNavItem(id: "home", label: "Home", icon: .home)
    static let inbox = TSBottomNavItem(id: "inbox", label: "Inbox", icon: .mail, badge: 3)
    static let search = TSBottomNavItem(id: "search", label: "Search", icon: .search)
    static let alerts = TSBottomNavItem(id: "alerts", label: "Alerts", icon: .bell, badge: 120)
    static let profile = TSBottomNavItem(id: "profile", label: "Profile", icon: .user)

    static let three = [home, search, profile]
    static let four = [home, inbox, search, alerts]
    static let five = [home, inbox, search, alerts, profile]
}

struct BottomNavGalleryView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            nav(BottomNavSample.three)
            nav(BottomNavSample.four, selection: "inbox")
            nav(BottomNavSample.five).forcingState(.pressed, for: "search")
            nav(BottomNavSample.five, selection: "alerts").forcingState(.focused, for: "home")
            nav(BottomNavSample.four).disabled(true)
        }
        .padding(.vertical, TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func nav(_ items: [TSBottomNavItem], selection: String? = nil) -> TSBottomNav {
        TSBottomNav(items: items, selection: .constant(selection ?? items[0].id))
    }
}
