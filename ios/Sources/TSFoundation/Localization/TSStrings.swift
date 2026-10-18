import Foundation

public enum TSStringKey: String, CaseIterable, Sendable {
    case loading = "ts.loading"
    case close = "ts.close"
    case dismiss = "ts.dismiss"
    case countOverflow = "ts.count.overflow"
    case showPassword = "ts.text-field.show-password"
    case hidePassword = "ts.text-field.hide-password"
}

public enum TSStrings {
    public static let overrideTable = "TSDesignSystem"

    public static var loading: String { string(.loading) }
    public static var close: String { string(.close) }
    public static var dismiss: String { string(.dismiss) }
    public static var showPassword: String { string(.showPassword) }
    public static var hidePassword: String { string(.hidePassword) }

    public static func countOverflow(_ maximum: Int, locale: Locale = .current) -> String {
        String(format: string(.countOverflow), locale: locale, maximum)
    }

    public static func string(_ key: TSStringKey, overrideBundle: Bundle = .main) -> String {
        let override = String(localized: String.LocalizationValue(key.rawValue), table: overrideTable, bundle: overrideBundle)
        if override != key.rawValue {
            return override
        }
        return String(localized: String.LocalizationValue(key.rawValue), bundle: TSFoundationResources.bundle)
    }
}
