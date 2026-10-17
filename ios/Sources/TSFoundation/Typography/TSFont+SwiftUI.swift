import SwiftUI
import UIKit

public extension TSFont {
    func font(for sizeCategory: UIContentSizeCategory) -> Font {
        Font(uiFont(for: sizeCategory) as CTFont)
    }

    func attributedText(_ string: String, sizeCategory: UIContentSizeCategory) -> AttributedString {
        TSTrackingCompat.attributedString(text(string), tracking: tracking(for: sizeCategory))
    }
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
        let sizeCategory = UIContentSizeCategory(dynamicTypeSize)
        let lineSpacing = font.lineSpacing(for: sizeCategory)
        content
            .font(font.font(for: sizeCategory))
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
        let sizeCategory = UIContentSizeCategory(dynamicTypeSize)
        text
            .tsTracking(font.tracking(for: sizeCategory))
            .modifier(TSFontModifier(font: font))
    }
}
