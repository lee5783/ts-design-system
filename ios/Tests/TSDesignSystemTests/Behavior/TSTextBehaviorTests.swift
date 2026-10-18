import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSText behaviour")
struct TSTextBehaviorTests {
    private func height(of view: some View) -> CGFloat {
        let host = UIHostingController(rootView: view.environment(\.dynamicTypeSize, .large))
        return host.sizeThatFits(in: CGSize(width: 390, height: CGFloat.greatestFiniteMagnitude)).height
    }

    @Test("The text is the verbatim string")
    func text() throws {
        #expect(try TSText("Licence %@").inspect().find(ViewType.Text.self).string() == "Licence %@")
    }

    @Test("Each role is one line of the role's line height", arguments: TSTypeRole.allCases)
    func lineHeight(role: TSTypeRole) {
        let expected = role.font.lineHeight(for: .large)
        #expect(abs(height(of: TSText("Sphinx", role: role)) - expected) < 1)
    }

    @Test("Defaults are body and primary")
    func defaults() {
        let sut = TSText("Body")
        #expect(sut.role == .body)
        #expect(sut.color == .primary)
    }
}
