import SwiftUI
#if canImport(UIKit)
import UIKit
#elseif canImport(AppKit)
import AppKit
#endif

public extension TSFont {
    #if canImport(UIKit)
    func font(for sizeCategory: UIContentSizeCategory) -> Font {
        Font(uiFont(for: sizeCategory) as CTFont)
    }

    func attributedText(_ string: String, sizeCategory: UIContentSizeCategory) -> AttributedString {
        TSTrackingCompat.attributedString(text(string), tracking: tracking(for: sizeCategory))
    }

    func font(dynamicTypeSize: DynamicTypeSize) -> Font {
        font(for: UIContentSizeCategory(dynamicTypeSize))
    }

    func lineHeight(dynamicTypeSize: DynamicTypeSize) -> CGFloat {
        lineHeight(for: UIContentSizeCategory(dynamicTypeSize))
    }

    func lineSpacing(dynamicTypeSize: DynamicTypeSize) -> CGFloat {
        lineSpacing(for: UIContentSizeCategory(dynamicTypeSize))
    }

    func tracking(dynamicTypeSize: DynamicTypeSize) -> CGFloat {
        tracking(for: UIContentSizeCategory(dynamicTypeSize))
    }
    #elseif canImport(AppKit)
    func attributedText(_ string: String) -> AttributedString {
        TSTrackingCompat.attributedString(text(string), tracking: tracking)
    }

    func font(dynamicTypeSize: DynamicTypeSize) -> Font {
        Font(baseFont as CTFont)
    }

    func lineHeight(dynamicTypeSize: DynamicTypeSize) -> CGFloat {
        lineHeight
    }

    func lineSpacing(dynamicTypeSize: DynamicTypeSize) -> CGFloat {
        lineSpacing
    }

    func tracking(dynamicTypeSize: DynamicTypeSize) -> CGFloat {
        tracking
    }
    #endif
}

public extension View {
    func tsFont(_ font: TSFont) -> some View {
        modifier(TSFontModifier(font: font))
    }
}

public extension Text {
    func tsFont(_ font: TSFont) -> some View {
        TSStyledText(text: self, font: font)
    }
}

struct TSFontModifier: ViewModifier {
    let font: TSFont

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    func body(content: Content) -> some View {
        let lineSpacing = font.lineSpacing(dynamicTypeSize: dynamicTypeSize)
        content
            .font(font.font(dynamicTypeSize: dynamicTypeSize))
            .lineSpacing(lineSpacing)
            .padding(.vertical, lineSpacing / 2)
            .textCase(font.isUppercased ? .uppercase : nil)
    }
}

struct TSStyledText: View {
    let text: Text
    let font: TSFont

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        text
            .tsTracking(font.tracking(dynamicTypeSize: dynamicTypeSize))
            .modifier(TSFontModifier(font: font))
    }
}
