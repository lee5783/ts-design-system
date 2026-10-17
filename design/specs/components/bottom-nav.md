# BottomNav

This is the cross-toolkit contract for the TS BottomNav.

- **Sources:**
  - `design/source/components/BottomNav/README.md`
  - `bundle.css` (`.ts-nav*`)
  - `index.d.ts` (`BottomNavProps`)
- **Constants:** `design/extensions/native-tokens.json`:
  - `bottom-nav-pill-width`, `bottom-nav-pill-height`
  - `bottom-nav-label-font-weight`
  - `bottom-nav-badge-*`
  - `tab-bar` (64)
- **Decisions:**
  - C-09: a custom bar on both platforms, plus a UIKit `TSTabBarController` that hides the system tab bar
  - C-12: pressed state
  - C-24: library strings
  - C-27 (j): the badge maximum is "99+"

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `BottomNav` | `TSBottomNavStyleSpec` (`resolve` per item, `container`, `badge`) | `TSBottomNav` (one `Button` per item with the internal `TSBottomNavItemStyle`) | `TSBottomNavView: UIControl` (internal `TSBottomNavItemView: UIControl` per item) | `TSBottomNav(items, selectedId, onSelect, modifier, label, enabled, windowInsets)`: a `Row` of `selectable` cells, each `weight(1f)` | `TSBottomNavView : LinearLayout` (internal `TSBottomNavItemView : ViewGroup` per item, weight 1) |
| app shell (not in the DS) | — | `TSBottomNavShell(items:selection:label:content:)`: a `ZStack` that keeps every destination alive, with the bar in `safeAreaInset(edge: .bottom)`. No `TabView`. | `TSTabBarController: UITabBarController` with the system tab bar hidden and `bottomNav` pinned to the bottom | none: the consumer places `TSBottomNav` at the bottom of its own `Column` / `Scaffold`. No M3 `NavigationBar`. | none: the consumer places `TSBottomNavView` at the bottom of its layout. No MDC `BottomNavigationView`. |
| `items: [{id, label, icon, badge?}]` | `TSBottomNavItem(id:label:icon:badge:)`, `badgeText`, `badged(_:)` (Android: `TSBottomNavItem(id, label, icon: TSIcon, badge: Int?)`, `badgeText`, `badged`) | `items:` | `items`. Changing only badges or labels updates the views in place; changing the ids rebuilds them, and an unknown selection falls back to the first item. | `items` (keyed by id) | `items`, same in-place / rebuild rule as UIKit |
| `badge?: number \| string` | `badge: Int?`, formatted by `TSBadgeCountFormatter` (≤ 0 hides it, > 99 shows "99+") | | | | |
| `value` / `defaultValue` / `onChange` | `Configuration.isSelected` (Android `Configuration.selected`) | `selection: Binding<String>` | `selectedID` + `.valueChanged` (programmatic sets are silent). On `TSTabBarController`: `selectedIndex` / `selectedViewController`, plus the `UITabBarControllerDelegate` `shouldSelect` / `didSelect`. | `selectedId` + `onSelect` (state hoisted) | `selectedId` (silent) + `setOnItemSelectedListener` |
| `label` | — | `label:` (accessible name of the bar) | `label` | `label` (content description of the group) | `label` / `app:tsLabel` |
| disabled (not in the DS props) | `TSControlState.disabled` | `.disabled(_:)` on the bar | `isEnabled` on the bar | `enabled` | `isEnabled` / `android:enabled` |
| safe area | — | `safeAreaInset(edge: .bottom)` | `safeAreaLayoutGuide.bottom` | `windowInsets` (default `WindowInsets.navigationBars`, bottom + horizontal) applied below the 64 dp band | navigation-bar insets applied as padding (`applySystemBarInsetsAsPadding`), so the bar is 64 dp + the inset |

## Matrix

| Item | Icon + label | Pill (56×28, capsule) |
|---|---|---|
| unselected | `text-muted` | — |
| unselected, pressed [est, C-12] | `text-muted` | `surface-pressed` |
| selected (pressing it changes nothing) | `accent-text` | `accent-subtle` |
| disabled | as above, each item at 0.45 (the bar chrome stays opaque) | |
| focused | focus ring around the pill (radius 14) | |

**Geometry:**

- **Bar:**
  - The bar is `tab-bar` (64) plus the bottom safe-area inset.
  - Its fill is `surface-card` and runs down into the safe area.
  - It has a 1 pt `border-subtle` line along the top.
- **Items:**
  - Every item has the same width: an `HStack` with `frame(maxWidth: .infinity)` in SwiftUI, and a `fillEqually` stack in UIKit. No `Layout` protocol is used.
  - Each item is a column: a 24 pt icon (`icon-lg`) centred in the 56×28 pill, then 2 pt, then the label.
  - The column is centred vertically in the 64 pt band.
- **Label:**
  - The label is `caption` at weight 500 (12/16), on one line, truncated at the tail.
  - It follows Dynamic Type up to **xxxLarge** (`labelMaximumSizeCategory`) and is clamped above that, so the bar never grows.
  - At accessibility sizes, a long press shows the **Large Content Viewer** with the icon and the label.
- **Badge:**
  - The badge is `danger-solid` with `text-on-accent` text at a fixed 10 pt, weight 600.
  - It is 16 pt high, at least 16 pt wide, with 4 pt horizontal padding, and it is a capsule.
  - **Position:**
    - Its top sits 6 pt below the item top at the default size: 3 pt above the pill, tracking the pill.
    - Its leading edge sits 6 pt past the item centre, so in RTL its trailing edge sits 6 pt before the centre.
  - The badge does not scale with Dynamic Type (fixed chrome).
- **Items per bar:** 3–5 (`itemCountRange`). This is documented but not enforced.
- **Motion:** the selection change animates over `duration-base`, or happens at once with reduce motion. There is no haptic, as on `UITabBar`.

## Accessibility

- **The bar** is a container (SwiftUI `.accessibilityElement(children: .contain)`, UIKit `.semanticGroup`) whose name is `label`.
- **Each item is one element:**
  - **Label:** the item label.
  - **Value:** "Tab, n of N" (`ts.tab.position`). With a badge, it adds ", <count> new" (`ts.bottom-nav.badge`, through `TSBottomNavAccessibility.value`). For example: "Inbox, selected, Tab, 2 of 4, 3 new", or "99+ new".
  - **Traits:** the selected trait only on the selected item, and **no button trait**.
  - This is the Tabs rule (T25). The container `.tabBar` trait is not used, because VoiceOver would announce the position twice.
- **Hidden from VoiceOver:** the badge view and the pill.
- **Large Content Viewer:**
  - SwiftUI: `accessibilityShowsLargeContentViewer` with a `Label`.
  - UIKit: `showsLargeContentViewer`, `largeContentTitle` and `largeContentImage` on each item, plus a `UILargeContentViewerInteraction` on the bar.
- **Hit area:** each item fills its whole band (at least 64 pt high and at least 64 pt wide with 5 items on a 320 pt screen).

## Shells and the system tab bar (C-09)

- **SwiftUI:**
  - `TSBottomNavShell` uses no `TabView`, so there is never a system tab bar, Liquid Glass or not.
  - Hidden destinations stay alive and keep their state, but are invisible, take no hits and are hidden from VoiceOver.
  - Destinations are built eagerly.
- **UIKit:**
  - **Hiding the system bar:** `TSTabBarController` hides the system bar through `TSTabBarCompat.hideSystemTabBar` on every layout pass:
    - iOS 18+ (`TSCompat.tabBarHidden == .native`): `setTabBarHidden(true, animated: false)`
    - every version: `tabBar.isHidden`, no user interaction, and `accessibilityElementsHidden`
  - **Insets:** it gives each child `additionalSafeAreaInsets.bottom = 64`, so content scrolls clear of the bar. This overwrites any bottom value the consumer set.
  - **Placement:**
    - Use `TSTabBarController` as a root or full-screen controller, with each tab in its own `UINavigationController`.
    - Pushing it inside a navigation controller leaves later tabs under the navigation bar. This is the same as a plain `UITabBarController`.
- **UIKit bar inside scrolling content:** the bar always adds its own bottom safe-area inset, because `stackView.bottom` is pinned to `safeAreaLayoutGuide.bottom`. Inside a scroll view it can grow while it passes the home-indicator area, so place the bar at the screen bottom, as intended.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSBottomNavStyleSpecTests.swift` | 8 configurations (unselected/selected × default, pressed, disabled, focused) → foreground, pill, font, icon size, opacity, focus; disabled ignores pressed; bar/pill geometry; badge constants (−3 from the pill); label clamp at xxxLarge (and `.unspecified` → large); the clamped label fits 64; `badgeText` nil/≤ 0/3/99/99+; the accessibility value with and without a badge |
| Strings | `TSStringsTests.swift` | "3 new" |
| Compat | `TSCompatTests.swift` | `tabBarHidden` path; both paths hide the bar and take it out of touch and VoiceOver (`isTabBarHidden` on 18+) |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSBottomNavBehaviorTests.swift` | tap moves the selection, the selected item is a no-op (no binding write), disabled blocks; label, value with the badge, selected trait, no button trait, bar label; badge text "3"/"99+" hidden from VoiceOver and absent without a count; equal widths (`maxWidth: .infinity`), 64 high, still 64 at AX5; the shell has no `TabView`, shows only the selected destination, and tapping switches |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSBottomNavViewBehaviorTests.swift` | tap selects and fires `.valueChanged` once; programmatic selection is silent; disabled blocks and dims; `accessibilityActivate`; accessibility; badges, and an in-place badge update; replacing items; 5 equal widths, 64 + safe area, the pill is 56×28, hairline; badge position LTR/RTL; AX5 clamped font, bar stays 64, Large Content Viewer. **`TSTabBarController`:** the system tab bar is hidden and no visible `UITabBar` is in the window; it stays hidden after selection and relayout; children get a 64 inset; nav → `selectedIndex` + delegate `didSelect`; `selectedIndex` / `selectedViewController` → nav; a refusing delegate; badge update through `items` |
| Snapshots | `TSBottomNavSnapshotTests` / `TSBottomNavViewSnapshotTests` (`bottomNav.gallery-*`) | 3 items; 4 with badges 3 and 99+; 5 pressed; 5 selected on a badge (focused in SwiftUI); disabled. Standard matrix, including AX5 (clamped labels). |
| Demo | `NavigationComponentsTests` (both iOS demos) | playground selection, value, counter, no-op re-tap, "99+ new" then a cleared badge, disabled blocks; the shell switches tabs, updates the badge from a tab, and `app.tabBars.count == 0` |
| Spec (Android) | `android/core/src/test/kotlin/com/ts/ds/core/style/TSBottomNavStyleSpecTest.kt` | 8 configurations → foreground, pill, caption 500, 56×28 / radius 14, icon 24, gap 2, opacity, focus ring, motion; disabled ignores pressed; 64 dp container + hairline; badge tokens and `topFromPillDp` −3; `labelFontScale` clamp at 1.3 and the clamped label fits 64; `badgeText` nil/≤ 0/3/99/99+; `itemCountRange`; unknown selection → first |
| Strings (Android) | `TSStringsTest.kt` | "3 new"; `TSBottomNavAccessibility.value` "Tab, 2 of 4", "Tab, 2 of 4, 3 new", "Tab, 4 of 5, 99+ new" |
| Compose | `android/compose/src/test/kotlin/com/ts/ds/compose/components/bottomnav/TSBottomNavTest.kt` (sdk 26 and 36) | tap selects, `Role.Tab`, "Tab, n of N"; re-tap no-op; disabled blocks; badge in the `stateDescription` and hidden from semantics; badge update; selectable group, label, `CollectionInfo` / `CollectionItemInfo`; 64 dp, equal 64 dp widths, 56×28 pill 9 dp from the top; a 24 dp navigation inset → 88 dp with the band on top; label clamped at fontScale 2.0 (bar 64, line 20.8); badge −3 from the pill and +6 past the centre, mirrored in RTL |
| Views | `android/views/src/test/kotlin/com/ts/ds/views/components/bottomnav/TSBottomNavViewTest.kt` (sdk 26 and 36) | tap selects and notifies once; re-tap no-op; programmatic selection silent, unknown → first; disabled blocks and dims each item; `View` class, selected, content description, value with the badge, `CollectionItemInfo`; bar `CollectionInfo` + label; badge "3"/"99+", hidden when empty, updated in place; replacing ids rebuilds; 64 px bar, 5 × 64, 56×28 pill at 9, `accent-subtle` / pressed `surface-pressed` pill; navigation-bar inset → padding and 88 (sdk 36); badge position LTR/RTL; fontScale 2.0 → label 15.6 px, badge 10 px, bar 64 |
| Snapshots (Android) | `TSBottomNavSnapshotTest` / `TSBottomNavViewSnapshotTest` (`gallery_*`) | same gallery as iOS (focus forced on every item in Compose, on the selected item in Views). Standard matrix, including fontScale 2.0 (clamped labels). |
| Demo (Android) | `NavigationComponentsTest` (both Android demos) | playground selection, value, counter, no-op re-tap, "99+ new" then a cleared badge, disabled blocks; the shell switches tabs, updates the badge from a tab, and a toast shows above the bar |

## Platform notes

- **iOS:**
  - `UITabBar` / `TabView` cannot draw the pill or the flat 64 pt bar.
  - On iOS 26 the system bar is the floating Liquid Glass bar, so both toolkits draw their own bar (C-09).
  - The SwiftUI pressed and focused states are forced in snapshots through the internal `forcingState(_:for:)`. UIKit uses `isHighlighted`, and its focus halo is a `UIFocusHaloEffect` around the pill (not snapshotted).
  - **SwiftUI `TSBottomNavShell`** builds every tab's content up front and keeps the unselected tabs in the hierarchy (opacity 0, no hit testing, hidden from accessibility), so tab state survives switching. A consequence: a `.tsDialog` whose binding turns true inside a hidden tab still presents. Drive dialogs from the visible tab, or gate the binding on the selection.
  - **UIKit `TSTabBarController`** gives each child a 64 pt `additionalSafeAreaInsets.bottom` for the bar. `hidesBottomBarWhenPushed` is not supported: the TS bar stays visible on pushed controllers. A child removed with `setViewControllers` keeps its 64 pt inset.
- **Android:**
  - **C-09:** both toolkits draw their own bar. M3 `NavigationBar` (80 dp, 64×32 pill) and MDC `BottomNavigationView` are not used.
  - **Label clamp:** `TSBottomNavStyleSpec.labelFontScale(fontScale)` caps the label at `LABEL_MAX_FONT_SCALE` = 1.3 (the xxxLarge equivalent; 12/16 → 15.6/20.8). Compose provides a clamped `Density` around the label; Views sets the size and line height in px from the clamped scale. The bar stays 64 dp at fontScale 2.0. The badge is a fixed 10 dp (dp, not sp). Android has no Large Content Viewer, so nothing replaces it.
  - **Accessibility (the T36 tabs decision):** Compose cells are `selectable(role = Role.Tab)` with `stateDescription` = the value ("Tab, 2 of 4, 3 new") and `CollectionItemInfo`; the bar is a `selectableGroup()` with `CollectionInfo(1, N)` and the label as its content description. Views items report the plain `View` class (no "button"), `isSelected`, the label as content description, the value as `stateDescription` and `CollectionItemInfo`; the bar reports `CollectionInfo`. The badge, pill and label are hidden (Compose `clearAndSetSemantics`, Views `IMPORTANT_FOR_ACCESSIBILITY_NO`).
  - **Touch target:** each item is the full 64 dp band, and at least 64 dp wide with 5 items on a 320 dp screen, so above 48 dp.
  - **Insets:** Compose takes `windowInsets` (pass `WindowInsets(0)` when a parent already handles them). Views installs its own `OnApplyWindowInsetsListener`; a consumer listener replaces it.
  - **Toast above the bar:** Views `TSToastHost.anchorView = bottomNav`; Compose `TSToastHost(state, bottomInset = 64.dp)` around the shell content (the toast layer already adds `safeDrawing`).
  - **Motion:** the pill and foreground colours cross-fade over `duration-base` with `easing-standard`; with reduce motion they switch at once. No haptic.
  - **Badge numerals** follow the locale (`ts_badge_count_overflow`), so an Arabic locale shows Arabic-Indic digits.
