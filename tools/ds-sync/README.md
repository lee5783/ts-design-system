# ds-sync

Keeps `design/source/` a verbatim, checksummed copy of the TinySolution design system artifact
(`https://claude.ai/artifact/X6CsvK4dLQo638zkNcA2cj`). `design/manifest.json` records the artifact
id, URL and version, the sync time, the sha256 and size of every file under `design/source/`, and
for every SVG asset listed in `design-system.json` its blob id, its path under
`design/assets/{icons,logos}/source/` and the sha256 and size of the served bytes (these differ from
the index's `indexedSize` because the server re-serializes SVG).

Verify also checks that no unlisted SVG sits in an asset `source/` folder, that every asset is free of
scripts, `foreignObject`, event handlers, external references and DOCTYPE/ENTITY declarations, and
that `design/assets/icons/icons.meta.json` has a boolean `mirrorsInRTL` for exactly the listed icons.

The artifact needs an authenticated claude.ai session, so the copy step is agent-assisted or
manual. CI only verifies.

## Commands

| Command | What it does |
|---|---|
| `make ds-sync` | Verify `design/source/`, the assets and `icons.meta.json` against the manifest. Exits 1 and lists every `changed`, `missing`, `added` and `unsafe` path on drift. |
| `make ds-sync FROM=<export-dir> DS_VERSION=<version>` | Replace `design/source/` with a verbatim copy of an artifact export without its SVGs, replace the SVGs in `design/assets/<group>/source/` with the export's `assets/<Group>/*.svg`, then rewrite the manifest. |
| `make ds-sync UPDATE=1` | Rewrite the manifest from the files already in `design/source/` and `design/assets/`. |
| `make tools-test` | Type-check and run the Vitest suites in `tools/`. |

`--from`, `--version` and `--update` can also be passed directly:
`npm --prefix tools run ds-sync -- --from <dir> --version <version>`.

## Syncing a new DS version

1. Read the artifact (agent) or export it (manual) into a scratch folder with the artifact's own
   layout: `README.md`, `tokens.json`, `design-system.json`, `components/**`,
   `assets/{Icons,Logos}/README.md` and every SVG fetched by blob id into `assets/{Icons,Logos}/<name>.svg`.
2. Run `make ds-sync FROM=<scratch-folder> DS_VERSION=<artifact-version>`.
3. If icons were added or removed, edit `design/assets/icons/icons.meta.json` (set `mirrorsInRTL: true`
   only for directional icons) until `make ds-sync` passes.
4. Run `make codegen` and `make test`, then review the diff of `design/source/` and the manifest.

Never hand-edit `design/source/`. Local additions belong in `design/extensions/`.
