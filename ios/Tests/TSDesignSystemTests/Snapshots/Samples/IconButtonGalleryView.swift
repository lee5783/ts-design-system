import SwiftUI
@testable import TSDesignSystem

struct IconButtonGalleryView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space4) {
            ForEach(TSIconButtonVariant.allCases, id: \.self) { variant in
                ForEach([false, true], id: \.self) { isRound in
                    HStack(spacing: TSSpacing.space4) {
                        TSIconButton(.search, label: "Search", variant: variant, isRound: isRound) {}
                        TSIconButton(.plus, label: "Add", variant: variant, isRound: isRound) {}
                            .forcingState(.pressed)
                        TSIconButton(.trash, label: "Delete", variant: variant, isRound: isRound) {}
                            .disabled(true)
                        TSIconButton(.chevronRight, label: "Next", variant: variant, isRound: isRound) {}
                            .forcingState(.focused)
                    }
                }
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
