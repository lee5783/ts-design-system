import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem
import TSTestSupport

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

    @Test("The glyph inside the button is decorative")
    func glyphIsDecorative() throws {
        let sut = TSIconButton(.more, label: "More options", isRound: true) {}
        let button = try sut.inspect().find(ViewType.Button.self)
        #expect(try button.find(TSIcon.self).actualView().body.inspect().accessibilityHidden())
    }

    @Test("A consumer image draws at the resolved icon size")
    func consumerImage() throws {
        let image = Image(uiImage: TSSampleIconImage.controller())
        let sut = TSIconButton(.image(image), label: "Controller", variant: .secondary) {}
        let button = try sut.inspect().find(ViewType.Button.self)
        let resolved = TSIconButtonStyleSpec.resolve(.init(variant: .secondary))
        #expect(try button.find(TSIconSourceView.self).actualView().pointSize == resolved.iconSize)
        #expect(try button.find(ViewType.Image.self).accessibilityHidden())
    }

    @Test("TS icons draw at the resolved icon size")
    func tsIconSize() throws {
        let sut = TSIconButton(.more, label: "More options") {}
        #expect(try sut.inspect().find(TSIcon.self).actualView().pointSize == TSIconButtonStyleSpec.resolve(.init()).iconSize)
    }
}
