TinySolution (short name **TS**) is a calm, neutral interface system for web and mobile apps: zinc greys, one violet accent, platform system type, and small modular parts that fit together on a 4px grid. One token set drives both platforms; px on web equal pt on iOS and dp/sp on Android.

## Principles

- **Neutral first, violet with intent.** Screens are greys; `accent-primary` marks the one thing to do next and the current selection. If everything is violet, nothing is.
- **Native feel.** Use the platform's own typeface and patterns (bottom nav and sheets on phones, sidebar and centred dialogs on desktop) so the app feels at home on every device.
- **Small parts, done well.** Prefer a few composable components over many special cases. A screen is mostly ListItems, Cards, Buttons and TextFields.
- **Readable in both themes.** Every text token names the grounds it is legible on; light and dark are designed together, not inverted.

## Content fundamentals

- Write in plain, direct English. Address the user as "you"; the product speaks as "we" only in system messages ("We couldn't sync your files").
- Sentence case everywhere: buttons, titles, tabs, menu items ("Save changes", "Recent activity"). Uppercase only via the `overline` style.
- Buttons start with a verb and name the result: "Create project", "Delete", "Send invite". Avoid "OK", "Yes", "Submit".
- Errors say what happened and how to fix it: "Must be at least 8 characters", "Check your connection and try again".
- Toasts are past tense with no period: "Project archived". Dates are short ("Oct 30"); relative time for the last 24 hours ("2m", "3h").
- No emoji in UI copy; no exclamation marks except in a genuine celebration.

## Color

Set the canvas to `background-primary`; put content on `surface-card` with a `border-subtle` outline; use `background-secondary` for recessed regions (sidebars, grouped lists, table headers). Floating layers use `surface-elevated` + `shadow-lg` over the `overlay` scrim.

- Text: `text-primary` for headings and body, `text-secondary` for supporting copy, `text-muted` for metadata, hints and placeholders, `text-disabled` only for disabled controls.
- Accent: fill primary buttons and selected controls with `accent-primary`, label them with `text-on-accent`; hover `accent-hover`, pressed `accent-pressed`. For violet **text** (links, active tab, active nav label) always use `accent-text`; the raw violet is too dark to read as text on the dark canvas. Tint selections with `accent-subtle`.
- Status: `success`, `warning`, `danger`, `info` for text and icons on their `-subtle` backgrounds; `danger-solid` fills destructive buttons. Status is never colour alone: pair it with an icon (Alert, TextField error) or a word (Badge).
- Interaction states: hover `surface-hover`, pressed `surface-pressed`, disabled `opacity-disabled` on the whole control.
- Never hard-code hex in product code; reference the token so dark mode follows.

Known contrast exceptions (kept from the source palette): `text-muted` on `background-secondary` in light is 4.4:1, so use it there at 14px+ only; `accent-primary` as a fill in dark is 2.9:1 against the canvas, so a violet fill always carries a white label or check mark.

## Typography

One family, the platform's own: SF Pro (iOS, macOS), Roboto (Android), Segoe UI (Windows), via the `sans` stack; `mono` for code and IDs. No web fonts to load.

- `title-1` screen titles, `title-2` section and dialog titles, `title-3` card and nav-bar titles; `display` only for hero numbers and marketing (web/tablet).
- `body` (16px) is the default for copy and every input value, so iOS never zooms on focus. `body-sm` for dense web tables and list subtitles, `body-lg` for long reading.
- `label` for buttons, form labels and tabs; `caption` for hints, timestamps and bottom-nav labels; `overline` (uppercase) for list-group headings.
- Mobile: respect Dynamic Type / font scaling; never lock line heights so tight that scaled text clips.

## Spacing and layout

A 4px grid. Default padding is `space-4` (cards, list rows, the mobile screen gutter); `space-6` for web card padding, dialogs and the desktop gutter; `space-5` between form fields; `space-8` (mobile) / `space-10` (web) between page sections.

- Heights: `control-md` (44) is the default for buttons, inputs and list rows and the minimum touch target on mobile; `control-sm` (32) only in dense web toolbars; `control-lg` (52) for full-width mobile CTAs.
- App chrome: top bar `nav-bar` (56), bottom nav `tab-bar` (64) plus the safe-area inset.
- Breakpoints: below `bp-md` use the mobile patterns (BottomNav, sheet Dialogs, full-width buttons); from `bp-md` a side rail; from `bp-lg` a persistent sidebar; cap content at `content-max`.

## Shape and elevation

Radii grow with the object: `radius-xs` checkboxes and code, `radius-sm` badges and small buttons, `radius-md` buttons and inputs, `radius-lg` cards, alerts and toasts, `radius-xl` dialogs and sheet tops, `radius-full` avatars, switches and pills.

Elevation is mostly borders. Cards sit flat with `border-subtle` (add `shadow-sm` only for emphasis); `shadow-md` for hover and menus; `shadow-lg` for dialogs, sheets and toasts. In dark mode, elevation comes from lighter surfaces (`surface-card`, `surface-elevated`) more than from shadows.

## Focus and accessibility

- Keyboard focus is `shadow-focus`: a 2px canvas-coloured gap, then a solid 2px `focus` ring (5.5:1+ on every ground in both themes). Show it on `:focus-visible` only; never remove it.
- Text meets 4.5:1 on the grounds its token note names in both themes; control edges use `border-strong` (3:1+).
- Every icon-only control has a label (`IconButton` requires one); touch targets are at least 44×44.
- Motion is short and functional (120–200ms colour and position transitions); honour reduced-motion settings.

## Iconography

Use the TS icon set (Icons asset group, and `TS.Icon` in code): 33 outline icons on a 24px grid, 1.75px stroke, round caps and joins, no fills except tiny dots. 20px (`icon-md`) inside buttons, inputs and rows; 24px (`icon-lg`) in app bars and the bottom nav; 16px (`icon-sm`) in badges and field messages. Colour icons with text tokens, usually `text-secondary`, `text-muted` or `accent-text`. On native, import the SVGs into the Xcode asset catalog (template rendering) or as Android Vector Drawables tinted by the theme. No emoji as icons.

## Logo

The Logos group holds a placeholder identity set in the system font: the `ts-monogram` (white "TS" on a violet rounded square), two wordmarks (`ts-wordmark-light` for light grounds, `ts-wordmark-dark` for dark grounds) and `ts-app-icon` (1024px, square, unmasked: iOS and Android apply their own corner mask). Replace them when a final logo exists. Give the logo clear space of at least the monogram's height ÷ 4 and never recolour the violet.

## Components

Built for React 18 in `components/bundle.js` as `window.TS`, styled by `components/bundle.css`, all values from the tokens.

- Actions: `Button`, `IconButton`.
- Forms: `TextField`, `Checkbox`, `Switch`.
- Navigation: `Tabs` (underline and segmented), `BottomNav`.
- Data display: `Badge`, `Avatar`, `ListItem` (wrap rows in `.ts-list`).
- Containers: `Card`. Overlays: `Dialog` (centred or bottom sheet).
- Feedback: `Alert`, `Toast`. Foundations: `Icon`.

Native apps implement the same components with SwiftUI / Jetpack Compose using the identical tokens, sizes and states; the web previews are the reference.
