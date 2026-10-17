import CoreGraphics
import Testing
import TSFoundation
import TSTokens

@Suite("TSSwitchStyleSpec")
struct TSSwitchStyleSpecTests {
    @Test("The matrix covers off/on × 4 states once each")
    func matrixIsComplete() {
        let configurations = TSSwitchStyleSpec.allConfigurations
        #expect(configurations.count == 8)
        #expect(Set(configurations).count == 8)
    }

    @Test("Every configuration resolves track and thumb tokens", arguments: TSSwitchStyleSpec.allConfigurations)
    func colors(configuration: TSSwitchStyleSpec.Configuration) {
        let resolved = TSSwitchStyleSpec.resolve(configuration)
        let isPressed = configuration.state == .pressed
        let expectedTrack: TSColor = configuration.isOn ? (isPressed ? .accentPressed : .accentPrimary) : .borderStrong
        #expect(resolved.trackColor == expectedTrack)
        #expect(resolved.trackOpacity == (isPressed && !configuration.isOn ? 0.85 : 1))
        #expect(resolved.thumbColor == .switchThumb)
        #expect(resolved.thumbShadow == TSShadowTokens.shadowSm)
        #expect(resolved.thumbLeadingOffset == (configuration.isOn ? 21 : 3))
        #expect(resolved.opacity == (configuration.state == .disabled ? 0.45 : 1))
        #expect(resolved.showsFocusRing == (configuration.state == .focused))
    }

    @Test("The track is 44×26 and fully rounded, the thumb is 20 pt with a 3 pt inset, and motion is 150 ms")
    func geometry() {
        let resolved = TSSwitchStyleSpec.resolve(.init())
        #expect(resolved.trackWidth == 44)
        #expect(resolved.trackHeight == 26)
        #expect(resolved.trackCornerRadius == 13)
        #expect(resolved.thumbSize == 20)
        #expect(resolved.thumbInset == 3)
        #expect(resolved.minHeight == 44)
        #expect(resolved.animationDuration == 0.15)
        #expect(TSColor.switchThumb.light == TSRGBA(hex: 0xFFFFFFFF))
        #expect(TSColor.switchThumb.dark == TSRGBA(hex: 0xFFFFFFFF))
    }

    @Test(
        "On moves the thumb to the trailing edge in both layout directions",
        arguments: [(false, false, CGFloat(3)), (true, false, 21), (false, true, 21), (true, true, 3)]
    )
    func thumbSide(isOn: Bool, isRightToLeft: Bool, originX: CGFloat) {
        let resolved = TSSwitchStyleSpec.resolve(.init(isOn: isOn))
        #expect(resolved.thumbOriginX(isRightToLeft: isRightToLeft) == originX)
    }
}
