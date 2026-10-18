# Release

One lockstep version (`VERSION`, plain `X.Y.Z`) ships both platforms: SPM consumers resolve the git tag `vX.Y.Z` (SPM strips the `v`), and Android consumers get `com.ts.ds:*:X.Y.Z` from GitHub Packages. Pushing a `vX.Y.Z` tag on a `master` commit starts the `release` workflow; the Android publish and the GitHub release happen only after every gate has passed. The GitHub release carries the changelog section, the Compose and Views demo APKs and the library AARs.

Tooling: `tools/release/src/release.ts` (bump, verify, notes), `tools/release/src/publish-android.ts` (publish), `cliff.toml` (changelog), `.github/workflows/release.yml`.

## Before the first release

These are one-time user actions (tracker D-001, D-002, D-007, D-008, D-026):

1. Decide what of `.claude/` and `CLAUDE.md` is committed.
2. Check the owner values: `.github/CODEOWNERS` and `ds.pom.developerId` / `ds.pom.developerName` / `ds.pom.organizationName` in `android/gradle.properties`.
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
   | `publish-android` | `make android-publish`: probes each module's POM in GitHub Packages, skips the published ones, publishes the missing libraries in one Gradle run, then the BOM | `packages: write` |
   | `android-packages` | `make release-android-packages`: builds the Compose and Views demo APKs (debug-signed, installable) and the library AARs into `.build/release/assets`, uploaded as the `release-assets` artifact | read |
   | `github-release` | `make release-github`: `gh release create vX.Y.Z --verify-tag` with the `CHANGELOG.md` section as notes (`make release-notes`) and every file in `.build/release/assets` attached | `contents: write` |

5. **After the release:** the tag becomes the iOS API baseline (`make ios-api-check-last-tag` on `master`). On the first release, do the one-time consumer smoke check: a real GitHub Packages download with a `read:packages` token and a real `github.com` SPM clone (D-055).

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

- **A gate fails:** nothing was published, but the tag exists. Fix on `master` (a new commit; `VERSION` stays), move the tag and push it again: `git tag -f vX.Y.Z && git push -f origin vX.Y.Z`. Only do this before any consumer could have resolved the tag.
- **`publish-android` fails halfway:** re-run the failed jobs from the workflow run page. Modules whose POM is already in GitHub Packages (HTTP 200) are skipped; missing ones (404/410) are published; any other status stops with a token hint. The BOM is always published last.
- **A module upload broke partway** (some files of the version uploaded, no POM): the re-run gets HTTP 409. Delete that package version in the GitHub Packages UI, then re-run (D-102).
- **`android-packages` or `github-release` fails:** the Android artifacts exist and SPM already sees the tag, but there is no GitHub release page yet. Re-run the failed jobs.
- **A version can never be re-published.** GitHub Packages refuses to overwrite a version, and a tag is final once published. To fix a bad release, release a new patch.

## Local dry runs

- `make release-bump V=<next> DRY_RUN=1`: the changelog section only.
- `make android-publish-local`: publishes all modules and the BOM to `~/.m2`; consume it with `-Pds.useLocal=false -Pds.mavenLocal=true -Pds.version=<v>` in the demos, or `mavenLocal()` in a scratch app.
- `make release-notes TAG=vX.Y.Z`: writes the section to `.build/release/notes.md`.
- `make release-android-packages`: builds the demo APKs and AARs into `.build/release/assets`.

The first real run of the workflow is still owed (D-052): it proves `packages: write` publishing, the 200/404 probe, the tag-push flow and the reusable-workflow fan-out.
