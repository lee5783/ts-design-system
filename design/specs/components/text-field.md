# TextField

Cross-toolkit contract for the TS TextField. Source: `design/source/components/TextField/README.md`, `bundle.css` (`.ts-field*`, `.ts-input*`), `index.d.ts` (`TextFieldProps`).

## API mapping

| DS (web) | Style spec (`TSFoundation`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `TextField` | `TSTextFieldStyleSpec` | `TSTextField` | `TSTextFieldView: UIControl` | `TSTextField` (`BasicTextField` + decoration box) | `TSTextFieldView : LinearLayout` (compound around `EditText`) |
| `label` | `labelFont`, `labelColor` | `_ label:` | `label` | `label` | `label` / `app:tsLabel` |
| `value` / `onChange` | — | `text: Binding<String>` | `text` + `.editingChanged` | `value`, `onValueChange` | `text`; `editText` for `TextWatcher` / listeners |
| `placeholder` | `placeholderColor` | `placeholder:` | `placeholder` | `placeholder` | `placeholder` / `app:tsPlaceholder` |
| `hint` | `messageColor` | `hint:` | `hint` | `hint` | `hint` / `app:tsHint` |
| `error` | `.error` state | `error:` | `errorMessage` | `error` | `errorMessage` / `app:tsError` |
| `leadingIcon` (`IconName`) | `leadingIconSize`, `leadingIconColor` | `leadingIcon: TSIconName?` | `leadingIcon` | `leadingIcon: TSIcon?` | `leadingIcon` / `app:tsLeadingIcon` |
| `trailing` (`ReactNode`) | — | `@ViewBuilder trailing:` | `trailingView: UIView?` | `trailing: (@Composable () -> Unit)?` | `trailingView: View?` |
| `multiline` | `minimumLines` 3, `maximumLines` 6, `multilineLayout` | `isMultiline:` | `isMultiline` | `multiline` (`minLines` 3, `maxLines` 6) | `isMultiline` / `app:tsMultiline` |
| `type=password` | `passwordToggle(isRevealed:)` | `isSecure:` (built-in show/hide) | `isSecure` | `secure` | `isSecure` / `app:tsSecure` |
| `disabled` | `.disabled` state | `.disabled(_:)` | `isEnabled` | `enabled` | `isEnabled` / `android:enabled` |
| `readOnly` (native addition) | `Configuration.isReadOnly` | `isReadOnly:` | `isReadOnly` | `readOnly` | `isReadOnly` / `app:tsReadOnly` |
| `inputMode`, `autoComplete`, return key | `TSTextInputTraits`, `TSReturnKey` | `inputTraits:` | `inputTraits` | `inputTraits: TSTextInputTraits` (`:core` `input/`) → `KeyboardOptions` + `ContentType` | `inputTraits` / `app:tsInputTraits` → `inputType`, `imeOptions`, `autofillHints` |
| submit | — | `.onSubmit` | `.primaryActionTriggered` | `onSubmit` (any IME action) | `editText.setOnEditorActionListener` |

## Matrix

| State | Border | Width | Fill | Opacity | Message |
|---|---|---|---|---|---|
| normal | `border-strong` | 1 | `surface-card` + `shadow-xs` | 1 | hint, `text-muted` |
| focused | `focus` | 2 | `surface-card` + `shadow-xs` | 1 | hint |
| error | `danger` | 1 | `surface-card` + `shadow-xs` | 1 | error, `danger`, `alert` icon 16 |
| error + focused | `danger` | 2 | `surface-card` + `shadow-xs` | 1 | error |
| disabled | `border-strong` | 1 | `background-secondary` | 0.45 (box only) | hint |
| read-only (any state) | `border-strong` or `danger` | 1, never a focus border | `background-secondary`, no shadow | 1 | hint or error |

5 states × read-only = 10 configurations. The web draws focus as a 1 px border plus a 1 px outer ring; native draws one 2 pt inner border in the same colour.

Anatomy:
- **Label:** `label` (14/20, 500), `text-primary`, 4 (`space-1`) above the box. Wraps; grows with Dynamic Type.
- **Box:** min height 44 (`control-md`), horizontal padding 12 (`space-3`), gap 8 (`space-2`), `radius-md`, continuous corners.
- **Input:** `body` (16/24), `text-primary`; vertical padding 10 (`text-field-padding-vertical`); placeholder `text-muted`.
- **Leading icon:** 20 (`icon-md`), `text-muted`, decorative.
- **Message:** `caption` (12/16) 4 below the box; the error replaces the hint.
- **Password toggle:** 32 (`control-sm`) with a 44 hit area, `radius-sm`. Hidden: `eye` in `text-muted`. Revealed: `eye` in `accent-text` on `accent-subtle` (selected).
- **Multiline:** 3 lines minimum, grows to 6, then scrolls.

## Accessibility

- The input is the only element. Its label is `label`; the placeholder is never the label. The label text and message are hidden from accessibility.
- `hint` → accessibility hint, only while there is no error.
- Error → accessibility value `"<text>, Error: <message>"` (`"Error: <message>"` for secure entry), and an announcement `"Error: <message>"` when an error appears or changes. Clearing it does not announce.
- Disabled → not editable, `.notEnabled`. Read-only → selectable, not editable; the value is the text (masked when secure).
- The password toggle is a button labelled "Show password" / "Hide password" (library strings `ts.text-field.*`).
- Everything scales with Dynamic Type; the box grows. Icons stay fixed size (see `icon.md`).

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSTextFieldStyleSpecTests.swift` | 10 configurations → border, width, ring, fill, opacity, editable/selectable, message colour; state precedence; anatomy; password toggle tokens; 3→6 line layout and scroll; masked read-only text; accessibility value, hint and announcement rules; input-trait presets; the multiline compat path |
| SwiftUI | `…/Behavior/TSTextFieldBehaviorTests.swift` | binding and typing; label/hint/value; error replaces hint, alert icon, value; announcement on a new error (iOS 17+ inspector); disabled blocks input; read-only is text, not a field; secure toggle modes and labels; native multiline vs the iOS 15 bridge; the bridge grows 3→6 lines then scrolls and writes back; slots |
| UIKit | `…/Behavior/TSTextFieldViewBehaviorTests.swift` | editing and `.editingChanged`; accessibility; error value, hint, icon, border and one announcement; 2 pt focus; disabled; read-only; secure toggle; 44 pt toggle hit area; multiline 3→6 then scroll; traits; Dynamic Type; trailing view |
| Snapshots | `TSTextFieldSnapshotTests`, `TSTextFieldViewSnapshotTests` | `states` (default, filled, focused, error, error focused, disabled, read-only) and `content` (icons and trailing, password hidden and revealed, multiline short and long, SwiftUI also the iOS 15 bridge), standard matrix |
| Demo | `FormComponentsTests` (both apps) | focus and typing, error value, password toggle, disabled and read-only |
| Spec (Android) | `android/core/src/test/…/core/style/TSTextFieldStyleSpecTest.kt`, `core/input/TSTextInputTraitsTest.kt`, `core/logic/TSStringsTest.kt` | 10 configurations → border, width, ring, fill, shadow, opacity, editable/selectable, message colour and icon; anatomy; state precedence; password toggle tokens; 3/6 lines; masked text; input-trait presets and autofill hints; error/toggle strings and the hint rule |
| Compose | `android/compose/src/test/…/components/textfield/TSTextFieldTest.kt` (sdk 26, 36) | typing; label and hint merged into the field, placeholder not; error semantics + `Error: …` live-region message; decorative icons; disabled (no `SetText`); read-only (selectable, no `SetText`); masked read-only without toggle; toggle role, label, selected and `Password` semantics; 48 dp toggle target; 44 dp box; multiline 3 → 6 lines; focus; IME submit; autofill `ContentType` |
| Views | `android/views/src/test/…/components/textfield/TSTextFieldViewTest.kt` (sdk 26, 36) | key-event typing and `TextWatcher`; node `hintText` = label + hint; `setError` / content-invalid, alert icon, live region; 2 dp focus border; disabled (no focus, box-only 0.45); read-only (not a text editor, still selectable) and back; masked read-only; toggle transformation, label, selected; 48 × 44 toggle; typeface kept; multiline 92 → 164 dp then scrolls; traits → `inputType`, `imeOptions`, autofill; XML attributes |
| Snapshots (Android) | `TSTextField[View]SnapshotTest` (standard matrix), `TSTextField[View]SdkSnapshotTest` (sdk 26/36 subset) | `states` and `content` galleries (Views shows *error focused* in `content`, since a window has one focus) |
| Demo (Android) | `FormComponentsTest` (both apps) | focus and typing, error semantics, password toggle, disabled and read-only |

## Platform notes

- **SwiftUI:**
  - Single line: `TextField` / `SecureField` with `prompt:`; the secure toggle swaps them and keeps focus.
  - Multiline: `TextField(axis: .vertical).lineLimit(3...6)` on iOS 16+ (`TSCompat.multilineTextField`); on iOS 15 the internal `TSMultilineTextView` bridge (`UIViewRepresentable` over `TSGrowingTextView`). The native path uses the system line height, because `lineLimit` counts font lines; the bridge and UIKit use the 24 pt DS line height.
  - Read-only: `Text` with `.textSelection(.enabled)`.
  - Focus is `@FocusState` (bridge: `UITextViewDelegate`).
- **UIKit:**
  - `TSTextFieldView` is a `UIControl` compound view: label, box (`UITextField` or `TSGrowingTextView`), message. It forwards `.editingChanged`, `.editingDidBegin`, `.editingDidEnd` and `.primaryActionTriggered` (return key).
  - `accessibilityIdentifier` is forwarded to the input.
  - Multiline and read-only use `TSGrowingTextView` (TextKit 1, so line heights match on iOS 15 and 26).
- **Compose:**
  - `BasicTextField` whose decoration box draws the label, box, message and toggle. The label and hint merge into the field's semantics node; the placeholder is cleared from semantics.
  - Error: `semantics { error(message) }` on the field, and the message row is its own node with `contentDescription` "Error: …" and a polite live region, so TalkBack announces a new or changed error (clearing it is silent).
  - Masking uses an internal visual transformation plus an explicit `password()` semantics; `PasswordVisualTransformation` left a stale `Password` flag after revealing (foundation 1.9).
  - Focus is any editing focus (`collectIsFocusedAsState`); snapshots force it with `LocalTSForcedControlState`, and the forced `selected` flag renders the revealed toggle.
  - The 32 dp toggle keeps a 48 dp touch target through Compose's minimum touch size, so the 44 dp box does not grow.
- **Views:**
  - `TSTextFieldView : LinearLayout` holds a label, a box (`LinearLayout` with a `MaterialShapeDrawable` fill + border and xs elevation), an internal `AppCompatEditText`, a trailing container, the toggle (`AppCompatImageButton`, 48 × 44 with the 32 dp fill inset) and the message row. `editText` is public for listeners.
  - Accessibility: label and hint views are hidden; the `EditText` node's `hintText` is "label, hint", and on error `setError(message)` + content-invalid. The message row becomes accessible only in error, with "Error: …" and a polite live region.
  - Read-only: `keyListener = null` (not a text editor, still focusable and selectable), no cursor or soft keyboard. Secure read-only stays masked and hides the toggle.
  - Multiline: `TYPE_TEXT_FLAG_MULTI_LINE` with `minHeight`/`maxHeight` of 3/6 × 24 sp line height + padding, then the `EditText` scrolls. `minLines`/`maxLines` were 4 px off at the cap because the last line drops its extra spacing.
  - The line height is set with `TextViewCompat.setLineHeight`, so API 26–27 also get 24 sp.
  - Changing `inputType` resets the typeface to monospace for passwords, so the body appearance is re-applied after each change.
- **Shared (`:core`, Android):** `style/TSTextFieldStyleSpec`, `logic/TSTextFieldAccessibility`, `input/TSTextInputTraits` (`TSKeyboardType`, `TSImeAction`, `TSCapitalization`, autofill hints), strings `ts_text_field_error`, `ts_text_field_show_password`, `ts_text_field_hide_password`.
- **Shared (`TSFoundation`):** `TSTextFieldStyleSpec`, `TSTextFieldAccessibility`, `TSTextInputTraits`, `TSGrowingTextView` (package).
- **Icons (C-14):** `eye-off` is not in the icon set, so the toggle uses `eye` with a selected state. Swap in `eye-off` for the revealed state once design adds it.
