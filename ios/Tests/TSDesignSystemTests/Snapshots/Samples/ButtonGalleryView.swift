import SwiftUI
@testable import TSDesignSystem

struct ButtonGalleryView: View {
    let variant: TSButtonVariant

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            ForEach(TSButtonSize.allCases, id: \.self) { size in
                TSButton("Default", variant: variant, size: size) {}
                TSButton("Pressed", variant: variant, size: size) {}
                    .forcingState(.pressed)
                TSButton("Disabled", variant: variant, size: size) {}
                    .disabled(true)
                TSButton("Loading", variant: variant, size: size, isLoading: true) {}
                TSButton("Focused", variant: variant, size: size) {}
                    .forcingState(.focused)
            }
            TSButton("Leading icon", variant: variant, leadingIcon: .plus) {}
            TSButton("Trailing icon", variant: variant, trailingIcon: .arrowRight) {}
            TSButton("Both icons", variant: variant, leadingIcon: .download, trailingIcon: .chevronRight) {}
            TSButton("Block", variant: variant, size: .lg, isBlock: true) {}
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
