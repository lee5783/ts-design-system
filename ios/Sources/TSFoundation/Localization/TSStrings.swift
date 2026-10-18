import Foundation

public enum TSStringKey: String, CaseIterable, Sendable {
    case loading = "ts.loading"
    case close = "ts.close"
    case dismiss = "ts.dismiss"
    case position = "ts.position"
    case countOverflow = "ts.count.overflow"
    case toggleOn = "ts.toggle.on"
    case toggleOff = "ts.toggle.off"
    case checkboxChecked = "ts.checkbox.checked"
    case checkboxUnchecked = "ts.checkbox.unchecked"
    case textFieldError = "ts.text-field.error"
    case showPassword = "ts.text-field.show-password"
    case hidePassword = "ts.text-field.hide-password"
    case tabPosition = "ts.tab.position"
    case bottomNavBadge = "ts.bottom-nav.badge"
    case listItemNavigates = "ts.list-item.navigates"
    case listItemExpanded = "ts.list-item.expanded"
    case listItemCollapsed = "ts.list-item.collapsed"
    case alertInfo = "ts.alert.info"
    case alertSuccess = "ts.alert.success"
    case alertWarning = "ts.alert.warning"
    case alertDanger = "ts.alert.danger"
}

public enum TSStrings {
    public static let overrideTable = "TSDesignSystem"

    public static var loading: String { string(.loading) }
    public static var close: String { string(.close) }
    public static var dismiss: String { string(.dismiss) }
    public static var toggleOn: String { string(.toggleOn) }
    public static var toggleOff: String { string(.toggleOff) }
    public static var checkboxChecked: String { string(.checkboxChecked) }
    public static var checkboxUnchecked: String { string(.checkboxUnchecked) }
    public static var showPassword: String { string(.showPassword) }
    public static var hidePassword: String { string(.hidePassword) }
    public static var listItemNavigates: String { string(.listItemNavigates) }
    public static var listItemExpanded: String { string(.listItemExpanded) }
    public static var listItemCollapsed: String { string(.listItemCollapsed) }

    public static func alertTone(_ tone: TSAlertTone) -> String {
        switch tone {
        case .info: string(.alertInfo)
        case .success: string(.alertSuccess)
        case .warning: string(.alertWarning)
        case .danger: string(.alertDanger)
        }
    }

    public static func toastTone(_ tone: TSToastTone) -> String? {
        switch tone {
        case .neutral: nil
        case .info: string(.alertInfo)
        case .success: string(.alertSuccess)
        case .warning: string(.alertWarning)
        case .danger: string(.alertDanger)
        }
    }

    public static func position(_ index: Int, of count: Int, locale: Locale = .current) -> String {
        String(format: string(.position), locale: locale, index, count)
    }

    public static func tabPosition(_ index: Int, of count: Int, locale: Locale = .current) -> String {
        String(format: string(.tabPosition), locale: locale, index, count)
    }

    public static func bottomNavBadge(_ count: String, locale: Locale = .current) -> String {
        String(format: string(.bottomNavBadge), locale: locale, count)
    }

    public static func countOverflow(_ maximum: Int, locale: Locale = .current) -> String {
        String(format: string(.countOverflow), locale: locale, maximum)
    }

    public static func textFieldError(_ message: String, locale: Locale = .current) -> String {
        String(format: string(.textFieldError), locale: locale, message)
    }

    public static func string(_ key: TSStringKey, overrideBundle: Bundle = .main) -> String {
        let override = String(localized: String.LocalizationValue(key.rawValue), table: overrideTable, bundle: overrideBundle)
        if override != key.rawValue {
            return override
        }
        return String(localized: String.LocalizationValue(key.rawValue), bundle: TSFoundationResources.bundle)
    }
}
