import Foundation

public enum TSBadgeCountFormatter {
    public static let defaultMaximum = 99

    public static func string(for count: Int, maximum: Int = defaultMaximum, locale: Locale = .current) -> String? {
        guard count > 0 else { return nil }
        guard count > maximum else { return String(count) }
        return TSStrings.countOverflow(maximum, locale: locale)
    }
}
