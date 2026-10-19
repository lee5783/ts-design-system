# Testing patterns

**Analysis date:** 2026-10-18

Two kinds of tests guard every component on all four toolkits: **spec** (shared layer, once per platform) and **behaviour** (per toolkit). There is no snapshot or screenshot-comparison testing; visible changes are checked with argent screenshots on one OS version per platform (the pinned iOS simulator from `.ios-simulator` and one Android emulator at API 36). Tooling has its own Vitest suites, and each demo app has UI tests. Per-component test files are listed in the *Tests* table of each `design/specs/components/<name>.md`.

## Test frameworks

| Layer | Framework | Where |
|---|---|---|
| Tools (sync, codegen, CI, release, Makefile recipes) | Vitest ^5 (+ `tsc --noEmit`) | `tools/**/test/` |
| iOS spec and logic | Swift Testing | `ios/Tests/TSTokensTests/`, `ios/Tests/TSFoundationTests/` |
| SwiftUI behaviour | Swift Testing + ViewInspector | `ios/Tests/TSDesignSystemTests/Behavior/` |
| UIKit behaviour | Swift Testing + UIKit (`sendActions`, real windows) | `ios/Tests/TSDesignSystemUIKitTests/Behavior/` |
| Android spec and logic | JUnit 4 (+ Robolectric where Android types are needed) | `android/core/src/test/` |
| Compose behaviour | Compose UI test on Robolectric | `android/compose/src/test/kotlin/com/tinysol/designsystem/compose/components/<name>/` |
| Views behaviour | Robolectric + Espresso on Robolectric | `android/views/src/test/kotlin/com/tinysol/designsystem/views/components/<name>/` |
| iOS demo UI | XCUITest | `demos/ios-*/…UITests/` |
| Android demo UI | AndroidX instrumented tests | `demos/android-*/app/src/androidTest/` |
| Demo support | Swift Testing / JUnit | `demos/ios-demo-support/Tests/`, `demos/android-demo-support/src/test/` |

## Run commands

```bash
make test                      # tools-test, codegen-check, ds-sync, demos-check-boundaries, ios-test, android-test
make tools-test                # tsc typecheck + all Vitest suites in tools/
make ios-build-for-testing     # build the Swift package tests once for the simulator
make ios-test                  # golden typecheck + build, then the IOS_RUNTIMES legs (local default: pinned)
IOS_TESTS="<Target/Suite ...>" make ios-test-only   # run only those suites, reusing the build
make android-test              # all Android unit and behaviour tests
make demos-test                # demo-support tests + all 4 demo UI suites
make demos-check-boundaries    # demo boundary rules + catalog cross-check
make api-check BASELINE=<ref>  # iOS digester + Android apiCheck
```

`swift test` cannot run the package: the targets import UIKit, so tests run through `xcodebuild` on a simulator (scheme `TSDesignSystem-Package`).

Run only the tests for what you touched. Full suites (`make test`, a bare `make ios-test` or `make android-test`) are for CI.

### Run one test

| What | Command |
|---|---|
| One Vitest file | `cd tools && npx vitest run codegen/test/unit/naming.test.ts` |
| One or more iOS suites | `make ios-build-for-testing` once, then `IOS_TESTS="TSFoundationTests/TSToastQueueTests TSDesignSystemTests/TSToastBehaviorTests" make ios-test-only` (`<Target>/<Swift Testing suite type>`, space-separated; each becomes `-only-testing:`). Rebuild with `ios-build-for-testing` after changing sources or tests |
| iOS suites on another runtime | `IOS_TESTS=… make ios-test-only IOS_SIM_OS=<os> IOS_SIM_DEVICE='<iPhone>'`, or `IOS_RUNTIMES=oldest` for the oldest installed runtime (CI runs both legs; locally use the pinned one) |
| One Android class | `cd android && ./gradlew :compose:testDebugUnitTest --tests 'com.tinysol.designsystem.compose.components.card.TSCardTest'` (or `:views:`, `:core:`) |
| One iOS demo UI test class | `make demo-ios-swiftui-build-for-testing` once, then `DEMO_IOS_SWIFTUI_TESTS="TSDemoSwiftUIUITests/CatalogSmokeTests" make demo-ios-swiftui-test-only` (UIKit: `demo-ios-uikit-*` with `DEMO_IOS_UIKIT_TESTS`) |
| One Android demo test class | `cd demos/android-compose && ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.tinysol.designsystem.compose.sample.CatalogSmokeTest` |
| Theme override tests only | `make demo-ios-swiftui-theme-test` / `make demo-ios-uikit-theme-test` (pinned simulator) |

### Make variables

| Variable | Default | Effect |
|---|---|---|
| `IOS_RUNTIMES` | `pinned` | Which iOS legs `ios-test`, `ios-test-only` and the demo test targets run (`pinned`, `oldest` or both). CI sets it per matrix leg (`pinned`, `oldest`) |
| `IOS_TESTS` | empty (every suite) | `<Target>/<Suite> …` for `ios-test` and `ios-test-only`, passed as `-only-testing:`. Assigned with `?=`, so an environment variable or a make argument both work |
| `DEMO_IOS_SUPPORT_TESTS`, `DEMO_IOS_SWIFTUI_TESTS`, `DEMO_IOS_UIKIT_TESTS` | empty | The same filter for the demo `*-test` / `*-test-only` targets |
| `IOS_SIM_DEVICE`, `IOS_SIM_OS` | read from `.ios-simulator` (`device=`, `os=`) | Pinned destination; override only for runs on another runtime |
| `IOS_SIMULATOR_FILE` | `.ios-simulator` | File the pin is read from |
| `IOS_OLDEST_DEVICE` | first iPhone on the oldest installed runtime | Device for the oldest leg |
| `IOS_REQUIRE_OLDEST=1` | unset (warn) | Fail when no runtime older than the pinned one exists (C-44 U-5: not set in CI) |
| `IOS_SIM_APPEARANCE` | unset | `ci-ios-simulator` boots the pinned device in `light` or `dark` |
| `GRADLE_WORKERS=<n>` | Gradle default | `-Dorg.gradle.workers.max` for every Gradle target (CI uses 2) |
| `UPDATE_GOLDEN=1` (env, Vitest) | unset | Rewrite codegen golden fixtures: `cd tools && UPDATE_GOLDEN=1 npx vitest run codegen/test/golden/swift.test.ts` (also `android-xml`, `compose`, `icons`) |

## Test file organization

- iOS: `ios/Tests/<Target>Tests/Behavior/TS<Name>BehaviorTests.swift`; UIKit files use `TS<Name>View…`. Shared helpers sit next to them (`TraitHost.swift`, `ManualToastScheduler.swift`, `RenderedInk.swift`, `RenderedPixels`). `ios/Tests/TSTestSupport` holds the cross-target helpers (`TSSampleIconImage`, `TSTestAppearance`).
- Android: `android/<module>/src/test/kotlin/com/tinysol/designsystem/<module>/components/<name>/TS<Name>Test.kt` (Compose) / `TS<Name>ViewTest.kt` (Views).
- Spec tests: `ios/Tests/TSFoundationTests/TS<Name>StyleSpecTests.swift`, `android/core/src/test/kotlin/com/tinysol/designsystem/core/style/TS<Name>StyleSpecTest.kt`.
- Codegen: `tools/codegen/test/unit/*.test.ts`, `tools/codegen/test/golden/*.test.ts` against `tools/codegen/test/fixtures/expected/**`. `make ios-golden-typecheck` type-checks the Swift goldens at iOS 15; `android/golden-check` compiles the Compose goldens.

## Patterns

### Spec tests

Iterate `allConfigurations` (or `configurations × states` on Android) and assert each resolved role. This is where parity is pinned; toolkit tests only check that the toolkit applied the spec.

### Compat fallbacks

- iOS: pass a `TSCompatPath` (`path: .fallback`) to the shim, for example `TSSheetCompat.detents(path:)`. CI also runs every suite on the oldest installed runtime (C-39); when the runner has none older than the pinned one, that leg only warns.
- Android: `@Config(sdk = [26, 36])` (or `[26, 28]`, `[28, 36]` where a level boundary matters).

### Timing

Inject the scheduler instead of sleeping: iOS `TSToastScheduling` (`ManualToastScheduler`), Android `TSToastScheduler` (`ManualToastScheduler` in tests). Haptics: iOS injectable performer; Android `ShadowView.lastHapticFeedbackPerformed()`. Toast timeouts are fixed (4 s, 10 s with an action: `TSToastQueue.timeout(for:)`, `TSToastTimeouts`) and asserted directly.

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

- Demo UI tests find elements by identifier (`accessibilityIdentifier` / `testTag`) and by visible text or state (for example a demo's "Selected: X" caption), not by accessibility labels, values or traits.
- **iOS keyboard caveat:** keyboard-dependent checks skip when a hardware keyboard is connected; `make ci-ios-simulator` turns the software keyboard on for CI.
- **ThemeOverrideTests** assert the app theme against the device appearance, so the cross case needs both appearances. Run the theme target once with the simulator in light (`xcrun simctl ui booted appearance light`) and once in dark. CI does this on the pinned leg (`IOS_SIM_APPEARANCE=light`, then `dark` + `demo-ios-*-theme-test`). The Android `ThemeOverrideTest` flips `cmd uimode` itself.
- **Android demo tests need a device:** set `ANDROID_SERIAL`; the Views target uninstalls the app after the run. Stop any argent tool-server that holds the device's UiAutomation before running connected tests (`argent run stop-all-simulator-servers --devices emulator-5554`), and restart it before using `describe` again.

## Test harness notes

These are behaviours of the test hosts that a new test must work around.

**iOS**

- Real presentation works in the SPM test host, but dismissal transitions never complete: test dismissal paths through the not-presented path or in the demo UI tests.
- The SPM test host window has a 4 pt bottom safe area: write inset assertions relative to `window.safeAreaInsets`.
- Compare sheet detents through `identifier`, behind `#available(iOS 16, *)`.
- ViewInspector: reach a `ButtonStyle`'s content through `buttonStyle()` + `inspect(isPressed:)`; reach a custom modifier inside a style body with `find(where:)` + `.modifier(_:)`; read `actualView().body` for computed views. `callOnChange` is iOS 17+. Geometry ViewInspector cannot see is measured with rendered pixels (`RenderedPixels` / `RenderedInk`).
- UIKit cell reuse: call `updateConfiguration(using: cell.configurationState)`; UIKit re-applies `contentConfiguration` with the real state.
- XCUITest cannot send a hardware Escape; test that path in behaviour tests.

**Android**

- Size assertions need `@GraphicsMode(NATIVE)`; so do `Region` and a failed `BitmapFactory` decode (legacy graphics decodes garbage).
- Legacy graphics mis-measures Compose text at sdk 36; `clipToOutline` is ignored in software rendering (sdk 26), so components clip in `dispatchDraw`.
- `boundsInRoot` / `getBoundsInRoot` are clipped by scroll containers and ancestors: use `assertIsNotDisplayed` or `positionInRoot`. The Robolectric window caps the Compose test width at 320 dp. In Compose, `testTag` must come after `offset` for the semantics bounds to follow the offset.
- Compose `captureToImage` times out under Robolectric: read pixels with `View.drawToBitmap()` on the host view. Reach a Compose dialog's back dispatcher through `ShadowDialog.getLatestDialog() as ComponentDialog`.
- Views RTL tests need `ApplicationInfo.FLAG_SUPPORTS_RTL` set on the test context. Insets below API 30 need a detached view. `View.performClick()` ignores `isEnabled`; Robolectric needs an attached view to post `performClick`.
- Typing in Robolectric goes through `KeyCharacterMap.getEvents`; in Espresso call `closeSoftKeyboard()` before tapping buttons inside a field. Espresso taps on full-screen dialog windows must avoid the status-bar region. `UiAutomation`-injected edge swipes are unreliable for a predictive-back cancel, so that path is covered in Robolectric.
- Host tests run on JDK 21 with a 2 GB heap (`DsBuildConstants.HOST_TEST_MAX_HEAP`).

## Mocking

No mocking framework. Inject protocols and functions instead: schedulers, haptic performers, `TSCompatPath` values, presenters (`TSDialogPresenter()` instead of `.shared`) and queues.

## Coverage

None enforced. Completeness comes from the spec matrices (`allConfigurations`), the catalog cross-check (every spec has a page in all four demos) and the page-walk suites.

## CI

| Workflow | Runs |
|---|---|
| `codegen-check.yml` | `tools-test`, `actionlint`, `codegen-check`, `ds-sync` |
| `ios.yml` | `ios-test` per runtime leg (`IOS_RUNTIMES=pinned`, `oldest`); `ios-api-check` and `ios-api-check-last-tag` |
| `android.yml` | `android-build` (Ubuntu); `android-test` (macOS) |
| `demos.yml` | boundaries, iOS demo support, each iOS demo on the pinned runtime (plus the dark theme test), each Android demo on API 36 |

Details on triggers, path areas and required checks: [integrations.md](integrations.md).
