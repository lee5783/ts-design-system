import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit TextField snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSTextFieldViewSnapshotTests {
    @Test("TextField states and content", arguments: TextFieldGalleryPage.allCases, TSSnapshotConfiguration.standardMatrix)
    func textField(page: TextFieldGalleryPage, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: TextFieldGalleryUIView(page: page), configuration: configuration, named: page.rawValue)
    }
}
