import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit Alert snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSAlertViewSnapshotTests {
    @Test("Four tones, message only, dismissible, action, custom icon and long text", arguments: AlertGalleryUIKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func alert(kind: AlertGalleryUIKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: AlertGalleryUIView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
