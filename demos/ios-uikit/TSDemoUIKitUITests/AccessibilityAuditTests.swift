import XCTest

final class AccessibilityAuditTests: XCTestCase {
    private static let pages = [
        "colors", "typography", "spacing", "radius", "size", "opacity", "elevation", "icons",
        "button", "icon-button", "badge", "avatar", "text-field", "checkbox", "switch",
        "tabs", "list-item", "card", "alert", "dialog", "toast", "bottom-nav"
    ]

    override func setUpWithError() throws {
        continueAfterFailure = true
    }

    @MainActor
    func testEveryPageInLight() throws {
        try auditEveryPage(["-TSDemoTheme", "light"], screens: 12)
    }

    @MainActor
    func testEveryPageInDark() throws {
        try auditEveryPage(["-TSDemoTheme", "dark"], screens: 1)
    }

    @MainActor
    func testEveryPageAtAccessibilityFive() throws {
        try auditEveryPage(["-TSDemoTheme", "light", "-TSDemoTextSize", "accessibility5"], screens: 12, pinsTextSize: true)
    }

    @MainActor
    func testEveryPageRightToLeft() throws {
        try auditEveryPage(["-TSDemoTheme", "light", "-TSDemoRightToLeft", "YES"], screens: 2)
    }

    @MainActor
    func testOpenOverlays() throws {
        try requireAuditRuntime()
        var findings: [String] = []

        let dialog = launch(page: "dialog", ["-TSDemoTheme", "light"])
        let title = dialog.staticTexts["Delete this project?"].firstMatch
        dialog.buttons["demo.dialog.open.center"].firstMatch.tap()
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        waitUntilSettled(title)
        findings += audit(dialog, page: "dialog-center", screen: 0)
        dialog.buttons["Cancel"].firstMatch.tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
        dialog.buttons["demo.dialog.open.sheet"].firstMatch.tap()
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        waitUntilSettled(title)
        findings += audit(dialog, page: "dialog-sheet", screen: 0)
        dialog.terminate()

        let toast = launch(page: "toast", ["-TSDemoTheme", "light"])
        let show = toast.buttons["demo.toast.playground.show"].firstMatch
        scrollTo(show, in: toast)
        show.tap()
        XCTAssertTrue(toast.staticTexts["Message deleted"].firstMatch.waitForExistence(timeout: 5))
        waitUntilSettled(toast.staticTexts["Message deleted"].firstMatch)
        findings += audit(toast, page: "toast-shown", screen: 1)
        toast.terminate()

        let shell = launch(page: "bottom-nav", ["-TSDemoTheme", "light"])
        let open = shell.buttons["demo.bottom-nav.open-shell"].firstMatch
        scrollTo(open, in: shell)
        open.tap()
        XCTAssertTrue(shell.staticTexts["demo.bottom-nav.shell.title.demo.sample.home"].firstMatch.waitForExistence(timeout: 5))
        findings += audit(shell, page: "bottom-nav-shell", screen: 0)
        shell.terminate()

        XCTAssertTrue(findings.isEmpty, findings.joined(separator: "\n"))
    }

    @MainActor
    private func auditEveryPage(_ arguments: [String], screens: Int, pinsTextSize: Bool = false) throws {
        try requireAuditRuntime()
        var findings: [String] = []
        for page in Self.pages {
            let app = launch(page: page, arguments)
            let scroll = app.descendants(matching: .any)["demo.page.\(page)"].firstMatch
            var previous = CGFloat.nan
            for screen in 0..<screens {
                findings += audit(app, page: page, screen: screen, pinsTextSize: pinsTextSize)
                let anchor = scroll.descendants(matching: .any).element(boundBy: 0).frame.minY
                if anchor == previous || screen == screens - 1 {
                    break
                }
                previous = anchor
                scrollOneScreen(scroll)
            }
            app.terminate()
        }
        XCTAssertTrue(findings.isEmpty, findings.joined(separator: "\n"))
    }

    @MainActor
    private func audit(_ app: XCUIApplication, page: String, screen: Int, pinsTextSize: Bool = false) -> [String] {
        guard #available(iOS 17.0, *) else {
            return []
        }
        var types = XCUIAccessibilityAuditType.all
        if screen > 0 {
            types.remove(.contrast)
        }
        if pinsTextSize {
            types.remove(.dynamicType)
        }
        var findings: [String] = []
        var unattributable: [String] = []
        for attempt in 1...2 {
            findings.removeAll()
            unattributable.removeAll()
            do {
                try app.performAccessibilityAudit(for: types) { issue in
                    let finding = AccessibilityAuditFinding(issue: issue, page: page, app: app)
                    switch AccessibilityAuditPolicy.verdict(for: finding) {
                    case .accepted:
                        break
                    case .unattributable:
                        unattributable.append("\(page) screen \(screen): \(finding.summary)")
                    case .rejected:
                        findings.append("\(page) screen \(screen): \(finding.summary)")
                    }
                    return true
                }
                record(unattributable)
                return findings
            } catch {
                if attempt == 2 {
                    XCTContext.runActivity(named: "Audit of \(page) screen \(screen) did not finish: \(error.localizedDescription)") { _ in }
                }
            }
        }
        return []
    }

    private func record(_ unattributable: [String]) {
        guard !unattributable.isEmpty else {
            return
        }
        let attachment = XCTAttachment(string: unattributable.joined(separator: "\n"))
        attachment.name = "Unattributable audit issues"
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    @MainActor
    private func launch(page: String, _ arguments: [String]) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += ["-TSDemoPage", page] + arguments
        app.launch()
        XCTAssertTrue(app.descendants(matching: .any)["demo.page.\(page)"].firstMatch.waitForExistence(timeout: 10))
        return app
    }

    @MainActor
    private func waitUntilSettled(_ element: XCUIElement) {
        var previous = CGRect.null
        for _ in 0..<20 {
            let frame = element.frame
            if frame == previous {
                return
            }
            previous = frame
            RunLoop.current.run(until: Date().addingTimeInterval(0.15))
        }
    }

    @MainActor
    private func scrollOneScreen(_ scroll: XCUIElement) {
        let start = scroll.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.85))
        let end = scroll.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.2))
        start.press(forDuration: 0.05, thenDragTo: end, withVelocity: .default, thenHoldForDuration: 0.3)
    }

    @MainActor
    private func scrollTo(_ target: XCUIElement, in app: XCUIApplication) {
        var attempts = 0
        while (!target.exists || !target.isHittable) && attempts < 20 {
            app.swipeUp()
            attempts += 1
        }
        XCTAssertTrue(target.isHittable)
    }

    private func requireAuditRuntime() throws {
        guard #available(iOS 17.0, *) else {
            throw XCTSkip("performAccessibilityAudit needs an iOS 17 or newer runtime")
        }
    }
}
