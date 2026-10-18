# Checkbox

Cross-toolkit contract for the TS Checkbox. Source: `design/source/components/Checkbox/README.md`, `bundle.css` (`.ts-check*`), `index.d.ts` (`CheckboxProps`). Constants: `design/extensions/native-tokens.json` (`checkbox-size`, `checkbox-border-width`).

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `Checkbox` | `TSCheckboxStyleSpec` | `TSCheckbox` (a `Button` with the internal `TSCheckboxStyle`) | `TSCheckboxView: UIControl` | `TSCheckbox` (`toggleable`, `Role.Checkbox`) | `TSCheckboxView : LinearLayout, Checkable` |
| `label` | — | `_ label:` (required) | `label` | `label` (required) | `label` / `android:text` |
| `checked` / `defaultChecked` / `onChange` | `Configuration.isOn` | `isOn: Binding<Bool>` | `isOn` + `.valueChanged` (programmatic sets are silent) | `checked` + `onCheckedChange` (hoisted) | `isChecked` (`android:checked`) + `setOnCheckedChangeListener` (user toggles only; programmatic sets are silent) |
| `disabled` | `TSControlState.disabled` | `.disabled(_:)` | `isEnabled` | `enabled` | `isEnabled` |
| label-less use | — | `isLabelHidden: true` | `isLabelHidden` | `labelHidden = true` | `isLabelHidden` / `app:tsLabelHidden` |
| `name` / `value` | dropped (form posting is web only) | | | | |

## Matrix

| State | Mark fill | Mark border | Check |
|---|---|---|---|
| off | `surface-card` | 1.5 pt `border-strong` | — |
| off, pressed [est, C-12] | `surface-pressed` | `border-strong` | — |
| on | `accent-primary` | `accent-primary` | `check`, 16 pt (`icon-sm`), `text-on-accent` |
| on, pressed [est, C-12] | `accent-pressed` | `accent-pressed` | as on |
| disabled | as off/on, whole control at 0.45 | | |
| focused | focus ring (2 pt gap + 2 pt `focus`) around the mark only | | |

Geometry: mark 20 × 20, radius `radius-xs`; label `body` 16/24 `text-primary`; gap `space-2` (8); the row is at least 44 pt high and centres the mark on the label block. Indeterminate and error are absent from the DS (C-14: no `minus` icon) and are not implemented. No animation.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSCheckboxStyleSpecTests.swift` | 8 configurations (off/on × default, pressed, disabled, focused) → fill, border, check, opacity, focus; geometry |
| Compat | `ios/Tests/TSFoundationTests/TSCompatTests.swift` | explicit path honoured; the selected trait is stripped |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSCheckboxBehaviorTests.swift` | tap flips the binding and plays a haptic; disabled blocks; label, value and trait on the native and fallback paths; mark hidden; check shown only when on; row ≥ 44 |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSCheckboxViewBehaviorTests.swift` | tap flips `isOn`, fires `.valueChanged` once and plays a haptic; programmatic set is silent; disabled blocks; `accessibilityActivate`; label, value and trait on both paths; row ≥ 44 and hit area; AX5 growth |
| Demo | `FormComponentsTests.testCheckboxTogglesFromTheLabelAndDisabledBlocks` (both iOS demos) | tap on the label side toggles; value Checked/Unchecked; counter; disabled blocks |
| Android spec | `android/core/src/test/.../core/style/TSCheckboxStyleSpecTest.kt` | 8 configurations → fill, border, check, opacity, focus; geometry; disabled press is not pressed |
| Compose | `android/compose/src/test/.../components/checkbox/TSCheckboxTest.kt` (sdk 26/36) | tap on the label toggles and reports the value; `Role.Checkbox`, `ToggleableState`, `StateDescription` "Checked"/"Unchecked"; disabled blocks; row 48 dp and the far end of the row toggles; hidden label → content description and 48 × 48 target; ToggleOn/ToggleOff haptic and the `hapticsEnabled` opt-out |
| Views | `android/views/src/test/.../components/checkbox/TSCheckboxViewTest.kt` (sdk 26/36) | tap toggles and notifies; programmatic sets are silent; disabled blocks and dims; node is a checkable `CheckBox` with the label and `ViewCompat` state description; row 48 dp hit; hidden label 48 × 48; pressed fills; haptic; XML attributes |
| Android demo | `FormComponentsTest.checkboxTogglesFromTheLabelAndDisabledBlocks` (both Android demos) | tap near the label end toggles; state description; value caption; counter; disabled blocks |

## Platform notes

- iOS: the SwiftUI pressed and focused states are forced in snapshots through the internal `forcingState(_:)`; UIKit uses `isHighlighted` and `UIFocusHaloEffect` around the mark (not snapshotted).
- Android Compose: `toggleable(role = Role.Checkbox)` on the whole row with an explicit `stateDescription`; the row is at least 48 dp (`tsMinimumTouchTarget`), so a hidden label gives a 48 × 48 target. Focus ring around the mark (`tsFocusRing`); pressed and focused are forced in snapshots through `LocalTSForcedControlState`.
- Android Views: `TSCheckboxView` is a row (`LinearLayout`) with the mark and a `MaterialTextView`; the node is a checkable `android.widget.CheckBox` with the label as text and the state description set through `ViewCompat`. The mark uses `TSControlBackground`, so the focus ring is drawn inside the mark (the T32 Views limitation). Listeners fire on user toggles only, matching iOS.
- Android haptics: `DsHaptics.toggleEvent(checked)` → TOGGLE_ON/OFF on 34+, KEYBOARD_TAP below (not the iOS selection haptic). Compose honours `TSTheme(hapticsEnabled)`, Views honours `View.isHapticFeedbackEnabled`.
- Hit area: the whole row (mark + gap + label), at least 44 pt high. With `isLabelHidden`, UIKit expands `point(inside:)` to 44 × 44 and SwiftUI expands the content shape.
- Toggling plays the selection haptic (`TSHaptics`, opt-out through `TSHaptics.shared.isEnabled`).
