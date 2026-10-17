import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSButton behaviour")
struct TSButtonBehaviorTests {
    @Test("Tapping fires the action once", arguments: TSButtonVariant.allCases)
    func tapFires(variant: TSButtonVariant) throws {
        let counter = TapCounter()
        let sut = TSButton("Save changes", variant: variant) { counter.tap() }
        try sut.inspect().find(ViewType.Button.self).tap()
        #expect(counter.count == 1)
    }

    @Test("A disabled button does not fire")
    func disabledBlocks() throws {
        let counter = TapCounter()
        let sut = TSButton("Save changes") { counter.tap() }.disabled(true)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.Button.self).tap()
        }
        #expect(counter.count == 0)
    }

    @Test("A loading button does not fire and shows the spinner instead of the leading icon")
    func loadingBlocks() throws {
        let counter = TapCounter()
        let sut = TSButton("Saving", leadingIcon: .check, isLoading: true) { counter.tap() }
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.Button.self).tap()
        }
        #expect(counter.count == 0)
        #expect(throws: Never.self) { try sut.inspect().find(TSSpinner.self) }
        #expect(throws: (any Error).self) { try sut.inspect().find(TSIcon.self) }
    }

    @Test("The accessibility label is the title and the value is empty at rest")
    func accessibilityAtRest() throws {
        let sut = TSButton("Save changes") {}
        let button = try sut.inspect().find(ViewType.Button.self)
        #expect(try button.accessibilityLabel().string() == "Save changes")
        #expect(try button.accessibilityValue().string() == "")
    }

    @Test("Loading sets the accessibility value to Loading")
    func accessibilityLoading() throws {
        let sut = TSButton("Saving", isLoading: true) {}
        let button = try sut.inspect().find(ViewType.Button.self)
        #expect(try button.accessibilityLabel().string() == "Saving")
        #expect(try button.accessibilityValue().string() == TSStrings.loading)
        #expect(TSStrings.loading == "Loading")
    }

    @Test("Icons inside a button are hidden from accessibility")
    func iconsAreDecorative() throws {
        let sut = TSButton("Continue", leadingIcon: .plus, trailingIcon: .arrowRight) {}
        let icons = try sut.inspect().findAll(TSIcon.self)
        #expect(icons.count == 2)
        for icon in icons {
            #expect(try icon.actualView().body.inspect().accessibilityHidden())
        }
    }

    @Test("The title is rendered as text")
    func title() throws {
        let sut = TSButton("Learn more", variant: .link) {}
        #expect(try sut.inspect().find(text: "Learn more").string() == "Learn more")
    }
}
