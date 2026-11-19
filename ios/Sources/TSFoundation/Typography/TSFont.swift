import TSTokens
#if canImport(UIKit)
import UIKit
#elseif canImport(AppKit)
import AppKit
#endif

public struct TSFont: Hashable, Sendable {
    public let token: TSTypographyToken
    public let isUppercased: Bool
    public let fontWeight: Int

    public init(_ token: TSTypographyToken, isUppercased: Bool = false, fontWeight: Int? = nil) {
        self.token = token
        self.isUppercased = isUppercased
        self.fontWeight = fontWeight ?? token.fontWeight
    }

    public static let display = TSFont(TSTypographyTokens.display)
    public static let title1 = TSFont(TSTypographyTokens.title1)
    public static let title2 = TSFont(TSTypographyTokens.title2)
    public static let title3 = TSFont(TSTypographyTokens.title3)
    public static let bodyLg = TSFont(TSTypographyTokens.bodyLg)
    public static let body = TSFont(TSTypographyTokens.body)
    public static let bodySm = TSFont(TSTypographyTokens.bodySm)
    public static let label = TSFont(TSTypographyTokens.label)
    public static let caption = TSFont(TSTypographyTokens.caption)
    public static let overline = TSFont(TSTypographyTokens.overline, isUppercased: true)
    public static let code = TSFont(TSTypographyTokens.code)

    public static let allFonts: [TSFont] = [
        display,
        title1,
        title2,
        title3,
        bodyLg,
        body,
        bodySm,
        label,
        caption,
        overline,
        code,
    ]

    public func weighted(_ fontWeight: Int) -> TSFont {
        TSFont(token, isUppercased: isUppercased, fontWeight: fontWeight)
    }

    #if canImport(UIKit)
    public var uiFontWeight: UIFont.Weight {
        TSFontWeight.uiFontWeight(fontWeight)
    }

    public var metrics: UIFontMetrics {
        UIFontMetrics(forTextStyle: token.baseTextStyle.uiTextStyle)
    }

    public var baseFont: UIFont {
        switch token.family {
        case .sans: UIFont.systemFont(ofSize: token.fontSize, weight: uiFontWeight)
        case .mono: UIFont.monospacedSystemFont(ofSize: token.fontSize, weight: uiFontWeight)
        }
    }

    public func uiFont(compatibleWith traitCollection: UITraitCollection? = nil) -> UIFont {
        metrics.scaledFont(for: baseFont, compatibleWith: traitCollection)
    }

    public func uiFont(for sizeCategory: UIContentSizeCategory) -> UIFont {
        uiFont(compatibleWith: UITraitCollection(preferredContentSizeCategory: sizeCategory))
    }

    public func scaleFactor(for sizeCategory: UIContentSizeCategory) -> CGFloat {
        uiFont(for: sizeCategory).pointSize / token.fontSize
    }

    public func lineHeight(for sizeCategory: UIContentSizeCategory) -> CGFloat {
        token.lineHeight * scaleFactor(for: sizeCategory)
    }

    public func tracking(for sizeCategory: UIContentSizeCategory) -> CGFloat {
        token.letterSpacingEm * uiFont(for: sizeCategory).pointSize
    }

    public func lineSpacing(for sizeCategory: UIContentSizeCategory) -> CGFloat {
        max(0, lineHeight(for: sizeCategory) - uiFont(for: sizeCategory).lineHeight)
    }

    public func paragraphStyle(for sizeCategory: UIContentSizeCategory, alignment: NSTextAlignment = .natural) -> NSParagraphStyle {
        let lineHeight = lineHeight(for: sizeCategory)
        let style = NSMutableParagraphStyle()
        style.minimumLineHeight = lineHeight
        style.maximumLineHeight = lineHeight
        style.alignment = alignment
        return style
    }

    public func baselineOffset(for sizeCategory: UIContentSizeCategory) -> CGFloat {
        (lineHeight(for: sizeCategory) - uiFont(for: sizeCategory).lineHeight) / 4
    }
    #elseif canImport(AppKit)
    public var nsFontWeight: NSFont.Weight {
        TSFontWeight.nsFontWeight(fontWeight)
    }

    public var baseFont: NSFont {
        switch token.family {
        case .sans: NSFont.systemFont(ofSize: token.fontSize, weight: nsFontWeight)
        case .mono: NSFont.monospacedSystemFont(ofSize: token.fontSize, weight: nsFontWeight)
        }
    }

    public var lineHeight: CGFloat {
        token.lineHeight
    }

    public var tracking: CGFloat {
        token.letterSpacingEm * token.fontSize
    }

    public var lineSpacing: CGFloat {
        max(0, lineHeight - TSFont.lineHeight(of: baseFont))
    }

    public func paragraphStyle(alignment: NSTextAlignment = .natural) -> NSParagraphStyle {
        let style = NSMutableParagraphStyle()
        style.minimumLineHeight = lineHeight
        style.maximumLineHeight = lineHeight
        style.alignment = alignment
        return style
    }

    public var baselineOffset: CGFloat {
        (lineHeight - TSFont.lineHeight(of: baseFont)) / 4
    }

    static func lineHeight(of font: NSFont) -> CGFloat {
        font.ascender - font.descender + font.leading
    }
    #endif

    public func text(_ string: String) -> String {
        isUppercased ? string.uppercased() : string
    }

    #if canImport(UIKit)
    public func attributes(
        for sizeCategory: UIContentSizeCategory,
        color: UIColor? = nil,
        alignment: NSTextAlignment = .natural
    ) -> [NSAttributedString.Key: Any] {
        var attributes: [NSAttributedString.Key: Any] = [
            .font: uiFont(for: sizeCategory),
            .paragraphStyle: paragraphStyle(for: sizeCategory, alignment: alignment),
            .baselineOffset: baselineOffset(for: sizeCategory),
        ]
        let tracking = tracking(for: sizeCategory)
        if tracking != 0 {
            attributes[.kern] = tracking
        }
        if let color {
            attributes[.foregroundColor] = color
        }
        return attributes
    }

    public func attributedString(
        _ string: String,
        sizeCategory: UIContentSizeCategory,
        color: UIColor? = nil,
        alignment: NSTextAlignment = .natural
    ) -> NSAttributedString {
        NSAttributedString(string: text(string), attributes: attributes(for: sizeCategory, color: color, alignment: alignment))
    }
    #elseif canImport(AppKit)
    public func attributes(color: NSColor? = nil, alignment: NSTextAlignment = .natural) -> [NSAttributedString.Key: Any] {
        var attributes: [NSAttributedString.Key: Any] = [
            .font: baseFont,
            .paragraphStyle: paragraphStyle(alignment: alignment),
            .baselineOffset: baselineOffset,
        ]
        if tracking != 0 {
            attributes[.kern] = tracking
        }
        if let color {
            attributes[.foregroundColor] = color
        }
        return attributes
    }

    public func attributedString(_ string: String, color: NSColor? = nil, alignment: NSTextAlignment = .natural) -> NSAttributedString {
        NSAttributedString(string: text(string), attributes: attributes(color: color, alignment: alignment))
    }
    #endif
}
