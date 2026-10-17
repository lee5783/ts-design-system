import Testing
import UIKit

public struct TSSnapshotConfiguration: Hashable, Sendable, CustomTestStringConvertible {
    public let appearance: TSTestAppearance
    public let contentSize: TSTestContentSize
    public let layoutDirection: TSTestLayoutDirection

    public init(
        appearance: TSTestAppearance = .light,
        contentSize: TSTestContentSize = .large,
        layoutDirection: TSTestLayoutDirection = .ltr
    ) {
        self.appearance = appearance
        self.contentSize = contentSize
        self.layoutDirection = layoutDirection
    }

    public static let standardMatrix: [TSSnapshotConfiguration] = TSTestAppearance.allCases.flatMap { appearance in
        TSTestContentSize.allCases.flatMap { contentSize in
            TSTestLayoutDirection.allCases.map { layoutDirection in
                TSSnapshotConfiguration(appearance: appearance, contentSize: contentSize, layoutDirection: layoutDirection)
            }
        }
    }

    public var name: String {
        [appearance.rawValue, contentSize.rawValue, layoutDirection.rawValue].joined(separator: "-")
    }

    public var testDescription: String { name }

    public var traitCollection: UITraitCollection {
        UITraitCollection(traitsFrom: [
            appearance.traitCollection,
            contentSize.traitCollection,
            layoutDirection.traitCollection,
            TSSnapshotDevice.traitCollection
        ])
    }
}
