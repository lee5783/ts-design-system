# Theming

The TS palette, type scale and spacing are fixed by the design system: there is no brand override, no dynamic colour (Material You is never applied) and no high-contrast theme yet. What a consumer controls is the **appearance** (light, dark, or follow the system), a few **behaviour switches**, and the **library strings**.

## Appearance

| Toolkit | Default | To force light or dark |
|---|---|---|
| SwiftUI | follows the system: every `TSColor` is a dynamic colour | set `overrideUserInterfaceStyle` on the **window** (and `preferredColorScheme` at the root if you like). The window override is what reaches the toast overlay window and the iOS 15 sheet bridge, which do not inherit a SwiftUI-only preference |
| UIKit | follows the system | `overrideUserInterfaceStyle` on every window of the scene, not per view controller, so presented dialogs and `TSToastWindow` follow |
| Compose | `TSTheme(darkTheme = isSystemInDarkTheme())` | pass `darkTheme = true/false`; set matching system bar styles (`enableEdgeToEdge(SystemBarStyle…)`) |
| Views | `Theme.TS` is a DayNight theme | `AppCompatDelegate.setDefaultNightMode(…)` (recreates the Activity) |

A UIKit `TSDialogViewController` follows live appearance and text-size changes of its window; it pins a trait only when the presenting controller has its own override.

The demo apps implement exactly this, plus a quick toggle and deep links ([../guides/running-demos.md](../guides/running-demos.md#demo-theme-independent-of-the-device-c-42)).

## Using tokens in your own UI

| Toolkit | Colour | Type | Spacing / radius |
|---|---|---|---|
| SwiftUI | `Color.tsAccentPrimary`, `TSColor.accentPrimary.color` | `Text("…").tsFont(.body)` (Dynamic Type, line height, tracking) | `TSSpacing.space4`, `TSRadius.radiusLg` |
| UIKit | `UIColor.tsAccentPrimary`, `TSColor.accentPrimary.uiColor` | `TSFont.body.uiFont(compatibleWith: traitCollection)`, `TSFont.body.attributedString(…)` | `TSSpacing.space4`, `TSRadius.radiusLg` |
| Compose | `TSTheme.colors.accentPrimary` | `TSTheme.typography.body` | `TSSpacing.space4`, `TSShapes.radiusLg` |
| Views | `?attr/tsColorAccentPrimary`, `@color/ts_accent_primary` | `@style/TextAppearance.TS.Body` | `@dimen/ts_space_4`, `@dimen/ts_radius_lg` |

Token names follow the DS names (`accent-primary`, `space-4`, `radius-lg`); see [../conventions.md](../conventions.md#naming-patterns) for the mapping rules and the demo apps' Foundations pages for every value.

## Controller and media-overlay colours

UI drawn **over live media or as a game controller** (a virtual gamepad over video, a translucent "Menu" chip over a video frame, a full-screen remote-controller canvas) uses a separate palette. It is kept out of `TSColors` / `TSColor` / `ts_colors.xml` and the `?attr/tsColor*` theme attributes, so apps without a controller keep the small main palette. The tokens live in `design/extensions/native-tokens.json` (family `controller-color`, `upstream: "pending"`).

| Token | Member | Light | Dark | Use |
|---|---|---|---|---|
| `controller-backdrop` | `backdrop` | `#fcfcfc` | `#121416` | Canvas behind a full-screen controller |
| `controller-key` | `key` | `#18181b29` | `#ffffff29` | Idle fill of a control |
| `controller-key-pressed` | `keyPressed` | `#18181b6b` | `#ffffff6b` | Fill of a held control |
| `controller-key-outline` | `keyOutline` | `#18181b99` | `#ffffff99` | Control boundary; carries the 3:1 non-text contrast |
| `controller-key-label` | `keyLabel` | `#18181b` | `#ffffff` | Text on a control (A, B, START) |
| `controller-backing` | `backing` | `#fcfcfce6` | `#000000ec` | Plate under each control so it reads over any video frame |
| `overlay-scrim-media` | `overlayScrimMedia` | `#fcfcfc99` | `#00000099` | Translucent chip or backdrop over live media. Not `overlay`, which is the modal scrim (dark in both themes) |
| `text-on-overlay-media` | `textOnOverlayMedia` | `#18181b` | `#ffffff` | Text on `overlay-scrim-media` |
| `controller-outline-width` | `outlineWidth` | 1.5 dp / pt | | Stroke of `keyOutline` |

Values are `#RRGGBBAA`. Contrast is checked by codegen tests (`tools/codegen/test/unit/merge-extensions.test.ts`), alpha-composited over both a black and a white video frame in each theme: every label pair (idle and pressed key label, media text) is at least 4.5:1 and the outline at least 3:1 against the backing.

| Toolkit | Colours | Outline width |
|---|---|---|
| Compose | `TSTheme.controllerColors.keyPressed` (`TSControllerColors`, follows `TSTheme(darkTheme)` like `TSTheme.colors`; `lightTSControllerColors()` / `darkTSControllerColors()` for a fixed theme) | `TSControllerSize.outlineWidth` (`Dp`) |
| SwiftUI | `TSControllerColor.keyPressed.color` (dynamic light/dark). iOS has no `TSTheme`, so this enum is the twin of `TSTheme.controllerColors` | `TSControllerSize.outlineWidth` (`CGFloat`, pt) |
| UIKit | `TSControllerColor.keyPressed.uiColor` (dynamic) | `TSControllerSize.outlineWidth` |
| Views | `@color/ts_controller_key_pressed` (resource names keep the full token: `ts_controller_*`, `ts_overlay_scrim_media`, `ts_text_on_overlay_media`; light in `values/`, dark in `values-night/`). No theme attributes | `@dimen/ts_controller_outline_width` |

There is no media-overlay variant of `TSButton`; every TS button variant stays opaque.

### Content colours, not chrome

These colours are part of the picture, not UI chrome, so they are **deliberately not tokens** and stay fixed in both themes:

- **Video letterbox:** always `#000000`.
- **Camera-preview scrim:** opaque `#000000` behind a scanner preview.
- **QR codes:** black modules on a white background in both themes, for scanner reliability.

Hard-code them in the app; do not map them to a TS role, and do not invert them in dark mode.

## Material interop (Android)

- **Compose:** `TSTheme` also provides a Material3 `MaterialTheme` colour scheme mapped from the TS colours (`TSMaterialBridge`), so plain M3 components inside it use the TS palette. TS components never read `MaterialTheme` directly.
- **Views:** `Theme.TS` maps the Material3 and AppCompat attributes (`colorPrimary`, `colorSurface`, …) to `?attr/tsColor*`, turns the M3 elevation overlay off, and styles sheets, dialogs and menus with `surface-elevated`. To use TS components inside an app whose theme is not `Theme.TS`, apply `ThemeOverlay.TS` to the container (`android:theme="@style/ThemeOverlay.TS"`).

## Behaviour switches

| Switch | iOS | Compose | Views |
|---|---|---|---|
| Pressed feedback | flat pressed fill (fixed) | flat fill; `TSTheme(rippleEnabled = true)` for a ripple | flat fill |
| Haptics | `TSHaptics.shared.isEnabled = false` turns tone and toggle haptics off | `TSTheme(hapticsEnabled = false)` | follows the system haptic setting |
| Reduce motion | follows the system setting | follows the system setting | follows the system setting |

## Overriding library strings

The library ships English strings for accessibility labels and built-in text ("Close", "Loading", "Show password", tone names, "n of N", …).

- **iOS:** add a string table named **`TSDesignSystem`** to your app's main bundle (`TSDesignSystem.xcstrings` or `TSDesignSystem.strings`) with any of the keys from `TSStringKey` (`ios/Sources/TSFoundation/Localization/TSStrings.swift`), for example `ts.close`, `ts.loading`, `ts.alert.danger`, `ts.tab.position`. Keys you do not define fall back to the library's English text. Format strings keep their placeholders (`ts.position` takes two integers).
- **Android:** define the same resource name in your app (`res/values-<locale>/strings.xml`), for example `ts_close`, `ts_loading`, `ts_position_of_total`; app resources override library resources. The full list is `android/core/src/main/res/values/ts_strings.xml`.

String keys are not covered by the API checks, so a renamed key would be listed in the [migration guides](migration-guides.md).
