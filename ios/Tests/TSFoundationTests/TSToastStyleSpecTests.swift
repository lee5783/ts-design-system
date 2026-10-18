import CoreGraphics
import Testing
@testable import TSFoundation
import TSTokens

@Suite("TSToastStyleSpec")
struct TSToastStyleSpecTests {
    private static let expectedTones: [TSToastTone: (icon: TSIconName?, iconColor: TSColor?)] = [
        .neutral: (nil, nil),
        .info: (.info, .infoOnInverse),
        .success: (.checkCircle, .successOnInverse),
        .warning: (.alert, .warningOnInverse),
        .danger: (.xCircle, .dangerOnInverse)
    ]

    @Test("The matrix covers 5 tones × action once each")
    func matrixIsComplete() {
        let configurations = TSToastStyleSpec.allConfigurations
        #expect(configurations.count == 10)
        #expect(Set(configurations).count == 10)
    }

    @Test("Each tone has its own icon in the on-inverse colour; neutral has none", arguments: TSToastStyleSpec.allConfigurations)
    func tone(configuration: TSToastStyleSpec.Configuration) throws {
        let resolved = TSToastStyleSpec.resolve(configuration)
        let expected = try #require(Self.expectedTones[configuration.tone])
        #expect(resolved.icon == expected.icon)
        #expect(resolved.iconColor == expected.iconColor)
        #expect(resolved.showsAction == configuration.hasAction)
    }

    @Test("Geometry, surface and text map to the DS tokens", arguments: TSToastStyleSpec.allConfigurations)
    func tokens(configuration: TSToastStyleSpec.Configuration) {
        let resolved = TSToastStyleSpec.resolve(configuration)
        #expect(resolved.fill == .surfaceInverse)
        #expect(resolved.cornerRadius == 12)
        #expect(resolved.shadow == TSShadowTokens.shadowLg)
        #expect(resolved.maxWidth == 420)
        #expect(resolved.horizontalPadding == 16)
        #expect(resolved.verticalPadding == 12)
        #expect(resolved.spacing == 12)
        #expect(resolved.outerMargin == 16)
        #expect(resolved.iconSize == .md)
        #expect(resolved.messageFont == .bodySm)
        #expect(resolved.messageColor == .textInverse)
        #expect(resolved.actionFont == TSFont.bodySm.weighted(600))
        #expect(resolved.actionColor == .textInverse)
        #expect(resolved.actionIsUnderlined)
        #expect(resolved.actionHorizontalPadding == 8)
        #expect(resolved.actionVerticalPadding == 4)
    }

    @Test("The on-inverse icon colours are the opposite theme's status values")
    func onInverseColours() {
        #expect(TSColor.successOnInverse.light == TSColor.success.dark)
        #expect(TSColor.warningOnInverse.light == TSColor.warning.dark)
        #expect(TSColor.dangerOnInverse.light == TSColor.danger.dark)
        #expect(TSColor.infoOnInverse.light == TSColor.info.dark)
    }
}

@MainActor
@Suite("TSToastShowEffects")
struct TSToastShowEffectsTests {
    @Test("Each new toast plays its tone haptic once")
    func playsHapticOnce() {
        var haptics: [TSHapticEvent] = []
        let effects = TSToastShowEffects(haptics: TSHaptics { haptics.append($0) })
        let saved = TSToastItem(message: "Saved", tone: .success)
        effects.didChange(to: saved)
        effects.didChange(to: saved)
        effects.didChange(to: TSToastItem(message: "Moved"))
        #expect(haptics == [.success])
    }
}
