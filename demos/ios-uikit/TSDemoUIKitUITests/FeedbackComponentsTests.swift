import XCTest

final class FeedbackComponentsTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testAlertMessageIsOneElementWithSeparateAction() {
        let app = launch(page: "alert")
        let message = app.staticTexts["Information, New sign-in, We noticed a sign-in from a new device."].firstMatch
        XCTAssertTrue(message.waitForExistence(timeout: 5))
        flip(app, "demo.alert.playground.action")
        let retry = element(app, "demo.alert.playground.retry")
        let counter = element(app, "demo.alert.playground.count")
        scrollTo(counter, in: app)
        XCTAssertEqual(retry.elementType, .button)
        retry.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        XCTAssertTrue(app.staticTexts["Information, You are offline, Some features are unavailable until the connection is restored."].firstMatch.exists)
    }

    @MainActor
    func testAlertDismissRemovesItAndShowAgainBringsItBack() {
        let app = launch(page: "alert")
        let show = element(app, "demo.alert.playground.show")
        scrollTo(show, in: app)
        let message = app.staticTexts["Information, You are offline, Some features are unavailable until the connection is restored."].firstMatch
        let dismiss = app.buttons["Dismiss"].firstMatch
        XCTAssertTrue(message.exists)
        XCTAssertFalse(show.isEnabled)
        XCTAssertTrue(dismiss.isHittable)
        dismiss.tap()
        XCTAssertTrue(message.waitForNonExistence(timeout: 5))
        XCTAssertFalse(dismiss.exists)
        XCTAssertTrue(show.isEnabled)
        show.tap()
        XCTAssertTrue(message.waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["Dismiss"].firstMatch.exists)
        flip(app, "demo.alert.playground.dismissible")
        XCTAssertFalse(app.buttons["Dismiss"].firstMatch.exists)
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
        show.tap()
        let first = app.staticTexts["Success, Photo uploaded"].firstMatch
        let second = app.staticTexts["Success, Album shared"].firstMatch
        let third = app.staticTexts["Success, Backup complete"].firstMatch
        XCTAssertTrue(first.waitForExistence(timeout: 5))
        XCTAssertFalse(second.exists)
        XCTAssertTrue(second.waitForExistence(timeout: 8))
        XCTAssertFalse(first.exists)
        XCTAssertFalse(third.exists)
        XCTAssertTrue(third.waitForExistence(timeout: 8))
        XCTAssertFalse(second.exists)
        XCTAssertTrue(third.waitForNonExistence(timeout: 8))
    }

    @MainActor
    func testToastUndoActionFiresAndDismisses() {
        let app = launch(page: "toast")
        let show = element(app, "demo.toast.playground.show")
        let counter = element(app, "demo.toast.playground.count")
        scrollTo(counter, in: app)
        show.tap()
        let message = app.staticTexts["Message deleted"].firstMatch
        XCTAssertTrue(message.waitForExistence(timeout: 5))
        let undo = app.buttons.matching(NSPredicate(format: "label == 'Undo'")).allElementsBoundByIndex
            .first { abs($0.frame.midY - message.frame.midY) < 30 }
        XCTAssertNotNil(undo)
        XCTAssertTrue(undo?.isHittable == true)
        undo?.tap()
        XCTAssertTrue(message.waitForNonExistence(timeout: 5))
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
        app.buttons["Copy link"].firstMatch.tap()
        let toast = app.staticTexts["Success, Link copied"].firstMatch
        XCTAssertTrue(toast.waitForExistence(timeout: 5))
        XCTAssertTrue(toast.isHittable)
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.12)).tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
    }

    @MainActor
    func testSpinnerPageShowsSpinnersLabelledLoading() {
        let app = launch(page: "spinner")
        for size in ["sm", "md", "lg"] {
            let spinner = element(app, "demo.spinner.size.\(size)")
            XCTAssertTrue(spinner.waitForExistence(timeout: 5))
            XCTAssertEqual(spinner.label, "Loading")
        }
        for tone in ["primary", "secondary", "accent"] {
            XCTAssertEqual(element(app, "demo.spinner.tone.\(tone)").label, "Loading")
        }
        let labelled = element(app, "demo.spinner.labelled")
        scrollTo(labelled, in: app)
        XCTAssertEqual(labelled.label, "Loading messages")
        XCTAssertEqual(element(app, "demo.spinner.in-context").label, "Loading messages")
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
