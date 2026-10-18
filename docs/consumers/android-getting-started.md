# Android: getting started

The TS design system ships through [JitPack](https://jitpack.io), built from this repository's `vX.Y.Z` tags. There are five Maven artifacts in group `com.github.lee5783.ts-design-system`: a BOM, the two toolkits (`compose`, `views`) and their shared dependencies (`core`, `tokens`). All five share one version, which is the git tag (with its `v`, for example `v0.2.0`). No credentials are needed.

**Verified locally:** a JitPack-shaped build (`JITPACK=true GROUP=com.github.lee5783 ARTIFACT=ts-design-system VERSION=<tag>`, the exact `install` line from the root `jitpack.yml`) publishes all five artifacts under `com.github.lee5783.ts-design-system:<module>:<tag>`, and the BOM constraints and the `compose`/`views` → `core` → `tokens` POM dependencies carry that same group and version. Both demo apps build against the `~/.m2` publication. **Not executed yet:** a real download from `jitpack.io`, because no tag with `jitpack.yml` has been built there. `v0.1.0` predates `jitpack.yml` and cannot be built by JitPack; the first consumable version is the next tag.

## Requirements

- `minSdk` 26 or higher; the libraries compile against SDK 36.
- Java 17 bytecode target (the libraries are built with JDK 21 targeting 17).
- Compose apps: the Compose BOM comes with `compose` as an `api` dependency (`2025.10.01` today), so your Compose versions align with it.

## 1. Add the repository

`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") {
            content { includeGroup("com.github.lee5783.ts-design-system") }
        }
    }
}
```

The `content` filter keeps Gradle from asking JitPack for anything else. No token, no `gradle.properties` entries.

## 2. Add the dependencies

App module `build.gradle.kts`:

```kotlin
dependencies {
    implementation(platform("com.github.lee5783.ts-design-system:bom:<tag>"))
    implementation("com.github.lee5783.ts-design-system:compose")   // Compose apps
    // implementation("com.github.lee5783.ts-design-system:views")  // Views (XML) apps
}
```

`<tag>` is the release tag, `v` included (for example `v0.2.0`). Do not declare `core` or `tokens`: they come transitively at the BOM version.

The first request for a new tag makes JitPack build it, which can take several minutes; a Gradle sync that times out on a fresh tag usually succeeds on retry. Build logs are at `https://jitpack.io/#lee5783/ts-design-system`.

## Try a build locally, without JitPack

From a checkout of this repository, `make android-publish-local` publishes every artifact and the BOM to `~/.m2` under the local group `com.tinysol.designsystem` and the version in `VERSION` (for example `com.tinysol.designsystem:bom:0.1.0`). In a scratch app, replace the JitPack block with:

```kotlin
mavenLocal { content { includeGroup("com.tinysol.designsystem") } }
```

and use `com.tinysol.designsystem:<module>` coordinates. This is a development aid only; consumers use JitPack.

## 3. First screen

**Compose:**

```kotlin
import com.tinysol.designsystem.compose.components.button.TSButton
import com.tinysol.designsystem.compose.components.dialog.TSDialog
import com.tinysol.designsystem.compose.components.toast.LocalTSToastState
import com.tinysol.designsystem.compose.components.toast.TSToastHost
import com.tinysol.designsystem.compose.components.toast.rememberTSToastState
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.core.logic.TSDialogAction
import com.tinysol.designsystem.core.style.TSButtonVariant

setContent {
    TSTheme {
        TSToastHost(rememberTSToastState()) {
            var showsDialog by rememberSaveable { mutableStateOf(false) }
            val toasts = LocalTSToastState.current
            TSButton("Delete", onClick = { showsDialog = true }, variant = TSButtonVariant.Danger)
            TSDialog(
                isPresented = showsDialog,
                onPresentedChange = { showsDialog = it },
                title = "Delete item?",
                message = "This cannot be undone.",
                actions = listOf(
                    TSDialogAction("Cancel"),
                    TSDialogAction("Delete", TSButtonVariant.Danger) { toasts?.show("Item deleted") },
                ),
            )
        }
    }
}
```

**Views:** use `Theme.TS` (or a theme whose parent is `Theme.TS`) for the activity, then:

```xml
<com.tinysol.designsystem.views.components.button.TSButtonView
    android:id="@+id/delete"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Delete"
    app:tsVariant="danger" />
```

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    TSToastHost.install(this)
    findViewById<TSButtonView>(R.id.delete).setOnClickListener {
        TSDialogFragment().apply {
            dialogTitle = "Delete item?"
            message = "This cannot be undone."
            actions = listOf(
                TSDialogAction("Cancel"),
                TSDialogAction("Delete", TSButtonVariant.Danger) { TSToastHost.from(this@MainActivity)?.show("Item deleted") },
            )
        }.present(supportFragmentManager, "delete")
    }
}
```

Rules that matter from day one:

- Install **one toast host per Activity**: `TSToastHost(state) { … }` around the Compose content, `TSToastHost.install(activity)` after `setContentView` in Views. If you keep the queue across a recreate, call `detach()` in `onDestroy` (`design/specs/components/toast.md`).
- Dialogs never stack. A second Compose `TSDialog` is **queued** and shown when the first closes (its `isPresented` stays `true`); set `TSDialogPresenter.shared.policy = TSDialogPolicy.Refuse` to refuse instead (then `onRefused` is called). Views `present(manager, tag)` returns `false` while another dialog is open. Always wire `onPresentedChange` to your state. An action's handler runs **before** the dialog closes, so it still sees the state the dialog was opened for. A Views dialog is not restored after recreation; re-present it from your own state and listen for `TSDialogFragmentResults.DROPPED` (`design/specs/components/dialog.md`).
- Dialogs keep immersive mode: a dialog opened over a screen with hidden system bars keeps them hidden. Nothing to configure.
- Give `TSListItemView` rows stable ids, so switch state survives a recreate.
- Dynamic colour is never applied; the TS palette is fixed. Theming, typography and string overrides: [theming.md](theming.md).

## TS building blocks instead of Material

Use these inside TS screens rather than a Material fallback, so stroke, size, colour and type stay on the spec:

| Need | Compose | Views | Instead of |
|---|---|---|---|
| Stand-alone loading indicator | `TSSpinner(size = TSSpinnerSize.Md, tone = TSSpinnerTone.Primary)` | `TSSpinnerView` (`app:tsSpinnerSize`, `app:tsSpinnerTone`) | `CircularProgressIndicator`, `ProgressBar` |
| Divider between rows outside a `TSList` (a `LazyColumn`, a plain stack) | `TSDivider()` | `TSDividerView`; in a `RecyclerView` keep `TSListDecoration` | `HorizontalDivider`, `MaterialDivider` |
| Row that expands into body text (licences, FAQs) | `TSDisclosureListItem(title, expanded, onExpandedChange) { … }` | `TSDisclosureListItemView` (XML children become the body) | a hand-built clickable `Column` |
| Text in a TS type role and colour | `TSText(text, role = TSTypeRole.Body, color = TSTextColor.Primary)` | `TextView` + `@style/TextAppearance.TS.<Role>` + `?attr/tsColorText<Colour>` | `Text` with `MaterialTheme.typography` |
| A product icon in a button | `TSButton(…, leadingIconSource = TSIconSource.Vector(myIcon))`, `TSIconButton(icon = TSIconSource.Painter(…), …)` | `leadingIconDrawable`, `setLeadingIconResource(…)`, `iconDrawable` | a Material `Button` / `IconButton` |
| A join-code / room-token field | `TSTextField(value, label, onValueChange, contentStyle = TSTextFieldContentStyle.Code, inputTraits = TSTextInputTraits.Code)` | `app:tsContentStyle="code"` + `app:tsInputTraits="code"` | `OutlinedTextField` |
| UI over live media or a game controller | `TSTheme.controllerColors` ([theming.md](theming.md#controller-and-media-overlay-colours)) | `@color/ts_controller_*` | app-local copies of the palette |

Contracts: `design/specs/components/{spinner,list-item,text,button,icon-button,text-field}.md`.

Each component's API is in `design/specs/components/<name>.md`; the Compose and Views demo apps show every variant ([../guides/running-demos.md](../guides/running-demos.md)).
