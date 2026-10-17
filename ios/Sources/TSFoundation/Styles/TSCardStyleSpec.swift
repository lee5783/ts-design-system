import CoreGraphics
import TSTokens

public enum TSCardStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let isRaised: Bool
        public let isInteractive: Bool
        public let state: TSControlState

        public init(isRaised: Bool = false, isInteractive: Bool = false, state: TSControlState = .normal) {
            self.isRaised = isRaised
            self.isInteractive = isInteractive
            self.state = state
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let fill: TSColor
        public let border: TSColor
        public let borderWidth: CGFloat
        public let cornerRadius: CGFloat
        public let shadow: TSShadowToken?
        public let showsFocusRing: Bool
        public let opacity: Double
        public let titleFont: TSFont
        public let titleColor: TSColor
        public let subtitleFont: TSFont
        public let subtitleColor: TSColor
        public let subtitleSpacing: CGFloat
        public let bodyFont: TSFont
        public let bodyColor: TSColor
        public let padding: CGFloat
        public let headerSpacing: CGFloat
        public let footerVerticalPadding: CGFloat
        public let footerHorizontalPadding: CGFloat
        public let footerSpacing: CGFloat
        public let divider: TSColor
        public let dividerThickness: CGFloat
    }

    public static let states: [TSControlState] = [.normal, .pressed, .disabled, .focused]

    public static let stackSpacing: CGFloat = TSSpacing.space4

    public static let allConfigurations: [Configuration] = [false, true].flatMap { isRaised in
        [false, true].flatMap { isInteractive in
            states.map { Configuration(isRaised: isRaised, isInteractive: isInteractive, state: $0) }
        }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let state = configuration.state
        let isInteractive = configuration.isInteractive
        return Resolved(
            fill: isInteractive && state.isInteractive && state.contains(.pressed) ? .surfacePressed : .surfaceCard,
            border: .borderSubtle,
            borderWidth: 1,
            cornerRadius: TSRadius.radiusLg,
            shadow: configuration.isRaised ? TSShadowTokens.shadowSm : nil,
            showsFocusRing: isInteractive && state.isInteractive && state.contains(.focused),
            opacity: state.contains(.disabled) ? TSOpacity.opacityDisabled : 1,
            titleFont: .title3,
            titleColor: .textPrimary,
            subtitleFont: .bodySm,
            subtitleColor: .textMuted,
            subtitleSpacing: TSComponentTokens.cardSubtitleSpacing,
            bodyFont: .bodySm,
            bodyColor: .textSecondary,
            padding: TSSpacing.space4,
            headerSpacing: TSSpacing.space3,
            footerVerticalPadding: TSSpacing.space3,
            footerHorizontalPadding: TSSpacing.space4,
            footerSpacing: TSSpacing.space2,
            divider: .borderSubtle,
            dividerThickness: 1
        )
    }
}
