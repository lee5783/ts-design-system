import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct OpacityPage: View {
    var body: some View {
        DemoPage(page: .opacity) {
            ForEach(TSOpacity.allTokens, id: \.name) { token in
                TokenRow(name: token.name, detail: "\(Int((token.value * 100).rounded()))%") {
                    HStack(spacing: TSSpacing.space1) {
                        RoundedRectangle(cornerRadius: TSRadius.radiusMd)
                            .fill(TSColor.accentPrimary.color)
                        RoundedRectangle(cornerRadius: TSRadius.radiusMd)
                            .fill(TSColor.accentPrimary.color)
                            .opacity(token.value)
                    }
                    .frame(width: TSSpacing.space16 + TSSpacing.space6, height: TSSize.controlMd)
                }
            }
        }
    }
}
