# Button

Cross-toolkit contract for the TS Button. Source: `design/source/components/Button/README.md`, `bundle.css` (`.ts-btn*`, `.ts-spinner`), `index.d.ts` (`ButtonProps`).

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI (`TSDesignSystem`) | UIKit (`TSDesignSystemUIKit`) | Compose (`com.ts.ds.compose.components.button`) | Views (`com.ts.ds.views.components.button`) |
|---|---|---|---|---|---|
| `Button` | `TSButtonStyleSpec` | `TSButton` (internal `TSButtonStyle: ButtonStyle`) | `TSButtonView: UIButton` (`UIButton.Configuration`) | `TSButton(text, onClick, …)` composable | `TSButtonView: LinearLayout` (XML or code) |
| `children` | — | `_ title: String` | `title` | `text: String` | `text` / `android:text` |
| `variant` | `TSButtonVariant` `primary · secondary · ghost · danger · link` (Kotlin `Primary` …) | `variant:` | `variant` | `variant =` | `variant` / `app:tsVariant` |
| `size` | `TSButtonSize` `md · lg` (`sm` dropped, C-22) | `size:` | `size` | `size =` | `size` / `app:tsSize` |
| `iconLeft` / `iconRight` | `TSIconName` / `TSIcon` | `leadingIcon:` / `trailingIcon:` | `leadingIcon` / `trailingIcon` | `leadingIcon =` / `trailingIcon =` | `leadingIcon` / `trailingIcon`, `app:tsLeadingIcon="arrow-left"` (DS name) |
| `loading` | `TSControlState.loading` | `isLoading:` | `isLoading` | `loading =`, `loadingDescription =` | `isLoading` / `app:tsLoading`, `loadingDescription` |
| `block` | — | `isBlock:` (fills the proposed width) | `isBlock` (low horizontal hugging; the container sets the width) | `Modifier.fillMaxWidth()` (content stays centred) | `layout_width="match_parent"` |
| `disabled` | `TSControlState.disabled` | `.disabled(_:)` | `isEnabled` | `enabled =` | `isEnabled` / `android:enabled` |
| `onClick` | — | `action:` closure | `primaryAction: UIAction?`, target-action | `onClick` lambda (+ optional `interactionSource`) | `setOnClickListener` |
| `as`, `href`, `className` | dropped | | | | |

Icons are leading/trailing, never left/right, so they follow the layout direction. Directional icons mirror in RTL.

## Matrix

Resolved once per platform in `TSButtonStyleSpec.resolve` (`TSFoundation/Styles`, `android/core/.../core/style`); both toolkits of the platform read it. Android resolves to roles (`TSColorRole`, `TSTypeRole`, `TSShadowRole`) and dp values, and each toolkit maps the roles to its own colours.

| Variant | Fill | Pressed fill | Label | Border | Shadow |
|---|---|---|---|---|---|
| primary | `accent-primary` | `accent-pressed` | `text-on-accent` | — | — |
| secondary | `surface-card` | `surface-pressed` | `text-primary` | 1 pt `border-strong` | `shadow-xs` |
| ghost | transparent | `surface-pressed` | `text-primary` | — | — |
| danger | `danger-solid` | `danger-pressed` (extension token, C-12) | `text-on-accent` | — | — |
| link | none | none (`opacity-pressed` 0.85) | `accent-text`, underlined | — | — |

| Size | Min height | Horizontal padding | Vertical padding | Radius | Type |
|---|---|---|---|---|---|
| md | `control-md` 44 | `space-4` 16 | `space-2` 8 | `radius-md` | `label` 14/20/500 |
| lg | `control-lg` 52 | `space-5` 20 | `space-2` 8 | `radius-md` | `body` 16/24 at weight 500 |
| link (any size) | none | 0 | 0 | 0 | as the size |

Icons: `icon-md` 20, gap `space-2` 8. Spinner: 16 pt, 2 pt ring, 0.7 s per turn, drawn in the label colour inside the 20 pt leading slot so the width does not change.

| State | Rule |
|---|---|
| default | as the tables above |
| pressed | pressed fill; link drops to `opacity-pressed`; 120 ms (`duration-fast`) colour change, none with Reduce Motion |
| disabled | whole control at `opacity-disabled` 0.45; not interactive |
| loading | spinner replaces the leading icon (or is added before the label); **not dimmed** (keeps label contrast); not interactive |
| focused | 2 pt `focus` ring outside a 2 pt gap (`shadow-focus`), keyboard focus only. SwiftUI draws the TS ring; UIKit uses `UIFocusHaloEffect` |

States are combined by `TSControlState.make(isPressed:isEnabled:isLoading:isFocused:)` (Kotlin `TSControlState.make(pressed, enabled, loading, focused)`): loading wins over disabled, disabled over pressed.

## Accessibility

- Role: button. Label: the title. Icons and the spinner are hidden.
- Loading: value "Loading" (`TSStrings.loading`, overridable), not enabled (SwiftUI `.disabled`, UIKit `.notEnabled` trait), still focusable.
- Height is a minimum: the label wraps and the button grows with Dynamic Type (up to AX5).
- Link: the visual height is the text, but the hit area is at least 44 × 44 (SwiftUI `contentShape` expanded by `TSHitArea.outset`; UIKit `point(inside:with:)` via `TSHitArea.expandedRect`).
- No haptics (C-13 reserves them for Switch, Checkbox, segments and Toast).

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSButtonStyleSpecTests.swift` | all 50 configurations → fill, label, border, opacity, spinner, focus; danger-pressed; loading not dimmed; sizes; link; shadow |
| Spec | `ios/Tests/TSFoundationTests/TSControlStateMakeTests.swift` | state precedence, hit-area outset, weighted font |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSButtonBehaviorTests.swift` | tap fires; disabled and loading block; label; "Loading" value; spinner replaces the icon; icons hidden |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSButtonViewBehaviorTests.swift` | `.touchUpInside` fires; `isEnabled = false` and loading block; traits and value; min height; link hit area ≥ 44; pressed fill; Dynamic Type growth |
| Snapshots | `…/Snapshots/TSButtonSnapshotTests.swift`, `TSButtonViewSnapshotTests.swift` | 5 variants × (md, lg × default/pressed/disabled/loading[/focused SwiftUI]) + icons + block, × the standard matrix |
| Demo | `demos/ios-*/…UITests/ActionComponentsTests.swift` | playground fires; loading and disabled block; link expanded hit area |
| Spec (Android) | `android/core/src/test/.../core/style/TSButtonStyleSpecTest.kt`, `TSControlStateMakeTest.kt` | the same 50 configurations against an independent table; precedence; raw loading + disabled reads as loading |
| Compose | `android/compose/src/test/.../components/button/TSButtonTest.kt` (sdk 26, 36) | click fires; disabled and loading block (`assertIsNotEnabled`); stateDescription "Loading"; `Role.Button` + text; icons hidden; 44 dp visual inside a 48 dp touch target; lg 52 dp; link touch target 48 × 48 |
| Views | `android/views/src/test/.../components/button/TSButtonViewTest.kt` (sdk 26, 36) | tap fires; disabled and loading block real touches; loading not dimmed, `ViewCompat` state description, node not enabled; node class `Button` + label text; spinner replaces the leading icon; 48 px target with a 44 px visual at mdpi; lg 52; link ≥ 48 and underlined; pressed fill; link pressed opacity; medium family; XML attributes |
| Snapshots (Android) | `TSButtonSnapshotTest`, `TSButtonViewSnapshotTest`, plus `TSButtonSdkSnapshotTest` / `TSButtonViewSdkSnapshotTest` (sdk 26/36) | 5 variants × md/lg × default/pressed/disabled/loading/focused + icons + block, standard matrix; the sdk 26 subset shows the label weight |
| Demo (Android) | `demos/android-*/app/src/androidTest/.../ActionComponentsTest.kt` | playground fires; loading and disabled block; link 48 dp target |

## Platform notes

- UIKit: in a horizontal `UIStackView` whose width is not fixed, give the buttons `.required` horizontal compression resistance; otherwise UIKit may wrap multi-line button titles to equal widths (system `UIButton` behaves the same).
- UIKit draws the border with the layer border, not `UIBackgroundConfiguration.strokeColor`, which rendered without corners in snapshots.
- Android touch target: the visual height stays 44 (md), but the touch target is at least 48 × 48 dp. Compose puts `clickable` outside `tsMinimumTouchTarget`, so the layout box is 48 dp and the chrome is centred in it. Views measures the view at ≥ 48 dp and `TSControlBackground` draws the 44 dp shape centred in it. Links get the same 48 dp box around the text.
- Android pressed colours change immediately (no 120 ms cross-fade), and so does the link opacity.
- Android weight: labels are 500. Views uses the `sans-serif-medium` family on every level. Compose uses `withDsFontWeight`, which swaps to `sans-serif-medium` below API 28, because Compose renders `FontWeight.Medium` on the default family as Regular there.
- Android focus ring: Compose draws the TS ring outside a 2 dp gap, keyboard focus only (`tsFocusRing`). Views (`TSControlBackground`) draws the ring outside a 2 dp gap when the view's bounds leave room around the visual shape (the 48 dp touch box around a 44 dp button, for example); when the shape fills the bounds (full-bleed), it draws a 2 dp `focus` stroke on the shape edge plus a 2 dp `background-primary` gap stroke inside it, so the ring stays visible on primary, where `focus` equals the fill. The parent clips children, so a ring can never extend past the view's own bounds.
- Android shadow (secondary): Compose uses `Modifier.shadow` with the token colour (the platform ignores the tint below API 28). Views uses `elevation` with the default shadow colour on every level.
- Views XML: `<com.ts.ds.views.components.button.TSButtonView android:text="…" app:tsVariant="secondary" app:tsSize="lg" app:tsLeadingIcon="download" />`. Icon attributes take the DS kebab name (`TSIcons.fromDsName`).
- Snapshot hook: Compose components read an internal `LocalTSForcedControlState` (tests only) to draw pressed, focused and loading without input. The spinner is static under it, because an infinite animation never lets the Robolectric looper go idle.
