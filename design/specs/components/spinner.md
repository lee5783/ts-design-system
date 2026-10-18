# Spinner

Cross-toolkit contract for the TS Spinner, the indeterminate loading indicator for stand-alone loading states (issue #6). The DS has no Spinner component; the drawing is the one the Button uses while loading (`design/source/components/Button/README.md`, `bundle.css` `.ts-btn-spinner`). Tokens: `icon-sm/md/lg`, `spinner-size`, `spinner-stroke-width`, `spinner-rotation-duration` (`design/extensions/native-tokens.json`), `text-primary`, `text-secondary`, `accent-primary`. Decisions: C-025 (sizes and tones), C-028 (own spec and demo page).

## API mapping

| Concept | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose (`…compose.components.spinner`) | Views (`…views.components.spinner`) |
|---|---|---|---|---|---|
| Spinner | `TSSpinnerStyleSpec` | `TSSpinner` | `TSSpinnerView: UIView` | `TSSpinner(modifier, size, tone, contentDescription)` | `TSSpinnerView : View` |
| size | `TSSpinnerSize` `sm · md · lg` (Android `Sm · Md · Lg`) | `size:` (default md) | `size` | `size =` (default `Md`) | `size` / `app:tsSpinnerSize="sm\|md\|lg"` |
| tint | `TSSpinnerTone` `primary · secondary · accent` (Android `Primary · Secondary · Accent`) | `tone:` (default primary) | `tone` | `tone =` (default `Primary`) | `tone` / `app:tsSpinnerTone="primary\|secondary\|accent"` |
| accessibility label | `TSStrings.loading` / `ts_loading` | `label:` (default "Loading", `nil` hides it) | `accessibilityLabel` (default "Loading"; `isAccessibilityElement = false` hides it) | `contentDescription` (default "Loading", `null` hides it) | `contentDescription` (default "Loading"; `importantForAccessibility="no"` hides it) |
| free colour, size or stroke | not offered | | | | |

The Button keeps drawing its loading spinner through the same internal drawing (Compose `components.shared.TSSpinner`, SwiftUI `TSSpinner(size:lineWidth:color:)`, UIKit `TSSpinnerView.lineWidth`, Views `TSSpinnerDrawable`); its size and stroke still come from `TSButtonStyleSpec`.

## Matrix

`TSSpinnerStyleSpec.allConfigurations` / `configurations`: 3 sizes × 3 tones = 9.

| Size | Diameter | Stroke [est] |
|---|---|---|
| sm | 16 (`icon-sm`) | 2 |
| md | 20 (`icon-md`) | 2.5 |
| lg | 24 (`icon-lg`) | 3 |

| Tone | Colour |
|---|---|
| primary | `text-primary` |
| secondary | `text-secondary` |
| accent | `accent-primary` |

- **Stroke** keeps the Button spinner's ratio: `spinner-stroke-width / spinner-size` (2 / 16) times the diameter. `sm` is pixel-identical to the Button's loading spinner. The md and lg strokes are estimates until the DS defines spinner sizes.
- **Arc:** 270° open arc starting at 45°, butt caps, drawn inside the frame (inset by half the stroke).
- **Motion:** one linear turn every `spinner-rotation-duration` (700 ms), repeating. Reduce motion does not stop it, as for the Button spinner: an indeterminate indicator is the only signal that work is in progress.
- No pressed, focused or disabled state; the spinner is not interactive.
- Snapshot and Robolectric tests draw a still frame at 0° (Compose `LocalTSForcedControlState`, SwiftUI and UIKit render the first frame); never let the infinite rotation run in a Robolectric test.

## Accessibility

- One element, label "Loading" (`ts.loading` / `ts_loading`, overridable through the app's `TSDesignSystem` table / string resource). Pass your own label for a specific task ("Loading messages").
- Compose: `contentDescription` plus `ProgressBarRangeInfo.Indeterminate`. Views: `ProgressBar` class name for TalkBack plus the content description. iOS: `.updatesFrequently` trait.
- When visible text next to the spinner already says it is loading, hide the spinner (`label: nil`, `contentDescription = null`, `isAccessibilityElement = false`, `importantForAccessibility="no"`).

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSSpinnerStyleSpecTests.swift` | 9 configurations; diameter from `icon-*`; stroke ratio (sm equals the Button spinner); tone → colour; rotation duration |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSSpinnerBehaviorTests.swift` | frame per size; default and custom label; hidden with `nil` |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSSpinnerViewBehaviorTests.swift` | intrinsic size per size; stroke and colour per tone; label; rotation only in a window |
| Snapshots | `TSSpinnerSnapshotTests`, `TSSpinnerViewSnapshotTests` (`spinner.gallery-*`) | sizes × tones, standard matrix |
| Spec (Android) | `android/core/src/test/.../style/TSSpinnerStyleSpecTest.kt` | same as iOS |
| Compose | `android/compose/src/test/.../spinner/TSSpinnerTest.kt` (sdk 26/36) | size per token; content description and indeterminate progress; hidden with `null` |
| Views | `android/views/src/test/.../spinner/TSSpinnerViewTest.kt` (sdk 26/36) | measured size; XML attrs; content description and class name |
| Snapshots (Android) | `TSSpinnerSnapshotTest`, `TSSpinnerViewSnapshotTest` (`gallery_*`) | sizes × tones, standard matrix |
| Demo | Spinner page in all four demos; `FeedbackComponentsTests.testSpinnerPageShowsSpinnersLabelledLoading` (both iOS demos), `FeedbackComponentsTest.spinnerPageShowsSpinnersLabelledLoading` (both Android demos) | page shows every size × tone; spinners are labelled "Loading" |

## Platform notes

- SwiftUI: the arc is a trimmed `Circle` rotated with `.rotationEffect`; UIKit a `CAShapeLayer` with a `transform.rotation.z` animation that runs only while the view is in a window and not hidden.
- Compose: an infinite transition that rotates a `Canvas` arc; it stops under `LocalTSForcedControlState` (test hook).
- Views: `TSSpinnerView` draws `TSSpinnerDrawable` (an `Animatable` drawable) and starts it while attached and visible.
- UIKit: `TSSpinnerView()` and `TSSpinnerView(size:tone:)` resolve the style spec. `TSSpinnerView(frame:)` is the unstyled path the Button uses internally (no spec, hidden from accessibility); do not use it for a stand-alone spinner.
- Views: the "spins right after attach" path is not asserted under Robolectric (the test window reports `GONE`); hide / show is.
- Use the spinner instead of Material `CircularProgressIndicator` / `ProgressBar` or a plain `ProgressView` / `UIActivityIndicatorView` inside TS screens, so the stroke, size and tint match the Button's loading state.
