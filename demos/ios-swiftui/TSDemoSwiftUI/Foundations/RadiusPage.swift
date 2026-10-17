import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct RadiusPage: View {
    private let sampleHeight = TSSize.controlLg

    var body: some View {
        DemoPage(page: .radius) {
            ForEach(TSRadius.allTokens, id: \.name) { token in
                TokenRow(
                    name: token.name,
                    detail: token.isCapsule ? DemoStrings.string(.capsule) : TokenFormat.points(token.value)
                ) {
                    RoundedRectangle(cornerRadius: token.cornerRadius(forHeight: sampleHeight))
                        .fill(TSColor.accentSubtle.color)
                        .overlay(
                            RoundedRectangle(cornerRadius: token.cornerRadius(forHeight: sampleHeight))
                                .stroke(TSColor.accentPrimary.color, lineWidth: 1.5)
                        )
                        .frame(width: TSSpacing.space16 + TSSpacing.space6, height: sampleHeight)
                }
            }
        }
    }
}
