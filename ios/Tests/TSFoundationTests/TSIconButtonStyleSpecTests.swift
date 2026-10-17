import Testing
import TSFoundation
import TSTokens

@Suite("TSIconButtonStyleSpec")
struct TSIconButtonStyleSpecTests {
    @Test("The matrix covers 3 variants × square/round × 4 states once each")
    func matrixIsComplete() {
        let configurations = TSIconButtonStyleSpec.allConfigurations
        #expect(configurations.count == 24)
        #expect(Set(configurations).count == 24)
    }

    @Test("Every configuration resolves fill, icon and border tokens", arguments: TSIconButtonStyleSpec.allConfigurations)
    func colors(configuration: TSIconButtonStyleSpec.Configuration) {
        let resolved = TSIconButtonStyleSpec.resolve(configuration)
        let isPressed = configuration.state.contains(.pressed)
        switch configuration.variant {
        case .ghost:
            #expect(resolved.fill == (isPressed ? .surfacePressed : nil))
            #expect(resolved.icon == .textPrimary)
            #expect(resolved.border == nil)
        case .secondary:
            #expect(resolved.fill == (isPressed ? .surfacePressed : .surfaceCard))
            #expect(resolved.icon == .textPrimary)
            #expect(resolved.border == .borderStrong)
            #expect(resolved.borderWidth == 1)
        case .primary:
            #expect(resolved.fill == (isPressed ? .accentPressed : .accentPrimary))
            #expect(resolved.icon == .textOnAccent)
            #expect(resolved.border == nil)
        }
        #expect(resolved.opacity == (configuration.state == .disabled ? 0.45 : 1))
        #expect(resolved.showsFocusRing == (configuration.state == .focused))
    }

    @Test("Size is 44 with a 24 pt icon; round is a full circle", arguments: [false, true])
    func geometry(isRound: Bool) {
        let resolved = TSIconButtonStyleSpec.resolve(.init(variant: .ghost, isRound: isRound))
        #expect(resolved.size == 44)
        #expect(resolved.iconSize == 24)
        #expect(resolved.cornerRadius == (isRound ? 22 : TSRadius.radiusMd))
    }
}
