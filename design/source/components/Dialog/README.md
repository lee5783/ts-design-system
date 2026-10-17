# Dialog

A modal that asks for a decision or a short input. `variant="sheet"` makes it a bottom sheet, the mobile pattern.

## Props
- `title` (required), `children` (body), `actions` (buttons, primary last).
- `open`, `onClose` (called on scrim click; also close on Escape / back gesture in your app).
- `variant`: `'center'` (default; web and tablet) · `'sheet'` (mobile, below `bp-md`).
- `contained`: positions the scrim inside the nearest positioned parent instead of the viewport.

## Do / don't
- Title is a question for confirmations ("Delete this project?"); the buttons answer it ("Delete", not "Yes").
- Keep it to one task; never stack dialogs.
