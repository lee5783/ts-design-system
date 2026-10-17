import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct TypographyPage: View {
    var body: some View {
        DemoPage(page: .typography) {
            ForEach(TSFont.allFonts, id: \.token.name) { font in
                VStack(alignment: .leading, spacing: TSSpacing.space1) {
                    Text("\(font.token.name) · \(Int(font.token.fontSize))/\(Int(font.token.lineHeight)) · \(font.token.fontWeight)")
                        .foregroundColor(TSColor.textMuted.color)
                        .tsFont(.code)
                    Text(DemoStrings.string(.pangram))
                        .foregroundColor(TSColor.textPrimary.color)
                        .tsFont(font)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityElement(children: .combine)
                .accessibilityIdentifier("demo.token.\(font.token.name)")
            }
        }
    }
}
