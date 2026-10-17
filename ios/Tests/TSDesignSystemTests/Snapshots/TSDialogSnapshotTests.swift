import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI Dialog snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSDialogSnapshotTests {
    @Test("Center and sheet panels over the scrim", arguments: TSDialogVariant.allCases, TSSnapshotConfiguration.standardMatrix)
    func dialog(variant: TSDialogVariant, configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: DialogGalleryView(variant: variant), configuration: configuration, named: variant.rawValue)
    }

    @available(iOS 17, *)
    @Test(
        "A long center dialog at AX5, scrolled to its end, stays below the top safe area",
        arguments: TSTestAppearance.allCases.map { TSSnapshotConfiguration(appearance: $0, contentSize: .accessibility5) }
    )
    func longCenterDialog(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: LongCenterDialogView(), configuration: configuration, named: "center-long")
    }
}
