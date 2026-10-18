import XCTest

final class ContentComponentsTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testBadgeIsStaticText() {
        let app = launch(page: "badge")
        let badge = element(app, "demo.badge.playground.badge")
        scrollTo(badge, in: app)
        let plainWidth = badge.frame.width
        XCTAssertFalse(app.buttons["demo.badge.playground.badge"].exists)
        flip(app, "demo.badge.playground.dot")
        flip(app, "demo.badge.playground.icon")
        scrollTo(badge, in: app)
        XCTAssertGreaterThan(badge.frame.width, plainWidth)
        XCTAssertFalse(app.buttons["demo.badge.playground.badge"].exists)
    }

    @MainActor
    func testAvatarIsStaticAndSurvivesAPhoto() {
        let app = launch(page: "avatar")
        let avatar = element(app, "demo.avatar.playground.avatar")
        scrollTo(avatar, in: app)
        XCTAssertFalse(app.buttons["demo.avatar.playground.avatar"].exists)
        flip(app, "demo.avatar.playground.photo")
        XCTAssertTrue(avatar.exists)
        XCTAssertFalse(app.buttons["demo.avatar.playground.avatar"].exists)
    }

    @MainActor
    func testListItemRowsPressOnlyWithAnAction() {
        let app = launch(page: "list-item")
        let defaultRow = element(app, "demo.list-item.state.default")
        let disabledRow = element(app, "demo.list-item.state.disabled")
        let staticRow = element(app, "demo.list-item.state.static")
        let counter = element(app, "demo.list-item.state.count")
        scrollTo(staticRow, in: app)
        XCTAssertGreaterThanOrEqual(defaultRow.frame.height, 44)
        defaultRow.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        disabledRow.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        XCTAssertNotEqual(staticRow.elementType, .button)
        staticRow.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        defaultRow.press(forDuration: 0.6)
        XCTAssertEqual(counter.label, "Tapped 2 times")
    }

    @MainActor
    func testListItemSwitchRowTogglesFromTitleAndSwitch() {
        let app = launch(page: "list-item")
        let row = element(app, "demo.list-item.wifi")
        let caption = element(app, "demo.list-item.wifi.value")
        scrollTo(row, in: app)
        XCTAssertEqual(caption.label, "On")
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.3, dy: 0.5)).tap()
        XCTAssertEqual(caption.label, "On")
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5)).tap()
        XCTAssertEqual(caption.label, "Off")
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.2)).tap()
        XCTAssertEqual(caption.label, "On")
    }

    @MainActor
    func testListItemPlaygroundTapCountsAndSwitchTrailing() {
        let app = launch(page: "list-item")
        let row = element(app, "demo.list-item.playground.row")
        let counter = element(app, "demo.list-item.playground.count")
        let value = element(app, "demo.list-item.playground.value")
        scrollTo(counter, in: app)
        row.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        flip(app, "demo.list-item.playground.subtitle")
        scrollTo(row, in: app)
        let trailing = app.segmentedControls["demo.list-item.playground.trailing"].firstMatch
        scrollTo(trailing, in: app)
        trailing.buttons["Switch"].tap()
        scrollTo(counter, in: app)
        XCTAssertEqual(value.label, "Off")
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5)).tap()
        XCTAssertEqual(value.label, "On")
        XCTAssertEqual(counter.label, "Tapped 1 times")
        flip(app, "demo.list-item.playground.disabled")
        scrollTo(row, in: app)
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5)).tap()
        XCTAssertEqual(value.label, "On")
    }

    @MainActor
    func testListItemDisclosureRowExpandsItsBody() {
        let app = launch(page: "list-item")
        let kotlin = element(app, "demo.list-item.disclosure.kotlin")
        let snapshot = element(app, "demo.list-item.disclosure.snapshot")
        scrollTo(snapshot, in: app)
        let kotlinCollapsed = kotlin.frame.height
        let snapshotCollapsed = snapshot.frame.height
        tapHeader(kotlin)
        XCTAssertTrue(waitForHeight(kotlin, above: kotlinCollapsed))
        scrollTo(snapshot, in: app)
        tapHeader(snapshot)
        XCTAssertTrue(waitForHeight(snapshot, above: snapshotCollapsed))
        XCTAssertTrue(waitForHeight(kotlin, atMost: kotlinCollapsed))
        tapHeader(snapshot)
        XCTAssertTrue(waitForHeight(snapshot, atMost: snapshotCollapsed))
    }

    @MainActor
    private func tapHeader(_ row: XCUIElement) {
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.3, dy: 0)).withOffset(CGVector(dx: 0, dy: 24)).tap()
    }

    @MainActor
    private func waitForHeight(_ row: XCUIElement, above height: CGFloat) -> Bool {
        let deadline = Date().addingTimeInterval(3)
        while Date() < deadline {
            if row.frame.height > height + 1 { return true }
            usleep(100_000)
        }
        return false
    }

    @MainActor
    private func waitForHeight(_ row: XCUIElement, atMost height: CGFloat) -> Bool {
        let deadline = Date().addingTimeInterval(3)
        while Date() < deadline {
            if row.frame.height <= height + 1 { return true }
            usleep(100_000)
        }
        return false
    }

    @MainActor
    func testTextPageShowsEveryTypeRoleAndTextColor() {
        let app = launch(page: "text")
        let roles = ["display", "title1", "title2", "title3", "bodyLg", "body", "bodySm", "label", "caption", "overline", "code"]
        for role in roles {
            let text = element(app, "demo.text.role.\(role)")
            scrollTo(text, in: app)
            XCTAssertEqual(text.label.lowercased(), "sphinx of black quartz, judge my vow")
        }
        for color in ["primary", "secondary", "muted", "disabled", "inverse", "onAccent"] {
            let text = element(app, "demo.text.color.\(color)")
            scrollTo(text, in: app)
            XCTAssertEqual(text.label, color)
        }
        let truncated = element(app, "demo.text.truncated")
        scrollTo(truncated, in: app)
        XCTAssertLessThan(truncated.frame.height, 40)
    }

    @MainActor
    func testInteractiveCardPresses() {
        let app = launch(page: "card")
        let card = element(app, "demo.card.state.default")
        let disabled = element(app, "demo.card.state.disabled")
        let counter = element(app, "demo.card.state.count")
        scrollTo(counter, in: app)
        XCTAssertFalse(app.buttons["Weekly report"].exists)
        card.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        card.press(forDuration: 0.6)
        XCTAssertEqual(counter.label, "Tapped 2 times")
        XCTAssertFalse(disabled.isEnabled)
        disabled.tap()
        XCTAssertEqual(counter.label, "Tapped 2 times")
    }

    @MainActor
    func testCardFooterButtonsStayTappable() {
        let app = launch(page: "card")
        let card = element(app, "demo.card.state.default")
        let counter = element(app, "demo.card.state.count")
        let lastAction = element(app, "demo.card.state.last-action")
        scrollTo(lastAction, in: app)
        XCTAssertEqual(lastAction.label, "None")
        let footerY = 1 - 34 / card.frame.height
        card.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: footerY)).tap()
        XCTAssertEqual(lastAction.label, "Open")
        XCTAssertEqual(counter.label, "Tapped 0 times")
        card.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        XCTAssertEqual(lastAction.label, "Open")
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
