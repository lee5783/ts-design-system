import XCTest

final class ContentComponentsTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testBadgeIsStaticTextLabelledByItsText() {
        let app = launch(page: "badge")
        let badge = element(app, "demo.badge.playground.badge")
        scrollTo(badge, in: app)
        XCTAssertEqual(badge.label, "Draft")
        XCTAssertEqual(badge.elementType, .staticText)
        XCTAssertFalse(app.buttons["demo.badge.playground.badge"].exists)
        flip(app, "demo.badge.playground.dot")
        flip(app, "demo.badge.playground.icon")
        XCTAssertEqual(badge.label, "Draft")
        XCTAssertEqual(badge.elementType, .staticText)
    }

    @MainActor
    func testAvatarIsAnImageLabelledByTheName() {
        let app = launch(page: "avatar")
        let avatar = element(app, "demo.avatar.playground.avatar")
        scrollTo(avatar, in: app)
        XCTAssertEqual(avatar.label, "Ada Lovelace")
        XCTAssertEqual(avatar.elementType, .image)
        flip(app, "demo.avatar.playground.photo")
        XCTAssertEqual(avatar.label, "Ada Lovelace")
        XCTAssertEqual(avatar.elementType, .image)
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
        XCTAssertEqual(defaultRow.label, "Default")
        XCTAssertEqual(defaultRow.elementType, .button)
        XCTAssertGreaterThanOrEqual(defaultRow.frame.height, 44)
        defaultRow.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        XCTAssertFalse(disabledRow.isEnabled)
        disabledRow.tap()
        XCTAssertEqual(staticRow.label, "No action, Favorites")
        XCTAssertNotEqual(staticRow.elementType, .button)
        staticRow.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        defaultRow.press(forDuration: 0.6)
        XCTAssertEqual(counter.label, "Tapped 2 times")
    }

    @MainActor
    func testListItemSwitchRowExposesSwitchSemantics() {
        let app = launch(page: "list-item")
        let row = element(app, "demo.list-item.wifi")
        let caption = element(app, "demo.list-item.wifi.value")
        scrollTo(row, in: app)
        XCTAssertEqual(row.label, "Wi-Fi")
        XCTAssertEqual(row.value as? String, "On")
        XCTAssertNotEqual(row.elementType, .staticText)
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.3, dy: 0.5)).tap()
        XCTAssertEqual(row.value as? String, "On")
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5)).tap()
        XCTAssertEqual(row.value as? String, "Off")
        XCTAssertEqual(caption.label, "Off")
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.2)).tap()
        XCTAssertEqual(row.value as? String, "On")
        XCTAssertEqual(caption.label, "On")
    }

    @MainActor
    func testListItemPlaygroundTapCountsAndSwitchTrailing() {
        let app = launch(page: "list-item")
        let row = element(app, "demo.list-item.playground.row")
        let counter = element(app, "demo.list-item.playground.count")
        let value = element(app, "demo.list-item.playground.value")
        scrollTo(counter, in: app)
        XCTAssertEqual(row.label, "Inbox")
        row.tap()
        XCTAssertEqual(counter.label, "Tapped 1 times")
        flip(app, "demo.list-item.playground.subtitle")
        scrollTo(row, in: app)
        XCTAssertEqual(row.label, "Inbox, 12 unread messages")
        let trailing = app.segmentedControls["demo.list-item.playground.trailing"].firstMatch
        scrollTo(trailing, in: app)
        trailing.buttons["Switch"].tap()
        scrollTo(counter, in: app)
        XCTAssertEqual(row.value as? String, "Off")
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5)).tap()
        XCTAssertEqual(row.value as? String, "On")
        XCTAssertEqual(value.label, "On")
        XCTAssertEqual(counter.label, "Tapped 1 times")
        flip(app, "demo.list-item.playground.disabled")
        scrollTo(row, in: app)
        XCTAssertFalse(row.isEnabled)
        row.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5)).tap()
        XCTAssertEqual(row.value as? String, "On")
    }

    @MainActor
    func testInteractiveCardIsOneElementAndPresses() {
        let app = launch(page: "card")
        let card = element(app, "demo.card.state.default")
        let disabled = element(app, "demo.card.state.disabled")
        let counter = element(app, "demo.card.state.count")
        scrollTo(counter, in: app)
        XCTAssertEqual(card.label, "Default, Interactive")
        XCTAssertEqual(card.elementType, .button)
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
