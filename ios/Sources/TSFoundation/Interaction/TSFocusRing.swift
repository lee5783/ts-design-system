import SwiftUI
import TSTokens
import UIKit

public enum TSFocusRing {
    public static var gap: CGFloat {
        TSShadowTokens.shadowFocus.light.first?.spread ?? 0
    }

    public static var width: CGFloat {
        (TSShadowTokens.shadowFocus.light.last?.spread ?? 0) - gap
    }

    public static var outset: CGFloat {
        gap + width
    }

    public static let gapColor = TSColor.backgroundPrimary
    public static let ringColor = TSColor.focus

    public static func ringCornerRadius(for cornerRadius: CGFloat) -> CGFloat {
        cornerRadius + outset
    }

    @MainActor
    public static func haloEffect(for view: UIView, cornerRadius: CGFloat) -> UIFocusHaloEffect {
        let effect = UIFocusHaloEffect(roundedRect: view.bounds, cornerRadius: cornerRadius, curve: .continuous)
        effect.referenceView = view
        effect.containerView = view.superview
        effect.position = .outside
        return effect
    }

    @MainActor
    public static func applyHalo(to view: UIView, cornerRadius: CGFloat) {
        view.focusEffect = haloEffect(for: view, cornerRadius: cornerRadius)
    }
}

public extension View {
    func tsFocusRing(cornerRadius: CGFloat) -> some View {
        modifier(TSFocusRingModifier(cornerRadius: cornerRadius))
    }

    func tsFocusRing(isFocused: Bool, cornerRadius: CGFloat, style: RoundedCornerStyle = .continuous) -> some View {
        modifier(TSFocusRingOverlay(isFocused: isFocused, cornerRadius: cornerRadius, style: style))
    }
}

struct TSFocusRingModifier: ViewModifier {
    let cornerRadius: CGFloat

    @FocusState private var isFocused: Bool

    func body(content: Content) -> some View {
        content
            .focused($isFocused)
            .modifier(TSFocusRingOverlay(isFocused: isFocused, cornerRadius: cornerRadius))
    }
}

struct TSFocusRingOverlay: ViewModifier {
    let isFocused: Bool
    let cornerRadius: CGFloat
    var style: RoundedCornerStyle = .continuous

    func body(content: Content) -> some View {
        content
            .tsFocusEffectDisabled()
            .overlay(
                ZStack {
                    RoundedRectangle(cornerRadius: TSFocusRing.ringCornerRadius(for: cornerRadius), style: style)
                        .strokeBorder(TSFocusRing.ringColor.color, lineWidth: TSFocusRing.width)
                    RoundedRectangle(cornerRadius: cornerRadius + TSFocusRing.gap, style: style)
                        .strokeBorder(TSFocusRing.gapColor.color, lineWidth: TSFocusRing.gap)
                        .padding(TSFocusRing.width)
                }
                .padding(-TSFocusRing.outset)
                .opacity(isFocused ? 1 : 0)
                .allowsHitTesting(false)
                .accessibilityHidden(true)
            )
    }
}
