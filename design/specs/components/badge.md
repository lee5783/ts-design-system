# Badge

Cross-toolkit contract for the TS Badge. Source: `design/source/components/Badge/README.md`, `bundle.css` (`.ts-badge*`), `index.d.ts` (`BadgeProps`).

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose (`…compose.components.badge`) | Views (`…views.components.badge`) |
|---|---|---|---|---|---|
| `Badge` | `TSBadgeStyleSpec` | `TSBadge` | `TSBadgeView: UIView` | `TSBadge(text, …)` | `TSBadgeView: LinearLayout` |
| `children` | — | `_ text:` | `text` | `text` | `text` / `android:text` |
| `tone` | `TSBadgeTone` `neutral · accent · success · warning · danger · info` (Android: `Neutral … Info`) | `tone:` (default neutral) | `tone` | `tone =` (default `Neutral`) | `tone` / `app:tsBadgeTone` |
| `dot` | `Configuration.hasDot` | `showsDot:` | `showsDot` | `showsDot =` | `showsDot` / `app:tsShowsDot` |
| `icon` (`IconName`) | `Configuration.hasIcon` | `icon: TSIconName?` | `icon` | `icon: TSIcon?` | `icon` / `app:tsIcon="star"` |
| `icon` (`ReactNode`) | dropped: `TSIconName` only | | | | |

## Matrix

| Tone | Fill | Text, dot and icon | Border |
|---|---|---|---|
| neutral | `background-secondary` | `text-secondary` | 1 pt `border-subtle` |
| accent | `accent-subtle` | `accent-text` | — |
| success | `success-subtle` | `success` | — |
| warning | `warning-subtle` | `warning` | — |
| danger | `danger-subtle` | `danger` | — |
| info | `info-subtle` | `info` | — |

Each tone is shown plain, with a dot and with an icon: 6 × 2 × 2 = 24 configurations.

Anatomy:
- **Height:** a minimum of 22 (`badge-height`), not a fixed height, so the text can scale. Vertical padding is 2 (`space-0.5`).
- **Padding and gap:** horizontal padding 8 (`space-2`), gap 4 (`space-1`).
- **Corners:** `radius-sm`, continuous.
- **Text:** `caption` (12/16) at weight 500, on one line. It truncates at the tail.
- **Dot:** 6 (`badge-dot-size`), in the text colour.
- **Icon:** 16 (`icon-sm`). Like every TS icon, it does not scale with Dynamic Type (see `icon.md`).
- **Order:** the dot comes first, then the icon, then the text, all leading-aligned, so they mirror in RTL.

States: static only. A badge is never a button (DS: "Don't use badges as buttons").

## Accessibility

- One element. The label is the text and the trait is static text. It is never a button, even inside a tappable row.
- The dot and icon are decorative. The word carries the meaning, and colour only reinforces it.
- Text scales with Dynamic Type. The height grows past 22 at large sizes, and long text truncates on one line. VoiceOver still reads the full text.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSBadgeStyleSpecTests.swift` | 24 configurations → fill, foreground, border, dot and icon flags; anatomy (min 22, padding, radius, dot 6, icon 16, 12/16 at 500) |
| SwiftUI | `…/Behavior/TSBadgeBehaviorTests.swift` | label = text; static-text trait, no button trait; no `Button`; decorative icon only when set; 6 pt dot only when requested |
| UIKit | `…/Behavior/TSBadgeViewBehaviorTests.swift` | label, static-text trait, not a `UIControl`; tone colours and border; dot and icon toggling and updates; height 22 at default and taller at AX5 |
| Snapshots | `TSBadgeSnapshotTests.badge`, `TSBadgeViewSnapshotTests.badge` | 6 tones × plain/dot/icon plus a truncating badge, standard matrix (light/dark × default/AX5 × LTR/RTL) |
| Demo | `ContentComponentsTests.testBadgeIsStaticTextLabelledByItsText` (both apps) | |
| Spec (Android) | `android/core/src/test/.../core/style/TSBadgeStyleSpecTest.kt` | the same 24 configurations → fill, foreground, border, dot and icon flags; anatomy; state does not change the result |
| Compose | `.../components/badge/TSBadgeTest.kt` | merged text label, no role, no click action; 22 dp at default and taller at 200 %; dot +10 dp and icon +20 dp, no content description; long text stays one line and ellipsizes |
| Views | `.../components/badge/TSBadgeViewTest.kt` | node text and `TextView` class, not clickable or focusable, children hidden; tone colours and neutral border; dot and icon toggling; 22 px at default and taller at 200 %; one-line ellipsis at 120 px; XML attributes |
| Snapshots (Android) | `TSBadgeSnapshotTest.badge`, `TSBadgeViewSnapshotTest.badge` (+ `TSBadge*SdkSnapshotTest`, sdk 26/36) | 6 tones × plain/dot/icon plus a truncating badge, standard matrix |
| Demo (Android) | `demos/android-*/app/src/androidTest/.../ContentComponentsTest.kt` | `badgeIsStaticTextLabelledByItsText` |

## Platform notes

- **SwiftUI:** `.accessibilityElement(children: .ignore)` with the text label and `.isStaticText`.
- **UIKit:**
  - `TSBadgeView` is a plain `UIView`, not a control.
  - The text is an attributed `TSFont` string re-applied on trait changes, with a tail-truncating paragraph style.
  - Colours are resolved against `traitCollection`. The border is drawn on the layer (`tsApplyBorder`).
  - The width follows the content. When it can run long, the container caps it with a `≤` width constraint.
- **Compose:**
  - A `Row` with `semantics(mergeDescendants = true)`, so the badge is one node whose text is the label. It has no role and no click action.
  - Text is `caption` with `withDsFontWeight(500)` (the sdk < 28 `sans-serif-medium` swap), `maxLines = 1`, `TextOverflow.Ellipsis`, and `weight(1f, fill = false)` so it shrinks before it leaves the badge.
  - `defaultMinSize(minHeight = 22.dp)`, not a fixed height.
- **Views:**
  - `TSBadgeView` is a plain `LinearLayout`, not clickable or focusable. The children are hidden from accessibility, and the node reports the text with the `TextView` class.
  - The text is a single-line `MaterialTextView` with `TruncateAt.END`, set in `sans-serif-medium` on every API level (as in `TSButtonView`).
  - The chrome is a `GradientDrawable` (fill, 1 px neutral stroke, `radius-sm`). `minimumHeight` is 22 dp.
  - The tone attribute is `app:tsBadgeTone`, not the shared `tsVariant` enum (see `button.md`).
