# ListItem and list group

Cross-toolkit contract for the TS ListItem and its grouped list (`.ts-list`). Source: `design/source/components/ListItem/README.md`, `bundle.css` (`.ts-list`, `.ts-list-item*`), `index.d.ts` (`ListItemProps`). Tokens: `control-md`, `duration-base`, `easing-standard`, `space-2/3/4`, `body`, `body-sm`, `overline`, `text-primary/secondary/muted`, `surface-card`, `surface-pressed`, `surface-hover`, `border-subtle`, `accent-subtle`, `accent-text`, `radius-md`, `radius-lg`, `list-item-tile-size` (`design/extensions/native-tokens.json`).

## API mapping

| DS (web) | Style spec (`TSFoundation` / `:core`) | SwiftUI | UIKit | Compose | Views |
|---|---|---|---|---|---|
| `ListItem` | `TSListItemStyleSpec` | `TSListItem` (generic `@ViewBuilder` leading/trailing slots) | `TSListItemConfiguration: UIContentConfiguration` + `TSListItemContentView`; hosted by `TSListItemCell: UICollectionViewListCell` or the standalone `TSListItemView: UIControl` | `TSListItem(title, modifier, subtitle, onClick, showsChevron, enabled, interactionSource, leading, trailing)` (composable slots) | `TSListItemView : LinearLayout` (standalone, XML or `RecyclerView` item) |
| `title` / `subtitle` | `Configuration.hasSubtitle` | `_ title:`, `subtitle:` | `title`, `subtitle` | `title`, `subtitle` | `title`, `subtitle` (`app:tsTitle`, `app:tsSubtitle`) |
| `leadingIcon` | `TSListItemStyleSpec.tile` | `leading: { TSListItemIcon(.mail) }` | `leading: .icon(.mail)` | `leading = { TSListItemIcon(TSIcon.Mail) }` | `leading = TSListItemLeading.Icon(TSIcon.Mail)` (`app:tsLeadingIcon`) |
| `leading` (any node, e.g. Avatar) | — | `leading: { TSAvatar(...) }` | `leading: .avatar(name:image:)` | `leading = { TSAvatar(...) }` | `leading = TSListItemLeading.Avatar(name, image)` (`app:tsName`) |
| `trailing` meta text / Badge | `trailingFont`, `trailingColor` | `trailing: { Text(...) }`, `trailing: { TSBadge(...) }` | `trailing: .text(_)`, `.badge(_, tone:)` | `trailing = { TSListItemValue(...) }`, `trailing = { TSBadge(...) }` | `trailing = TSListItemTrailing.Text(_)` (`app:tsTrailingText`), `.Badge(_, tone)` |
| `trailing` Switch | `TSListItemRole.toggle` | `TSListItem(_:subtitle:isOn:leading:)` (trailing `TSListItemSwitch`) | `trailing: .toggle(isOn:)` + `onToggle` (and `.valueChanged` on `TSListItemView`) | `TSListItem(title, checked, onCheckedChange, modifier, subtitle, enabled, interactionSource, leading)` | `trailing = TSListItemTrailing.Switch` + `isChecked` + `setOnCheckedChangeListener` |
| `chevron` | `Resolved.chevron` | `showsChevron:` | `showsChevron` | `showsChevron` | `showsChevron` (`app:tsShowsChevron`) |
| `onClick` / `href` | `Configuration.isInteractive`, `TSListItemRole` | `action:` | `isInteractive` (+ cell selection, or `.primaryActionTriggered` on `TSListItemView`) | `onClick` | `setOnClickListener` (a listener makes the row interactive) |
| disabled (not in the DS) | `TSControlState.disabled` | `.disabled(_:)` | `isEnabled` (configuration or control) | `enabled` | `isEnabled` |
| `.ts-list` | `TSListItemStyleSpec.group` | `TSList { … }`, `TSListSection(header) { … }` on `LazyVStack` | `TSListView(items:)` (stack); `TSListLayout.section/makeLayout` + `TSListSectionHeaderView` (collection view) | `TSList { … }`, `TSListSection(header) { … }` (`Column` card) | `TSListView` (stacked card), `TSListDecoration(context, positionOf)` + `TSListSectionHeaderView` (`RecyclerView`) |
| divider outside a `TSList` (lazy lists, issue #6) | `TSListItemStyleSpec.group` (`divider`, `dividerThickness`, `dividerInset`) | `TSDivider()` | `TSDividerView` | `TSDivider(modifier)` | `TSDividerView : View` (or `TSListDecoration` on a `RecyclerView`) |
| disclosure row (not in the DS, issue #6) | `TSListItemStyleSpec.disclosure`, `TSListItemAccessibility` expansion state | `TSDisclosureListItem(_ title:, subtitle:, isExpanded: Binding<Bool>, leading:, content:)` | `TSDisclosureListItemView: UIControl` (`title`, `subtitle`, `leading`, `bodyView`, `isExpanded`; `.valueChanged` on user toggles) | `TSDisclosureListItem(title, expanded, onExpandedChange, modifier, subtitle, enabled, interactionSource, leading, content)` | `TSDisclosureListItemView : LinearLayout` (`title`, `subtitle`, `isExpanded` / `app:tsExpanded`, `setOnExpandedChangeListener`; XML children become the body) |

## Matrix

`TSListItemStyleSpec.allConfigurations` (Android: `configurations × states`): subtitle × interactive × layout (horizontal, stacked) × default, pressed, disabled, focused = 32.

| State | Row background | Opacity |
|---|---|---|
| default | none (the group's `surface-card`) | 1 |
| pressed, with an action | `surface-pressed` | 1 |
| pressed, without an action (plain or switch row) | none | 1 |
| focused, with an action [est] | `surface-hover` | 1 |
| disabled [est] | none | 0.45 for the row content |

- **Selection is not a visual state.** The DS defines no selected row. A UIKit cell's `isSelected` (collection-view selection) is ignored by `TSListItemConfiguration.updated(for:)`, so a selected cell is never drawn as pressed; show a current choice with trailing content (a badge, text or icon) instead.
- **Focus is a fill here, by exception.** ListItem is the only focusable component that shows keyboard focus as the `surface-hover` fill instead of the TS focus ring, because rows are full width inside the group card, as in the system list style. Both the focused and the disabled values are [est] and pending upstream.

Geometry: padding 12 / 16, gap 12, minimum height 44 (`control-md`). Title `body` 16/24 in `text-primary`; subtitle `body-sm` 14/20 in `text-muted`. Heights at the default text size: **48** with a title, **68** with a subtitle, 60 with a 36 pt tile or 40 pt avatar, 50 with a trailing switch (the switch lays out at its 26 pt track; its 44 pt hit area overflows vertically).

- Leading: content in `text-secondary`; the icon tile is 36 × 36, `radius-md`, `accent-subtle`, with a 20 pt icon in `accent-text`. Leading content is decorative for accessibility.
- Trailing: `body-sm`, `text-muted`; trailing content and chevron are 8 apart.
- Chevron: `chevron-right`, `icon-md` (20), `text-muted` [est]. It mirrors in RTL (asset language direction / `flipsForRightToLeftLayoutDirection`).
- Group: `surface-card`, 1 pt `border-subtle` border, `radius-lg`, **full-width 1 pt dividers** in `border-subtle` (C-question "full width vs inset": DS wins). Section headers are `overline` in `text-muted`, padded 16, 8 above the card; sections are 24 apart.

Dynamic Type: at accessibility sizes (`dynamicTypeSize.isAccessibilitySize` / `isAccessibilityCategory`) the row reflows to the **stacked** layout — leading, then text, then trailing, stacked with 8 pt gaps, chevron still at the trailing edge — and title, subtitle and trailing text wrap. At other sizes the title and subtitle are one line, truncated. No `ViewThatFits` (iOS 16).

## Divider

A full-width divider for rows that are not inside a `TSList` / `TSListView` (for example rows in a `LazyColumn`, a SwiftUI `LazyVStack` or a plain stack). It is the `TSList` divider: `border-subtle`, 1 pt / dp thick, inset 0 (`TSListItemStyleSpec.group`). Place it between rows; it draws nothing else (no card, border or corners). It is never an accessibility element. Inside a `TSList` the rows already draw their dividers, so do not add one there.

## Disclosure row

`TSDisclosureListItem` is a row whose title toggles a body below it (for example a third-party-notices screen: library name → licence text). C-027: a separate component, not a slot on `TSListItem`.

- **Header:** a list row with the title, optional subtitle and optional leading content, always interactive (pressed / focused fills as for a row with an action), with the chevron at the trailing edge. Heights as for a row with an action (48, 68 with a subtitle).
- **Chevron:** `chevron-right`, `icon-md`, `text-muted`; it rotates from 0° (collapsed, pointing to the trailing edge) to 90° (expanded, pointing down) over `duration-base` (150 ms) with `easing-standard`; instantly under reduce motion. In RTL the glyph is mirrored, so it rotates by -90° to point down (`TSListItemStyleSpec.disclosure.rotationDegrees(expanded, rightToLeft)`); SwiftUI mirrors `rotationEffect` together with the glyph, so it always passes `isRightToLeft: false`.
- **Body:** the consumer's content (use `TSText` / `TSFont` styles), shown only when expanded, below the header, padded 16 at the sides and 12 at the bottom (`horizontalPadding`, `verticalPadding` of the row). The body appears and disappears without a height animation [est].
- **State:** controlled. Compose `expanded` + `onExpandedChange`, SwiftUI `Binding<Bool>`, UIKit `isExpanded` (+ `.valueChanged` when the user toggles), Views `isExpanded` (+ listener on user toggles only). Disabled rows do not toggle and dim to 0.45.
- **In a group:** inside `TSList` the header draws the group divider above it like any row; the body belongs to the same row, so no divider separates header and body.

## Accessibility

- One element per row. Label = "title, subtitle". Trailing text or badge text is the value.
- Row with an action: button trait. With a chevron, the hint "Opens another screen" (`ts.list-item.navigates`).
- Row with a trailing switch: switch semantics on the row (value "On"/"Off", toggle trait on iOS 17+, button trait on 15–16, never selected — `TSAccessibilityTraitsCompat`); activating the row toggles the switch. The row itself is not tappable; only the switch (44 pt hit area) is.
- Disabled rows add not-enabled and block their action or switch.
- Disclosure row: the header is one button element, label "title, subtitle", value "Expanded" / "Collapsed" (`ts.list-item.expanded` / `ts.list-item.collapsed`, `ts_list_item_expanded` / `ts_list_item_collapsed`); activating it toggles. Compose also exposes `expand` / `collapse` actions; Views sets the state description and `AccessibilityAction.ACTION_EXPAND` / `ACTION_COLLAPSE`. The body is separate elements after the header, in reading order.
- Divider: hidden from accessibility on every toolkit.
- UIKit: the hosting cell or `TSListItemView` is the element (so `accessibilityIdentifier` works); the content view is not.
- Views: `TSListItemView` builds its accessibility node through view overrides, so it keeps its label, state and actions when a `RecyclerView` recycles the row (`TSRecycledAccessibilityTest`).

## Tests

| Layer | File | What |
|---|---|---|
| Spec | `ios/Tests/TSFoundationTests/TSListItemStyleSpecTests.swift` | 32 configurations → background (pressed/focused only with an action), opacity, tokens; line limits per layout; reflow rule; 48/68 heights from tokens; tile and group constants; role and label/hint rules |
| SwiftUI | `ios/Tests/TSDesignSystemTests/Behavior/TSListItemBehaviorTests.swift` | action fires; disabled blocks; pressed only with an action; navigates hint; switch semantics on both paths; switch activation + haptic; heights 48/68/50/60; AX5 reflow; full-width divider in a list, none standalone |
| UIKit | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSListItemViewBehaviorTests.swift` | `.primaryActionTriggered`; plain/disabled never fire; pressed only with an action; element label/value/hint/traits; switch semantics on both paths; activation toggles + `onToggle` + `.valueChanged`; hit testing; heights; AX5 reflow; RTL chevron; `TSListView` dividers |
| UIKit cells | `ios/Tests/TSDesignSystemUIKitTests/Behavior/TSListItemCellBehaviorTests.swift` | content configuration and cell as element; pressed only with an action; **reuse resets state**; toggle survives state updates; group positions; `TSListLayout` collection view positions, insets and header |
| Snapshots | `TSListItemSnapshotTests`, `TSListItemViewSnapshotTests` (`listItem.contents-*`, `listItem.states-*`) | contents (title, icon + badge + chevron, avatar + meta, switch, long text) and states (default, pressed, focused in SwiftUI, disabled, static pressed, switch off/disabled); standard matrix incl. AX5 and RTL |
| Spec (Android) | `android/core/src/test/.../style/TSListItemStyleSpecTest.kt`, `logic/TSStringsTest.kt` | 32 configurations → background, opacity, roles, line limits; 48/68 from tokens; stacking from fontScale 1.5; tile/group constants; `TSListGroupPosition` (make, corners, divider); role and label/hint rules |
| Compose | `android/compose/src/test/.../listitem/TSListItemTest.kt` (sdk 26/36) | action fires; disabled blocks; plain row has no click action; leading is decorative; navigates hint (click label); pressed `surface-pressed` / focused `surface-hover` pixels only with an action; switch semantics + activation + haptic; only the switch takes taps; 48 dp switch hit area outside the 26 dp track; heights 48/68/60/64/50; fontScale 2 stacking; divider in a `TSList` only |
| Views | `android/views/src/test/.../listitem/TSListItemViewTest.kt` (sdk 26/36) | click; disabled; plain never presses; Button/View/SwitchCompat class; label, state description, hint; switch activation + listener + haptic; touch delegate 48 × 48; heights; fontScale 2 stacking; RTL chevron; group-shaped pressed background |
| Views list | `TSListDecorationTest`, `TSListViewTest` | `RecyclerView` binding/recycling (state rebinds after scroll, holders reused, press cleared); offsets (24 below the last row); group runs and full-width 1 dp divider bounds; open edges when a group is cut by the viewport; `TSListView` positions and dividers |
| Snapshots (Android) | `TSListItemSnapshotTest`, `TSListItemViewSnapshotTest` (`contents_*`, `states_*`) | same galleries as iOS; standard matrix incl. fontScale 2 and RTL (16 + 16) |
| Divider / disclosure (#6) | spec tests above (`disclosure` rotation, duration, expansion strings); Compose `TSDividerTest`, `TSDisclosureListItemTest`; Views `TSDisclosureListItemViewTest` (incl. `dividerViewMatchesTheListDivider`, `removeAllViewsClearsOnlyTheBody`, `restoringStateSetsTheChevronWithoutAnimating`); SwiftUI `TSDisclosureListItemBehaviorTests` (incl. `divider`); UIKit `TSDisclosureListItemViewBehaviorTests` (incl. `divider`); snapshots `disclosure` / `divider` in `TSListItemSnapshotTest` / `TSListItemViewSnapshotTest`, and the `ListItemExtrasKind` cases in `TSListItemSnapshotTests` / `TSListItemViewSnapshotTests` | divider colour, thickness and full width equal to the `TSList` divider, not accessible; tap toggles; programmatic changes do not notify; chevron rotation (mirrored in RTL); collapsed / expanded accessibility value and actions; disabled blocks; header height and body padding |
| Demo | `ContentComponentsTests` (both iOS demos): `testListItemRowsPressOnlyWithAnAction`, `testListItemSwitchRowExposesSwitchSemantics`, `testListItemPlaygroundTapCountsAndSwitchTrailing`; `testListItemDisclosureRowExpandsItsBody`; `CatalogSmokeTests` (catalog built from ListItem). Android: `ContentComponentsTest` (both demos) `listItemRowsPressOnlyWithAnAction`, `listItemSwitchRowExposesSwitchSemantics`, `listItemPlaygroundTapCountsAndSwitchTrailing`, `listItemDisclosureRowExpandsItsBody`, Compose also `listItemLazyListRowsAreDividedOutsideAList`; `CatalogSmokeTest` | ListItem page sections "Divider outside a list" (lazy on SwiftUI / Compose, a plain stack on UIKit / Views) and "Disclosure rows" |

## Platform notes

- iOS: SwiftUI `List` is not used (separators, insets and backgrounds are constrained). `TSList` is a `LazyVStack`; each row draws its own top divider inside a list, and the first one sits under the card border.
- iOS UIKit cells: `TSListLayout` is a plain list layout with system separators off; each `TSListItemCell` learns its group position from its layout attributes and draws its edges, corners and top divider. Rows inserted or removed without a layout-attribute change keep their old position until the next layout pass. Configure a cell through `cell.item = TSListItemConfiguration(…)`, not by assigning `contentConfiguration` directly: the cell re-derives its configuration for every state change from `item`, so a trailing switch keeps its value across highlight and selection updates.
- Android strings: `ts_list_item_navigates` ("Opens another screen") in `:core` `ts_strings.xml`. Views reads it as the node's hint text; Compose has no hint semantics, so it is the click action label.
- Android reflow: the row stacks from `fontScale` 1.5 (`TSListItemLayout.STACKED_FONT_SCALE`, [est]; between the largest non-accessibility iOS size, about 1.35×, and AX1, about 1.65×). Heights at the default size: 48, 68 with a subtitle, 60 with the tile, 64 with the 40 dp avatar, 50 for a title-only switch row.
- Android switch row: one switch node (Compose `Role.Switch` + `ToggleableState` + `stateDescription` via `clearAndSetSemantics`; Views `SwitchCompat` class, checkable, ACTION_CLICK toggles). Only the 44 × 26 track takes touches, extended to 48 × 48 (Compose minimum touch target hit testing; Views `TouchDelegate` on the 50 dp row). The switch dims itself when disabled; the rest of the row uses 0.45.
- Compose: `TSList` is a `Column` card (clip, fill, border drawn over its rows); each row inside it draws a 1 dp top divider, and the first sits under the border. `TSListSection` adds the uppercase overline header. Rows use `TextDirection.Content`, so LTR text in an RTL layout keeps its order.
- Views: `TSListItemView` reads `resources.configuration.fontScale`, keeps the title/subtitle at their line heights (`minHeight`), and draws its pressed/focused fill with corners from `groupPosition`. `TSListDecoration` sets `groupPosition` on `TSListItemView` children, draws the card fill under rows (`onDraw`) and the border and dividers over them (`onDrawOver`), and adds the 24 dp section spacing below a group's last row. `TSListView` is the non-recycling card. `:views` depends on `androidx.recyclerview` 1.4.0 (`api`).
- Views state across a recreate: a switch row saves its checked state with the view hierarchy, so **give each `TSListItemView` a stable id** (an `android:id`, or the same programmatic id on every creation); Android saves view state by id, so rows without one come back unchecked.
- **Divider outside a list:** SwiftUI `TSDivider`, Compose `TSDivider`, UIKit `TSDividerView` and Views `TSDividerView` are plain views for stacks and lazy lists. In a `RecyclerView`, keep using `TSListDecoration`, which already draws the group dividers; there is no separate `ItemDecoration` for a bare divider.
- **Disclosure row, SwiftUI:** an `.accessibilityIdentifier` set on `TSDisclosureListItem` also lands on the body content (the outer identifier wins). In UI tests, find the body by its label, or set identifiers on the body views themselves.
- **Disclosure row, UIKit:** `TSDisclosureListItemView` is a standalone `UIControl` (also usable as a `TSListView` item); it forwards `accessibilityIdentifier` to its header. There is no collection-view cell variant: inside a `TSListLayout` collection view, host it in your own cell. The consumer owns the body view's text alignment.
- **Disclosure row, Views:** every child added in XML or code goes into the body; `removeAllViews()` / `removeView` act on the body only, never on the header. The expanded state is saved with the view hierarchy and restored without animating the chevron. `android:enabled` is not read from XML (as for `TSListItemView`); call `isEnabled` in code.
