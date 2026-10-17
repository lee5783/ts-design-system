import CoreGraphics
import TSTokens

public enum TSAlertTone: String, CaseIterable, Hashable, Sendable {
    case info
    case success
    case warning
    case danger
}

public enum TSAlertStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let tone: TSAlertTone
        public let hasTitle: Bool
        public let isDismissible: Bool

        public init(tone: TSAlertTone = .info, hasTitle: Bool = true, isDismissible: Bool = false) {
            self.tone = tone
            self.hasTitle = hasTitle
            self.isDismissible = isDismissible
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let fill: TSColor
        public let borderWidth: CGFloat
        public let cornerRadius: CGFloat
        public let horizontalPadding: CGFloat
        public let verticalPadding: CGFloat
        public let spacing: CGFloat
        public let icon: TSIconName
        public let iconSize: TSIconSize
        public let iconColor: TSColor
        public let showsTitle: Bool
        public let titleFont: TSFont
        public let titleColor: TSColor
        public let messageFont: TSFont
        public let messageColor: TSColor
        public let showsDismiss: Bool
        public let dismissIcon: TSIconName
        public let dismissIconSize: TSIconSize
        public let dismissColor: TSColor
    }

    public static let allConfigurations: [Configuration] = TSAlertTone.allCases.flatMap { tone in
        [false, true].flatMap { hasTitle in
            [false, true].map { Configuration(tone: tone, hasTitle: hasTitle, isDismissible: $0) }
        }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let tone = configuration.tone
        return Resolved(
            fill: fill(tone),
            borderWidth: 1,
            cornerRadius: TSRadius.radiusLg,
            horizontalPadding: TSSpacing.space4,
            verticalPadding: TSSpacing.space3,
            spacing: TSSpacing.space3,
            icon: icon(tone),
            iconSize: .md,
            iconColor: iconColor(tone),
            showsTitle: configuration.hasTitle,
            titleFont: TSFont.bodySm.weighted(TSComponentTokens.alertTitleFontWeight),
            titleColor: .textPrimary,
            messageFont: .bodySm,
            messageColor: .textSecondary,
            showsDismiss: configuration.isDismissible,
            dismissIcon: .close,
            dismissIconSize: .md,
            dismissColor: .textSecondary
        )
    }

    public static func icon(_ tone: TSAlertTone) -> TSIconName {
        switch tone {
        case .info: .info
        case .success: .checkCircle
        case .warning: .alert
        case .danger: .xCircle
        }
    }

    static func fill(_ tone: TSAlertTone) -> TSColor {
        switch tone {
        case .info: .infoSubtle
        case .success: .successSubtle
        case .warning: .warningSubtle
        case .danger: .dangerSubtle
        }
    }

    static func iconColor(_ tone: TSAlertTone) -> TSColor {
        switch tone {
        case .info: .info
        case .success: .success
        case .warning: .warning
        case .danger: .danger
        }
    }
}
