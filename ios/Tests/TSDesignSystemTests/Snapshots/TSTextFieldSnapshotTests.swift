import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI TextField snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSTextFieldSnapshotTests {
    @Test("TextField states and content", arguments: TextFieldGalleryPage.allCases, TSSnapshotConfiguration.standardMatrix)
    func textField(page: TextFieldGalleryPage, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: TextFieldGalleryView(page: page), configuration: configuration, named: page.rawValue)
    }
}
