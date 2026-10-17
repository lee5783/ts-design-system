import CoreGraphics
import TSTokens

public enum TSCheckboxStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let isOn: Bool
        public let state: TSControlState

        public init(isOn: Bool = false, state: TSControlState = .normal) {
            self.isOn = isOn
            self.state = state
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let markSize: CGFloat
        public let markCornerRadius: CGFloat
        public let markFill: TSColor
        public let markBorder: TSColor
        public let markBorderWidth: CGFloat
        public let showsCheck: Bool
        public let checkColor: TSColor
        public let checkSize: TSIconSize
        public let labelFont: TSFont
        public let labelColor: TSColor
        public let spacing: CGFloat
        public let minHeight: CGFloat
        public let opacity: Double
        public let showsFocusRing: Bool
    }

    public static let states: [TSControlState] = [.normal, .pressed, .disabled, .focused]

    public static let allConfigurations: [Configuration] = [false, true].flatMap { isOn in
        states.map { Configuration(isOn: isOn, state: $0) }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let isOn = configuration.isOn
        let state = configuration.state
        let isPressed = state.contains(.pressed) && state.isInteractive
        let accent: TSColor = isPressed ? .accentPressed : .accentPrimary
        return Resolved(
            markSize: TSComponentTokens.checkboxSize,
            markCornerRadius: TSRadius.radiusXs,
            markFill: isOn ? accent : (isPressed ? .surfacePressed : .surfaceCard),
            markBorder: isOn ? accent : .borderStrong,
            markBorderWidth: TSComponentTokens.checkboxBorderWidth,
            showsCheck: isOn,
            checkColor: .textOnAccent,
            checkSize: .sm,
            labelFont: .body,
            labelColor: .textPrimary,
            spacing: TSSpacing.space2,
            minHeight: TSHitArea.minimumSize,
            opacity: state.contains(.disabled) ? TSOpacity.opacityDisabled : 1,
            showsFocusRing: state.contains(.focused) && !state.contains(.disabled)
        )
    }
}
