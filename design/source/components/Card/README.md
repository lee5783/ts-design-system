# Card

Groups related content and actions on `surface-card`, separated from the background by a `border-subtle` outline.

## Props
- `title`, `subtitle`, `actions` (top-right node: a Badge or IconButton), `children` (body), `footer` (right-aligned buttons).
- `raised`: adds `shadow-sm`. `interactive`: hover lift (`shadow-md`) for clickable cards; also pass `onClick` and make the title a link.

## Do / don't
- Padding is `space-4`; gap between cards `space-4` on mobile, `space-6` on web.
- Don't nest cards inside cards; use a divider or a ListItem group instead.
