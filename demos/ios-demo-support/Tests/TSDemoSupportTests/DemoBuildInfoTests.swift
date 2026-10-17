import Foundation
import Testing
import TSDemoSupport

@Suite("DemoBuildInfo")
struct DemoBuildInfoTests {
    @Test("Reads the library version and the design-system artifact version")
    func reads() {
        let manifest = Data(#"{"schemaVersion":1,"artifact":{"id":"x","version":"1792078910-43d8"},"files":{}}"#.utf8)
        let info = DemoBuildInfo(versionFile: Data("0.1.0\n".utf8), manifest: manifest)
        #expect(info == DemoBuildInfo(libraryVersion: "0.1.0", designSystemVersion: "1792078910-43d8"))
    }

    @Test("Missing or malformed inputs give nil versions")
    func missing() {
        #expect(DemoBuildInfo(versionFile: nil, manifest: nil) == DemoBuildInfo(libraryVersion: nil, designSystemVersion: nil))
        #expect(DemoBuildInfo(versionFile: Data(" \n".utf8), manifest: Data("{}".utf8)) == DemoBuildInfo(libraryVersion: nil, designSystemVersion: nil))
    }

    @Test("A bundle without the resources gives nil versions")
    func emptyBundle() {
        #expect(DemoBuildInfo.load(from: Bundle(for: BundleToken.self)).libraryVersion == nil)
    }
}

private final class BundleToken {}
