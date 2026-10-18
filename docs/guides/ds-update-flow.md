# DS update flow

How a change in the TinySolution design system reaches both platforms. The sync tool's own reference is `tools/ds-sync/README.md`; the generator is `tools/codegen/`.

## 1. Sync the artifact

The artifact needs an authenticated claude.ai session, so the copy is agent-assisted or a manual export; CI only verifies.

1. Read or export the artifact into a scratch folder with its own layout: `README.md`, `tokens.json`, `design-system.json`, `components/**`, `assets/{Icons,Logos}/README.md`, and every SVG fetched by blob id into `assets/{Icons,Logos}/<name>.svg`.
2. Copy it in:

   ```bash
   make ds-sync FROM=<scratch-folder> DS_VERSION=<artifact-version>
   ```

   This replaces `design/source/` (without the SVGs), replaces `design/assets/{icons,logos}/source/*.svg` with the export's `assets/{Icons,Logos}/*.svg`, and rewrites `design/manifest.json` (version, sync time, sha256 and size of every file).
3. If icons were added or removed, edit `design/assets/icons/icons.meta.json`: one `mirrorsInRTL` boolean per icon, `true` only for directional icons. `make ds-sync` fails until the list matches the DS exactly.
4. Verify: `make ds-sync` (no arguments) exits 0, or lists every `changed`, `missing`, `added` and `unsafe` path.

`make ds-sync UPDATE=1` rewrites the manifest from the files already on disk. Use it only after a deliberate local fix to an asset, never to hide drift. Never hand-edit `design/source/`.

## 2. Reconcile local extensions

`design/extensions/native-tokens.json` holds tokens the DS does not define yet. Each entry has `name`, `value` (a string, or `{ light, dark }` for colours), `usage`, `upstream` (`pending`, `accepted` or `rejected`) and `origin`; component constants also carry `component` and `type`.

- **The DS shipped a token we defined locally:** codegen stops with `native-tokens <family>/<name>: duplicate key, already defined in design/source/tokens.json; remove it from design/extensions/native-tokens.json once the DS ships it`. Delete the local entry (the DS value wins) and check the affected demo pages.
- **The DS renamed or removed a token** that the libraries expose: add `"old-name": "new-name"` to the `renames` map. Codegen then emits a deprecated alias on every platform (Swift `@available(*, deprecated, renamed:)`, Kotlin `@Deprecated(…, ReplaceWith(…))`), and the rename is a minor change instead of a major one. Codegen refuses a rename whose old name still exists or whose target does not.
- Update an entry's `upstream` status when design accepts or rejects it.

**Controller families.** Two extension families hold the controller and media-overlay palette ([../consumers/theming.md](../consumers/theming.md#controller-and-media-overlay-colours)) and are merged into their own model bucket (`TokenModel.controller`), never into `model.color` / `model.size`:

| Family | Entry value | Generates |
|---|---|---|
| `controller-color` | `{ light, dark }` or a single colour; aliases such as `{text-primary}` resolve against the main palette | Compose `TSControllerColors` + `lightTSControllerColors()` / `darkTSControllerColors()` (read through `TSTheme.controllerColors`), Swift `TSControllerColor`, Views `ts_controller_colors.xml` (light + night) |
| `controller-size` | one px value (a themed size is rejected) | Compose `TSControllerSize`, Swift `TSControllerSize`, Views `ts_controller_dimens.xml` |

- Member names drop the `controller-` prefix (`controller-key-pressed` → `keyPressed`). Codegen rejects a controller name that already exists in the main model, a duplicate, or two names that map to the same member.
- The contrast of the controller pairs is asserted in `tools/codegen/test/unit/merge-extensions.test.ts`, alpha-composited over black and white frames with the helpers in `tools/codegen/src/transforms/contrast.ts` (`compositeOver`, `flatten`, `compositedContrastRatio`). A value change that drops a label pair below 4.5:1 or the outline below 3:1 fails `make tools-test`.
- The `renames` map and `diff-report` do not cover controller tokens (they are not in `TOKEN_FAMILIES`): renaming or removing one generates no deprecated alias and is caught only by the API checks. Treat it as breaking by hand.

`npm --prefix tools run --silent codegen -- model` prints the merged model: token counts per family (controller colours and sizes included), aliases, extension statuses and deprecated aliases.

## 3. Generate

```bash
make codegen                 # TARGET=all (default), tokens or icons
make codegen-check           # fails if any generated file is stale or hand-edited
```

Outputs: `ios/Sources/TSTokens/Generated/`, `ios/Sources/TSFoundation/Resources/{Icons,Logos}.xcassets/`, `android/tokens/src/generated/`, `android/core/src/generated/`, `android/compose/src/generated/`, `android/views/src/generated/`, `design/assets/*/normalized/`.

Adding an icon means: sync the SVG, add its `icons.meta.json` row, run `make codegen`. The icon becomes `TSIconName.<camel>` (iOS), `TSIcon.<Pascal>` (Android) and `@drawable/ts_ic_<snake>`.

## 4. Classify the change

```bash
npm --prefix tools run --silent codegen -- diff-report --base-ref <last-release-tag>
# or: --base <old tokens.json> [--head <new tokens.json>] [--renames <json>] [--json]
```

It prints `diff-report: suggested bump <level>` and one line per token change:

| Change | Suggested bump |
|---|---|
| removed | major |
| added | minor |
| renamed (with a `renames` alias) | minor |
| value changed | minor |
| only `usage` / sample text changed | patch |

The suggestion covers tokens only. API and behaviour changes in the libraries are classified by the API checks and by review ([deprecation-policy.md](deprecation-policy.md)).

## 5. Verify

1. `make test`: unit and golden tests, `codegen-check`, `ds-sync`, demo boundaries, iOS and Android suites. No test compares pixels.
2. Take argent screenshots of the affected demo pages before and after the sync, on the pinned iOS simulator (`.ios-simulator`) and one Android emulator at API 36, and put the intended changes in the PR's Evidence section. CI does not compare pixels.
3. If golden fixtures in `tools/codegen/test/fixtures/expected/**` must change because the generator changed, refresh them with `cd tools && UPDATE_GOLDEN=1 npx vitest run codegen/test/golden/<swift|android-xml|compose|icons>.test.ts` and review the diff.
4. Open the four demo apps and check the changed tokens and components by hand ([running-demos.md](running-demos.md)). Each About screen shows the library version and the DS version from `design/manifest.json`.

## 6. Release

One lockstep version for both platforms: [release.md](release.md).
