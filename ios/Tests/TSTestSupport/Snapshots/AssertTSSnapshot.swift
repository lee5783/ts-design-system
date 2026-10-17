import SnapshotTesting
import SwiftUI
import Testing
import UIKit

@MainActor
public func assertTSSnapshot(
    of view: some View,
    configuration: TSSnapshotConfiguration,
    named name: String? = nil,
    width: CGFloat = TSSnapshotDevice.width,
    filePath: StaticString = #filePath,
    testName: String = #function,
    sourceLocation: SourceLocation = #_sourceLocation
) {
    let root = view
        .frame(width: width, alignment: .topLeading)
        .fixedSize(horizontal: false, vertical: true)
        .environment(\.colorScheme, configuration.appearance == .dark ? .dark : .light)
        .environment(\.dynamicTypeSize, DynamicTypeSize(configuration.contentSize.sizeCategory) ?? .large)
        .environment(\.layoutDirection, configuration.layoutDirection.layoutDirection)
    let hosting = UIHostingController(rootView: root)
    assertTSSnapshot(
        host: TSSnapshotHostViewController(hosting: hosting),
        configuration: configuration,
        named: name,
        width: width,
        filePath: filePath,
        testName: testName,
        sourceLocation: sourceLocation
    )
}

@MainActor
public func assertTSSnapshot(
    of view: UIView,
    configuration: TSSnapshotConfiguration,
    named name: String? = nil,
    width: CGFloat = TSSnapshotDevice.width,
    filePath: StaticString = #filePath,
    testName: String = #function,
    sourceLocation: SourceLocation = #_sourceLocation
) {
    TSSnapshotMeasurement.applySemanticContentAttribute(configuration.layoutDirection.semanticContentAttribute, to: view)
    assertTSSnapshot(
        host: TSSnapshotHostViewController(contentView: view),
        configuration: configuration,
        named: name,
        width: width,
        filePath: filePath,
        testName: testName,
        sourceLocation: sourceLocation
    )
}

@MainActor
func assertTSSnapshot(
    host: TSSnapshotHostViewController,
    configuration: TSSnapshotConfiguration,
    named name: String?,
    width: CGFloat,
    filePath: StaticString,
    testName: String,
    sourceLocation: SourceLocation
) {
    let traits = configuration.traitCollection
    let size = TSSnapshotMeasurement.fittingSize(of: host, width: width, traits: traits)
    let failure = verifySnapshot(
        of: TSSnapshotMeasurement.image(of: host, size: size, traits: traits),
        as: .image(
            precision: TSSnapshotDevice.precision,
            perceptualPrecision: TSSnapshotDevice.perceptualPrecision,
            scale: TSSnapshotDevice.displayScale
        ),
        named: TSSnapshotNaming.snapshotName(name, configuration: configuration),
        record: TSSnapshotDevice.isRecording ? .all : .never,
        file: filePath,
        testName: TSSnapshotNaming.testName(testName),
        line: UInt(sourceLocation.line),
        column: UInt(sourceLocation.column)
    )
    guard let failure, !TSSnapshotDevice.isRecording else { return }
    Issue.record(Comment(rawValue: failure), sourceLocation: sourceLocation)
}
