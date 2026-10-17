import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSSwitch behaviour")
struct TSSwitchBehaviorTests {
    @Test("Tapping flips the binding and plays a selection haptic")
    func tapToggles() throws {
        let box = ToggleBox(true)
        var haptics: [TSHapticEvent] = []
        let sut = TSSwitch("Wi-Fi", isOn: box.binding).playingHaptics { haptics.append($0) }
        try sut.inspect().find(ViewType.Button.self).tap()
        #expect(!box.value)
        #expect(haptics == [.selection])
    }

    @Test("A disabled switch does not toggle")
    func disabledBlocks() throws {
        let box = ToggleBox()
        let sut = TSSwitch("Wi-Fi", isOn: box.binding).disabled(true)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.Button.self).tap()
        }
        #expect(!box.value)
    }

    @Test(
        "One element: the label, an on/off value, and the toggle trait on 17+ or the button trait on 15–16",
        arguments: [TSCompatPath.native, .fallback], [false, true]
    )
    func accessibilityHierarchy(path: TSCompatPath, isOn: Bool) throws {
        let sut = TSSwitch("Wi-Fi", isOn: .constant(isOn), isLabelHidden: true).usingTogglePath(path)
        let button = try sut.inspect().find(ViewType.Button.self)
        #expect(try button.accessibilityLabel().string() == "Wi-Fi")
        #expect(try button.accessibilityValue().string() == (isOn ? "On" : "Off"))
        let traits = try button.accessibilityTraits()
        #expect(!traits.contains(.isSelected))
        if path == .native, #available(iOS 17, *) {
            #expect(traits.contains(.isToggle))
        } else {
            #expect(traits.contains(.isButton))
        }
        let style = try #require(try button.buttonStyle() as? TSSwitchStyle)
        let track = try style.inspect(isPressed: false).find(TSSwitchTrack.self)
        #expect(try track.actualView().body.inspect().accessibilityHidden())
    }

    @Test(
        "The thumb sits on the trailing side when on, in LTR and RTL",
        arguments: [(false, LayoutDirection.leftToRight, 13.0), (true, .leftToRight, 31.0), (false, .rightToLeft, 31.0), (true, .rightToLeft, 13.0)]
    )
    func thumbSide(isOn: Bool, direction: LayoutDirection, thumbCenterX: Double) {
        let resolved = TSSwitchStyleSpec.resolve(.init(isOn: isOn))
        let track = TSSwitchTrack(resolved: resolved)
            .environment(\.layoutDirection, direction)
            .environment(\.colorScheme, .light)
        let image = RenderedPixels.image(of: track, size: CGSize(width: 44, height: 26))
        let otherCenterX = 44 - thumbCenterX
        #expect(RenderedPixels.isLight(image, at: CGPoint(x: thumbCenterX, y: 13)))
        #expect(!RenderedPixels.isLight(image, at: CGPoint(x: otherCenterX, y: 13)))
    }

    @Test("The whole row is at least 44 pt high, even with the label hidden")
    func rowHeight() {
        let host = UIHostingController(rootView: TSSwitch("Wi-Fi", isOn: .constant(false), isLabelHidden: true))
        let size = host.sizeThatFits(in: CGSize(width: 320, height: CGFloat.greatestFiniteMagnitude))
        #expect(size.height >= 44)
        #expect(size.width >= 44)
    }
}
