# Technology stack

**Analysis date:** 2026-10-16

Version numbers below are the pins in the repo today. When a pin moves, the source file named in each row is the truth; see [guides/toolchain-upgrades.md](guides/toolchain-upgrades.md) for how pins move.

## Languages

**Primary:**

- Swift 6 (language mode `.v6`, `swift-tools-version: 6.0`, `Package.swift`): the iOS library and both iOS demos.
- Kotlin 2.2.21 (`android/gradle/libs.versions.toml`): the Android library and both Android demos.

**Secondary:**

- TypeScript (^7, run through `tsx`) on Node: `tools/`.
- Make (GNU make, `Makefile`), Bash recipes.
- XML (Android resources, generated and hand-written), SVG (DS assets).

## Runtime and platform targets

| Platform | Minimum | Compile / target | Source |
|---|---|---|---|
| iOS | iOS 15 | Xcode 26.6 SDK | `Package.swift` (`.iOS(.v15)`), `.xcode-version` |
| Android | API 26 (`MIN_SDK`) | `COMPILE_SDK` / `TARGET_SDK` 36 | `android/build-logic/convention/src/main/kotlin/ProjectExtensions.kt` (`DsBuildConstants`) |

Raising either minimum is a major release (see [guides/deprecation-policy.md](guides/deprecation-policy.md)).

## Toolchain pins

| Tool | Pin | Source |
|---|---|---|
| Xcode | 26.6 | `.xcode-version` (CI selects it with `make ci-xcode`) |
| Pinned snapshot simulator | iPhone 17 / iOS 26.5 | `ios/Tests/TSTestSupport/Snapshots/TSSnapshotDevice.swift` (`pinnedSimulator`, `pinnedOSVersion`); the Makefile reads it |
| Node | 22 | `mise.toml`, `tools/package.json` `engines.node >=22`, `setup-node` in the workflows |
| JDK | Temurin 21 (host and Gradle) | `mise.toml`, `setup-java` in the workflows |
| Bytecode target | Java 17 | `DsBuildConstants.JVM_TARGET`; host tests run on JDK 21 (`HOST_TEST_JVM`) through the Gradle toolchain and the foojay resolver (`android/settings.gradle.kts`) |
| Gradle | 8.14.3 | `android/gradle/wrapper/gradle-wrapper.properties` (the demos have their own wrappers) |
| git-cliff | 2 (`npx` fallback pinned to `git-cliff@2.14.2`) | `mise.toml`, `tools/release/src/release.ts` |
| actionlint | 1.7.12, checksum-verified download | `tools/ci/src/actionlint.ts` |

Install with `mise install` (Node, JDK, git-cliff) and Xcode 26.6 from Apple.

## Frameworks

**Core (library runtime):**

- iOS: SwiftUI and UIKit only. No third-party runtime dependency.
- Android Compose: Jetpack Compose from the Compose BOM `2025.10.01` (`api` platform dependency of `:compose`, so consumers inherit it), `compose-ui` and `compose-foundation` (api), Material3 (implementation), `activity-compose` 1.8.2.
- Android Views: AppCompat 1.7.1, Material Components 1.13.0, RecyclerView 1.4.0 (all `api` of `:views`), `core-ktx` 1.17.0.

**Testing:** see [testing.md](testing.md).

- iOS: Swift Testing, ViewInspector 0.10.5, swift-snapshot-testing 1.19.6 (both `.upToNextMinor`, test targets only; `Package.resolved` also pins swift-custom-dump, swift-syntax and xctest-dynamic-overlay as their dependencies).
- Android: JUnit 4.13.2, Robolectric 4.16.1, Roborazzi 1.51.0, Compose UI test, AndroidX Test (core 1.7.0, ext-junit 1.3.0), Espresso 3.7.0, Accessibility Test Framework 4.1.1.
- Tools: Vitest ^5.

**Build/dev:**

- AGP 8.13.2, the Kotlin Android and Compose compiler plugins, vanniktech `gradle-maven-publish-plugin` 0.34.0, Kotlin binary-compatibility-validator 0.18.1, Slack `compose-lint-checks` 1.4.2.
- Codegen: Style Dictionary ^4.4 (only for the DTCG bridge test, see [concerns.md](concerns.md)), `canvaskit-wasm` 0.42.0 and `opentype.js` 2.0.0 (wordmark outlining), `yaml` ^2.9.

## Key dependencies

**Critical:**

- The DS artifact copy in `design/source/` (DS version recorded in `design/manifest.json`).
- Compose BOM: every Compose version comes from it. A BOM bump moves consumers' Compose versions too, because the BOM is an `api` dependency.

**Infrastructure:** GitHub Actions, GitHub Packages, Renovate (see [integrations.md](integrations.md)).

## Configuration

**Environment:** no `.env` files are used by the build. The only secrets are GitHub's own `GITHUB_TOKEN` in CI and a consumer's personal access token for GitHub Packages (see [integrations.md](integrations.md)).

**Build:**

- `Package.swift` (root), `android/settings.gradle.kts`, `android/gradle.properties` (`org.gradle.jvmargs=-Xmx4g`, caching, parallel, `android.nonTransitiveRClass=true`, the `ds.pom.*` POM values), `android/gradle/libs.versions.toml`.
- Convention plugins: `ds.android.library` (SDK levels, `resourcePrefix "ts_"`, explicit API, lint), `ds.android.compose`, `ds.android.testing` (JDK 21 host tests, 2 GB heap, local snapshot references as optional task inputs), `ds.publishing`, `ds.api-validation`.
- `VERSION` feeds the Android `version` (`dsVersion()`), the Makefile banner and both demos' About screens.

## Platform requirements

**Development:** macOS with Xcode 26.6 and an iPhone 17 / iOS 26.5 simulator (for the snapshot suites), the Android SDK with an API 26 and an API 36 emulator image for demo tests, JDK 21, Node 22.

**Distribution:** iOS through SPM only (git tag `X.Y.Z` on this repo); Android through GitHub Packages (`com.ts.ds:*`). CocoaPods, Maven Central, Kotlin Multiplatform and web are out of scope.
