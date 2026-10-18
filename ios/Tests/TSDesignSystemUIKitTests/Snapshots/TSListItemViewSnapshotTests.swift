import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit ListItem snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSListItemViewSnapshotTests {
    @Test("Leading, trailing, chevron and switch contents, plus default, pressed, disabled and static states", arguments: ListItemGalleryUIKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func listItem(kind: ListItemGalleryUIKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ListItemGalleryUIView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}

@MainActor
@Suite("UIKit ListItem disclosure and divider snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSListItemViewExtrasSnapshotTests {
    @Test("Disclosure rows collapsed, expanded, pressed and disabled, and TSDividerView next to a TSListView", arguments: ListItemExtrasGalleryUIKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func listItem(kind: ListItemExtrasGalleryUIKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ListItemExtrasGalleryUIView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
