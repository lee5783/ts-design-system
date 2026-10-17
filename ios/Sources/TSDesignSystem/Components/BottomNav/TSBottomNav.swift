import SwiftUI

public struct TSBottomNav: View {
    let items: [TSBottomNavItem]
    @Binding var selection: String
    let label: String?
    var stateOverrides: [String: TSControlState] = [:]

    @FocusState private var focusedID: String?
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    public init(items: [TSBottomNavItem], selection: Binding<String>, label: String? = nil) {
        self.items = items
        _selection = selection
        self.label = label
    }

    public var body: some View {
        let container = TSBottomNavStyleSpec.container
        HStack(spacing: 0) {
            ForEach(Array(items.enumerated()), id: \.element.id) { index, item in
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
                .accessibilityLabel(Text(item.label))
                .accessibilityValue(Text(TSBottomNavAccessibility.value(index: index, count: items.count, badge: item.badge)))
                .accessibilityRemoveTraits(.isButton)
                .accessibilityAddTraits(item.id == selection ? .isSelected : [])
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
        .animation(TSReduceMotion.animation(reduceMotion: reduceMotion), value: selection)
        .modifier(TSTabListAccessibility(label: label))
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
