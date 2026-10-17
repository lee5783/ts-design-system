import CoreGraphics
import TSTokens

public enum TSIconButtonVariant: String, CaseIterable, Hashable, Sendable {
    case ghost
    case secondary
    case primary
}

public enum TSIconButtonStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let variant: TSIconButtonVariant
        public let isRound: Bool
        public let state: TSControlState

        public init(variant: TSIconButtonVariant = .ghost, isRound: Bool = false, state: TSControlState = .normal) {
            self.variant = variant
            self.isRound = isRound
            self.state = state
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let fill: TSColor?
        public let icon: TSColor
        public let border: TSColor?
        public let borderWidth: CGFloat
        public let size: CGFloat
        public let cornerRadius: CGFloat
        public let iconSize: CGFloat
        public let opacity: Double
        public let showsFocusRing: Bool
    }

    public static let states: [TSControlState] = [.normal, .pressed, .disabled, .focused]

    public static let allConfigurations: [Configuration] = TSIconButtonVariant.allCases.flatMap { variant in
        [false, true].flatMap { isRound in
            states.map { Configuration(variant: variant, isRound: isRound, state: $0) }
        }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let variant = configuration.variant
        let state = configuration.state
        let isPressed = state.contains(.pressed) && state.isInteractive
        let size = TSSize.controlMd
        return Resolved(
            fill: fill(variant, isPressed: isPressed),
            icon: variant == .primary ? .textOnAccent : .textPrimary,
            border: variant == .secondary ? TSColor.borderStrong : nil,
            borderWidth: variant == .secondary ? 1 : 0,
            size: size,
            cornerRadius: configuration.isRound ? size / 2 : TSRadius.radiusMd,
            iconSize: TSIconSize.lg.pointSize,
            opacity: state.contains(.disabled) ? TSOpacity.opacityDisabled : 1,
            showsFocusRing: state.contains(.focused) && !state.contains(.disabled)
        )
    }

    static func fill(_ variant: TSIconButtonVariant, isPressed: Bool) -> TSColor? {
        switch variant {
        case .ghost: isPressed ? .surfacePressed : nil
        case .secondary: isPressed ? .surfacePressed : .surfaceCard
        case .primary: isPressed ? .accentPressed : .accentPrimary
        }
    }
}
