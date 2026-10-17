import CoreGraphics
import Testing
import TSFoundation
import TSTokens

@Suite("TSCardStyleSpec")
struct TSCardStyleSpecTests {
    @Test("The matrix covers raised × interactive × 4 states once each")
    func matrixIsComplete() {
        let configurations = TSCardStyleSpec.allConfigurations
        #expect(configurations.count == 16)
        #expect(Set(configurations).count == 16)
    }

    @Test("Pressed draws surface-pressed and focused draws the ring only on an interactive card", arguments: TSCardStyleSpec.allConfigurations)
    func stateTreatment(configuration: TSCardStyleSpec.Configuration) {
        let resolved = TSCardStyleSpec.resolve(configuration)
        let expectedFill: TSColor = configuration.isInteractive && configuration.state == .pressed ? .surfacePressed : .surfaceCard
        #expect(resolved.fill == expectedFill)
        #expect(resolved.showsFocusRing == (configuration.isInteractive && configuration.state == .focused))
        #expect(resolved.opacity == (configuration.state == .disabled ? 0.45 : 1))
    }

    @Test("Raised adds shadow-sm; a flat card has no shadow", arguments: TSCardStyleSpec.allConfigurations)
    func shadow(configuration: TSCardStyleSpec.Configuration) {
        let resolved = TSCardStyleSpec.resolve(configuration)
        #expect(resolved.shadow == (configuration.isRaised ? TSShadowTokens.shadowSm : nil))
    }

    @Test("Container, header, body and footer map to the DS tokens", arguments: TSCardStyleSpec.allConfigurations)
    func tokens(configuration: TSCardStyleSpec.Configuration) {
        let resolved = TSCardStyleSpec.resolve(configuration)
        #expect(resolved.border == .borderSubtle)
        #expect(resolved.borderWidth == 1)
        #expect(resolved.cornerRadius == 12)
        #expect(resolved.titleFont == .title3)
        #expect(resolved.titleColor == .textPrimary)
        #expect(resolved.subtitleFont == .bodySm)
        #expect(resolved.subtitleColor == .textMuted)
        #expect(resolved.subtitleSpacing == 2)
        #expect(resolved.bodyFont == .bodySm)
        #expect(resolved.bodyColor == .textSecondary)
        #expect(resolved.padding == 16)
        #expect(resolved.headerSpacing == 12)
        #expect(resolved.footerVerticalPadding == 12)
        #expect(resolved.footerHorizontalPadding == 16)
        #expect(resolved.footerSpacing == 8)
        #expect(resolved.divider == .borderSubtle)
        #expect(resolved.dividerThickness == 1)
    }

    @Test("Cards stack 16 apart on mobile")
    func stackSpacing() {
        #expect(TSCardStyleSpec.stackSpacing == 16)
    }

    @Test("The accessibility label joins title and subtitle; custom actions list footer actions first")
    func accessibility() {
        #expect(TSCardAccessibility.label(title: "Weekly report", subtitle: "Updated today") == "Weekly report, Updated today")
        #expect(TSCardAccessibility.label(title: "Weekly report", subtitle: nil) == "Weekly report")
        #expect(TSCardAccessibility.label(title: nil, subtitle: "Updated today") == "Updated today")
        #expect(TSCardAccessibility.label(title: "", subtitle: nil) == "")
        let actions = TSCardAccessibility.customActions(
            footer: [TSCardAction("Share") {}, TSCardAction("Open", variant: .primary) {}],
            additional: [TSCardAction("More options") {}]
        )
        #expect(actions.map(\.label) == ["Share", "Open", "More options"])
        #expect(actions.map(\.variant) == [.secondary, .primary, .secondary])
    }

    @Test("Without a title or subtitle the body text is the label; with a heading it is the value")
    func bodyFallback() {
        #expect(TSCardAccessibility.label(title: nil, subtitle: nil, body: "Revenue is up 12%.") == "Revenue is up 12%.")
        #expect(TSCardAccessibility.label(title: "Weekly report", subtitle: nil, body: "Revenue is up 12%.") == "Weekly report")
        #expect(TSCardAccessibility.value(title: "Weekly report", subtitle: nil, body: "Revenue is up 12%.") == "Revenue is up 12%.")
        #expect(TSCardAccessibility.value(title: nil, subtitle: "", body: "Revenue is up 12%.") == nil)
        #expect(TSCardAccessibility.value(title: "Weekly report", subtitle: nil, body: nil) == nil)
    }

    @Test("A typed accessory becomes a custom action after the footer actions and before the extra ones")
    @MainActor
    func accessoryAction() {
        var fired = 0
        let accessory = TSCardAccessory.iconButton(icon: .more, label: "More options") { fired += 1 }
        let actions = TSCardAccessibility.customActions(
            footer: [TSCardAction("Open", variant: .primary) {}],
            accessory: accessory,
            additional: [TSCardAction("Pin") {}]
        )
        #expect(actions.map(\.label) == ["Open", "More options", "Pin"])
        actions[1].handler()
        #expect(fired == 1)
    }
}
