# iOS: getting started

The TS design system ships as one Swift package with three products. Distribution is SPM only, from this repository's `vX.Y.Z` tags.

**Verified** (T50, against a tagged `file://` copy of the repo, Xcode 26.6, iPhone 17 / iOS 26.5): adding the package by URL with an exact version, resolving, building and running both iOS demos from it, with only this package fetched. **Not executed yet:** a clone from `github.com` and a private-repository login, because no tag has been published; those steps are marked *(not executed)*.

## Requirements

- iOS 15 or later.
- Xcode 26.x with the Swift 6 toolchain (the package declares `swift-tools-version: 6.0`).

## 1. Add the package

**Xcode:** File → Add Package Dependencies…, URL `https://github.com/lee5783/ts-design-system.git` *(not executed against github.com)*.

- Version rule: **Exact Version** `0.1.0` (verified), or **Up to Next Minor Version**. While the version is 0.x, a minor release may break, so do not use "Up to Next Major".
- Tags are `vX.Y.Z`; SPM strips the `v`, so version requirements stay plain (`0.1.0`).

**`Package.swift`:**

```swift
dependencies: [
    .package(url: "https://github.com/lee5783/ts-design-system.git", exact: "0.1.0")
    // or: .upToNextMinor(from: "0.1.0")
],
targets: [
    .target(
        name: "MyApp",
        dependencies: [
            .product(name: "TSDesignSystem", package: "ts-design-system")
        ]
    )
]
```

**Private repository** *(not executed)*: Xcode or git needs GitHub credentials: a GitHub account in Xcode → Settings → Accounts, an SSH URL (`git@github.com:lee5783/ts-design-system.git`), or a git credential helper.

## 2. Pick one product per target

| Product | Use for | Import |
|---|---|---|
| `TSDesignSystem` | SwiftUI apps | `import TSDesignSystem` |
| `TSDesignSystemUIKit` | UIKit apps | `import TSDesignSystemUIKit` |
| `TSTokens` | tokens only (colours, spacing, typography values, icon names) | `import TSTokens` |

Both UI products re-export `TSFoundation` (fonts, style specs, strings, resources) and `TSTokens`, so one import is enough. `TSFoundation` is not a product; do not try to add it.

## 3. What gets fetched

- Only `ts-design-system` is fetched. The repo's test-only packages (swift-snapshot-testing, ViewInspector) are not, even though the repo's own `Package.resolved` pins them.
- SPM clones the whole monorepo, `android/` included (about 114 MB of working tree).
- The repository holds no snapshot test PNGs and no Git LFS objects, so the checkout needs no git-lfs.

## 4. First screen

**SwiftUI:**

```swift
import SwiftUI
import TSDesignSystem

@main
struct MyApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .tsToastHost()
        }
    }
}

struct ContentView: View {
    @State private var showsDialog = false

    var body: some View {
        TSButton("Delete", variant: .danger) { showsDialog = true }
            .tsDialog(
                isPresented: $showsDialog,
                title: "Delete item?",
                message: "This cannot be undone.",
                actions: [
                    TSDialogAction("Cancel"),
                    TSDialogAction("Delete", variant: .danger) {
                        TSToastQueue.shared.enqueue(TSToastItem(message: "Item deleted"))
                    }
                ]
            )
    }
}
```

**UIKit:**

```swift
import UIKit
import TSDesignSystemUIKit

final class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?
    private var toastWindow: TSToastWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = UINavigationController(rootViewController: ViewController())
        window.makeKeyAndVisible()
        self.window = window
        toastWindow = TSToastWindow(windowScene: windowScene)
    }
}

final class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        let button = TSButtonView(title: "Delete", variant: .danger, primaryAction: UIAction { [weak self] _ in
            guard let self else { return }
            let dialog = TSDialogViewController(title: "Delete item?", message: "This cannot be undone.", actions: [
                TSDialogAction("Cancel"),
                TSDialogAction("Delete", variant: .danger)
            ])
            _ = dialog.present(from: self)
        })
        button.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(button)
        NSLayoutConstraint.activate([
            button.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            button.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
    }
}
```

Rules that matter from day one:

- Install **one toast host per scene**: `.tsToastHost()` at the SwiftUI root, `TSToastWindow(windowScene:)` in the scene delegate. If your app overrides traits on a container, forward them with `toastWindow.applyTraits(from:)`.
- Present UIKit dialogs with `present(from:)`, not `present(_:animated:)`; it returns `false` when another TS dialog is open (dialogs never stack). SwiftUI calls `onRefused:` instead.
- Colours follow the system light/dark appearance. Theming, fonts and string overrides: [theming.md](theming.md).

Each component's full API (props, states, accessibility) is in `design/specs/components/<name>.md`; the SwiftUI and UIKit demo apps show every variant ([../guides/running-demos.md](../guides/running-demos.md)).
