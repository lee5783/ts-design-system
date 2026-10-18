import SwiftUI

public struct TSListItem<Leading: View, Trailing: View>: View {
    let title: String
    let subtitle: String?
    let showsChevron: Bool
    let action: (() -> Void)?
    let toggle: Binding<Bool>?
    let leading: Leading
    let trailing: Trailing
    var stateOverride: TSControlState?

    @FocusState private var isFocused: Bool
    @Environment(\.isEnabled) private var isEnabled

    public init(
        _ title: String,
        subtitle: String? = nil,
        showsChevron: Bool = false,
        action: (() -> Void)? = nil,
        @ViewBuilder leading: () -> Leading,
        @ViewBuilder trailing: () -> Trailing
    ) {
        self.title = title
        self.subtitle = subtitle
        self.showsChevron = showsChevron
        self.action = action
        self.toggle = nil
        self.leading = leading()
        self.trailing = trailing()
    }

    init(title: String, subtitle: String?, toggle: Binding<Bool>, leading: Leading, trailing: Trailing) {
        self.title = title
        self.subtitle = subtitle
        self.showsChevron = false
        self.action = nil
        self.toggle = toggle
        self.leading = leading
        self.trailing = trailing
    }

    var role: TSListItemRole {
        .make(hasAction: action != nil, showsChevron: showsChevron, hasToggle: toggle != nil)
    }

    public var body: some View {
        switch role {
        case .action, .navigation:
            pressableRow
        case .toggle:
            toggleRow
        case .plain:
            plainRow
        }
    }

    func forcingState(_ state: TSControlState) -> Self {
        var copy = self
        copy.stateOverride = state
        return copy
    }

    private var content: TSListItemContent<Leading, Trailing> {
        TSListItemContent(title: title, subtitle: subtitle, showsChevron: showsChevron, leading: leading, trailing: trailing)
    }

    private var pressableRow: some View {
        Button(action: perform) {
            content
        }
        .buttonStyle(TSListItemStyle(isFocused: isFocused, stateOverride: stateOverride))
        .focused($isFocused)
        .tsFocusEffectDisabled()
    }

    private var toggleRow: some View {
        content
            .modifier(TSListItemChrome(state: stateOverride ?? .make(isEnabled: isEnabled), isInteractive: false))
    }

    private var plainRow: some View {
        content
            .modifier(TSListItemChrome(state: stateOverride ?? .make(isEnabled: isEnabled), isInteractive: false))
    }

    private func perform() {
        action?()
    }
}

public extension TSListItem where Leading == EmptyView, Trailing == EmptyView {
    init(_ title: String, subtitle: String? = nil, showsChevron: Bool = false, action: (() -> Void)? = nil) {
        self.init(title, subtitle: subtitle, showsChevron: showsChevron, action: action, leading: { EmptyView() }, trailing: { EmptyView() })
    }
}

public extension TSListItem where Trailing == EmptyView {
    init(
        _ title: String,
        subtitle: String? = nil,
        showsChevron: Bool = false,
        action: (() -> Void)? = nil,
        @ViewBuilder leading: () -> Leading
    ) {
        self.init(title, subtitle: subtitle, showsChevron: showsChevron, action: action, leading: leading, trailing: { EmptyView() })
    }
}

public extension TSListItem where Leading == EmptyView {
    init(
        _ title: String,
        subtitle: String? = nil,
        showsChevron: Bool = false,
        action: (() -> Void)? = nil,
        @ViewBuilder trailing: () -> Trailing
    ) {
        self.init(title, subtitle: subtitle, showsChevron: showsChevron, action: action, leading: { EmptyView() }, trailing: trailing)
    }
}

public extension TSListItem where Trailing == TSListItemSwitch {
    init(_ title: String, subtitle: String? = nil, isOn: Binding<Bool>, @ViewBuilder leading: () -> Leading) {
        self.init(title: title, subtitle: subtitle, toggle: isOn, leading: leading(), trailing: TSListItemSwitch(title, isOn: isOn))
    }
}

public extension TSListItem where Leading == EmptyView, Trailing == TSListItemSwitch {
    init(_ title: String, subtitle: String? = nil, isOn: Binding<Bool>) {
        self.init(title: title, subtitle: subtitle, toggle: isOn, leading: EmptyView(), trailing: TSListItemSwitch(title, isOn: isOn))
    }
}
