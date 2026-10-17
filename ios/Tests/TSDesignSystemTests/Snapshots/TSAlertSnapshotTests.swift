import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Alert snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSAlertSnapshotTests {
    @Test("Four tones, message only, dismissible, action, custom icon and long text", arguments: AlertGalleryKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func alert(kind: AlertGalleryKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: AlertGalleryView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
