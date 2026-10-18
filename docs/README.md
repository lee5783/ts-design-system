# docs/: the doc corpus

Entry point into this repo's documentation. Read only the page that answers the current question; pages are short on purpose and link to each other instead of repeating content. The cross-toolkit contract of each component lives next to the design source, in `design/specs/components/<name>.md`.

Pages are kept in step with the code by the `document-writer` role; the deep-dive pages follow the `codebase-mapper` skill's templates (`.claude/skills/codebase-mapper/`).

## Routing table

This table matches the routing table in the root `CLAUDE.md`.

| Question | Read |
|---|---|
| Where does X live / where should new code go? | [structure.md](structure.md) |
| How does data/control flow, what are the layers/abstractions? | [architecture.md](architecture.md) |
| What naming/style/error-handling convention should I follow? | [conventions.md](conventions.md) |
| Where do tests actually run, what's the mocking style? | [testing.md](testing.md) |
| Third-party services, auth, analytics, CI/CD, env/build config | [integrations.md](integrations.md) |
| Language/framework/dependency versions | [stack.md](stack.md) |
| Known tech debt, bugs, fragile areas, security notes | [concerns.md](concerns.md) |
| **Known issues (check before fixing)** | `.claude/COMMON-ISSUES.md` (index) → `.claude/common-issues/<area>.md` |
| **What does term/jargon X mean?** | `.claude/DICTIONARY.md` (index) → `.claude/dictionary/<term>.md` |
| **How does flow X work end-to-end?** | `.claude/HOW-X-WORKS.md` (index) → `.claude/how-x-works/<flow>.md` |
| **Repo-specific durable facts / workarounds** | `.claude/MEMORY.md` (index) → `.claude/memory/<name>.md` |

## Maintainer guides (`guides/`)

| Task | Read |
|---|---|
| Bring a new DS version in: sync, extensions, codegen, bump classification | [guides/ds-update-flow.md](guides/ds-update-flow.md) |
| Add a component to all four toolkits and all four demos | [guides/adding-a-component.md](guides/adding-a-component.md) |
| Cut a release, acknowledge API breaks, recover a failed publish | [guides/release.md](guides/release.md) |
| What is breaking, how to deprecate, how the API checks work | [guides/deprecation-policy.md](guides/deprecation-policy.md) |
| Renovate PRs, manual toolchain bumps, the yearly platform bump | [guides/toolchain-upgrades.md](guides/toolchain-upgrades.md) |
| Build and run the demos, launch options, theme deep links, argent | [guides/running-demos.md](guides/running-demos.md) |

## Consumer pages (`consumers/`)

| Question | Read |
|---|---|
| Add the iOS library (SPM) to an app | [consumers/ios-getting-started.md](consumers/ios-getting-started.md) |
| Add the Android library (GitHub Packages) to an app | [consumers/android-getting-started.md](consumers/android-getting-started.md) |
| Light/dark, tokens in app UI, Material interop, string overrides | [consumers/theming.md](consumers/theming.md) |
| What to change when upgrading across a breaking release | [consumers/migration-guides.md](consumers/migration-guides.md) |

The repository overview and quickstart is the root [`README.md`](../README.md).
