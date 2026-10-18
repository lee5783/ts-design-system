import SwiftUI
import TSDesignSystem

struct SpinnerGalleryView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            ForEach(TSSpinnerTone.allCases, id: \.self) { tone in
                HStack(spacing: TSSpacing.space4) {
                    ForEach(TSSpinnerSize.allCases, id: \.self) { size in
                        TSSpinner(size: size, tone: tone)
                    }
                    TSText(tone.rawValue, role: .bodySm, color: .muted)
                }
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
