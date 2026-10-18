# Button

Cross-toolkit contract for the TS Button. Source: `design/source/components/Button/README.md`, `bundle.css` (`.ts-btn*`, `.ts-spinner`), `index.d.ts` (`ButtonProps`).

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI (`TSDesignSystem`) | UIKit (`TSDesignSystemUIKit`) | Compose (`com.tinysol.designsystem.compose.components.button`) | Views (`com.tinysol.designsystem.views.components.button`) |
|---|---|---|---|---|---|
| `Button` | `TSButtonStyleSpec` | `TSButton` (internal `TSButtonStyle: ButtonStyle`) | `TSButtonView: UIButton` (`UIButton.Configuration`) | `TSButton(text, onClick, …)` composable | `TSButtonView: LinearLayout` (XML or code) |
| `children` | — | `_ title: String` | `title` | `text: String` | `text` / `android:text` |
| `variant` | `TSButtonVariant` `primary · secondary · ghost · danger · link` (Kotlin `Primary` …) | `variant:` | `variant` | `variant =` | `variant` / `app:tsVariant` |
| `size` | `TSButtonSize` `md · lg` (`sm` dropped, C-22) | `size:` | `size` | `size =` | `size` / `app:tsSize` |
| `iconLeft` / `iconRight` | `TSIconName` / `TSIcon` | `leadingIcon:` / `trailingIcon:` | `leadingIcon` / `trailingIcon` | `leadingIcon =` / `trailingIcon =` | `leadingIcon` / `trailingIcon`, `app:tsLeadingIcon="arrow-left"` (DS name) |
| consumer icon (native, issue #2) | — (drawn at `Resolved.iconSize`, tinted with `Resolved.label`) | `TSIconSource` `.ts(TSIconName)` · `.image(Image)`; init `TSButton(_:variant:size:leadingIconSource:trailingIconSource:isLoading:isBlock:action:)` (`leadingIconSource:` has no default) | `leadingIconImage: UIImage?` / `trailingIconImage: UIImage?` (wins over `leadingIcon` / `trailingIcon` in the same slot) | `TSIconSource` (`com.tinysol.designsystem.compose.components.icon`) `Ts(TSIcon)` · `Vector(ImageVector)` · `Painter(Painter)`; overload with `leadingIconSource =` / `trailingIconSource =` | `leadingIconDrawable` / `trailingIconDrawable: Drawable?`, `setLeadingIconResource(@DrawableRes)` / `setTrailingIconResource(@DrawableRes)` (win over `leadingIcon` / `trailingIcon`); no XML attribute |
| label line control (native, issue #2) | — (type role stays `Resolved.typeRole`) | environment `.lineLimit(_:)` / `.truncationMode(_:)` | `numberOfLines` (default 0), `lineBreakMode` (default `.byWordWrapping`) | `maxLines =` (default `Int.MAX_VALUE`), `overflow =` (default `TextOverflow.Clip`) on the `TSIconSource` overload | `maxLines`, `ellipsize` / `android:maxLines`, `android:ellipsize` |
| `loading` | `TSControlState.loading` | `isLoading:` | `isLoading` | `loading =` | `isLoading` / `app:tsLoading` |
| `block` | — | `isBlock:` (fills the proposed width) | `isBlock` (low horizontal hugging; the container sets the width) | `Modifier.fillMaxWidth()` (content stays centred) | `layout_width="match_parent"` |
| `disabled` | `TSControlState.disabled` | `.disabled(_:)` | `isEnabled` | `enabled =` | `isEnabled` / `android:enabled` |
| `onClick` | — | `action:` closure | `primaryAction: UIAction?`, target-action | `onClick` lambda (+ optional `interactionSource`) | `setOnClickListener` |
| `as`, `href`, `className` | dropped | | | | |

Icons are leading/trailing, never left/right, so they follow the layout direction. Directional icons mirror in RTL.

The existing `TSIcon` / `TSIconName` signatures are unchanged and delegate to the consumer-icon overloads, so both paths render through one code path. No overload takes a colour, a size or padding.

## Consumer icons

A consumer can pass its own icon (a product mark, a game controller, a QR glyph) instead of a `TSIcon`. TS still draws it: the spec decides how it looks, the consumer only supplies the shape.

- **Size:** `Resolved.iconSize` (`icon-md` 20), aspect-fitted into the square slot (Compose `ContentScale.Fit`, SwiftUI `resizable().scaledToFit()`, UIKit `UIImage.tsTemplateIcon(pointSize:)`, Views `FIT_CENTER`). A larger or non-square source is scaled down to the slot.
- **Tint:** one colour, the resolved label colour, in every state (pressed, disabled, loading). Compose `ColorFilter.tint` (SrcIn), SwiftUI `.renderingMode(.template)`, UIKit `.alwaysTemplate`, Views a `SRC_IN` colour filter (works for any `Drawable`). A multi-colour source therefore renders as a single-colour silhouette.
- **Spacing:** the same slot and gap (`space-2` 8) as a `TSIcon`; a custom icon in a Primary md button takes exactly the space a `TSIcon` takes.
- **Loading:** the spinner still replaces only the leading slot; a trailing consumer icon stays.

### Icon contract

Draw consumer icons like the TS set so they sit next to TS icons without standing out:

- 24 px grid (the slot scales it to 20).
- Outline only, 1.75 px stroke, round caps and joins.
- No fills.
- Single colour: TS replaces the colour with the label colour, so do not rely on a second colour for meaning.
- **RTL is the consumer's job.** A consumer source carries no TS mirroring metadata: set `ImageVector.autoMirror` (Compose, honoured by `rememberVectorPainter`; a `Painter` is drawn as given), the asset catalog "Direction" / `flipsForRightToLeftLayoutDirection` (iOS; `TSIconSource.image` cannot read it from a SwiftUI `Image`, so set it on the asset), or `android:autoMirrored="true"` on the drawable (Views).
- **Contribute upstream** an icon that more than one app needs, so it becomes a `TSIcon` with DS metadata instead of being shipped per app.

Worked example: the demo controller icon follows this contract (`demos/android-compose/app/src/main/res/drawable/demo_ic_controller.xml`, `demos/android-views/app/src/main/res/drawable/demo_ic_controller.xml`, `demos/ios-*/…/App/Assets.xcassets/DemoController.imageset/demo-controller.svg`).

## Label line control

The label wraps by default and the button grows (unchanged). A consumer can keep it on fewer lines with the toolkit's line controls (table above); the type role and colour still come from the spec. Use it for a count or a long label on a narrow screen, together with a width limit from the container.

## Matrix

Resolved once per platform in `TSButtonStyleSpec.resolve` (`TSFoundation/Styles`, `android/core/.../core/style`); both toolkits of the platform read it. Android resolves to roles (`TSColorRole`, `TSTypeRole`, `TSShadowRole`) and dp values, and each toolkit maps the roles to its own colours.

| Variant | Fill | Pressed fill | Label | Border | Shadow |
|---|---|---|---|---|---|
| primary | `accent-primary` | `accent-pressed` | `text-on-accent` | — | — |
| secondary | `surface-card` | `surface-pressed` | `text-primary` | 1 pt `border-strong` | `shadow-xs` |
| ghost | transparent | `surface-pressed` | `text-primary` | — | — |
| danger | `danger-solid` | `danger-pressed` (extension token, C-12) | `text-on-accent` | — | — |
| link | none | none (`opacity-pressed` 0.85) | `accent-text`, underlined | — | — |

| Size | Min height | Horizontal padding | Vertical padding | Radius | Type |
|---|---|---|---|---|---|
| md | `control-md` 44 | `space-4` 16 | `space-2` 8 | `radius-md` | `label` 14/20/500 |
| lg | `control-lg` 52 | `space-5` 20 | `space-2` 8 | `radius-md` | `body` 16/24 at weight 500 |
| link (any size) | none | 0 | 0 | 0 | as the size |

Icons: `icon-md` 20 (`Resolved.iconSize`, for TS icons and consumer icons alike on every toolkit), gap `space-2` 8. Spinner: 16 pt, 2 pt ring, 0.7 s per turn, drawn in the label colour inside the 20 pt leading slot so the width does not change.

| State | Rule |
|---|---|
| default | as the tables above |
| pressed | pressed fill; link drops to `opacity-pressed`; 120 ms (`duration-fast`) colour change |
| disabled | whole control at `opacity-disabled` 0.45; not interactive |
| loading | spinner replaces the leading icon (or is added before the label); **not dimmed** (keeps label contrast); not interactive |
| focused | 2 pt `focus` ring outside a 2 pt gap (`shadow-focus`), keyboard focus only. SwiftUI draws the TS ring; UIKit uses `UIFocusHaloEffect` |

States are combined by `TSControlState.make(isPressed:isEnabled:isLoading:isFocused:)` (Kotlin `TSControlState.make(pressed, enabled, loading, focused)`): loading wins over disabled, disabled over pressed.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSButtonStyleSpecTests.swift` | all 50 configurations → fill, label, border, opacity, spinner, focus; danger-pressed; loading not dimmed; sizes; link; shadow |
| Spec | `ios/Tests/TSFoundationTests/TSControlStateMakeTests.swift` | state precedence, hit-area outset, weighted font |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSButtonBehaviorTests.swift` | tap fires; disabled and loading block; title; icons hidden (decorative); consumer icon sizing; loading replaces the leading icon; line limit |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSButtonViewBehaviorTests.swift` | `.touchUpInside` fires; `isEnabled = false` and loading block; min height; link hit area ≥ 44; pressed fill; Dynamic Type growth; fixed corners; disabled icon dimmed once; leading icon ink |
| Demo | `demos/ios-*/…UITests/ActionComponentsTests.swift` | playground fires; loading and disabled block; link expanded hit area |
| Spec (Android) | `android/core/src/test/.../core/style/TSButtonStyleSpecTest.kt`, `TSControlStateMakeTest.kt` | the same 50 configurations against an independent table; precedence; raw loading + disabled reads as loading |
| Compose | `android/compose/src/test/.../components/button/TSButtonTest.kt` (sdk 26, 36) | click fires; label exposed as text; disabled and loading block; loading wins over disabled but stays blocked; 44 dp visual inside a 48 dp touch target; lg 52 dp; link touch target 48 × 48 |
| Views | `android/views/src/test/.../components/button/TSButtonViewTest.kt` (sdk 26, 36) | tap fires; disabled and loading block real touches; loading not dimmed and restores the user's enabled state when finished; spinner replaces the leading icon; 48 px target with a 44 px visual at mdpi; lg 52; link ≥ 48 and underlined; pressed fill; link pressed opacity; medium family; XML attributes |
| Demo (Android) | `demos/android-*/app/src/androidTest/.../ActionComponentsTest.kt` | playground fires; loading and disabled block; link 48 dp target |
| Consumer icons + line control (iOS) | `TSButtonBehaviorTests` `iconsUseResolvedSize`, `loadingReplacesLeadingConsumerIcon`, `lineLimit`; `TSButtonViewBehaviorTests` `consumerImage`, `loadingKeepsTrailingConsumerImage`, `lineLimit` (sample `ios/Tests/TSTestSupport/TSSampleIconImage.swift`) | TS icon vs consumer image per size; pressed / disabled / loading tint; a 48 pt image fitted to the slot; one line vs wrap |
| Consumer icons + line control (Android) | `TSButtonTest` `consumerIconsTakeTheSameSpaceAsTSIcons`, `loadingReplacesOnlyTheLeadingConsumerIcon`, `maxLinesKeepsTheLabelOnOneLineWithAnEllipsis`; `TSButtonViewTest` `consumerDrawablesFillTheIconSlotsAndAreTinted`, `spinnerReplacesOnlyTheLeadingConsumerDrawable`, `maxLinesKeepsTheLabelOnOneLineWithAnEllipsis`, `inflatesLineControlFromXml` | same as iOS, with a multi-colour sample to prove the single-colour tint |
| Demo | Button page, "Consumer icon" and "One-line label" sections in all four demos (`demo.button.consumer-icon`, `demo.button.line-limit`) | shown, not asserted by a UI test |

## Platform notes

- UIKit: in a horizontal `UIStackView` whose width is not fixed, give the buttons `.required` horizontal compression resistance; otherwise UIKit may wrap multi-line button titles to equal widths (system `UIButton` behaves the same).
- UIKit draws the border with the layer border, not `UIBackgroundConfiguration.strokeColor`, which renders without corners.
- Android touch target: the visual height stays 44 (md), but the touch target is at least 48 × 48 dp. Compose puts `clickable` outside `tsMinimumTouchTarget`, so the layout box is 48 dp and the chrome is centred in it. Views measures the view at ≥ 48 dp and `TSControlBackground` draws the 44 dp shape centred in it. Links get the same 48 dp box around the text.
- Android pressed colours change immediately (no 120 ms cross-fade), and so does the link opacity.
- Android weight: labels are 500. Views uses the `sans-serif-medium` family on every level. Compose uses `withDsFontWeight`, which swaps to `sans-serif-medium` below API 28, because Compose renders `FontWeight.Medium` on the default family as Regular there.
- Android focus ring: Compose draws the TS ring outside a 2 dp gap, keyboard focus only (`tsFocusRing`). Views (`TSControlBackground`) draws the ring outside a 2 dp gap when the view's bounds leave room around the visual shape (the 48 dp touch box around a 44 dp button, for example); when the shape fills the bounds (full-bleed), it draws a 2 dp `focus` stroke on the shape edge plus a 2 dp `background-primary` gap stroke inside it, so the ring stays visible on primary, where `focus` equals the fill. The parent clips children, so a ring can never extend past the view's own bounds.
- Android shadow (secondary): Compose uses `Modifier.shadow` with the token colour (the platform ignores the tint below API 28). Views uses `elevation` with the default shadow colour on every level.
- Views XML: `<com.tinysol.designsystem.views.components.button.TSButtonView android:text="…" app:tsVariant="secondary" app:tsSize="lg" app:tsLeadingIcon="download" />`. Icon attributes take the DS kebab name (`TSIcons.fromDsName`).
- **SwiftUI consumer-icon init:** `leadingIconSource:` is a required argument (pass `nil` for a trailing-only icon). With a default it would make `TSButton("Save") { }` ambiguous between the two inits.
- **Compose overload choice:** `TSButton(text, onClick)` and every existing call resolve to the `TSIcon` overload; naming `leadingIconSource` / `trailingIconSource` / `maxLines` / `overflow` selects the new one. Its JVM name is mangled (`TSButton-pDkBHBI`) because `TextOverflow` is a value class; Kotlin callers are unaffected.
- **One-line label with a trailing icon (Compose, Views):** in a width-bounded button the label is measured first, so a one-line label can squeeze the trailing icon. UIKit reserves the trailing slot and is not affected. Give the button enough width or drop the trailing icon when you limit lines.
- **Views:** consumer drawables are Kotlin properties / `set…Resource` only; there is no `app:` attribute for them. `android:maxLines` and `android:ellipsize` work in XML.
- Test hook: Compose components read an internal `LocalTSForcedControlState` (tests only) to draw pressed, focused and loading without input. The spinner is static under it, because an infinite animation never lets the Robolectric looper go idle.
- Height is a minimum: the label wraps and the button grows with Dynamic Type (up to AX5).
- Link: the visual height is the text, but the hit area is at least 44 × 44 (SwiftUI `contentShape` expanded by `TSHitArea.outset`; UIKit `point(inside:with:)` via `TSHitArea.expandedRect`).
- No haptics (C-13 reserves them for Switch, Checkbox, segments and Toast).
