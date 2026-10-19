# Dependency and toolchain upgrades

Most version bumps arrive as Renovate PRs; the toolchain items Renovate cannot see are bumped by hand with the checklist below. Current pins: [../stack.md](../stack.md).

## Renovate

Config: `.github/renovate.json` (tested by `tools/ci/test/renovate.test.ts`). It runs monthly (`* 0-5 1 * *`, UTC), keeps the dependency dashboard on, never auto-merges, and has no PR cap (`prConcurrentLimit: 0`, `prHourlyLimit: 0`).

**Groups** (minor and patch updates; every major gets its own PR):

| Group | Contents | Label |
|---|---|---|
| AndroidX | `androidx.*`, including the demo-only entries in the demos' `settings.gradle.kts` | `android` |
| AGP, Kotlin and KSP | AGP, the Android application/library plugins, Kotlin libraries and plugins | `android` |
| Compose BOM | `androidx.compose:compose-bom` (calendar versions, so majors are not split off) | `android` |
| Material Components | `com.google.android.material:material` | `android` |
| Android test libraries | JUnit, Robolectric, `androidx.test*` | `android` |
| Gradle wrapper | the library and both demo wrappers | `android` |
| SPM test dependencies | ViewInspector (`Package.swift`) | `ios` |
| npm tools | `tools/package.json` (`@types/node` capped below 23 to follow Node 22) | `tools` |
| Codegen-sensitive npm packages | `canvaskit-wasm`, `opentype.js`, `style-dictionary` | `tools`, `codegen` |
| GitHub Actions | action major tags | `ci` |

Not grouped (one PR each): `compose-lints`, the vanniktech publish plugin, the binary-compatibility validator, the foojay resolver.

**Toolchain groups need dashboard approval** (tick the box on the dependency dashboard issue to get the PR): `Node toolchain` (mise `node` + `setup-node`), `JDK toolchain` (mise `java` + `setup-java`, in one PR so the workflows keep matching `mise.toml`), `CI runner images` (`ubuntu-24.04`, `macos-26`). `git-cliff` in `mise.toml` gets the `toolchain` label without approval.

**Reviewing a Renovate PR:**

- CI runs only the areas the PR touches ([../integrations.md](../integrations.md)): a Compose BOM bump runs the Android library and Android demo jobs, an SPM bump the iOS jobs.
- CI compares no pixels. For a bump that can change visuals (Compose BOM, MDC, Xcode), take argent screenshots of the affected demo pages on `master` and on the PR (pinned iOS simulator, one Android emulator at API 36) and compare them before approving.
- **SPM PRs edit `Package.swift` only.** Run `swift package resolve` locally and push the updated `Package.resolved` to the PR branch. Patch releases inside an `.upToNextMinor` range never get a PR, because the committed `Package.resolved` pins them.
- A codegen-sensitive npm bump can change generated output: run `make codegen-check` and the golden tests.
- Before approving a JDK or Node major, check AGP and Gradle support (Gradle 8.14 cannot run on JDK 25).

## Manual upgrades (Renovate does not cover these)

| Item | Where | Together with |
|---|---|---|
| **Xcode** | `.xcode-version` | the `macos-*` runner image in the workflows; the simulator runtime; usually the pinned iOS simulator (below) |
| **Pinned iOS simulator** | `device=` and `os=` in `.ios-simulator` (the Makefile reads them into `IOS_SIM_DEVICE` / `IOS_SIM_OS`; never set them in the Makefile) | local test runs and evidence screenshots move to the new simulator; CI's `ci-ios-simulator` fails if the runner image lacks it |
| **Android SDK levels** | `COMPILE_SDK`, `TARGET_SDK`, `MIN_SDK` in `android/build-logic/convention/src/main/kotlin/ProjectExtensions.kt`; `compileSdk` / `targetSdk` / `minSdk` in both `demos/android-*/app/build.gradle.kts` | the emulator `api-level: 36` in `.github/workflows/demos.yml`; `@Config(sdk = …)` values in tests; raising `MIN_SDK` is a major release |
| **`swift-tools-version` and `.iOS(.v15)`** | `Package.swift`, `demos/ios-demo-support/Package.swift`, `IPHONEOS_DEPLOYMENT_TARGET` in both demo `project.pbxproj` files | raising either is a major release |
| **`engines.node`** | `tools/package.json` (`>=22`) | a Node major (Renovate detects the range but opens no PR while it is satisfied) |
| **actionlint** | `ACTIONLINT_VERSION` and the four SHA-256 checksums in `tools/ci/src/actionlint.ts` | copy the hashes from that release's checksums file |
| **git-cliff npx fallback** | `GIT_CLIFF_NPM_PACKAGE` in `tools/release/src/release.ts` | the `mise.toml` major |

## Yearly platform bump (C-37)

Once a year, when Apple and Google ship new OS versions:

1. Move to the new Xcode and SDK, and to the new compile/target SDK on Android.
2. Re-check the custom chrome that new OS designs break most often: the hidden system tab bar and `TSBottomNavShell` (Liquid Glass on iOS), sheet geometry and detents, predictive back, edge-to-edge insets, and the toast overlay window above sheets and the keyboard.
3. Run the four demo apps and the behaviour suites on the beta OS versions; record OS-specific differences in [../architecture.md](../architecture.md) and [../concerns.md](../concerns.md).
4. Bump the pinned simulator in `.ios-simulator` once the new runtime is on the CI runner image.
5. Batch minimum-OS raises into a major release ([deprecation-policy.md](deprecation-policy.md)).
