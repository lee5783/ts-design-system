# External integrations

**Analysis date:** 2026-10-18

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

**No credentials anywhere in distribution.** Consumers read SPM (iOS) and JitPack (Android) anonymously, and nothing in the Gradle build reads a token. The only secret a workflow uses is the built-in `GITHUB_TOKEN`, passed as `GH_TOKEN` to `make release-github` (see *CI/CD and deployment*).

**JitPack (Android distribution).** JitPack clones the repository at a tag (or commit) and runs the root `jitpack.yml`:

```yaml
jdk:
  - openjdk21
install:
  - cd android && ./gradlew publishToMavenLocal -x test -x lint --no-daemon --stacktrace -Dorg.gradle.jvmargs="-Xmx3g -Dfile.encoding=UTF-8"
```

The Gradle root is `android/`, so the install step changes into it; tests and lint are skipped, and the heap is held below the repo's own `-Xmx4g`. JitPack then serves whatever `publishToMavenLocal` produced. Group and version come from `android/build-logic/convention/src/main/kotlin/ProjectExtensions.kt` (`dsGroup()`, `dsVersion()`), applied by both `DsAndroidLibraryPlugin` and `DsPublishingPlugin`:

| Env | Effect |
|---|---|
| `JITPACK` unset or not `true` (local, CI, demos) | group `com.tinysol.designsystem` (`DsBuildConstants.GROUP`), version from the repo-root `VERSION` file (for example `0.1.0`) |
| `JITPACK=true` (set by JitPack) | group `"$GROUP.$ARTIFACT"` = `com.github.lee5783.ts-design-system`, version = `$VERSION` (the tag, `v` included, for example `v0.2.0`) |

Under `JITPACK=true`, a missing or blank `GROUP`, `ARTIFACT` or `VERSION` fails configuration with "JITPACK=true but environment variable '<name>' is not set". Because both convention plugins set the same group, the BOM constraints and the inter-module POM dependencies (`compose`/`views` → `core` → `tokens`) always carry the effective group and version. To reproduce a JitPack build locally, from the repo root:

```bash
JITPACK=true GROUP=com.github.lee5783 ARTIFACT=ts-design-system VERSION=v0.0.0-test \
  sh -c 'cd android && ./gradlew publishToMavenLocal -x test -x lint --no-daemon -Dmaven.repo.local=/tmp/m2-jitpack'
```

**Demo library switch** (`demos/android-*/settings.gradle.kts`):

| Property | Default | Effect |
|---|---|---|
| `ds.useLocal` | `true` (`gradle.properties`) | `includeBuild("../../android")` substitutes `com.tinysol.designsystem:*` with the in-tree modules |
| `ds.mavenLocal` | `false` | With `ds.useLocal=false`: resolve `com.tinysol.designsystem` from `~/.m2` (after `make android-publish-local`) |
| `ds.version` | `../../VERSION` | The BOM version the demo requests |

`ds.useLocal=false` requires `ds.mavenLocal=true`: there is no remote published mode, so without it the library does not resolve. The demos have no JitPack mode.

iOS demos always use local package references (`../..`); a remote reference fails `make demos-check-boundaries`.

**POM metadata:** `android/gradle.properties` holds `ds.pom.repository` (`lee5783/ts-design-system`), `ds.pom.organizationName`, `ds.pom.developerId`, `ds.pom.developerName`. `DsPublishingPlugin` derives every POM URL from `ds.pom.repository`: project `url`, `scm` (`url`, `connection`, `developerConnection`), `organization` and `developers` URLs (the owner), `issueManagement` (`…/issues`) and the licence URL `https://github.com/lee5783/ts-design-system/blob/master/LICENSE` (the default branch is `master`). Environment variables such as `GITHUB_REPOSITORY` do not influence the POM, and a missing `ds.pom.repository` fails configuration. The licence name is read from the first line of `LICENSE`. The developer id is `lee5783`. No javadoc jar is published; a sources jar is.

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
| `ios.yml` | same | `changes`; `test` (matrix `pinned`, `oldest`, as `IOS_RUNTIMES`); `api-check` (`ios-api-check` against the PR base, `ios-api-check-last-tag`) | `ios-ok` |
| `android.yml` | same | `changes`; `build` (`assemble lint apiCheck`, Ubuntu); `test` (`android-test`, macOS) | `android-ok` |
| `demos.yml` | same | `changes`; `boundaries`; `ios-demo-support`; `ios-demo` (per demo on the pinned runtime, light, then the dark `ThemeOverrideTests`); `android-demo` (per demo on an API 36 emulator) | `demos-ok` |
| `release.yml` | push of a `vX.Y.Z` tag | `verify` → the 4 gate workflows (reusable) → `android-packages` (the two debug demo APKs) → `github-release` (notes + those APKs) | none |

- **Path areas** (`tools/ci/src/changes.ts`, `make ci-changes BASE=<sha>`): `ios`, `android`, `boundaries` and one area per demo. `Makefile`, `.gitattributes`, `tools/package*.json` and `tools/ci/**` touch every area; `design/**` and `tools/codegen/**` touch both libraries and all demos; `design/specs/components/**` touches `boundaries`. To route a new path, add it to `AREA_PATTERNS` and extend `tools/ci/test/changes.test.ts`. Without a base commit (a tag push, a new branch), every area runs. A Compose BOM bump therefore runs the Android library and Android demo jobs, not the iOS ones.
- **Aggregators:** each gate workflow ends in one `if: always()` job that runs `make ci-required` with `NEEDS: ${{ toJSON(needs) }}`. It fails on any `failure` or `cancelled`, and accepts `skipped` only when that workflow's `changes` job succeeded. Make only the four aggregators required in branch protection.
- **Runners:** `ubuntu-24.04` for `changes`, tools, Android build and Android demos (KVM through `make ci-android-kvm`); `macos-26` for iOS, iOS demos and the Android unit tests.
- **Caches:** npm (`setup-node`), Gradle (`gradle/actions/setup-gradle`), SPM `.build/xcode/SourcePackages` keyed on `.xcode-version` + `Package.resolved`, and Robolectric jars.
- **Concurrency:** gate groups are `<file>-${{ github.workflow }}-${{ github.event_name }}-${{ github.ref }}`, so a release-called gate (tag ref), a manual dispatch and a master push never cancel each other. `release` has one group, never cancelled.
- **Permissions:** `contents: read` everywhere; `contents: write` only in `github-release`, which checks out with `persist-credentials: false` and installs npm dependencies (`make tools-deps`) before the token step. No workflow has `packages: write`.
- **Action pins:** major tags (`actions/checkout@v7`, …). Renovate keeps them current.
- **CI-only Make targets:** `ci-xcode` (selects `/Applications/Xcode_<version>.app` or `Xcode-<version>.app` from `.xcode-version`), `ci-ios-simulator` (software keyboard, checks the pinned simulator exists, optional `IOS_SIM_APPEARANCE=light|dark`), `ci-android-kvm`, `ci-required`, `release-android-packages`, `release-github`.

**Hosting / distribution:**

- **iOS:** SPM resolves the `vX.Y.Z` tag the maintainer pushes (SPM strips the `v`). Consumers fetch only this package; the test-only packages are never fetched.
- **Android:** JitPack, `com.github.lee5783.ts-design-system:{tokens,core,compose,views,bom}:<tag>` from `https://jitpack.io`, built from the tag by `jitpack.yml` on the first request. No CI job publishes Android artifacts. `v0.1.0` predates `jitpack.yml` and does not build there. Consumer setup: [consumers/android-getting-started.md](consumers/android-getting-started.md).
- **GitHub release:** each tag gets a release with the `CHANGELOG.md` section as notes and the Compose and Views demo APKs (debug-signed). No library AARs are attached. Release procedure: [guides/release.md](guides/release.md).

**Visual checks:** no workflow compares pixels, and the repo has no snapshot tests. Visual changes are reviewed through the argent screenshots in the PR's Evidence section, taken on the pinned iOS simulator (`.ios-simulator`) and one Android emulator at API 36.

**Renovate** (`.github/renovate.json`): monthly (`* 0-5 1 * *` UTC), dependency dashboard on, no auto-merge, `prConcurrentLimit: 0`. Labels: `dependencies` plus `android`, `ios`, `tools`, `ci`, `toolchain` or `codegen`. Groups and the approval flow: [guides/toolchain-upgrades.md](guides/toolchain-upgrades.md).

**CODEOWNERS** (`.github/CODEOWNERS`): every path is owned by `@lee5783`.

**PR template:** `.github/pull_request_template.md` (platform checkboxes, the gate checklist, the Evidence section: argent screenshots on one OS version per platform and the targeted tests run).

## Environment configuration

**Required env vars:**

- JitPack build: `JITPACK=true`, `GROUP`, `ARTIFACT`, `VERSION` (injected by JitPack; see *Authentication and identity*).
- CI release: `TAG` (`vX.Y.Z`), `RELEASE_REF` (`refs/tags/vX.Y.Z`), `GH_TOKEN`.
- Local: none. Optional Make variables are listed in [testing.md](testing.md).

**Secrets location:** no repository secrets are defined; workflows use the built-in `GITHUB_TOKEN`. Consumers need no token for either channel.

## Webhooks and callbacks

Not applicable.
