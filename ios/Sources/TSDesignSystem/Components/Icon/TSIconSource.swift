import SwiftUI

public enum TSIconSource {
    case ts(TSIconName)
    case image(Image)
}

struct TSIconSourceView: View {
    let source: TSIconSource
    let pointSize: CGFloat

    var body: some View {
        switch source {
        case .ts(let name):
            TSIcon(name, pointSize: pointSize)
        case .image(let image):
            image
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: pointSize, height: pointSize)
                .accessibilityHidden(true)
        }
    }
}
