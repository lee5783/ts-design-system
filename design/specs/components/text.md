# Text

Cross-toolkit contract for TS text: a type role plus a text colour, so screens never mix Material or system type with TS type (issue #6). The DS has no Text component; the roles are the DS type scale (`design/source/tokens.json` typography) and the `text-*` colour roles. Tokens: `display`, `title-1/2/3`, `body-lg`, `body`, `body-sm`, `label`, `caption`, `overline`, `code`; `text-primary`, `text-secondary`, `text-muted`, `text-disabled`, `text-inverse`, `text-on-accent`. Decisions: C-026 (`text-*` roles only, no `TSSurface`), C-002 (a `TSText` on Compose and SwiftUI; UIKit and Views use their platform text views with TS styles), C-028 (own spec and demo page).

## API mapping

| Concept | Shared (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose (`…compose.components.text`) | Views |
|---|---|---|---|---|---|
| Text | — | `TSText` | `UILabel` + `TSFont.attributedString(_:sizeCategory:color:alignment:)` | `TSText(text, modifier, role, color, textAlign, maxLines, overflow)` | `TextView` + `android:textAppearance="@style/TextAppearance.TS.<Role>"` |
| type role | `TSTypeRole` `display · title1 · title2 · title3 · bodyLg · body · bodySm · label · caption · overline · code` (Android `Display … Code`); iOS `role.font` → `TSFont` | `role:` (default body) | `TSTypeRole.font` / `TSFont.<role>` | `role =` (default `Body`) | `TextAppearance.TS.Display … TextAppearance.TS.Code` |
| text colour | `TSTextColor` `primary · secondary · muted · disabled · inverse · onAccent` (Android `Primary … OnAccent`) → `text-*` roles | `color:` (default primary) | `TSTextColor.uiColor` | `color =` (default `Primary`) | `android:textColor="?attr/tsColorTextPrimary"` (… `TextOnAccent`) |
| alignment | — | `.multilineTextAlignment(_:)` | `NSTextAlignment` in the paragraph style | `textAlign =` (default start) | `android:textAlignment` |
| line limit | — | `.lineLimit(_:)`, `.truncationMode(_:)` | `numberOfLines`, `lineBreakMode` (set it in the paragraph style) | `maxLines`, `overflow` | `android:maxLines`, `android:ellipsize` |
| free font, size, weight or colour | not offered | | | | |

`TSSurface` is out of scope (C-026); use the toolkit's own container with `background-*` / `surface-*` colours.

## Matrix

11 roles × 6 colours. Each role renders exactly like the component that uses it (size, weight, line height, tracking, `overline` uppercased, `code` monospaced):

| Role | Size / line height | Weight |
|---|---|---|
| display | 36 / 44 | 700 |
| title1 | 28 / 36 | 700 |
| title2 | 22 / 28 | 600 |
| title3 | 18 / 24 | 600 |
| bodyLg | 17 / 26 | 400 |
| body | 16 / 24 | 400 |
| bodySm | 14 / 20 | 400 |
| label | 14 / 20 | 500 |
| caption | 12 / 16 | 400 |
| overline | 11 / 16, uppercase | 600 |
| code | 13 / 20, monospaced | 400 |

The values above are the token values at the default text size; the generated tokens are the source of truth. Text scales with Dynamic Type / font scale like every TS text.

## Accessibility

- Plain static text: the string is the label; no traits.
- `overline` is uppercased only visually on iOS (`textCase`), so VoiceOver reads the original string. Compose `TSText` uppercases the string itself, as `TSListSection` does; on Views set `android:textAllCaps="true"` (`TextAppearance.TS.Overline` does not).
- Headings are the caller's job (`.accessibilityAddTraits(.isHeader)`, `Modifier.semantics { heading() }`).

## Tests

| Layer | File | What |
|---|---|---|
| Shared (iOS) | `ios/Tests/TSFoundationTests/TSTextStyleTests.swift` | every role → its `TSFont`; every colour → its `text-*` role |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSTextBehaviorTests.swift` | label is the string; height equals the role's line height |
| Snapshots | `TSTextSnapshotTests` (`text.roles-*`, `text.colors-*`) | every role, every colour, standard matrix incl. AX5 and RTL |
| Shared (Android) | `android/core/src/test/.../style/TSTextColorTest.kt` | every colour → its `text-*` role |
| Compose | `android/compose/src/test/.../text/TSTextTest.kt` (sdk 26/36) | text node; line height and colour per role; `maxLines` / `overflow` |
| Snapshots (Android) | `TSTextSnapshotTest`, `TSTextSdkSnapshotTest` (`roles_*`, `colors_*`) | same galleries; sdk 26 subset for the weight-600 fallback |
| Demo | Text page in all four demos; `ContentComponentsTests.testTextPageShowsEveryTypeRoleAndTextColor` (both iOS demos), `ContentComponentsTest.textPageShowsEveryTypeRoleAndTextColor` (both Android demos) | SwiftUI / Compose show `TSText`; UIKit / Views show the documented equivalents |

## Platform notes

- SwiftUI `TSText` is `Text(verbatim:)` with `.tsFont(role.font)` and the colour; it takes plain strings (localise before passing).
- Compose `TSText` is `BasicText` with `TSTheme.typography.style(role)`, `TSTheme.colors.color(color.role)` and `TextDirection.Content`; the weight-600 roles fall back to `sans-serif-medium` below API 28 like every TS text.
- UIKit and Views have no `TSText` (C-002): `UILabel` with `TSFont` attributes, and `TextView` with `TextAppearance.TS.*`, already give the same result, and a wrapper view would only re-expose their properties.
- Use `TSText` (or the UIKit / Views equivalents) instead of Material `Text` with `MaterialTheme.typography`, so a TS screen never mixes the two type scales.
