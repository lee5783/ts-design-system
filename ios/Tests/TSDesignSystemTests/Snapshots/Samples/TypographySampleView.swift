import SwiftUI
import TSDesignSystem

struct TypographySampleView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            ForEach(TSFont.allFonts, id: \.token.name) { font in
                VStack(alignment: .leading, spacing: TSSpacing.space1) {
                    Text(font.token.name)
                        .foregroundColor(TSColor.textMuted.color)
                        .tsFont(.caption)
                    Text("Sphinx of black quartz")
                        .foregroundColor(TSColor.textPrimary.color)
                        .tsFont(font)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding(TSSpacing.space4)
    }
}
