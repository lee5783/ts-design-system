import SwiftUI
import Testing
import TSTokens
import UIKit
@testable import TSFoundation

@MainActor
@Suite("SwiftUI modifiers")
struct TSSwiftUIModifierTests {
    private func fittingSize(_ view: some View) -> CGSize {
        let controller = UIHostingController(rootView: view)
        return controller.sizeThatFits(in: CGSize(width: 320, height: CGFloat.greatestFiniteMagnitude))
    }

    @Test(
        "A single line of tsFont text is one DS line height tall",
        arguments: [TSFont.body, .caption, .overline, .title1],
        [DynamicTypeSize.large, .accessibility5]
    )
    func singleLineHeight(font: TSFont, dynamicTypeSize: DynamicTypeSize) {
        let size = fittingSize(Text("Ag").tsFont(font).environment(\.dynamicTypeSize, dynamicTypeSize))
        let expected = font.lineHeight(for: UIContentSizeCategory(dynamicTypeSize))
        #expect(abs(size.height - expected) <= 1)
    }

    @Test("Two lines of tsFont text are two DS line heights tall")
    func twoLineHeight() {
        let size = fittingSize(Text("Ag\nAg").tsFont(.body).environment(\.dynamicTypeSize, .large))
        #expect(abs(size.height - 48) <= 1.5)
    }

    @Test("Shadow, focus ring, press and onChange modifiers render")
    func modifiersRender() {
        let view = VStack {
            Text("Shadow").tsShadow(TSShadowTokens.shadowLg)
            Text("Focus").tsFocusRing(cornerRadius: 8)
            Text("Focused").tsFocusRing(isFocused: true, cornerRadius: 8)
            Text("Press").tsPressFeedback(.opacity, isPressed: true)
            Text("Change").tsOnChange(of: 1, initial: true) { _, _ in }
        }
        #expect(fittingSize(view).height > 0)
    }
}
