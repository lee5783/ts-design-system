import SwiftUI
import Testing
import TSTokens
import UIKit
@testable import TSFoundation

@Suite("TSFont scaling")
struct TSFontTests {
    static let sizeCategories: [UIContentSizeCategory] = [.extraSmall, .large, .accessibilityExtraExtraExtraLarge]

    @Test("Line height is the DS line height times the font scale factor", arguments: TSFont.allFonts, sizeCategories)
    func lineHeightScales(font: TSFont, sizeCategory: UIContentSizeCategory) {
        let scaled = font.uiFont(for: sizeCategory)
        let factor = scaled.pointSize / font.token.fontSize
        #expect(abs(font.scaleFactor(for: sizeCategory) - factor) < 0.0001)
        #expect(abs(font.lineHeight(for: sizeCategory) - font.token.lineHeight * factor) < 0.0001)
    }

    @Test("The default size category keeps the exact DS sizes", arguments: TSFont.allFonts)
    func defaultSize(font: TSFont) {
        #expect(font.uiFont(for: .large).pointSize == font.token.fontSize)
        #expect(font.scaleFactor(for: .large) == 1)
        #expect(font.lineHeight(for: .large) == font.token.lineHeight)
    }

    @Test("Smaller categories shrink and accessibility categories grow", arguments: TSFont.allFonts)
    func scalingDirection(font: TSFont) {
        #expect(font.scaleFactor(for: .extraSmall) <= 1)
        #expect(font.scaleFactor(for: .accessibilityExtraExtraExtraLarge) > 1)
        #expect(font.lineHeight(for: .accessibilityExtraExtraExtraLarge) > font.token.lineHeight)
    }

    @Test("The scale factor follows UIFontMetrics for the base text style", arguments: TSFont.allFonts, sizeCategories)
    func followsFontMetrics(font: TSFont, sizeCategory: UIContentSizeCategory) {
        let traits = UITraitCollection(preferredContentSizeCategory: sizeCategory)
        let expected = font.metrics.scaledValue(for: font.token.fontSize, compatibleWith: traits)
        #expect(abs(font.uiFont(for: sizeCategory).pointSize - expected) < 0.5)
    }

    @Test("Line heights never clip the glyph line box", arguments: TSFont.allFonts, sizeCategories)
    func neverClipped(font: TSFont, sizeCategory: UIContentSizeCategory) {
        #expect(font.lineHeight(for: sizeCategory) >= font.uiFont(for: sizeCategory).lineHeight)
        #expect(font.lineSpacing(for: sizeCategory) >= 0)
    }

    @Test("Weights and families come from the tokens")
    func weightsAndFamilies() {
        let weights = TSFont.allFonts.map { weight(of: $0.baseFont) }
        #expect(weights == TSFont.allFonts.map { $0.token.uiFontWeight.rawValue })
        #expect(weight(of: TSFont.title2.baseFont) == UIFont.Weight.semibold.rawValue)
        #expect(weight(of: TSFont.label.baseFont) == UIFont.Weight.medium.rawValue)
        #expect(TSFont.code.baseFont.fontDescriptor.symbolicTraits.contains(.traitMonoSpace))
        #expect(TSFont.allFonts.count == TSTypographyTokens.allTokens.count)
    }

    private func weight(of font: UIFont) -> CGFloat? {
        let traits = font.fontDescriptor.object(forKey: .traits) as? [UIFontDescriptor.TraitKey: Any]
        return traits?[.weight] as? CGFloat
    }

    @Test("Tracking scales with the point size in em")
    func tracking() {
        #expect(abs(TSFont.overline.tracking(for: .large) - 0.66) < 0.0001)
        #expect(TSFont.overline.tracking(for: .accessibilityExtraExtraExtraLarge) > 0.66)
        #expect(TSFont.body.tracking(for: .large) == 0)
    }

    @Test("UIKit attributes carry the scaled font, a fixed line height, and kerning")
    func uiKitAttributes() throws {
        let attributed = TSFont.overline.attributedString("New", sizeCategory: .large)
        #expect(attributed.string == "NEW")
        let attributes = attributed.attributes(at: 0, effectiveRange: nil)
        let paragraph = try #require(attributes[.paragraphStyle] as? NSParagraphStyle)
        #expect(paragraph.minimumLineHeight == 16)
        #expect(paragraph.maximumLineHeight == 16)
        #expect((attributes[.kern] as? CGFloat).map { abs($0 - 0.66) < 0.0001 } == true)
        #expect((attributes[.font] as? UIFont)?.pointSize == 11)
        #expect(TSFont.body.attributes(for: .large)[.kern] == nil)
    }

    @Test("SwiftUI attributed strings carry the tracking through the compat shim")
    func swiftUIAttributedString() {
        let attributed = TSFont.overline.attributedText("New", sizeCategory: .large)
        #expect(String(attributed.characters) == "NEW")
        #expect(attributed.kern.map { abs($0 - 0.66) < 0.0001 } == true)
        #expect(TSFont.body.attributedText("Body", sizeCategory: .large).kern == nil)
    }

    @Test("Dynamic Type sizes map to the same size categories", arguments: [
        (DynamicTypeSize.xSmall, UIContentSizeCategory.extraSmall),
        (.large, .large),
        (.accessibility5, .accessibilityExtraExtraExtraLarge),
    ])
    func dynamicTypeMapping(dynamicTypeSize: DynamicTypeSize, sizeCategory: UIContentSizeCategory) {
        #expect(UIContentSizeCategory(dynamicTypeSize) == sizeCategory)
    }
}
