# ListItem

One row of a list: settings, people, files, search results. The core building block of mobile screens. Wrap rows in a `div.ts-list` for the grouped card style with dividers.

## Props
- `title` (required), `subtitle`.
- `leadingIcon` (icon in an accent tile) or `leading` (any node, e.g. Avatar).
- `trailing` (meta text, a Badge or a Switch), `chevron` (navigates somewhere).
- `onClick` renders a `<button>`, `href` an `<a>`; otherwise a plain `<div>`.

## Do / don't
- Minimum height 44px; title on one line, truncated.
- Use `chevron` only when the row opens another screen.
