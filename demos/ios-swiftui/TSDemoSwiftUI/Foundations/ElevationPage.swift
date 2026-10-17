import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct ElevationPage: View {
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        DemoPage(page: .elevation) {
            LazyVGrid(columns: [GridItem(.adaptive(minimum: dynamicTypeSize.isAccessibilitySize ? 280 : 140), spacing: TSSpacing.space6)], spacing: TSSpacing.space6) {
                ForEach(TSShadowTokens.allTokens, id: \.name) { token in
                    VStack(alignment: .leading, spacing: TSSpacing.space2) {
                        Text(token.name)
                            .foregroundColor(TSColor.textPrimary.color)
                            .tsFont(.label)
                        Text("\(DemoStrings.string(.lightAppearance)) \(token.light.count) · \(DemoStrings.string(.darkAppearance)) \(token.dark.count)")
                            .foregroundColor(TSColor.textMuted.color)
                            .tsFont(.code)
                    }
                    .padding(TSSpacing.space4)
                    .frame(maxWidth: .infinity, minHeight: TSSpacing.space16 + TSSpacing.space8, alignment: .topLeading)
                    .background(
                        RoundedRectangle(cornerRadius: TSRadius.radiusLg)
                            .fill(TSColor.surfaceCard.color)
                            .tsShadow(token)
                    )
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier("demo.token.\(token.name)")
                }
            }
            .padding(.vertical, TSSpacing.space2)
        }
    }
}
