# Running the demo apps

Four demo apps, one per toolkit, each consuming only its own library product the way an external app would. Each has a catalog (foundations and one page per component), settings for theme, text size, RTL and reduce motion, and an About screen with the library and DS versions.

| Demo | Toolkit | Project | Id | URL scheme |
|---|---|---|---|---|
| `demos/ios-swiftui` | SwiftUI (`TSDesignSystem`) | `TSDemoSwiftUI.xcodeproj`, scheme `TSDemoSwiftUI` | `com.tinysol.design-system-swiftui.sample` | `tsds-swiftui` |
| `demos/ios-uikit` | UIKit (`TSDesignSystemUIKit`) | `TSDemoUIKit.xcodeproj`, scheme `TSDemoUIKit` | `com.tinysol.design-system.sample` | `tsds-uikit` |
| `demos/android-compose` | Compose (`com.tinysol.designsystem:compose`) | Gradle build in the folder | `com.tinysol.designsystem.compose.sample` | `tsds-compose` |
| `demos/android-views` | Views (`com.tinysol.designsystem:views`) | Gradle build in the folder | `com.tinysol.designsystem.views.sample` | `tsds-views` |

The iOS UI-test runners are `<id>.uitests`. The Kotlin packages match the Android ids; the shared Android demo-support module is `com.tinysol.designsystem.sample.support` (coordinate `com.tinysol.designsystem.sample:support`, wired through `includeBuild("../android-demo-support")`).

**Old demo installs:** builds before the identifier rename installed as `com.ts.ds.demo.{swiftui,uikit,compose,views}`. They register the same `tsds-*` URL schemes, so with both installed a deep link can open the old app. Uninstall them once per simulator or emulator:

```bash
xcrun simctl uninstall booted com.ts.ds.demo.swiftui; xcrun simctl uninstall booted com.ts.ds.demo.uikit
adb uninstall com.ts.ds.demo.compose; adb uninstall com.ts.ds.demo.views
```

## Build and run

**iOS** (Xcode 26.6; open the `.xcodeproj` in Xcode, or):

```bash
make demo-ios-swiftui                         # build for the generic simulator
make demo-ios-swiftui-run                     # build, boot the pinned simulator, install, launch
make demo-ios-swiftui-run DEMO_ARGS="-demoTheme dark -TSDemoPage button"
make demo-ios-uikit / demo-ios-uikit-run      # same for UIKit
make demo-ios-app-icon                        # re-render both iOS app icons from design/assets/logos/normalized/ts-app-icon.svg
```

`*-run` uses `IOS_SIM_DEVICE` / `IOS_SIM_OS` (default: the pinned iPhone 17 / iOS 26.5) and passes `DEMO_ARGS` to `simctl launch`.

**Android** (JDK 21; open the demo folder in Android Studio, or):

```bash
make demo-android-compose                     # assembleDebug, lint, demo-support tests and lint
make demo-android-views
cd demos/android-compose && ./gradlew :app:installDebug
adb shell am start -n com.tinysol.designsystem.compose.sample/.MainActivity
```

**All four:** `make demos-build`; tests: `make demos-test`, or one class through the `*-build-for-testing` / `*-test-only` targets. See [../testing.md](../testing.md#demo-ui-tests).

## Launch options

The parsing lives in each platform's demo-support module (`DemoLaunchOptions`, `DemoLaunchOverrides`), so both demos of a platform accept the same options.

**iOS launch arguments** (`DEMO_ARGS`, an Xcode scheme, or `xcrun simctl launch booted <bundle id> …`):

| Argument | Values |
|---|---|
| `-demoTheme` | `system`, `light`, `dark` (unknown values mean `system`); takes precedence over `-TSDemoTheme` |
| `-TSDemoTheme` | `system`, `light`, `dark` |
| `-TSDemoTextSize` | `system`, `extraSmall`, `small`, `medium`, `large`, `extraLarge`, `extraExtraLarge`, `extraExtraExtraLarge`, `accessibility1` … `accessibility5` |
| `-TSDemoRightToLeft`, `-TSDemoReduceMotion` | `YES` / `NO` (also `true`/`false`, `1`/`0`) |
| `-TSDemoPage` | a page id: `colors`, `typography`, `spacing`, `radius`, `size`, `opacity`, `elevation`, `icons`, `button`, `icon-button`, `badge`, `avatar`, `text-field`, `checkbox`, `switch`, `tabs`, `list-item`, `card`, `alert`, `dialog`, `toast`, `bottom-nav` |

**Android intent extras** (strings, `adb shell am start -n <id>/.MainActivity --es <key> <value> …`):

| Extra | Values |
|---|---|
| `demo.theme` | `system`, `light`, `dark` (unknown values mean `system`) |
| `TSDemoTheme` | `system`, `light`, `dark` |
| `TSDemoFontScale` | `0.85`, `1`, `1.3`, `1.5`, `2` |
| `TSDemoRightToLeft`, `TSDemoReduceMotion` | `YES` / `NO` |
| `TSDemoPage` | the page ids above |

```bash
adb shell am start -n com.tinysol.designsystem.views.sample/.MainActivity --es TSDemoPage colors --es demo.theme dark --es TSDemoFontScale 2 --es TSDemoRightToLeft YES
```

## Demo theme, independent of the device (C-42)

The theme is one persisted setting (`system`, `light`, `dark`), applied app-wide so dialogs, sheets, the toast overlay, system bars and the keyboard follow it without changing the device appearance.

- **Quick toggle:** a top-bar action on the catalog and every page cycles System → Light → Dark. Its icon shows the current mode; its accessibility label is "Theme: <mode>" and its identifier / test tag is `demo.action.theme`. The settings sheet keeps the full picker; settings Reset returns to System.
- **At launch:** `-demoTheme dark` (iOS) or `--es demo.theme dark` (Android).
- **While running (deep link):**

  ```bash
  xcrun simctl openurl booted 'tsds-swiftui://settings?theme=dark'      # or tsds-uikit
  adb shell am start -a android.intent.action.VIEW -d 'tsds-compose://settings?theme=dark'   # or tsds-views
  ```

  A link only applies to its own scheme. On Android, a link in the launch intent wins over the `demo.theme` extra.
- An external value persists like an in-app change. A live change while a dialog, sheet or toast is open re-renders it in the new theme without closing it. The Views demo recreates the Activity to apply it; open overlays and page state survive.

**Theme tests:** `ThemeOverrideTests` (iOS) / `ThemeOverrideTest` (Android) launch the app in the opposite theme of the device. On iOS, run `make demo-ios-swiftui-theme-test` (or `-uikit-`) once with the simulator in light and once in dark (`xcrun simctl ui booted appearance dark`); the Android test flips `cmd uimode` itself.

## Building against a local Maven publication

By default the demos build against the in-tree library (`ds.useLocal=true`, `includeBuild("../../android")`). The only other mode is a local Maven publication; there is no remote (JitPack) mode in the demos:

| Platform | How |
|---|---|
| Android, from `~/.m2` | `make android-publish-local`, then `cd demos/android-compose && ./gradlew -Pds.useLocal=false -Pds.mavenLocal=true -Pds.version=0.1.0 :app:assembleDebug` |
| iOS | The projects always use a local package reference (the boundary check forbids remote ones). To try a tag, copy the tree to a scratch folder, tag it, and switch the scratch project's `XCLocalSwiftPackageReference "../.."` to an `XCRemoteSwiftPackageReference` with an exact version |

`-Pds.useLocal=false` needs `-Pds.mavenLocal=true`: without it the library is looked up only in `google()` / `mavenCentral()` and does not resolve. To try a JitPack build, use a scratch consumer app ([../consumers/android-getting-started.md](../consumers/android-getting-started.md)).

In zsh, pass several `-P` flags as separate words (an array), not as one quoted string.

## Driving the demos with argent

[argent](https://www.npmjs.com/package/@swmansion/argent) drives simulators and emulators for QA. From the CLI (`argent run <tool> --help` lists each tool's flags):

```bash
argent run list-devices
argent run launch-app --udid <udid> --bundleId com.tinysol.design-system-swiftui.sample --launchArgs-json '["-demoTheme","dark","-TSDemoPage","dialog"]'
argent run open-url --udid <udid> --url 'tsds-swiftui://settings?theme=light'
argent run screenshot --udid <udid> --out ./swiftui-dialog.png
argent run launch-app --udid emulator-5554 --bundleId com.tinysol.designsystem.compose.sample
```

- Use the custom schemes for deep links; `launch-app` relaunches a running app to apply `launchArgs` (iOS only; on Android pass extras through `adb`).
- argent holds the device's `UiAutomation` on Android. Before `connectedDebugAndroidTest`, stop its services for that device only: `argent run stop-all-simulator-servers --devices emulator-5554`. Restart argent's tool-server before using `describe` again.
- `adb shell screenrecord` must be stopped with `pkill -INT screenrecord` on the device before the local `adb` process exits, or the MP4 is unplayable.
