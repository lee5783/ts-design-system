# Dialog

Cross-toolkit contract for the TS Dialog, center and sheet, and its presenter. Source: `design/source/components/Dialog/README.md`, `bundle.css` (`.ts-dialog*`), `index.d.ts` (`DialogProps`). Tokens: `overlay`, `surface-elevated`, `text-primary/secondary`, `border-strong`, `title-2`, `body`, `radius-xl`, `shadow-lg`, `space-2/3/4/6`, `dialog-max-width` (440) and `sheet-grabber-width/height` (36 × 4, `design/extensions/native-tokens.json`). Decisions: C-01 (iOS 15 through `TSSheetBridge` / `TSOverFullScreenBridge`), C-29 (`TSDialogViewController`), C-35 (center vs sheet); DS rule "never stack dialogs".

## API mapping

| DS (web) | Style spec / logic (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `Dialog` | `TSDialogStyleSpec` | `.tsDialog(isPresented:title:…)` | `TSDialogViewController` | `TSDialog(isPresented, onPresentedChange, title, …)` | `TSDialogFragment` (center), `TSBottomSheetDialogFragment` (sheet) |
| `variant` `center · sheet` | `TSDialogVariant` | `variant:` (default center) | `variant` (init) | `variant =` (default `Center`) | the fragment class |
| `title` (required) | `titleFont` / `titleTypeRole` | `title:` | `dialogTitle` (init `title:`) | `title =` | `dialogTitle` |
| `children` (body) | `bodyFont` / `bodyTypeRole` | `message:` or `@ViewBuilder content` | `message` or `bodyView` | `message =` overload or trailing `content` | `message` or `bodyView` |
| `actions` (buttons, primary last) | `TSDialogAction` (label, button variant, `dismisses`, handler / `onClick`), `TSDialogLayout.orderedActions` | `actions:` | `actions` | `actions =` | `actions` |
| `open` | — | `isPresented: Binding<Bool>` | `present(from:animated:completion:) -> Bool` | `isPresented` + `onPresentedChange` (a binding) | `present(manager, tag): Boolean` (`show` / `showNow` route through it) |
| `onClose` (scrim) | — | `onDismiss:` | `onDismiss` | `onDismiss =` | `onUserDismiss` (`DialogFragment.onDismiss` is taken) |
| "never stack dialogs" | `TSDialogPresenter` (`shared`, `begin` / `end`, `refusedCount`; Android adds `addListener`) | used internally | used by `present(from:)` | `presenter =` (default `shared`) | `presenter` (default `shared`) |
| (native) refusal | — | `onRefused:` | `present(from:)` returns `false` | `onRefused =` | `present(manager, tag)` returns `false` |
| `contained` | — | not mapped (native presentations are window-level) | not mapped | not mapped | not mapped |

`onDismiss` fires when the user closes the dialog without an action: scrim tap, escape, the "Close" accessibility action or a sheet drag. An action closes the dialog (unless `dismisses: false`) and then runs its own handler; it does not call `onDismiss`.

## Presentation

| | SwiftUI | UIKit |
|---|---|---|
| Center, every OS | `TSOverFullScreenBridge`: a `UIHostingController` presented `.overFullScreen` + `.crossDissolve` from a hidden anchor controller | `.overFullScreen` + `.crossDissolve` |
| Sheet, iOS 16.4+ | native `.sheet` + `presentationDetents([.height(fit), .large])`, `presentationDragIndicator(.visible)`, `presentationCornerRadius(16)`, `presentationBackground(surface-elevated)` (`View+SheetPresentationCompat`) | `.pageSheet` + `UISheetPresentationController`, custom fit detent + `.large` |
| Sheet, iOS 15–16.3 | `TSSheetBridge`: `UISheetPresentationController` with `.medium()` / `.large()` (fit + large on 16.0–16.3) | `.medium()` / `.large()` |
| Sheet, reduce motion | `TSSheetBridge` with `.crossDissolve` | `.crossDissolve` |

- The route is `TSDialogRoute.resolve(variant:sheetPath:reduceMotion:)`; the OS paths are `TSCompat.sheetPresentation` (16.4) and `TSCompat.customSheetDetents` (16), injectable for tests.
- Detents come from `TSSheetCompat` (`detents(path:)`, `configure(_:cornerRadius:path:fitHeight:)`): corner radius 16, system grabber visible, the first detent selected. There is no fit-content detent on iOS 15, so the fallback opens at medium.
- The bridges forward the host's colour scheme, dynamic type size, layout direction and locale. `TSDialogViewController.present(from:)` copies the source's interface style, content size category and layout direction onto its presentation.
- The sheet's scrim is the system dimming view, and its grabber is the system one (36 × 5, system colour), not the DS 36 × 4 `border-strong` bar. The panel still reserves the DS grabber space (12 + 4 + 16 = 32 pt above the title).
- **iOS 26 sheet geometry:** on iOS 26 the system draws sheets in its floating, inset style (rounded on all corners, inset from the screen edges) instead of the full-width, top-rounded sheet of earlier versions, in both toolkits. The DS "full width, top corners 16" geometry in the matrix applies to iOS 15–18 and to Android; on iOS 26 the system geometry wins and `preferredCornerRadius` / `presentationCornerRadius` are system-managed.

### Android

| | Compose | Views |
|---|---|---|
| Center, every API | `Dialog(DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false))`; window dim off, the DS `overlay` scrim drawn full screen, panel fades in and out over 150 ms | `TSDialogFragment`: an `AppCompatDialog` with `ThemeOverlay.TS.CenterDialog` (full-screen, transparent, no dim, fade window animation) hosting `TSDialogScrimLayout` (the `overlay` scrim) |
| Sheet, every API | M3 `ModalBottomSheet` (`skipPartiallyExpanded`, `sheetMaxWidth = Unspecified`, top radius 16, `containerColor = surface-elevated`, `tonalElevation = 0`, `scrimColor = overlay`, `dragHandle = null`; the panel draws the DS 36 × 4 handle) | `TSBottomSheetDialogFragment`: MDC `BottomSheetDialog` (`skipCollapsed`, expanded), the panel draws its own `surface-elevated` fill, top radius 16 and the DS handle; window dim = the `overlay` alpha |
| Back, API 26–32 | the dialog window's `OnBackPressedDispatcher` (`PredictiveBackHandler` gets one completion) | `OnBackPressedCallback.handleOnBackPressed` |
| Predictive back, API 34+ | center: `PredictiveBackHandler` scales the panel 1 → 0.9 with progress, restores on cancel, dismisses on commit; sheet: M3's built-in predictive back | center: `handleOnBackProgressed` / `handleOnBackCancelled` scale the panel the same way; sheet: MDC's built-in predictive back |

- Both demo apps set `android:enableOnBackInvokedCallback="true"`, which predictive back on API 33+ needs.
- Panels resolve to `surface-elevated` with no M3 tint (T17-fix): Compose passes the token as `containerColor` with `tonalElevation = 0`; Views draws a `GradientDrawable` and `Theme.TS` disables the elevation overlay.
- The pane title is the dialog title (`semantics { paneTitle }` / `ViewCompat.setAccessibilityPaneTitle`), the window title is set on the Views dialog, and the title is a heading.
- The scrim is not an accessibility element (Compose uses `pointerInput`, not `clickable`; Views sets `IMPORTANT_FOR_ACCESSIBILITY_NO`).
- Back and predictive back replace the iOS escape gesture. The panel also exposes `ACTION_DISMISS` and a "Close" custom action (`ts_close`).

## Presenter (no stacking)

`TSDialogPresenter.shared` grants one dialog at a time. A second presentation while one is open, or while it is still dismissing, is **refused** (not queued): SwiftUI resets the second binding to `false` and calls `onRefused`; UIKit `present(from:)` returns `false` and does not present. `refusedCount` counts refusals, and DEBUG builds log each refusal (`os.Logger`, category `TSDialogPresenter`).

- **Weak owner:** the presenter holds its owner weakly (iOS `weak`, Android `WeakReference`). A deallocated owner frees the presenter, so a dialog that disappears without a dismiss callback can never wedge it. A presentation that UIKit itself refuses (the source is already presenting) or that the SwiftUI coordinator rolls back ends the claim at once and reports a refusal; the Views presenter rolls the claim back if `show` throws.
- **Handlers run after dismissal, so actions can chain:** a dismissing action's handler runs in the dismiss completion, after the presenter is released (UIKit `perform`, SwiftUI `TSDialogSession.withdraw(then:)`, Compose `TSDialogPendingAction`, Views after `onDismiss`). A handler may therefore present the next dialog ("Delete" → "Undo?"). A non-dismissing action (`dismisses: false`) runs its handler while the dialog stays open. The presenter is released when the dialog has finished dismissing (`viewDidDisappear`, the sheet's `presentationControllerDidDismiss`, the bridge's dismiss completion, or the native sheet's `onDismiss`). A UIKit consumer that calls `present(_:animated:)` directly bypasses the refusal.

Android uses the same contract (`TSDialogPresenter.shared` in `:core`, owners compared by identity):

- Compose: a refused `TSDialog` calls `onPresentedChange(false)` and renders nothing, like the SwiftUI binding reset. It then calls `onRefused` when one is set; `refusedCount` and `addListener` also make refusals observable (the demo's caption uses the listener). The presenter is released when the center fade-out or the sheet hide animation has finished, or when the `TSDialog` leaves composition.
- Views: `present(manager, tag)` returns `false` when refused, and also, before claiming the presenter, when the `FragmentManager` has saved its state or is destroyed (`isStateSaved || isDestroyed`), because a commit would then throw or be lost. `show`, `showNow` and `show(transaction, tag)` are overridden to go through the presenter, and `onCreate` claims the presenter again, so a fragment added by a raw `FragmentTransaction` is removed when refused. **There is no bypass on Android**, unlike UIKit `present(_:animated:)`. The presenter is released in `onDismiss` (and `onDestroy` as a fallback).
- Views dialogs are not restored after recreation (configuration change or process death): handlers cannot be saved, so a restored `TSDialogFragment` / `TSBottomSheetDialogFragment` removes itself instead of showing a dialog whose buttons do nothing.
  - **Consumer contract:** a Views consumer that keeps "dialog open" in its own state (a `ViewModel`, saved state) must re-present the dialog from that state after recreation, with fresh handlers. Compose does not need this, because `TSDialog` re-shows from a saved `isPresented`.
  - **Signal:** when a restored fragment removes itself, it sets a `FragmentResult` on its parent `FragmentManager` under `TSDialogFragmentResults.DROPPED` (`"ts.dialog.dropped"`). The bundle carries the fragment tag under `TSDialogFragmentResults.KEY_TAG` (`"ts.dialog.tag"`, null when shown without a tag). Register with `supportFragmentManager.setFragmentResultListener(TSDialogFragmentResults.DROPPED, owner) { _, bundle -> … }`; the result is kept until a listener is set, so a listener registered in the recreated activity's `onCreate` still receives it. A refusal on first show does not emit it: there `present()` returning `false` is the signal.


## Matrix

`TSDialogStyleSpec.allConfigurations` (Android `configurations`): center/sheet × default/accessibility text = 4.

| | Center | Sheet |
|---|---|---|
| Panel | max width 440, outer margin 16, all corners `radius-xl` 16, `shadow-lg` | full width, top corners 16, no shadow |
| Padding | 24 all round | 12 top (+ 4 grabber + 16 gap), 16 sides, 24 bottom (+ safe area) |
| Title | `title-2` (22/28, 600) `text-primary`, header; 8 below | same |
| Body | `body` `text-secondary` | same |
| Actions | 24 above, gap 8, trailing row, primary last | stacked, block width, **primary on top** |
| Accessibility sizes | stacked like the sheet | stacked |

Scrim: `overlay` (center). Fill: `surface-elevated`. Enter/exit motion is undefined in the DS: center fades (cross-dissolve); sheets use the system slide, or a fade with reduce motion.

## Accessibility

- Modal: the presented view sets `accessibilityViewIsModal` (SwiftUI also adds the `.isModal` trait to the panel).
- Focus moves to the title when the dialog appears (SwiftUI `@AccessibilityFocusState`; UIKit `.screenChanged` with the title label).
- The escape gesture (two-finger Z) closes the dialog, and the panel has a "Close" custom action (`ts.close`) so the decorative grabber never blocks dismissal.
- **Hardware keyboard Escape** closes the dialog like the escape gesture: UIKit returns a `UIKeyCommand.inputEscape` key command (with priority over system behaviour) and becomes first responder in `viewDidAppear`; SwiftUI places a hidden, zero-size, accessibility-hidden button with `.keyboardShortcut(.cancelAction)` in the panel. Both go through the user-dismiss path, so `onDismiss` fires. Android uses back instead.
- The scrim is not an accessibility element.
- Body content scrolls when it is taller than the screen (AX5).

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSDialogStyleSpecTests.swift` | 4 configurations → scrim, fill, radius, padding, grabber, text, action axis/order/block; action ordering |
| Presenter | `ios/Tests/TSFoundationTests/TSDialogPresenterTests.swift` | **second presentation refused**, same owner idempotent, only the owner ends, shared instance |
| Compat | `ios/Tests/TSFoundationTests/TSSheetCompatTests.swift` | runtime paths (16.4 / 16); **detent set by OS path** (fit + large vs medium + large); radius, grabber, selection |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSDialogBehaviorTests.swift` | **bridge chosen below 16.4** and with reduce motion; bridge configuration on the iOS 15 path; the coordinator presents from its anchor and defers without a window; drag dismissal reported; session refusal; title header; action order and taps; escape / Close; heights and the 440 cap |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSDialogViewControllerBehaviorTests.swift` | center presentation and scrim; **`accessibilityViewIsModal`**; **detents by OS path**; fit height; action order per variant and at AX5; **second dialog refused** (real presentation in a window); escape, scrim and Close dismiss and free the presenter; actions; drag dismissal; trait inheritance; reduce motion |
| Snapshots | `TSDialogSnapshotTests`, `TSDialogViewSnapshotTests` (`dialog.center-*`, `dialog.sheet-*`) | panel over the scrim; standard matrix |
| Demo | `FeedbackComponentsTests` (both iOS demos): `testCenterDialogOpensAndScrimDismisses`, `testSheetOpensAndDismisses`, `testSecondDialogIsRefusedWhileOneIsOpen` | |
| Spec (Android) | `android/core/src/test/.../style/TSDialogStyleSpecTest.kt` | 4 configurations → scrim, fill, radius, shadow, max width, padding, grabber, text, action axis/order/block; accessibility size from font scale ≥ 1.5; predictive-back scale; action ordering and defaults |
| Presenter (Android) | `android/core/src/test/.../logic/TSDialogPresenterTest.kt` | **second presentation refused**, same owner idempotent, only the owner ends, identity owners, listeners, shared instance |
| Compose | `android/compose/src/test/.../components/dialog/TSDialogTest.kt` (sdk 26, 36) | dialog window, pane title, heading; center row primary last; sheet grabber and primary first; action closes without `onDismiss`; `dismisses = false`; **scrim and back dismiss** (center and sheet); **predictive back** progress scales the panel, cancel restores, commit dismisses; accessibility dismiss and "Close"; **second dialog refused** and its binding reset; presenter freed |
| Views | `android/views/src/test/.../components/dialog/TSDialogFragmentTest.kt` (sdk 26, 36) | pane title, heading, scrim not accessible, 440 cap; row vs stacked actions; action dismisses without `onUserDismiss`; **scrim tap and back dismiss** and free the presenter; panel tap ignored; **predictive back** scale / cancel / commit through the dialog's dispatcher; accessibility dismiss and "Close"; **second `present` refused**, `show` routed through the presenter, a raw transaction cannot bypass it |
| Snapshots (Android) | `TSDialogSnapshotTest`, `TSDialogViewSnapshotTest` (`center_*`, `sheet_*`) | panel over the scrim; standard matrix |
| Demo (Android) | `OverlayFlowsTest` (both Android demos): `centerDialogOpensAndScrimDismisses`, `sheetOpensActionClosesAndBackDismisses`, `backDismissesTheCenterDialog`, `predictiveBackGestureDismissesCenterAndSheet` (API 34+, gesture navigation; an injected edge swipe), `secondDialogIsRefusedWhileOneIsOpen` | |

## Platform notes

- XCUITest cannot send Escape or the VoiceOver escape gesture to an iOS app, so escape is covered by the behaviour tests only.
- Android: back and predictive back replace escape. See *Presentation → Android* for the routes.
- Android accessibility size is `fontScale >= 1.5` (`TSDialogStyleSpec.isAccessibilitySize`, the ListItem/Card threshold); center actions then stack at block width, primary first.
- Android action buttons keep the 48 dp touch target, so stacked actions are taller than on iOS.
- The Views sheet's scrim [est] is the window dim at the `overlay` alpha, so in light mode it is black at 40 %, not `#18181B` at 40 %; the dark overlay is black, so it matches. The Compose sheet and both center dialogs draw the exact `overlay` colour.
- The cancelled predictive-back path is covered by the Robolectric tests; the instrumented test only commits, because an injected cancel gesture under `UiAutomation` was not reliable on the API 35 emulator (it works when injected from `adb`).
- `TSDialog` in Compose takes `List<TSDialogAction>`, which the Compose lint reports as unstable (same as `TSCard`).
