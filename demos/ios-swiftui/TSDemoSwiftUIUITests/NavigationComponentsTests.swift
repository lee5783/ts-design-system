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
        let week = tab("Week", in: tabs)
        XCTAssertEqual(week.value as? String, "Tab, 2 of 4")
        XCTAssertFalse(week.isSelected)
        XCTAssertTrue(tab("Day", in: tabs).isSelected)

        week.tap()
        XCTAssertTrue(week.isSelected)
        XCTAssertFalse(tab("Day", in: tabs).isSelected)
        XCTAssertEqual(value.label, "Selected: Week")
        XCTAssertEqual(counter.label, "Tapped 1 times")
        week.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")

        let month = tab("Month", in: tabs)
        month.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0)).withOffset(CGVector(dx: 0, dy: -2)).tap()
        XCTAssertTrue(month.isSelected)
        XCTAssertEqual(counter.label, "Tapped 2 times")

        flip(app, "demo.tabs.playground.disabled")
        scrollTo(tabs, in: app)
        tab("Year", in: tabs).tap()
        XCTAssertTrue(month.isSelected)
        XCTAssertFalse(tab("Year", in: tabs).isSelected)
        XCTAssertEqual(counter.label, "Tapped 2 times")
    }

    @MainActor
    func testUnderlineTabsScrollWhenTheyOverflow() {
        let app = launch(page: "tabs")
        let tabs = app.descendants(matching: .any)["demo.tabs.overflow"].firstMatch
        scrollTo(tabs, in: app)
        let reports = tab("Reports", in: tabs)
        XCTAssertEqual(reports.value as? String, "Tab, 9 of 9")
        XCTAssertFalse(reports.isHittable)
        var swipes = 0
        while !reports.isHittable && swipes < 6 {
            tabs.swipeLeft()
            swipes += 1
        }
        XCTAssertTrue(reports.isHittable)
        reports.tap()
        XCTAssertTrue(reports.isSelected)
        XCTAssertEqual(app.staticTexts["demo.tabs.overflow.value"].firstMatch.label, "Selected: Reports")
    }

    @MainActor
    func testBottomNavSelectionBadgeAndDisabled() {
        let app = launch(page: "bottom-nav")
        let nav = app.descendants(matching: .any)["demo.bottom-nav.playground.nav"].firstMatch
        let value = app.staticTexts["demo.bottom-nav.playground.value"].firstMatch
        let counter = app.staticTexts["demo.bottom-nav.playground.changes"].firstMatch
        scrollTo(nav, in: app)
        let search = tab("Search", in: nav)
        XCTAssertEqual(search.value as? String, "Tab, 3 of 4")
        XCTAssertEqual(tab("Inbox", in: nav).value as? String, "Tab, 2 of 4, 3 new")
        XCTAssertTrue(tab("Home", in: nav).isSelected)

        search.tap()
        XCTAssertTrue(search.isSelected)
        XCTAssertFalse(tab("Home", in: nav).isSelected)
        XCTAssertEqual(value.label, "Selected: Search")
        XCTAssertEqual(counter.label, "Tapped 1 times")
        search.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")

        app.buttons["demo.bottom-nav.playground.add-hundred"].firstMatch.tap()
        XCTAssertEqual(tab("Inbox", in: nav).value as? String, "Tab, 2 of 4, 99+ new")
        app.buttons["demo.bottom-nav.playground.clear"].firstMatch.tap()
        XCTAssertEqual(tab("Inbox", in: nav).value as? String, "Tab, 2 of 4")

        flip(app, "demo.bottom-nav.playground.disabled")
        scrollTo(nav, in: app)
        tab("Alerts", in: nav).tap()
        XCTAssertFalse(tab("Alerts", in: nav).isSelected)
        XCTAssertTrue(search.isSelected)
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

        let inbox = shellTab("Inbox", in: app)
        XCTAssertEqual(inbox.value as? String, "Tab, 2 of 4, 3 new")
        XCTAssertTrue(shellTab("Home", in: app).isSelected)
        app.buttons["demo.bottom-nav.shell.add-one.demo.sample.home"].firstMatch.tap()
        XCTAssertEqual(inbox.value as? String, "Tab, 2 of 4, 4 new")

        inbox.tap()
        XCTAssertTrue(inbox.isSelected)
        let inboxTitle = app.staticTexts["demo.bottom-nav.shell.title.demo.sample.inbox"].firstMatch
        XCTAssertTrue(inboxTitle.waitForExistence(timeout: 2))
        XCTAssertTrue(inboxTitle.isHittable)
        XCTAssertFalse(homeTitle.exists && homeTitle.isHittable)
        app.buttons["demo.bottom-nav.shell.clear.demo.sample.inbox"].firstMatch.tap()
        XCTAssertEqual(inbox.value as? String, "Tab, 2 of 4")

        shellTab("Alerts", in: app).tap()
        XCTAssertTrue(app.staticTexts["demo.bottom-nav.shell.title.demo.sample.alerts"].firstMatch.waitForExistence(timeout: 2))
        XCTAssertEqual(app.tabBars.count, 0)
    }

    @MainActor
    private func shellTab(_ label: String, in app: XCUIApplication) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@ AND value BEGINSWITH %@", label, "Tab, "))
            .firstMatch
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
