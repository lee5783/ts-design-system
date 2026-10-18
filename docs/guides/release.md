# Release

One lockstep version (`VERSION`, plain `X.Y.Z`) ships both platforms: SPM consumers resolve the git tag `vX.Y.Z` (SPM strips the `v`), and Android consumers get `com.github.lee5783.ts-design-system:*:vX.Y.Z` from JitPack, which builds the same tag with the root `jitpack.yml` on first request. Those are the only two distribution channels. Pushing a `vX.Y.Z` tag on a `master` commit starts the `release` workflow, which publishes no packages: it runs every gate, then creates the GitHub release with the changelog section as notes and the Compose and Views debug demo APKs attached (no library AARs).

Tooling: `tools/release/src/release.ts` (bump, verify, notes), `cliff.toml` (changelog), `.github/workflows/release.yml`, `jitpack.yml` (the JitPack build; see [../integrations.md](../integrations.md#authentication-and-identity)).

## Before the first release

These are one-time user actions (tracker D-001, D-002, D-007, D-008, D-026):

1. Decide what of `.claude/` and `CLAUDE.md` is committed.
2. Check the owner values: `.github/CODEOWNERS` and `ds.pom.repository` / `ds.pom.developerId` / `ds.pom.developerName` / `ds.pom.organizationName` in `android/gradle.properties`.
3. Commit the tree with conventional commit messages: the first changelog is generated from the whole history.
4. In branch protection on `master`, require only `codegen-check-ok`, `ios-ok`, `android-ok` and `demos-ok`.
5. Enable the Renovate GitHub app.

## Steps

1. **Pick the version.** Semver `X.Y.Z` in `VERSION` (no pre-release suffix); the tag is the same version with a `v` prefix. Use `codegen diff-report` for token changes, `make api-check BASELINE=<last tag>` for API changes, and [deprecation-policy.md](deprecation-policy.md) for what is major. Before 1.0, minor releases may break.
2. **Bump on a branch:**

   ```bash
   git fetch --tags origin
   make release-bump V=X.Y.Z DRY_RUN=1   # prints the new changelog section, writes nothing
   make release-bump V=X.Y.Z             # writes VERSION and regenerates CHANGELOG.md
   ```

   `release-bump` refuses a version that is not greater than `VERSION` (except the first release: while no release tag exists, `V` may equal `VERSION`, so `0.1.0` can be cut) and a version whose tag (`vX.Y.Z` or `X.Y.Z`) already exists locally or on `origin`. It regenerates the whole `CHANGELOG.md` with git-cliff, found in this order: `$GIT_CLIFF`, `git-cliff` on `PATH` (installed by `mise`), then `npx --yes git-cliff@2.14.2`.
3. **Commit** `chore(release): X.Y.Z` (skipped by the changelog), open the PR, merge it after the gates pass.
4. **Tag the merged commit and push the tag:**

   ```bash
   git checkout master && git pull
   git tag vX.Y.Z
   git push origin vX.Y.Z
   ```

   The tag push starts the `release` workflow. The jobs run in order:

   | Job | What it does | Permissions |
   |---|---|---|
   | `verify` | `make release-verify TAG=vX.Y.Z RELEASE_REF=refs/tags/vX.Y.Z`: the tag is `v` + `VERSION`, `CHANGELOG.md` has a `## X.Y.Z` section, the run was started by that tag, and the tagged commit is on `origin/master` | read |
   | `codegen-check`, `ios`, `android`, `demos` | the four PR gate workflows, called as reusable workflows on the tagged commit (every area runs) | read |
   | `android-packages` | needs all four gates. `make release-android-packages`: builds the Compose and Views demo APKs (debug-signed, installable) into `.build/release/assets` as `ts-ds-demo-{compose,views}-X.Y.Z.apk`, uploaded as the `release-assets` artifact | read |
   | `github-release` | needs `android-packages`. `make release-github`: `gh release create vX.Y.Z --verify-tag` with the `CHANGELOG.md` section as notes (`make release-notes`) and the two APKs from `.build/release/assets` attached | `contents: write` |

5. **Trigger the JitPack build:** open `https://jitpack.io/#lee5783/ts-design-system`, pick the tag under *Releases* and press *Get it* (or request `https://jitpack.io/com/github/lee5783/ts-design-system/bom/vX.Y.Z/bom-vX.Y.Z.pom`). JitPack builds on the first request anyway, but triggering it yourself surfaces a failing build before a consumer hits it. Read the build log: it must end in `BUILD SUCCESSFUL` and list the five artifacts (`tokens`, `core`, `compose`, `views`, `bom`).
6. **After the release:** the tag becomes the iOS API baseline (`make ios-api-check-last-tag` on `master`). On the first JitPack release, do the one-time consumer smoke check: a scratch app with `maven("https://jitpack.io")`, `platform("com.github.lee5783.ts-design-system:bom:vX.Y.Z")` and `:compose`, confirming `core` and `tokens` resolve transitively, plus a real `github.com` SPM clone (D-055). `v0.1.0` predates `jitpack.yml` and cannot be built by JitPack; the first consumable Android version is the next tag.

## Changelog groups

`cliff.toml` groups commits by conventional type: Features (`feat`), Fixes (`fix`), Performance (`perf`), Refactoring (`refactor`), Reverts (`revert`), Documentation (`docs`), Tests (`test`), Maintenance (`build`, `ci`, `chore`, `style`, `deps`), Other (anything else). The scope is shown in bold, and breaking commits (`feat!:` or a `BREAKING CHANGE` footer) are marked `[**breaking**]`. `chore(release)` and merge commits are skipped. Only `vX.Y.Z` (and legacy plain `X.Y.Z`) tags count as releases; the `v` is dropped from changelog headings.

## API acknowledgements

An intended iOS break must be acknowledged before the release, or `ios-api-check` fails in the gates:

```bash
make ios-api-check BASELINE=<last-tag>          # lists the breaking changes
make ios-api-check-record BASELINE=<last-tag>   # writes them to ios/api/breaking-changes.txt
```

Commit `ios/api/breaking-changes.txt`. Each line is `<baseline VERSION> <breakage>`; entries recorded against an older release are ignored and reported as stale, so the file needs no clean-up. Removed or renamed SPM products are reported as `Package: …` lines and acknowledged the same way. On Android, run `cd android && ./gradlew apiDump` and commit the `api/*.api` diff. Neither check enforces a major bump when a break is acknowledged (D-012): that is the reviewer's job.

## Recovery

- **A gate fails:** no GitHub release exists, but the tag does, and JitPack would build it if anyone requested it. Fix on `master` (a new commit; `VERSION` stays), move the tag and push it again: `git tag -f vX.Y.Z && git push -f origin vX.Y.Z`. Only do this before any consumer could have resolved the tag.
- **`android-packages` or `github-release` fails:** SPM already sees the tag and JitPack can build it, but there is no GitHub release page yet. Re-run the failed jobs.
- **The JitPack build fails:** read the log on `jitpack.io`. Fix on `master` and release a new patch; JitPack caches a tag's result, so a moved tag needs its build deleted on `jitpack.io` before it is rebuilt.
- **A version is final once consumed.** To fix a bad release, release a new patch.

## Local dry runs

- `make release-bump V=<next> DRY_RUN=1`: the changelog section only.
- `make android-publish-local`: publishes all modules and the BOM to `~/.m2` under `com.tinysol.designsystem`; consume it with `-Pds.useLocal=false -Pds.mavenLocal=true -Pds.version=<v>` in the demos, or `mavenLocal()` in a scratch app.
- `make release-notes TAG=vX.Y.Z`: writes the section to `.build/release/notes.md`.
- `make release-android-packages`: builds the two demo APKs into `.build/release/assets`.
- JitPack, locally: run the `jitpack.yml` install line with `JITPACK=true GROUP=com.github.lee5783 ARTIFACT=ts-design-system VERSION=v0.0.0-test` (full command in [../integrations.md](../integrations.md#authentication-and-identity)).

The first real run of the workflow is still owed (D-052): it proves the tag-push flow, the reusable-workflow fan-out and the release with APKs. The first real JitPack build of a tag is owed too: it proves the builder environment (SDK 36, memory) and BOM/transitive resolution under the JitPack group.
