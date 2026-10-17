import XCTest

struct AccessibilityAuditFinding {
    enum Kind: String {
        case contrast
        case dynamicType
        case textClipped
        case hitRegion
        case elementDetection
        case description
        case other
    }

    let kind: Kind
    let page: String
    let compactDescription: String
    let elementType: XCUIElement.ElementType?
    let label: String
    let identifier: String
    let isEnabled: Bool
    let frame: CGRect
    let navigationBarMaxY: CGFloat

    @available(iOS 17.0, *)
    @MainActor
    init(issue: XCUIAccessibilityAuditIssue, page: String, app: XCUIApplication) {
        kind = Self.kind(of: issue.auditType)
        self.page = page
        compactDescription = issue.compactDescription
        elementType = issue.element?.elementType
        label = issue.element?.label ?? ""
        identifier = issue.element?.identifier ?? ""
        isEnabled = issue.element?.isEnabled ?? true
        frame = issue.element?.frame ?? .null
        navigationBarMaxY = app.navigationBars.firstMatch.exists ? app.navigationBars.firstMatch.frame.maxY : 0
    }

    var hasElement: Bool {
        elementType != nil
    }

    var summary: String {
        let element = elementType.map { "type \($0.rawValue) '\(identifier)' '\(label)' enabled=\(isEnabled) frame=\(frame)" } ?? "no element"
        return "[\(kind.rawValue)] \(compactDescription) :: \(element)"
    }

    @available(iOS 17.0, *)
    private static func kind(of type: XCUIAccessibilityAuditType) -> Kind {
        switch type {
        case .contrast: .contrast
        case .dynamicType: .dynamicType
        case .textClipped: .textClipped
        case .hitRegion: .hitRegion
        case .elementDetection: .elementDetection
        case .sufficientElementDescription: .description
        default: .other
        }
    }
}

enum AccessibilityAuditPolicy {
    enum Verdict {
        case accepted
        case unattributable
        case rejected
    }

    private static let scrollEdgeBand: CGFloat = 32
    private static let compositeControlLabels: [String: Set<String>] = [
        "switch": ["Wi-Fi", "Notifications", "Dark mode"],
        "bottom-nav": ["Home", "Inbox", "Search", "Alerts", "Profile"],
        "bottom-nav-shell": ["Home", "Inbox", "Search", "Alerts"],
    ]
    private static let elevatedSurfacePages: Set<String> = ["toast"]
    private static let fixedSizeTextPages: Set<String> = ["avatar", "bottom-nav", "bottom-nav-shell"]
    private static let truncatingOrScaledTextPages: Set<String> = ["badge", "tabs", "dialog-sheet"]

    static func verdict(for finding: AccessibilityAuditFinding) -> Verdict {
        if !finding.hasElement {
            return finding.kind == .contrast ? .accepted : .unattributable
        }
        return accepts(finding) ? .accepted : .rejected
    }

    private static func accepts(_ finding: AccessibilityAuditFinding) -> Bool {
        switch finding.kind {
        case .contrast:
            acceptsContrast(finding)
        case .dynamicType:
            acceptsDynamicType(finding)
        case .textClipped:
            acceptsTextClipped(finding)
        default:
            false
        }
    }

    private static func acceptsContrast(_ finding: AccessibilityAuditFinding) -> Bool {
        if !finding.isEnabled || elevatedSurfacePages.contains(finding.page) {
            return true
        }
        if finding.frame.minY < finding.navigationBarMaxY + scrollEdgeBand {
            return true
        }
        return compositeControlLabels[finding.page]?.contains(finding.label) == true
    }

    private static func acceptsDynamicType(_ finding: AccessibilityAuditFinding) -> Bool {
        if finding.compactDescription.contains("partially") {
            return true
        }
        return fixedSizeTextPages.contains(finding.page)
    }

    private static func acceptsTextClipped(_ finding: AccessibilityAuditFinding) -> Bool {
        if finding.elementType == .textField || isOverlineHeader(finding.label) {
            return true
        }
        return truncatingOrScaledTextPages.contains(finding.page)
    }

    private static func isOverlineHeader(_ label: String) -> Bool {
        label.contains { $0.isLetter } && label == label.uppercased()
    }
}
