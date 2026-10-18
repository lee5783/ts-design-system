import SwiftUI
import TSDesignSystem

enum TextGalleryKind: String, CaseIterable {
    case roles
    case colors
}

struct TextGalleryView: View {
    let kind: TextGalleryKind

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            switch kind {
            case .roles:
                ForEach(TSTypeRole.allCases, id: \.self) { role in
                    TSText("\(role.rawValue) Sphinx of black quartz", role: role)
                }
                TSText("Body, one line, truncated at the end of the line because it is long")
                    .lineLimit(1)
            case .colors:
                ForEach(TSTextColor.allCases, id: \.self) { color in
                    TSText("\(color.rawValue) Sphinx of black quartz", color: color)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(TSSpacing.space1)
                        .background(background(for: color))
                }
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func background(for color: TSTextColor) -> Color {
        switch color {
        case .inverse: TSColor.surfaceInverse.color
        case .onAccent: TSColor.accentPrimary.color
        default: .clear
        }
    }
}
