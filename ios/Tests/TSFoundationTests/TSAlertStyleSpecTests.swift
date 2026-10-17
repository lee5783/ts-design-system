import CoreGraphics
import Testing
import TSFoundation
import TSTokens

@Suite("TSAlertStyleSpec")
struct TSAlertStyleSpecTests {
    private static let expectedTones: [TSAlertTone: (fill: TSColor, icon: TSIconName, iconColor: TSColor, name: String)] = [
        .info: (.infoSubtle, .info, .info, "Information"),
        .success: (.successSubtle, .checkCircle, .success, "Success"),
        .warning: (.warningSubtle, .alert, .warning, "Warning"),
        .danger: (.dangerSubtle, .xCircle, .danger, "Error")
    ]

    @Test("The matrix covers 4 tones × title × dismissible once each")
    func matrixIsComplete() {
        let configurations = TSAlertStyleSpec.allConfigurations
        #expect(configurations.count == 16)
        #expect(Set(configurations).count == 16)
    }

    @Test("Each tone has its subtle fill and its own icon in the tone colour", arguments: TSAlertStyleSpec.allConfigurations)
    func tone(configuration: TSAlertStyleSpec.Configuration) throws {
        let resolved = TSAlertStyleSpec.resolve(configuration)
        let expected = try #require(Self.expectedTones[configuration.tone])
        #expect(resolved.fill == expected.fill)
        #expect(resolved.icon == expected.icon)
        #expect(resolved.iconColor == expected.iconColor)
        #expect(resolved.showsTitle == configuration.hasTitle)
        #expect(resolved.showsDismiss == configuration.isDismissible)
    }

    @Test("Geometry and text map to the DS tokens", arguments: TSAlertStyleSpec.allConfigurations)
    func tokens(configuration: TSAlertStyleSpec.Configuration) {
        let resolved = TSAlertStyleSpec.resolve(configuration)
        #expect(resolved.horizontalPadding == 16)
        #expect(resolved.verticalPadding == 12)
        #expect(resolved.borderWidth == 1)
        #expect(resolved.spacing == 12)
        #expect(resolved.cornerRadius == 12)
        #expect(resolved.iconSize == .md)
        #expect(resolved.titleFont == TSFont.bodySm.weighted(600))
        #expect(resolved.titleColor == .textPrimary)
        #expect(resolved.messageFont == .bodySm)
        #expect(resolved.messageColor == .textSecondary)
        #expect(resolved.dismissIcon == .close)
        #expect(resolved.dismissIconSize == .md)
        #expect(resolved.dismissColor == .textSecondary)
    }

    @Test("The accessibility label and announcement read tone, title and message", arguments: TSAlertTone.allCases)
    func accessibilityLabel(tone: TSAlertTone) throws {
        let name = try #require(Self.expectedTones[tone]).name
        #expect(TSStrings.alertTone(tone) == name)
        #expect(TSAlertAccessibility.label(tone: tone, title: "Payment failed", message: "Update your card.") == "\(name), Payment failed, Update your card.")
        #expect(TSAlertAccessibility.label(tone: tone, title: nil, message: "Update your card.") == "\(name), Update your card.")
        #expect(TSAlertAccessibility.label(tone: tone, title: "Saved", message: nil) == "\(name), Saved")
    }
}
