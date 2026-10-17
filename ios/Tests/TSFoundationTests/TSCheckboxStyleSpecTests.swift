import Testing
import TSFoundation
import TSTokens

@Suite("TSCheckboxStyleSpec")
struct TSCheckboxStyleSpecTests {
    @Test("The matrix covers off/on × 4 states once each")
    func matrixIsComplete() {
        let configurations = TSCheckboxStyleSpec.allConfigurations
        #expect(configurations.count == 8)
        #expect(Set(configurations).count == 8)
    }

    @Test("Every configuration resolves mark fill, border and check tokens", arguments: TSCheckboxStyleSpec.allConfigurations)
    func colors(configuration: TSCheckboxStyleSpec.Configuration) {
        let resolved = TSCheckboxStyleSpec.resolve(configuration)
        let isPressed = configuration.state == .pressed
        let expected: (fill: TSColor, border: TSColor) = switch (configuration.isOn, isPressed) {
        case (false, false): (.surfaceCard, .borderStrong)
        case (false, true): (.surfacePressed, .borderStrong)
        case (true, false): (.accentPrimary, .accentPrimary)
        case (true, true): (.accentPressed, .accentPressed)
        }
        #expect(resolved.markFill == expected.fill)
        #expect(resolved.markBorder == expected.border)
        #expect(resolved.showsCheck == configuration.isOn)
        #expect(resolved.checkColor == .textOnAccent)
        #expect(resolved.labelColor == .textPrimary)
        #expect(resolved.labelFont == .body)
        #expect(resolved.opacity == (configuration.state == .disabled ? 0.45 : 1))
        #expect(resolved.showsFocusRing == (configuration.state == .focused))
    }

    @Test("The mark is 20 pt, radius-xs, with a 1.5 pt border and a 16 pt check; the row is at least 44 pt")
    func geometry() {
        let resolved = TSCheckboxStyleSpec.resolve(.init())
        #expect(resolved.markSize == 20)
        #expect(resolved.markCornerRadius == TSRadius.radiusXs)
        #expect(resolved.markBorderWidth == 1.5)
        #expect(resolved.checkSize.pointSize == 16)
        #expect(resolved.spacing == 8)
        #expect(resolved.minHeight == 44)
    }

    @Test("A disabled checkbox ignores pressed")
    func disabledIgnoresPressed() {
        let resolved = TSCheckboxStyleSpec.resolve(.init(isOn: true, state: [.disabled, .pressed]))
        #expect(resolved.markFill == .accentPrimary)
    }
}
