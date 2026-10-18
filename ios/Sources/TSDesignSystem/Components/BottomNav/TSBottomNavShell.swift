import SwiftUI

public struct TSBottomNavShell<Content: View>: View {
    let items: [TSBottomNavItem]
    @Binding var selection: String
    let content: (TSBottomNavItem) -> Content

    public init(
        items: [TSBottomNavItem],
        selection: Binding<String>,
        @ViewBuilder content: @escaping (TSBottomNavItem) -> Content
    ) {
        self.items = items
        _selection = selection
        self.content = content
    }

    public var body: some View {
        ZStack {
            ForEach(items) { item in
                let isSelected = item.id == selection
                content(item)
                    .opacity(isSelected ? 1 : 0)
                    .allowsHitTesting(isSelected)
                    .accessibilityHidden(!isSelected)
            }
        }
        .safeAreaInset(edge: .bottom, spacing: 0) {
            TSBottomNav(items: items, selection: $selection)
        }
    }
}
