import XCTest

final class OverlayFlowsTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testLongCenterDialogAtAccessibilityFiveKeepsItsActionsReachable() {
        let app = launch(page: "dialog", ["-TSDemoTextSize", "accessibility5"])
        flip(app, "demo.dialog.playground.long")
        let open = element(app, "demo.dialog.playground.open")
        scrollTo(open, in: app)
        open.tap()
        let archive = app.buttons["Archive"].firstMatch
        XCTAssertTrue(archive.waitForExistence(timeout: 5))
        var attempts = 0
        while !archive.isHittable && attempts < 10 {
            app.swipeUp()
            attempts += 1
        }
        XCTAssertTrue(archive.isHittable)
        attachScreenshot(app, named: "dialog-center-long-ax5")
        archive.tap()
        XCTAssertTrue(archive.waitForNonExistence(timeout: 5))
        let result = element(app, "demo.dialog.result")
        scrollBackTo(result, in: app)
        XCTAssertEqual(result.label, "Result: Archive")
    }

    @MainActor
    func testSheetInDarkRightToLeftMirrorsAndDismisses() {
        let app = launch(page: "dialog", ["-TSDemoTheme", "dark", "-TSDemoRightToLeft", "YES"])
        let title = app.staticTexts["Delete this project?"].firstMatch
        element(app, "demo.dialog.open.sheet").tap()
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        title.waitUntilSettled()
        let quarter = title.frame.width / 4
        let rightQuarter = CGRect(x: title.frame.maxX - quarter, y: title.frame.minY, width: quarter, height: title.frame.height)
        let leftQuarter = CGRect(x: title.frame.minX, y: title.frame.minY, width: quarter, height: title.frame.height)
        XCTAssertGreaterThan(app.inkContrast(in: rightQuarter), app.inkContrast(in: leftQuarter) + 0.2, "The sheet title should start at the right edge in RTL")
        let cancel = app.buttons["Cancel"].firstMatch
        let delete = app.buttons["Delete"].firstMatch
        XCTAssertTrue(cancel.isHittable)
        XCTAssertTrue(delete.isHittable)
        if abs(cancel.frame.midY - delete.frame.midY) < 4 {
            XCTAssertGreaterThan(cancel.frame.midX, delete.frame.midX, "Side-by-side actions should mirror in RTL")
        }
        attachScreenshot(app, named: "dialog-sheet-dark-rtl")
        cancel.tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
        XCTAssertEqual(element(app, "demo.dialog.result").label, "Result: Cancel")
    }

    @MainActor
    func testInputDialogTypesWithTheKeyboardUpAndKeepsItsActionsVisible() throws {
        let app = launch(page: "dialog")
        element(app, "demo.dialog.open.input").tap()
        let field = app.textFields["demo.dialog.input.field"].firstMatch
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.8)).tap()
        try requireSoftwareKeyboard(app)
        let current = field.value as? String ?? ""
        field.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: current.count) + "Roadmap")
        XCTAssertEqual(field.value as? String, "Roadmap")
        let rename = app.buttons["Rename"].firstMatch
        XCTAssertTrue(rename.isHittable)
        XCTAssertLessThanOrEqual(rename.frame.maxY, app.keyboards.firstMatch.frame.minY)
        attachScreenshot(app, named: "dialog-input-keyboard")
        rename.tap()
        XCTAssertTrue(field.waitForNonExistence(timeout: 5))
        XCTAssertTrue(element(app, "demo.dialog.result").label.hasSuffix("Roadmap"), element(app, "demo.dialog.result").label)
    }

    @MainActor
    func testToastShownWhileTypingStaysAboveTheKeyboard() throws {
        let app = launch(page: "toast")
        let field = app.textFields["demo.toast.typing.field"].firstMatch
        scrollTo(field, in: app)
        field.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.8)).tap()
        try requireSoftwareKeyboard(app)
        let keyboard = app.keyboards.firstMatch
        let show = element(app, "demo.toast.typing.show")
        XCTAssertTrue(show.isHittable)
        show.tap()
        let toast = app.images["check-circle"].firstMatch
        XCTAssertTrue(toast.waitForExistence(timeout: 5))
        XCTAssertTrue(keyboard.exists)
        XCTAssertLessThanOrEqual(toast.frame.maxY, keyboard.frame.minY, "The toast \(toast.frame) is covered by the keyboard \(keyboard.frame)")
        XCTAssertTrue(toast.isHittable)
        attachScreenshot(app, named: "toast-while-typing")
    }

    @MainActor
    func testToastUndoAtAccessibilityFiveInRightToLeft() {
        let app = launch(page: "toast", ["-TSDemoTextSize", "accessibility5", "-TSDemoRightToLeft", "YES"])
        let show = element(app, "demo.toast.playground.show")
        let counter = element(app, "demo.toast.playground.count")
        scrollTo(counter, in: app)
        let baseline = iconCount(app, "info")
        show.tap()
        XCTAssertTrue(waitForIconCount(app, "info", baseline + 1, timeout: 5))
        attachScreenshot(app, named: "toast-undo-ax5-rtl")
        let window = app.windows.firstMatch.frame
        app.windows.firstMatch.coordinate(withNormalizedOffset: CGVector(dx: 0.12, dy: toastIcon(app, "info").frame.midY / window.height)).tap()
        XCTAssertTrue(waitForIconCount(app, "info", baseline, timeout: 5))
        XCTAssertEqual(counter.label, "Tapped 1 times")
    }

    @MainActor
    func testToastFromASheetOutlivesTheSheet() {
        let app = launch(page: "toast")
        let open = element(app, "demo.toast.sheet.open")
        scrollTo(open, in: app)
        open.tap()
        let title = app.staticTexts["Toasts sit above sheets"].firstMatch
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        let baseline = iconCount(app, "check-circle")
        app.buttons["Copy link"].firstMatch.tap()
        XCTAssertTrue(waitForIconCount(app, "check-circle", baseline + 1, timeout: 5))
        let toastFrame = toastIcon(app, "check-circle").frame
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.12)).tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 3))
        XCTAssertTrue(hasIcon(app, "check-circle", at: toastFrame), "The toast should outlive the sheet it was raised from")
        attachScreenshot(app, named: "toast-after-sheet")
        XCTAssertTrue(waitForNoIcon(app, "check-circle", at: toastFrame, timeout: 10))
    }

    @MainActor
    func testBottomNavShellMirrorsInRightToLeft() {
        let app = launch(page: "bottom-nav", ["-TSDemoRightToLeft", "YES"])
        let open = app.buttons["demo.bottom-nav.open-shell"].firstMatch
        scrollTo(open, in: app)
        open.tap()
        XCTAssertTrue(app.staticTexts["demo.bottom-nav.shell.title.demo.sample.home"].firstMatch.waitForExistence(timeout: 5))
        attachScreenshot(app, named: "bottom-nav-shell-rtl")
        XCTAssertEqual(app.tabBars.count, 0)
        let window = app.windows.firstMatch
        window.coordinate(withNormalizedOffset: CGVector(dx: 1 - 1.5 / 4, dy: (window.frame.height - 66) / window.frame.height)).tap()
        XCTAssertTrue(app.staticTexts["demo.bottom-nav.shell.title.demo.sample.inbox"].firstMatch.waitForExistence(timeout: 2))
    }

    @MainActor
    private func iconCount(_ app: XCUIApplication, _ identifier: String) -> Int {
        app.images.matching(identifier: identifier).count
    }

    @MainActor
    private func toastIcon(_ app: XCUIApplication, _ identifier: String) -> XCUIElement {
        let icons = app.images.matching(identifier: identifier).allElementsBoundByIndex
        return icons.max { $0.frame.midY < $1.frame.midY } ?? app.images[identifier].firstMatch
    }

    @MainActor
    private func waitForIconCount(_ app: XCUIApplication, _ identifier: String, _ count: Int, timeout: TimeInterval) -> Bool {
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            if iconCount(app, identifier) == count { return true }
            usleep(100_000)
        }
        return false
    }

    @MainActor
    private func hasIcon(_ app: XCUIApplication, _ identifier: String, at frame: CGRect) -> Bool {
        let icons = app.images.matching(identifier: identifier)
        return (0..<icons.count).contains { index in
            guard let icon = try? icons.element(boundBy: index).snapshot().frame else { return false }
            return abs(icon.midY - frame.midY) < 4 && abs(icon.midX - frame.midX) < 4
        }
    }

    @MainActor
    private func waitForNoIcon(_ app: XCUIApplication, _ identifier: String, at frame: CGRect, timeout: TimeInterval) -> Bool {
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            if !hasIcon(app, identifier, at: frame) { return true }
            usleep(100_000)
        }
        return false
    }

    @MainActor
    private func launch(page: String, _ arguments: [String] = []) -> XCUIApplication {
        let app = XCUIApplication()
        let theme = arguments.contains("-TSDemoTheme") ? [] : ["-TSDemoTheme", "light"]
        app.launchArguments += ["-TSDemoPage", page] + theme + arguments
        app.launch()
        XCTAssertTrue(app.descendants(matching: .any)["demo.page.\(page)"].firstMatch.waitForExistence(timeout: 10))
        return app
    }

    @MainActor
    private func flip(_ app: XCUIApplication, _ identifier: String) {
        let toggle = app.switches[identifier].firstMatch
        scrollTo(toggle, in: app)
        toggle.tap()
    }

    @MainActor
    private func requireSoftwareKeyboard(_ app: XCUIApplication) throws {
        let keyboard = app.keyboards.firstMatch
        let onScreen = NSPredicate { _, _ in keyboard.exists && keyboard.frame.intersects(app.windows.firstMatch.frame) }
        let expectation = XCTNSPredicateExpectation(predicate: onScreen, object: nil)
        guard XCTWaiter().wait(for: [expectation], timeout: 5) == .completed else {
            throw XCTSkip("No software keyboard: the simulator has a hardware keyboard connected (I/O > Keyboard > Connect Hardware Keyboard)")
        }
    }

    @MainActor
    private func attachScreenshot(_ app: XCUIApplication, named name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    private func element(_ app: XCUIApplication, _ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier].firstMatch
    }

    @MainActor
    private func scrollTo(_ target: XCUIElement, in app: XCUIApplication) {
        var attempts = 0
        while (!target.exists || !target.isHittable) && attempts < 30 {
            app.swipeUp()
            attempts += 1
        }
        XCTAssertTrue(target.isHittable)
    }

    @MainActor
    private func scrollBackTo(_ target: XCUIElement, in app: XCUIApplication) {
        var attempts = 0
        while (!target.exists || !target.isHittable) && attempts < 30 {
            app.swipeDown()
            attempts += 1
        }
        XCTAssertTrue(target.isHittable)
    }
}
