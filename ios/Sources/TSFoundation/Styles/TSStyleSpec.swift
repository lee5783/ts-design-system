import Foundation

public struct TSControlState: OptionSet, Hashable, Sendable {
    public let rawValue: Int

    public init(rawValue: Int) {
        self.rawValue = rawValue
    }

    public static let pressed = TSControlState(rawValue: 1 << 0)
    public static let disabled = TSControlState(rawValue: 1 << 1)
    public static let focused = TSControlState(rawValue: 1 << 2)
    public static let loading = TSControlState(rawValue: 1 << 3)
    public static let error = TSControlState(rawValue: 1 << 4)
    public static let selected = TSControlState(rawValue: 1 << 5)

    public static let normal: TSControlState = []

    public var isInteractive: Bool {
        isDisjoint(with: [.disabled, .loading])
    }
}

public protocol TSStyleSpec {
    associatedtype Configuration: Hashable & Sendable
    associatedtype Resolved: Equatable & Sendable

    static var allConfigurations: [Configuration] { get }

    static func resolve(_ configuration: Configuration) -> Resolved
}

public extension TSStyleSpec {
    static var matrix: [(configuration: Configuration, resolved: Resolved)] {
        allConfigurations.map { ($0, resolve($0)) }
    }
}
