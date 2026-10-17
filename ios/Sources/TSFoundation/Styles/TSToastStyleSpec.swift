import CoreGraphics
import TSTokens

public enum TSToastStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let tone: TSToastTone
        public let hasAction: Bool

        public init(tone: TSToastTone = .neutral, hasAction: Bool = false) {
            self.tone = tone
            self.hasAction = hasAction
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let fill: TSColor
        public let cornerRadius: CGFloat
        public let shadow: TSShadowToken
        public let maxWidth: CGFloat
        public let horizontalPadding: CGFloat
        public let verticalPadding: CGFloat
        public let spacing: CGFloat
        public let outerMargin: CGFloat
        public let icon: TSIconName?
        public let iconSize: TSIconSize
        public let iconColor: TSColor?
        public let messageFont: TSFont
        public let messageColor: TSColor
        public let showsAction: Bool
        public let actionFont: TSFont
        public let actionColor: TSColor
        public let actionIsUnderlined: Bool
        public let actionHorizontalPadding: CGFloat
        public let actionVerticalPadding: CGFloat
    }

    public static let allConfigurations: [Configuration] = TSToastTone.allCases.flatMap { tone in
        [false, true].map { Configuration(tone: tone, hasAction: $0) }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let tone = configuration.tone
        return Resolved(
            fill: .surfaceInverse,
            cornerRadius: TSRadius.radiusLg,
            shadow: TSShadowTokens.shadowLg,
            maxWidth: TSComponentTokens.toastMaxWidth,
            horizontalPadding: TSSpacing.space4,
            verticalPadding: TSSpacing.space3,
            spacing: TSSpacing.space3,
            outerMargin: TSSpacing.space4,
            icon: icon(tone),
            iconSize: .md,
            iconColor: iconColor(tone),
            messageFont: .bodySm,
            messageColor: .textInverse,
            showsAction: configuration.hasAction,
            actionFont: TSFont.bodySm.weighted(TSComponentTokens.toastActionFontWeight),
            actionColor: .textInverse,
            actionIsUnderlined: true,
            actionHorizontalPadding: TSSpacing.space2,
            actionVerticalPadding: TSSpacing.space1
        )
    }

    public static func icon(_ tone: TSToastTone) -> TSIconName? {
        switch tone {
        case .neutral: nil
        case .info: .info
        case .success: .checkCircle
        case .warning: .alert
        case .danger: .xCircle
        }
    }

    static func iconColor(_ tone: TSToastTone) -> TSColor? {
        switch tone {
        case .neutral: nil
        case .info: .infoOnInverse
        case .success: .successOnInverse
        case .warning: .warningOnInverse
        case .danger: .dangerOnInverse
        }
    }
}
