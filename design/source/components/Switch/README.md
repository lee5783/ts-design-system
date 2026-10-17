# Switch

Turns a single setting on or off immediately, the mobile-native toggle (also used on web settings pages).

## Props
`label`, plus native checkbox props (`checked`, `defaultChecked`, `onChange`, `disabled`); it renders `role="switch"`.

## Do / don't
- Use for instant effects only; if the change needs a Save button, use Checkbox.
- In settings lists, place the Switch as the `trailing` of a ListItem.
