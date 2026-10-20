# Changelog

All notable changes to the TS Design System libraries (iOS and Android, released in lockstep) are listed here.

## 0.2.1 - 2026-10-20

### Documentation

- Add JitPack badge to README

### Tests

- **demos:** Fix page walks, UIKit toast theme flip and catalog order
- **ci:** Expect the demos workflow on one OS version per platform

### Maintenance

- **demos:** Run demo UI tests on one OS version per platform

## 0.2.0 - 2026-10-19

### Features

- **tokens:** Add controller and media-overlay colour roles
- **text-field:** [**breaking**] Add code content style and code-entry input traits
- **button:** Accept consumer icons and label line control
- **components:** [**breaking**] Make spinner, divider, disclosure row and text public
- **android:** [**breaking**] Distribute through JitPack only

### Fixes

- **demos:** Make the demo UI tests pass on API 26 and stop the iOS toast race
- **dialog:** [**breaking**] Queue a second dialog, run actions before dismissal, keep immersive mode
- Keep UIKit text reachable and restore 10 s action toasts

### Refactoring

- **android:** [**breaking**] Remove the screen-reader accessibility layer
- **ios:** [**breaking**] Remove the screen-reader accessibility layer
- [**breaking**] Move identifiers to com.tinysol

### Documentation

- Document consumer icons, code fields, dialog queue and new primitives
- Document the accessibility removal and trim component specs
- Drop snapshot and screen-reader docs, document targeted iOS runs
- Document accent-text contrast per surface
- Describe SPM and JitPack distribution and new identifiers

### Tests

- **demos:** Dismiss a lingering soft keyboard before tapping the text field
- **demos:** Wait for the device night-mode flip in systemFollowsTheDeviceAgain
- **demos:** Wait for every theme pixel check in the Compose ThemeOverrideTest
- **demos:** Drive demo UI tests by identifiers and visible state

### Maintenance

- **release:** Release on a pushed vX.Y.Z tag and attach Android packages
- **test:** Remove snapshot capture and run iOS tests without rebuilding
- **test:** Remove accessibility audits and contrast tooling
- **android:** Drop unused semantics imports
- **demos:** Drop demo strings left unused by the accessibility removal

## 0.1.0 - 2026-10-17

### Features

- **design:** Add TinySolution DS source, component specs, assets and token codegen
- **ios:** Add TSDesignSystem (SwiftUI), TSDesignSystemUIKit and TSTokens Swift packages
- **android:** Add tokens, core, Compose and Views libraries with BOM and publishing
- **demos:** Add SwiftUI, UIKit, Compose and Views demo apps

### Documentation

- Add README, docs corpus, guides and consumer docs
- Restructure README with overview, requirements, layout and contributing

### Maintenance

- Add Makefile, toolchain pins and tools workspace
- Add GitHub Actions workflows, API checks, release tooling and Renovate

### Other

- Initial commit
