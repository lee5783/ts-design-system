import SwiftUI

public struct TSDivider: View {
    public init() {}

    public var body: some View {
        let group = TSListItemStyleSpec.group
        Rectangle()
            .fill(group.divider.color)
            .frame(height: group.dividerThickness)
            .padding(.leading, group.dividerInset)
            .frame(maxWidth: .infinity)
            .accessibilityHidden(true)
    }
}
