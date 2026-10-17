import CoreGraphics
import TSTokens

public enum TSButtonVariant: String, CaseIterable, Hashable, Sendable {
    case primary
    case secondary
    case ghost
    case danger
    case link
}

public enum TSButtonSize: String, CaseIterable, Hashable, Sendable {
    case md
    case lg
}

public enum TSButtonStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let variant: TSButtonVariant
        public let size: TSButtonSize
        public let state: TSControlState

        public init(variant: TSButtonVariant = .primary, size: TSButtonSize = .md, state: TSControlState = .normal) {
            self.variant = variant
            self.size = size
            self.state = state
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let fill: TSColor?
        public let label: TSColor
        public let border: TSColor?
        public let borderWidth: CGFloat
        public let shadow: TSShadowToken?
        public let font: TSFont
        public let minHeight: CGFloat
        public let horizontalPadding: CGFloat
        public let verticalPadding: CGFloat
        public let cornerRadius: CGFloat
        public let iconSize: CGFloat
        public let iconSpacing: CGFloat
        public let spinnerSize: CGFloat
        public let spinnerStrokeWidth: CGFloat
        public let opacity: Double
        public let isUnderlined: Bool
        public let showsSpinner: Bool
        public let showsFocusRing: Bool
        public let minimumHitSize: CGFloat
    }

    public static let states: [TSControlState] = [.normal, .pressed, .disabled, .loading, .focused]

    public static let allConfigurations: [Configuration] = TSButtonVariant.allCases.flatMap { variant in
        TSButtonSize.allCases.flatMap { size in
            states.map { Configuration(variant: variant, size: size, state: $0) }
        }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let variant = configuration.variant
        let state = configuration.state
        let isPressed = state.contains(.pressed) && state.isInteractive
        let isLink = variant == .link
        return Resolved(
            fill: fill(variant, isPressed: isPressed),
            label: label(variant),
            border: variant == .secondary ? TSColor.borderStrong : nil,
            borderWidth: variant == .secondary ? 1 : 0,
            shadow: variant == .secondary ? TSShadowTokens.shadowXs : nil,
            font: font(configuration.size),
            minHeight: isLink ? 0 : minHeight(configuration.size),
            horizontalPadding: isLink ? 0 : horizontalPadding(configuration.size),
            verticalPadding: isLink ? 0 : TSSpacing.space2,
            cornerRadius: isLink ? 0 : TSRadius.radiusMd,
            iconSize: TSSize.iconMd,
            iconSpacing: TSSpacing.space2,
            spinnerSize: TSComponentTokens.spinnerSize,
            spinnerStrokeWidth: TSComponentTokens.spinnerStrokeWidth,
            opacity: opacity(isLink: isLink, state: state, isPressed: isPressed),
            isUnderlined: isLink,
            showsSpinner: state.contains(.loading),
            showsFocusRing: state.contains(.focused) && !state.contains(.disabled),
            minimumHitSize: TSHitArea.minimumSize
        )
    }

    public static func font(_ size: TSButtonSize) -> TSFont {
        switch size {
        case .md: TSFont.label
        case .lg: TSFont.body.weighted(TSTypographyTokens.label.fontWeight)
        }
    }

    public static func minHeight(_ size: TSButtonSize) -> CGFloat {
        switch size {
        case .md: TSSize.controlMd
        case .lg: TSSize.controlLg
        }
    }

    public static func horizontalPadding(_ size: TSButtonSize) -> CGFloat {
        switch size {
        case .md: TSSpacing.space4
        case .lg: TSSpacing.space5
        }
    }

    static func fill(_ variant: TSButtonVariant, isPressed: Bool) -> TSColor? {
        switch variant {
        case .primary: isPressed ? .accentPressed : .accentPrimary
        case .secondary: isPressed ? .surfacePressed : .surfaceCard
        case .ghost: isPressed ? .surfacePressed : nil
        case .danger: isPressed ? .dangerPressed : .dangerSolid
        case .link: nil
        }
    }

    static func label(_ variant: TSButtonVariant) -> TSColor {
        switch variant {
        case .primary, .danger: .textOnAccent
        case .secondary, .ghost: .textPrimary
        case .link: .accentText
        }
    }

    static func opacity(isLink: Bool, state: TSControlState, isPressed: Bool) -> Double {
        if state.contains(.disabled) {
            return TSOpacity.opacityDisabled
        }
        if isLink, isPressed {
            return TSOpacity.opacityPressed
        }
        return 1
    }
}
