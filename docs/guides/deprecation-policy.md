# Deprecation policy and API stability

The public surface of both platforms is versioned together with one `VERSION` (semver). This page says what counts as breaking, how to deprecate, and how the API checks enforce it.

## What is public

- **iOS:** the products `TSDesignSystem`, `TSDesignSystemUIKit` and `TSTokens`, and everything they re-export from `TSFoundation` (style specs, logic, helpers are public by decision C-44 U-2). `package` declarations (the `Compat/` shims) are not public.
- **Android:** every public declaration of `com.ts.ds:{tokens,core,compose,views}` (Kotlin explicit API mode), including the `:core` style specs (C-43), plus the XML attributes, styles and resources of `:views`.
- **Not checked by any tool:** resource API on both platforms (string keys such as `ts.alert.info` / `ts_alert_info`, asset and drawable names, `app:ts*` attribute names, style names). Treat renaming one as breaking by hand.

## Version levels

| Level | Covers |
|---|---|
| **Major** | Removing or renaming API or a token without an alias; changing behaviour a consumer relies on; raising a minimum OS (iOS 15, API 26); a `swift-tools-version` beyond the supported Xcode range |
| **Minor** | New components, tokens, API; token value changes; deprecations; escalating a Kotlin deprecation to `ERROR` |
| **Patch** | Fixes with no API change |

Before 1.0, a minor release may break; consumers should pin `.upToNextMinor` / an exact version ([../consumers/ios-getting-started.md](../consumers/ios-getting-started.md)).

## How to deprecate

1. **Deprecate in a minor release**, always with a replacement:
   - Swift: `@available(*, deprecated, renamed: "newName")` (or `message:` when there is no one-to-one replacement).
   - Kotlin: `@Deprecated("Use newName.", ReplaceWith("newName"), level = DeprecationLevel.WARNING)`.
   - Tokens: add `"old-name": "new-name"` to `renames` in `design/extensions/native-tokens.json`; codegen generates the deprecated alias on every platform with these same annotations ([ds-update-flow.md](ds-update-flow.md)).
2. **Escalate** the Kotlin level to `ERROR` in a minor release at least three months later.
3. **Remove** in the next major release, with an entry in [../consumers/migration-guides.md](../consumers/migration-guides.md).

Update the component spec (`design/specs/components/<name>.md`) in the same change.

## Experimental API

Unstable API is opt-in and excluded from the checks:

- **Kotlin:** annotate with `@ExperimentalTSApi` (`android/core/src/main/kotlin/com/ts/ds/core/ExperimentalTSApi.kt`, `RequiresOptIn` level `ERROR`). Consumers opt in with `@OptIn(ExperimentalTSApi::class)`. The binary-compatibility validator lists the annotation as a non-public marker (`DsApiValidationPlugin.kt`), so declarations that carry it are left out of the dumps; code that merely opts in is still checked.
- **Swift:** declare with `@_spi(Experimental) public`; consumers write `@_spi(Experimental) import TSDesignSystem`. The iOS check ignores the `Experimental` SPI group. The demos must not use `@_spi` (boundary check).

Experimental API can change or disappear in any release without a deprecation cycle.

## The checks

```bash
make api-check BASELINE=<tag-or-ref>   # iOS against the baseline + Android against the in-tree dumps
make ios-api-check BASELINE=<ref>      # iOS only
make android-api-check                 # Android only (./gradlew apiCheck)
make ios-api-check-last-tag            # iOS against the latest X.Y.Z tag before HEAD; skips when there is none
```

- **Android:** `apiCheck` compares the compiled API with `android/<module>/api/<module>.api`. After an intended change, run `cd android && ./gradlew apiDump` and commit the dumps; the diff is the review record. `make android-build` runs `apiCheck` too.
- **iOS:** `tools/api-check/src/ios.ts` builds every library module for the simulator triple at the baseline and at HEAD, dumps both with `swift-api-digester`, and diagnoses the difference. It also diffs the SPM library products (a removed or renamed product is a `Package:` break). It exists because `swift package diagnose-api-breaking-changes` cannot dump this iOS-only package (see [../concerns.md](../concerns.md)).
  - `BASELINE` is required; before the first tag use a branch or commit (`BASELINE=HEAD`).
  - A baseline commit without `Package.swift` has no Swift API yet, so iOS is skipped with a notice.
  - Acknowledge an intended break with `make ios-api-check-record BASELINE=<ref>` and commit `ios/api/breaking-changes.txt` ([release.md](release.md#api-acknowledgements)).
- **CI:** `ios.yml` runs `ios-api-check` against `origin/<base branch>` on pull requests and `ios-api-check-last-tag` on every other event (pushes to `master`, dispatch, the release); `android.yml` runs `apiCheck` in its build job. Neither enforces a major bump when a break is acknowledged (tracker D-012); the reviewer checks it.
