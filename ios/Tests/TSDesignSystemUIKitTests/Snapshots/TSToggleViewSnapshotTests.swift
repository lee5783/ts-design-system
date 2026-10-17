import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit Checkbox and Switch snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSToggleViewSnapshotTests {
    @Test("Off/on × default, pressed and disabled, plus a wrapping label and hidden labels", arguments: ToggleGalleryUIKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func toggle(kind: ToggleGalleryUIKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ToggleGalleryUIView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
