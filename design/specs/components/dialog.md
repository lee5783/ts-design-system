# Dialog

Cross-toolkit contract for the TS Dialog, center and sheet, and its presenter. Source: `design/source/components/Dialog/README.md`, `bundle.css` (`.ts-dialog*`), `index.d.ts` (`DialogProps`). Tokens: `overlay`, `surface-elevated`, `text-primary/secondary`, `border-strong`, `title-2`, `body`, `radius-xl`, `shadow-lg`, `space-2/3/4/6`, `dialog-max-width` (440) and `sheet-grabber-width/height` (36 × 4, `design/extensions/native-tokens.json`). Decisions: C-01 (iOS 15 through `TSSheetBridge` / `TSOverFullScreenBridge`), C-29 (`TSDialogViewController`), C-35 (center vs sheet); DS rule "never stack dialogs"; issue #5 decisions C-021 (queue by default, handlers before dismissal), C-022 (UIKit and Views keep refusing), C-023 (queue semantics), C-024 (immersive inheritance on Android).

## API mapping

| DS (web) | Style spec / logic (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `Dialog` | `TSDialogStyleSpec` | `.tsDialog(isPresented:title:…)` | `TSDialogViewController` | `TSDialog(isPresented, onPresentedChange, title, …)` | `TSDialogFragment` (center), `TSBottomSheetDialogFragment` (sheet) |
| `variant` `center · sheet` | `TSDialogVariant` | `variant:` (default center) | `variant` (init) | `variant =` (default `Center`) | the fragment class |
| `title` (required) | `titleFont` / `titleTypeRole` | `title:` | `dialogTitle` (init `title:`) | `title =` | `dialogTitle` |
| `children` (body) | `bodyFont` / `bodyTypeRole` | `message:` or `@ViewBuilder content` | `message` or `bodyView` | `message =` overload or trailing `content` | `message` or `bodyView` |
| `actions` (buttons, primary last) | `TSDialogAction` (label, button variant, `dismisses`, handler / `onClick`; iOS also `afterDismissal`), `TSDialogLayout.orderedActions` | `actions:` | `actions` | `actions =` | `actions` |
| `open` | — | `isPresented: Binding<Bool>` | `present(from:animated:completion:) -> Bool` | `isPresented` + `onPresentedChange` (a binding) | `present(manager, tag): Boolean` (`show` / `showNow` route through it) |
| `onClose` (scrim) | — | `onDismiss:` | `onDismiss` | `onDismiss =` | `onUserDismiss` (`DialogFragment.onDismiss` is taken) |
| "never stack dialogs" | `TSDialogPresenter` (`shared`; `policy: TSDialogPolicy` `queue · refuse`, default queue; `request` / `end` for declarative hosts, `begin` / `end` for imperative ones; `pendingCount`, `isPending`, `refusedCount`; Android adds `addListener`) | uses `TSDialogPresenter.shared` (set `TSDialogPresenter.shared.policy` to choose) | used by `present(from:)` | `presenter =` (default `shared`; pass `TSDialogPresenter(policy = TSDialogPolicy.Refuse)` to opt out of queueing) | `presenter` (default `shared`) |
| (native) conflict | — | queued (binding untouched); under `.refuse`: binding reset + `onRefused:` | `present(from:)` returns `false` (either policy; see chaining) | queued (binding untouched); under `Refuse`: `onPresentedChange(false)` + `onRefused =` | `present(manager, tag)` returns `false` (either policy; see chaining) |
| `contained` | — | not mapped (native presentations are window-level) | not mapped | not mapped | not mapped |

`onDismiss` fires when the user closes the dialog without an action: scrim tap, escape, or a sheet drag. An action runs its own handler and then closes the dialog (unless `dismisses: false`); it does not call `onDismiss`. See *Presenter → Action order* for the exact sequence.

## Presentation

| | SwiftUI | UIKit |
|---|---|---|
| Center, every OS | `TSOverFullScreenBridge`: a `UIHostingController` presented `.overFullScreen` + `.crossDissolve` from a hidden anchor controller | `.overFullScreen` + `.crossDissolve` |
| Sheet, iOS 16.4+ | native `.sheet` + `presentationDetents([.height(fit), .large])`, `presentationDragIndicator(.visible)`, `presentationCornerRadius(16)`, `presentationBackground(surface-elevated)` (`View+SheetPresentationCompat`) | `.pageSheet` + `UISheetPresentationController`, custom fit detent + `.large` |
| Sheet, iOS 15–16.3 | `TSSheetBridge`: `UISheetPresentationController` with `.medium()` / `.large()` (fit + large on 16.0–16.3) | `.medium()` / `.large()` |

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
- Back and predictive back replace the iOS escape gesture. The panel also exposes `ACTION_DISMISS` and a "Close" custom action (`ts_close`).
- **Immersive mode is inherited (C-024), automatically, no API.** Before the dialog window first draws, it copies the host Activity window's hidden status / navigation bars and `systemBarsBehavior` (`WindowInsetsControllerCompat`); on API 26–29 a host hidden with `WindowManager.LayoutParams.FLAG_FULLSCREEN` also counts as a hidden status bar. When the dialog closes, the host's captured state is applied to the host again, because on API 26 opening a dialog window cleared the host's hidden flags. Applies to the Compose center `Dialog` and `ModalBottomSheet` windows (internal `TSDialogSystemBars.kt` in the Compose dialog folder, applied in a `SideEffect` of the first composition) and to both Views fragments (`TSDialogSession.inheritSystemBars`, at the start of `onStart`, before the dialog shows).
  - The copy is one way, taken once per dialog: a host that shows or hides its bars while a dialog is open is not followed, and a host that showed its bars meanwhile has them hidden again on close.
  - The host is the Activity window from the dialog's context; a `TSDialog` opened from inside another window still inherits from the Activity.

## Presenter (one dialog on screen)

`TSDialogPresenter.shared` (`ios/Sources/TSFoundation/Logic/TSDialogPresenter.swift`, `android/core/src/main/kotlin/com/ts/ds/core/logic/TSDialogPresenter.kt`) grants one dialog at a time, on both platforms and all four toolkits. Dialogs never stack. What happens to a second request depends on the API and on the presenter's `policy` (`TSDialogPolicy`):

| Second request while one dialog is open or still dismissing | `queue` (default) | `refuse` |
|---|---|---|
| SwiftUI `.tsDialog`, Compose `TSDialog` (declarative, `request`) | **queued**: the binding stays `true`, nothing is called; the dialog shows once the first has fully dismissed | refused: the binding is reset (`onPresentedChange(false)` on Compose) and `onRefused` is called |
| UIKit `present(from:)`, Views `present(manager, tag)` (imperative, `begin`) | refused: returns `false` (C-022) | refused: returns `false` |

Choose the policy on the presenter: `TSDialogPresenter.shared.policy = .refuse` (iOS) / `TSDialogPresenter.shared.policy = TSDialogPolicy.Refuse` (Android), or, on Compose, pass your own `presenter = TSDialogPresenter(policy = TSDialogPolicy.Refuse)`. There is no per-`.tsDialog` policy on SwiftUI.

### Queue semantics (C-023)

- **FIFO, unbounded,** one entry per owner: a repeated request keeps its place and the latest callback.
- The head is granted only after the current dialog has **fully** dismissed (fade-out or sheet hide finished), so two dialogs are never on screen together.
- A pending request is dropped when its binding goes `false`, when its owner leaves composition / disappears (Compose `DisposableEffect`, SwiftUI `onDisappear`), or when the owner is collected (held weakly). Dead entries are pruned on every `request` and `end`.
- `pendingCount` and `isPending(owner)` expose the queue; `refusedCount` counts only real refusals (the `refuse` policy and the imperative APIs). Android `addListener` listeners hear enqueue, grant and withdrawal.
- `request(owner, onGranted)` returns `TSDialogRequestResult` `granted · queued · refused`; `end(owner)` ends the active presentation and grants the next live entry, or withdraws `owner`'s pending request. `begin(owner)` (imperative) never queues.
- **After recreation (Compose):** every `TSDialog` whose saved `isPresented` is `true` requests again in composition order, so the dialog composed first is shown first, which may not be the one that was on screen before. Not covered by a test.
- `handOver(from, to)` is public on both presenters but exists for the library's own chaining (below). Do not call it from app code: it can move the slot to an owner that never presents.

### Action order (C-021)

A **dismissing** action (`dismisses = true`, the default) runs its handler **first, while the dialog is still on screen and still holds the presenter**, and only then closes the dialog. The handler therefore sees the state that closing the dialog would clear (for example the selected item a "Remove" action needs). A non-dismissing action (`dismisses = false`) runs its handler and the dialog stays open.

| Toolkit | Sequence for a dismissing action |
|---|---|
| Compose | `action.onClick()`, then `onPresentedChange(false)`, then the fade-out, then the presenter is released. A second tap in the same frame is ignored (internal action guard) |
| SwiftUI | handler, then the binding is reset and the session withdraws; after the dismissal `TSDialogSession.didDismiss` releases the presenter and runs `afterDismissal` |
| UIKit | handler, then `dismiss`; the dismiss completion releases the presenter (or hands it to a chained dialog), then runs `afterDismissal` |
| Views | handler (the claim is kept), then `dismissAllowingStateLoss()` if the fragment is still added, otherwise `presenter.end`; the presenter is released in `onDismiss` |

Actions that arrive after the dialog started closing are ignored.

**Chaining** ("Delete" → "Undo?") keeps working with a handler-first order:

- **Compose, SwiftUI:** a dialog requested from the handler is **queued** and shown once the first has dismissed. Under the `refuse` policy it is **refused**, because the first dialog still owns the presenter: opting into `refuse` to "keep the old behaviour" loses chaining.
- **UIKit:** inside a dismissing handler, `present(from:)` on another `TSDialogViewController` returns `true` and defers the presentation to the end of the first dialog's dismissal, when the slot is handed over to it (so a queued SwiftUI request cannot take it first). The deferral applies only when the source is in the same window as the dismissing dialog (or the dialog was never presented). If the deferred presentation then fails, `completion` is still called and the slot is freed. Outside a handler, `present(from:)` still returns `false` on conflict.
- **Views:** inside a dismissing handler, a `present()` on the same presenter takes the slot directly through `handOver` (a scoped chain grant); a queued Compose request on the shared presenter waits until the Views dialog has dismissed.

**Presenting other UI from an action (iOS, `afterDismissal`).** While the handler runs, the dialog is still presented over the source, so UIKit drops a `present(_:)` of a non-TS controller (`UIActivityViewController`, `UIAlertController`) and a SwiftUI `.sheet` / `.alert` set from the handler. Put that work in the second phase:

```swift
TSDialogAction("Share", variant: .primary, handler: { prepareShare() }, afterDismissal: { presentShareSheet() })
```

`TSDialogAction(_:variant:handler:afterDismissal:)` implies `dismisses = true`. `afterDismissal` runs once the dialog has dismissed and released the presenter. It runs **after** a chained UIKit dialog is presented and after SwiftUI has granted the next queued dialog, so an `afterDismissal` that presents system UI can still be dropped while a chained or queued TS dialog is presenting: use one or the other for a given action. Android has no `afterDismissal`: an Activity, a `DialogFragment` or a `TSDialog` started from a handler opens its own window, so nothing is dropped.

### Consumer rules

- **Honour `onPresentedChange(false)` (Compose).** After a dismissing action, the dialog ignores further actions until `isPresented` becomes `true` again. A consumer that ignores `onPresentedChange(false)` (for example to veto a close after validation fails), or a handler that throws, leaves the dialog on screen with every action ignored. To keep a dialog open, use a `dismisses = false` action and close it yourself.
- **Mixed toolkits share one presenter.** Imperative and declarative dialogs in one app (UIKit + SwiftUI, Views + Compose) queue and refuse against each other.

### Weak owner and release

- The presenter holds its owner weakly (iOS `weak`, Android `WeakReference`). A deallocated owner frees the presenter, so a dialog that disappears without a dismiss callback can never wedge it. A presentation that UIKit itself refuses (the source is already presenting) or that the SwiftUI coordinator rolls back ends the claim at once and reports a refusal; the Views presenter rolls the claim back if `show` throws. A SwiftUI session granted from the queue whose binding goes `false` before it ever appeared releases the presenter.
- The presenter is released when the dialog has finished dismissing (`viewDidDisappear`, the sheet's `presentationControllerDidDismiss`, the bridge's dismiss completion, or the native sheet's `onDismiss`; Compose when the center fade-out or the sheet hide animation has finished, or when the `TSDialog` leaves composition). A UIKit consumer that calls `present(_:animated:)` directly bypasses the presenter.
- DEBUG builds log each refusal on iOS (`os.Logger`, category `TSDialogPresenter`).

Android specifics (`TSDialogPresenter.shared` in `:core`, owners compared by identity):

- Compose: `refusedCount`, `pendingCount` and `addListener` make refusals and the queue observable (the demo's "Refused" and "Queued presentations" captions use them).
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

Scrim: `overlay` (center). Fill: `surface-elevated`. Enter/exit motion is undefined in the DS: center fades (cross-dissolve); sheets use the system slide.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSDialogStyleSpecTests.swift` | 4 configurations → scrim, fill, radius, padding, grabber, text, action axis/order/block; action ordering |
| Presenter | `ios/Tests/TSFoundationTests/TSDialogPresenterTests.swift` | queue is the default (also for `.shared`); a second imperative `begin` refused under either policy; a request on a free presenter granted; a conflicting request queued and granted after `end`; `.refuse` refuses; FIFO one at a time; `end` withdraws a pending request; no duplicates, latest callback; `handOver`; a deallocated pending owner skipped; same owner idempotent, only the owner ends |
| Compat | `ios/Tests/TSFoundationTests/TSSheetCompatTests.swift` | runtime paths (16.4 / 16); **detent set by OS path** (fit + large vs medium + large); radius, grabber, selection |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSDialogBehaviorTests.swift` | **bridge chosen below 16.4** and with reduce motion; bridge configuration on the iOS 15 path; the coordinator presents from its anchor and defers without a window; drag dismissal reported; `sessionQueued` (queued, not refused, granted after the first dismisses); `sessionRefusal` under `.refuse`; `queuedSessionWithdrawn`; `grantedButNeverShownReleases`; `handlerRunsBeforeDismissal`; `nonDismissingAndLateActions`; `chainedFollowUpIsQueued`; `afterDismissalRunsOnceDismissed`, `afterDismissalPresentsAnAlert`; title header; action order and taps; escape / Close; heights and the 440 cap |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSDialogViewControllerBehaviorTests.swift` | center presentation and scrim; **`accessibilityViewIsModal`**; **detents by OS path**; fit height; action order per variant and at AX5; **second dialog refused** (real presentation in a window); `refusedOutsideAHandler`; `chainedDialog` (handler runs while the claim is held), `chainedDialogWaitsForTheDismissal`, `chainedHandOverAndFailure`, `otherWindowIsNotChained`; `afterDismissalPresentsAnAlert`; escape, scrim and Close dismiss and free the presenter; actions; drag dismissal; trait inheritance; reduce motion |
| Demo | `FeedbackComponentsTests`: `testCenterDialogOpensAndScrimDismisses`, `testSheetOpensAndDismisses` (both iOS demos); SwiftUI `testSecondDialogIsQueuedWhileOneIsOpenAndShownAfterItCloses`; UIKit `testSecondDialogIsRefusedWhileOneIsOpen` (refusal kept, C-022) | |
| Spec (Android) | `android/core/src/test/.../style/TSDialogStyleSpecTest.kt` | 4 configurations → scrim, fill, radius, shadow, max width, padding, grabber, text, action axis/order/block; accessibility size from font scale ≥ 1.5; predictive-back scale; action ordering and defaults |
| Presenter (Android) | `android/core/src/test/.../logic/TSDialogPresenterTest.kt` | `queueIsTheDefaultPolicy`; `secondBeginIsRefusedUnderEitherPolicy`; granted on a free presenter; queued then granted after `end`; `Refuse` refuses; FIFO one at a time; `end` withdraws; no duplicates, latest callback; collected pending and active owners; `handOver` (and ignored from a non-owner); listeners hear enqueue, grant and withdrawal; same owner idempotent, identity owners, shared instance |
| Compose | `android/compose/src/test/.../components/dialog/TSDialogTest.kt` (sdk 26, 36); `TSDialogSystemBarsTest.kt` (sdk 26, 36) | dialog window, pane title, heading; center row primary last; sheet grabber and primary first; `actionRunsThenClosesWithoutOnDismiss`; `handlerRunsBeforeOnPresentedChangeClearsItsState`; `aDoubleTapRunsADismissingActionOnce`; `dismisses = false`; **scrim and back dismiss** (center and sheet); **predictive back** progress scales the panel, cancel restores, commit dismisses; accessibility dismiss and "Close"; `secondDialogIsQueuedAndShownAfterTheFirstCloses`; `refusePolicyRefusesTheSecondDialogAndResetsItsBinding`; `queuedDialogIsDroppedWhenItsBindingGoesFalse` / `…WhenItLeavesComposition`; chained follow-up pending while the first dismisses; presenter freed. System bars: center and sheet keep the host's hidden bars, `FLAG_FULLSCREEN` host (sdk 26), visible host bars stay visible |
| Views | `android/views/src/test/.../components/dialog/TSDialogFragmentTest.kt` (sdk 26, 36) | pane title, heading, scrim not accessible, 440 cap; row vs stacked actions; `actionRunsThenDismissesWithoutUserDismiss`; center / sheet handler runs while the dialog is still showing; a handler that removes its dialog does not crash and frees the presenter; a queued request waits for the Views dialog; a chained dialog takes the slot ahead of a queued request; center and sheet keep the host's hidden bars, visible bars stay visible; **scrim tap and back dismiss** and free the presenter; panel tap ignored; **predictive back** scale / cancel / commit through the dialog's dispatcher; accessibility dismiss and "Close"; **second `present` refused**, `show` routed through the presenter, a raw transaction cannot bypass it |
| Demo (Android) | `OverlayFlowsTest` (both Android demos): `centerDialogOpensAndScrimDismisses`, `sheetOpensActionClosesAndBackDismisses`, `backDismissesTheCenterDialog`, `predictiveBackGestureDismissesCenterAndSheet` (API 34+, gesture navigation; an injected edge swipe), Compose `secondDialogIsQueuedWhileOneIsOpenAndShownAfterItCloses`, Views `secondDialogIsRefusedWhileOneIsOpen` (refusal kept, C-022) | |

## Platform notes

- XCUITest cannot send Escape to an iOS app, so escape is covered by the behaviour tests only.
- Android: back and predictive back replace escape. See *Presentation → Android* for the routes.
- Android accessibility size is `fontScale >= 1.5` (`TSDialogStyleSpec.isAccessibilitySize`, the ListItem/Card threshold); center actions then stack at block width, primary first.
- Android action buttons keep the 48 dp touch target, so stacked actions are taller than on iOS.
- The Views sheet's scrim [est] is the window dim at the `overlay` alpha, so in light mode it is black at 40 %, not `#18181B` at 40 %; the dark overlay is black, so it matches. The Compose sheet and both center dialogs draw the exact `overlay` colour.
- The cancelled predictive-back path is covered by the Robolectric tests; the instrumented test only commits, because an injected cancel gesture under `UiAutomation` was not reliable on the API 35 emulator (it works when injected from `adb`).
- `TSDialog` in Compose takes `List<TSDialogAction>`, which the Compose lint reports as unstable (same as `TSCard`).
- **Hardware keyboard Escape** closes the dialog and goes through the user-dismiss path, so `onDismiss` fires: UIKit returns a `UIKeyCommand.inputEscape` key command (with priority over system behaviour) and becomes first responder in `viewDidAppear`; SwiftUI places a hidden, zero-size button with `.keyboardShortcut(.cancelAction)` in the panel. Android uses back instead.
- Body content scrolls when it is taller than the screen (AX5).
