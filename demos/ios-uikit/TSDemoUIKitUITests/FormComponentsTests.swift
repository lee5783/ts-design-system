import XCTest

final class FormComponentsTests: XCTestCase {
    private let fieldID = "demo.text-field.playground.field"

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testTextFieldTypingErrorAndPasswordToggle() {
        let app = launch(page: "text-field")
        let field = app.textFields[fieldID].firstMatch
        let container = app.descendants(matching: .any)[fieldID].firstMatch
        scrollTo(field, in: app)
        field.tap()
        field.typeText("ada@example.com\n")
        XCTAssertEqual(field.value as? String, "ada@example.com")

        flip(app, "demo.text-field.playground.error")
        scrollTo(field, in: app)
        XCTAssertEqual(field.value as? String, "ada@example.com")

        flip(app, "demo.text-field.playground.error")
        flip(app, "demo.text-field.playground.secure")
        let secure = app.secureTextFields[fieldID].firstMatch
        scrollTo(secure, in: app)
        XCTAssertFalse(app.textFields[fieldID].exists)
        passwordToggle(app, container: container, field: secure).tap()
        XCTAssertTrue(app.textFields[fieldID].firstMatch.waitForExistence(timeout: 5))
        XCTAssertEqual(app.textFields[fieldID].firstMatch.value as? String, "ada@example.com")
        passwordToggle(app, container: container, field: app.textFields[fieldID].firstMatch).tap()
        XCTAssertTrue(app.secureTextFields[fieldID].firstMatch.waitForExistence(timeout: 5))
    }

    @MainActor
    func testCodeFieldShowsTypedCodeUnmasked() {
        let app = launch(page: "text-field")
        let codeID = "demo.text-field.code.field"
        let field = app.textFields[codeID].firstMatch
        scrollTo(field, in: app)
        field.tap()
        field.typeText("AB12CD\n")
        XCTAssertEqual(field.value as? String, "AB12CD")
        XCTAssertFalse(app.secureTextFields[codeID].exists)
    }

    @MainActor
    func testDisabledAndReadOnlyBlockEditing() {
        let app = launch(page: "text-field")
        flip(app, "demo.text-field.playground.disabled")
        let field = app.textFields[fieldID].firstMatch
        scrollTo(field, in: app)
        XCTAssertFalse(field.isEnabled)
        flip(app, "demo.text-field.playground.disabled")
        flip(app, "demo.text-field.playground.read-only")
        let readOnly = app.textViews[fieldID].firstMatch
        scrollTo(readOnly, in: app)
        XCTAssertFalse(app.textFields[fieldID].exists)
    }

    @MainActor
    func testCheckboxTogglesFromTheLabelAndDisabledBlocks() {
        assertToggle(page: "checkbox", control: "demo.checkbox.playground.checkbox", off: "Unchecked", on: "Checked")
    }

    @MainActor
    func testSwitchTogglesFromTheLabelAndDisabledBlocks() {
        assertToggle(page: "switch", control: "demo.switch.playground.switch", off: "Off", on: "On")
    }

    @MainActor
    private func assertToggle(page: String, control identifier: String, off: String, on: String) {
        let app = launch(page: page)
        let control = app.descendants(matching: .any)[identifier].firstMatch
        let counter = app.staticTexts["demo.\(page).playground.count"].firstMatch
        let state = app.staticTexts["demo.\(page).playground.value"].firstMatch
        scrollTo(control, in: app)
        XCTAssertGreaterThanOrEqual(control.frame.height, 44)
        XCTAssertEqual(state.label, off)
        control.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5)).tap()
        XCTAssertEqual(state.label, on)
        control.tap()
        XCTAssertEqual(state.label, off)
        control.tap()
        XCTAssertEqual(state.label, on)
        XCTAssertEqual(counter.label, "Tapped 3 times")

        flip(app, "demo.\(page).playground.disabled")
        scrollTo(control, in: app)
        XCTAssertFalse(control.isEnabled)
        control.tap()
        XCTAssertEqual(state.label, on)
        XCTAssertEqual(counter.label, "Tapped 3 times")
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
    private func passwordToggle(_ app: XCUIApplication, container: XCUIElement, field: XCUIElement) -> XCUICoordinate {
        app.coordinate(withNormalizedOffset: .zero).withOffset(CGVector(dx: container.frame.maxX - 38, dy: field.frame.midY))
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
