import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Badge and Avatar snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSBadgeSnapshotTests {
    @Test("Badge tones with dot, icon and truncation", arguments: TSSnapshotConfiguration.standardMatrix)
    func badge(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: BadgeGalleryView(), configuration: configuration)
    }

    @Test("Avatar sizes with initials, image and placeholder", arguments: TSSnapshotConfiguration.standardMatrix)
    func avatar(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: AvatarGalleryView(), configuration: configuration)
    }
}
