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
| SwiftUI | `Color.tsAccentText`, `TSColor.accentText.color` | `Text("…").tsFont(.body)` (Dynamic Type, line height, tracking) | `TSSpacing.space4`, `TSRadius.radiusLg` |
| UIKit | `UIColor.tsAccentText`, `TSColor.accentText.uiColor` | `TSFont.body.uiFont(compatibleWith: traitCollection)`, `TSFont.body.attributedString(…)` | `TSSpacing.space4`, `TSRadius.radiusLg` |
| Compose | `TSTheme.colors.accentText` | `TSTheme.typography.body` | `TSSpacing.space4`, `TSShapes.radiusLg` |
| Views | `?attr/tsColorAccentText`, `@color/ts_accent_text` | `@style/TextAppearance.TS.Body` | `@dimen/ts_space_4`, `@dimen/ts_radius_lg` |

The colour column shows `accent-text`, the role for accent-coloured **text** (links, active labels). `accent-primary` is a **fill** colour (primary button background, checkbox, switch, cursor): in dark mode it is only about 2.5:1 against the surfaces, so never draw text in it. Token names follow the DS names (`accent-text`, `space-4`, `radius-lg`); see [../conventions.md](../conventions.md#naming-patterns) for the mapping rules and the demo apps' Foundations pages for every value.

## Text contrast per surface

WCAG 2.x contrast ratios of each text role against each surface role, computed from the token hex values in `design/source/tokens.json` (relative-luminance formula, rounded to two decimals). **✓** = at least 4.5:1 (normal text, WCAG AA). There is no `surface-sunken` token: the recessed surface is `background-secondary`.

**Dark theme**

| Text role | `background-primary` `#1e1e1e` | `background-secondary` `#161616` | `surface-card` `#262626` | `surface-elevated` `#2c2c2c` | `accent-subtle` `#2a2140` | `surface-hover` `#313131` ¹ | `surface-pressed` `#383838` ¹ |
|---|---|---|---|---|---|---|---|
| `text-primary` `#dcddde` | 12.26 ✓ | 13.31 ✓ | 11.13 ✓ | 10.27 ✓ | 11.11 ✓ | 9.57 | 8.62 |
| `text-secondary` `#b4b4b6` | 8.05 ✓ | 8.74 ✓ | 7.31 ✓ | 6.75 ✓ | 7.30 ✓ | 6.28 | 5.66 |
| `text-muted` `#949494` | 5.50 ✓ | 5.97 ✓ | 4.99 ✓ | 4.60 ✓ | 4.98 ✓ | 4.29 | 3.87 |
| `accent-text` `#a78bfa` | 6.13 ✓ | 6.65 ✓ | 5.56 ✓ | 5.13 ✓ | 5.55 ✓ | 4.78 | 4.31 |
| `accent-primary` `#7c3aed` (not a text role) | 2.93 ✗ | 3.18 ✗ | 2.66 ✗ | 2.45 ✗ | 2.65 ✗ | 2.28 | 2.06 |

**Light theme**

| Text role | `background-primary` `#fcfcfc` | `background-secondary` `#f4f4f5` | `surface-card` `#ffffff` | `surface-elevated` `#ffffff` | `accent-subtle` `#f5f3ff` | `surface-hover` `#efeff1` ¹ | `surface-pressed` `#e4e4e7` ¹ |
|---|---|---|---|---|---|---|---|
| `text-primary` `#18181b` | 17.27 ✓ | 16.12 ✓ | 17.72 ✓ | 17.72 ✓ | 16.15 ✓ | 15.43 | 13.96 |
| `text-secondary` `#3f3f46` | 10.18 ✓ | 9.50 ✓ | 10.44 ✓ | 10.44 ✓ | 9.52 ✓ | 9.09 | 8.23 |
| `text-muted` `#71717a` | 4.71 ✓ | 4.40 ✗ | 4.83 ✓ | 4.83 ✓ | 4.41 ✗ | 4.21 | 3.81 |
| `accent-text` `#7c3aed` | 5.55 ✓ | 5.18 ✓ | 5.70 ✓ | 5.70 ✓ | 5.20 ✓ | 4.96 | 4.49 |

¹ **Outside the guarantee.** `surface-hover` and `surface-pressed` are transient state fills (highlight, press), not resting surfaces, so no text role is guaranteed on them. Several pairs drop below 4.5:1 there (dark `accent-text` 4.31 and `text-muted` 3.87 on pressed; light `accent-text` 4.49 and `text-muted` 3.81 on pressed).

**Guarantees** (resting surfaces, at least 4.5:1):

| Text role | Guaranteed on |
|---|---|
| `text-primary`, `text-secondary` | `background-primary`, `background-secondary`, `surface-card`, `surface-elevated`, `accent-subtle`, both themes (they also clear 4.5:1 on hover and pressed, but those stay outside the guarantee) |
| `accent-text` | `background-primary`, `background-secondary`, `surface-card`, `surface-elevated`, `accent-subtle`, both themes |
| `text-muted` | dark: the same five surfaces. Light: `background-primary`, `surface-card`, `surface-elevated` only. On light `background-secondary` and `accent-subtle` (4.40 / 4.41) keep it to large text (14 pt / 18.66 px bold or 18 pt / 24 px regular and up), or use `text-secondary` |
| `accent-primary` | never as text. It is a fill; text on it uses `text-on-accent` (5.70:1) |

Every TS component that draws accent-coloured text (link button, selected tab, selected bottom-nav item, list-item accent tile, accent badge, avatar initials, text-field reveal icon) uses `accent-text`, on both platforms. Dim violet text in an app almost always means app code drew text in `accent-primary` directly or through Material (next section).

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

Values are `#RRGGBBAA`. The palette was designed so that, alpha-composited over both a black and a white video frame in each theme, every label pair (idle and pressed key label, media text) is at least 4.5:1 and the outline at least 3:1 against the backing. No test asserts this: the repo has no contrast tooling, so re-check the pairs by hand when these tokens change.

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
- **Accent text is `accentText`, never Material `primary`.** In both bridges Material's primary colour resolves to `accent-primary`: Compose `MaterialTheme.colorScheme.primary` (also `secondary`, `tertiary`; `TSMaterialBridge.kt`) and Views `?attr/colorPrimary` / `colorSecondary` (`ts_themes.xml`). That is right for fills, but text drawn in it is 2.45:1 on dark `surface-elevated` (see [Text contrast per surface](#text-contrast-per-surface)). Material components that draw text in `primary` include M3 `TextButton`, `OutlinedButton` and links. For accent-coloured text use `TSTheme.colors.accentText` / `?attr/tsColorAccentText`, or a TS component (`TSButton` / `TSButtonView` with the `link`, `secondary` or `ghost` variant). The Views demo settings sheet (`demos/android-views/app/src/main/res/layout/sheet_demo_settings.xml`) is the worked example: its Reset action is a `TSButtonView` with `app:tsVariant="secondary"`, not a Material `OutlinedButton`, whose `colorPrimary` label was illegible on the dark sheet.

## Behaviour switches

| Switch | iOS | Compose | Views |
|---|---|---|---|
| Pressed feedback | flat pressed fill (fixed) | flat fill; `TSTheme(rippleEnabled = true)` for a ripple | flat fill |
| Haptics | `TSHaptics.shared.isEnabled = false` turns tone and toggle haptics off | `TSTheme(hapticsEnabled = false)` | follows the system haptic setting |
| Reduce motion | not read by the library; DS animations always run | not read by the library | not read by the library (platform animators still follow the system animation scale) |

## Overriding library strings

The library ships English strings for its built-in text ("Close", "Dismiss", "Loading", "Show password", "Hide password", the badge overflow "n+").

- **iOS:** add a string table named **`TSDesignSystem`** to your app's main bundle (`TSDesignSystem.xcstrings` or `TSDesignSystem.strings`) with any of the keys from `TSStringKey` (`ios/Sources/TSFoundation/Localization/TSStrings.swift`), for example `ts.close`, `ts.loading`, `ts.text-field.show-password`. Keys you do not define fall back to the library's English text. Format strings keep their placeholders (`ts.count.overflow` takes one integer).
- **Android:** define the same resource name in your app (`res/values-<locale>/strings.xml`), for example `ts_close`, `ts_loading`, `ts_badge_count_overflow`; app resources override library resources. The full list is `android/core/src/main/res/values/ts_strings.xml`.

String keys are not covered by the API checks, so a renamed key would be listed in the [migration guides](migration-guides.md).
