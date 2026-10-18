import SwiftUI

public struct TSTabs: View {
    let items: [TSTabItem]
    @Binding var selection: String
    let variant: TSTabsVariant
    let isBlock: Bool
    var stateOverrides: [String: TSControlState] = [:]
    var playHaptic: @MainActor (TSHapticEvent) -> Void = { TSHaptics.shared.play($0) }

    @FocusState private var focusedID: String?
    @Namespace private var namespace
    @Environment(\.isEnabled) private var isEnabled

    public init(
        items: [TSTabItem],
        selection: Binding<String>,
        variant: TSTabsVariant = .underline,
        isBlock: Bool = false
    ) {
        self.items = items
        _selection = selection
        self.variant = variant
        self.isBlock = isBlock
    }

    public var body: some View {
        let container = TSTabsStyleSpec.container(variant, isEnabled: isEnabled)
        Group {
            switch variant {
            case .underline:
                underline(container)
            case .segmented:
                segmented(container)
            }
        }
        .animation(TSAnimation.standard(), value: selection)
        .accessibilityElement(children: .contain)
    }

    func forcingState(_ state: TSControlState, for id: String) -> Self {
        var copy = self
        copy.stateOverrides[id] = state
        return copy
    }

    func playingHaptics(with playHaptic: @escaping @MainActor (TSHapticEvent) -> Void) -> Self {
        var copy = self
        copy.playHaptic = playHaptic
        return copy
    }

    private func underline(_ container: TSTabsStyleSpec.Container) -> some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: container.spacing) {
                    tabs
                }
            }
            .fixedSize(horizontal: false, vertical: true)
            .onAppear { proxy.scrollTo(selection) }
            .tsOnChange(of: selection) { _, newValue in
                withAnimation(TSAnimation.standard()) {
                    proxy.scrollTo(newValue, anchor: .center)
                }
            }
        }
        .frame(minHeight: container.minHeight)
        .background(
            Rectangle()
                .fill(container.border.color)
                .frame(height: container.borderWidth)
                .opacity(container.opacity),
            alignment: .bottom
        )
    }

    private func segmented(_ container: TSTabsStyleSpec.Container) -> some View {
        let track = RoundedRectangle(cornerRadius: container.cornerRadius, style: .continuous)
        return HStack(spacing: container.spacing) {
            tabs
        }
        .fixedSize(horizontal: false, vertical: true)
        .padding(container.padding + container.borderWidth)
        .frame(minHeight: container.minHeight)
        .background(
            track
                .fill(container.fill?.color ?? .clear)
                .overlay(track.strokeBorder(container.border.color, lineWidth: container.borderWidth))
                .opacity(container.opacity)
        )
    }

    private var tabs: some View {
        ForEach(items, id: \.id) { item in
            Button {
                select(item)
            } label: {
                Text(item.label)
            }
            .buttonStyle(
                TSTabStyle(
                    variant: variant,
                    isSelected: item.id == selection,
                    isBlock: isBlock,
                    isFocused: focusedID == item.id,
                    stateOverride: stateOverrides[item.id],
                    namespace: namespace
                )
            )
            .focused($focusedID, equals: item.id)
            .tsFocusEffectDisabled()
            .modifier(TSTabLargeContent(label: item.label, isEnabled: variant == .segmented))
            .id(item.id)
        }
    }

    private func select(_ item: TSTabItem) {
        guard item.id != selection else { return }
        selection = item.id
        if variant == .segmented {
            playHaptic(.selection)
        }
    }
}

struct TSTabLargeContent: ViewModifier {
    let label: String
    let isEnabled: Bool

    func body(content: Content) -> some View {
        if isEnabled {
            content.accessibilityShowsLargeContentViewer {
                Text(label)
            }
        } else {
            content
        }
    }
}
