import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Tabs snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSTabsSnapshotTests {
    @Test("Default, pressed, focused and disabled, plus overflow (underline) and inline/four-segment (segmented)", arguments: TSTabsVariant.allCases, TSSnapshotConfiguration.standardMatrix)
    func tabs(variant: TSTabsVariant, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: TabsGalleryView(variant: variant), configuration: configuration, named: variant.rawValue)
    }
}
