import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct SpacingPage: View {
    var body: some View {
        DemoPage(page: .spacing) {
            ForEach(TSSpacing.allTokens, id: \.name) { token in
                TokenRow(name: token.name, detail: TokenFormat.points(token.value)) {
                    HStack(spacing: 0) {
                        RoundedRectangle(cornerRadius: TSRadius.radiusXs)
                            .fill(TSColor.accentPrimary.color)
                            .frame(width: token.value, height: TSSize.iconMd)
                        Spacer(minLength: 0)
                    }
                    .frame(width: TSSpacing.space16)
                }
            }
        }
    }
}
