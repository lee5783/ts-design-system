# Migration guides

One section per release that removes or renames public API, tokens, string keys or resources, newest first. Deprecations are announced a minor release ahead with a replacement ([../guides/deprecation-policy.md](../guides/deprecation-policy.md)); this page lists what to change when the deprecated API is finally removed, and anything the API checks cannot see (string keys, resource names, behaviour changes).

`CHANGELOG.md` lists every change; breaking commits are marked `[**breaking**]` there.

## 0.1.0

First release. Nothing to migrate.

Names that changed during pre-release development, in case early integration code used them:

| Before | Now |
|---|---|
| SwiftUI `TSIconView` | `TSIcon` |
| UIKit `TSIconImageView` | `TSIconView` |

## Template for a breaking release

```markdown
## X.0.0

### Removed
| Platform | Removed | Use instead | Deprecated since |
|---|---|---|---|
| iOS | `TSOld` | `TSNew` | 1.4.0 |

### Renamed tokens
| Old token | New token |
|---|---|

### Changed behaviour
- What changed, what to do.

### Minimum OS
- iOS 16 / API 28 (if raised).
```
