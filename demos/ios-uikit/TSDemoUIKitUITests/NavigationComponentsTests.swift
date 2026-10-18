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

        let week = tab("Week", in: tabs)
        week.tap()
        XCTAssertEqual(value.label, "Selected: Week")
        XCTAssertEqual(counter.label, "Tapped 1 times")
        week.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")

        tab("Month", in: tabs).tap()
        XCTAssertEqual(value.label, "Selected: Month")
        XCTAssertEqual(counter.label, "Tapped 2 times")

        flip(app, "demo.tabs.playground.disabled")
        scrollTo(tabs, in: app)
        tab("Year", in: tabs).tap()
        XCTAssertEqual(value.label, "Selected: Month")
        XCTAssertEqual(counter.label, "Tapped 2 times")
    }

    @MainActor
    func testUnderlineTabsScrollWhenTheyOverflow() {
        let app = launch(page: "tabs")
        let tabs = app.descendants(matching: .any)["demo.tabs.overflow"].firstMatch
        scrollTo(tabs, in: app)
        let reports = tab("Reports", in: tabs)
        XCTAssertFalse(reports.isHittable)
        var swipes = 0
        while !reports.isHittable && swipes < 6 {
            tabs.swipeLeft()
            swipes += 1
        }
        XCTAssertTrue(reports.isHittable)
        reports.tap()
        XCTAssertEqual(app.staticTexts["demo.tabs.overflow.value"].firstMatch.label, "Selected: Reports")
    }

    @MainActor
    func testBottomNavSelectionBadgeAndDisabled() {
        let app = launch(page: "bottom-nav")
        let nav = app.descendants(matching: .any)["demo.bottom-nav.playground.nav"].firstMatch
        let value = app.staticTexts["demo.bottom-nav.playground.value"].firstMatch
        let counter = app.staticTexts["demo.bottom-nav.playground.changes"].firstMatch
        scrollTo(nav, in: app)
        XCTAssertEqual(value.label, "Selected: Home")
        XCTAssertTrue(tab("3", in: nav).exists)

        let search = tab("Search", in: nav)
        search.tap()
        XCTAssertEqual(value.label, "Selected: Search")
        XCTAssertEqual(counter.label, "Tapped 1 times")
        search.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")

        app.buttons["demo.bottom-nav.playground.add-hundred"].firstMatch.tap()
        XCTAssertTrue(tab("99+", in: nav).exists)
        app.buttons["demo.bottom-nav.playground.clear"].firstMatch.tap()
        XCTAssertFalse(tab("99+", in: nav).exists)
        XCTAssertFalse(tab("3", in: nav).exists)

        flip(app, "demo.bottom-nav.playground.disabled")
        scrollTo(nav, in: app)
        tab("Alerts", in: nav).tap()
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

        XCTAssertTrue(shellTab("3", in: app).exists)
        app.buttons["demo.bottom-nav.shell.add-one.demo.sample.home"].firstMatch.tap()
        XCTAssertTrue(shellTab("4", in: app).exists)

        shellTab("Inbox", in: app).tap()
        let inboxTitle = app.staticTexts["demo.bottom-nav.shell.title.demo.sample.inbox"].firstMatch
        XCTAssertTrue(inboxTitle.waitForExistence(timeout: 2))
        XCTAssertTrue(inboxTitle.isHittable)
        XCTAssertFalse(homeTitle.exists && homeTitle.isHittable)

        shellTab("Alerts", in: app).tap()
        XCTAssertTrue(app.staticTexts["demo.bottom-nav.shell.title.demo.sample.alerts"].firstMatch.waitForExistence(timeout: 2))
        XCTAssertEqual(app.tabBars.count, 0)
    }

    @MainActor
    private func shellTab(_ label: String, in app: XCUIApplication) -> XCUIElement {
        let matches = app.staticTexts.matching(NSPredicate(format: "label == %@", label)).allElementsBoundByIndex
        return matches.max { $0.frame.midY < $1.frame.midY } ?? app.staticTexts[label].firstMatch
    }

    @MainActor
    private func tab(_ label: String, in tabs: XCUIElement) -> XCUIElement {
        tabs.descendants(matching: .any).matching(NSPredicate(format: "label == %@", label)).firstMatch
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
