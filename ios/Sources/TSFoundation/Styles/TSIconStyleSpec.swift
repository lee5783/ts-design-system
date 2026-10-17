import CoreGraphics
import TSTokens

public enum TSIconSize: String, CaseIterable, Hashable, Sendable {
    case sm
    case md
    case lg

    public var pointSize: CGFloat {
        switch self {
        case .sm: TSSize.iconSm
        case .md: TSSize.iconMd
        case .lg: TSSize.iconLg
        }
    }
}

public enum TSIconStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let size: TSIconSize
        public let hasLabel: Bool

        public init(size: TSIconSize = .md, hasLabel: Bool = false) {
            self.size = size
            self.hasLabel = hasLabel
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let pointSize: CGFloat
        public let isAccessibilityElement: Bool
    }

    public static let allConfigurations: [Configuration] = TSIconSize.allCases.flatMap { size in
        [false, true].map { Configuration(size: size, hasLabel: $0) }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        Resolved(pointSize: configuration.size.pointSize, isAccessibilityElement: configuration.hasLabel)
    }
}
