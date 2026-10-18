# Changelog

All notable changes to the TS Design System libraries (iOS and Android, released in lockstep) are listed here.

## Unreleased

### Breaking

- **distribution:** Ship through two channels only: Swift Package Manager (iOS) and JitPack (Android). GitHub Packages publishing is removed.
- **android:** New consumer coordinates `com.github.lee5783.ts-design-system:<module>:<tag>` (`bom`, `compose`, `views`, `core`, `tokens`) from `https://jitpack.io`, built from the release tag by the new root `jitpack.yml`. No credentials are needed. `v0.1.0` cannot be built by JitPack; the next tag is the first JitPack release.
- **android:** Rename Kotlin packages and namespaces from `com.ts.ds.*` to `com.tinysol.designsystem.*` (`tokens`, `core`, `views`, `compose`); the local Maven group is `com.tinysol.designsystem`.

### Fixes

- **android:** Published POMs point at `https://github.com/lee5783/ts-design-system` (project, SCM, issues, licence `blob/master/LICENSE`) from the new `ds.pom.repository` property, instead of `github.com/unconfigured` (#7).
- **demos:** The Views demo settings sheet Reset action is a TS secondary button, so its label is legible on the dark sheet (#8).

### Documentation

- Per-surface text contrast table and guarantees for every text role, `accentText` in the colour example, and a Material-interop rule: accent text uses `accentText`, never Material `primary` (#8).
- JitPack consumer setup, release steps and the first-JitPack-build step.

### Maintenance

- **demos:** New app ids: Android `com.tinysol.designsystem.compose.sample` and `com.tinysol.designsystem.views.sample`; iOS `com.tinysol.design-system.sample` (UIKit) and `com.tinysol.design-system-swiftui.sample` (SwiftUI). Uninstall the old `com.ts.ds.demo.*` apps; they register the same `tsds-*` URL schemes.
- **ios:** `TSDialogPresenter` logs under the subsystem `com.tinysol.design-system`. SPM package, product and module names are unchanged.
- **release:** The GitHub release carries the changelog notes and the two debug demo APKs; library AARs are no longer attached. The `android-publish` Make target and the `publish-android` job are removed; `android-publish-local` stays for development.

## 0.1.0 - 2026-10-17

### Features

- **design:** Add TinySolution DS source, component specs, assets and token codegen
- **ios:** Add TSDesignSystem (SwiftUI), TSDesignSystemUIKit and TSTokens Swift packages
- **android:** Add tokens, core, Compose and Views libraries with BOM and publishing
- **demos:** Add SwiftUI, UIKit, Compose and Views demo apps

### Documentation

- Add README, docs corpus, guides and consumer docs

### Maintenance

- Add Makefile, toolchain pins and tools workspace
- Add GitHub Actions workflows, API checks, release tooling and Renovate

### Other

- Initial commit
