import CoreGraphics
import Foundation
import TSTokens

public enum TSSpinnerSize: String, CaseIterable, Hashable, Sendable {
    case sm
    case md
    case lg

    public var iconSize: TSIconSize {
        switch self {
        case .sm: .sm
        case .md: .md
        case .lg: .lg
        }
    }
}

public enum TSSpinnerTone: String, CaseIterable, Hashable, Sendable {
    case primary
    case secondary
    case accent
}

public enum TSSpinnerStyleSpec: TSStyleSpec {
    public static let startAngleDegrees: Double = 45
    public static let sweepFraction: CGFloat = 0.75

    public struct Configuration: Hashable, Sendable {
        public let size: TSSpinnerSize
        public let tone: TSSpinnerTone

        public init(size: TSSpinnerSize = .md, tone: TSSpinnerTone = .primary) {
            self.size = size
            self.tone = tone
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let size: CGFloat
        public let strokeWidth: CGFloat
        public let color: TSColor
        public let rotationDuration: TimeInterval
    }

    public static let allConfigurations: [Configuration] = TSSpinnerSize.allCases.flatMap { size in
        TSSpinnerTone.allCases.map { Configuration(size: size, tone: $0) }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let size = configuration.size.iconSize.pointSize
        return Resolved(
            size: size,
            strokeWidth: strokeWidth(size),
            color: color(configuration.tone),
            rotationDuration: TSComponentTokens.spinnerRotationDuration
        )
    }

    public static func strokeWidth(_ size: CGFloat) -> CGFloat {
        size * TSComponentTokens.spinnerStrokeWidth / TSComponentTokens.spinnerSize
    }

    public static func color(_ tone: TSSpinnerTone) -> TSColor {
        switch tone {
        case .primary: .textPrimary
        case .secondary: .textSecondary
        case .accent: .accentPrimary
        }
    }
}
