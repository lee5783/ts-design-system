import CoreGraphics
import Testing
import TSFoundation

@Suite("TSControlState.make and TSHitArea")
struct TSControlStateMakeTests {
    @Test("Loading wins over disabled, and disabled wins over pressed")
    func precedence() {
        #expect(TSControlState.make() == .normal)
        #expect(TSControlState.make(isPressed: true) == .pressed)
        #expect(TSControlState.make(isPressed: true, isEnabled: false) == .disabled)
        #expect(TSControlState.make(isPressed: true, isEnabled: false, isLoading: true) == .loading)
        #expect(TSControlState.make(isFocused: true) == .focused)
        #expect(TSControlState.make(isEnabled: false, isFocused: true) == .disabled)
    }

    @Test("Hit areas grow to 44 pt and never shrink")
    func hitArea() {
        #expect(TSHitArea.outset(for: 20) == 12)
        #expect(TSHitArea.outset(for: 52) == 0)
        let expanded = TSHitArea.expandedRect(CGRect(x: 0, y: 0, width: 80, height: 20))
        #expect(expanded == CGRect(x: 0, y: -12, width: 80, height: 44))
        let small = TSHitArea.expandedRect(CGRect(x: 0, y: 0, width: 24, height: 24))
        #expect(small.width == 44 && small.height == 44)
    }

    @Test("A weighted font keeps the token metrics and changes only the weight")
    func weightedFont() {
        let font = TSFont.body.weighted(500)
        #expect(font.token == TSFont.body.token)
        #expect(font.fontWeight == 500)
        #expect(font.baseFont.pointSize == 16)
        #expect(TSFont.body.fontWeight == 400)
        #expect(font != TSFont.body)
    }
}
