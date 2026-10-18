import Testing
import TSFoundation
import TSTokens
import UIKit

@Suite("TSIconStyleSpec")
struct TSIconStyleSpecTests {
    @Test("Sizes map to the icon-sm/md/lg tokens", arguments: zip(TSIconSize.allCases, [TSSize.iconSm, TSSize.iconMd, TSSize.iconLg]))
    func sizes(size: TSIconSize, expected: CGFloat) {
        #expect(TSIconStyleSpec.resolve(.init(size: size)).pointSize == expected)
    }

    @Test("Template icons render at the requested size and keep RTL mirroring", arguments: [TSIconName.home, .chevronRight])
    func templateImage(name: TSIconName) throws {
        let image = try #require(name.templateImage(pointSize: TSSize.iconMd))
        #expect(image.size == CGSize(width: 20, height: 20))
        #expect(image.renderingMode == .alwaysTemplate)
        #expect(image.flipsForRightToLeftLayoutDirection == name.flipsForRightToLeftLayoutDirection)
    }
}
