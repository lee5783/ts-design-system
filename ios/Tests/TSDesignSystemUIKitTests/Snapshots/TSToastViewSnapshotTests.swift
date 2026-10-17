import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit Toast snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSToastViewSnapshotTests {
    @Test("Neutral and four tones, undo actions and long text", arguments: ToastGalleryUIKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func toast(kind: ToastGalleryUIKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ToastGalleryUIView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
