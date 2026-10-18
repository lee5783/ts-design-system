# Alert

Cross-toolkit contract for the TS Alert. Source: `design/source/components/Alert/README.md`, `bundle.css` (`.ts-alert*`), `index.d.ts` (`AlertProps`). Tokens: `info/success/warning/danger`, their `*-subtle` fills, `text-primary/secondary`, `body-sm`, `radius-lg`, `space-3/4`, `icon-md`, `alert-title-font-weight` (600, `design/extensions/native-tokens.json`). Decision: C-27 (h) optional trailing close button.

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `Alert` | `TSAlertStyleSpec` | `TSAlert<Action>` | `TSAlertView: UIView` | `TSAlert(message, modifier, …, action)` | `TSAlertView : LinearLayout` |
| `tone` | `TSAlertTone` `info · success · warning · danger` | `tone:` (default info) | `tone` | `tone` (default `Info`) | `tone` (`app:tsAlertTone`) |
| `title` | `Configuration.hasTitle` | `title:` | `title` | `title` | `title` (`app:tsTitle`) |
| `children` (one sentence) | `messageFont` | `message:` | `message` | `message` | `message` (`app:tsMessage`) |
| `icon` (`IconName`) | `TSAlertStyleSpec.icon(_:)` default | `icon:` | `icon` | `icon` | `icon` (`app:tsIcon`) |
| `action` (small ghost Button / IconButton) | — | `@ViewBuilder action` | `actionView` | `action` slot | `actionView` |
| "dismissed" (README only) | `Configuration.isDismissible` | `onDismiss:` | `onDismiss` | `onDismiss` | `onDismiss` |

The alert does not own its visibility: `onDismiss` tells the owner, which removes or hides it.

## Matrix

`TSAlertStyleSpec.allConfigurations`: 4 tones × title × dismissible = 16.

| Tone | Fill | Icon [est] | Icon colour |
|---|---|---|---|
| info | `info-subtle` | `info` | `info` |
| success | `success-subtle` | `check-circle` | `success` |
| warning | `warning-subtle` | `alert` | `warning` |
| danger | `danger-subtle` | `x-circle` | `danger` |

Geometry: padding 12 / 16 plus the web's 1 px transparent border (content inset 13 / 17), `radius-lg`, gap 12, top-aligned. Icon `icon-md` 20. Title `body-sm` at weight 600 in `text-primary`; message `body-sm` in `text-secondary`; both wrap. Action slot, then the dismiss button, at the trailing edge. Dismiss: `close` at `icon-md` in `text-secondary`, label "Dismiss" (`ts.dismiss`), 44 × 44 hit area around the 20 pt glyph (so it does not grow the alert), opacity press feedback. Heights at the default size: 46 one line, 66 title + message.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSAlertStyleSpecTests.swift` | 16 configurations → fill, icon, icon colour, title/dismiss flags, tokens; label/announcement text per tone |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSAlertBehaviorTests.swift` | one message element per tone; icon override; **dismiss callback fires** (button + escape); no dismiss without `onDismiss`; action stays separate; **announcement posted on appear** only when asked; heights |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSAlertViewBehaviorTests.swift` | message element + fill + icon per tone; **dismiss fires** (touch, activation, escape); hidden without `onDismiss`; 44 pt dismiss hit area; action view order; **announcement posted** when added to a window and when unhidden, never for a static alert; heights |
| Spec (Android) | `android/core/src/test/kotlin/com/ts/ds/core/style/TSAlertStyleSpecTest.kt`, `logic/TSStringsTest.kt` | 16 configurations → fill, icon, icon colour, flags, insets 17/13, title weight 600, dismiss tokens; state-independent; tone names and label |
| Compose | `android/compose/src/test/kotlin/com/ts/ds/compose/components/alert/TSAlertTest.kt` (sdk 26 + 36) | one message node per tone; **dismiss fires** (button + `Dismiss` semantics action); none without `onDismiss`; dismiss hit area beyond the 20 dp glyph; action separate; **`liveRegion = Polite` only when `announcesWhenShown`**; heights 46 / 66; custom icon |
| Views | `android/views/src/test/kotlin/com/ts/ds/views/components/alert/TSAlertViewTest.kt` (sdk 26 + 36) | message node per tone; **dismiss fires** (touch + `ACTION_DISMISS`); hidden without `onDismiss`; touch delegate around the glyph; action before dismiss; **live region**; heights |
| Demo | `FeedbackComponentsTests` (both iOS demos): `testAlertDismissRemovesItAndShowAgainBringsItBack`, `testAlertMessageIsOneElementWithSeparateAction` | |
| Demo (Android) | `FeedbackComponentsTest` (both Android demos): `alertMessageIsOneElementWithSeparateAction`, `alertDismissRemovesItAndShowAgainBringsItBack` | |

## Platform notes

- Android (T38):
  - Dismiss is a 20 dp glyph; Compose relies on the automatic 48 dp pointer hit expansion, Views uses `ensureMinTouchTarget` (a touch delegate on the alert). Neither grows the 46 dp alert.
  - Title weight 600: Compose `withDsFontWeight`; Views `TextAppearance.TS.Component.AlertTitle` (medium family below API 28, `textFontWeight` 600 from 28).
- Status never depends on colour: each tone has its own icon.
