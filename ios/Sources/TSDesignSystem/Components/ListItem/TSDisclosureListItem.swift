import SwiftUI

public struct TSDisclosureListItem<Leading: View, Content: View>: View {
    let title: String
    let subtitle: String?
    @Binding var isExpanded: Bool
    let leading: Leading
    let content: Content
    var stateOverride: TSControlState?

    @FocusState private var isFocused: Bool
    @Environment(\.isEnabled) private var isEnabled

    public init(
        _ title: String,
        subtitle: String? = nil,
        isExpanded: Binding<Bool>,
        @ViewBuilder leading: () -> Leading,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.subtitle = subtitle
        _isExpanded = isExpanded
        self.leading = leading()
        self.content = content()
    }

    public var body: some View {
        let disclosure = TSListItemStyleSpec.disclosure
        VStack(spacing: 0) {
            Button(action: toggle) {
                TSListItemContent(
                    title: title,
                    subtitle: subtitle,
                    showsChevron: true,
                    leading: leading,
                    trailing: EmptyView(),
                    chevronRotation: disclosure.rotationDegrees(isExpanded: isExpanded, isRightToLeft: false)
                )
            }
            .buttonStyle(TSListItemStyle(isFocused: isFocused, stateOverride: stateOverride))
            .focused($isFocused)
            .tsFocusEffectDisabled()
            .accessibilityValue(Text(TSListItemAccessibility.expansionValue(isExpanded: isExpanded)))
            if isExpanded {
                content
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, disclosure.bodyHorizontalPadding)
                    .padding(.bottom, disclosure.bodyBottomPadding)
                    .opacity(isEnabled ? 1 : TSOpacity.opacityDisabled)
            }
        }
    }

    func forcingState(_ state: TSControlState) -> Self {
        var copy = self
        copy.stateOverride = state
        return copy
    }

    func toggle() {
        guard isEnabled else { return }
        isExpanded.toggle()
    }
}

public extension TSDisclosureListItem where Leading == EmptyView {
    init(_ title: String, subtitle: String? = nil, isExpanded: Binding<Bool>, @ViewBuilder content: () -> Content) {
        self.init(title, subtitle: subtitle, isExpanded: isExpanded, leading: { EmptyView() }, content: content)
    }
}
