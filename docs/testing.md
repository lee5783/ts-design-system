# Testing patterns

**Analysis date:** 2026-10-16

Three kinds of tests guard every component on all four toolkits: **spec** (shared layer, once per platform), **behaviour** (per toolkit) and **snapshot** (per toolkit, against one matrix). Tooling has its own Vitest suites, and each demo app has UI tests. Per-component test files are listed in the *Tests* table of each `design/specs/components/<name>.md`.

## Test frameworks

| Layer | Framework | Where |
|---|---|---|
| Tools (sync, codegen, CI, release, Makefile recipes) | Vitest ^5 (+ `tsc --noEmit`) | `tools/**/test/` |
| iOS spec and logic | Swift Testing | `ios/Tests/TSTokensTests/`, `ios/Tests/TSFoundationTests/` |
| SwiftUI behaviour | Swift Testing + ViewInspector | `ios/Tests/TSDesignSystemTests/Behavior/` |
| UIKit behaviour | Swift Testing + UIKit (`sendActions`, accessibility, real windows) | `ios/Tests/TSDesignSystemUIKitTests/Behavior/` |
| iOS snapshots | swift-snapshot-testing through `TSTestSupport` | `ios/Tests/*/Snapshots/`, local git-ignored references in `__Snapshots__/` |
| Android spec and logic | JUnit 4 (+ Robolectric where Android types are needed) | `android/core/src/test/` |
| Compose behaviour | Compose UI test on Robolectric | `android/compose/src/test/kotlin/com/ts/ds/compose/components/<name>/` |
| Views behaviour | Robolectric + Espresso on Robolectric | `android/views/src/test/kotlin/com/ts/ds/views/components/<name>/` |
| Android snapshots | Roborazzi (Robolectric native graphics) through `:test-support` | `android/*/src/test/kotlin/…/snapshots/`, local git-ignored references in `src/test/snapshots/` |
| iOS demo UI | XCUITest (`performAccessibilityAudit` on iOS 17+) | `demos/ios-*/…UITests/` |
| Android demo UI | AndroidX instrumented tests + Accessibility Test Framework (ATF) | `demos/android-*/app/src/androidTest/` |
| Demo support | Swift Testing / JUnit | `demos/ios-demo-support/Tests/`, `demos/android-demo-support/src/test/` |

## Run commands

```bash
make test                      # tools-test, codegen-check, ds-sync, demos-check-boundaries, ios-test, android-test
make tools-test                # tsc typecheck + all Vitest suites in tools/
make ios-test                  # golden typecheck, then pinned + oldest iOS runtimes; snapshot suites skip
make android-test              # all Android unit and behaviour tests; snapshot tests render without comparing
make ios-snapshot-verify       # local only: compare the iOS snapshot suites with your recorded references
make android-snapshot-verify   # local only: ./gradlew test -Proborazzi.test.verify=true
make demos-test                # demo-support tests + all 4 demo UI suites (no iOS audits)
make demos-audit               # iOS demo accessibility audits only (slow)
make demos-check-boundaries    # demo boundary rules + catalog cross-check
make api-check BASELINE=<ref>  # iOS digester + Android apiCheck
```

`swift test` cannot run the package: the targets import UIKit, so tests run through `xcodebuild` on a simulator (scheme `TSDesignSystem-Package`).

### Run one test

| What | Command |
|---|---|
| One Vitest file | `cd tools && npx vitest run codegen/test/unit/naming.test.ts` |
| One iOS suite | `xcodebuild test -scheme TSDesignSystem-Package -destination 'platform=iOS Simulator,name=iPhone 17,OS=26.5' -derivedDataPath .build/xcode -only-testing:TSFoundationTests/TSToastQueueTests` (target / Swift Testing suite type) |
| iOS unit tests only, any runtime | `make ios-test IOS_RUNTIMES=pinned IOS_SIM_OS=<os> IOS_SIM_DEVICE='<iPhone>'` (snapshot suites skip unless verifying or recording) |
| One Android class | `cd android && ./gradlew :compose:testDebugUnitTest --tests 'com.ts.ds.compose.components.card.TSCardTest'` (or `:views:`, `:core:`) |
| One iOS demo UI test class | `xcodebuild test -project demos/ios-swiftui/TSDemoSwiftUI.xcodeproj -scheme TSDemoSwiftUI -destination 'platform=iOS Simulator,name=iPhone 17,OS=26.5' -derivedDataPath .build/xcode-demos -only-testing:TSDemoSwiftUIUITests/CatalogSmokeTests` |
| One Android demo test class | `cd demos/android-compose && ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ts.ds.demo.compose.CatalogSmokeTest` |
| Theme override tests only | `make demo-ios-swiftui-theme-test` / `make demo-ios-uikit-theme-test` (pinned simulator) |

### Make variables

| Variable | Default | Effect |
|---|---|---|
| `IOS_RUNTIMES` | `pinned oldest` | Which iOS legs `ios-test` and the demo test targets run |
| `IOS_SIM_DEVICE`, `IOS_SIM_OS` | read from `TSSnapshotDevice.swift` | Pinned destination; override only for unit-test runs on another runtime |
| `IOS_SNAPSHOT_DEVICE_SWIFT` | `ios/Tests/TSTestSupport/Snapshots/TSSnapshotDevice.swift` | File the pin is read from |
| `IOS_OLDEST_DEVICE` | first iPhone on the oldest installed runtime | Device for the oldest leg |
| `IOS_REQUIRE_OLDEST=1` | unset (warn) | Fail when no runtime older than the pinned one exists (C-44 U-5: not set in CI) |
| `IOS_SNAPSHOT_TEST_TARGETS` | `TSDesignSystemTests TSDesignSystemUIKitTests` | What `ios-snapshot-record` and `ios-snapshot-verify` run; accepts `<Target>/<Suite>` |
| `IOS_SIM_APPEARANCE` | unset | `ci-ios-simulator` boots the pinned device in `light` or `dark` |
| `SNAPSHOT_ARTIFACTS=<dir>` | unset | `ios-snapshot-verify` keeps failed snapshot images there (`TEST_RUNNER_SNAPSHOT_ARTIFACTS`) |
| `GRADLE_WORKERS=<n>` | Gradle default | `-Dorg.gradle.workers.max` for every Gradle target (CI uses 2) |
| `UPDATE_GOLDEN=1` (env, Vitest) | unset | Rewrite codegen golden fixtures: `cd tools && UPDATE_GOLDEN=1 npx vitest run codegen/test/golden/swift.test.ts` (also `android-xml`, `compose`, `icons`) |

## Test file organization

- iOS: `ios/Tests/<Target>Tests/{Behavior,Snapshots}/TS<Name>{BehaviorTests,SnapshotTests}.swift`; UIKit files use `TS<Name>View…`. Shared helpers sit next to them (`TraitHost.swift`, `TapCounter.swift`, `ManualToastScheduler.swift`, `RenderedInk.swift`).
- Android: `android/<module>/src/test/kotlin/com/ts/ds/<module>/components/<name>/TS<Name>Test.kt` (Compose) / `TS<Name>ViewTest.kt` (Views), snapshots in `…/snapshots/TS<Name>SnapshotTest.kt` and `TS<Name>SdkSnapshotTest.kt`.
- Spec tests: `ios/Tests/TSFoundationTests/TS<Name>StyleSpecTests.swift`, `android/core/src/test/kotlin/com/ts/ds/core/style/TS<Name>StyleSpecTest.kt`.
- Codegen: `tools/codegen/test/unit/*.test.ts`, `tools/codegen/test/golden/*.test.ts` against `tools/codegen/test/fixtures/expected/**`. `make ios-golden-typecheck` type-checks the Swift goldens at iOS 15; `android/golden-check` compiles the Compose goldens.

## Snapshot matrix

Both platforms run the same standard matrix (8 variants): light/dark × default/large text × LTR/RTL.

| | iOS (`TSSnapshotConfiguration.standardMatrix`) | Android (`SnapshotMatrix.standard`) |
|---|---|---|
| Text | `.large` / `.accessibilityExtraExtraExtraLarge` | font scale 1.0 / 2.0 |
| Direction | LTR / RTL | `en-rUS` + `ldltr` / `ar-rEG` + `ldrtl` (Robolectric needs an RTL locale for `ldrtl`) |
| Canvas | 390 pt wide, fitted height up to 4000, scale 2, iPhone idiom | 360 dp × 2000 dp window, `xhdpi` |
| Tolerance | `precision` 0.999, `perceptualPrecision` 0.98 | `changeThreshold` 0.001 |
| Reference name | `<test>.<appearance>-<size>-<direction>.png` | `src/test/snapshots/<TestClass>/<name>_<variant>_sdk<N>.png` |
| Runtime | pinned simulator (iPhone 17 / iOS 26.5), only with `TS_SNAPSHOT_VERIFY` or `TS_SNAPSHOT_RECORD`; otherwise skipped | `@Config(sdk = [36])`; `*SdkSnapshotTest` add an sdk 26 subset (`SnapshotMatrix.sdkSubset`) for the weight-600 and line-height fallbacks |

Record and verify, locally only (references are git-ignored and CI compares no pixels): [guides/snapshot-workflow.md](guides/snapshot-workflow.md). The Android references are optional inputs of the `test` tasks, so a changed reference re-runs the comparison instead of coming from the build cache, and a clone without references still tests.

## Patterns

### Spec tests

Iterate `allConfigurations` (or `configurations × states` on Android) and assert each resolved role. This is where parity is pinned; toolkit tests only check that the toolkit applied the spec.

### Compat fallbacks

- iOS: pass a `TSCompatPath` (`path: .fallback`) to the shim, for example `TSAccessibilityTraitsCompat.toggle(isOn:kind:path:)` or `TSSheetCompat.detents(path:)`. CI also runs every suite on the oldest installed runtime (C-39); when the runner has none older than the pinned one, that leg only warns.
- Android: `@Config(sdk = [26, 36])` (or `[26, 28]`, `[28, 36]` where a level boundary matters).

### Timing

Inject the scheduler instead of sleeping: iOS `TSToastScheduling` (`ManualToastScheduler`), Android `TSToastScheduler` (`ManualToastScheduler` in tests). Haptics: iOS injectable performer; Android `ShadowView.lastHapticFeedbackPerformed()`. API 29+ toast timeouts are asserted through a shadow `AccessibilityManager` timeout, not by calling the production function.

### Recreate tests (Android)

`ActivityController.recreate()` (Robolectric) or `ActivityScenario.recreate()` and waiting on a new activity identity (instrumented). Views need ids for state to survive. Deep-link tests use `Instrumentation.callActivityOnNewIntent` plus `resolveActivity`; never call `setIntent` in `onNewIntent` under `ActivityScenario`.

### Demo UI tests

| Suite | iOS | Android |
|---|---|---|
| Catalog smoke | `CatalogSmokeTests` | `CatalogSmokeTest` |
| Page walk | `PageWalkTests` | `PageWalkTest` |
| Components | `ActionComponentsTests`, `FormComponentsTests`, `ContentComponentsTests`, `FeedbackComponentsTests`, `NavigationComponentsTests` | same names, `…Test` |
| Overlays (dialog, sheet, toast over a sheet) | `OverlayFlowsTests` | `OverlayFlowsTest` |
| Launch options | `LaunchOptionsTests` | — (`DemoLaunchOptionsTest` in demo support) |
| Theme override (C-42) | `ThemeOverrideTests` | `ThemeOverrideTest` |
| Accessibility | `AccessibilityAuditTests` + `AccessibilityAuditPolicy.swift` | `AccessibilityChecksTest` (ATF) |

- **iOS audits** (`performAccessibilityAudit`, iOS 17+) walk every page and open overlays. They are slow, so `demos-test` skips them; `demos-audit` runs them, as does `demos-audit.yml` nightly and on PRs labelled `a11y-audit`. Accepted findings live in each demo's `AccessibilityAuditPolicy.swift`.
- **iOS keyboard caveat:** keyboard-dependent checks skip when a hardware keyboard is connected; `make ci-ios-simulator` turns the software keyboard on for CI.
- **ThemeOverrideTests** assert the app theme against the device appearance, so the cross case needs both appearances. Run the theme target once with the simulator in light (`xcrun simctl ui booted appearance light`) and once in dark. CI does this on the pinned leg (`IOS_SIM_APPEARANCE=light`, then `dark` + `demo-ios-*-theme-test`). The Android `ThemeOverrideTest` flips `cmd uimode` itself.
- **Android ATF suites** scroll every page in steps of 0.8 viewport and check each step. Known false positives are suppressed with a reason in `AccessibilityChecksTest.kt` (for example the bounds Compose reports for content scrolled under the top bar).
- **Android demo tests need a device:** set `ANDROID_SERIAL`; the Views target uninstalls the app after the run. Stop any argent tool-server that holds the device's UiAutomation before running connected tests (`argent run stop-all-simulator-servers --devices emulator-5554`), and restart it before using `describe` again.
- In demo tests, exclude the tap counter when matching live regions.

## Test harness notes

These are behaviours of the test hosts that a new test must work around.

**iOS**

- Snapshots are measured and rendered in one window with a zero safe area; TextKit 1 text views are force-displayed before capture. Re-record after any harness change.
- Real presentation works in the SPM test host, but dismissal transitions never complete: test dismissal paths through the not-presented path or in the demo UI tests.
- The SPM test host window has a 4 pt bottom safe area: write inset assertions relative to `window.safeAreaInsets`.
- Compare sheet detents through `identifier`, behind `#available(iOS 16, *)`.
- ViewInspector: reach a `ButtonStyle`'s content through `buttonStyle()` + `inspect(isPressed:)`; reach a custom modifier inside a style body with `find(where:)` + `.modifier(_:)`; read `actualView().body` for computed views. `callOnChange` is iOS 17+. ViewInspector cannot invoke `accessibilityAction` on iOS 26, so test custom actions through the modifier. Geometry ViewInspector cannot see is measured with rendered pixels (`RenderedPixels` / `RenderedInk`).
- UIKit cell reuse: call `updateConfiguration(using: cell.configurationState)`; UIKit re-applies `contentConfiguration` with the real state.
- XCUITest cannot send Escape or the VoiceOver escape gesture; XCUITest lists the children of SwiftUI combined elements.
- Snapshots do not catch scrolling or re-styled `UITextView` content (blind spot).

**Android**

- Size assertions need `@GraphicsMode(NATIVE)`; so do `Region` and a failed `BitmapFactory` decode (legacy graphics decodes garbage).
- Legacy graphics mis-measures Compose text at sdk 36; `clipToOutline` is ignored in software rendering (sdk 26 snapshots), so components clip in `dispatchDraw`.
- `boundsInRoot` / `getBoundsInRoot` are clipped by scroll containers and ancestors: use `assertIsNotDisplayed` or `positionInRoot`. The Robolectric window caps the Compose test width at 320 dp. In Compose, `testTag` must come after `offset` for the semantics bounds to follow the offset.
- The unmerged Compose tree still lists children of `clearAndSetSemantics` nodes: assert on the merged node. Toast semantics expose the message as a content description, not text.
- Compose `captureToImage` times out under Robolectric: read pixels with `View.drawToBitmap()` on the host view. Reach a Compose dialog's back dispatcher through `ShadowDialog.getLatestDialog() as ComponentDialog`.
- Views RTL tests need `ApplicationInfo.FLAG_SUPPORTS_RTL` (the snapshot environment sets it). Insets below API 30 need a detached view. `View.performClick()` ignores `isEnabled`; Robolectric needs an attached view to post `performClick`.
- Typing in Robolectric goes through `KeyCharacterMap.getEvents`; in Espresso call `closeSoftKeyboard()` before tapping buttons inside a field. Espresso taps on full-screen dialog windows must avoid the status-bar region. `UiAutomation`-injected edge swipes are unreliable for a predictive-back cancel, so that path is covered in Robolectric.
- Host tests run on JDK 21 with a 2 GB heap (`DsBuildConstants.HOST_TEST_MAX_HEAP`).

## Mocking

No mocking framework. Inject protocols and functions instead: schedulers, haptic performers, `isVoiceOverRunning` closures, `TSCompatPath` values, presenters (`TSDialogPresenter()` instead of `.shared`) and queues.

## Coverage

None enforced. Completeness comes from the spec matrices (`allConfigurations`), the catalog cross-check (every spec has a page in all four demos) and the page-walk and accessibility suites.

## CI

| Workflow | Runs |
|---|---|
| `codegen-check.yml` | `tools-test`, `actionlint`, `codegen-check`, `ds-sync` |
| `ios.yml` | `ios-test` per runtime leg (snapshot suites skip); `ios-api-check` and `ios-api-check-last-tag` |
| `android.yml` | `android-build` (Ubuntu); `android-test`, snapshots rendered but not compared (macOS) |
| `demos.yml` | boundaries, iOS demo support, each iOS demo × runtime (plus the dark theme test on the pinned leg), each Android demo × API 26 / 36 |
| `demos-audit.yml` | iOS audits, nightly and with the `a11y-audit` label (re-runs on pushes to a labelled PR) |

Details on triggers, path areas and required checks: [integrations.md](integrations.md).
