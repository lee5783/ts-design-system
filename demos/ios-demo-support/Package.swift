// swift-tools-version: 6.0

import PackageDescription

let package = Package(
    name: "TSDemoSupport",
    defaultLocalization: "en",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(name: "TSDemoSupport", targets: ["TSDemoSupport"])
    ],
    targets: [
        .target(
            name: "TSDemoSupport",
            resources: [
                .process("Resources")
            ]
        ),
        .testTarget(
            name: "TSDemoSupportTests",
            dependencies: ["TSDemoSupport"]
        )
    ],
    swiftLanguageModes: [.v6]
)
