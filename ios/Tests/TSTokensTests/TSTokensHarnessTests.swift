import Testing
@testable import TSTokens

@Suite("TSTokens harness")
struct TSTokensHarnessTests {
    @Test("The TSTokens module is linked into its test target")
    func moduleIsLinked() {
        #expect(TSTokensModule.name == "TSTokens")
    }
}
