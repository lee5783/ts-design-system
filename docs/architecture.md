# Architecture

**Analysis date:** 2026-10-16

## Pattern overview

**Overall:** a generated-token, shared-spec design-system monorepo with four native UI toolkits built in parallel.

**Key characteristics:**

- One checked-in copy of the DS (`design/`) feeds a tested generator (`tools/codegen`) that writes every token and icon file for both platforms.
- Each platform has one non-rendering shared layer (`TSFoundation` on iOS, `:core` on Android) that resolves variant × size × state into tokens. Both UI toolkits of a platform read the same spec.
- The UI toolkits are **native parallel implementations** (C-06): SwiftUI and UIKit render natively, and so do Compose and Views. No toolkit wraps another.
- Library targets have **no third-party runtime dependency**. Test-only packages stay on test targets.
- iOS 15 and Android API 26 are the minimums. Newer-OS APIs go through compat shims (tables below).
- The four demo apps sit outside the library (`demos/`) and consume only the public products and coordinates.

## Layers

```
iOS                                          Android
TSTokens (generated)                         :tokens (generated values + res)
   ▲                                            ▲
TSFoundation (specs, logic, compat)          :core (style specs, logic, strings, icons, haptics)
   ▲                  ▲                         ▲                  ▲
TSDesignSystem   TSDesignSystemUIKit         :compose (M3 impl)   :views (AppCompat + MDC)
───────────────────── library boundary (public products / com.ts.ds coordinates) ─────────────────────
demos/ios-swiftui   demos/ios-uikit          demos/android-compose   demos/android-views
   └──── TSDemoSupport ────┘                    └──── demo-support ────┘   (toolkit-agnostic, no library dependency)
```

| Layer | Path | Responsibility | Depends on |
|---|---|---|---|
| Tokens | `ios/Sources/TSTokens/Generated/`, `android/tokens/src/generated/` | Generated values: colours (light/dark), spacing, radius, size, opacity, shadow, typography, motion, component constants | nothing |
| Shared spec layer | `ios/Sources/TSFoundation/`, `android/core/` | `TS<Name>StyleSpec` (role-based: colour roles, type roles, shadow roles), `TSControlState`, presenters and queues, strings, icon mapping, compat shims, haptics (`TSHaptics` / `DsHaptics`) | tokens |
| SwiftUI | `ios/Sources/TSDesignSystem/` | Views, styles, modifiers, presentation bridges | foundation, tokens |
| UIKit | `ios/Sources/TSDesignSystemUIKit/` | `UIControl` / `UIView` / view-controller types | foundation, tokens |
| Compose | `android/compose/` | `TSTheme`, composables, M3 bridge (`TSMaterialBridge`) | `:core` (api), Compose BOM (api), Material3 (implementation) |
| Views | `android/views/` | `Theme.TS`, `TS<Name>View`, dialog fragments, toast host | `:core`, AppCompat, MDC, RecyclerView (all api) |

Rules:

- Dependencies point downward only. **SwiftUI never imports `TSDesignSystemUIKit`; Compose never depends on `:views`** (and the reverse). A SwiftUI component may call UIKit *framework* APIs internally (the presentation bridges do).
- Style specs are unit-tested once in the shared layer; behaviour tests then cover each toolkit. This is the main defence against parity drift.
- `TSFoundation` helpers and the `:core` specs are **public API** (C-43, C-44 U-2), frozen by the API checks from the first tag.

## Data flow

### DS change → shipped code

1. `make ds-sync FROM=<export> DS_VERSION=<v>` copies the artifact into `design/source/` and the SVGs into `design/assets/*/source/`, and rewrites `design/manifest.json`.
2. `make codegen` loads `tokens.json` (`load-ts-tokens`), drops web-only families, merges `design/extensions/native-tokens.json` and its `renames` (deprecated aliases), and writes Swift, Android XML, Compose Kotlin and icon assets.
3. `codegen diff-report` classifies the token change and suggests a semver bump.
4. Visual changes are reviewed through argent screenshots of the four demo apps (pinned iOS simulator, one Android emulator at API 36); no test compares pixels.
5. One lockstep release (`VERSION`) ships both platforms. Full procedure: [guides/ds-update-flow.md](guides/ds-update-flow.md), [guides/release.md](guides/release.md).

### Component rendering

`TS<Name>StyleSpec.resolve(_ configuration:)` (iOS, the configuration carries the `TSControlState`) / `TS<Name>StyleSpec.resolve(configuration, state)` (Android) returns a `Resolved` value of roles; the toolkit maps roles to tokens (`TSColor` / `TSTheme.colors` / `?attr/tsColor*`) and draws natively. Interaction state comes from `TSControlState.make(…)` on iOS and `TSControlState` on Android; tests force a state through the internal forcing hooks (SwiftUI `forcingState`, Compose `LocalTSForcedControlState`).

### State management

- iOS uses `ObservableObject` / `@Published` everywhere (Observation is iOS 17 and is not used).
- Library-owned singletons: `TSToastQueue.shared`, `TSDialogPresenter.shared` (both platforms). Consumers can inject their own queue, presenter and scheduler.

## Key abstractions

### Dialog presenter (one per process, both platforms)

`TSDialogPresenter` (`ios/Sources/TSFoundation/Logic/TSDialogPresenter.swift`, `android/core/src/main/kotlin/com/ts/ds/core/logic/TSDialogPresenter.kt`) enforces the DS rule "never stack dialogs": one dialog on screen at a time, shared by all four toolkits. Its `policy` (`TSDialogPolicy`) is `queue` by default: a declarative request (`request(owner, onGranted)`, used by SwiftUI `.tsDialog` and Compose `TSDialog`) that conflicts is kept in a weakly held FIFO and granted once the current dialog has fully dismissed; under `refuse` it is refused and the caller's binding is reset. Imperative presentations (`begin`, used by UIKit `present(from:)` and Views `present(manager, tag)`) are refused under either policy. `end(owner)` releases the slot and grants the next pending owner, or withdraws a pending request; `pendingCount`, `isPending` and `refusedCount` observe it. The owner is held **weakly**: a deallocated owner frees the presenter or drops out of the queue. A dismissing action's handler runs **before** dismissal, while the dialog still holds the slot; a follow-up dialog requested from it is queued (Compose, SwiftUI) or handed the slot when the first finishes (UIKit deferred `present(from:)`, Views `handOver`), so actions still chain. iOS actions can add an `afterDismissal` step for presenting non-TS UI. Android dialog windows inherit the host Activity's hidden system bars (immersive mode). The full contract, including queue semantics and the per-toolkit action order, is `design/specs/components/dialog.md`.

Routes per OS:

| Toolkit | Center | Sheet |
|---|---|---|
| SwiftUI | `TSOverFullScreenBridge` (`.overFullScreen` + cross-dissolve) on every OS | native `.sheet` on 16.4+; `TSSheetBridge` (`UISheetPresentationController`) on 15–16.3 |
| UIKit | `TSDialogViewController`, `.overFullScreen` + cross-dissolve | `.pageSheet` + `UISheetPresentationController` (fit + large on 16+, medium + large on 15) |
| Compose | `Dialog` window with the DS scrim; `PredictiveBackHandler` | M3 `ModalBottomSheet` |
| Views | `TSDialogFragment` (`ThemeOverlay.TS.CenterDialog`) | `TSBottomSheetDialogFragment` (MDC `BottomSheetDialog`) |

The route logic is `TSDialogRoute.resolve(variant:sheetPath:)` (SwiftUI). UIKit dialogs inherit the source's appearance, text size and layout direction live; they pin a trait only when the source has its own explicit override (`overrideUserInterfaceStyle`, a parent's `overrideTraitCollection(forChild:)`, a presentation controller override, or `traitOverrides` on iOS 17+ through `TSTraitOverridesCompat`). The full contract is `design/specs/components/dialog.md`.

### Toast overlay model

The library owns the toast queue, the timer and the host (C-10). Data flows **queue → presenter (timer, show once) → host → layer**:

- **Queue:** `TSToastQueue` (FIFO, one `current` item). The timeout is fixed: 4 s, or 10 s when the toast has an action, on both platforms (iOS `TSToastTimeout.standard` / `.extended` via `TSToastQueue.timeout(for:)`, which also honours a longer custom `duration`; Android `TSToastTimeouts.DEFAULT_MILLIS` / `EXTENDED_MILLIS`, fixed at enqueue). It does not depend on screen-reader state.
- **iOS hosts are separate windows.** SwiftUI `.tsToastHost()` opens an internal `TSToastOverlayWindow`; UIKit uses the public `TSToastWindow`. Both sit at `UIWindow.Level.alert + 1`, are never key, pass touches through outside the toast, and therefore cover any sheet or dialog presented in the app window. The overlay window does its own keyboard avoidance (`TSToastWindowing.keyboardInset` → `additionalSafeAreaInsets.bottom`; UIKit pins to `keyboardLayoutGuide`), including a floating iPad keyboard in SwiftUI.
- **Android hosts are layers.** Compose `TSToastHost(state) { content }` draws a pass-through layer over the content, and each `TSDialog` adds its own layer inside its window. Views `TSToastHost.install(activity)` adds a layer to `android.R.id.content`; `TSDialogFragment` / `TSBottomSheetDialogFragment` attach an overlay layer in `onStart`. The top-most layer shows the toast, so a toast opened under a dialog moves into the dialog's window.
- **One host per scene (iOS) or Activity (Android).** Toasts post no accessibility announcements.

Full contract, timing tables and hosts: `design/specs/components/toast.md`.

### App shell (C-09)

The DS bottom bar is custom on both platforms; the library never shows a system tab bar or an M3 / MDC navigation bar.

- SwiftUI `TSBottomNavShell` has no `TabView`. UIKit `TSTabBarController` hides the system bar on every layout pass and, on iOS 18+, through `setTabBarHidden` (`TSTabBarCompat`); children get a 64 pt bottom inset. This also keeps behaviour identical under iOS 26 Liquid Glass.
- Android ships no shell. Apps place `TSBottomNav` / `TSBottomNavView` themselves; toasts avoid it through `bottomInset` (Compose, Views) or `anchorView` (Views).

### Disabled dimming

Containers own disabled dimming: a disabled card dims its content once, and nested controls skip their own dimming (`isDimmedByContainer` on UIKit and Views buttons and icon buttons, `tsDimmedByContainer` in SwiftUI, `LocalTSDimmedByContainer` in Compose).

## Minimum-OS compatibility

Every newer-OS API goes through a shim, and tests force the fallback path (`TSCompatPath` parameters on iOS, `@Config(sdk = [26, …])` on Android).

### iOS: APIs newer than iOS 15

| API (min iOS) | Used for | Rule / shim |
|---|---|---|
| `presentationDetents`, `presentationDragIndicator` (16); `presentationCornerRadius`, `presentationBackground` (16.4) | SwiftUI sheet dialog | 16.4+: native `.sheet` via `View+SheetPresentationCompat` (`TSCompat.sheetPresentation`). 15–16.3: `TSSheetBridge` with `TSSheetCompat` detents |
| Custom (fit) sheet detents (16) | Sheet height | `TSCompat.customSheetDetents`: fit + large on 16+, `.medium()` + `.large()` on 15 |
| `fullScreenCover` with a clear background (16.4) | Center dialog | Not used; `TSOverFullScreenBridge` on every OS |
| Observation `@Observable` (17) | Queue and presenter state | Not used; `ObservableObject` |
| `NavigationStack` (16) | SwiftUI demo only | `DemoNavigationContainer`: `NavigationView` on 15 |
| `ViewThatFits`, `Layout` (16) | ListItem reflow, BottomNav widths | Not used; `dynamicTypeSize.isAccessibilitySize` and `frame(maxWidth: .infinity)` |
| `TextField(axis:)`, `lineLimit(_:)` ranges (16) | Multiline text field | `TSCompat.multilineTextField`: native on 16+, `TSMultilineTextView` (wrapping `TSGrowingTextView`) on 15 |
| `focusEffectDisabled` (17) | TS focus ring | `View+FocusEffectCompat` (`tsFocusEffectDisabled()`); on 15–16 the system effect stays (accepted difference) |
| `View.tracking` / `View.fontWeight` (16) | Typography | `TSTrackingCompat` (`tsTracking`, `tsFontWeight` on `Text`) |
| `onChange(of:initial:_:)` (17) | State reactions | `View+OnChangeCompat` (`tsOnChange`) |
| `traitOverrides` (17) | UIKit dialog trait inheritance | `TSTraitOverridesCompat` |
| `setTabBarHidden` (18) | UIKit shell | `TSTabBarCompat` |
| `sensoryFeedback` (17) | Haptics | Not used; feedback generators on every OS (`TSHaptics`) |
| `LocalizedStringResource` (16) | Strings | Not used; `String(localized:table:bundle:)` |
| Liquid Glass tab bar (26) | BottomNav | System bar hidden on every OS |
| Floating inset sheet (26) | Sheet dialog | System geometry on iOS 26; see `design/specs/components/dialog.md` |

The `TSCompat` paths live in `ios/Sources/TSFoundation/Compat/TSCompat.swift` (`onChange`, `focusEffect`, `multilineTextField`, `sheetPresentation`, `customSheetDetents`, `traitOverrides`, `tabBarHidden`).

### Android: APIs newer than API 26

| API (min level) | Used for | Rule |
|---|---|---|
| `android:lineHeight` (28) | Text appearances | Generated styles emit both the platform and the AppCompat/MDC attr; Compose `TextStyle.lineHeight` works on all levels |
| Weight 600 via `Typeface.create(family, weight, italic)` (28) | title-2, title-3, overline, Alert title, initials, badges, toast action | 28+: true 600. 26–27: Medium 500. Compose: `DsFontWeights` / `withDsFontWeight` switch to the `sans-serif-medium` family below 28. Views: `values-v28/ts_text_appearances.xml` vs `values/` with `sans-serif-medium` |
| Shadow colour (`outlineSpotShadowColor`, Compose `shadow(spotColor)`) (28) | Shadow tint | 28+: tinted; 26–27: default black |
| `HapticFeedbackConstants` `CONFIRM`/`REJECT` (30), `TOGGLE_ON`/`TOGGLE_OFF`/`SEGMENT_TICK` (34) | Haptics | `DsHaptics.constantFor(event, sdkInt)` maps to older constants |
| Predictive back (33/34) | Dialogs | `OnBackPressedDispatcher` / `PredictiveBackHandler` with a plain back fallback |
| `Configuration.isNightModeActive` (30) | Theme checks | `uiMode and UI_MODE_NIGHT_MASK` |
| Monochrome adaptive icon (33) | Demo launchers | Ignored below 33 |
| Edge-to-edge | All screens | `enableEdgeToEdge()`; insets through `ViewCompat` / `WindowInsets` |

## Entry points

- **Consumers:** the SPM products `TSDesignSystem`, `TSDesignSystemUIKit`, `TSTokens`; the Maven coordinates `com.ts.ds:{bom,compose,views,core,tokens}` (see [consumers/](consumers/ios-getting-started.md)).
- **Maintainers:** `make help`. Every CI step is one `make` call.

## Error handling

- Generator and tooling errors stop with a message that names the file and the fix (for example the duplicate-key error in `merge-extensions.ts`, or `release-verify` refusals).
- Library APIs do not throw for presentation conflicts: a conflicting SwiftUI or Compose dialog is queued (default) or, under the `refuse` policy, calls `onRefused`; UIKit `present(from:)` and Views `present(manager, tag)` return `false`.

## Cross-cutting concerns

- **Accessibility (layout only):** Dynamic Type and font scale, a keyboard focus ring, 44 pt / 48 dp hit areas, RTL, and identifiers / test tags on every component. The library authors no screen-reader semantics (labels, values, roles, announcements, custom actions) and does not follow reduce motion; screen readers read the visible text and native controls. UIKit cards, list rows, tabs, bottom-nav items, checkboxes, switches, alerts and toasts are not accessibility elements themselves, so the visible text inside them stays reachable (and findable by XCUITest).
- **RTL:** supported from v1; directional icons mirror (`icons.meta.json` → asset and VectorDrawable flags).
- **Strings:** English base, overridable by consumers (see [consumers/theming.md](consumers/theming.md)).
- **Demo theme (C-42, demos only):** each demo stores one theme (`system`, `light`, `dark`) in its demo-support settings and applies it at window or Activity level, so library overlays follow: SwiftUI `preferredColorScheme` plus `overrideUserInterfaceStyle` on the window; UIKit `overrideUserInterfaceStyle` on every window of the scene; Compose `TSTheme(darkTheme)` plus system bar styles; Views `AppCompatDelegate.setDefaultNightMode` with an Activity recreate. It can be set from outside through a launch argument, an intent extra or a deep link ([guides/running-demos.md](guides/running-demos.md)). There is no library API for it.
