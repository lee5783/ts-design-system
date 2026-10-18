# Android: getting started

The TS design system ships as five Maven artifacts in group `com.ts.ds` on GitHub Packages: a BOM, the two toolkits (`compose`, `views`) and their shared dependencies (`core`, `tokens`). All five share one version.

**Verified** (T50, with the `0.1.0` publication in `~/.m2` standing in for GitHub Packages): the BOM plus a toolkit artifact resolve and build in both demo apps, with `core` and `tokens` coming transitively, and the demos' smoke tests pass against the published artifacts. The repository block with the `content { includeGroup("com.ts.ds") }` filter was exercised. **Not executed yet:** a real download from `maven.pkg.github.com` with a personal access token, because nothing has been published; those steps are marked *(not executed)*.

## Requirements

- `minSdk` 26 or higher; the libraries compile against SDK 36.
- Java 17 bytecode target (the libraries are built with JDK 21 targeting 17).
- Compose apps: the Compose BOM comes with `com.ts.ds:compose` as an `api` dependency (`2025.10.01` in `0.1.0`), so your Compose versions align with it.

## 1. Create a GitHub token *(not executed)*

GitHub Packages needs a token even for public packages.

- **Classic token:** scope `read:packages`, plus `repo` if the repository is private (the package inherits the repository's visibility).
- **Fine-grained token:** Packages → read, on the account or organization that owns the repository.

Store it outside the project, in `~/.gradle/gradle.properties`:

```properties
gpr.user=<github-username>
gpr.key=<token>
```

In CI, use `GITHUB_ACTOR` / `GITHUB_TOKEN` instead (the snippet below falls back to them). Never commit a token.

## 2. Add the repository

`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "TSDesignSystem"
            url = uri("https://maven.pkg.github.com/lee5783/ts-design-system")
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("GITHUB_ACTOR")).get()
                password = providers.gradleProperty("gpr.key").orElse(providers.environmentVariable("GITHUB_TOKEN")).get()
            }
            content { includeGroup("com.ts.ds") }
        }
    }
}
```

`gpr.user` / `gpr.key` are names you choose; any property names work as long as the snippet reads them. This repository's own demo apps use a different, Gradle-derived pair, `GitHubPackagesUsername` / `GitHubPackagesPassword`, because they declare `credentials(PasswordCredentials::class)` on a repository named `GitHubPackages` (see [../integrations.md](../integrations.md#authentication-and-identity)). A consumer can use that form too:

```kotlin
maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/lee5783/ts-design-system")
    credentials(PasswordCredentials::class)   // reads GitHubPackagesUsername / GitHubPackagesPassword
    content { includeGroup("com.ts.ds") }
}
```

## 3. Add the dependencies

App module `build.gradle.kts`:

```kotlin
dependencies {
    implementation(platform("com.ts.ds:bom:0.1.0"))
    implementation("com.ts.ds:compose")   // Compose apps
    // implementation("com.ts.ds:views")  // Views (XML) apps
}
```

Do not declare `com.ts.ds:core` or `com.ts.ds:tokens`: they come transitively at the BOM version.

## Try a release locally, without a token

From a checkout of this repository: `make android-publish-local` publishes every artifact and the BOM to `~/.m2`. Replace the `maven { … }` block with:

```kotlin
mavenLocal { content { includeGroup("com.ts.ds") } }
```

## 4. First screen

**Compose:**

```kotlin
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.compose.components.dialog.TSDialog
import com.ts.ds.compose.components.toast.LocalTSToastState
import com.ts.ds.compose.components.toast.TSToastHost
import com.ts.ds.compose.components.toast.rememberTSToastState
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.style.TSButtonVariant

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
<com.ts.ds.views.components.button.TSButtonView
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
