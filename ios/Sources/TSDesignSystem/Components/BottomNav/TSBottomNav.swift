import SwiftUI

public struct TSBottomNav: View {
    let items: [TSBottomNavItem]
    @Binding var selection: String
    var stateOverrides: [String: TSControlState] = [:]

    @FocusState private var focusedID: String?

    public init(items: [TSBottomNavItem], selection: Binding<String>) {
        self.items = items
        _selection = selection
    }

    public var body: some View {
        let container = TSBottomNavStyleSpec.container
        HStack(spacing: 0) {
            ForEach(items, id: \.id) { item in
                Button {
                    select(item)
                } label: {
                    Text(item.label)
                }
                .buttonStyle(
                    TSBottomNavItemStyle(
                        item: item,
                        isSelected: item.id == selection,
                        isFocused: focusedID == item.id,
                        stateOverride: stateOverrides[item.id]
                    )
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .focused($focusedID, equals: item.id)
                .tsFocusEffectDisabled()
                .accessibilityShowsLargeContentViewer {
                    Label {
                        Text(item.label)
                    } icon: {
                        TSIcon(item.icon, size: .lg)
                    }
                }
            }
        }
        .frame(height: container.height)
        .frame(maxWidth: .infinity)
        .background(
            container.fill.color
                .overlay(
                    Rectangle()
                        .fill(container.border.color)
                        .frame(height: container.borderWidth),
                    alignment: .top
                )
                .edgesIgnoringSafeArea(.bottom)
        )
        .animation(TSAnimation.standard(), value: selection)
        .accessibilityElement(children: .contain)
    }

    func forcingState(_ state: TSControlState, for id: String) -> Self {
        var copy = self
        copy.stateOverrides[id] = state
        return copy
    }

    private func select(_ item: TSBottomNavItem) {
        guard item.id != selection else { return }
        selection = item.id
    }
}
