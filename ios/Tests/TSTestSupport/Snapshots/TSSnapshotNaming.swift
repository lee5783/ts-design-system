enum TSSnapshotNaming {
    static func testName(_ function: String) -> String {
        String(function.prefix { $0 != "(" })
    }

    static func snapshotName(_ name: String?, configuration: TSSnapshotConfiguration) -> String {
        [name, configuration.name].compactMap { $0 }.joined(separator: "-")
    }
}
