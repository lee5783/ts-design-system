import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit Tabs snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSTabsViewSnapshotTests {
    @Test("Default, pressed and disabled, plus overflow (underline) and inline/four-segment (segmented)", arguments: TSTabsVariant.allCases, TSSnapshotConfiguration.standardMatrix)
    func tabs(variant: TSTabsVariant, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: TabsGalleryUIView(variant: variant), configuration: configuration, named: variant.rawValue)
    }
}
