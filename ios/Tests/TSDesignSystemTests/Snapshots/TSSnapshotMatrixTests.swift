import Testing
import TSTestSupport
import UIKit

@Suite("Snapshot matrix")
struct TSSnapshotMatrixTests {
    @Test("The standard matrix covers appearance × text size × layout direction once each")
    func standardMatrixIsComplete() {
        let matrix = TSSnapshotConfiguration.standardMatrix
        #expect(matrix.count == 8)
        #expect(Set(matrix).count == 8)
        #expect(Set(matrix.map(\.name)).count == 8)
        #expect(Set(matrix.map(\.appearance)) == [.light, .dark])
        #expect(Set(matrix.map(\.contentSize)) == [.large, .accessibility5])
        #expect(Set(matrix.map(\.layoutDirection)) == [.ltr, .rtl])
    }

    @Test("Configuration names are stable file-name components")
    func configurationNames() {
        #expect(TSSnapshotConfiguration().name == "light-large-ltr")
        #expect(TSSnapshotConfiguration(appearance: .dark, contentSize: .accessibility5, layoutDirection: .rtl).name == "dark-accessibility5-rtl")
    }

    @Test("Traits carry the configuration and the pinned device", arguments: TSSnapshotConfiguration.standardMatrix)
    func traitsCarryConfiguration(configuration: TSSnapshotConfiguration) {
        let traits = configuration.traitCollection
        #expect(traits.userInterfaceStyle == configuration.appearance.userInterfaceStyle)
        #expect(traits.preferredContentSizeCategory == configuration.contentSize.sizeCategory)
        #expect(traits.layoutDirection == configuration.layoutDirection.traitLayoutDirection)
        #expect(traits.displayScale == TSSnapshotDevice.displayScale)
        #expect(traits.userInterfaceIdiom == .phone)
    }

    @Test("UIKit views are forced into the configuration's layout direction")
    func semanticContentAttribute() {
        #expect(TSTestLayoutDirection.ltr.semanticContentAttribute == .forceLeftToRight)
        #expect(TSTestLayoutDirection.rtl.semanticContentAttribute == .forceRightToLeft)
    }

    @Test("The largest text size is accessibilityExtraExtraExtraLarge")
    func largestTextSize() {
        #expect(TSTestContentSize.accessibility5.sizeCategory == .accessibilityExtraExtraExtraLarge)
    }

    @Test("Record mode is opt-in through TS_SNAPSHOT_RECORD")
    func recordModeIsOptIn() {
        #expect(!TSSnapshotDevice.isRecording(environment: [:]))
        #expect(!TSSnapshotDevice.isRecording(environment: ["TS_SNAPSHOT_RECORD": "0"]))
        #expect(TSSnapshotDevice.isRecording(environment: ["TS_SNAPSHOT_RECORD": "1"]))
        #expect(TSSnapshotDevice.isRecording(environment: ["TS_SNAPSHOT_RECORD": "true"]))
    }

    @Test("Only the pinned OS runs the suites; requiring the pin is opt-in and its failure names both versions")
    func pinnedRuntime() {
        let key = TSSnapshotDevice.requirePinnedEnvironmentKey
        #expect(TSSnapshotDevice.isPinnedRuntime(runtimeOSVersion: TSSnapshotDevice.pinnedOSVersion, environment: [key: "1"]))
        #expect(!TSSnapshotDevice.isPinnedRuntime(runtimeOSVersion: "15.0", environment: [:]))
        #expect(!TSSnapshotDevice.isPinnedRuntime(runtimeOSVersion: "15.0", environment: [key: "0"]))
        #expect(!TSSnapshotDevice.requiresPinnedRuntime(environment: [:]))
        #expect(!TSSnapshotDevice.requiresPinnedRuntime(environment: [key: "no"]))
        #expect(TSSnapshotDevice.requiresPinnedRuntime(environment: [key: "1"]))
        #expect(TSSnapshotDevice.requiresPinnedRuntime(environment: [key: "TRUE"]))
        let message = TSSnapshotDevice.pinnedRuntimeMismatch(runtimeOSVersion: "15.0")
        #expect(message.contains("iOS 15.0"))
        #expect(message.contains("iOS \(TSSnapshotDevice.pinnedOSVersion)"))
        #expect(message.contains(TSSnapshotDevice.pinnedSimulator))
    }

    @Test("Verify mode is opt-in through TS_SNAPSHOT_VERIFY")
    func verifyModeIsOptIn() {
        #expect(!TSSnapshotDevice.isVerifying(environment: [:]))
        #expect(!TSSnapshotDevice.isVerifying(environment: ["TS_SNAPSHOT_VERIFY": "0"]))
        #expect(TSSnapshotDevice.isVerifying(environment: ["TS_SNAPSHOT_VERIFY": "1"]))
        #expect(TSSnapshotDevice.isVerifying(environment: ["TS_SNAPSHOT_VERIFY": "TRUE"]))
    }

    @Test("The suites run only when verifying or recording on the pinned OS, and say why they skip")
    func suitesAreOptIn() {
        let pinned = TSSnapshotDevice.pinnedOSVersion
        let requirePinned = TSSnapshotDevice.requirePinnedEnvironmentKey
        #expect(!TSSnapshotDevice.isEnabled(runtimeOSVersion: pinned, environment: [:]))
        #expect(!TSSnapshotDevice.isEnabled(runtimeOSVersion: "15.0", environment: [requirePinned: "1"]))
        #expect(TSSnapshotDevice.isEnabled(runtimeOSVersion: pinned, environment: ["TS_SNAPSHOT_VERIFY": "1", requirePinned: "1"]))
        #expect(TSSnapshotDevice.isEnabled(runtimeOSVersion: pinned, environment: ["TS_SNAPSHOT_RECORD": "1"]))
        #expect(!TSSnapshotDevice.isEnabled(runtimeOSVersion: "15.0", environment: ["TS_SNAPSHOT_VERIFY": "1"]))
        let optIn = TSSnapshotDevice.skipReason(runtimeOSVersion: pinned, environment: [:])
        #expect(optIn.contains("make ios-snapshot-verify"))
        #expect(optIn.contains("make ios-snapshot-record"))
        let runtime = TSSnapshotDevice.skipReason(runtimeOSVersion: "15.0", environment: ["TS_SNAPSHOT_VERIFY": "1"])
        #expect(runtime.contains("iOS 15.0"))
        #expect(runtime.contains("iOS \(pinned)"))
    }
}
