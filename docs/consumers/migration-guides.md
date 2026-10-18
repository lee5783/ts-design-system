# Migration guides

One section per release that removes or renames public API, tokens, string keys or resources, newest first. Deprecations are announced a minor release ahead with a replacement ([../guides/deprecation-policy.md](../guides/deprecation-policy.md)); this page lists what to change when the deprecated API is finally removed, and anything the API checks cannot see (string keys, resource names, behaviour changes).

`CHANGELOG.md` lists every change; breaking commits are marked `[**breaking**]` there.

## Unreleased (next release after 0.1.0)

Pre-1.0 minor release with breaking changes (accepted under the deprecation policy's pre-1.0 rule). The API breaks are recorded in `android/core/api/core.api` and `ios/api/breaking-changes.txt`.

### Changed: Android distribution, coordinates and packages

Android ships through JitPack only; GitHub Packages is gone. Setup: [android-getting-started.md](android-getting-started.md).

| Before | After |
|---|---|
| repository `https://maven.pkg.github.com/lee5783/ts-design-system` with a `read:packages` token (`gpr.user` / `gpr.key`) | `maven("https://jitpack.io")`, no credentials; delete the token properties |
| `com.ts.ds:bom:X.Y.Z`, `com.ts.ds:{compose,views}` | `com.github.lee5783.ts-design-system:bom:vX.Y.Z` (the tag, `v` included), `com.github.lee5783.ts-design-system:{compose,views}` |
| `includeGroup("com.ts.ds")` | `includeGroup("com.github.lee5783.ts-design-system")` |
| Kotlin imports and XML view tags `com.ts.ds.<module>.…` | `com.tinysol.designsystem.<module>.…` (for example `com.tinysol.designsystem.compose.theme.TSTheme`, `<com.tinysol.designsystem.views.components.button.TSButtonView>`) |

Class names, XML attributes, styles and resource names are unchanged, so a search-and-replace of the package prefix plus the dependency block is the whole migration. iOS is unaffected (SPM package, products and modules keep their names).

### Removed: the screen-reader accessibility layer

The libraries no longer author screen-reader (TalkBack and VoiceOver) semantics, announcements or labels, and no longer read the OS reduce-motion setting. Everything below is a binary and source break. The API breaks are recorded in `android/{core,views,compose}/api/*.api` and `ios/api/breaking-changes.txt`.

**What stays:** Dynamic Type and font-scale layout (including the stacked footer and accessibility-size dialog layout), minimum touch targets and hit areas, RTL and `layoutDirection`, `accessibilityIdentifier` on iOS and `testTag` on Android, and the library's identifier forwarding. Visible labels (button, tab, bottom-nav item, text-field, dialog action titles) are unchanged.

**Android Views XML attribute (fails resource linking).** `app:tsAnnouncesWhenShown` on `TSAlertView` is removed. A layout that still sets it fails AAPT resource linking after the upgrade; delete the attribute. The `tsLabel` entries are also gone from the `TSTabsView` and `TSBottomNavView` styleables (the `tsLabel` attribute stays for `TSTextFieldView`).

| Platform | Removed | What to do |
|---|---|---|
| Android Views | `TSAlertView.announcesWhenShown`, `TSCardView.accessibilityActions`, `TSButtonView.loadingDescription`, `TSTabsView.label`, `TSBottomNavView.label`, `setAccessibilityRole(View, ...)` (`com.tinysol.designsystem.views.foundation`) | Delete the calls. Cards expose their footer actions as ordinary buttons |
| Android Compose | `TSAlert(announcesWhenShown)`, `TSCard(accessibilityActions)` (all 4 overloads), `TSButton(loadingDescription)` (both overloads), `TSSpinner(contentDescription)`, `TSTabs(label)`, `TSBottomNav(label)`, `TSIcon(contentDescription)`, `TSIconButton(contentDescription)` (both overloads) | Remove the argument. `TSIcon(icon, modifier, size, tint)` is the new signature |
| Android `:core` | `TSAlertAccessibility`, `TSBottomNavAccessibility`, `TSCardAccessibility`, `TSListItemAccessibility`, `TSTabsAccessibility`, `TSTextFieldAccessibility`, `TSToastAccessibility`, `TSToggleAccessibility`, `com.tinysol.designsystem.core.motion.TSReduceMotion`, `TSCardAccessory.customAction`, `TSIconStyleSpec.Configuration.hasLabel`, `TSIconStyleSpec.Resolved.importantForAccessibility` | Delete the calls. `TSListItemRole`, `TSCardAction` and `TSCardAccessory` stay. `TSIconStyleSpec.Configuration` is now one entry per size |
| Android `:core` | `TSToastTimeouts.resolve(...)` (both overloads), `timeoutFor(Context)`, `SCREEN_READER_MILLIS`, `RECOMMENDED_TIMEOUT_MIN_SDK` | Use `TSToastTimeouts.default`, or the constants `DEFAULT_MILLIS` (4 s) and `EXTENDED_MILLIS` (10 s, new) |
| iOS SwiftUI (`TSDesignSystem`) | `accessibilityActions:` parameter on every `TSCard` initializer, `announcesOnAppear:` on `TSAlert`, `label:` on `TSBottomNav`, `TSBottomNavShell`, `TSIcon` and `TSTabs` | Remove the argument |
| iOS UIKit (`TSDesignSystemUIKit`) | `TSAlertView.announcesWhenShown` and the `announcesWhenShown:` initializer argument, `TSCardView.accessibilityActions` and the `accessibilityActions:` argument, `label` on `TSBottomNavView`, `TSIconView`, `TSTabsView` and `TSTabBarController` (properties and initializer arguments), the `accessibilityActivate()`, `accessibilityPerformEscape()` and `accessibilityTraits` overrides on the control views | Remove the argument or call |
| iOS `TSFoundation` | `TSAlertAccessibility`, `TSAvatarAccessibility`, `TSBottomNavAccessibility`, `TSCardAccessibility`, `TSListItemAccessibility`, `TSTextFieldAccessibility`, `TSToastAccessibility`, `TSToggleAccessibility`, `TSToggleKind`, `TSToggleTrait`, `TSAccessibilityTraitsCompat`, `TSCompat.toggleTrait`, `TSReduceMotion`, `View.tsAnimation(_:duration:easing:)`, `View.tsToggleAccessibility(isOn:kind:path:)`, `TSPressFeedback.animation(reduceMotion:)`, `TSCardAccessory.customAction`, `TSIconStyleSpec.Configuration.hasLabel`, `TSIconStyleSpec.Resolved.isAccessibilityElement`, `TSToastShowEffects.init(announce:haptics:)` (now `init(haptics:)`), `TSToastQueue.init(scheduler:isVoiceOverRunning:)` (now `init(scheduler:)`), `TSToastTimeout.duration(hasAction:isVoiceOverRunning:standard:)` | Delete the calls. `TSListItemRole` stays |
| iOS `TSFoundation` | `TSToastQueue.timeout(for:)` returns `TimeInterval` instead of `TimeInterval?` | Drop optional handling at call sites |

**Removed string keys and resources.** Apps that translated or overrode these ([theming.md](theming.md#overriding-library-strings)) can delete their overrides; code that names a removed `TSStringKey` case or `TSStrings` helper no longer compiles.

| iOS `TSStringKey` | Android resource |
|---|---|
| `position` | `ts_position_of_total` |
| `tabPosition` | `ts_tab_position` |
| `bottomNavBadge` | `ts_bottom_nav_badge` |
| `toggleOn`, `toggleOff` | `ts_switch_on`, `ts_switch_off` |
| `checkboxChecked`, `checkboxUnchecked` | `ts_checkbox_checked`, `ts_checkbox_unchecked` |
| `textFieldError` | `ts_text_field_error` |
| `listItemNavigates` | `ts_list_item_navigates` |
| `alertInfo`, `alertSuccess`, `alertWarning`, `alertDanger` | `ts_alert_info`, `ts_alert_success`, `ts_alert_warning`, `ts_alert_danger` |
| (none) | `ts_list_item_expanded`, `ts_list_item_collapsed` |

The `TSStrings.position`, `tabPosition`, `bottomNavBadge`, `textFieldError`, `alertTone`, `toastTone`, `checkboxChecked`, `checkboxUnchecked` and `listItemNavigates` helpers are removed with them. The keys `loading`, `close`, `dismiss`, `countOverflow`, `showPassword` and `hidePassword` (`ts_loading`, `ts_close`, `ts_dismiss`, `ts_badge_count_overflow`, `ts_text_field_show_password`, `ts_text_field_hide_password`) remain.

**Changed behaviour.**

- **Toast timeouts are fixed and no longer depend on the screen reader.** A plain toast lasts 4 s; a toast with an action lasts 10 s, on both platforms (Android `TSToastTimeouts.DEFAULT_MILLIS` / `EXTENDED_MILLIS`; iOS `TSToastTimeout.standard` / `.extended`, where `TSToastQueue.timeout(for:)` returns `max(duration ?? 4, 10)` for an action toast and `duration ?? 4` otherwise). The longer timeout for screen-reader users, the Android system recommended timeout (API 29+) and the "never auto-dismiss with an action" rule are gone.
- **No reduce-motion handling.** The libraries no longer read the OS reduce-motion setting; press feedback and transitions always animate. An app that must honour reduce motion has to do so itself.
- **No announcements or semantics.** Alerts and toasts no longer post announcements when shown, controls no longer set state, role, position or error descriptions, and cards no longer merge their children or expose custom actions. Icons and avatars are no longer exposed as labelled elements, and the library sets no accessibility label from its own strings.
- **Tests that located nodes by accessibility label or state** (`contentDescription`, `stateDescription`, `accessibilityLabel`, `accessibilityValue`) must switch to `testTag` / `accessibilityIdentifier` or visible text.

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

Everything else is additive: new overloads and properties keep the existing signatures (`TSIconSource`, `leadingIconSource` / `trailingIconSource`, `maxLines` / `overflow`, `contentStyle`, `TSTextInputTraits.Code` / `.code`, `TSSpinner`, `TSDivider`, `TSDisclosureListItem`, `TSText`, `TSTypeRole` on iOS, `TSTextColor`, `TSControllerColors` / `TSControllerColor`, `TSDialogPolicy`, `TSDialogRequestResult`).

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
