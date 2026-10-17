import Testing
import TSDesignSystemUIKit
import TSTestSupport
import UIKit

@MainActor
@Suite("UIKit foundation samples snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSFoundationSamplesUIKitSnapshotTests {
    @Test("Typography sample", arguments: TSSnapshotConfiguration.standardMatrix)
    func typography(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: TypographySampleUIView(), configuration: configuration)
    }

    @Test("Color swatch grid", arguments: TSSnapshotConfiguration.standardMatrix)
    func colorSwatchGrid(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: ColorSwatchGridUIView(), configuration: configuration)
    }
}
