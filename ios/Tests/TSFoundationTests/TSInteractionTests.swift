import SwiftUI
import Testing
import TSTokens
import UIKit
@testable import TSFoundation

@Suite("Interaction primitives")
struct TSInteractionTests {
    @Test("The focus ring geometry comes from the shadow-focus token: 2pt gap, 2pt ring")
    func focusRingGeometry() {
        #expect(TSFocusRing.gap == 2)
        #expect(TSFocusRing.width == 2)
        #expect(TSFocusRing.outset == 4)
        #expect(TSFocusRing.ringCornerRadius(for: 8) == 12)
        #expect(TSFocusRing.ringColor == TSColor.focus)
        #expect(TSFocusRing.gapColor == TSColor.backgroundPrimary)
    }

    @Test("The UIKit focus halo is an outside, continuous halo on the view")
    @MainActor
    func focusHalo() {
        let container = UIView()
        let view = UIView(frame: CGRect(x: 0, y: 0, width: 100, height: 44))
        container.addSubview(view)
        TSFocusRing.applyHalo(to: view, cornerRadius: 8)
        let halo = view.focusEffect as? UIFocusHaloEffect
        #expect(halo != nil)
        #expect(halo?.position == .outside)
        #expect(halo?.containerView === container)
    }

    @Test("Opacity press feedback uses opacity-pressed; fill feedback swaps the color")
    func pressFeedback() {
        #expect(TSPressFeedback.opacity.opacity(isPressed: true) == TSOpacity.opacityPressed)
        #expect(TSPressFeedback.opacity.opacity(isPressed: false) == 1)
        #expect(TSPressFeedback.fill.opacity(isPressed: true) == 1)
        #expect(TSPressFeedback.fill.color(.accentPrimary, pressed: .accentPressed, isPressed: true) == .accentPressed)
        #expect(TSPressFeedback.fill.color(.accentPrimary, pressed: .accentPressed, isPressed: false) == .accentPrimary)
        #expect(TSPressFeedback.opacity.color(.accentPrimary, pressed: .accentPressed, isPressed: true) == .accentPrimary)
        #expect(TSPressFeedback.duration == TSMotion.durationFast)
    }

    @Test("Haptics map to UIFeedbackGenerator types")
    func hapticMapping() {
        #expect(TSHapticEvent.selection.notificationType == nil)
        #expect(TSHapticEvent.success.notificationType == .success)
        #expect(TSHapticEvent.warning.notificationType == .warning)
        #expect(TSHapticEvent.error.notificationType == .error)
    }

    @Test("Haptics are opt-out")
    @MainActor
    func hapticsOptOut() {
        var played: [TSHapticEvent] = []
        let haptics = TSHaptics { played.append($0) }
        haptics.play(.selection)
        haptics.isEnabled = false
        haptics.play(.success)
        #expect(played == [.selection])
    }

    @Test("System haptics play without crashing for every event", arguments: TSHapticEvent.allCases)
    @MainActor
    func systemHaptics(event: TSHapticEvent) {
        TSHaptics.playSystemFeedback(event)
    }
}

@Suite("TSShadow")
struct TSShadowTests {
    @Test("Negative spread insets the shadow path; corner radius follows")
    func spread() {
        let bounds = CGRect(x: 0, y: 0, width: 100, height: 40)
        #expect(TSShadow.shadowRect(for: bounds, spread: -4) == CGRect(x: 4, y: 4, width: 92, height: 32))
        #expect(TSShadow.shadowPath(for: bounds, cornerRadius: 12, spread: -4).boundingBox == CGRect(x: 4, y: 4, width: 92, height: 32))
        #expect(TSShadow.shadowRect(for: bounds, spread: 0) == bounds)
    }

    @Test("Light shadow-lg uses two layers and dark uses one")
    func layers() {
        #expect(TSShadow.layers(TSShadowTokens.shadowLg, for: .light).count == 2)
        #expect(TSShadow.layers(TSShadowTokens.shadowLg, for: .dark).count == 1)
    }

    @Test("Applying a token configures one CALayer per shadow layer and clears the rest")
    @MainActor
    func applyToLayers() throws {
        let primary = CALayer()
        let secondary = CALayer()
        primary.frame = CGRect(x: 0, y: 0, width: 100, height: 40)
        secondary.frame = primary.frame

        TSShadow.apply(TSShadowTokens.shadowLg, style: .light, to: [primary, secondary], cornerRadius: 12)
        let first = try #require(TSShadowTokens.shadowLg.light.first)
        #expect(primary.shadowOpacity == first.opacity)
        #expect(primary.shadowRadius == 16)
        #expect(primary.shadowOffset == CGSize(width: 0, height: 12))
        #expect(primary.shadowPath != nil)
        let shadowColor = try #require(primary.shadowColor)
        #expect(shadowColor.alpha == 1)
        #expect(secondary.shadowOpacity > 0)

        TSShadow.apply(TSShadowTokens.shadowLg, style: .dark, to: [primary, secondary], cornerRadius: 12)
        let dark = try #require(TSShadowTokens.shadowLg.dark.first)
        #expect(primary.shadowOpacity == dark.opacity)
        #expect(secondary.shadowOpacity == 0)
        #expect(secondary.shadowPath == nil)
    }
}
