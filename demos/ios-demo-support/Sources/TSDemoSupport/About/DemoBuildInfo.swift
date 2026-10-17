import Foundation

public struct DemoBuildInfo: Hashable, Sendable {
    public static let versionResourceName = "VERSION"
    public static let manifestResourceName = "manifest"
    public static let manifestResourceExtension = "json"

    public let libraryVersion: String?
    public let designSystemVersion: String?

    public init(libraryVersion: String?, designSystemVersion: String?) {
        self.libraryVersion = libraryVersion
        self.designSystemVersion = designSystemVersion
    }

    public init(versionFile: Data?, manifest: Data?) {
        let version = versionFile
            .flatMap { String(data: $0, encoding: .utf8) }?
            .trimmingCharacters(in: .whitespacesAndNewlines)
        let designSystemVersion = manifest
            .flatMap { try? JSONDecoder().decode(Manifest.self, from: $0) }?
            .artifact.version
        self.init(
            libraryVersion: version?.isEmpty == false ? version : nil,
            designSystemVersion: designSystemVersion
        )
    }

    public static func load(from bundle: Bundle) -> DemoBuildInfo {
        let versionURL = bundle.url(forResource: versionResourceName, withExtension: nil)
        let manifestURL = bundle.url(forResource: manifestResourceName, withExtension: manifestResourceExtension)
        return DemoBuildInfo(
            versionFile: versionURL.flatMap { try? Data(contentsOf: $0) },
            manifest: manifestURL.flatMap { try? Data(contentsOf: $0) }
        )
    }

    private struct Manifest: Decodable {
        struct Artifact: Decodable {
            let version: String
        }

        let artifact: Artifact
    }
}
