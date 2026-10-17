import SwiftUI
import TSDesignSystem

struct DemoIcon: View {
    let name: TSIconName
    var size: CGFloat = TSSize.iconLg

    var body: some View {
        Group {
            if let image = name.image() {
                Image(uiImage: image)
                    .renderingMode(.template)
                    .resizable()
            } else {
                Color.clear
            }
        }
        .frame(width: size, height: size)
        .flipsForRightToLeftLayoutDirection(name.flipsForRightToLeftLayoutDirection)
        .accessibilityHidden(true)
    }
}
