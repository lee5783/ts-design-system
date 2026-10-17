# TextField

A labelled text input with optional hint, error message and leading icon; `multiline` turns it into a textarea.

## Props
- `label` (always provide one; placeholders are not labels), `hint`, `error` (replaces the hint, turns the border `danger`, adds an alert icon and `aria-invalid`).
- `leadingIcon`, `trailing` (a node, e.g. an IconButton to show a password), `multiline`.
- Everything else goes to the `<input>`: `type`, `value`, `onChange`, `placeholder`, `disabled`, `inputMode`, `autoComplete`.

## Do / don't
- Input text is 16px (`body`) so iOS Safari never zooms on focus.
- Write errors as a fix: "Must be at least 8 characters", not "Invalid".
- Stack fields with `space-5` between them; full width on mobile.
