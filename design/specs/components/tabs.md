# Tabs

Cross-toolkit contract for the TS Tabs. Source: `design/source/components/Tabs/README.md`, `bundle.css` (`.ts-tabs*`, `.ts-tab*`), `index.d.ts` (`TabsProps`). Constants: `design/extensions/native-tokens.json` (`tabs-segmented-track-padding`, `tabs-segment-padding-vertical`, `tabs-segment-padding-horizontal`). Decisions: C-27 (b) segmented padded to 46 with radius-md, (f) underline scrolls when it overflows; C-12 pressed; C-13 segment-change haptic.

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `Tabs` | `TSTabsStyleSpec` (`resolve` per tab, `container(_:isEnabled:)` for the list) | `TSTabs` (one `Button` per tab with the internal `TSTabStyle`) | `TSTabsView: UIControl` (internal `TSTabView: UIControl` per tab) | `TSTabs` (one `selectable(role = Role.Tab)` box per tab in a `selectableGroup()`) | `TSTabsView: FrameLayout` (internal `TSTabView: MaterialTextView` per tab) |
| `items: [{id, label}]` | `TSTabItem(id:label:)` | `items:` | `items` (replacing rebuilds the tabs; an unknown selection falls back to the first item) | `items: List<TSTabItem>` | `items` (replacing rebuilds; XML `android:entries`, id = label) |
| `value` / `defaultValue` / `onChange` | `Configuration.isSelected` | `selection: Binding<String>` | `selectedID` + `.valueChanged` (programmatic sets are silent) | `selectedId` + `onSelect` (not called for the selected tab) | `selectedId` + `setOnTabSelectedListener` (programmatic sets are silent) |
| `variant: 'underline' \| 'segmented'` | `TSTabsVariant` | `variant:` (default `.underline`) | `variant` | `variant` (default `Underline`) | `variant`, XML `app:tsTabsVariant` |
| `block` | — | `isBlock:` (segmented only: equal-width segments fill the width) | `isBlock` | `block` | `isBlock`, XML `app:tsBlock` |
| `label` | — | `label:` (accessible name of the tab list) | `label` | `label` (content description of the group) | `label`, XML `app:tsLabel` (content description) |
| disabled (not in the DS props) | `TSControlState.disabled` | `.disabled(_:)` on the whole list | `isEnabled` on the whole list | `enabled` | `isEnabled`, XML `android:enabled` |

## Matrix

| Variant | State | Label | Fill | Indicator / shadow |
|---|---|---|---|---|
| underline | unselected | `label` 14/20/500 `text-muted` | — | — |
| underline | unselected, pressed [est, C-12] | `text-primary` (the web hover colour) | — | — |
| underline | selected (pressed has no extra effect) | `accent-text` | — | 2 pt `accent-text` underline over the 1 pt `border-subtle` hairline |
| segmented | unselected | `text-secondary` | — | — |
| segmented | unselected, pressed [est, C-12] | `text-secondary` | `surface-pressed`, radius-md | — |
| segmented | selected | `text-primary` | `surface-card` thumb, radius-md | `shadow-sm` |
| both | disabled | as above, everything at 0.45 once (tabs, track, hairline) | | |
| both | focused | focus ring around the tab (radius-sm for underline, radius-md for segmented) | | |

Geometry:

- **underline:** tabs hug their label with 0 horizontal padding, `space-3` (12) vertical padding, at least 44 pt high, `space-4` (16) apart. The list is a horizontal scroll view that scrolls when the tabs overflow (C-27 f). The hairline spans the full width. A selection change scrolls the selected tab into view.
- **segmented:** track `background-secondary`, 1 pt `border-subtle`, radius-lg, 3 pt padding. Segments have 6/16 padding and radius-md, at least 38 pt high, so the track is **46 pt** (`TSTabsStyleSpec.segmentedHeight`, C-27 b; the web is 40). With `isBlock` the segments share the width equally; otherwise they hug their labels. Segments never scroll.
- Labels are single-line. Underline labels grow with Dynamic Type (the list scrolls); segmented labels grow too but shrink to fit down to 0.5 (`segmentMinimumScaleFactor`) and then truncate. Long-pressing a segment shows the Large Content Viewer.
- Motion: the underline and the thumb slide to the new tab over `duration-base` (150 ms, `easing-standard`), or change at once with reduce motion.

## Accessibility

- The list is a container (SwiftUI `.accessibilityElement(children: .contain)`, UIKit `accessibilityContainerType = .semanticGroup`) whose label is `label`.
- Each tab is one element: label = item label, value = "Tab, n of N" (`TSStrings.tabPosition`, key `ts.tab.position`, overridable), the selected trait only on the selected tab, and **no button trait**. VoiceOver reads "Week, selected, Tab, 2 of 3", as for a system tab bar item.
- The container `.tabBar` trait is not used, because VoiceOver would then add its own "tab, n of N" on UIKit and SwiftUI 17+ (and SwiftUI has no `.isTabBar` on 15–16). One rule on every OS version.
- Hit areas: underline tabs are at least 44 pt high. Segments are 38 pt high and expand their hit area vertically by 3 pt to 44 (SwiftUI content shape on the vertical edges; UIKit `point(inside:)` through `TSHitArea`).
- Selecting a segment plays the selection haptic (`TSHaptics`, opt-out); underline tabs do not.

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSTabsStyleSpecTests.swift` | 16 configurations (2 variants × unselected/selected × default, pressed, disabled, focused) → label, fill, shadow, indicator, opacity, focus; underline and segmented geometry (46 track, 44 hit); disabled dims once |
| Strings | `ios/Tests/TSFoundationTests/TSStringsTests.swift` | "Tab, 2 of 4" |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSTabsBehaviorTests.swift` | tap moves the selection binding and plays the segmented haptic; the selected tab is a no-op; disabled blocks; label, "Tab, n of N", selected trait, no button trait; list label; underline is a horizontal scroll view that overflows instead of squeezing; segmented has no scroll view; track 46 and segment hit area ≥ 44; underline ≥ 44 |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSTabsViewBehaviorTests.swift` | tap selects and fires `.valueChanged` once (no-op on the selected tab); programmatic selection is silent; disabled blocks and dims once; `accessibilityActivate`; label, value, traits and container; replacing items; overflow scrolls and selection scrolls into view with the indicator under the tab; block segments are equal with the thumb under the selection; track 46, segment hit ≥ 44 including `hitTest` 2.5 pt above a segment; AX5 growth |
| Snapshots | `TSTabsSnapshotTests` (`tabs.<variant>-*`), `TSTabsViewSnapshotTests` (`tabs.<variant>-*`) | default, pressed, disabled (+ focused in SwiftUI), underline overflow, segmented inline and four segments; standard matrix |
| Spec (Android) | `android/core/src/test/kotlin/com/ts/ds/core/style/TSTabsStyleSpecTest.kt` | the same 16 configurations plus the containers (hairline, 46 track, 4 dp content inset, 1/2 dp touch outset), disabled dims once, unknown selection falls back to the first |
| Strings (Android) | `android/core/src/test/kotlin/com/ts/ds/core/logic/TSStringsTest.kt` | `ts_tab_position` → "Tab, 2 of 4" |
| Compose | `android/compose/src/test/kotlin/com/ts/ds/compose/components/tabs/TSTabsTest.kt` | tap selects; `Role.Tab`, selected, `stateDescription` "Tab, n of N"; `selectableGroup` + `CollectionInfo` 1 × N + `CollectionItemInfo`; selected tab no-op; disabled blocks; segmented haptic only; segmented 48 box, 48 touch, 38 thumb 5 dp from the top; block equal widths; underline 48 tall with the indicator 2 dp above the bottom; overflow scroll + selection scrolls into view; segmented never scrolls; RTL start alignment; 150 ms slide and reduce motion (sdk 26 and 36) |
| Views | `android/views/src/test/kotlin/com/ts/ds/views/components/tabs/TSTabsViewTest.kt` | tap selects and notifies once; selected no-op; programmatic selection silent; disabled blocks and dims once; node selected + `CollectionItemInfo` + "Tab, n of N"; list `CollectionInfo` and label; 46 track inside a 48 view, 48 segments, 38 thumb; selection haptic only on segments; block equal widths; underline indicator; overflow scrolls and selection scrolls into view; 150 ms `PathInterpolator` and reduce motion; replacing items; XML attributes (sdk 26 and 36) |
| Snapshots (Android) | `TSTabsSnapshotTest`, `TSTabsViewSnapshotTest` (`underline_*`, `segmented_*`) | default, pressed, disabled, focused, underline overflow, segmented block and two-segment inline; standard matrix |
| Demo | `NavigationComponentsTests` (both iOS demos) | selecting in the playground updates the value, selected trait and counter; overflow scrolls to a far tab; disabled blocks |

## Platform notes

- iOS: `UISegmentedControl` / `Picker(.segmented)` cannot be given the DS radius, padding or shadow, so both variants are custom. The SwiftUI pressed and focused states are forced in snapshots through the internal `forcingState(_:for:)`; UIKit uses `isHighlighted` and `UIFocusHaloEffect` per tab (not snapshotted).
- iOS UIKit: segmented labels use the plain scaled `label` font (no fixed line height) so shrink-to-fit text stays centred.
- Android demos: `NavigationComponentsTest` in both apps: the playground tap 2 dp below the top of the 48 dp segment box (3 dp above the visual segment) selects it and updates the value and counter, re-tapping is a no-op, disabled blocks; the overflow list is swiped until "Reports" is fully visible and then selected.
- Android touch target: the whole list is at least 48 dp tall (the Android minimum). Underline tabs are a 44 dp visual band centred in a 48 dp touch box (the hairline sits at the bottom of the band, 2 dp above the view bottom). The 46 dp segmented track is centred in the 48 dp box, and each 38 dp segment's touch box is the full 48 dp height (5 dp outset), so a tap just above the visual segment selects it in both toolkits.
- Android accessibility: each tab has the selected state, `stateDescription` "Tab, n of N" (`ts_tab_position`, `TSTabsAccessibility.position`) and `CollectionItemInfo`; the list carries `CollectionInfo` (1 × N) and the `label` as content description. Compose also sets `Role.Tab` (TalkBack role "Tab"); Views reports the plain `View` class (no button role), as iOS drops the button trait.
- Compose: tabs are laid out by an internal `Layout` that places the indicator or thumb in the same pass as the tabs, so the first frame (and every snapshot) already shows it. The slide uses two `Animatable`s held at the old position until the effect starts; reduce motion (`LocalTSReduceMotion` / system "Remove animations") moves it at once. The underline list is `horizontalScroll`, and the selected tab calls `BringIntoViewRequester.bringIntoView()` when it becomes selected. Pressed and focused states are forced in snapshots through `LocalTSForcedControlState` (applied to every tab of the list).
- Views: `TSTabsView` hosts a `HorizontalScrollView` (scrolling disabled and measured to fit for segmented) whose content holds the thumb, the tab row (raised by the thumb elevation so labels draw above the shadowed thumb) and the indicator; the hairline and the track are drawn by `TSTabsView` itself. The indicator and thumb are laid out manually after each content layout and slide with a `ValueAnimator` + `PathInterpolator` on user selection (programmatic changes are immediate). Selection scrolls the tab into view through `HorizontalScrollView.requestChildRectangleOnScreen`, so the page's vertical scroll is untouched. Snapshots use `isPressed` and a real `requestFocus` (ring drawn inside the shape, the T32 Views limitation).
- Android segmented labels are single-line with an end ellipsis; they do not shrink to 0.5 as on iOS (see the T36 follow-ups).
