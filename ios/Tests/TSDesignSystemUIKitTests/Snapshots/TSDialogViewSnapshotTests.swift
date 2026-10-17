import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit Dialog snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSDialogViewSnapshotTests {
    @Test("Center and sheet panels over the scrim", arguments: TSDialogVariant.allCases, TSSnapshotConfiguration.standardMatrix)
    func dialog(variant: TSDialogVariant, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: DialogGalleryUIView(variant: variant), configuration: configuration, named: variant.rawValue)
    }
}
