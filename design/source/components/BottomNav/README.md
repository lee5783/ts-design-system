# BottomNav

The mobile app's primary navigation: 3–5 top-level destinations pinned to the bottom (`tab-bar`, 64px plus the safe-area inset). From `bp-md` up, move these destinations to a side rail or sidebar.

## Props
`items`: `[{id, label, icon, badge?}]`; `value` + `onChange` or `defaultValue`; `label` (accessible name).

## Do / don't
- Always show labels, one word each.
- Active item: `accent-text` icon and label on an `accent-subtle` pill.
- `badge` is a count; it uses `danger-solid`.
