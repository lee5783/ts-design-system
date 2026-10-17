import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI BottomNav snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSBottomNavSnapshotTests {
    @Test("Three, four with badges (3, 99+), five pressed, five focused, and disabled", arguments: TSSnapshotConfiguration.standardMatrix)
    func bottomNav(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: BottomNavGalleryView(), configuration: configuration, named: "gallery")
    }
}
