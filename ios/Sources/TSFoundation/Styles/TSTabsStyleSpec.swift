import CoreGraphics
import TSTokens

public enum TSTabsVariant: String, CaseIterable, Hashable, Sendable {
    case underline
    case segmented
}

public struct TSTabItem: Identifiable, Hashable, Sendable {
    public let id: String
    public let label: String

    public init(id: String, label: String) {
        self.id = id
        self.label = label
    }
}

public enum TSTabsStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let variant: TSTabsVariant
        public let isSelected: Bool
        public let state: TSControlState

        public init(variant: TSTabsVariant = .underline, isSelected: Bool = false, state: TSControlState = .normal) {
            self.variant = variant
            self.isSelected = isSelected
            self.state = state
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let labelFont: TSFont
        public let labelColor: TSColor
        public let fill: TSColor?
        public let shadow: TSShadowToken?
        public let indicatorColor: TSColor?
        public let indicatorHeight: CGFloat
        public let cornerRadius: CGFloat
        public let paddingVertical: CGFloat
        public let paddingHorizontal: CGFloat
        public let minHeight: CGFloat
        public let opacity: Double
        public let showsFocusRing: Bool
    }

    public struct Container: Equatable, Sendable {
        public let fill: TSColor?
        public let border: TSColor
        public let borderWidth: CGFloat
        public let cornerRadius: CGFloat
        public let padding: CGFloat
        public let spacing: CGFloat
        public let minHeight: CGFloat
        public let isScrollable: Bool
        public let opacity: Double
    }

    public static let segmentedHeight: CGFloat = 46
    public static let borderWidth: CGFloat = 1
    public static let segmentMinimumScaleFactor: CGFloat = 0.5

    public static let states: [TSControlState] = [.normal, .pressed, .disabled, .focused]

    public static let allConfigurations: [Configuration] = TSTabsVariant.allCases.flatMap { variant in
        [false, true].flatMap { isSelected in
            states.map { Configuration(variant: variant, isSelected: isSelected, state: $0) }
        }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let state = configuration.state
        let isSelected = configuration.isSelected
        let isPressed = state.contains(.pressed) && state.isInteractive
        let opacity = state.contains(.disabled) ? TSOpacity.opacityDisabled : 1
        let showsFocusRing = state.contains(.focused) && !state.contains(.disabled)
        switch configuration.variant {
        case .underline:
            return Resolved(
                labelFont: .label,
                labelColor: isSelected ? .accentText : (isPressed ? .textPrimary : .textMuted),
                fill: nil,
                shadow: nil,
                indicatorColor: isSelected ? .accentText : nil,
                indicatorHeight: 2,
                cornerRadius: TSRadius.radiusSm,
                paddingVertical: TSSpacing.space3,
                paddingHorizontal: 0,
                minHeight: TSHitArea.minimumSize,
                opacity: opacity,
                showsFocusRing: showsFocusRing
            )
        case .segmented:
            let inset = TSComponentTokens.tabsSegmentedTrackPadding + borderWidth
            return Resolved(
                labelFont: .label,
                labelColor: isSelected ? .textPrimary : .textSecondary,
                fill: isSelected ? .surfaceCard : (isPressed ? .surfacePressed : nil),
                shadow: isSelected ? TSShadowTokens.shadowSm : nil,
                indicatorColor: nil,
                indicatorHeight: 0,
                cornerRadius: TSRadius.radiusMd,
                paddingVertical: TSComponentTokens.tabsSegmentPaddingVertical,
                paddingHorizontal: TSComponentTokens.tabsSegmentPaddingHorizontal,
                minHeight: segmentedHeight - inset * 2,
                opacity: opacity,
                showsFocusRing: showsFocusRing
            )
        }
    }

    public static func container(_ variant: TSTabsVariant, isEnabled: Bool = true) -> Container {
        let opacity = isEnabled ? 1 : TSOpacity.opacityDisabled
        switch variant {
        case .underline:
            return Container(
                fill: nil,
                border: .borderSubtle,
                borderWidth: Self.borderWidth,
                cornerRadius: 0,
                padding: 0,
                spacing: TSSpacing.space4,
                minHeight: TSHitArea.minimumSize,
                isScrollable: true,
                opacity: opacity
            )
        case .segmented:
            return Container(
                fill: .backgroundSecondary,
                border: .borderSubtle,
                borderWidth: Self.borderWidth,
                cornerRadius: TSRadius.radiusLg,
                padding: TSComponentTokens.tabsSegmentedTrackPadding,
                spacing: 0,
                minHeight: segmentedHeight,
                isScrollable: false,
                opacity: opacity
            )
        }
    }

    public static func hitOutset(_ variant: TSTabsVariant) -> CGFloat {
        TSHitArea.outset(for: resolve(.init(variant: variant)).minHeight)
    }
}
