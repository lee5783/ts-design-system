# Alert

An inline message about the state of a page or section, persistent until the problem is resolved or dismissed.

## Props
`tone`: `'info'` (default) · `'success'` · `'warning'` · `'danger'`; `title`; `children` (one sentence); `icon` (override); `action` (a small ghost Button or IconButton).

## Do / don't
- Every tone has its own icon, so status never depends on colour alone.
- Title says what happened, body says what to do next.
- For transient confirmations ("Saved"), use Toast instead.
