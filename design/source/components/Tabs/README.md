# Tabs

Switches between views of the same content. **underline** for page-level sections (web and mobile top tabs); **segmented** for 2–4 short options that filter one view (iOS-style segmented control).

## Props
- `items`: `[{id, label}]`. `value` + `onChange` (controlled) or `defaultValue`.
- `variant`: `'underline'` (default) · `'segmented'`. `block`: segmented fills its container. `label`: accessible name of the tab list.

## Do / don't
- One or two words per tab, no icons in segmented controls.
- Don't use Tabs for steps in a flow, or for app-level destinations (that is BottomNav).
