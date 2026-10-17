import XCTest

final class ActionComponentsTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testButtonPlaygroundFiresAndLoadingBlocks() {
        let app = launch(page: "button")
        let button = element(app, "demo.button.playground.button")
        let count = element(app, "demo.button.playground.count")
        scrollTo(button, in: app)
        button.tap()
        XCTAssertEqual(count.label, "Tapped 1 times")
        flip(app, "demo.button.playground.loading")
        XCTAssertEqual(button.value as? String, "Loading")
        button.tap()
        XCTAssertEqual(count.label, "Tapped 1 times")
        flip(app, "demo.button.playground.loading")
        flip(app, "demo.button.playground.disabled")
        XCTAssertFalse(button.isEnabled)
        button.tap()
        XCTAssertEqual(count.label, "Tapped 1 times")
    }

    @MainActor
    func testLinkButtonHasAnExpandedHitArea() {
        let app = launch(page: "button")
        let button = element(app, "demo.button.playground.button")
        let count = element(app, "demo.button.playground.count")
        scrollTo(button, in: app)
        app.segmentedControls["demo.button.playground.variant"].buttons["link"].tap()
        button.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).withOffset(CGVector(dx: 0, dy: -19)).tap()
        XCTAssertEqual(count.label, "Tapped 1 times")
    }

    @MainActor
    func testIconButtonPlaygroundFiresAndDisabledBlocks() {
        let app = launch(page: "icon-button")
        let button = element(app, "demo.icon-button.playground.button")
        let count = element(app, "demo.icon-button.playground.count")
        scrollTo(button, in: app)
        XCTAssertEqual(button.label, "More options")
        button.tap()
        XCTAssertEqual(count.label, "Tapped 1 times")
        flip(app, "demo.icon-button.playground.disabled")
        button.tap()
        XCTAssertEqual(count.label, "Tapped 1 times")
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
