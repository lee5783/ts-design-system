import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Card snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSCardSnapshotTests {
    @Test("Header, accessory, body and footer, raised, plus interactive default, pressed, focused and disabled", arguments: CardGalleryKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func card(kind: CardGalleryKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: CardGalleryView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
