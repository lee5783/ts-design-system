import SnapshotTesting
import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport
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

    @Test("Snapshot support and the test appearance matrix are linked")
    func snapshotSupportIsLinked() {
        #expect(ViewImageConfig.iPhone13.size != nil)
        #expect(TSTestAppearance.allCases.map(\.userInterfaceStyle) == [.light, .dark])
    }
}
