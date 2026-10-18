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

        public init(size: TSIconSize = .md) {
            self.size = size
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let pointSize: CGFloat
    }

    public static let allConfigurations: [Configuration] = TSIconSize.allCases.map { Configuration(size: $0) }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        Resolved(pointSize: configuration.size.pointSize)
    }
}
