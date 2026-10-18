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
}
