import CoreGraphics
import Testing
@testable import TSFoundation

@Suite("TSToastWindowing")
struct TSToastWindowingTests {
    private let window = CGRect(x: 0, y: 0, width: 390, height: 844)

    @Test("The keyboard inset is the keyboard overlap above the window's own bottom safe area")
    func overlap() {
        let keyboard = CGRect(x: 0, y: 544, width: 390, height: 300)
        #expect(TSToastWindowing.keyboardInset(keyboardFrame: keyboard, windowBounds: window, safeAreaBottom: 34) == 266)
        #expect(TSToastWindowing.keyboardInset(keyboardFrame: keyboard, windowBounds: window, safeAreaBottom: 0) == 300)
    }

    @Test("A hidden, empty, off-screen or shorter-than-safe-area keyboard gives no inset", arguments: [
        CGRect.null,
        CGRect.zero,
        CGRect(x: 0, y: 844, width: 390, height: 300),
        CGRect(x: 0, y: 824, width: 390, height: 20),
    ])
    func noOverlap(keyboard: CGRect) {
        #expect(TSToastWindowing.keyboardInset(keyboardFrame: keyboard, windowBounds: window, safeAreaBottom: 34) == 0)
    }
}
