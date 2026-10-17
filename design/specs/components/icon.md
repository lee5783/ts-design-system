# Icon

Cross-toolkit contract for TS icons. Source: `design/source/components/Icon/README.md`, `design/assets/icons/**`, `icons.meta.json`.

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose (`…compose.components.icon`) | Views |
|---|---|---|---|---|---|
| `Icon` | `TSIconStyleSpec` | `TSIcon` | `TSIconView: UIImageView` (SwiftUI `TSName`, UIKit `TSNameView`, C-29) | `TSIcon(icon, contentDescription, …)` composable | `ImageView` + `TSIcon.drawableRes` (`@drawable/ts_ic_*`) + `ImageViewCompat.setImageTintList`; no wrapper view |
| `name` | `TSIconName` / `TSIcon` (33, generated) | `_ name:` | `name` | `icon` | `setImageResource(icon.drawableRes)` |
| `size` 16/20/24 | `TSIconSize` `sm · md · lg` → `icon-sm/md/lg` | `size:` (default md) | `size` | `size =` (default `Md`) | layout size from `TSIconSize.dp` |
| `label` | `Configuration.hasLabel` | `label:` | `label` | `contentDescription` (null hides it) | `contentDescription`; `importantForAccessibility="no"` when unlabelled |
| `strokeWidth` | dropped (pre-baked vectors) | | | | |
| colour (`currentColor`) | — | `.foregroundColor` | `tintColor` | `tint =` (default `textPrimary`) | `app:tint` / `ImageViewCompat.setImageTintList` |

Raw images: `TSIconName.image(compatibleWith:)` (asset, template) and `TSIconName.templateImage(pointSize:)` (resized template, mirrored for RTL when directional) for `UIButton.Configuration`.

## Rules

- Template rendering; colour comes from text tokens, never raw hex.
- Size is fixed at the token size; icons do not scale with Dynamic Type (follow-up, see T21 output).
- RTL: `arrow-left/right`, `chevron-left/right` and `logout` mirror (asset direction in UIKit, `flipsForRightToLeftLayoutDirection` in SwiftUI, `android:autoMirrored` on the VectorDrawables in both Android toolkits).

## Accessibility

Hidden unless `label` is set; with a label it is an image element whose label is `label`.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSIconStyleSpecTests.swift` | size → token; accessibility element only with a label; template image size and RTL flag |
| SwiftUI | `…/Behavior/TSIconBehaviorTests.swift` | hidden without label; label exposed; frame per size |
| UIKit | `…/Behavior/TSIconViewBehaviorTests.swift` | intrinsic size per token; accessibility; RTL mirroring |
| Snapshots | `TSButtonSnapshotTests.icon`, `TSButtonViewSnapshotTests.icon` | three sizes incl. directional icons, labelled status icons, standard matrix |
| Spec (Android) | `android/core/src/test/.../core/style/TSIconButtonStyleSpecTest.kt` (`TSIconStyleSpecTest`) | size → token; important for accessibility only with a label |
| Compose | `.../components/icon/TSIconTest.kt` | size per token; no content description without a label; label exposed |
| Snapshots (Android) | `TSButtonSnapshotTest.icon`, `TSButtonViewSnapshotTest.icon` | the same gallery as iOS, standard matrix (RTL shows the mirrored icons) |
