# TS Design System

Native libraries for the TinySolution (TS) design system: tokens, icons and 15 components (Alert, Avatar, Badge, BottomNav, Button, Card, Checkbox, Dialog, Icon, IconButton, ListItem, Switch, Tabs, TextField, Toast), built natively for **SwiftUI, UIKit, Jetpack Compose and Android Views** from one checked-in copy of the design system. iOS 15+ and Android API 26+, light and dark, Dynamic Type and font scale, VoiceOver and TalkBack, RTL.

One version covers both platforms (`VERSION`, currently `0.1.0`).

## iOS (Swift Package Manager)

```swift
.package(url: "https://github.com/lee5783/ts-design-system.git", exact: "0.1.0")
```

| Product | For |
|---|---|
| `TSDesignSystem` | SwiftUI |
| `TSDesignSystemUIKit` | UIKit |
| `TSTokens` | tokens only |

```swift
.product(name: "TSDesignSystem", package: "ts-design-system")
```

Full setup: [docs/consumers/ios-getting-started.md](docs/consumers/ios-getting-started.md).

## Android (GitHub Packages)

`settings.gradle.kts` (GitHub Packages needs a token with `read:packages`, even for public packages):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/lee5783/ts-design-system")
            credentials {
                username = providers.gradleProperty("gpr.user").get()
                password = providers.gradleProperty("gpr.key").get()
            }
            content { includeGroup("com.ts.ds") }
        }
    }
}
```

App `build.gradle.kts`:

```kotlin
implementation(platform("com.ts.ds:bom:0.1.0"))
implementation("com.ts.ds:compose")   // or "com.ts.ds:views"
```

Full setup, CI credentials and local testing: [docs/consumers/android-getting-started.md](docs/consumers/android-getting-started.md). Theming and string overrides: [docs/consumers/theming.md](docs/consumers/theming.md).

## Demo apps

Each toolkit has its own demo app with every token and every component variant, size and state. They live in `demos/` and consume the library like an external app.

| App | Toolkit | Run |
|---|---|---|
| [`demos/ios-swiftui`](demos/ios-swiftui) | SwiftUI | `make demo-ios-swiftui-run` |
| [`demos/ios-uikit`](demos/ios-uikit) | UIKit | `make demo-ios-uikit-run` |
| [`demos/android-compose`](demos/android-compose) | Compose | `make demo-android-compose`, then install from Android Studio or `./gradlew :app:installDebug` |
| [`demos/android-views`](demos/android-views) | Views | `make demo-android-views`, then install the same way |

Launch options, the theme toggle and deep links: [docs/guides/running-demos.md](docs/guides/running-demos.md).

## Repository layout

| Path | Contents |
|---|---|
| `design/` | The DS source copy, assets, local token extensions, per-component specs (`design/specs/components/`) |
| `tools/` | Sync, code generator, CI and release tooling (Node 22, Vitest) |
| `ios/`, `Package.swift` | The Swift package |
| `android/` | The Gradle project (`tokens`, `core`, `compose`, `views`, `bom`) |
| `demos/` | The four demo apps |
| `docs/` | Architecture, conventions, testing, maintainer and consumer guides |

## Development

Requirements: macOS, Xcode 26.6, JDK 21, Node 22 (`mise install`).

```bash
make help                              # every target
make test                              # tools, codegen and sync checks, demo boundaries, iOS and Android suites
make ios-snapshot-record               # record local snapshot references (git-ignored; or android-snapshot-record)
make ios-snapshot-verify               # compare against them after a UI change (or android-snapshot-verify)
```

Start at [docs/README.md](docs/README.md). Updating the design system: [docs/guides/ds-update-flow.md](docs/guides/ds-update-flow.md). Releasing: [docs/guides/release.md](docs/guides/release.md).

## License

MIT, see [LICENSE](LICENSE).
