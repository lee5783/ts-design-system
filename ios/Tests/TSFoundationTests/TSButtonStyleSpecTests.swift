import CoreGraphics
import Testing
import TSFoundation
import TSTokens

@Suite("TSButtonStyleSpec")
struct TSButtonStyleSpecTests {
    struct VariantExpectation: Sendable {
        let fill: TSColor?
        let pressedFill: TSColor?
        let label: TSColor
        let border: TSColor?
    }

    static let expectations: [TSButtonVariant: VariantExpectation] = [
        .primary: VariantExpectation(fill: .accentPrimary, pressedFill: .accentPressed, label: .textOnAccent, border: nil),
        .secondary: VariantExpectation(fill: .surfaceCard, pressedFill: .surfacePressed, label: .textPrimary, border: .borderStrong),
        .ghost: VariantExpectation(fill: nil, pressedFill: .surfacePressed, label: .textPrimary, border: nil),
        .danger: VariantExpectation(fill: .dangerSolid, pressedFill: .dangerPressed, label: .textOnAccent, border: nil),
        .link: VariantExpectation(fill: nil, pressedFill: nil, label: .accentText, border: nil),
    ]

    @Test("The matrix covers 5 variants × 2 sizes × 5 states once each")
    func matrixIsComplete() {
        let configurations = TSButtonStyleSpec.allConfigurations
        #expect(configurations.count == 50)
        #expect(Set(configurations).count == 50)
        #expect(TSButtonStyleSpec.states == [.normal, .pressed, .disabled, .loading, .focused])
    }

    @Test("Every configuration resolves fill, label and border tokens", arguments: TSButtonStyleSpec.allConfigurations)
    func colors(configuration: TSButtonStyleSpec.Configuration) throws {
        let expected = try #require(Self.expectations[configuration.variant])
        let resolved = TSButtonStyleSpec.resolve(configuration)
        let isPressed = configuration.state.contains(.pressed)
        #expect(resolved.fill == (isPressed ? expected.pressedFill : expected.fill))
        #expect(resolved.label == expected.label)
        #expect(resolved.border == expected.border)
        #expect(resolved.borderWidth == (expected.border == nil ? 0 : 1))
    }

    @Test("Every configuration resolves opacity, spinner and focus ring", arguments: TSButtonStyleSpec.allConfigurations)
    func states(configuration: TSButtonStyleSpec.Configuration) {
        let resolved = TSButtonStyleSpec.resolve(configuration)
        let state = configuration.state
        let expectedOpacity: Double = switch state {
        case .disabled: 0.45
        case .pressed where configuration.variant == .link: 0.85
        default: 1
        }
        #expect(resolved.opacity == expectedOpacity)
        #expect(resolved.showsSpinner == (state == .loading))
        #expect(resolved.showsFocusRing == (state == .focused))
    }

    @Test("Danger pressed uses the danger-pressed extension token")
    func dangerPressed() {
        let resolved = TSButtonStyleSpec.resolve(.init(variant: .danger, size: .md, state: .pressed))
        #expect(resolved.fill == .dangerPressed)
        #expect(resolved.fill?.light.hex == 0xB91C1CFF)
    }

    @Test("Loading keeps the resting fill and full opacity")
    func loadingIsNotDimmed() {
        for variant in TSButtonVariant.allCases {
            let resting = TSButtonStyleSpec.resolve(.init(variant: variant, state: .normal))
            let loading = TSButtonStyleSpec.resolve(.init(variant: variant, state: .loading))
            #expect(loading.fill == resting.fill)
            #expect(loading.opacity == 1)
        }
    }

    @Test("Sizes resolve height, padding, radius and type", arguments: TSButtonSize.allCases)
    func sizes(size: TSButtonSize) {
        let resolved = TSButtonStyleSpec.resolve(.init(variant: .primary, size: size))
        switch size {
        case .md:
            #expect(resolved.minHeight == 44)
            #expect(resolved.horizontalPadding == 16)
            #expect(resolved.font == .label)
        case .lg:
            #expect(resolved.minHeight == 52)
            #expect(resolved.horizontalPadding == 20)
            #expect(resolved.font.token == TSTypographyTokens.body)
            #expect(resolved.font.fontWeight == 500)
        }
        #expect(resolved.verticalPadding == 8)
        #expect(resolved.cornerRadius == TSRadius.radiusMd)
        #expect(resolved.iconSize == 20)
        #expect(resolved.iconSpacing == 8)
        #expect(resolved.spinnerSize == 16)
        #expect(resolved.spinnerStrokeWidth == 2)
    }

    @Test("Link has no chrome, is underlined and keeps a 44 pt hit area", arguments: TSButtonSize.allCases)
    func link(size: TSButtonSize) {
        let resolved = TSButtonStyleSpec.resolve(.init(variant: .link, size: size))
        #expect(resolved.minHeight == 0)
        #expect(resolved.horizontalPadding == 0)
        #expect(resolved.verticalPadding == 0)
        #expect(resolved.isUnderlined)
        #expect(resolved.minimumHitSize == 44)
    }

    @Test("Only secondary carries shadow-xs")
    func shadow() {
        for variant in TSButtonVariant.allCases {
            let resolved = TSButtonStyleSpec.resolve(.init(variant: variant))
            #expect(resolved.shadow == (variant == .secondary ? TSShadowTokens.shadowXs : nil))
        }
    }
}
