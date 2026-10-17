# IconButton

A square button holding only an icon, for toolbars, app bars and row actions. `label` is required: it becomes the accessible name and the hover tooltip.

## Props
- `icon`: icon name or node. `label`: what it does ("Search", "More options").
- `variant`: `'ghost'` (default) · `'secondary'` · `'primary'`. `size`: `'md'` (44px) · `'sm'` (32px, web only). `round`: pill shape, for a floating action button on mobile.

## Do / don't
- Use only icons whose meaning is unambiguous (close, search, more); otherwise use a Button with a label.
- Keep 44×44 touch targets on mobile: don't use `size="sm"` there.
