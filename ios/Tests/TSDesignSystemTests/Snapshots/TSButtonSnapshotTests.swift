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

    @Test("Button with a consumer icon per variant", arguments: TSButtonVariant.allCases, TSSnapshotConfiguration.standardMatrix)
    func buttonCustomIcon(variant: TSButtonVariant, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ButtonCustomIconGalleryView(variant: variant), configuration: configuration, named: variant.rawValue)
    }

    @Test("IconButton with a consumer icon", arguments: TSSnapshotConfiguration.standardMatrix)
    func iconButtonCustomIcon(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: IconButtonCustomIconGalleryView(), configuration: configuration)
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
