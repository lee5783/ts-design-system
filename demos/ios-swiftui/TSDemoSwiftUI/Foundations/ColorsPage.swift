import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct ColorsPage: View {
    var body: some View {
        DemoPage(page: .colors) {
            ForEach(TSColor.allTokens, id: \.name) { token in
                TokenRow(
                    name: token.name,
                    detail: "\(DemoStrings.string(.lightAppearance)) \(TokenFormat.hex(token.light)) · \(DemoStrings.string(.darkAppearance)) \(TokenFormat.hex(token.dark))"
                ) {
                    RoundedRectangle(cornerRadius: TSRadius.radiusMd)
                        .fill(token.color)
                        .overlay(
                            RoundedRectangle(cornerRadius: TSRadius.radiusMd)
                                .stroke(TSColor.borderSubtle.color, lineWidth: 1)
                        )
                        .frame(width: TSSize.controlMd, height: TSSize.controlMd)
                }
            }
        }
    }
}
