import Foundation

public enum TSBottomNavAccessibility {
    public static func value(index: Int, count: Int, badge: Int?, locale: Locale = .current) -> String {
        let position = TSStrings.tabPosition(index + 1, of: count, locale: locale)
        guard let badge, let text = TSBadgeCountFormatter.string(for: badge, locale: locale) else { return position }
        return "\(position), \(TSStrings.bottomNavBadge(text, locale: locale))"
    }
}
