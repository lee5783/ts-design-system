import SwiftUI
import TSTestSupport
@testable import TSDesignSystem

struct ButtonCustomIconGalleryView: View {
    let variant: TSButtonVariant

    private let controller = Image(uiImage: TSSampleIconImage.controller())
    private let largeController = Image(uiImage: TSSampleIconImage.controller(side: 48))
    private let longTitle = "Join the session with a very long label"

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            ForEach(TSButtonSize.allCases, id: \.self) { size in
                HStack(spacing: TSSpacing.space2) {
                    TSButton(size.rawValue, variant: variant, size: size, leadingIcon: .plus) {}
                    TSButton(size.rawValue, variant: variant, size: size, leadingIconSource: .image(controller)) {}
                }
            }
            TSButton("Pressed", variant: variant, leadingIconSource: .image(controller), trailingIconSource: .image(controller)) {}
                .forcingState(.pressed)
            TSButton("Disabled", variant: variant, leadingIconSource: .image(controller), trailingIconSource: .image(controller)) {}
                .disabled(true)
            TSButton("Loading", variant: variant, leadingIconSource: .image(controller), trailingIconSource: .image(controller), isLoading: true) {}
            TSButton("Large image", variant: variant, leadingIconSource: .image(largeController)) {}
            TSButton(longTitle, variant: variant, leadingIconSource: .image(controller)) {}
                .lineLimit(1)
                .frame(width: 220)
            TSButton(longTitle, variant: variant, leadingIconSource: .image(controller)) {}
                .frame(width: 220)
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct IconButtonCustomIconGalleryView: View {
    private let controller = Image(uiImage: TSSampleIconImage.controller())

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space4) {
            ForEach(TSIconButtonVariant.allCases, id: \.self) { variant in
                HStack(spacing: TSSpacing.space4) {
                    TSIconButton(.plus, label: "Add", variant: variant) {}
                    TSIconButton(.image(controller), label: "Controller", variant: variant) {}
                    TSIconButton(.image(controller), label: "Controller", variant: variant) {}
                        .forcingState(.pressed)
                    TSIconButton(.image(controller), label: "Controller", variant: variant) {}
                        .disabled(true)
                    TSIconButton(.image(controller), label: "Controller", variant: variant, isRound: true) {}
                        .forcingState(.focused)
                }
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
