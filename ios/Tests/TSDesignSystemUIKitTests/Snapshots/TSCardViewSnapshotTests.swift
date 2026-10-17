import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit Card snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSCardViewSnapshotTests {
    @Test("Header, accessory, body and footer, raised, plus interactive default, pressed and disabled", arguments: CardGalleryUIKind.allCases, TSSnapshotConfiguration.standardMatrix)
    func card(kind: CardGalleryUIKind, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: CardGalleryUIView(kind: kind), configuration: configuration, named: kind.rawValue)
    }
}
