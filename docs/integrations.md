# External integrations

**Analysis date:** 2026-10-16

The library has no runtime service integration (no network, analytics or auth). Every integration below is build, CI or distribution infrastructure.

## APIs and external services

**DS artifact (source of truth):** the TinySolution artifact recorded in `design/manifest.json` (`artifact.id`, `url`, `version`). Reading it needs an authenticated claude.ai session, so the copy step is agent-assisted or a manual export; CI only verifies the copy (`make ds-sync`). Procedure: [guides/ds-update-flow.md](guides/ds-update-flow.md) and `tools/ds-sync/README.md`.

**Image loading:** the library ships no image loader (no-loader slot pattern). Load images in the app and hand the result to the component:

- SwiftUI: `AsyncImage(url:) { phase in TSAvatar(name:phase:) }`, or `TSAvatar(name:image:)` with a `UIImage`.
- UIKit: `TSAvatarView.image = UIImage`.
- Compose: `TSAvatar(name, image = painter)` with a `Painter` once it has loaded.
- Views: `TSAvatarView.image = drawable`.

Until an image is set, or when it cannot be used, the avatar shows initials.

## Data storage

Not applicable. Demo settings persist in `UserDefaults` / SharedPreferences inside the demo-support modules only.

## Authentication and identity

**GitHub Packages (Android distribution).** Maven repository `https://maven.pkg.github.com/lee5783/ts-design-system`. Reading needs a token even for public packages; the package inherits the repository's visibility. Three credential paths exist, with different property names:

| Who | Where configured | Credentials |
|---|---|---|
| CI publish (`release.yml` → `make android-publish`) | `android/build-logic/convention/src/main/kotlin/DsPublishingPlugin.kt`, repository name `GitHubPackages` | env `GITHUB_ACTOR` / `GITHUB_TOKEN` (the Actions token with `packages: write`) |
| An external consumer app | the consumer's `settings.gradle.kts` (snippet in [consumers/android-getting-started.md](consumers/android-getting-started.md)) | Gradle properties `gpr.user` / `gpr.key` in `~/.gradle/gradle.properties`, falling back to env `GITHUB_ACTOR` / `GITHUB_TOKEN` |
| This repo's demos in published mode (`-Pds.useLocal=false` without `ds.mavenLocal`) | `demos/android-{compose,views}/settings.gradle.kts`, repository name `GitHubPackages`, `credentials(PasswordCredentials::class)` | Gradle properties `GitHubPackagesUsername` / `GitHubPackagesPassword` (or env `ORG_GRADLE_PROJECT_GitHubPackagesUsername` / `…Password`) |

The consumer names (`gpr.*`) are a common community convention; the demo names come from Gradle's `PasswordCredentials` naming rule (`<repositoryName>Username` / `Password`). Both work; they are not interchangeable. A demo build without its properties fails fast with "The following Gradle properties are missing for 'GitHubPackages' credentials".

The repository path comes from `GITHUB_REPOSITORY`, then `-Pds.githubRepository=<owner>/ts-design-system`, then the placeholder `unconfigured/ts-design-system` (local POMs only).

**Demo library switch** (`demos/android-*/settings.gradle.kts`):

| Property | Default | Effect |
|---|---|---|
| `ds.useLocal` | `true` (`gradle.properties`) | `includeBuild("../../android")` substitutes `com.ts.ds:*` with the in-tree modules |
| `ds.mavenLocal` | `false` | With `ds.useLocal=false`: resolve `com.ts.ds` from `~/.m2` (after `make android-publish-local`), no credentials |
| `ds.version` | `../../VERSION` | The BOM version the demo requests |
| `ds.githubRepository` | `GITHUB_REPOSITORY` | Owner/repo of the GitHub Packages URL |

iOS demos always use local package references (`../..`); a remote reference fails `make demos-check-boundaries`.

**POM metadata:** `android/gradle.properties` holds `ds.pom.organizationName`, `ds.pom.developerId`, `ds.pom.developerName`. The licence name is read from the first line of `LICENSE`. The developer id is `lee5783`. No javadoc jar is published; a sources jar is.

## Monitoring and observability

Not applicable at runtime. CI failure artifacts (14-day retention):

| Artifact | Job | Contents |
|---|---|---|
| `ios-test-pinned`, `ios-test-oldest` | `ios.yml` `test` | `.build/xcode/Logs/Test` |
| `android-test-reports` | `android.yml` `test` | test reports and results |
| `android-demo-<demo>-api-<level>` | `demos.yml` `android-demo` | demo test reports and `androidTest-results` |

## CI/CD and deployment

**CI pipeline:** GitHub Actions, `.github/workflows/`. Every `run:` step is a single `make` call; values reach make only through `env:` (a workflow test forbids `${{ }}` inside `run:`).

| Workflow | Triggers | Jobs | Aggregator (required check) |
|---|---|---|---|
| `codegen-check.yml` | PR, push to `master`, dispatch, `workflow_call` | `tools`: `tools-test`, `actionlint`, `codegen-check`, `ds-sync` | `codegen-check-ok` |
| `ios.yml` | same | `changes`; `test` (matrix `pinned`, `oldest`; snapshot suites skip); `api-check` (`ios-api-check` against the PR base, `ios-api-check-last-tag`) | `ios-ok` |
| `android.yml` | same | `changes`; `build` (`assemble lint apiCheck`, Ubuntu); `test` (`android-test`, snapshots render without comparing, macOS) | `android-ok` |
| `demos.yml` | same | `changes`; `boundaries`; `ios-demo-support`; `ios-demo` (per demo × runtime, light, then the dark `ThemeOverrideTests` on the pinned leg); `android-demo` (per demo × API 26, 36 on emulators) | `demos-ok` |
| `demos-audit.yml` | nightly 02:00 UTC, PR `labeled` / `synchronize` with label `a11y-audit`, dispatch | `ios-audit` per iOS demo | none (not required) |
| `release.yml` | push of a `vX.Y.Z` tag | `verify` → the 4 gate workflows (reusable) → `publish-android` + `android-packages` (demo APKs, library AARs) → `github-release` | none |

- **Path areas** (`tools/ci/src/changes.ts`, `make ci-changes BASE=<sha>`): `ios`, `android`, `boundaries` and one area per demo. `Makefile`, `.gitattributes`, `tools/package*.json` and `tools/ci/**` touch every area; `design/**` and `tools/codegen/**` touch both libraries and all demos; `design/specs/components/**` touches `boundaries`. To route a new path, add it to `AREA_PATTERNS` and extend `tools/ci/test/changes.test.ts`. Without a base commit (a tag push, a new branch), every area runs. A Compose BOM bump therefore runs the Android library and Android demo jobs, not the iOS ones.
- **Aggregators:** each gate workflow ends in one `if: always()` job that runs `make ci-required` with `NEEDS: ${{ toJSON(needs) }}`. It fails on any `failure` or `cancelled`, and accepts `skipped` only when that workflow's `changes` job succeeded. Make only the four aggregators required in branch protection.
- **Runners:** `ubuntu-24.04` for `changes`, tools, Android build and Android demos (KVM through `make ci-android-kvm`); `macos-26` for iOS, iOS demos and the Android unit tests (Robolectric renders the snapshot tests the same way it does on a contributor's Mac).
- **Caches:** npm (`setup-node`), Gradle (`gradle/actions/setup-gradle`), SPM `.build/xcode/SourcePackages` keyed on `.xcode-version` + `Package.resolved`, and Robolectric jars.
- **Concurrency:** gate groups are `<file>-${{ github.workflow }}-${{ github.event_name }}-${{ github.ref }}`, so a release-called gate (tag ref), a manual dispatch and a master push never cancel each other. `release` has one group, never cancelled.
- **Permissions:** `contents: read` everywhere; `packages: write` only in `publish-android`, `contents: write` only in `github-release`. Both write jobs check out with `persist-credentials: false` and install npm dependencies (`make tools-deps`) before the token step.
- **Action pins:** major tags (`actions/checkout@v7`, …). Renovate keeps them current.
- **CI-only Make targets:** `ci-xcode` (selects `/Applications/Xcode_<version>.app` or `Xcode-<version>.app` from `.xcode-version`), `ci-ios-simulator` (software keyboard, checks the pinned simulator exists, optional `IOS_SIM_APPEARANCE=light|dark`), `ci-android-kvm`, `ci-required`, `android-publish`, `release-android-packages`, `release-github`.

**Hosting / distribution:**

- **iOS:** SPM resolves the `vX.Y.Z` tag the maintainer pushes (SPM strips the `v`). Consumers fetch only this package; the test-only packages are never fetched.
- **Android:** `com.ts.ds:{tokens,core,compose,views,bom}` on GitHub Packages. A published version can never be overwritten. Each GitHub release also attaches the Compose and Views demo APKs (debug-signed) and the library AARs. Release procedure: [guides/release.md](guides/release.md).

**Snapshot references:** not in git and not in CI. They are git-ignored PNGs that each contributor records and verifies locally; no workflow compares pixels. See [guides/snapshot-workflow.md](guides/snapshot-workflow.md).

**Renovate** (`.github/renovate.json`): monthly (`* 0-5 1 * *` UTC), dependency dashboard on, no auto-merge, `prConcurrentLimit: 0`. Labels: `dependencies` plus `android`, `ios`, `tools`, `ci`, `toolchain` or `codegen`. Groups and the approval flow: [guides/toolchain-upgrades.md](guides/toolchain-upgrades.md).

**CODEOWNERS** (`.github/CODEOWNERS`): every path is owned by `@lee5783`.

**PR template:** `.github/pull_request_template.md` (platform checkboxes, the gate checklist, the `a11y-audit` label note).

## Environment configuration

**Required env vars:**

- CI publish: `GITHUB_REPOSITORY`, `GITHUB_ACTOR`, `GITHUB_TOKEN` (`make android-publish` fails fast without them).
- CI release: `TAG` (`vX.Y.Z`), `RELEASE_REF` (`refs/tags/vX.Y.Z`), `GH_TOKEN`.
- Local: none. Optional Make variables are listed in [testing.md](testing.md).

**Secrets location:** no repository secrets are defined; workflows use the built-in `GITHUB_TOKEN`. Consumer tokens belong in `~/.gradle/gradle.properties` or the CI secret store, never in a project file.

## Webhooks and callbacks

Not applicable.
