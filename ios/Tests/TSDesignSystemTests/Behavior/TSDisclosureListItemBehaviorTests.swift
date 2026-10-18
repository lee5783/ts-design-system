import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSDisclosureListItem and TSDivider behaviour")
struct TSDisclosureListItemBehaviorTests {
    private func height(of view: some View) -> CGFloat {
        let host = UIHostingController(rootView: view.environment(\.dynamicTypeSize, .large))
        return host.sizeThatFits(in: CGSize(width: 390, height: CGFloat.greatestFiniteMagnitude)).height
    }

    private func disclosure(_ box: ToggleBox) -> TSDisclosureListItem<EmptyView, Text> {
        TSDisclosureListItem("Kotlin", subtitle: "Apache License 2.0", isExpanded: box.binding) {
            Text("Licence text")
        }
    }

    @Test("Tapping the header toggles the binding")
    func tapToggles() throws {
        let box = ToggleBox()
        try disclosure(box).inspect().find(ViewType.Button.self).tap()
        #expect(box.value)
        try disclosure(box).inspect().find(ViewType.Button.self).tap()
        #expect(!box.value)
    }

    @Test("The body is shown only when expanded")
    func bodyOnlyWhenExpanded() throws {
        #expect(throws: (any Error).self) {
            try disclosure(ToggleBox(false)).inspect().find(text: "Licence text")
        }
        #expect(try disclosure(ToggleBox(true)).inspect().find(text: "Licence text").string() == "Licence text")
    }

    @Test("A disabled row does not toggle")
    func disabledBlocks() throws {
        let box = ToggleBox()
        let sut = disclosure(box).disabled(true)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.Button.self).tap()
        }
        #expect(!box.value)
    }

    @Test("The chevron turns 90° when expanded in both directions; SwiftUI mirrors the turn with the glyph", arguments: [false, true], [LayoutDirection.leftToRight, .rightToLeft])
    func chevronRotation(isExpanded: Bool, direction: LayoutDirection) throws {
        let sut = disclosure(ToggleBox(isExpanded)).environment(\.layoutDirection, direction)
        let content = try sut.inspect().find(TSListItemContent<EmptyView, EmptyView>.self).actualView()
        #expect(content.chevronRotation == (isExpanded ? 90 : 0))
        #expect(content.showsChevron)
    }

    @Test("The collapsed header is 68 with a subtitle; the body adds its text and bottom padding")
    func heights() {
        #expect(abs(height(of: disclosure(ToggleBox(false))) - 68) < 1)
        let expanded = height(of: disclosure(ToggleBox(true)))
        #expect(expanded > 68 + TSListItemStyleSpec.disclosure.bodyBottomPadding)
    }

    @Test("TSDivider is 1 pt of border-subtle, full width, hidden from accessibility")
    func divider() throws {
        #expect(abs(height(of: TSDivider()) - TSListItemStyleSpec.group.dividerThickness) < 0.01)
        let image = RenderedPixels.image(of: TSDivider().environment(\.colorScheme, .light).background(Color.white), size: CGSize(width: 390, height: 1))
        #expect(!RenderedPixels.isLight(image, at: CGPoint(x: 1, y: 0.5)))
        #expect(!RenderedPixels.isLight(image, at: CGPoint(x: 388, y: 0.5)))
        #expect(try TSDivider().body.inspect().accessibilityHidden())
    }
}
