# Toast

A brief, non-blocking confirmation on `surface-inverse` that disappears after about 4 seconds (snackbar on mobile).

## Props
`children` (one short sentence), `tone` (adds a status icon), `actionLabel` + `onAction` (one action, e.g. "Undo"). The consumer positions it (bottom-centre on mobile above the BottomNav, bottom-right on web, `z-toast`) and handles the timer.

## Do / don't
- Past tense, no period: "Project archived".
- Never put errors that need action in a toast; use Alert or a Dialog.
