import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSCheckbox behaviour")
struct TSCheckboxBehaviorTests {
    @Test("Tapping flips the binding and plays a selection haptic")
    func tapToggles() throws {
        let box = ToggleBox()
        var haptics: [TSHapticEvent] = []
        let sut = TSCheckbox("Email me about updates", isOn: box.binding).playingHaptics { haptics.append($0) }
        try sut.inspect().find(ViewType.Button.self).tap()
        #expect(box.value)
        #expect(haptics == [.selection])
        try TSCheckbox("Email me about updates", isOn: box.binding).playingHaptics { haptics.append($0) }
            .inspect().find(ViewType.Button.self).tap()
        #expect(!box.value)
        #expect(haptics.count == 2)
    }

    @Test("A disabled checkbox does not toggle")
    func disabledBlocks() throws {
        let box = ToggleBox()
        let sut = TSCheckbox("Remember me", isOn: box.binding).disabled(true)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.Button.self).tap()
        }
        #expect(!box.value)
    }

    @Test("The mark is decorative", arguments: [false, true])
    func markIsDecorative(isOn: Bool) throws {
        let sut = TSCheckbox("Email me about updates", isOn: .constant(isOn))
        let button = try sut.inspect().find(ViewType.Button.self)
        let style = try #require(try button.buttonStyle() as? TSCheckboxStyle)
        let mark = try style.inspect(isPressed: false).find(TSCheckboxMark.self)
        #expect(try mark.actualView().body.inspect().accessibilityHidden())
    }

    @Test("The check glyph shows only when checked", arguments: [false, true])
    func checkGlyph(isOn: Bool) throws {
        let sut = TSCheckbox("Remember me", isOn: .constant(isOn))
        let style = try #require(try sut.inspect().find(ViewType.Button.self).buttonStyle() as? TSCheckboxStyle)
        let mark = try style.inspect(isPressed: false).find(TSCheckboxMark.self)
        #expect(try mark.actualView().resolved.showsCheck == isOn)
        #expect(((try? mark.find(TSIcon.self)) != nil) == isOn)
    }

    @Test("The whole row is at least 44 pt high", arguments: [false, true])
    func rowHeight(isLabelHidden: Bool) {
        let host = UIHostingController(rootView: TSCheckbox("Remember me", isOn: .constant(false), isLabelHidden: isLabelHidden))
        let size = host.sizeThatFits(in: CGSize(width: 320, height: CGFloat.greatestFiniteMagnitude))
        #expect(size.height >= 44)
        #expect(size.width >= (isLabelHidden ? 20 : 44))
    }
}
