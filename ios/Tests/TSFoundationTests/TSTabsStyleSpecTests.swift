import Testing
import TSFoundation
import TSTokens

@Suite("TSTabsStyleSpec")
struct TSTabsStyleSpecTests {
    @Test("The matrix covers underline/segmented × unselected/selected × 4 states once each")
    func matrixIsComplete() {
        let configurations = TSTabsStyleSpec.allConfigurations
        #expect(configurations.count == 16)
        #expect(Set(configurations).count == 16)
    }

    @Test("Every configuration resolves label, fill, shadow and indicator tokens", arguments: TSTabsStyleSpec.allConfigurations)
    func colors(configuration: TSTabsStyleSpec.Configuration) {
        let resolved = TSTabsStyleSpec.resolve(configuration)
        let isPressed = configuration.state == .pressed
        let expected: (label: TSColor, fill: TSColor?, shadow: TSShadowToken?, indicator: TSColor?) =
            switch (configuration.variant, configuration.isSelected, isPressed) {
            case (.underline, false, false): (.textMuted, nil, nil, nil)
            case (.underline, false, true): (.textPrimary, nil, nil, nil)
            case (.underline, true, _): (.accentText, nil, nil, .accentText)
            case (.segmented, false, false): (.textSecondary, nil, nil, nil)
            case (.segmented, false, true): (.textSecondary, .surfacePressed, nil, nil)
            case (.segmented, true, _): (.textPrimary, .surfaceCard, TSShadowTokens.shadowSm, nil)
            }
        #expect(resolved.labelColor == expected.label)
        #expect(resolved.fill == expected.fill)
        #expect(resolved.shadow == expected.shadow)
        #expect(resolved.indicatorColor == expected.indicator)
        #expect(resolved.labelFont == .label)
        #expect(resolved.opacity == (configuration.state == .disabled ? 0.45 : 1))
        #expect(resolved.showsFocusRing == (configuration.state == .focused))
    }

    @Test("Underline tabs are 44 pt high with 12 pt vertical padding and a 2 pt indicator")
    func underlineGeometry() {
        let resolved = TSTabsStyleSpec.resolve(.init(variant: .underline, isSelected: true))
        #expect(resolved.paddingVertical == 12)
        #expect(resolved.paddingHorizontal == 0)
        #expect(resolved.minHeight == 44)
        #expect(resolved.indicatorHeight == 2)
        let container = TSTabsStyleSpec.container(.underline)
        #expect(container.spacing == 16)
        #expect(container.border == .borderSubtle)
        #expect(container.borderWidth == 1)
        #expect(container.isScrollable)
        #expect(TSTabsStyleSpec.hitOutset(.underline) == 0)
    }

    @Test("Segmented is padded to 46 with a radius-lg track and radius-md segments; segment hit area reaches 44")
    func segmentedGeometry() {
        let resolved = TSTabsStyleSpec.resolve(.init(variant: .segmented, isSelected: true))
        #expect(resolved.cornerRadius == TSRadius.radiusMd)
        #expect(resolved.paddingVertical == 6)
        #expect(resolved.paddingHorizontal == 16)
        #expect(resolved.minHeight == 38)
        let container = TSTabsStyleSpec.container(.segmented)
        #expect(container.fill == .backgroundSecondary)
        #expect(container.border == .borderSubtle)
        #expect(container.cornerRadius == TSRadius.radiusLg)
        #expect(container.padding == 3)
        #expect(container.minHeight == 46)
        #expect(resolved.minHeight + (container.padding + container.borderWidth) * 2 == 46)
        #expect(!container.isScrollable)
        #expect(resolved.minHeight + TSTabsStyleSpec.hitOutset(.segmented) * 2 >= 44)
    }

    @Test("A disabled tab list dims the container once and ignores pressed", arguments: TSTabsVariant.allCases)
    func disabled(variant: TSTabsVariant) {
        #expect(TSTabsStyleSpec.container(variant, isEnabled: false).opacity == 0.45)
        #expect(TSTabsStyleSpec.container(variant).opacity == 1)
        let resolved = TSTabsStyleSpec.resolve(.init(variant: variant, state: [.disabled, .pressed]))
        #expect(resolved.fill == nil)
        #expect(resolved.labelColor == (variant == .underline ? .textMuted : .textSecondary))
    }
}
