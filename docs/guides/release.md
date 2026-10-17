# Release

One lockstep version (`VERSION`, plain `X.Y.Z`) ships both platforms: SPM consumers resolve the git tag `X.Y.Z`, and Android consumers get `com.ts.ds:*:X.Y.Z` from GitHub Packages. Releases run from `master` through the `release` workflow; nothing public exists until every gate and the Android publish have passed, and the tag is created last.

Tooling: `tools/release/src/release.ts` (bump, verify, notes), `tools/release/src/publish-android.ts` (publish), `cliff.toml` (changelog), `.github/workflows/release.yml`.

## Before the first release

These are one-time user actions (tracker D-001, D-002, D-007, D-008, D-026):

1. Decide what of `.claude/` and `CLAUDE.md` is committed. Snapshot references are git-ignored and stay out of the commit (C-45).
2. Check the owner values: `.github/CODEOWNERS` and `ds.pom.developerId` / `ds.pom.developerName` / `ds.pom.organizationName` in `android/gradle.properties`.
3. Commit the tree with conventional commit messages: the first changelog is generated from the whole history.
4. In branch protection on `master`, require only `codegen-check-ok`, `ios-ok`, `android-ok` and `demos-ok`.
5. Enable the Renovate GitHub app.

## Steps

1. **Pick the version.** Semver, plain `X.Y.Z` (no `v`, no pre-release suffix). Use `codegen diff-report` for token changes, `make api-check BASELINE=<last tag>` for API changes, and [deprecation-policy.md](deprecation-policy.md) for what is major. Before 1.0, minor releases may break.
2. **Bump on a branch:**

   ```bash
   git fetch --tags origin
   make release-bump V=X.Y.Z DRY_RUN=1   # prints the new changelog section, writes nothing
   make release-bump V=X.Y.Z             # writes VERSION and regenerates CHANGELOG.md
   ```

   `release-bump` refuses a version that is not greater than `VERSION` (except the first release: while no `X.Y.Z` tag exists, `V` may equal `VERSION`, so `0.1.0` can be cut) and a tag that already exists locally or on `origin`. It regenerates the whole `CHANGELOG.md` with git-cliff, found in this order: `$GIT_CLIFF`, `git-cliff` on `PATH` (installed by `mise`), then `npx --yes git-cliff@2.14.2`.
3. **Commit** `chore(release): X.Y.Z` (skipped by the changelog), open the PR, merge it after the gates pass.
4. **Dispatch the release:** GitHub → Actions → **release** → *Run workflow* on `master`, input `version` = `X.Y.Z`. The jobs run in order:

   | Job | What it does | Permissions |
   |---|---|---|
   | `verify` | `make release-verify TAG=X.Y.Z RELEASE_REF=refs/heads/master`: the tag equals `VERSION`, `CHANGELOG.md` has a `## X.Y.Z` section, the ref is `master`, the tag does not exist locally or on `origin`, HEAD is on `origin/master` | read |
   | `codegen-check`, `ios`, `android`, `demos` | the four PR gate workflows, called as reusable workflows on the release commit | read |
   | `publish-android` | `make android-publish`: probes each module's POM in GitHub Packages, skips the published ones, publishes the missing libraries in one Gradle run, then the BOM | `packages: write` |
   | `github-release` | `make release-github`: `gh release create X.Y.Z --target $GITHUB_SHA` with the `CHANGELOG.md` section as notes (`make release-notes`), which creates the tag | `contents: write` |

5. **After the release:** the tag becomes the iOS API baseline (`make ios-api-check-last-tag` on `master`). On the first release, do the one-time consumer smoke check: a real GitHub Packages download with a `read:packages` token and a real `github.com` SPM clone (D-055).

## Changelog groups

`cliff.toml` groups commits by conventional type: Features (`feat`), Fixes (`fix`), Performance (`perf`), Refactoring (`refactor`), Reverts (`revert`), Documentation (`docs`), Tests (`test`), Maintenance (`build`, `ci`, `chore`, `style`, `deps`), Other (anything else). The scope is shown in bold, and breaking commits (`feat!:` or a `BREAKING CHANGE` footer) are marked `[**breaking**]`. `chore(release)` and merge commits are skipped. Only plain `X.Y.Z` tags count as releases.

## API acknowledgements

An intended iOS break must be acknowledged before the release, or `ios-api-check` fails in the gates:

```bash
make ios-api-check BASELINE=<last-tag>          # lists the breaking changes
make ios-api-check-record BASELINE=<last-tag>   # writes them to ios/api/breaking-changes.txt
```

Commit `ios/api/breaking-changes.txt`. Each line is `<baseline VERSION> <breakage>`; entries recorded against an older release are ignored and reported as stale, so the file needs no clean-up. Removed or renamed SPM products are reported as `Package: …` lines and acknowledged the same way. On Android, run `cd android && ./gradlew apiDump` and commit the `api/*.api` diff. Neither check enforces a major bump when a break is acknowledged (D-012): that is the reviewer's job.

## Recovery

- **A gate fails:** nothing was published or tagged. Fix on `master` (a new commit; `VERSION` stays), then dispatch again.
- **`publish-android` fails halfway:** re-run the failed job (or dispatch again). Modules whose POM is already in GitHub Packages (HTTP 200) are skipped; missing ones (404/410) are published; any other status stops with a token hint. The BOM is always published last.
- **A module upload broke partway** (some files of the version uploaded, no POM): the re-run gets HTTP 409. Delete that package version in the GitHub Packages UI, then re-run (D-102).
- **`github-release` fails:** the Android artifacts exist but the tag does not, so SPM consumers do not see the release yet. Re-run the job.
- **A version can never be re-published.** GitHub Packages refuses to overwrite a version, and a tag is final. To fix a bad release, release a new patch.

## Local dry runs

- `make release-bump V=<next> DRY_RUN=1`: the changelog section only.
- `make android-publish-local`: publishes all modules and the BOM to `~/.m2`; consume it with `-Pds.useLocal=false -Pds.mavenLocal=true -Pds.version=<v>` in the demos, or `mavenLocal()` in a scratch app.
- `make release-notes TAG=X.Y.Z`: writes the section to `.build/release/notes.md`.

The first real run of the workflow is still owed (D-052): it proves `packages: write` publishing, the 200/404 probe, the dispatch flow and the reusable-workflow fan-out.
