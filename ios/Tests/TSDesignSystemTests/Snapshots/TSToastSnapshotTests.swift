import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Toast snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSToastSnapshotTests {
    @Test("Neutral and four tones, undo actions and long text", arguments: ToastGalleryKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func toast(kind: ToastGalleryKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ToastGalleryView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
