## Summary

What changed and why, in one to three sentences.

## Platforms touched

- [ ] Design source or tokens (`design/`, `tools/codegen`)
- [ ] iOS library (`ios/`, `Package.swift`)
- [ ] Android library (`android/`)
- [ ] Demo apps (`demos/`)
- [ ] Tooling or CI (`tools/`, `Makefile`, `.github/`)

## Checklist

- [ ] `make tools-test codegen-check ds-sync` pass, and generated code was regenerated with `make codegen`, not edited by hand
- [ ] `make ios-test` / `make android-test` pass for the platforms touched
- [ ] For visual changes, `make ios-snapshot-verify` / `make android-snapshot-verify` were run against references recorded locally before the change, and every diff is intended (references are git-ignored; CI does not compare pixels)
- [ ] Public API changes are intended: `make api-check BASELINE=<last tag>` passes, or the Android `api/*.api` dumps were updated with `apiDump`
- [ ] The minimum-OS rule holds: iOS 15 and API 26 fallbacks go through the `Compat/` shims
- [ ] Each touched component is shown in both of its platform's demo apps, and `make demos-check-boundaries` passes
- [ ] Docs under `docs/` are updated, or no behaviour changed

## Evidence

Screenshots or snapshot diffs for visual changes. Logs for tooling changes.

## Accessibility audits

Add the `a11y-audit` label to run the iOS demo accessibility audits (about two hours on macOS runners). They also run nightly on `master`.
