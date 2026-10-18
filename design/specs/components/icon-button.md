# IconButton

Cross-toolkit contract for the TS IconButton. Source: `design/source/components/IconButton/README.md`, `bundle.css` (`.ts-icon-btn*`), `index.d.ts` (`IconButtonProps`).

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose (`…compose.components.iconbutton`) | Views (`…views.components.iconbutton`) |
|---|---|---|---|---|---|
| `IconButton` | `TSIconButtonStyleSpec` | `TSIconButton` (internal `TSIconButtonStyle`) | `TSIconButtonView: UIButton` (`UIButton.Configuration`) | `TSIconButton(icon, contentDescription, onClick, …)` | `TSIconButtonView: AppCompatImageButton` |
| `icon` | `TSIconName` / `TSIcon` | `_ icon:` | `icon` | `icon =` | `icon` / `app:tsIcon="search"` |
| consumer icon (native, issue #2) | — (drawn at `Resolved.iconSize`, tinted with `Resolved.icon`) | `TSIconButton(_ icon: TSIconSource, label:variant:isRound:action:)` (`.ts(_)` · `.image(Image)`) | `iconImage: UIImage?` (wins over `icon`), `convenience init(iconImage:label:variant:isRound:primaryAction:)` | `TSIconButton(icon: TSIconSource, contentDescription, onClick, …)` | `iconDrawable: Drawable?`, `setIconResource(@DrawableRes)` (wins over `icon`); no XML attribute |
| `label` (required) | — | `label:` | `label` | `contentDescription =` (required) | `label` / `android:contentDescription` (also the long-press tooltip) |
| `variant` | `TSIconButtonVariant` `ghost · secondary · primary` | `variant:` (default ghost) | `variant` | `variant =` (default `Ghost`) | `variant` / `app:tsVariant` (`danger` and `link` throw) |
| `round` | `Configuration.isRound` / `Configuration.round` | `isRound:` | `isRound` | `round =` | `isRound` / `app:tsRound` |
| `size` | md only (`sm` dropped, C-22) | | | | |
| `disabled` | `TSControlState.disabled` | `.disabled(_:)` | `isEnabled` | `enabled =` | `isEnabled` |
| tooltip | dropped (web only); Large Content Viewer shows icon + label instead | | | | |

The `TSIcon` / `TSIconName` signatures are unchanged and delegate to the consumer-icon ones. `label` / `contentDescription` stays required on every overload.

## Consumer icons

Same rules as the Button ([button.md](button.md#consumer-icons)): TS draws the consumer icon at the resolved icon size (`icon-lg` 24 here), aspect-fitted, tinted single-colour with `Resolved.icon` in every state, and hidden from accessibility; the consumer cannot pass a colour, size or padding. Follow the [icon contract](button.md#icon-contract) (24 px grid, 1.75 px outline stroke, no fills, single colour, RTL mirroring set on the asset, contribute shared icons upstream). The Large Content Viewer (iOS) shows the consumer image as a template.

## Matrix

| Variant | Fill | Pressed fill | Icon | Border |
|---|---|---|---|---|
| ghost | transparent | `surface-pressed` | `text-primary` | — |
| secondary | `surface-card` | `surface-pressed` [est] | `text-primary` | 1 pt `border-strong`, **no shadow** |
| primary | `accent-primary` | `accent-pressed` [est] | `text-on-accent` | — |

Geometry: 44 × 44 (`control-md`), radius `radius-md`, or a circle when `isRound`. Icon 24 pt (`icon-lg`, C-25).

States: default, pressed, disabled (0.45), focused (ring as Button; circular ring when round). No loading state.

## Accessibility

- Role: button; label = `label` (required in every initializer). The icon is hidden.
- The control stays 44 × 44 at every text size; Large Content Viewer shows the icon and label (SwiftUI `accessibilityShowsLargeContentViewer`, UIKit `UILargeContentViewerInteraction`).
- In-content use only; in system bars prefer `ToolbarItem` / `UIBarButtonItem` with DS colours.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSIconButtonStyleSpecTests.swift` | 24 configurations → fill, icon, border, opacity, focus; size, icon size, round radius |
| SwiftUI | `…/Behavior/TSIconButtonBehaviorTests.swift` | tap fires; disabled blocks; label; icon hidden |
| UIKit | `…/Behavior/TSIconButtonViewBehaviorTests.swift` | `.touchUpInside` fires; disabled blocks; label, trait, large content; 44 × 44 |
| Snapshots | `TSButtonSnapshotTests.iconButton`, `TSButtonViewSnapshotTests.iconButton` | 3 variants × square/round × states, standard matrix |
| Demo | `ActionComponentsTests.testIconButtonPlaygroundFiresAndDisabledBlocks` | |
| Spec (Android) | `android/core/src/test/.../core/style/TSIconButtonStyleSpecTest.kt` | the same 24 configurations; round radius 22; icon 24; touch target 48 |
| Compose | `.../components/iconbutton/TSIconButtonTest.kt` | click through the label; disabled blocks; 44 dp inside a 48 × 48 target; icon hidden |
| Views | `.../components/iconbutton/TSIconButtonViewTest.kt` | tap fires; disabled blocks and dims; content description and `ImageButton` class; 48 px target with a 44 px visual; pressed primary fill; XML attributes |
| Snapshots (Android) | `TSButtonSnapshotTest.iconButton`, `TSButtonViewSnapshotTest.iconButton` | 3 variants × square/round × states, standard matrix |
| Demo (Android) | `demos/android-*/app/src/androidTest/.../ActionComponentsTest.kt` | playground fires; disabled blocks |
| Consumer icon | `TSIconButtonBehaviorTests` `consumerImage`, `tsIconSize`; `TSIconButtonViewBehaviorTests.consumerImage`; `TSIconButtonTest.consumerIconKeepsTheLabelAndTheSize`; `TSIconButtonViewTest.consumerDrawableIsSizedToTheIconAndKeepsTheLabel`; snapshots `iconButtonCustomIcon` in `TSButtonSnapshotTests`, `TSButtonViewSnapshotTests`, `TSButtonSnapshotTest`, `TSButtonViewSnapshotTest` | label kept; resolved icon size; large content title (UIKit); custom icon per variant × state |
| Demo | IconButton page, "Consumer icon" section in all four demos (`demo.icon-button.consumer-icon`) | shown, not asserted by a UI test |

## Platform notes

- Android: 44 dp visual inside a 48 × 48 dp touch target (see `button.md`). There is no Large Content Viewer; Views sets a long-press tooltip (`TooltipCompat`) from the label instead.
- Compose round buttons use `CircleShape`, so the focus ring is a true circle.
- **UIKit `init(iconImage:…)`:** `icon` is non-optional, so this init stores `.plus` in `icon` as a placeholder; `iconImage` wins whenever it is set. Setting `iconImage = nil` later shows that `+`. Set `icon` to the TS icon you want before clearing `iconImage`.
- **Views:** the consumer drawable is wrapped in an internal slot drawable that reports the resolved icon size, because the image button uses `ScaleType.CENTER`.
