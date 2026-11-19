# macOS: getting started

The Swift package also builds for **macOS 13 or later** (`platforms: [.iOS(.v15), .macOS(.v13)]`). It is the same package and the same tags as iOS ([ios-getting-started.md](ios-getting-started.md)): add it the same way and depend on `TSTokens` and/or `TSDesignSystem`.

**Verified** (Xcode 26.6, macOS 26, Apple silicon): `make macos-build`, `make macos-test`, and a throwaway SwiftPM executable that depends on the package by local path and renders tokens, the type scale, `TSButton`, `TSBadge`, `TSCard`, `TSToast`, `TSAlert`, `TSSpinner` and `TSIcon` in light and dark. A second throwaway executable hosts a view with `.tsToastHost()` and enqueues a toast with an action. A capture of the real window shows the toast. A real click on the action runs its handler, and a click on a transparent area of the panel reaches the host view. **Not executed yet:** a signed, sandboxed macOS app target.

## What is on macOS

| Product | On macOS |
|---|---|
| `TSTokens` | Everything. `TSColor.color` (SwiftUI) works on both platforms; the AppKit bridges are `nsColor`, `NSColor.ts<Name>`, `rgba(for: NSAppearance)`, `TSShadowToken.layers(for: NSAppearance)`, `TSBaseTextStyle.nsTextStyle`, `TSFontWeight.nsFontWeight(_:)`, `TSTypographyToken.nsFontWeight` and `TSCubicBezier.timingFunction` (`CAMediaTimingFunction`). The UIKit bridges (`uiColor`, `UIColor.ts<Name>`, `uiTextStyle`, `uiFontWeight`, `timingParameters`) stay iOS-only. |
| `TSDesignSystem` | Every SwiftUI component except `TSBottomNav` / `TSBottomNavShell`. `TSFont` gives `baseFont` (`NSFont`, the same name as the iOS `UIFont` accessor), `lineHeight`, `lineSpacing`, `tracking`, `attributes(color:alignment:)` and `attributedString(_:color:alignment:)` for AppKit text. |
| `TSDesignSystemUIKit` | Compiles to an empty module. Do not depend on it from a macOS target. |

`TSFont(…).font(dynamicTypeSize:)`, `lineHeight(dynamicTypeSize:)`, `lineSpacing(dynamicTypeSize:)` and `tracking(dynamicTypeSize:)` are the cross-platform SwiftUI accessors; `.tsFont(_:)` uses them.

## How it differs from iOS

- **No Dynamic Type.** macOS has no content size categories, so typography resolves at the token's base size and ignores `dynamicTypeSize`.
- **Dark mode** follows the view's appearance: SwiftUI `colorScheme` and `NSAppearance` both resolve `TSColor` (dynamic `NSColor` with `aqua` / `darkAqua`).
- **Dialogs** (`.tsDialog`) present as a native SwiftUI `.sheet` for both variants; there is no over-full-screen centre dialog or UIKit sheet bridge.
- **Toasts** (`.tsToastHost()`) show in a borderless, non-activating `NSPanel`. The panel is a child window of the host window and covers its content area. It follows resizes and opens only while the host window is on screen. A toast enqueued earlier appears when the host window opens. The toast and its action take the first click. Clicks on the panel's transparent area go to the host window.
- **Haptics** (`TSHaptics`) are a no-op; `TSHapticEvent.notificationType` is iOS-only.
- **Text input traits:** `TSTextInputTraits` keeps `textContentType` (`NSTextContentType`), `isAutocorrectionEnabled` and `returnKey` and the same presets; keyboard type and autocapitalisation do not exist on macOS. On an `NSTextView`, `isAutocorrectionEnabled == false` turns automatic spelling correction and text replacement off; `true` leaves the user's system setting alone rather than forcing them on. `NSTextField` has no per-field flag and AppKit shares one field editor across a window's fields, so `apply(to: NSTextField)` only sets the content type — to disable correction for a single field, give it its own field editor (`windowWillReturnFieldEditor(_:to:)`) and call `apply(to:)` on that. Multiline `TSTextField` always uses the native `TextField(axis: .vertical)`.
- **Focus ring:** the SwiftUI `.tsFocusRing` overlay works; `TSFocusRing.haloEffect` / `applyHalo` (UIKit) are iOS-only.
- **Icons** load from the same asset catalog (`TSIconName.image()` returns `NSImage`); right-to-left flipping is applied by `TSIcon` only.

## Building and testing the package for macOS

```bash
make macos-build              # xcodebuild for platform=macOS
make macos-test               # golden type-check at macOS 13, then the platform-neutral suites
```

Use `xcodebuild` (these targets), not `swift build` / `swift test`: the SwiftPM command line copies `Localizable.xcstrings` and the asset catalogs uncompiled, so strings and icons do not resolve and the `TSStrings` tests fail there. Test files that need UIKit are compiled out on macOS (`#if canImport(UIKit)`), so `macos-test` runs only the platform-neutral suites; the full suite still runs on iOS (`make ios-test`). CI runs both in `.github/workflows/ios.yml` (`test (macOS)` job).
