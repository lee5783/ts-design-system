import CoreGraphics
import Testing
@testable import TSFoundation
import TSTokens

@Suite("TSToastStyleSpec")
struct TSToastStyleSpecTests {
    private static let expectedTones: [TSToastTone: (icon: TSIconName?, iconColor: TSColor?, name: String?)] = [
        .neutral: (nil, nil, nil),
        .info: (.info, .infoOnInverse, "Information"),
        .success: (.checkCircle, .successOnInverse, "Success"),
        .warning: (.alert, .warningOnInverse, "Warning"),
        .danger: (.xCircle, .dangerOnInverse, "Error")
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

    @Test("The label reads tone and message; the announcement adds the action", arguments: TSToastTone.allCases)
    func accessibilityLabel(tone: TSToastTone) throws {
        let name = try #require(Self.expectedTones[tone]).name
        let prefix = name.map { "\($0), " } ?? ""
        #expect(TSStrings.toastTone(tone) == name)
        #expect(TSToastAccessibility.label(tone: tone, message: "Project archived") == "\(prefix)Project archived")
        let plain = TSToastItem(message: "Project archived", tone: tone)
        #expect(TSToastAccessibility.announcement(for: plain) == "\(prefix)Project archived")
        let undo = TSToastItem(message: "Project archived", tone: tone, action: TSToastAction(title: "Undo") {})
        #expect(TSToastAccessibility.announcement(for: undo) == "\(prefix)Project archived, Undo")
    }
}

@MainActor
@Suite("TSToastShowEffects")
struct TSToastShowEffectsTests {
    @Test("Each new toast is announced once and plays its tone haptic")
    func announcesOnce() {
        var announcements: [String] = []
        var haptics: [TSHapticEvent] = []
        let effects = TSToastShowEffects(announce: { announcements.append($0) }, haptics: TSHaptics { haptics.append($0) })
        let saved = TSToastItem(message: "Saved", tone: .success)
        effects.didChange(to: saved)
        effects.didChange(to: saved)
        effects.didChange(to: TSToastItem(message: "Moved"))
        #expect(announcements == ["Success, Saved", "Moved"])
        #expect(haptics == [.success])
    }

    @Test("A toast shown again after the queue empties is announced again")
    func reannouncesAfterEmpty() {
        var announcements: [String] = []
        let effects = TSToastShowEffects(announce: { announcements.append($0) }, haptics: TSHaptics(isEnabled: false))
        let saved = TSToastItem(message: "Saved")
        effects.didChange(to: saved)
        effects.didChange(to: nil)
        effects.didChange(to: saved)
        #expect(announcements == ["Saved", "Saved"])
    }
}
