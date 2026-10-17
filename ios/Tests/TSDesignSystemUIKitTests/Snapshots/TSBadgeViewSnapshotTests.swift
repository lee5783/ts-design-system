import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit Badge and Avatar snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSBadgeViewSnapshotTests {
    @Test("Badge tones with dot, icon and truncation", arguments: TSSnapshotConfiguration.standardMatrix)
    func badge(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: BadgeGalleryUIView(), configuration: configuration)
    }

    @Test("Avatar sizes with initials, image and placeholder", arguments: TSSnapshotConfiguration.standardMatrix)
    func avatar(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: AvatarGalleryUIView(), configuration: configuration)
    }
}
