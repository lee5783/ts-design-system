import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Text snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSTextSnapshotTests {
    @Test("Every type role and every text colour", arguments: TextGalleryKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func text(kind: TextGalleryKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: TextGalleryView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
