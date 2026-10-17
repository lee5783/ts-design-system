import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Checkbox and Switch snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSToggleSnapshotTests {
    @Test("Off/on × default, pressed, disabled and focused, plus a wrapping label and hidden labels", arguments: ToggleGalleryKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func toggle(kind: ToggleGalleryKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ToggleGalleryView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
