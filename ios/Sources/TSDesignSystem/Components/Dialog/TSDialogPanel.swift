import SwiftUI
import UIKit

struct TSDialogPanel<Content: View>: View {
    let title: String
    let variant: TSDialogVariant
    let actions: [TSDialogAction]
    let hasBody: Bool
    let onAction: (TSDialogAction) -> Void
    let onClose: () -> Void
    let content: Content
    var focusesTitle = true

    @AccessibilityFocusState private var isTitleFocused: Bool
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var resolved: TSDialogStyleSpec.Resolved {
        TSDialogStyleSpec.resolve(.init(variant: variant, isAccessibilitySize: dynamicTypeSize.isAccessibilitySize))
    }

    var body: some View {
        let resolved = resolved
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .foregroundColor(resolved.titleColor.color)
                .fixedSize(horizontal: false, vertical: true)
                .tsFont(resolved.titleFont)
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityAddTraits(.isHeader)
                .accessibilityFocused($isTitleFocused)
            if hasBody {
                content
                    .foregroundColor(resolved.bodyColor.color)
                    .tsFont(resolved.bodyFont)
                    .fixedSize(horizontal: false, vertical: true)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, resolved.titleSpacing)
            }
            if !actions.isEmpty {
                TSDialogActions(
                    actions: TSDialogLayout.orderedActions(actions, primaryFirst: resolved.primaryActionFirst),
                    resolved: resolved,
                    onAction: onAction
                )
                .padding(.top, resolved.actionsTopSpacing)
            }
        }
        .padding(.top, resolved.contentTopInset)
        .padding(.horizontal, resolved.paddingHorizontal)
        .padding(.bottom, resolved.paddingBottom)
        .frame(maxWidth: resolved.maxWidth ?? .infinity)
        .background(TSDialogPanelBackground(resolved: resolved))
        .accessibilityElement(children: .contain)
        .accessibilityAddTraits(.isModal)
        .modifier(TSDialogEscape(onClose: onClose))
        .task {
            guard focusesTitle else { return }
            try? await Task.sleep(nanoseconds: 350_000_000)
            isTitleFocused = true
        }
    }
}

struct TSDialogEscape: ViewModifier {
    let onClose: () -> Void

    func body(content: Content) -> some View {
        content
            .accessibilityAction(.escape, onClose)
            .accessibilityAction(named: Text(TSStrings.close), onClose)
            .background(TSDialogEscapeKey(onClose: onClose))
    }
}

struct TSDialogEscapeKey: View {
    let onClose: () -> Void

    var body: some View {
        Button(action: onClose) {
            EmptyView()
        }
        .keyboardShortcut(.cancelAction)
        .frame(width: 0, height: 0)
        .opacity(0)
        .accessibilityHidden(true)
    }
}

struct TSDialogActions: View {
    let actions: [TSDialogAction]
    let resolved: TSDialogStyleSpec.Resolved
    let onAction: (TSDialogAction) -> Void

    var body: some View {
        if resolved.actionsAxis == .horizontal {
            HStack(spacing: resolved.actionsSpacing) {
                Spacer(minLength: 0)
                buttons
            }
        } else {
            VStack(spacing: resolved.actionsSpacing) {
                buttons
            }
        }
    }

    private var buttons: some View {
        ForEach(Array(actions.enumerated()), id: \.offset) { _, action in
            TSButton(action.label, variant: action.variant, isBlock: resolved.blockActions) {
                onAction(action)
            }
        }
    }
}

struct TSDialogPanelBackground: View {
    let resolved: TSDialogStyleSpec.Resolved

    var body: some View {
        let shape = TSDialogPanelShape(cornerRadius: resolved.cornerRadius, roundsBottomCorners: resolved.roundsBottomCorners)
        if let shadow = resolved.shadow {
            shape.fill(resolved.fill.color).tsShadow(shadow)
        } else {
            shape.fill(resolved.fill.color)
        }
    }
}

struct TSDialogPanelShape: Shape {
    let cornerRadius: CGFloat
    let roundsBottomCorners: Bool

    func path(in rect: CGRect) -> Path {
        let corners: UIRectCorner = roundsBottomCorners ? .allCorners : [.topLeft, .topRight]
        let radii = CGSize(width: cornerRadius, height: cornerRadius)
        return Path(UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: radii).cgPath)
    }
}

struct TSDialogCenterContainer<Panel: View>: View {
    let resolved: TSDialogStyleSpec.Resolved
    let onScrimTap: () -> Void
    let panel: Panel

    var body: some View {
        GeometryReader { proxy in
            ScrollView {
                ZStack {
                    Color.clear
                        .contentShape(Rectangle())
                        .onTapGesture(perform: onScrimTap)
                        .accessibilityHidden(true)
                    panel
                        .padding(resolved.outerMargin)
                }
                .frame(width: proxy.size.width)
                .frame(minHeight: proxy.size.height)
            }
            .clipped()
        }
        .background(resolved.scrim.color.edgesIgnoringSafeArea(.all))
    }
}

struct TSDialogSheetContainer<Panel: View>: View {
    let onHeightChange: (CGFloat) -> Void
    let panel: Panel

    var body: some View {
        ScrollView {
            panel
                .background(
                    GeometryReader { proxy in
                        Color.clear.preference(key: TSDialogHeightKey.self, value: proxy.size.height)
                    }
                )
        }
        .onPreferenceChange(TSDialogHeightKey.self, perform: onHeightChange)
    }
}

struct TSDialogHeightKey: PreferenceKey {
    static let defaultValue: CGFloat = 0

    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = max(value, nextValue())
    }
}
