import SwiftUI
import TSTokens
import UIKit

public enum TSShadow {
    public static func layers(_ token: TSShadowToken, for colorScheme: ColorScheme) -> [TSShadowLayer] {
        token.layers(for: colorScheme == .dark ? .dark : .light)
    }

    public static func shadowRect(for bounds: CGRect, spread: CGFloat) -> CGRect {
        bounds.insetBy(dx: -spread, dy: -spread)
    }

    public static func shadowPath(for bounds: CGRect, cornerRadius: CGFloat, spread: CGFloat) -> CGPath {
        let rect = shadowRect(for: bounds, spread: spread)
        let radius = max(0, min(cornerRadius + spread, min(rect.width, rect.height) / 2))
        return UIBezierPath(roundedRect: rect, cornerRadius: radius).cgPath
    }

    @MainActor
    public static func apply(_ shadowLayer: TSShadowLayer?, to layer: CALayer, cornerRadius: CGFloat) {
        guard let shadowLayer else {
            layer.shadowOpacity = 0
            layer.shadowPath = nil
            return
        }
        let color = shadowLayer.color
        layer.shadowColor = TSRGBA(hex: color.hex | 0xFF).uiColor.cgColor
        layer.shadowOpacity = shadowLayer.opacity
        layer.shadowRadius = shadowLayer.radius
        layer.shadowOffset = shadowLayer.offset
        layer.shadowPath = shadowPath(for: layer.bounds, cornerRadius: cornerRadius, spread: shadowLayer.spread)
    }

    @MainActor
    public static func apply(
        _ token: TSShadowToken,
        style: UIUserInterfaceStyle,
        to layers: [CALayer],
        cornerRadius: CGFloat
    ) {
        let shadowLayers = token.layers(for: style)
        for (index, layer) in layers.enumerated() {
            apply(index < shadowLayers.count ? shadowLayers[index] : nil, to: layer, cornerRadius: cornerRadius)
        }
    }
}

public extension View {
    func tsShadow(_ token: TSShadowToken) -> some View {
        modifier(TSShadowModifier(token: token))
    }
}

struct TSShadowModifier: ViewModifier {
    let token: TSShadowToken

    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        let layers = TSShadow.layers(token, for: colorScheme)
        content
            .shadow(layers.indices.contains(0) ? layers[0] : nil)
            .shadow(layers.indices.contains(1) ? layers[1] : nil)
    }
}

private extension View {
    func shadow(_ layer: TSShadowLayer?) -> some View {
        shadow(
            color: layer.map { Color(uiColor: $0.color.uiColor) } ?? .clear,
            radius: layer?.radius ?? 0,
            x: layer?.offsetX ?? 0,
            y: layer?.offsetY ?? 0
        )
    }
}
