import CoreGraphics
import TSTokens
import UIKit

public struct TSBottomNavItem: Identifiable, Hashable, Sendable {
    public let id: String
    public let label: String
    public let icon: TSIconName
    public let badge: Int?

    public init(id: String, label: String, icon: TSIconName, badge: Int? = nil) {
        self.id = id
        self.label = label
        self.icon = icon
        self.badge = badge
    }

    public var badgeText: String? {
        badge.flatMap { TSBadgeCountFormatter.string(for: $0) }
    }

    public func badged(_ badge: Int?) -> TSBottomNavItem {
        TSBottomNavItem(id: id, label: label, icon: icon, badge: badge)
    }
}

public enum TSBottomNavStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let isSelected: Bool
        public let state: TSControlState

        public init(isSelected: Bool = false, state: TSControlState = .normal) {
            self.isSelected = isSelected
            self.state = state
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let labelFont: TSFont
        public let foreground: TSColor
        public let pillFill: TSColor?
        public let pillWidth: CGFloat
        public let pillHeight: CGFloat
        public let pillCornerRadius: CGFloat
        public let iconSize: TSIconSize
        public let spacing: CGFloat
        public let opacity: Double
        public let showsFocusRing: Bool
    }

    public struct Container: Equatable, Sendable {
        public let height: CGFloat
        public let fill: TSColor
        public let border: TSColor
        public let borderWidth: CGFloat
    }

    public struct Badge: Equatable, Sendable {
        public let fill: TSColor
        public let foreground: TSColor
        public let fontSize: CGFloat
        public let fontWeight: Int
        public let minWidth: CGFloat
        public let height: CGFloat
        public let horizontalPadding: CGFloat
        public let cornerRadius: CGFloat
        public let offsetTop: CGFloat
        public let offsetStart: CGFloat
        public let topFromPill: CGFloat
    }

    public static let itemCountRange = 3...5
    public static let labelMaximumSizeCategory: UIContentSizeCategory = .extraExtraExtraLarge

    public static let states: [TSControlState] = [.normal, .pressed, .disabled, .focused]

    public static let allConfigurations: [Configuration] = [false, true].flatMap { isSelected in
        states.map { Configuration(isSelected: isSelected, state: $0) }
    }

    public static let container = Container(
        height: TSSize.tabBar,
        fill: .surfaceCard,
        border: .borderSubtle,
        borderWidth: 1
    )

    public static let badge = Badge(
        fill: .dangerSolid,
        foreground: .textOnAccent,
        fontSize: TSComponentTokens.bottomNavBadgeFontSize,
        fontWeight: TSComponentTokens.bottomNavBadgeFontWeight,
        minWidth: TSComponentTokens.bottomNavBadgeSize,
        height: TSComponentTokens.bottomNavBadgeSize,
        horizontalPadding: TSSpacing.space1,
        cornerRadius: TSComponentTokens.bottomNavBadgeSize / 2,
        offsetTop: TSComponentTokens.bottomNavBadgeOffsetTop,
        offsetStart: TSComponentTokens.bottomNavBadgeOffsetStart,
        topFromPill: TSComponentTokens.bottomNavBadgeOffsetTop - defaultContentInset
    )

    static var defaultContentInset: CGFloat {
        let label = TSFont.caption.token.lineHeight
        return (TSSize.tabBar - TSComponentTokens.bottomNavPillHeight - TSSpacing.space0_5 - label) / 2
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let state = configuration.state
        let isSelected = configuration.isSelected
        let isPressed = state.contains(.pressed) && state.isInteractive
        return Resolved(
            labelFont: TSFont.caption.weighted(TSComponentTokens.bottomNavLabelFontWeight),
            foreground: isSelected ? .accentText : .textMuted,
            pillFill: isSelected ? .accentSubtle : (isPressed ? .surfacePressed : nil),
            pillWidth: TSComponentTokens.bottomNavPillWidth,
            pillHeight: TSComponentTokens.bottomNavPillHeight,
            pillCornerRadius: TSComponentTokens.bottomNavPillHeight / 2,
            iconSize: .lg,
            spacing: TSSpacing.space0_5,
            opacity: state.contains(.disabled) ? TSOpacity.opacityDisabled : 1,
            showsFocusRing: state.contains(.focused) && !state.contains(.disabled)
        )
    }

    public static func labelSizeCategory(for sizeCategory: UIContentSizeCategory) -> UIContentSizeCategory {
        guard let index = sizeCategoryOrder.firstIndex(of: sizeCategory) else { return .large }
        let maximum = sizeCategoryOrder.firstIndex(of: labelMaximumSizeCategory) ?? index
        return sizeCategoryOrder[min(index, maximum)]
    }

    static let sizeCategoryOrder: [UIContentSizeCategory] = [
        .extraSmall,
        .small,
        .medium,
        .large,
        .extraLarge,
        .extraExtraLarge,
        .extraExtraExtraLarge,
        .accessibilityMedium,
        .accessibilityLarge,
        .accessibilityExtraLarge,
        .accessibilityExtraExtraLarge,
        .accessibilityExtraExtraExtraLarge,
    ]

    public static func badgeFont() -> UIFont {
        UIFont.systemFont(ofSize: badge.fontSize, weight: TSFontWeight.uiFontWeight(badge.fontWeight))
    }
}
