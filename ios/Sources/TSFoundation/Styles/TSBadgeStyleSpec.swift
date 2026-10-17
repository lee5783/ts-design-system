import CoreGraphics
import TSTokens

public enum TSBadgeTone: String, CaseIterable, Hashable, Sendable {
    case neutral
    case accent
    case success
    case warning
    case danger
    case info
}

public enum TSBadgeStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let tone: TSBadgeTone
        public let hasDot: Bool
        public let hasIcon: Bool

        public init(tone: TSBadgeTone = .neutral, hasDot: Bool = false, hasIcon: Bool = false) {
            self.tone = tone
            self.hasDot = hasDot
            self.hasIcon = hasIcon
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let fill: TSColor
        public let foreground: TSColor
        public let border: TSColor?
        public let borderWidth: CGFloat
        public let font: TSFont
        public let minHeight: CGFloat
        public let horizontalPadding: CGFloat
        public let verticalPadding: CGFloat
        public let spacing: CGFloat
        public let cornerRadius: CGFloat
        public let showsDot: Bool
        public let dotSize: CGFloat
        public let showsIcon: Bool
        public let iconSize: TSIconSize
    }

    public static let allConfigurations: [Configuration] = TSBadgeTone.allCases.flatMap { tone in
        [false, true].flatMap { hasDot in
            [false, true].map { Configuration(tone: tone, hasDot: hasDot, hasIcon: $0) }
        }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let tone = configuration.tone
        return Resolved(
            fill: fill(tone),
            foreground: foreground(tone),
            border: tone == .neutral ? TSColor.borderSubtle : nil,
            borderWidth: tone == .neutral ? 1 : 0,
            font: TSFont.caption.weighted(500),
            minHeight: TSComponentTokens.badgeHeight,
            horizontalPadding: TSSpacing.space2,
            verticalPadding: TSSpacing.space0_5,
            spacing: TSSpacing.space1,
            cornerRadius: TSRadius.radiusSm,
            showsDot: configuration.hasDot,
            dotSize: TSComponentTokens.badgeDotSize,
            showsIcon: configuration.hasIcon,
            iconSize: .sm
        )
    }

    static func fill(_ tone: TSBadgeTone) -> TSColor {
        switch tone {
        case .neutral: .backgroundSecondary
        case .accent: .accentSubtle
        case .success: .successSubtle
        case .warning: .warningSubtle
        case .danger: .dangerSubtle
        case .info: .infoSubtle
        }
    }

    static func foreground(_ tone: TSBadgeTone) -> TSColor {
        switch tone {
        case .neutral: .textSecondary
        case .accent: .accentText
        case .success: .success
        case .warning: .warning
        case .danger: .danger
        case .info: .info
        }
    }
}
