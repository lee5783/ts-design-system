import SwiftUI

struct TSControlChrome<ChromeShape: InsettableShape>: ViewModifier {
    let shape: ChromeShape
    let fill: TSColor?
    let border: TSColor?
    let borderWidth: CGFloat
    let shadow: TSShadowToken?

    func body(content: Content) -> some View {
        content.background(background)
    }

    @ViewBuilder
    private var background: some View {
        let filled = shape
            .fill(fill?.color ?? .clear)
            .overlay(shape.strokeBorder(border?.color ?? .clear, lineWidth: borderWidth))
        if let shadow {
            filled.tsShadow(shadow)
        } else {
            filled
        }
    }
}

struct TSMinimumHitAreaModifier: ViewModifier {
    let outset: CGFloat
    var edges: Edge.Set = .all

    func body(content: Content) -> some View {
        content
            .padding(edges, outset)
            .contentShape(Rectangle())
            .padding(edges, -outset)
    }
}

extension View {
    func tsControlChrome<ChromeShape: InsettableShape>(
        _ shape: ChromeShape,
        fill: TSColor?,
        border: TSColor?,
        borderWidth: CGFloat,
        shadow: TSShadowToken? = nil
    ) -> some View {
        modifier(TSControlChrome(shape: shape, fill: fill, border: border, borderWidth: borderWidth, shadow: shadow))
    }

    func tsMinimumHitArea(outset: CGFloat, edges: Edge.Set = .all) -> some View {
        modifier(TSMinimumHitAreaModifier(outset: outset, edges: edges))
    }
}
