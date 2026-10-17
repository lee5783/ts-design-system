# Logo outline font

`roboto-variable.ttf` is used by `tools/codegen` to convert the `<text>` in `design/assets/logos/source/*.svg` into outlines (`wght` axis set from each element's `font-weight`). It is a build input only; no font ships in the iOS or Android libraries.

- Font: Roboto variable (`wght` 100–900, `wdth` 75–100, `ital` 0–1), version 3.011.
- Copyright 2011 The Roboto Project Authors (https://github.com/googlefonts/roboto-classic).
- License: SIL Open Font License 1.1 (https://openfontlicense.org). The file is unmodified and carries the license in its `name` table.
- Taken from the Android Studio layoutlib font set (`plugins/design-tools/resources/layoutlib/data/fonts/Roboto-Regular.ttf`), sha256 `9e157d5ff70ae52cc8c527b36e89d9b3c69c6c814da79fa6237a0bf0895abc7c`.

**Placeholder.** The DS sets the logos "in the system font" (SF Pro on iOS, Roboto on Android). SF Pro cannot be redistributed, so Roboto stands in on both platforms until outlined logo artwork exists. Replace the logo sources with outlined SVGs, or swap this file, and run `make codegen`.
