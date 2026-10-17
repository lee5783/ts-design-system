import CoreGraphics
import Foundation
import Testing
import UIKit

public enum TSSnapshotDevice {
    public static let width: CGFloat = 390
    public static let maximumHeight: CGFloat = 4000
    public static let displayScale: CGFloat = 2
    public static let precision: Float = 0.999
    public static let perceptualPrecision: Float = 0.98
    public static let pinnedSimulator = "iPhone 17"
    public static let pinnedOSVersion = "26.5"
    public static let recordEnvironmentKey = "TS_SNAPSHOT_RECORD"
    public static let verifyEnvironmentKey = "TS_SNAPSHOT_VERIFY"
    public static let requirePinnedEnvironmentKey = "TS_SNAPSHOT_REQUIRE_PINNED"

    public static var traitCollection: UITraitCollection {
        UITraitCollection(traitsFrom: [
            UITraitCollection(displayScale: displayScale),
            UITraitCollection(userInterfaceIdiom: .phone),
            UITraitCollection(horizontalSizeClass: .compact),
            UITraitCollection(verticalSizeClass: .regular)
        ])
    }

    public static var runtimeOSVersion: String {
        let version = ProcessInfo.processInfo.operatingSystemVersion
        return "\(version.majorVersion).\(version.minorVersion)"
    }

    public static var isEnabled: Bool {
        isEnabled(runtimeOSVersion: runtimeOSVersion, environment: ProcessInfo.processInfo.environment)
    }

    public static func isEnabled(runtimeOSVersion: String, environment: [String: String]) -> Bool {
        isRequested(environment: environment) && isPinnedRuntime(runtimeOSVersion: runtimeOSVersion, environment: environment)
    }

    public static func isRequested(environment: [String: String]) -> Bool {
        isRecording(environment: environment) || isVerifying(environment: environment)
    }

    public static var isPinnedRuntime: Bool {
        isPinnedRuntime(runtimeOSVersion: runtimeOSVersion, environment: ProcessInfo.processInfo.environment)
    }

    public static func isPinnedRuntime(runtimeOSVersion: String, environment: [String: String]) -> Bool {
        let isPinned = runtimeOSVersion == pinnedOSVersion
        if !isPinned, requiresPinnedRuntime(environment: environment) {
            fatalError(pinnedRuntimeMismatch(runtimeOSVersion: runtimeOSVersion))
        }
        return isPinned
    }

    public static func requiresPinnedRuntime(environment: [String: String]) -> Bool {
        guard let value = environment[requirePinnedEnvironmentKey]?.lowercased() else { return false }
        return ["1", "true", "yes"].contains(value)
    }

    public static func pinnedRuntimeMismatch(runtimeOSVersion: String) -> String {
        "\(requirePinnedEnvironmentKey) is set, so the snapshot suites must run, but this runtime is iOS \(runtimeOSVersion) and the references are pinned to iOS \(pinnedOSVersion) (\(pinnedSimulator)); make ios-snapshot-verify reads IOS_SIM_OS and IOS_SIM_DEVICE from this file, so pass neither, or bump pinnedOSVersion and re-record"
    }

    public static var skipReason: Comment {
        Comment(rawValue: skipReason(runtimeOSVersion: runtimeOSVersion, environment: ProcessInfo.processInfo.environment))
    }

    public static func skipReason(runtimeOSVersion: String, environment: [String: String]) -> String {
        guard isRequested(environment: environment) else {
            return "Snapshot suites are local and opt-in: make ios-snapshot-verify compares with the local references (\(verifyEnvironmentKey)), make ios-snapshot-record records them (\(recordEnvironmentKey))"
        }
        return "Snapshot references are recorded on iOS \(pinnedOSVersion) (\(pinnedSimulator)); this runtime is iOS \(runtimeOSVersion)"
    }

    public static var isRecording: Bool {
        isRecording(environment: ProcessInfo.processInfo.environment)
    }

    public static func isRecording(environment: [String: String]) -> Bool {
        guard let value = environment[recordEnvironmentKey]?.lowercased() else { return false }
        return ["1", "true", "yes", "all"].contains(value)
    }

    public static func isVerifying(environment: [String: String]) -> Bool {
        guard let value = environment[verifyEnvironmentKey]?.lowercased() else { return false }
        return ["1", "true", "yes"].contains(value)
    }
}
