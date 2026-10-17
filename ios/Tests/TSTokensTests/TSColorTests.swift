import SwiftUI
import Testing
import TSTokens
import UIKit

@MainActor
@Suite("TSColor")
struct TSColorTests {
    private func resolvedRGBA(_ color: UIColor, _ style: UIUserInterfaceStyle) -> TSRGBA {
        let resolved = color.resolvedColor(with: UITraitCollection(userInterfaceStyle: style))
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        resolved.getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        let byte = { (component: CGFloat) in UInt32((component * 255).rounded()) }
        return TSRGBA(hex: byte(red) << 24 | byte(green) << 16 | byte(blue) << 8 | byte(alpha))
    }

    @Test(
        "Sampled dynamic colors resolve to the tokens.json value per theme",
        arguments: ["background-primary", "text-primary", "accent-text", "danger", "focus"]
    )
    func sampledColorsMatchSource(name: String) throws {
        let source = try DesignTokensSource()
        let color = try #require(TSColor.allTokens.first { $0.name == name })
        for (style, theme) in [(UIUserInterfaceStyle.light, "light"), (.dark, "dark")] {
            let expected = try source.rgba(name, theme: theme)
            #expect(color.rgba(for: style) == expected)
            #expect(resolvedRGBA(color.uiColor, style) == expected)
            #expect(resolvedRGBA(UIColor(color.color), style) == expected)
        }
    }

    @Test("Unspecified appearance resolves to the light value")
    func unspecifiedResolvesLight() {
        #expect(resolvedRGBA(TSColor.textPrimary.uiColor, .unspecified) == TSColor.textPrimary.light)
    }

    @Test("Overlay keeps its alpha: 0.40 in light and 0.64 in dark")
    func overlayAlpha() {
        #expect(abs(TSColor.overlay.light.opacity - 0.40) < 0.005)
        #expect(abs(TSColor.overlay.dark.opacity - 0.64) < 0.005)
        let alpha = { (style: UIUserInterfaceStyle) in
            UIColor.tsOverlay.resolvedColor(with: UITraitCollection(userInterfaceStyle: style)).cgColor.alpha
        }
        #expect(abs(alpha(.light) - 0.40) < 0.005)
        #expect(abs(alpha(.dark) - 0.64) < 0.005)
    }

    @Test("Focus is resolved from its accent-text alias")
    func focusAlias() {
        #expect(TSColor.focus.light == TSColor.accentText.light)
        #expect(TSColor.focus.dark == TSColor.accentText.dark)
    }

    @Test("Every source and extension color is generated once, with matching accessors")
    func catalogIsComplete() throws {
        let source = try DesignTokensSource()
        let names = TSColor.allTokens.map(\.name)
        #expect(Set(names).count == names.count)
        #expect(Set(source.colors.keys).isSubset(of: Set(names)))
        #expect(names.contains("danger-pressed"))
        #expect(resolvedRGBA(.tsDangerPressed, .dark) == TSColor.dangerPressed.dark)
        let swiftUIColor: Color = .tsDangerPressed
        #expect(resolvedRGBA(UIColor(swiftUIColor), .dark) == TSColor.dangerPressed.dark)
        #expect(resolvedRGBA(UIColor(swiftUIColor), .light) == TSColor.dangerPressed.light)
    }

    @Test("TSRGBA round-trips its packed hex value")
    func rgbaRoundTrip() {
        let rgba = TSRGBA(hex: 0x18181B66)
        #expect([rgba.red, rgba.green, rgba.blue, rgba.alpha] == [0x18, 0x18, 0x1B, 0x66])
        #expect(rgba.hex == 0x18181B66)
    }
}
