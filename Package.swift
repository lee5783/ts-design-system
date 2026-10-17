// swift-tools-version: 6.0

import PackageDescription

let package = Package(
    name: "TSDesignSystem",
    defaultLocalization: "en",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(name: "TSDesignSystem", targets: ["TSDesignSystem"]),
        .library(name: "TSDesignSystemUIKit", targets: ["TSDesignSystemUIKit"]),
        .library(name: "TSTokens", targets: ["TSTokens"])
    ],
    dependencies: [
        .package(url: "https://github.com/pointfreeco/swift-snapshot-testing", .upToNextMinor(from: "1.19.6")),
        .package(url: "https://github.com/nalexn/ViewInspector", .upToNextMinor(from: "0.10.5"))
    ],
    targets: [
        .target(
            name: "TSTokens",
            path: "ios/Sources/TSTokens"
        ),
        .target(
            name: "TSFoundation",
            dependencies: ["TSTokens"],
            path: "ios/Sources/TSFoundation",
            resources: [
                .process("Resources")
            ]
        ),
        .target(
            name: "TSDesignSystem",
            dependencies: ["TSFoundation", "TSTokens"],
            path: "ios/Sources/TSDesignSystem"
        ),
        .target(
            name: "TSDesignSystemUIKit",
            dependencies: ["TSFoundation", "TSTokens"],
            path: "ios/Sources/TSDesignSystemUIKit"
        ),
        .target(
            name: "TSTestSupport",
            dependencies: [
                "TSTokens",
                .product(name: "SnapshotTesting", package: "swift-snapshot-testing")
            ],
            path: "ios/Tests/TSTestSupport"
        ),
        .testTarget(
            name: "TSTokensTests",
            dependencies: ["TSTokens"],
            path: "ios/Tests/TSTokensTests"
        ),
        .testTarget(
            name: "TSFoundationTests",
            dependencies: ["TSFoundation"],
            path: "ios/Tests/TSFoundationTests"
        ),
        .testTarget(
            name: "TSDesignSystemTests",
            dependencies: [
                "TSDesignSystem",
                "TSTestSupport",
                .product(name: "SnapshotTesting", package: "swift-snapshot-testing"),
                .product(name: "ViewInspector", package: "ViewInspector")
            ],
            path: "ios/Tests/TSDesignSystemTests"
        ),
        .testTarget(
            name: "TSDesignSystemUIKitTests",
            dependencies: [
                "TSDesignSystemUIKit",
                "TSTestSupport",
                .product(name: "SnapshotTesting", package: "swift-snapshot-testing")
            ],
            path: "ios/Tests/TSDesignSystemUIKitTests"
        )
    ],
    swiftLanguageModes: [.v6]
)
