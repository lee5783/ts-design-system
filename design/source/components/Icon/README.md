# Icon

The TinySolution icon set as a React component: 33 outline icons on a 24px grid, 1.75px stroke, round caps and joins, drawn in `currentColor` so they take the text colour around them.

## Props
`name` (one of `TS.Icon.names`), `size` (16 · 20 default · 24, see `icon-sm/md/lg`), `strokeWidth`, `label` (give one only when the icon stands alone and carries meaning; otherwise it is hidden from screen readers).

## Do / don't
- Same set on web and mobile. 20px in buttons, inputs and rows; 24px in app bars and the bottom nav.
- Colour icons with text tokens (`text-secondary`, `text-muted`, `accent-text`), never with raw hex.
- The SVG files are in the Icons asset group for native apps (import into Xcode asset catalogs or Android Vector Drawables).
