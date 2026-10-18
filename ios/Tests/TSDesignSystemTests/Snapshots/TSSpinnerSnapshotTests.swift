import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Spinner snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSSpinnerSnapshotTests {
    @Test("Three sizes in each tone", arguments: TSSnapshotConfiguration.standardMatrix)
    func spinner(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: SpinnerGalleryView(), configuration: configuration, named: "gallery")
    }
}
