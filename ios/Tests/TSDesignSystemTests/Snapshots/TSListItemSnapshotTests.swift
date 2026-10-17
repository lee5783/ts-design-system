import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI ListItem snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSListItemSnapshotTests {
    @Test("Leading, trailing, chevron and switch contents in a section, plus default, pressed, focused, disabled and static states", arguments: ListItemGalleryKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func listItem(kind: ListItemGalleryKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ListItemGalleryView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
