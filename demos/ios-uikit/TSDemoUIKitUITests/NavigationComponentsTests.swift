import XCTest

final class NavigationComponentsTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testTabsSelectionUpdatesTheValueAndDisabledBlocks() {
        let app = launch(page: "tabs")
        let tabs = app.descendants(matching: .any)["demo.tabs.playground.tabs"].firstMatch
        let value = app.staticTexts["demo.tabs.playground.value"].firstMatch
        let counter = app.staticTexts["demo.tabs.playground.count"].firstMatch
        scrollTo(tabs, in: app)
        XCTAssertEqual(value.label, "Selected: Day")

        tap(tabs, item: 1, of: 4)
        XCTAssertEqual(value.label, "Selected: Week")
        XCTAssertEqual(counter.label, "Tapped 1 times")
        tap(tabs, item: 1, of: 4)
        XCTAssertEqual(counter.label, "Tapped 1 times")

        tap(tabs, item: 2, of: 4)
        XCTAssertEqual(value.label, "Selected: Month")
        XCTAssertEqual(counter.label, "Tapped 2 times")

        flip(app, "demo.tabs.playground.disabled")
        scrollTo(tabs, in: app)
        tap(tabs, item: 3, of: 4)
        XCTAssertEqual(value.label, "Selected: Month")
        XCTAssertEqual(counter.label, "Tapped 2 times")
    }

    @MainActor
    func testUnderlineTabsScrollWhenTheyOverflow() {
        let app = launch(page: "tabs")
        let tabs = app.descendants(matching: .any)["demo.tabs.overflow"].firstMatch
        scrollTo(tabs, in: app)
        repeat3Swipes(tabs)
        tabs.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5)).tap()
        XCTAssertEqual(app.staticTexts["demo.tabs.overflow.value"].firstMatch.label, "Selected: Reports")
    }

    @MainActor
    func testBottomNavSelectionAndDisabled() {
        let app = launch(page: "bottom-nav")
        let nav = app.descendants(matching: .any)["demo.bottom-nav.playground.nav"].firstMatch
        let value = app.staticTexts["demo.bottom-nav.playground.value"].firstMatch
        let counter = app.staticTexts["demo.bottom-nav.playground.changes"].firstMatch
        scrollTo(nav, in: app)
        XCTAssertEqual(value.label, "Selected: Home")

        tap(nav, item: 2, of: 4)
        XCTAssertEqual(value.label, "Selected: Search")
        XCTAssertEqual(counter.label, "Tapped 1 times")
        tap(nav, item: 2, of: 4)
        XCTAssertEqual(counter.label, "Tapped 1 times")

        flip(app, "demo.bottom-nav.playground.disabled")
        scrollTo(nav, in: app)
        tap(nav, item: 3, of: 4)
        XCTAssertEqual(value.label, "Selected: Search")
        XCTAssertEqual(counter.label, "Tapped 1 times")
    }

    @MainActor
    func testBottomNavShellSwitchesTabsWithoutTheSystemTabBar() {
        let app = launch(page: "bottom-nav")
        let open = app.buttons["demo.bottom-nav.open-shell"].firstMatch
        scrollTo(open, in: app)
        open.tap()
        let homeTitle = app.staticTexts["demo.bottom-nav.shell.title.demo.sample.home"].firstMatch
        XCTAssertTrue(homeTitle.waitForExistence(timeout: 5))
        XCTAssertEqual(app.tabBars.count, 0)

        tapShellTab(app, item: 1, of: 4)
        let inboxTitle = app.staticTexts["demo.bottom-nav.shell.title.demo.sample.inbox"].firstMatch
        XCTAssertTrue(inboxTitle.waitForExistence(timeout: 2))
        XCTAssertTrue(inboxTitle.isHittable)
        XCTAssertFalse(homeTitle.exists && homeTitle.isHittable)

        tapShellTab(app, item: 3, of: 4)
        XCTAssertTrue(app.staticTexts["demo.bottom-nav.shell.title.demo.sample.alerts"].firstMatch.waitForExistence(timeout: 2))
        XCTAssertEqual(app.tabBars.count, 0)
    }

    @MainActor
    private func tap(_ bar: XCUIElement, item: Int, of count: Int) {
        bar.coordinate(withNormalizedOffset: CGVector(dx: (Double(item) + 0.5) / Double(count), dy: 0.5)).tap()
    }

    @MainActor
    private func tapShellTab(_ app: XCUIApplication, item: Int, of count: Int) {
        let window = app.windows.firstMatch
        let x = (Double(item) + 0.5) / Double(count)
        window.coordinate(withNormalizedOffset: CGVector(dx: x, dy: (window.frame.height - 66) / window.frame.height)).tap()
    }

    @MainActor
    private func repeat3Swipes(_ tabs: XCUIElement) {
        for _ in 0..<3 {
            tabs.swipeLeft()
        }
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
