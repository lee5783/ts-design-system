# Card

Cross-toolkit contract for the TS Card. Source: `design/source/components/Card/README.md`, `bundle.css` (`.ts-card*`), `index.d.ts` (`CardProps`). Tokens: `surface-card`, `surface-pressed`, `border-subtle`, `radius-lg`, `shadow-sm`, `title-3`, `body-sm`, `text-primary/secondary/muted`, `space-2/3/4`, `opacity-disabled`, `card-subtitle-spacing` (`design/extensions/native-tokens.json`). Decisions: C-12 (pressed fill `surface-pressed`), C-27 (i) (an interactive card may have footer actions; they become accessibility custom actions).

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `Card` | `TSCardStyleSpec` | `TSCard<Accessory, Content>` | `TSCardView: UIControl` | `TSCard(modifier, title, subtitle, …, accessory, content)`; `TSCard(text, …)` | `TSCardView : LinearLayout` |
| `title` / `subtitle` | `titleFont`, `subtitleFont` | `title:`, `subtitle:` | `title`, `subtitle` | `title`, `subtitle` | `title`, `subtitle` (`app:tsTitle`, `app:tsSubtitle`) |
| `children` (body) | `bodyFont`, `bodyColor` | `@ViewBuilder content` or `text:` | `bodyText`, `bodyView` | `content` slot or `text`; `TSCardBodyText` | `bodyText` (`app:tsBodyText`), `bodyView` |
| `actions` (top-right Badge or IconButton) | `headerSpacing`; `TSCardAccessory` (`TSFoundation/Logic`, `:core` `logic`) | `@ViewBuilder accessory`, or `accessory: TSCardAccessory` | `accessoryView`, or `accessory: TSCardAccessory?` | `accessory` slot, or `accessory: TSCardAccessory` | `accessoryView`, or `accessory: TSCardAccessory?` |
| `footer` (right-aligned buttons) | `footer*` | `footerActions: [TSCardAction]` (rendered as `TSButton`s) | `footerActions` (rendered as `TSButtonView`s) | `footerActions: List<TSCardAction>` (`TSButton`s) | `footerActions` (`TSButtonView`s) |
| `raised` | `Configuration.isRaised` → `shadow-sm` | `isRaised:` | `isRaised` | `raised` | `isRaised` (`app:tsRaised`) |
| `interactive` + `onClick` | `Configuration.isInteractive` | `action:` (interactive when set) | `isInteractive` + `.primaryActionTriggered` | `onClick` (interactive when set) | `setOnClickListener` (`isInteractive` reads it) |
| disabled (not in the DS) | `TSControlState.disabled` | `.disabled(_:)` | `isEnabled` | `enabled` | `isEnabled` (`android:enabled`) |

`TSCardAction(label, variant: = .secondary, handler:)` is shared by both toolkits (`TSFoundation/Logic`); on Android `TSCardAction(label, variant = Secondary, onClick)` lives in `:core` `logic/TSCardAction.kt`. The footer is data, not a free slot.

### Typed accessory (`TSCardAccessory`)

`TSCardAccessory` is the typed form of the top-right action: today one case, an icon button (`.iconButton(icon:label:action:)` on iOS, `TSCardAccessory.IconButton(icon, label, onClick)` on Android). The card renders it as a `TSIconButton` / `TSIconButtonView` and keeps it independently tappable on an interactive card. A free-form accessory (`@ViewBuilder accessory`, `accessoryView`, the Compose `accessory` slot) is also independently tappable.

**UIKit and Views: last writer wins.** `TSCardView` has both `accessory` (typed) and `accessoryView` (any view). Setting `accessory` builds the icon button and stores it as `accessoryView`; setting `accessoryView` directly clears the typed `accessory`, so its custom action disappears with it. Whichever was set last is shown and exposed. When a previous `TSButtonView` / `TSIconButtonView` accessory is replaced, its container dimming flag is reset.

## Matrix

`TSCardStyleSpec.allConfigurations`: raised × interactive × default, pressed, disabled, focused = 16.

| State | Fill | Shadow | Focus ring | Opacity |
|---|---|---|---|---|
| default | `surface-card` | `shadow-sm` when raised | — | 1 |
| pressed, interactive [est, C-12] | `surface-pressed` | as default | — | 1 |
| pressed, static | `surface-card` | as default | — | 1 |
| focused, interactive | `surface-card` | as default | `focus` ring (SwiftUI, Compose) / `UIFocusHaloEffect` (UIKit) / ring drawn inside the shape (Views) | 1 |
| disabled [est] | `surface-card` | as default | — | 0.45 |

The web hover lift (`shadow-md`) has no touch equivalent and is dropped.

Geometry: 1 pt `border-subtle`, `radius-lg` (12, continuous), content clipped. Header padding 16 / 16 / 0 (16 at the bottom when there is no body), gap 12; title `title-3` 18/24 in `text-primary` (header trait); subtitle `body-sm` 14/20 in `text-muted`, 2 below the title. Body padding 16, `body-sm` in `text-secondary`. Footer: 1 pt `border-subtle` top divider, padding 12 / 16, trailing-aligned buttons 8 apart; at accessibility text sizes the buttons stack vertically, still trailing. Cards stack 16 apart on mobile (`TSCardStyleSpec.stackSpacing`). Heights at the default text size: 56 title only, 92 title + one-line body, +69 with a footer.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSCardStyleSpecTests.swift` | 16 configurations → fill (pressed only when interactive), ring, opacity, shadow, tokens; stack spacing; label and custom-action order |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSCardBehaviorTests.swift` | interactive card is one button element with the label; action fires (tap + activation); custom actions fire; footer buttons fire; static card is not a button and its title is a header; disabled blocks and drops custom actions; pressed treatment; heights; AX5 footer reflow |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSCardViewBehaviorTests.swift` | one element + hit testing; `UIAccessibilityCustomAction`s fire; `.primaryActionTriggered` and activation; footer buttons stay hittable; static card; pressed fill; disabled; raised shadow + clipping surface; heights; accessory; AX5 footer reflow |
| Spec (Android) | `android/core/src/test/kotlin/com/ts/ds/core/style/TSCardStyleSpecTest.kt` | 4 configurations × 4 states → fill, ring, opacity, shadow, tokens; stack spacing; footer stacking threshold; label and custom-action order (dropped while disabled) |
| Compose | `android/compose/src/test/kotlin/com/ts/ds/compose/components/card/TSCardTest.kt` (sdk 26 + 36) | one button node with the label and no child semantics; tap + `OnClick` fire; `CustomActions` order and handlers; footer buttons and accessory tappable without firing the card; static card is a container with a heading title; disabled blocks and drops custom actions; pressed fill only when interactive; heights 56 / 92 / 165; footer stacks at fontScale 2 |
| Views | `android/views/src/test/kotlin/com/ts/ds/views/components/card/TSCardViewTest.kt` (sdk 26 + 36) | Button node with the label, content hidden from accessibility; tap + `ACTION_CLICK`; custom actions fire; footer buttons and accessory tappable; static card (heading title, footer buttons accessible); disabled; pressed fill; raised elevation + `clipToOutline`; heights; footer stacking |
| Demo | `ContentComponentsTests` (both iOS demos): `testInteractiveCardIsOneElementAndPresses`, `testCardFooterButtonsStayTappable` | |
| Demo (Android) | `ContentComponentsTest` (both Android demos): `interactiveCardIsOneElementAndPresses`, `cardFooterButtonsStayTappable` | |

## Platform notes

- UIKit: the card's own layer carries the shadow; an inner `surfaceView` clips and draws fill and border.
- Android (T38):
  - Footer buttons and the accessory keep their own clicks (child-first dispatch in both toolkits).
  - Footer buttons keep the 48 dp touch target, so the footer adds 73 dp on Android (69 pt on iOS). Heights: 56 / 92 / 165.
  - Footer stacks vertically, still trailing, from `TSCardStyleSpec.STACKED_FOOTER_FONT_SCALE` = 1.5 (the ListItem threshold, [est]).
  - Views draws fill, border and focus ring with the shared `TSControlBackground`, sets `clipToOutline` and uses `elevation` for `shadow-sm`. The card fills its bounds, so its focus ring is drawn on the shape edge with an inner gap stroke (the full-bleed case of `TSControlBackground`; see `button.md`).
  - Compose card text uses `TextDirection.Content` aligned to the layout start, so English text in RTL keeps its punctuation order but aligns right.
- Touch: the whole card is the tap target; footer buttons and the accessory stay independently tappable (SwiftUI: the card button is a background behind the content, text ignores hits; UIKit: `hitTest` returns the card unless the touch is inside a footer button or the accessory).
- Disabled cards disable their footer buttons and a `UIControl` accessory.
