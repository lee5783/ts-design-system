# Migration guides

One section per release that removes or renames public API, tokens, string keys or resources, newest first. Deprecations are announced a minor release ahead with a replacement ([../guides/deprecation-policy.md](../guides/deprecation-policy.md)); this page lists what to change when the deprecated API is finally removed, and anything the API checks cannot see (string keys, resource names, behaviour changes).

`CHANGELOG.md` lists every change; breaking commits are marked `[**breaking**]` there.

## Unreleased (next release after 0.1.0)

Pre-1.0 minor release with breaking changes (accepted under the deprecation policy's pre-1.0 rule). The API breaks are recorded in `android/core/api/core.api` and `ios/api/breaking-changes.txt`.

### Changed behaviour: dialogs (issue #5)

- **A second dialog is queued, not refused (Compose `TSDialog`, SwiftUI `.tsDialog`).** While one dialog is open, a second request keeps its `isPresented` / binding `true` and is shown once the first has fully dismissed; `onPresentedChange(false)` and `onRefused` are no longer called for it. To get the old behaviour, set `TSDialogPresenter.shared.policy = TSDialogPolicy.Refuse` (Android) / `.refuse` (iOS), or pass `presenter = TSDialogPresenter(policy = TSDialogPolicy.Refuse)` to a Compose `TSDialog`. Remove app-side gates that existed only to stop dialogs from being dropped.
- **Action handlers run before the dialog closes (all four toolkits).** A dismissing action now runs its handler while the dialog is still on screen, then closes it. Handlers that read state cleared on dismissal now work; drop `dismisses = false` + manual close workarounds added for that. Code that relied on the dialog being gone when the handler runs must change:
  - **iOS, presenting other UI from a handler** (`UIActivityViewController`, `UIAlertController`, a SwiftUI `.sheet` / `.alert`): move it to the new `afterDismissal` step, `TSDialogAction(_:variant:handler:afterDismissal:)`; from the handler UIKit drops it because the dialog is still presented.
  - **Chaining** ("Delete" → "Undo?") still works by default: Compose and SwiftUI queue the follow-up, UIKit `present(from:)` called inside the handler returns `true` and presents after the dismissal, Views `present()` inside the handler takes the slot over. **Under the `refuse` policy a follow-up from a handler is refused**, so do not switch to `refuse` if you chain dialogs.
- **Compose:** honour `onPresentedChange(false)`. A dialog whose close is vetoed by ignoring it stays on screen with its actions disabled; keep a dialog open with a `dismisses = false` action instead.
- **Android immersive mode:** dialog windows now inherit the host Activity's hidden system bars and `systemBarsBehavior`, and re-hide the host's bars on close. Remove any workaround that re-hid the bars after a dialog.
- UIKit `present(from:)` and Views `present(manager, tag)` still return `false` on a conflict outside an action handler, under either policy.

### Source and binary changes

| Platform | Change | What to do |
|---|---|---|
| Android `:core` | `TSKeyboardType` gains `Ascii` | Add a branch to any exhaustive `when (keyboardType)` |
| Android `:core` | `TSTextFieldStyleSpec.Configuration` gains `contentStyle: TSTextFieldContentStyle = Default`; the JVM constructor and `copy` change from `(Boolean)` to `(Boolean, TSTextFieldContentStyle)` | Recompile. Kotlin calls with the default compile unchanged; positional destructuring gains a second component |
| iOS `TSFoundation` | `TSStringKey` gains `listItemExpanded` and `listItemCollapsed` | Add the two cases to any exhaustive `switch` over `TSStringKey` |

Everything else is additive: new overloads and properties keep the existing signatures (`TSIconSource`, `leadingIconSource` / `trailingIconSource`, `maxLines` / `overflow`, `contentStyle`, `TSTextInputTraits.Code` / `.code`, `TSSpinner`, `TSDivider`, `TSDisclosureListItem`, `TSText`, `TSTypeRole` on iOS, `TSTextColor`, `TSControllerColors` / `TSControllerColor`, `TSDialogPolicy`, `TSDialogRequestResult`).

### New library strings

| iOS key | Android resource | English |
|---|---|---|
| `ts.list-item.expanded` | `ts_list_item_expanded` | "Expanded" |
| `ts.list-item.collapsed` | `ts_list_item_collapsed` | "Collapsed" |

Apps that translate the library strings ([theming.md](theming.md#overriding-library-strings)) should add them.

## 0.1.0

First release. Nothing to migrate.

Names that changed during pre-release development, in case early integration code used them:

| Before | Now |
|---|---|
| SwiftUI `TSIconView` | `TSIcon` |
| UIKit `TSIconImageView` | `TSIconView` |

## Template for a breaking release

```markdown
## X.0.0

### Removed
| Platform | Removed | Use instead | Deprecated since |
|---|---|---|---|
| iOS | `TSOld` | `TSNew` | 1.4.0 |

### Renamed tokens
| Old token | New token |
|---|---|

### Changed behaviour
- What changed, what to do.

### Minimum OS
- iOS 16 / API 28 (if raised).
```
