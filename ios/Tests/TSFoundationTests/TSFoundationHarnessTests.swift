import Foundation
import Testing
@testable import TSFoundation

@Suite("TSFoundation harness")
struct TSFoundationHarnessTests {
    @Test("TSFoundation ships its own resource bundle")
    func resourceBundleIsPackaged() {
        let bundle = TSFoundationResources.bundle
        #expect(bundle != Bundle.main)
        #expect(bundle.bundleURL.lastPathComponent.hasSuffix(".bundle"))
    }
}
