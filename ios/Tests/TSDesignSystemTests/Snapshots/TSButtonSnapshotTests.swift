import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Icon, Button and IconButton snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSButtonSnapshotTests {
    @Test("Button matrix per variant", arguments: TSButtonVariant.allCases, TSSnapshotConfiguration.standardMatrix)
    func button(variant: TSButtonVariant, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ButtonGalleryView(variant: variant), configuration: configuration, named: variant.rawValue)
    }

    @Test("IconButton matrix", arguments: TSSnapshotConfiguration.standardMatrix)
    func iconButton(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: IconButtonGalleryView(), configuration: configuration)
    }

    @Test("Icon sizes and colours", arguments: TSSnapshotConfiguration.standardMatrix)
    func icon(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: IconGalleryView(), configuration: configuration)
    }
}
