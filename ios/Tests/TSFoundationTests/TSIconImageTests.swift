import Testing
import TSTokens
import UIKit
@testable import TSFoundation

@Suite("TSIconName images")
struct TSIconImageTests {
    @Test("Every TSIconName resolves to a 24pt template image", arguments: TSIconName.allCases)
    func resolvesTemplateImage(name: TSIconName) throws {
        let image = try #require(name.image())
        #expect(image.renderingMode == .alwaysTemplate)
        #expect(image.size == CGSize(width: 24, height: 24))
    }

    @Test("Only directional icons resolve mirrored in right-to-left layouts", arguments: TSIconName.allCases)
    func mirroring(name: TSIconName) throws {
        let leftToRight = try #require(name.image(compatibleWith: UITraitCollection(layoutDirection: .leftToRight)))
        let rightToLeft = try #require(name.image(compatibleWith: UITraitCollection(layoutDirection: .rightToLeft)))
        #expect(leftToRight.imageOrientation == .up)
        #expect(rightToLeft.imageOrientation == (name.flipsForRightToLeftLayoutDirection ? .upMirrored : .up))
    }

    @Test("The icon set has 33 icons and 5 directional ones")
    func iconSet() {
        #expect(TSIconName.allCases.count == 33)
        #expect(
            TSIconName.allCases.filter(\.flipsForRightToLeftLayoutDirection) == [
                .arrowLeft, .arrowRight, .chevronLeft, .chevronRight, .logout
            ]
        )
    }

    @Test("The wordmark has distinct light and dark appearances")
    func wordmarkAppearances() throws {
        let bundle = TSFoundationResources.bundle
        let light = try #require(UIImage(named: "ts-wordmark", in: bundle, compatibleWith: UITraitCollection(userInterfaceStyle: .light)))
        let dark = try #require(UIImage(named: "ts-wordmark", in: bundle, compatibleWith: UITraitCollection(userInterfaceStyle: .dark)))
        #expect(light.size == CGSize(width: 248, height: 48))
        #expect(light.renderingMode != .alwaysTemplate)
        #expect(light.pngData() != dark.pngData())
    }

    @Test("The monogram resolves as an original-color image")
    func monogram() throws {
        let monogram = try #require(UIImage(named: "ts-monogram", in: TSFoundationResources.bundle, compatibleWith: nil))
        #expect(monogram.size == CGSize(width: 64, height: 64))
        #expect(monogram.renderingMode != .alwaysTemplate)
    }
}
