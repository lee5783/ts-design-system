import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit BottomNav snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSBottomNavViewSnapshotTests {
    @Test("Three, four with badges (3, 99+), five pressed, five selected on a badge, and disabled", arguments: TSSnapshotConfiguration.standardMatrix)
    func bottomNav(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: BottomNavGalleryUIView(), configuration: configuration, named: "gallery")
    }
}
