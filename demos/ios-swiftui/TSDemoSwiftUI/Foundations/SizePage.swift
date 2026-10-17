import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct SizePage: View {
    private let visualLimit = TSSpacing.space16

    var body: some View {
        DemoPage(page: .size) {
            ForEach(TSSize.allTokens, id: \.name) { token in
                TokenRow(name: token.name, detail: TokenFormat.points(token.value)) {
                    ZStack {
                        RoundedRectangle(cornerRadius: TSRadius.radiusXs)
                            .stroke(TSColor.accentPrimary.color, style: StrokeStyle(lineWidth: 1, dash: token.value > visualLimit ? [4, 3] : []))
                            .background(
                                RoundedRectangle(cornerRadius: TSRadius.radiusXs)
                                    .fill(TSColor.accentSubtle.color)
                            )
                            .frame(width: min(token.value, visualLimit), height: min(token.value, visualLimit))
                    }
                    .frame(width: visualLimit, height: visualLimit)
                }
            }
        }
    }
}
