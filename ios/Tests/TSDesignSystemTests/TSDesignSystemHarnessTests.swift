import SwiftUI
import Testing
import TSDesignSystem
import ViewInspector

@MainActor
@Suite("TSDesignSystem harness")
struct TSDesignSystemHarnessTests {
    @Test("Importing TSDesignSystem re-exports TSFoundation and TSTokens")
    func productReexportsFoundationAndTokens() {
        #expect(TSTokensModule.name == "TSTokens")
        #expect(TSFoundationResources.bundle != Bundle.main)
    }

    @Test("ViewInspector can inspect a SwiftUI view")
    func viewInspectorInspectsSwiftUI() throws {
        let view = Text("TS")
        #expect(try view.inspect().text().string() == "TS")
    }
}
