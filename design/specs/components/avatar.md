# Avatar

Cross-toolkit contract for the TS Avatar. Source: `design/source/components/Avatar/README.md`, `bundle.css` (`.ts-avatar*`), `index.d.ts` (`AvatarProps`).

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose (`…compose.components.avatar`) | Views (`…views.components.avatar`) |
|---|---|---|---|---|---|
| `Avatar` | `TSAvatarStyleSpec` | `TSAvatar` | `TSAvatarView: UIView` | `TSAvatar(name, …)` | `TSAvatarView: FrameLayout` |
| `name` (required) | `TSInitials.initials(for:)` / `TSInitials.from(name)` | `name:` | `name` | `name` | `name` / `app:tsName` |
| `src` (URL) | `TSAvatarContent.image` / `TSAvatarContent.Image` | an image slot: `image: UIImage?`, or `phase: AsyncImagePhase` | `image: UIImage?` | `image: Painter?` | `image: Drawable?` / `android:src` |
| `size` | `TSAvatarSize` `xs · sm · md · lg` (Android: `Xs … Lg`) | `size:` (default md) | `size` | `size =` (default `Md`) | `size` / `app:tsAvatarSize` |

There is no image loader in the library. Consumers load the image themselves and hand it over.

In SwiftUI the usual pattern is:

```swift
AsyncImage(url: url) { phase in
    TSAvatar(name: name, phase: phase)
}
```

## Matrix

| Size | Diameter | Initials | Placeholder icon |
|---|---|---|---|
| xs | 24 | 10 (`avatar-xs-font-size`) | 16 (`icon-sm`) |
| sm | 32 | 12 | 16 |
| md | 40 | 14 | 20 (`icon-md`) |
| lg | 56 | 20 | 24 (`icon-lg`) |

Content (`TSAvatarContent`), resolved by `TSAvatarContent.make(initials:hasImage:)`:
- **image:** a usable image fills the circle (aspect-fill, clipped).
- **initials:** the image is nil or failed. The initials are drawn on `accent-subtle` in `accent-text` at weight 600.
- **placeholder:** there is no image and the name has no initials (blank). It shows the `user` icon in `accent-text`.

4 sizes × 3 contents = 12 configurations. The fill is always `accent-subtle`, so it shows behind a transparent image too.

## Rules

- **Initials** (`TSInitials`, analysis open question):
  - the first letter of the first and the last word, at most 2, uppercased for the locale
  - whole grapheme clusters, so emoji and CJK are kept
  - a single word gives one letter
- **Failure:**
  - `TSAvatarImage.usable(_:)` rejects nil, zero-size and pixel-less images, for example an image whose data failed to decode. The avatar then shows the initials.
  - The `AsyncImagePhase` initializer shows the initials for `.empty` and `.failure`.
  - Android (`com.tinysol.designsystem.core.logic.TSAvatarImage`): `usable(Drawable?)` rejects null, a `BitmapDrawable` whose bitmap is null (failed decode) or recycled, and a drawable with a zero intrinsic size. A drawable with no intrinsic size (-1, such as a `ColorDrawable`) is usable. Compose uses `usableSize(width, height)` on `Painter.intrinsicSize`: an unspecified size is usable, a zero width or height is not.
- **Clamping:**
  - The circle is fixed chrome and does not scale with Dynamic Type.
  - Initials use a fixed system font at the size above, on one line.
  - Wide pairs shrink with `minimumScaleFactor` 0.5 (SwiftUI) or `adjustsFontSizeToFitWidth` (UIKit), inside a 2 pt inset, so they never leave the circle.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSAvatarStyleSpecTests.swift` | 12 configurations → diameter, radius, font size/weight, colours, the visible content (exactly one); content fallback order; `TSAvatarImage.usable` rejects nil, empty and undecodable images |
| SwiftUI | `…/Behavior/TSAvatarBehaviorTests.swift` | initials when there is no image, at a fixed size with line limit and scale factor; usable image replaces the initials; undecodable image, `.failure` and `.empty` fall back to initials; blank name → placeholder |
| UIKit | `…/Behavior/TSAvatarViewBehaviorTests.swift` | fixed circle per size at AX5; initials; image and clearing it; failed image → initials; blank name → placeholder |
| Demo | `ContentComponentsTests.testAvatarIsStaticAndSurvivesAPhoto` (both apps) | |
| Spec (Android) | `android/core/src/test/.../core/style/TSAvatarStyleSpecTest.kt`, `.../core/logic/TSAvatarImageTest.kt` | the same 12 configurations; content fallback order; `TSAvatarImage` rejects null, undecodable, recycled and zero-size images (sdk 26/36, native graphics) |
| Compose | `.../components/avatar/TSAvatarTest.kt` | initials without an image; a usable `Painter` replaces them; a failed or zero-size `Painter` falls back; blank name → placeholder; fixed circle at 200 %; `Painter` usability rule |
| Views | `.../components/avatar/TSAvatarViewTest.kt` | initials with uniform auto-size 7–14 px at md; image and clearing it; undecodable bitmap → initials; blank name → placeholder; fixed circle per size; circle and initials do not scale at 200 %; XML attributes |
| Demo (Android) | `demos/android-*/app/src/androidTest/.../ContentComponentsTest.kt` | `avatarIsStaticAndSurvivesAPhoto`, before and after the photo toggle |

## Platform notes

- **Android, no loader:** pass a `Painter` (for example from `painterResource`, a `BitmapPainter`, or a loader's painter once it has succeeded) or a `Drawable`. While a loader is still loading or has failed, pass `null` and the initials show.
- **Compose:**
  - Initials use a fixed size: the dp value is converted with `Dp.toSp()`, so the system font scale does not change it. `BasicText(autoSize = TextAutoSize.StepBased(…))` shrinks wide pairs down to 0.5 inside the 2 dp inset. Weight 600 goes through `withDsFontWeight`.
  - The image is drawn with `ContentScale.Crop`, clipped to `CircleShape`.
- **Views:**
  - `TSAvatarView` is a `FrameLayout` measured at exactly the diameter. Children are clipped to the circle in `dispatchDraw`, because `clipToOutline` is ignored by software rendering (sdk 26 drew square photos).
  - Initials: a `MaterialTextView` with uniform auto-size in px (`TextViewCompat`), so the font scale does not change it. The weight comes from `TextAppearance.TS.Component.AvatarInitials`: `sans-serif-medium` below API 28, and `textFontWeight` 600 from `values-v28`.
  - The node reports `name` as the content description with the `ImageView` class.
