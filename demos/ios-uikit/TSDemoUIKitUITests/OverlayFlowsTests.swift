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
        let toast = app.staticTexts["Success, Changes saved"].firstMatch
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
        show.tap()
        let message = app.staticTexts["Message deleted"].firstMatch
        XCTAssertTrue(message.waitForExistence(timeout: 5))
        let undo = app.buttons.matching(NSPredicate(format: "label == 'Undo'")).allElementsBoundByIndex
            .first { $0.frame.minY >= message.frame.minY - 200 && $0.isHittable }
        XCTAssertNotNil(undo)
        attachScreenshot(app, named: "toast-undo-ax5-rtl")
        undo?.tap()
        XCTAssertTrue(message.waitForNonExistence(timeout: 5))
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
        app.buttons["Copy link"].firstMatch.tap()
        let toast = app.staticTexts["Success, Link copied"].firstMatch
        XCTAssertTrue(toast.waitForExistence(timeout: 5))
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.12)).tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 3))
        XCTAssertTrue(toast.exists, "The toast should outlive the sheet it was raised from")
        XCTAssertTrue(toast.isHittable)
        attachScreenshot(app, named: "toast-after-sheet")
        XCTAssertTrue(toast.waitForNonExistence(timeout: 10))
    }

    @MainActor
    func testBottomNavShellMirrorsInRightToLeft() {
        let app = launch(page: "bottom-nav", ["-TSDemoRightToLeft", "YES"])
        let open = app.buttons["demo.bottom-nav.open-shell"].firstMatch
        scrollTo(open, in: app)
        open.tap()
        XCTAssertTrue(app.staticTexts["demo.bottom-nav.shell.title.demo.sample.home"].firstMatch.waitForExistence(timeout: 5))
        let home = shellTab("Home", in: app)
        let inbox = shellTab("Inbox", in: app)
        XCTAssertGreaterThan(home.frame.midX, inbox.frame.midX)
        attachScreenshot(app, named: "bottom-nav-shell-rtl")
        XCTAssertEqual(app.tabBars.count, 0)
        inbox.tap()
        XCTAssertTrue(inbox.isSelected)
        XCTAssertTrue(app.staticTexts["demo.bottom-nav.shell.title.demo.sample.inbox"].firstMatch.waitForExistence(timeout: 2))
    }

    @MainActor
    private func shellTab(_ label: String, in app: XCUIApplication) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@ AND value BEGINSWITH %@", label, "Tab, "))
            .firstMatch
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
