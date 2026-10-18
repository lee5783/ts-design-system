# Adding a component

A component is done when it exists in all four toolkits, reads one shared style spec per platform, has spec and behaviour tests, and has a page in all four demo apps. Use the closest existing component as the template (for a control, `Button`; for a container, `Card`; for an overlay, `Dialog` or `Toast`) and mirror its files one for one. Names below use `Chip` / `chip` as the example.

## 1. Write the contract

Create `design/specs/components/chip.md` (kebab-case, matching the DS component). Copy the section layout of an existing spec:

- **Header:** the DS sources (`design/source/components/Chip/README.md`, `bundle.css`, `index.d.ts`), the tokens used, the decisions that apply.
- **API mapping:** one row per DS prop, one column each for the style spec, SwiftUI, UIKit, Compose and Views.
- **Matrix:** `allConfigurations` (variants × sizes × states) and what each state resolves to. Mark estimated values `[est]`.
- **Tests:** one row per layer with the file path.
- **Platform notes:** every intentional difference between toolkits, plus Dynamic Type layout, hit areas, RTL and identifiers.

The spec file is also what the catalog check reads: once it exists, `make demos-check-boundaries` fails until all four demos have a page for it (step 6).

## 2. Tokens

Use DS tokens only. A value the DS hard-codes goes into `design/extensions/native-tokens.json` as a `component` constant with `upstream: "pending"`, then `make codegen` ([ds-update-flow.md](ds-update-flow.md)). Never put a raw number or hex value in a toolkit file.

## 3. Shared layer (once per platform)

| | iOS | Android |
|---|---|---|
| Style spec | `ios/Sources/TSFoundation/Styles/TSChipStyleSpec.swift` | `android/core/src/main/kotlin/com/ts/ds/core/style/TSChipStyleSpec.kt` |
| Shared logic (if any) | `ios/Sources/TSFoundation/Logic/` | `android/core/src/main/kotlin/com/ts/ds/core/logic/` |
| Library strings | `ios/Sources/TSFoundation/Resources/Localizable.xcstrings` + a `TSStringKey` case (`ts.chip.*`) | `android/core/src/main/res/values/ts_strings.xml` (`ts_chip_*`) |
| Spec tests | `ios/Tests/TSFoundationTests/TSChipStyleSpecTests.swift` | `android/core/src/test/kotlin/com/ts/ds/core/style/TSChipStyleSpecTest.kt` |

The spec takes a `Configuration` (including the `TSControlState`), returns a `Resolved` of roles, and lists `allConfigurations`. The spec tests iterate every configuration. Newer-OS behaviour goes through `TSFoundation/Compat/` with a `TSCompat` path ([../conventions.md](../conventions.md)).

## 4. The four toolkits

| Toolkit | Files | Tests |
|---|---|---|
| SwiftUI | `ios/Sources/TSDesignSystem/Components/Chip/TSChip.swift` (+ `TSChipStyle.swift` for a `ButtonStyle`) | `ios/Tests/TSDesignSystemTests/Behavior/TSChipBehaviorTests.swift` |
| UIKit | `ios/Sources/TSDesignSystemUIKit/Components/Chip/TSChipView.swift` | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSChipViewBehaviorTests.swift` |
| Compose | `android/compose/src/main/kotlin/com/ts/ds/compose/components/chip/TSChip.kt` | `android/compose/src/test/kotlin/com/ts/ds/compose/components/chip/TSChipTest.kt` |
| Views | `android/views/src/main/kotlin/com/ts/ds/views/components/chip/TSChipView.kt`; `app:ts*` attrs in `android/views/src/main/res/values/ts_component_attrs.xml`; default styles in `ts_component_styles.xml` | `android/views/src/test/kotlin/com/ts/ds/views/components/chip/TSChipViewTest.kt` |

Rules that every component meets (details in [../conventions.md](../conventions.md)): the C-29 names (`TSChip` / `TSChipView`); 44 pt / 48 dp hit areas; the TS focus ring; RTL; Dynamic Type and font scale; strings from the shared layer; an identifier / test tag; no screen-reader semantics (no labels, values, traits, announcements or custom actions); Android behaviour tests at `@Config(sdk = [26, 36])`; state forcing in tests through `forcingState` / `LocalTSForcedControlState`.

## 5. Test, then check the API

```bash
make ios-build-for-testing
IOS_TESTS="TSFoundationTests/TSChipStyleSpecTests TSDesignSystemTests/TSChipBehaviorTests TSDesignSystemUIKitTests/TSChipViewBehaviorTests" make ios-test-only
cd android && ./gradlew :core:testDebugUnitTest --tests '*TSChipStyleSpecTest' :compose:testDebugUnitTest --tests '*TSChipTest' :views:testDebugUnitTest --tests '*TSChipViewTest'
cd android && ./gradlew apiDump    # commit the changed android/*/api/*.api files
make api-check BASELINE=<last-release-tag>
```

New public API is additive (minor). Mark unstable API `@ExperimentalTSApi` (Kotlin) or `@_spi(Experimental)` (Swift) ([deprecation-policy.md](deprecation-policy.md)).

## 6. Add the page to all four demos

1. **Catalog (both platforms):**
   - iOS: a case in `demos/ios-demo-support/Sources/TSDemoSupport/Catalog/DemoPageID.swift` (raw value `chip`; kebab-case for multi-word ids such as `icon-button`), the id in a section of `DemoCatalog.swift`, and a `demo.page.chip` title in `Resources/DemoStrings.xcstrings`.
   - Android: an entry `Chip("chip", DemoPageKind.Component, R.string.demo_page_chip)` in `demos/android-demo-support/src/main/kotlin/com/ts/ds/demo/support/catalog/DemoPageId.kt`, the id in a section of `DemoCatalog.kt`, and `demo_page_chip` in `src/main/res/values/demo_strings.xml`.
2. **Page files** (the catalog check accepts exactly these names, in `Components/` or `Foundations/`):

   | Demo | Page file | Route it in |
   |---|---|---|
   | SwiftUI | `demos/ios-swiftui/TSDemoSwiftUI/Components/ChipPage.swift` | `Shell/DemoPageView.swift` |
   | UIKit | `demos/ios-uikit/TSDemoUIKit/Components/ChipViewController.swift` | `Shell/DemoPageFactory.swift` |
   | Compose | `demos/android-compose/app/src/main/kotlin/com/ts/ds/demo/compose/components/ChipPage.kt` | `shell/DemoPageScreen.kt` |
   | Views | `demos/android-views/app/src/main/kotlin/com/ts/ds/demo/views/components/ChipFragment.kt` (or `ChipPageFragment.kt`) + `res/layout/page_chip.xml` | `shell/DemoNavigation.kt` and `res/navigation/demo_nav.xml` (`@+id/page_chip`) |

   The iOS projects use folder-synchronized groups, so a new file needs no `project.pbxproj` edit.
3. **Content:** every variant, size and state, using the shared sample data; follow the sibling page's sections (`DemoComponentSection` / `DemoPageViewController` / `DemoPageFragment` with `DemoPageState`). Interactive demos show a result caption that the UI tests can read.
4. **UI tests:** extend the matching component suite in each demo (`ActionComponentsTests`, `FormComponentsTests`, `ContentComponentsTests`, `FeedbackComponentsTests` or `NavigationComponentsTests`, and the `…Test` twins on Android). Find elements by identifier / test tag and visible text. The page walk suites pick the new page up from the catalog.
5. **Check:** `make demos-check-boundaries` (boundaries plus the catalog cross-check), `make demos-build`, the demo test classes you extended, and argent screenshots of the new page on the pinned iOS simulator and one Android emulator at API 36.

## 7. Finish

- Update the spec's *Tests* table with the real file names.
- Note any new OS fallback in [../architecture.md](../architecture.md) and any known gap in [../concerns.md](../concerns.md).
- Use a conventional commit (`feat(chip): …`) so the changelog groups it under Features ([release.md](release.md)).
