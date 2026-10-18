# Codebase concerns

**Analysis date:** 2026-10-16

Known gaps, visible fallbacks and fragile areas as of `VERSION` 0.1.0. Items carry their id in the session tracker, `.claude/working-sessions/20261015225536-mobile-ds-libraries/develop/output-deferred-tracker.md` (a local, agent-maintained file), so they can be followed up. The tracker also lists the open user decisions and the checks that need a first real CI run or release.

## DS gaps (filled locally, pending upstream)

Every local addition lives in `design/extensions/native-tokens.json` with an `upstream` status (`pending`, `accepted`, `rejected`). All 56 are **`pending`** today:

| Family | Tokens | Why |
|---|---|---|
| color (6) | `danger-pressed`, `switch-thumb`, `success/warning/danger/info-on-inverse` | Pressed danger fill (C-12); the hard-coded switch thumb; icon colours on `surface-inverse` for the Toast (C-26) |
| duration (3), easing (1) | `duration-fast/base/slow`, `easing-standard` | The DS defines no motion (C-13) |
| component (37) | button, avatar, bottom-nav, alert, toast, switch, checkbox, badge, tabs, list-item, dialog, sheet grabber, text-field, spinner and card constants | Values the DS hard-codes in `bundle.css` |
| controller-color (8), controller-size (1) | `controller-backdrop`, `controller-key`, `controller-key-pressed`, `controller-key-outline`, `controller-key-label`, `controller-backing`, `overlay-scrim-media`, `text-on-overlay-media`; `controller-outline-width` | UI over live media and game controllers (issue #3); a separate palette outside `TSColors` ([consumers/theming.md](consumers/theming.md#controller-and-media-overlay-colours)). The DS should adopt them upstream |

Open design questions on these values (tracker D-017, D-020): the controller light values are hex literals equal to `background-primary` / `text-primary` rather than aliases, so they will not follow an upstream change of those tokens; the Spinner md / lg strokes (2.5 / 3) are [est] until the DS defines spinner sizes; dark `danger-pressed` `#cd2323` reaches only 2.78:1 on `surface-card`; the pressed, motion and icon-only `*-on-inverse` tokens; the [est] ListItem focused and disabled states; the 1.5 font-scale stacking threshold for list items and card footers; `segmentedHeight` 46; native multiline line height on iOS 16+; vertically centred leading icons in multiline fields; truncating badge rows; card pressed as a fill only.

Missing DS assets (D-018, D-019): no `eye-off` icon (the password toggle uses `eye` with a selected fill) and no `minus` icon (the indeterminate checkbox); the wordmark is outlined from a Roboto placeholder font (`tools/codegen/fonts/`) until brand artwork exists.

When the DS ships one of these tokens, codegen stops with a duplicate-key error; remove the entry from the extensions file ([guides/ds-update-flow.md](guides/ds-update-flow.md)).

## OS fallbacks (visible differences)

| Platform / version | Difference |
|---|---|
| iOS 15 | Sheet dialogs open at `.medium()` / `.large()`; there is no fit-content detent |
| iOS 15–16 | The system focus effect stays visible under the TS focus ring |
| iOS 15–16 | Checkbox and Switch read as a button with an "on/off" value instead of a toggle |
| iOS 26 | Sheet dialogs use the system's floating inset sheet geometry (`design/specs/components/dialog.md`) |
| iOS, all | The sheet scrim and grabber are the system's (36 × 5), not the DS 36 × 4 `border-strong` bar |
| Android 26–27 | Weight 600 renders as Medium 500 in both toolkits (`sans-serif-medium`); shadows are untinted black |
| Android 26–28 | Toast timeout is 4 s, or 10 s with TalkBack; 29+ uses `getRecommendedTimeoutMillis` (table in `design/specs/components/toast.md`) |
| Android Views | The sheet scrim is the window dim at the `overlay` alpha, so in light mode it is black at 40 %, not `#18181B` at 40 % |
| Android Views | The focus ring is drawn inside full-bleed shapes; icon buttons, segmented tabs and toggles have no outside ring (D-060) |

## Known limitations

**Toast placement (D-010, D-011):** a bottom toast covers an open sheet's bottom actions (its Close button) for its 4 s on all toolkits; with the keyboard up, the iOS toast sits above the keyboard but over the focused field. Both need a design call.

**Dynamic Type for non-text (D-014):** icons, badge dots, checkbox marks and bottom-nav badge counts do not scale with Dynamic Type or font scale. The Views bottom-nav `TextSizeCheck` ATF suppression points to this.

**Tab touch targets (D-015, D-061):** short underline labels get overlapping 48 dp touch areas (Views) or trimmed ones (Compose, about 45 dp); the Views touch area outside the first and last tab gets no touches.

**Screen-reader device pass (D-021, D-107):** VoiceOver and TalkBack were not run on devices, and keyboard / Full Keyboard Access evidence is part of the same pass. Required before the first release.

**Inline Alert Escape (D-016):** hardware-keyboard Escape is not handled for the inline Alert; the accessibility escape works.

**Toast host lifecycle (Android):** `TSToastHost` never detaches itself. An app that keeps its queue across an Activity recreate must call `detach()` in `onDestroy`; on any recreate (rotation included) the current toast restarts its full timeout and is announced again (C-43, `design/specs/components/toast.md`).

**Dialog queue and action order (issue #5, `design/specs/components/dialog.md`):**
- Under the `refuse` policy a follow-up dialog requested from a dismissing handler is refused (the first dialog still owns the presenter), so `refuse` and chaining do not mix.
- Compose: a consumer that ignores `onPresentedChange(false)` (to veto a close) or a handler that throws leaves the dialog on screen with every action ignored until `isPresented` becomes `true` again.
- iOS `afterDismissal` runs after a chained UIKit dialog is presented and after SwiftUI grants the next queued dialog, so system UI presented from it can still be dropped while a TS dialog is presenting.
- SwiftUI: if a granted session's binding goes `false` after the presentation started but before the content appeared, the slot is freed early and the next queued dialog can be refused by UIKit ("already presenting"), which resets its binding. The window is a fraction of the transition.
- UIKit: if a chained (handed-over) presentation fails inside UIKit while the app keeps a strong reference to the follow-up dialog, the slot stays held until that dialog is deallocated.
- `handOver(from:to:)` is public on both presenters but is library-internal in intent; it is not hidden behind `@RestrictTo` / `@_spi`.
- Mixed-toolkit apps share one presenter: imperative (UIKit, Views) and declarative (SwiftUI, Compose) dialogs refuse and queue against each other.

**Android immersive dialogs:** the host's system-bar state is copied once per dialog and re-applied to the host on close; a host that changes its bars while a dialog is open is not followed.

**Text-field code keyboard (Compose):** `TSTextInputTraits.Code` opens a `KeyboardType.Password` keyboard (visible text) because Compose cannot send `NO_SUGGESTIONS`; a password manager may offer autofill on the field, and Gboard treats capitals as a hint there. Views sends `NO_SUGGESTIONS` and is not affected (`design/specs/components/text-field.md`).

**Consumer icons and line control (issue #2):** consumer icons carry no RTL metadata (mirroring is set on the asset); a one-line Compose / Views button label can squeeze a trailing icon in a width-bounded button; UIKit `TSIconButtonView(iconImage:…)` stores `.plus` in `icon` as a placeholder, which shows if `iconImage` is cleared (`design/specs/components/button.md`, `icon-button.md`).

**New primitives (issue #6):** the disclosure row body appears without a height animation [est]; UIKit `TSDisclosureListItemView` has no collection-view cell variant; an `.accessibilityIdentifier` on a SwiftUI `TSDisclosureListItem` also lands on its body; `TSText` (SwiftUI) takes a `String` only, with no `LocalizedStringKey` overload; UIKit and Views have no `TSText` (they use `UILabel` + `TSFont` and `TextAppearance.TS.*`); there is no `TSSurface` (`design/specs/components/text.md`, `list-item.md`).

**Views dialogs after recreation:** handlers cannot be saved, so a restored `TSDialogFragment` removes itself and emits `TSDialogFragmentResults.DROPPED`; the app re-presents from its own state (`design/specs/components/dialog.md`).

**Views accessibility delegates (D-123):** Views components other than ListItem, Card, Checkbox and Switch use a replaceable accessibility delegate that a `RecyclerView` can wipe on recycle (Alert, Button, IconButton, Badge, Avatar, Toast, Tabs, BottomNav, TextField).

**Placeholder accessibility (D-064):** both Android toolkits drop the text-field placeholder from accessibility.

**High-contrast themes (C-34):** the token model keys colours by theme id, but every format writes only `light` / `dark`. Adding an increase-contrast theme needs format work in `tools/codegen/src/formats/**`.

**API checks do not cover resources:** string keys, asset names and Android resource names are unchecked on both platforms; treat a rename as breaking by hand ([guides/deprecation-policy.md](guides/deprecation-policy.md)).

## Tech debt

- **Style Dictionary is unused in production (D-013):** `tools/codegen/src/model/to-style-dictionary.ts` and its tests are a bridge kept under test; removal is the default unless a DTCG/web export is wanted.
- **iOS API check tool:** `tools/api-check/src/ios.ts` exists because `swift package diagnose-api-breaking-changes --triple arm64-apple-ios15.0-simulator` builds the package but runs `swift-api-digester -dump-sdk` without `-target`, so every baseline dump is empty and the command fails (Swift 6.3.3, Xcode 26.6); a macOS host build is impossible because the package uses UIKit. The tool builds each module for the simulator triple and runs the digester itself. Delete it when `diagnose-api-breaking-changes --triple` works for this package.
- **Compose below-28 font family** lives in `DsFontWeights` instead of codegen, and the 48 dp touch target and 2 dp ring width are local dimens rather than tokens (D-079).
- **Demo cleanup (D-082):** demo-only versions repeated outside the version catalog; unused Views `PendingComponentFragment`, `page_pending.xml` and `demo_page_pending`; `AccessibilityAuditPolicy.swift` duplicated in both iOS demos with broad acceptances (D-057).
- **Tooling (D-080):** no Make target for `codegen diff-report`, which also classifies an `aliasOf` change as a value change; `release-bump` does not suggest the semver level; Renovate leaves `Package.resolved` stale on SPM PRs.
- **iOS presentation internals (D-072, D-073, D-114, D-116):** the SwiftUI `tsDialog` session can stay claimed after a dismissal from below; the coordinator retries an off-window presentation every main-queue turn; the sheet fit-height measure builds a new hosting controller each time.

## Security considerations

- **Credentials:** CI publishing reads `GITHUB_ACTOR` / `GITHUB_TOKEN` from the environment only; no secret is stored in the repo. `DsPublishingPlugin.kt` reads them with `.orNull` at configuration time, so the token can land in a configuration-cache entry (D-058); prefer `credentials(PasswordCredentials::class)` with `ORG_GRADLE_PROJECT_*` variables.
- **Workflow inputs:** every workflow-fed Makefile variable is read by the shell, never expanded by make, and `release-verify` rejects non-semver tags (`tools/make/test/injection.test.ts`).
- **Write-scoped jobs** install npm dependencies before the token step and check out without persisted credentials.
- **SVG input:** `ds-sync` rejects unsafe SVG content, and codegen reads only allow-listed attributes.
- **Action pins** are major tags, not SHAs (D-003, a user decision).

## Performance and cost

- The release re-runs all four gate workflows, including the emulator and simulator matrices (hours, D-004).
- The iOS API check rebuilds the baseline on every run (about 40 s); emulators cold-boot each leg (D-081).
- SPM clones the whole monorepo, `android/` included (about 114 MB of working tree). Snapshot PNGs are not in the repository at all (C-45).

## Fragile areas

- **No visual-regression gate in CI (C-45, a user decision):** snapshot references are git-ignored and never committed, and CI runs `ios-test` (snapshot suites skipped) and `android-test` (snapshots rendered, nothing compared). CI still catches crashes and behaviour regressions, but a pixel change merges unnoticed unless the contributor recorded a baseline before the change and ran `make ios-snapshot-verify` / `make android-snapshot-verify` ([guides/snapshot-workflow.md](guides/snapshot-workflow.md)). Reviewers rely on the before/after screenshots in the PR's Evidence section. Each contributor's baseline is their own, so two machines can disagree after a toolchain change.
- **Pinned snapshot runtime:** references match iPhone 17 / iOS 26.5 only. A pin bump means every contributor re-records their local baseline ([guides/toolchain-upgrades.md](guides/toolchain-upgrades.md)). The F-02 guard reports a mismatch by crashing the test runner, with the message only in the crash log (D-126).
- **Snapshot tolerance:** Android `changeThreshold` 0.001 can hide a regression the size of a focus ring or dot (D-120).
- **Compose ATF scroll suppression (D-124):** an element taller than about 20 % of the viewport that only straddles the top edge is checked partly.
- **Release edge cases:** a module upload that breaks halfway (files uploaded, no POM) gets HTTP 409 on re-run and needs that version deleted by hand (D-102); commits merged between `release-bump` and the dispatch are released but not listed in the changelog (D-098).

## Test coverage gaps

From the final review audit (D-103 to D-112): no end-to-end test from `tokens.json` to every platform's generated colour; no app-icon or wordmark-switch test; no Views page with a plain `EditText` and MDC `Snackbar`; no 200-row smooth-scroll check; no focus-order or keyboard-vs-touch ring test; no reduce-motion test for the Compose toast, dialog and sheet or the iOS toast; no pseudo-locale or long-string test; parity rests on contact sheets; no live settings test in the SwiftUI demo; the four demos were never installed side by side. Instrumented Android runs on API 26 and 36 happen in CI only (D-054). From issues #2–#6: no test covers the Compose dialog order after a recreate with a queued request (TC-026); the UIKit `afterDismissal` unit test uses the not-presented path because the SPM test host never completes a dismissal (real presentation is covered by the demo chain UI tests); no demo page hides the system bars, so immersive dialogs are covered by Robolectric (`TSDialogSystemBarsTest`, `TSDialogFragmentTest`) plus a one-off device check; the Button / IconButton demo sections for consumer icons and one-line labels have no UI test; the Compose disclosure chevron rotation is covered by spec tests and snapshots only.
