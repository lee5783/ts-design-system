import CoreGraphics
import Foundation
import TSTokens

public enum TSSwitchStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let isOn: Bool
        public let state: TSControlState

        public init(isOn: Bool = false, state: TSControlState = .normal) {
            self.isOn = isOn
            self.state = state
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let trackWidth: CGFloat
        public let trackHeight: CGFloat
        public let trackColor: TSColor
        public let trackOpacity: Double
        public let thumbSize: CGFloat
        public let thumbInset: CGFloat
        public let thumbLeadingOffset: CGFloat
        public let thumbColor: TSColor
        public let thumbShadow: TSShadowToken
        public let labelFont: TSFont
        public let labelColor: TSColor
        public let spacing: CGFloat
        public let minHeight: CGFloat
        public let opacity: Double
        public let showsFocusRing: Bool
        public let animationDuration: TimeInterval

        public var trackCornerRadius: CGFloat {
            trackHeight / 2
        }

        public func thumbOriginX(isRightToLeft: Bool) -> CGFloat {
            isRightToLeft ? trackWidth - thumbLeadingOffset - thumbSize : thumbLeadingOffset
        }
    }

    public static let states: [TSControlState] = [.normal, .pressed, .disabled, .focused]

    public static let allConfigurations: [Configuration] = [false, true].flatMap { isOn in
        states.map { Configuration(isOn: isOn, state: $0) }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let isOn = configuration.isOn
        let state = configuration.state
        let isPressed = state.contains(.pressed) && state.isInteractive
        let inset = TSComponentTokens.switchThumbInset
        return Resolved(
            trackWidth: TSComponentTokens.switchTrackWidth,
            trackHeight: TSComponentTokens.switchTrackHeight,
            trackColor: isOn ? (isPressed ? .accentPressed : .accentPrimary) : .borderStrong,
            trackOpacity: isPressed && !isOn ? TSOpacity.opacityPressed : 1,
            thumbSize: TSComponentTokens.switchThumbSize,
            thumbInset: inset,
            thumbLeadingOffset: isOn ? inset + TSComponentTokens.switchThumbTravel : inset,
            thumbColor: .switchThumb,
            thumbShadow: TSShadowTokens.shadowSm,
            labelFont: .body,
            labelColor: .textPrimary,
            spacing: TSSpacing.space2,
            minHeight: TSHitArea.minimumSize,
            opacity: state.contains(.disabled) ? TSOpacity.opacityDisabled : 1,
            showsFocusRing: state.contains(.focused) && !state.contains(.disabled),
            animationDuration: TSMotion.durationBase
        )
    }
}
