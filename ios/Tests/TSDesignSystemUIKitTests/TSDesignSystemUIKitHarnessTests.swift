import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("TSDesignSystemUIKit harness")
struct TSDesignSystemUIKitHarnessTests {
    @Test("Importing TSDesignSystemUIKit re-exports TSFoundation and TSTokens")
    func productReexportsFoundationAndTokens() {
        #expect(TSTokensModule.name == "TSTokens")
        #expect(TSFoundationResources.bundle != Bundle.main)
    }

    @Test("The test appearance matrix resolves distinct dynamic colors")
    func appearanceMatrixResolvesDynamicColors() {
        let resolved = TSTestAppearance.allCases.map {
            UIColor.systemBackground.resolvedColor(with: $0.traitCollection)
        }
        #expect(resolved.count == 2)
        #expect(resolved[0] != resolved[1])
    }
}
