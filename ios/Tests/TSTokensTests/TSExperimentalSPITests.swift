import Testing
@_spi(Experimental) import TSTokens

@Suite("Experimental SPI")
struct TSExperimentalSPITests {
    @Test("Experimental API is reachable through @_spi(Experimental) import")
    func experimentalSPIIsReachable() {
        #expect(TSExperimentalProbe.isAvailable)
    }
}
