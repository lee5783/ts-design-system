# Button

Triggers an action. Use **primary** once per screen for the main action, **secondary** beside it, **ghost** in toolbars and dense rows, **danger** only for irreversible actions (always behind a confirm Dialog), **link** inside running text.

## Props
- `variant`: `'primary' | 'secondary' | 'ghost' | 'danger' | 'link'` (default `primary`).
- `size`: `'sm'` (32px, dense web only) · `'md'` (44px, default, the mobile touch target) · `'lg'` (52px, mobile CTAs).
- `iconLeft` / `iconRight`: an icon name from `TS.Icon.names` or a node.
- `loading`: shows a spinner and disables the button. `block`: full width (mobile forms and sheets).
- `as`: render as another element (e.g. `'a'` with `href`).
- Everything else goes to the `<button>`.

## Do / don't
- Label with a verb in sentence case: "Save changes", not "OK" or "SAVE".
- On mobile, put the primary action full-width (`block`, `size="lg"`) at the bottom of the screen or sheet.
- Don't place two primary buttons side by side; don't use danger for "Cancel".
