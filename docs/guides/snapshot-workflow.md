# Snapshot workflow

Every component is snapshot-tested in all four toolkits against one matrix (light/dark × default/large text × LTR/RTL). Snapshot comparison is a **local, opt-in check**: reference PNGs are git-ignored and never committed, and CI never compares pixels. Before a visual change you record a baseline on `master`, make the change, then verify against that baseline and inspect the diffs. Matrix details and tolerances: [../testing.md](../testing.md#snapshot-matrix).

## Where references live

References are written to these paths on your machine only; `.gitignore` excludes `ios/Tests/**/__Snapshots__/` and `android/*/src/test/snapshots/`.

| Platform | Tests | References (local, git-ignored) |
|---|---|---|
| iOS | `ios/Tests/{TSDesignSystemTests,TSDesignSystemUIKitTests}/Snapshots/TS<Name>{,View}SnapshotTests.swift` | `…/Snapshots/__Snapshots__/<TestFile>/<test>.<appearance>-<size>-<direction>.png` (`named:` adds a state name) |
| Android | `android/{compose,views}/src/test/kotlin/…/snapshots/TS<Name>{,View}{,Sdk}SnapshotTest.kt` | `android/<module>/src/test/snapshots/<TestClass>/<name>_<variant>_sdk<N>.png` |

## Writing a snapshot test

**iOS** (`TSTestSupport`):

```swift
import SwiftUI
import Testing
import TSDesignSystem
import TSTestSupport

@MainActor
@Suite("TSButton snapshots", .enabled(if: TSSnapshotDevice.isEnabled, TSSnapshotDevice.skipReason))
struct TSButtonSnapshotTests {
    @Test("Primary", arguments: TSSnapshotConfiguration.standardMatrix)
    func primary(configuration: TSSnapshotConfiguration) {
        assertTSSnapshot(of: TSButton("Save") {}, configuration: configuration)
    }
}
```

`assertTSSnapshot(of:configuration:named:)` takes a SwiftUI `View` or a `UIView`. Both share one render path: the view is hosted in a controller inside a window with a zero safe area, traits are overridden for the configuration (and set in the SwiftUI environment), UIKit views get the layout direction through `semanticContentAttribute`, and TextKit 1 text views are force-displayed before capture. The canvas is 390 pt wide with a fitted height (up to 4000 pt) at scale 2. Every suite carries the `.enabled(if: TSSnapshotDevice.isEnabled, …)` trait: it runs only when `TS_SNAPSHOT_VERIFY` or `TS_SNAPSHOT_RECORD` is set **and** the runtime is the pinned one, and otherwise skips with a reason that names `make ios-snapshot-verify` / `make ios-snapshot-record`.

**Android** (`:test-support`):

```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSCardSnapshotTest(private val variant: SnapshotVariant) {
    @Test
    fun variants() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "variants", variant), variant) {
            TSTheme(darkTheme = variant.dark) { CardVariantsSample() }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.standard)
    }
}
```

Views tests use `captureViewSnapshot(file, variant, R.style.Theme_TS) { context -> … }`. Tests that cover the API 26 fallbacks (weight 600, line height) are `*SdkSnapshotTest` classes with `@Config(sdk = [26, 36])` and `SnapshotMatrix.sdkSubset`.

## The local loop

```bash
git switch master
make ios-snapshot-record          # and/or: make android-snapshot-record
git switch my-ui-change            # make the change
make ios-snapshot-verify          # and/or: make android-snapshot-verify
```

1. **Baseline.** Record on `master` (or whatever your branch starts from) before touching UI code. A fresh clone has no references at all, so verify fails on every test with "No reference was found" until you record.
2. **Change.** Edit the component, the tokens, or the harness.
3. **Verify.** Run the verify target and look at every failing image (see below). Intended diffs are fine; unintended ones are regressions to fix.
4. **Re-baseline.** Once the change is what you want, re-record so the next change starts from it. Nothing is committed either way.

Put before/after screenshots of intended visual changes in the PR's Evidence section, since reviewers cannot see your local references.

## Verify

```bash
make ios-snapshot-verify          # pinned simulator, TS_SNAPSHOT_VERIFY=1, TS_SNAPSHOT_REQUIRE_PINNED=1
make ios-snapshot-verify SNAPSHOT_ARTIFACTS=.build/snapshot-artifacts
make android-snapshot-verify      # ./gradlew test -Proborazzi.test.verify=true
cd android && ./gradlew :compose:verifyRoborazziDebug --tests '*TSCardSnapshotTest'
```

A missing reference fails; nothing is written silently. `ios-snapshot-verify` runs every test in `TSDesignSystemTests` and `TSDesignSystemUIKitTests` (narrow it with `IOS_SNAPSHOT_TEST_TARGETS=TSDesignSystemTests/TSButtonSnapshotTests`). From Xcode, set `TS_SNAPSHOT_VERIFY=1` in the scheme or test plan environment.

The iOS pin is one source of truth: `TSSnapshotDevice.pinnedSimulator` / `pinnedOSVersion` (iPhone 17 / iOS 26.5), which the Makefile reads for `IOS_SIM_DEVICE` / `IOS_SIM_OS`. `ios-snapshot-verify` sets `TS_SNAPSHOT_REQUIRE_PINNED`, so a runtime mismatch stops the run instead of skipping green; the message is in the test runner's crash log ("crashed while preparing to run tests"). Without the verify or record key, the snapshot suites skip on every runtime.

The Android references are optional inputs of the `test` tasks: a changed reference re-runs the comparison rather than being served from the Gradle build cache, and a clone with no references still builds and tests.

## What the default test targets do

```bash
make ios-test        # unit and behaviour tests; every snapshot suite is reported skipped
make android-test    # ./gradlew test, no verify flag: snapshot tests render (crashes still fail) but compare nothing
```

These are what `make test` and CI run. CI therefore catches crashes and behaviour regressions, not visual ones (see [../concerns.md](../concerns.md)).

## Record

```bash
make ios-snapshot-record                     # all suites in TSDesignSystemTests and TSDesignSystemUIKitTests
make ios-snapshot-record IOS_SNAPSHOT_TEST_TARGETS=TSDesignSystemTests/TSButtonSnapshotTests
make android-snapshot-record                 # ./gradlew recordRoborazziDebug
cd android && ./gradlew :compose:recordRoborazziDebug --tests '*TSCardSnapshotTest'
```

- iOS records on the pinned simulator with `TEST_RUNNER_TS_SNAPSHOT_RECORD=1` and overwrites the references of the selected suites. From Xcode, set `TS_SNAPSHOT_RECORD=1` in the scheme or test plan environment.
- Re-record after any change to the snapshot harness itself (`ios/Tests/TSTestSupport/`, `android/test-support/`), after a DS sync or dependency bump you have reviewed, and after bumping the iOS pin.
- Recorded PNGs stay on your machine. `git status` does not show them; do not force-add them.

## When a verify fails

- **iOS:** the failure message's `@+` path points into the simulator's tmp folder. Keep the failed images with `make ios-snapshot-verify SNAPSHOT_ARTIFACTS=<dir>` (sets `TEST_RUNNER_SNAPSHOT_ARTIFACTS`).
- **Android:** Roborazzi writes the comparison images under `android/<module>/build/outputs/roborazzi/`.
