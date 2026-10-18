# TS Design System

**Analysis Date:** 2026-10-18

## Project Summary

- One-liner: native iOS and Android libraries for the TinySolution (TS) design system, built for **SwiftUI, UIKit, Jetpack Compose and Android Views** from one checked-in copy of the design source.
- Primary capabilities:
  - Generated tokens (color light/dark, type, spacing, radius, shadow, size, opacity) and icons for both platforms.
  - 15 components in all four toolkits: Alert, Avatar, Badge, BottomNav, Button, Card, Checkbox, Dialog, Icon, IconButton, ListItem, Switch, Tabs, TextField, Toast.
  - Light and dark, Dynamic Type and font scale, 44 pt / 48 dp touch targets, RTL.
  - Four demo apps, one per toolkit, each showing every token and every component variant, size and state.
- Who it is for: iOS and Android app teams adopting the TS design system, and the maintainers who keep the libraries in step with the design source.

One version covers both platforms (`VERSION`, currently `0.1.0`; history in `CHANGELOG.md`).

## Goals and Non-Goals

- Goals:
  - One design source (`design/`) feeding generated tokens and assets for both platforms, so a DS update is a sync plus codegen, not hand edits.
  - Native components per toolkit with the same tokens, sizes, states and behaviour contract (`design/specs/components/<name>.md`).
  - A stable public API from the first tag, guarded by API checks on both platforms.
- Non-goals (from `docs/stack.md`): CocoaPods, Maven Central, Kotlin Multiplatform and web distribution. iOS ships through SPM only, Android through JitPack only.

## Requirements

- Runtime/Environment:
  - Consumers: iOS 15+ (SwiftUI or UIKit apps); Android API 26+ (Compose or Views apps).
  - Contributors: macOS with Xcode 26.6 (`.xcode-version`), JDK 21 and Node 22 (`mise.toml`; `mise install`).
- Build/Tooling:
  - Swift Package Manager (`Package.swift`, `ios/`).
  - Gradle with a convention-plugin build (`android/build-logic/`).
  - A Node/Vitest tools workspace (`tools/`) for sync, codegen, CI and release.
  - `make` as the single entry point for every task (`Makefile`).
- External services:
  - JitPack (`https://jitpack.io`), which builds the Android artifacts from a release tag with the root `jitpack.yml`; no token needed.
  - GitHub Actions for CI and release (`.github/workflows/`).
  - Renovate for dependency PRs (`.github/renovate.json`).
- Configuration notes:
  - No credentials are needed to consume either platform.
  - POM metadata, including the repository the POM URLs point at (`ds.pom.repository`), is in `android/gradle.properties`.

## Getting Started

### Install

**iOS (Swift Package Manager):**

```swift
.package(url: "https://github.com/lee5783/ts-design-system.git", exact: "0.1.0")
```

| Product | For |
|---|---|
| `TSDesignSystem` | SwiftUI |
| `TSDesignSystemUIKit` | UIKit |
| `TSTokens` | tokens only |

```swift
.product(name: "TSDesignSystem", package: "ts-design-system")
```

**Android (JitPack):** add the repository to `settings.gradle.kts`. No token is needed.

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") {
            content { includeGroup("com.github.lee5783.ts-design-system") }
        }
    }
}
```

Then the dependencies in the app's `build.gradle.kts`, with the release tag (`v` included) as the version:

```kotlin
implementation(platform("com.github.lee5783.ts-design-system:bom:<tag>"))
implementation("com.github.lee5783.ts-design-system:compose")   // or ":views"
```

Kotlin packages are `com.tinysol.designsystem.*` (for example `com.tinysol.designsystem.compose.theme.TSTheme`). `v0.1.0` predates the JitPack setup; the first JitPack-consumable version is the next tag.

### Configure

- **Android:** full setup, first screen and local testing against `~/.m2`: `docs/consumers/android-getting-started.md`.
- **iOS:** setup, including private-repository access: `docs/consumers/ios-getting-started.md`.
- **Theming, Material interop and string overrides:** `docs/consumers/theming.md`.

### Run (Development)

Contributor setup:

```bash
mise install          # Node 22, JDK 21, git-cliff
make tools-deps       # install the tools workspace
make help             # list every target
```

The demo apps live in `demos/` and consume the library like an external app:

| App | Toolkit | Run |
|---|---|---|
| `demos/ios-swiftui` | SwiftUI | `make demo-ios-swiftui-run` |
| `demos/ios-uikit` | UIKit | `make demo-ios-uikit-run` |
| `demos/android-compose` | Compose | `make demo-android-compose`, then install from Android Studio or `./gradlew :app:installDebug` |
| `demos/android-views` | Views | `make demo-android-views`, then install the same way |

Launch options, the in-app theme toggle and the theme deep links are in `docs/guides/running-demos.md`.

### Run (Production)

Not applicable: this repo ships libraries. A maintainer bumps `VERSION` on a branch, merges it, then pushes a `vX.Y.Z` tag on `master`. SPM resolves that tag directly and JitPack builds the Android artifacts from it on first request. The tag push starts the `release` workflow (`.github/workflows/release.yml`), which runs every gate and then creates the GitHub release with the changelog notes and the two debug demo APKs. Process: `docs/guides/release.md`.

### Test

```bash
make test                     # tools, codegen and sync checks, demo boundaries, iOS and Android suites
make ios-test                 # iOS unit and behaviour tests (local default: the pinned simulator in .ios-simulator)
make android-test             # Android unit and behaviour tests
make api-check BASELINE=<tag> # public API compatibility on both platforms
```

Run only the suites you touched: `make ios-build-for-testing` once, then `IOS_TESTS="<Target/Suite ...>" make ios-test-only`; on Android, `./gradlew :<module>:testDebugUnitTest --tests '<class>'` from `android/`. There is no snapshot testing and CI compares no pixels; visual changes are shown with screenshots in the PR. Details: `docs/testing.md`.

## Project Layout

```
ts-design-system/
├── `design/`      - DS source copy, normalized assets, local token extensions, component specs (`design/specs/components/`)
├── `tools/`       - Node 22 / Vitest workspace: DS sync, token codegen, CI, release, API and demo checks
├── `ios/`         - Swift package sources and tests (`TSTokens`, `TSFoundation`, `TSDesignSystem`, `TSDesignSystemUIKit`)
├── `Package.swift`- SPM manifest for the iOS products
├── `android/`     - Gradle project: `tokens`, `core`, `compose`, `views`, `bom`, `golden-check`, `build-logic`
├── `demos/`       - Four demo apps plus a shared demo-support module per platform
├── `docs/`        - Architecture, conventions, testing, maintainer guides and consumer pages
├── `.github/`     - CI and release workflows, Renovate, CODEOWNERS, PR template
└── `Makefile`     - Entry point for every build, test, codegen, demo and release task
```

## Documentation Map

- `docs/README.md`: entry point and routing table for the whole corpus.
- **Reference pages:**
  - `docs/structure.md`: where code lives.
  - `docs/architecture.md`: layers and data flow.
  - `docs/conventions.md`: naming and style.
  - `docs/testing.md`: test suites.
  - `docs/integrations.md`: CI, JitPack distribution, release jobs.
  - `docs/stack.md`: versions.
  - `docs/concerns.md`: known risks.
- **Maintainer guides (`docs/guides/`):**
  - `ds-update-flow.md`, `adding-a-component.md`
  - `release.md`, `deprecation-policy.md`
  - `toolchain-upgrades.md`, `running-demos.md`
- **Consumer pages (`docs/consumers/`):** `ios-getting-started.md`, `android-getting-started.md`, `theming.md`, `migration-guides.md`.
- **Component contracts:** `design/specs/components/<name>.md`.
- `docs/features/INDEX.md`: not present. Components are documented through the specs above.

## Contributing

- Branch from `master` and open a PR. Fill in `.github/pull_request_template.md`.
- The checks to pass are `codegen-check-ok`, `ios-ok`, `android-ok` and `demos-ok`.
- Use conventional commit messages (`feat(ios): …`, `fix(android): …`, `docs: …`). `CHANGELOG.md` is generated from them by git-cliff (`cliff.toml`).
- Generated code is regenerated with `make codegen`, never edited by hand.
- Public API changes must pass `make api-check`; Android changes need updated `api/*.api` dumps.
- Visual changes are shown with screenshots in the PR's Evidence section: the pinned iOS simulator (`.ios-simulator`) and one Android emulator at API 36.
- Updating the design system: `docs/guides/ds-update-flow.md`. Adding a component: `docs/guides/adding-a-component.md`.

## License

MIT, see `LICENSE`.
