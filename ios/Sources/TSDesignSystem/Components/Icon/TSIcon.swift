import SwiftUI

public struct TSIcon: View {
    let name: TSIconName
    let size: TSIconSize
    var pointSize: CGFloat?

    public init(_ name: TSIconName, size: TSIconSize = .md) {
        self.name = name
        self.size = size
    }

    init(_ name: TSIconName, pointSize: CGFloat) {
        self.init(name)
        self.pointSize = pointSize
    }

    public var body: some View {
        let resolved = TSIconStyleSpec.resolve(.init(size: size))
        let side = pointSize ?? resolved.pointSize
        image
            .frame(width: side, height: side)
            .flipsForRightToLeftLayoutDirection(name.flipsForRightToLeftLayoutDirection)
            .accessibilityHidden(true)
    }

    @ViewBuilder
    private var image: some View {
        if let uiImage = name.image() {
            Image(uiImage: uiImage)
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
        } else {
            Color.clear
        }
    }
}
