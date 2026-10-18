import XCTest

final class FeedbackComponentsTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testAlertPlaygroundActionIsSeparateFromTheMessage() {
        let app = launch(page: "alert")
        XCTAssertTrue(playgroundAlert(app).waitForExistence(timeout: 5))
        flip(app, "demo.alert.playground.action")
        let retry = element(app, "demo.alert.playground.retry")
        let counter = element(app, "demo.alert.playground.count")
        scrollTo(counter, in: app)
        XCTAssertEqual(retry.elementType, .button)
        retry.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        XCTAssertTrue(playgroundAlert(app).exists)
    }

    @MainActor
    func testAlertDismissRemovesItAndShowAgainBringsItBack() {
        let app = launch(page: "alert")
        let show = element(app, "demo.alert.playground.show")
        scrollTo(show, in: app)
        let message = playgroundAlert(app)
        let dismiss = playgroundAlert(app).images["close"].firstMatch
        XCTAssertTrue(message.exists)
        XCTAssertFalse(show.isEnabled)
        XCTAssertTrue(dismiss.isHittable)
        dismiss.tap()
        XCTAssertTrue(message.waitForNonExistence(timeout: 5))
        XCTAssertFalse(dismiss.exists)
        XCTAssertTrue(show.isEnabled)
        show.tap()
        XCTAssertTrue(message.waitForExistence(timeout: 5))
        XCTAssertTrue(playgroundAlert(app).images["close"].firstMatch.exists)
        flip(app, "demo.alert.playground.dismissible")
        XCTAssertFalse(playgroundAlert(app).images["close"].firstMatch.exists)
    }

    @MainActor
    func testCenterDialogOpensAndScrimDismisses() {
        let app = launch(page: "dialog")
        let title = app.staticTexts["Delete this project?"].firstMatch
        element(app, "demo.dialog.open.center").tap()
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["Delete"].firstMatch.exists)
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.12)).tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
        XCTAssertEqual(element(app, "demo.dialog.result").label, "Result: Dismissed")
        element(app, "demo.dialog.open.center").tap()
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        app.buttons["Delete"].firstMatch.tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
        XCTAssertEqual(element(app, "demo.dialog.result").label, "Result: Delete")
    }

    @MainActor
    func testSheetOpensAndDismisses() {
        let app = launch(page: "dialog")
        let title = app.staticTexts["Delete this project?"].firstMatch
        element(app, "demo.dialog.open.sheet").tap()
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["Cancel"].firstMatch.isHittable)
        app.buttons["Cancel"].firstMatch.tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
        XCTAssertEqual(element(app, "demo.dialog.result").label, "Result: Cancel")
        element(app, "demo.dialog.open.sheet").tap()
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.12)).tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
        XCTAssertEqual(element(app, "demo.dialog.result").label, "Result: Dismissed")
    }

    @MainActor
    func testSecondDialogIsRefusedWhileOneIsOpen() {
        let app = launch(page: "dialog")
        let stackingTitle = app.staticTexts["One dialog at a time"].firstMatch
        let secondTitle = app.staticTexts["Delete this project?"].firstMatch
        let open = element(app, "demo.dialog.open.stacking")
        scrollTo(open, in: app)
        open.tap()
        XCTAssertTrue(stackingTitle.waitForExistence(timeout: 5))
        app.buttons["Open another dialog"].firstMatch.tap()
        XCTAssertFalse(secondTitle.waitForExistence(timeout: 2))
        XCTAssertTrue(stackingTitle.exists)
        app.buttons["Close"].firstMatch.tap()
        XCTAssertTrue(stackingTitle.waitForNonExistence(timeout: 5))
        XCTAssertEqual(element(app, "demo.dialog.refused").label, "Refused presentations: 1")
        XCTAssertEqual(element(app, "demo.dialog.result").label, "Result: Refused")
    }

    @MainActor
    func testDialogActionPresentsAFollowUpDialog() {
        let app = launch(page: "dialog")
        let title = app.staticTexts["Delete this project?"].firstMatch
        let followUp = app.staticTexts["Project deleted"].firstMatch
        let open = element(app, "demo.dialog.open.chain")
        scrollTo(open, in: app)
        open.tap()
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        app.buttons["Delete"].firstMatch.tap()
        XCTAssertTrue(followUp.waitForExistence(timeout: 5))
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
        XCTAssertEqual(element(app, "demo.dialog.result").label, "Result: Delete")
        app.buttons["Undo"].firstMatch.tap()
        XCTAssertTrue(followUp.waitForNonExistence(timeout: 5))
        XCTAssertEqual(element(app, "demo.dialog.result").label, "Result: Undo")
        open.tap()
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        app.buttons["Cancel"].firstMatch.tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
    }

    @MainActor
    func testQueuedToastsShowOneAtATimeInOrder() {
        let app = launch(page: "toast")
        let show = element(app, "demo.toast.queue.show")
        scrollTo(show, in: app)
        let baseline = iconCount(app, "check-circle")
        show.tap()
        XCTAssertTrue(waitForIconCount(app, "check-circle", baseline + 1, timeout: 5))
        XCTAssertTrue(waitForIconCount(app, "check-circle", baseline, timeout: 40))
    }

    @MainActor
    func testToastUndoActionFiresAndDismisses() {
        let app = launch(page: "toast")
        let show = element(app, "demo.toast.playground.show")
        let counter = element(app, "demo.toast.playground.count")
        scrollTo(counter, in: app)
        let baseline = iconCount(app, "info")
        show.tap()
        XCTAssertTrue(waitForIconCount(app, "info", baseline + 1, timeout: 5))
        let window = app.windows.firstMatch.frame
        app.windows.firstMatch.coordinate(withNormalizedOffset: CGVector(dx: 0.88, dy: toastIcon(app, "info").frame.midY / window.height)).tap()
        XCTAssertTrue(waitForIconCount(app, "info", baseline, timeout: 5))
        XCTAssertEqual(counter.label, "Tapped 1 times")
    }

    @MainActor
    func testToastAppearsAboveAnOpenSheetAndPassesTouchesThrough() {
        let app = launch(page: "toast")
        let open = element(app, "demo.toast.sheet.open")
        scrollTo(open, in: app)
        open.tap()
        let title = app.staticTexts["Toasts sit above sheets"].firstMatch
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        let baseline = iconCount(app, "check-circle")
        app.buttons["Copy link"].firstMatch.tap()
        XCTAssertTrue(waitForIconCount(app, "check-circle", baseline + 1, timeout: 5))
        XCTAssertTrue(toastIcon(app, "check-circle").isHittable)
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.12)).tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
    }

    @MainActor
    func testSpinnerPageShowsEverySizeAndTone() {
        let app = launch(page: "spinner")
        for size in ["sm", "md", "lg"] {
            XCTAssertTrue(element(app, "demo.spinner.size.\(size)").waitForExistence(timeout: 5))
        }
        for tone in ["primary", "secondary", "accent"] {
            XCTAssertTrue(element(app, "demo.spinner.tone.\(tone)").exists)
        }
        XCTAssertTrue(element(app, "demo.spinner.labelled").exists)
        XCTAssertEqual(element(app, "demo.spinner.in-context").label, "Loading messages")
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
    private func launch(page: String) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += ["-TSDemoPage", page, "-TSDemoTheme", "light"]
        app.launch()
        XCTAssertTrue(app.descendants(matching: .any)["demo.page.\(page)"].firstMatch.waitForExistence(timeout: 10))
        return app
    }

    @MainActor
    private func flip(_ app: XCUIApplication, _ identifier: String) {
        let toggle = app.switches[identifier].firstMatch
        scrollTo(toggle, in: app)
        toggle.coordinate(withNormalizedOffset: CGVector(dx: 0.95, dy: 0.5)).tap()
    }

    private func playgroundAlert(_ app: XCUIApplication) -> XCUIElement {
        app.descendants(matching: .any)["demo.alert.playground.alert"].firstMatch
    }

    private func element(_ app: XCUIApplication, _ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier].firstMatch
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
}
