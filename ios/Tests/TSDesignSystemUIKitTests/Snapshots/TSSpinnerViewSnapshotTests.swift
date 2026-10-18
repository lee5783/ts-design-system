import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit Spinner snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSSpinnerViewSnapshotTests {
    @Test("Three sizes in each tone", arguments: TSSnapshotConfiguration.standardMatrix)
    func spinner(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: SpinnerGalleryUIView(), configuration: configuration, named: "gallery")
    }
}
