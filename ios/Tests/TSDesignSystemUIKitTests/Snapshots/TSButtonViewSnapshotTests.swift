import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit Icon, Button and IconButton snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSButtonViewSnapshotTests {
    @Test("Button matrix per variant", arguments: TSButtonVariant.allCases, TSSnapshotConfiguration.standardMatrix)
    func button(variant: TSButtonVariant, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ButtonGalleryUIView(variant: variant), configuration: configuration, named: variant.rawValue)
    }

    @Test("IconButton matrix", arguments: TSSnapshotConfiguration.standardMatrix)
    func iconButton(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: IconButtonGalleryUIView(), configuration: configuration)
    }

    @Test("Icon sizes and colours", arguments: TSSnapshotConfiguration.standardMatrix)
    func icon(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: IconGalleryUIView(), configuration: configuration)
    }
}
