import Testing
import TSDesignSystemUIKit
import UIKit

@MainActor
@Suite("TSIconView behaviour")
struct TSIconViewBehaviorTests {
    @Test("Sizes map to the icon tokens", arguments: TSIconSize.allCases)
    func size(size: TSIconSize) {
        let view = TSIconView(.home, size: size)
        #expect(view.intrinsicContentSize == CGSize(width: size.pointSize, height: size.pointSize))
        #expect(view.image?.renderingMode == .alwaysTemplate)
    }

    @Test("Directional icons mirror in right-to-left layouts")
    func mirroring() {
        let rightToLeft = UITraitCollection(layoutDirection: .rightToLeft)
        let chevron = TSIconView(.chevronRight).image?.imageAsset?.image(with: rightToLeft)
        let home = TSIconView(.home).image?.imageAsset?.image(with: rightToLeft)
        #expect(chevron?.imageOrientation == .upMirrored)
        #expect(home?.imageOrientation == .up)
    }
}
