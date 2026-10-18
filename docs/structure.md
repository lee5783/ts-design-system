# Codebase structure

**Analysis date:** 2026-10-16

One monorepo holds the TS design system (DS) source, the code generator, the iOS Swift package, the Android Gradle project and four demo apps. This page answers "where does X live, and where does new code go?". For why the layers exist, read [architecture.md](architecture.md).

## Directory layout

```
ts-design-system/
├── Package.swift            # SPM manifest (root, required for git-URL consumers); targets point into ios/
├── Package.resolved         # pins the test-only SPM packages; consumers never fetch them
├── Makefile                 # every build, test, codegen, release and CI entry point (`make help`)
├── VERSION                  # lockstep library version X.Y.Z (iOS tag and Android artifacts)
├── CHANGELOG.md  cliff.toml # git-cliff changelog and its config
├── mise.toml  .xcode-version .editorconfig .gitattributes .gitignore  LICENSE (MIT)
├── design/                  # single source of truth for the DS
├── tools/                   # one Node 22 workspace: ds-sync, codegen, CI, release, demo checks (Vitest)
├── ios/                     # Swift sources and tests (built through the root Package.swift)
├── android/                 # Gradle project: tokens, core, compose, views, bom (+ internal modules)
├── demos/                   # 4 demo apps + 2 toolkit-agnostic demo-support modules, outside the library
├── docs/                    # this corpus
└── .github/                 # workflows, renovate.json, CODEOWNERS, PR template
```

## Directory purposes

### `design/`

| Path | Contents | Edited by |
|---|---|---|
| `design/source/` | Verbatim copy of the DS artifact: `tokens.json`, `design-system.json`, `README.md`, `components/<Name>/README.md`, `components/bundle.css`, `components/index.d.ts` | `make ds-sync FROM=…` only, never by hand |
| `design/manifest.json` | Artifact id, URL and version, sync time, sha256 and size of every source file and asset | `make ds-sync FROM=…` / `UPDATE=1` |
| `design/assets/icons/source/`, `design/assets/logos/source/` | The DS SVGs (33 icons, 4 logos) as served | `make ds-sync FROM=…` |
| `design/assets/icons/icons.meta.json` | `mirrorsInRTL` per icon | by hand, when icons change |
| `design/assets/{icons,logos}/normalized/` | Normalized SVGs (generated) | `make codegen` |
| `design/extensions/native-tokens.json` | Local token additions with an `upstream` status, plus the `renames` map for deprecated aliases | by hand, see [guides/ds-update-flow.md](guides/ds-update-flow.md) |
| `design/specs/components/<name>.md` | The cross-toolkit contract per component: API mapping for all 4 toolkits, matrix, tests, platform notes | by hand; one spec per component is also what `demos-check-boundaries` checks against |

### `tools/` (Node 22, `tools/package.json`)

| Path | Purpose | Make target |
|---|---|---|
| `tools/ds-sync/` | Verify or refresh `design/source` against the manifest (see `tools/ds-sync/README.md`) | `ds-sync` |
| `tools/codegen/src/{cli,generate,generators}.ts` | Codegen entry point and generator list | `codegen`, `codegen-check` |
| `tools/codegen/src/model/` | Token model: `load-ts-tokens`, `merge-extensions` (extensions + renames), `drop-web-only`, `diff-report`, `radius` | — |
| `tools/codegen/src/transforms/` | Naming, colour (`#RRGGBBAA` → `#AARRGGBB`), CSS value, per-platform transforms (`android.ts`, `css-values.ts`, `naming.ts`, `swift.ts`) | — |
| `tools/codegen/src/formats/{swift,android-xml,compose,icons}/` | One writer per output format | — |
| `tools/codegen/src/icons/` | SVG reading, outline and text-outline conversion (wordmark), vector assets | — |
| `tools/codegen/fonts/` | Roboto placeholder font used to outline the wordmark | — |
| `tools/codegen/test/{unit,golden}/`, `test/fixtures/` | Unit tests and golden-file tests against `fixtures/expected/**` | `tools-test` |
| `tools/api-check/` | iOS public-API check through `swift-api-digester` | `ios-api-check`, `ios-api-check-record` |
| `tools/ci/` | `changes.ts` (path areas), `required.ts` (aggregator), `actionlint.ts`, workflow and Renovate tests | `ci-changes`, `ci-required`, `actionlint` |
| `tools/release/` | `release.ts` (bump, verify, notes) | `release-bump`, `release-verify`, `release-notes` |
| `tools/demos/` | `check-boundaries.ts`, `check-catalog.ts`, `render-app-icon.swift` | `demos-check-boundaries`, `demo-ios-app-icon` |
| `tools/make/test/` | Vitest suites that run the Makefile recipes against stub tools | `tools-test` |

### `ios/` (Swift package `TSDesignSystem`, declared in the root `Package.swift`)

| Target | Path | Product | Contents |
|---|---|---|---|
| `TSTokens` | `ios/Sources/TSTokens/Generated/*.swift` | `TSTokens` | 100% generated: `TSColor`, `TSSpacing`, `TSRadius`, `TSSize`, `TSOpacity`, `TSShadowTokens`, `TSTypographyTokens`, `TSMotion`, `TSComponentTokens`, `TSDimensionToken`, `TSIconName`, and the separate controller palette `TSControllerColor` / `TSControllerSize`. `TSExperimentalProbe.swift` sits outside `Generated/` |
| `TSFoundation` | `ios/Sources/TSFoundation/` | none (re-exported) | Shared, non-rendering layer, see the folder map below |
| `TSDesignSystem` | `ios/Sources/TSDesignSystem/` | `TSDesignSystem` | SwiftUI: `Components/<Name>/`, `Components/Shared/`, `Presentation/` |
| `TSDesignSystemUIKit` | `ios/Sources/TSDesignSystemUIKit/` | `TSDesignSystemUIKit` | UIKit: `Components/<Name>/`, `Components/Shared/` |
| `TSTestSupport` | `ios/Tests/TSTestSupport/` | none (test only) | Shared test helpers: `TSSampleIconImage`, `TSTestAppearance` |
| test targets | `ios/Tests/{TSTokensTests,TSFoundationTests,TSDesignSystemTests,TSDesignSystemUIKitTests}/` | — | UI targets keep their suites in `Behavior/` |

Both UI products `@_exported import TSFoundation` and `TSTokens` (`ios/Sources/TSDesignSystem/Exports.swift`, `ios/Sources/TSDesignSystemUIKit/Exports.swift`), so a consumer imports one module.

**`TSFoundation` folder map:**

| Folder | Holds |
|---|---|
| `Styles/` | `TS<Name>StyleSpec` per component, `TSStyleSpec`, `TSControlState+Make` |
| `Logic/` | `TSInitials`, `TSBadgeCountFormatter`, `TSAvatarImage`, `TSCardAction` (+ `TSCardAccessory`), `TSDialogAction`, `TSDialogPresenter`, `TSListItemRole`, `TSToastQueue`, `TSToastWindowing` |
| `Interaction/` | `TSFocusRing`, `TSHaptics`, `TSHitArea`, `TSPressFeedback`, `TSTextInputTraits` |
| `Compat/` | Every `#available` branch: `TSCompat` (the paths), `TSSheetCompat`, `TSTabBarCompat`, `TSTrackingCompat`, `TSTraitOverridesCompat`, `TSGrowingTextView`, `View+OnChangeCompat`, `View+FocusEffectCompat`, `View+SheetPresentationCompat` |
| `Typography/` | `TSFont`, `TSFont+SwiftUI` (`tsFont(_:)`), `TSTypeRole` (`role.font`), `TSTextColor` |
| `Shadow/` | `TSShadow` |
| `Icons/` | `TSIconImage` |
| `Localization/` | `TSStrings`, `TSStringKey` |
| `Resources/` | `Icons.xcassets`, `Logos.xcassets` (generated), `Localizable.xcstrings` |

**`TSDesignSystem/Presentation/`** (SwiftUI only): `TSSheetBridge`, `TSOverFullScreenBridge`, `TSPresentationCoordinator`, `TSToastOverlayWindow`, `TSToastWindowBridge`.

### `android/` (Gradle project `ts-design-system-android`, `android/settings.gradle.kts`)

| Module | Artifact (local group) | Contents |
|---|---|---|
| `:tokens` | `com.tinysol.designsystem:tokens` | `src/generated/kotlin/com/tinysol/designsystem/tokens/TSTokenValues.kt`, `src/generated/res/values{,-night}/ts_{colors,dimens,integers}.xml`, plus the controller palette `ts_controller_colors.xml` (light + night) and `ts_controller_dimens.xml` |
| `:core` | `com.tinysol.designsystem:core` | `core/{style,logic,input,haptics,icon}`; generated `src/generated/kotlin/com/tinysol/designsystem/core/icon/TSIcon.kt` and `src/generated/res/drawable{,-night}/ts_ic_*`, `ts_logo_*` |
| `:compose` | `com.tinysol.designsystem:compose` | `compose/{theme,foundation,components/<name>,components/shared}`; generated `src/generated/kotlin/com/tinysol/designsystem/compose/theme/TS{Colors,Typography,Spacing,Size,Shapes,Elevation,Motion,ControllerColors,ControllerSize}.kt` |
| `:views` | `com.tinysol.designsystem:views` | `views/{theme,foundation,components/<name>,components/shared}`; `src/main/res/values/{ts_themes,ts_component_attrs,ts_component_styles,ts_views_dimens,ts_views_ids}.xml`; generated `src/generated/res/values{,-v28}/ts_{theme_attrs,theme_colors,text_appearances,shape_appearances}.xml` |
| `:bom` | `com.tinysol.designsystem:bom` | `java-platform` constraining the four libraries at `VERSION` |
| `:golden-check` | never | Compiles the Kotlin and Compose codegen golden fixtures (`tools/codegen/test/fixtures/expected/compose/**`), the Android twin of `make ios-golden-typecheck` |
| `build-logic/convention` | never | Convention plugins `ds.android.library`, `ds.android.compose`, `ds.android.testing`, `ds.publishing`, `ds.api-validation`; constants in `ProjectExtensions.kt` (`DsBuildConstants`) |

The artifact ids are the same on JitPack, where the group is `com.github.lee5783.ts-design-system` (set only when `JITPACK=true`; see [integrations.md](integrations.md#authentication-and-identity)).

Each library module keeps its public API dump in `android/<module>/api/<module>.api`. Tests live in `src/test/kotlin/com/tinysol/designsystem/<module>/{components/<name>,…}`.

Android component folders use the lowercase name: `switch` is `switchcontrol` (a Kotlin keyword), `iconbutton`, `listitem`, `textfield`, `bottomnav`. The Views toolkit has no `icon` component; Views icons are `ImageView` plus `TSIcon.drawableRes` (see `design/specs/components/icon.md`).

### `demos/` (outside the library; consumes it like an external app)

| Path | What |
|---|---|
| `demos/ios-demo-support/` | Local SPM package `TSDemoSupport` (iOS 15): `Catalog/` (`DemoCatalog`, `DemoSection`, `DemoPageID`), `SampleData/`, `Settings/` (`DemoSettingsModel`, `DemoTheme`, `DemoTextSize`, `DemoLaunchOptions`, `DemoLaunchOverrides`, `DemoThemeStore`), `About/DemoBuildInfo`, `Localization/` + `Resources/DemoStrings.xcstrings`. No library dependency |
| `demos/ios-swiftui/` | `TSDemoSwiftUI.xcodeproj` (bundle id `com.tinysol.design-system-swiftui.sample`, scheme `tsds-swiftui`); `TSDemoSwiftUI/{App,Shell,Foundations,Components}`; UI tests in `TSDemoSwiftUIUITests/` |
| `demos/ios-uikit/` | `TSDemoUIKit.xcodeproj` (`com.tinysol.design-system.sample`, `tsds-uikit`); `TSDemoUIKit/{App,Shell,Foundations,Components}`; `TSDemoUIKitUITests/` |
| `demos/android-demo-support/` | Included build `android-demo-support` (coordinate `com.tinysol.designsystem.sample:support`, package `com.tinysol.designsystem.sample.support`): `catalog/`, `sample/`, `settings/`. No library dependency, no toolkit |
| `demos/android-compose/` | Standalone Gradle build, `applicationId` `com.tinysol.designsystem.compose.sample`, scheme `tsds-compose`; `app/src/main/kotlin/com/tinysol/designsystem/compose/sample/{shell,foundations,components}`; `app/src/androidTest/…` |
| `demos/android-views/` | Standalone Gradle build, `com.tinysol.designsystem.views.sample`, scheme `tsds-views`; `app/src/main/kotlin/com/tinysol/designsystem/views/sample/{shell,foundations,components}`; `res/{layout,navigation,menu,anim,…}` |

Both iOS projects use folder-synchronized groups: a new Swift file in a page folder needs no `project.pbxproj` edit.

## Key file locations

- **Entry points:** `Makefile` (start with `make help`); `Package.swift`; `android/settings.gradle.kts`; `demos/*/settings.gradle.kts`; `demos/ios-swiftui/TSDemoSwiftUI/App/TSDemoSwiftUIApp.swift`; `demos/ios-uikit/TSDemoUIKit/App/SceneDelegate.swift`; `demos/android-*/app/src/main/kotlin/com/tinysol/designsystem/*/sample/MainActivity.kt`.
- **Configuration:** `VERSION`, `.xcode-version`, `mise.toml`, `android/gradle/libs.versions.toml`, `android/gradle.properties`, `android/build-logic/convention/src/main/kotlin/ProjectExtensions.kt`, `.github/renovate.json`, `.gitattributes`.
- **Pinned iOS simulator:** `.ios-simulator` (`device=`, `os=`); the Makefile reads it.
- **API baselines:** `android/*/api/*.api`; iOS acknowledgements in `ios/api/breaking-changes.txt` (created by `make ios-api-check-record`).

## Naming conventions

- Swift files: `TS<Name>.swift` (SwiftUI) and `TS<Name>View.swift` (UIKit), one folder per component. Kotlin: `TS<Name>.kt` (Compose) and `TS<Name>View.kt` (Views). Full rules in [conventions.md](conventions.md).
- Demo page files: SwiftUI `<Name>Page.swift`, UIKit `<Name>ViewController.swift`, Compose `<Name>Page.kt`, Views `<Name>Fragment.kt` or `<Name>PageFragment.kt` (the catalog check accepts these names only).
- Docs, specs and other new non-source files: kebab-case (root `CLAUDE.md`).

## Where to add new code

| Adding | Put it in |
|---|---|
| A token | Upstream DS first. Until it ships: `design/extensions/native-tokens.json`, then `make codegen` |
| A component | Follow [guides/adding-a-component.md](guides/adding-a-component.md): spec, style spec in `TSFoundation/Styles` and `:core/style`, four toolkit folders, tests, four demo pages |
| Shared logic | `ios/Sources/TSFoundation/Logic/` and `android/core/src/main/kotlin/com/tinysol/designsystem/core/logic/` |
| An `#available` / `SDK_INT` branch | iOS: a shim in `TSFoundation/Compat/` with a `TSCompat` path; Android: a resource qualifier (`values-v28`) or a `sdkInt` parameter on the helper |
| A SwiftUI-only presentation bridge | `ios/Sources/TSDesignSystem/Presentation/` |
| A tooling step | A new folder under `tools/` with `src/` and `test/`, an npm script in `tools/package.json`, and a Make target |
| Demo-only behaviour | The demo app, or the platform's demo-support module when both demos of that platform need it (toolkit-agnostic code only) |

## Special directories

- **Generated** (never edit; `make codegen-check` fails on drift): `ios/Sources/TSTokens/Generated/`, `ios/Sources/TSFoundation/Resources/{Icons,Logos}.xcassets/`, `android/*/src/generated/`, `design/assets/*/normalized/`.
- **Vendored:** `design/source/` (`linguist-vendored`).
- **Build output (ignored):** `.build/xcode`, `.build/xcode-demos`, `.build/api-check`, `.build/tools/actionlint`, `.build/release`; `android/**/build/`; `tools/node_modules/`.
