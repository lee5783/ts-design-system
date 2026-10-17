import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("SwiftUI foundation samples snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSFoundationSamplesSnapshotTests {
    @Test("Typography sample", arguments: TSSnapshotConfiguration.standardMatrix)
    func typography(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: TypographySampleView(), configuration: configuration)
    }

    @Test("Color swatch grid", arguments: TSSnapshotConfiguration.standardMatrix)
    func colorSwatchGrid(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ColorSwatchGridView(), configuration: configuration)
    }
}
