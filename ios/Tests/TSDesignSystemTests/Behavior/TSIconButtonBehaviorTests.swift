import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSIconButton behaviour")
struct TSIconButtonBehaviorTests {
    @Test("Tapping fires the action once", arguments: TSIconButtonVariant.allCases)
    func tapFires(variant: TSIconButtonVariant) throws {
        let counter = TapCounter()
        let sut = TSIconButton(.search, label: "Search", variant: variant) { counter.tap() }
        try sut.inspect().find(ViewType.Button.self).tap()
        #expect(counter.count == 1)
    }

    @Test("A disabled icon button does not fire")
    func disabledBlocks() throws {
        let counter = TapCounter()
        let sut = TSIconButton(.close, label: "Close") { counter.tap() }.disabled(true)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.Button.self).tap()
        }
        #expect(counter.count == 0)
    }

    @Test("The required label becomes the accessibility label")
    func accessibilityLabel() throws {
        let sut = TSIconButton(.more, label: "More options", isRound: true) {}
        let button = try sut.inspect().find(ViewType.Button.self)
        #expect(try button.accessibilityLabel().string() == "More options")
        #expect(try button.find(TSIcon.self).actualView().body.inspect().accessibilityHidden())
    }
}
