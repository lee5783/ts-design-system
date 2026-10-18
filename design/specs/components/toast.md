# Toast

Cross-toolkit contract for the TS Toast (snackbar), its queue and its hosts. Source: `design/source/components/Toast/README.md`, `bundle.css` (`.ts-toast*`), `index.d.ts` (`ToastProps`). Tokens: `surface-inverse`, `text-inverse`, `success/warning/danger/info-on-inverse` (C-26), `body-sm`, `radius-lg`, `shadow-lg`, `space-1/2/3/4`, `toast-max-width` (420) and `toast-action-font-weight` (600, `design/extensions/native-tokens.json`). Decisions: C-10 (the library owns the timer and the queue), C-13 (motion, opt-out tone haptics), C-26 (on-inverse icon colours).

## API mapping

| DS (web) | Style spec / logic (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `Toast` | `TSToastStyleSpec` | `TSToast` | `TSToastView: UIView` | `TSToast` | `TSToastView : LinearLayout` |
| `children` (one short sentence) | `messageFont` / `messageTypeRole` | `TSToast(_ message:)` | `message` | `TSToast(message)` | `message` / `app:tsMessage` |
| `tone` (adds a status icon) | `TSToastTone` `neutral · info · success · warning · danger` | `tone:` (default neutral) | `tone` | `tone =` (default `Neutral`) | `tone` / `app:tsToastTone` |
| `actionLabel` + `onAction` | `Configuration.hasAction` | `actionTitle:` + `onAction:` | `actionTitle` + `onAction` | `actionLabel =` + `onAction =` | `actionLabel` / `app:tsActionLabel` + `onAction` |
| "consumer handles the timer" | `TSToastQueue` (`shared`, `enqueue`, `dismiss`, `dismissCurrent`, `performAction`, `cancelAll`), `TSToastItem`, `TSToastAction`, `TSToastTimeout`; Android `:core`: `TSToastQueue`, `TSToastMessage`, `TSToastEntry`, `TSToastTimeouts` (T16), `TSToastPresenter` + `TSToastScheduler` (timer, show-once) | `.tsToastHost(_ queue: = .shared, bottomInset:)` | `TSToastWindow(windowScene:queue:)`, `bottomInset`, `applyTraits(from:)` | `TSToastHost(state, modifier, bottomInset) { content }`, `rememberTSToastState(queue, scheduler)`, `TSToastState.show(…)`, `LocalTSToastState` | `TSToastHost.install(activity, queue, scheduler)`, `TSToastHost.from(context)`, `show(…)`, `anchorView`, `bottomInset`, `attachOverlay` / `detachOverlay` |
| position `bottom-centre`, `z-toast` | `outerMargin` / `outerMarginDp` (16), `TSToastWindowing.windowLevel` | overlay window above the scene | the window itself | a layer over the host content, and one inside each `TSDialog` window | a layer in `android.R.id.content`, and one in each TS dialog's window |

The bare views do not time out or position themselves: they are the DS `Toast`. A host shows the queue's `current` item, so only one toast is visible at a time, in FIFO order.

## Timing (`TSToastTimeout`, from T12; Android `TSToastTimeouts`, from T16)

A toast auto-dismisses after 4 s, or after 10 s when it has an action, on both platforms. iOS: `TSToastQueue.timeout(for:)` returns `duration ?? TSToastTimeout.standard` (4 s) for a plain toast and `max(duration ?? 4, TSToastTimeout.extended)` (at least 10 s) for an action toast. Android: `TSToastTimeouts.default` returns `DEFAULT_MILLIS` (4000 ms), or `EXTENDED_MILLIS` (10 000 ms) when the message has an action. No system accessibility setting changes the timeout.

`TSToastPresenter` runs the timer through an injected `TSToastScheduler` (`TSToastScheduler.Main` posts to the main `Handler`), cancels it on every head change, and dismisses by id. On Android the action calls `TSToastQueue.performAction`, which runs the handler first and then dismisses.

The action dismisses the toast and then runs its handler; the next queued toast is shown at once. A late timer for a toast that is already gone cannot dismiss the next one.

## Hosts

| | SwiftUI `.tsToastHost()` | UIKit `TSToastWindow` |
|---|---|---|
| Container | a hidden anchor view finds its window scene and opens an internal `TSToastOverlayWindow` hosting `TSToastHostView` | a public pass-through `UIWindow` whose root is the host controller |
| Level | `UIWindow.Level.alert + 1`, never key | same |
| Above sheets and dialogs | yes: it is a separate window, so it covers anything presented in the app window | same |
| Pass-through | `hitTest` returns `nil` outside the toast frame reported by SwiftUI | `hitTest` returns `nil` unless the hit view is inside the toast |
| Placement | bottom-centre, 16 from the edges and above the safe area (plus `bottomInset`, e.g. a BottomNav); up to 420 wide | same, pinned to `keyboardLayoutGuide` |
| Traits | forwards colour scheme, dynamic type size, layout direction and locale from the host's environment | `applyTraits(from:)` forwards style, content size category and layout direction (consumer overrides) |
| Visibility | shown when a toast is current; hidden after the exit animation (`durationBase + durationFast`) | same |
| Status bar | forwards the app window's status bar style and visibility | same |

Install one host per scene: SwiftUI at the root view, UIKit in the scene delegate.

### Android hosts

| | Compose `TSToastHost(state) { content }` | Views `TSToastHost` |
|---|---|---|
| Install | once, around the app content (it provides `LocalTSToastState`) | `TSToastHost.install(activity)` once, after `setContentView` |
| Container | a pass-through layer drawn over `content` | a pass-through `FrameLayout` added to `android.R.id.content` |
| Over sheets and dialogs | `TSDialog` (center and sheet) adds its own layer inside its window when a `LocalTSToastState` is present; the top-most layer shows the toast | `TSDialogFragment` / `TSBottomSheetDialogFragment` call `attachOverlay(window.decorView)` in `onStart` and `detachOverlay` in `onStop`; the top-most layer shows the toast |
| Why | a `Dialog` / `ModalBottomSheet` is its own window; an app-level overlay cannot draw above it | same |
| Pass-through | only the toast takes touches; outside it, touches reach the content or the sheet | the layer is not clickable; the toast is |
| Placement | bottom-centre, 16 from the edges, inside `WindowInsets.safeDrawing` (system bars, cutout, IME), plus `bottomInset` | same with system bar, cutout and IME insets; with `anchorView` (e.g. a BottomNav) it sits 16 above the anchor instead of the bottom inset |
| Motion | fade + slide up (`durationBase`), fade out (`durationFast`) | same |
| Haptics | success → `Confirm`, warning / danger → `Reject` (`TSToastStyleSpec.hapticEvent`), through `rememberTSHaptics()` (honours `TSTheme(hapticsEnabled)`) | same, through `View.performHapticFeedback` |

### Host lifecycle (Android)

- **One host per Activity.** `TSToastHost.install(activity)` returns the host already attached to that Activity's `android.R.id.content` instead of adding a second one. Compose installs one `TSToastHost { }` at the root of the content.
- **Compose** starts the presenter when `TSToastHost` enters composition and stops it in `onDispose`. `rememberTSToastState()` creates its queue per composition; pass your own `queue` to keep toasts across a recreate.
- **Views stays manual (C-43):** the host never detaches itself. An app that keeps its `TSToastQueue` across a recreate (for example in a `ViewModel`, passed to `install(activity, queue)`) **must call `detach()` in `onDestroy`**, or the old host keeps listening to the queue. `detach()` stops the presenter, cancels its timer and removes the layer. A pending timeout holds the queue only weakly, so it never retains the Activity.
- **On any recreate, rotation included,** the new host shows the queue's current toast again: its full timeout restarts (the timer belongs to the presenter, not the queue).

## Matrix

`TSToastStyleSpec.allConfigurations`: 5 tones × action = 10.

| Tone | Icon [est] | Icon colour |
|---|---|---|
| neutral | none | — |
| info | `info` | `info-on-inverse` |
| success | `check-circle` | `success-on-inverse` |
| warning | `alert` | `warning-on-inverse` |
| danger | `x-circle` | `danger-on-inverse` |

Geometry: `surface-inverse`, padding 12 / 16, `radius-lg`, `shadow-lg`, gap 12, items centred, max width 420. Icon `icon-md` 20. Message `body-sm` in `text-inverse`, wraps. Action: `body-sm` at weight 600, underlined, `text-inverse`, padding 4 / 8 with the web's negative margins (it does not grow the toast; the trailing padding is 8), 44 pt hit area, opacity press feedback. Height at the default size: 44 for one line, with or without an action.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSToastStyleSpecTests.swift` | 10 configurations → icon, on-inverse colour, action flag; tokens; on-inverse = opposite theme; `TSToastShowEffects` plays the tone haptic once |
| Queue | `ios/Tests/TSFoundationTests/TSToastQueueTests.swift` (T12) | FIFO; 4 s default, 10 s with an action (`actionExtends`), explicit durations honoured; cancel; stale timer; action; tone haptics |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSToastBehaviorTests.swift` | shown message and tone icon; action button fires; no action without a handler; 44 pt height and 420 width; **host shows queued toasts in order**; **auto-dismiss timing through an injected scheduler**; **host action runs the handler and shows the next toast**; overlay window level, visibility and hide after the exit animation; overlay avoids the keyboard; **overlay passes touches through outside the toast** |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSToastViewBehaviorTests.swift`, `TSToastWindowBehaviorTests.swift` | the toast and its message are not accessibility elements (message and action text stay reachable), fill and icon per tone; underlined action fires; 44 pt action hit area; geometry; icon ignores modal tint dimming; **window shows queued toasts in order**; **timing**; **action**; **touches pass through outside the toast**, the toast and its action receive theirs; `bottomInset`; `applyTraits` |
| Demo | `FeedbackComponentsTests` (both iOS demos): `testQueuedToastsShowOneAtATimeInOrder`, `testToastUndoActionFiresAndDismisses`, `testToastAppearsAboveAnOpenSheetAndPassesTouchesThrough` | |
| Spec (Android) | `android/core/src/test/.../style/TSToastStyleSpecTest.kt` | 10 configurations → icon, on-inverse colour, haptic, action flag; tokens; on-inverse = opposite theme; state does not change it |
| Presenter (Android) | `android/core/src/test/.../logic/TSToastPresenterTest.kt`, `TSToastQueueTest.kt`, `TSToastHostingRobolectricTest.kt` | FIFO; 4 s default and 10 s with an action (`defaultTimeoutIsFourSecondsAndTenWithAnAction`) through a manual scheduler; stale timer; action then next; show once across restarts; stop |
| Compose | `android/compose/src/test/.../toast/TSToastTest.kt` (sdk 26 and 36) | shown message and tone icon; action button fires; no action without a handler; 44 dp height, 48 dp action touch height, 420 dp max width; **host shows queued toasts in order**; **timing through an injected scheduler**; **host action runs the handler and shows the next toast**; `LocalTSToastState` and content stays interactive; `bottomInset`; **toast inside an open sheet and centre dialog** (one visible toast, action fires) |
| Views | `android/views/src/test/.../toast/TSToastViewTest.kt`, `TSToastHostTest.kt` (sdk 26 and 36) | icon for every tone except neutral; underlined action button fires; no action without a handler; 44 px at mdpi, ≥ 48 touch delegate, 420 max width; XML attributes; one host per activity; **order**; **timing**; **action**; pass-through; anchor; system-window bottom inset; **toast inside an open sheet and centre dialog, and back on the activity after close** |
| Demo (Android) | `FeedbackComponentsTest` (both Android demos): `queuedToastsShowOneAtATimeInOrder`, `toastUndoActionFiresAndDismisses`, `toastIsShownInsideAnOpenSheetAndPassesTouchesThrough` | |

## Platform notes

- iOS has no native toast. Both hosts use an overlay window so the toast sits above sheets (`z-toast`); a toast hosted inside the app window could not cover a presented sheet.
- The overlay window is never key, so it does not take the keyboard or first responder.
- Android: the bare views are `TSToast` (Compose) and `TSToastView` (Views); they never time out or position themselves. The name avoids `android.widget.Toast`; neither toolkit uses the system `Toast` or MDC `Snackbar`.
- Android: the host lives in the app window and moves the toast into the top-most TS dialog window while one is open (see *Android hosts*). Dialogs that are not TS dialogs (a plain `Dialog`, MDC `BottomSheetDialog`, the demo settings sheet) do not get a layer; Views consumers can call `attachOverlay(window.decorView)` themselves.
- Android: the action has no vertical padding and only the 8 dp start padding; that matches the web's negative margins (text 16 from the trailing edge, 20 from the message) without growing the toast. Compose expands its touch target to 48 dp automatically; Views adds a 48 dp touch delegate.
- Android: while a toast is visible it covers the bottom of an open sheet (for example its stacked Close button), as on iOS.
- Android: the action text uses `withDsFontWeight(600)` (Compose) and `sans-serif-medium` (Views), so it stays semibold below API 28.
- Tone haptics: success/warning/error notification feedback on show, through `TSHaptics.shared` (opt-out). Neutral and info play none.
