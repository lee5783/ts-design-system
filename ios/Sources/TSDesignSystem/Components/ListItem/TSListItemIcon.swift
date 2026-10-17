import SwiftUI

public struct TSListItemIcon: View {
    let name: TSIconName

    public init(_ name: TSIconName) {
        self.name = name
    }

    public var body: some View {
        let tile = TSListItemStyleSpec.tile
        TSIcon(name, size: tile.iconSize)
            .foregroundColor(tile.foreground.color)
            .frame(width: tile.size, height: tile.size)
            .background(
                RoundedRectangle(cornerRadius: tile.cornerRadius, style: .continuous)
                    .fill(tile.fill.color)
            )
            .accessibilityHidden(true)
    }
}
