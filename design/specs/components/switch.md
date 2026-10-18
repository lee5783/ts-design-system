# Switch

Cross-toolkit contract for the TS Switch. Source: `design/source/components/Switch/README.md`, `bundle.css` (`.ts-switch*`), `index.d.ts` (`SwitchProps`). Tokens: `switch-thumb` colour, `switch-track-width/height`, `switch-thumb-size/inset/travel`, `duration-base`, `easing-standard` (`design/extensions/native-tokens.json`, C-08, C-13).

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `Switch` | `TSSwitchStyleSpec` | `TSSwitch` (a `Button` with the internal `TSSwitchStyle`) | `TSSwitchView: UIControl` (custom, not `UISwitch`, C-08) | `TSSwitch` (`toggleable`, `Role.Switch`; custom, not M3 `Switch`) | `TSSwitchView : LinearLayout, Checkable` (custom, not `SwitchCompat`) |
| `label` | — | `_ label:` (required) | `label` | `label` (required) | `label` / `android:text` |
| `checked` / `defaultChecked` / `onChange` | `Configuration.isOn` | `isOn: Binding<Bool>` | `isOn`, `setOn(_:animated:)` + `.valueChanged` (programmatic sets are silent) | `checked` + `onCheckedChange` (hoisted) | `isChecked`, `setChecked(_, animated)` + `setOnCheckedChangeListener` (user toggles only) |
| `disabled` | `TSControlState.disabled` | `.disabled(_:)` | `isEnabled` | `enabled` | `isEnabled` |
| label-less use (ListItem trailing) | — | `isLabelHidden: true` | `isLabelHidden` | `labelHidden = true` | `isLabelHidden` / `app:tsLabelHidden` |

## Matrix

| State | Track | Thumb |
|---|---|---|
| off | `border-strong` | `switch-thumb` (white in both themes), `shadow-sm`, leading |
| off, pressed [est, C-12] | `border-strong` at `opacity-pressed` (0.85) | as off |
| on | `accent-primary` | trailing |
| on, pressed [est, C-12] | `accent-pressed` | trailing |
| disabled | as off/on, whole control at 0.45 | |
| focused | circular focus ring around the track | |

Geometry: track 44 × 26, fully rounded; thumb 20, inset 3, travel 18 (leading offset 3 off, 21 on). Label `body` 16/24 after the track, gap `space-2` (8); the row is at least 44 pt high.

Motion: thumb and track colour animate over `duration-base` (150 ms) with `easing-standard`.

RTL: "on" is always the trailing side. SwiftUI pads the thumb from the leading edge inside a leading-aligned track (mirrored by the layout direction); UIKit uses `Resolved.thumbOriginX(isRightToLeft:)`.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSSwitchStyleSpecTests.swift` | 8 configurations → track, opacity, thumb colour and offset, shadow, focus; geometry and 150 ms; thumb side in LTR and RTL |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSSwitchBehaviorTests.swift` | tap flips the binding and plays a haptic; disabled blocks; label, value and trait on both paths; track hidden; rendered thumb side in LTR and RTL; row ≥ 44 |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSSwitchViewBehaviorTests.swift` | tap flips `isOn`, fires `.valueChanged` once and plays a haptic; `setOn(_:animated:)` is silent and immediate with reduce motion; disabled blocks; label, value and trait on both paths; thumb frame in LTR and RTL; row ≥ 44; colours |
| Demo | `FormComponentsTests.testSwitchTogglesFromTheLabelAndDisabledBlocks` (both iOS demos) | tap on the label side toggles; value On/Off; counter; disabled blocks |
| Android spec | `android/core/src/test/.../core/style/TSSwitchStyleSpecTest.kt` | 8 configurations → track colour and opacity, thumb token, offset and shadow, focus; `thumbLeftDp` in LTR and RTL; 150 ms + `easing-standard`; `TSReduceMotion.durationMillis` |
| Compose | `android/compose/src/test/.../components/switchcontrol/TSSwitchTest.kt` (sdk 26/36) | tap on the label toggles; `Role.Switch`, `StateDescription` "On"/"Off"; haptic; disabled blocks; track 44 × 26, thumb 20, row 48 dp; hidden label 48 × 48; thumb side in LTR and RTL; thumb mid-way at 80 ms; immediate with reduce motion |
| Views | `android/views/src/test/.../components/switchcontrol/TSSwitchViewTest.kt` (sdk 26/36) | tap toggles, notifies and plays the haptic; programmatic sets silent and immediate; disabled; checkable `SwitchCompat` node + state description; track hidden from accessibility; sizes; thumb side LTR/RTL; user toggle runs a 150 ms `PathInterpolator` animator; none with reduce motion; off-track pressed opacity; XML attributes |
| Android demo | `FormComponentsTest.switchTogglesFromTheLabelAndDisabledBlocks` (both Android demos) | tap near the label end toggles; state description; value caption; counter; disabled blocks |

## Platform notes

- iOS: native `UISwitch` / `Toggle` are 51 × 31 and are not used (C-08).
- Android Compose: `toggleable(role = Role.Switch)` on the whole row (≥ 48 dp). One `animateFloatAsState` progress drives the thumb's start padding and the track colour (lerp between the off and on resolutions), so pressed colours change immediately while the toggle animates; `tween(150, easing-standard)`, or `snap()` when `TSReduceMotion.isEnabled` (system "Remove animations"). RTL comes from start padding in a `CenterStart` box.
- Android Views: the thumb is a child `View` with `shadow-sm` elevation inside a 44 × 26 track that uses `TSControlBackground`; its x is `Resolved.thumbLeftDp(rightToLeft)`. User taps animate through a `ValueAnimator` with a `PathInterpolator`; programmatic `isChecked` is immediate; `setChecked(checked, animated = true)` animates. Node class `androidx.appcompat.widget.SwitchCompat`, checkable, state description "On"/"Off" via `ViewCompat`.
- Android haptics: TOGGLE_ON/OFF on 34+, KEYBOARD_TAP below (`DsHaptics.toggleEvent`).
- Hit area: the whole row, at least 44 pt high; with the label hidden the row is 44 × 44.
- Toggling plays the selection haptic (opt-out through `TSHaptics.shared.isEnabled`).
