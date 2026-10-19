import XCTest

final class CatalogSmokeTests: XCTestCase {
    private static let foundationPages = ["colors", "typography", "spacing", "radius", "size", "opacity", "elevation", "icons"]
    private static let componentPages = [
        "button", "icon-button", "text-field", "checkbox", "switch", "tabs", "bottom-nav",
        "badge", "avatar", "list-item", "card", "alert", "dialog", "toast"
    ]

    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    private func launch() {
        app = XCUIApplication()
        app.launchArguments += ["-TSDemoTheme", "light"]
        app.launch()
    }

    @MainActor
    func testLaunchShowsCatalog() {
        launch()
        XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 10))
        XCTAssertTrue(element("demo.catalog.colors").exists)
        XCTAssertTrue(element("demo.settings").exists)
        XCTAssertTrue(element("demo.about").exists)
    }

    @MainActor
    func testCatalogListsEveryPage() {
        launch()
        XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 10))
        for page in Self.foundationPages + Self.componentPages {
            let row = element("demo.catalog.\(page)")
            scrollUntilHittable(row)
            XCTAssertTrue(row.isHittable, "Missing catalog row \(page)")
        }
    }

    @MainActor
    func testOpensEveryFoundationsPage() {
        launch()
        XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 10))
        for page in Self.foundationPages {
            let row = element("demo.catalog.\(page)")
            scrollUntilHittable(row)
            row.tap()
            XCTAssertTrue(element("demo.page.\(page)").waitForExistence(timeout: 5), "Page \(page) did not open")
            attachScreenshot(named: "foundation-\(page)")
            app.navigationBars.buttons.element(boundBy: 0).tap()
            XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 5))
        }
    }

    @MainActor
    func testThemeToggle() {
        launch()
        XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 10))
        attachScreenshot(named: "theme-light")
        element("demo.settings").tap()
        let dark = app.segmentedControls["demo.settings.theme"].buttons["Dark"]
        XCTAssertTrue(dark.waitForExistence(timeout: 5))
        XCTAssertTrue(app.segmentedControls["demo.settings.theme"].buttons["Light"].isSelected)
        dark.tap()
        XCTAssertTrue(dark.isSelected)
        attachScreenshot(named: "settings-dark")
        element("demo.settings.done").tap()
        XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 5))
        attachScreenshot(named: "theme-dark")
        element("demo.settings").tap()
        XCTAssertTrue(app.segmentedControls["demo.settings.theme"].buttons["Dark"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.segmentedControls["demo.settings.theme"].buttons["Dark"].isSelected)
        element("demo.settings.reset").tap()
        XCTAssertTrue(app.segmentedControls["demo.settings.theme"].buttons["System"].isSelected)
    }

    @MainActor
    func testAboutShowsVersions() {
        launch()
        XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 10))
        element("demo.about").tap()
        let library = element("demo.about.library-version")
        let designSystem = element("demo.about.design-system-version")
        XCTAssertTrue(library.waitForExistence(timeout: 5))
        XCTAssertFalse(library.label.contains("Unknown"), library.label)
        XCTAssertFalse(designSystem.label.contains("Unknown"), designSystem.label)
        attachScreenshot(named: "about")
    }

    @MainActor
    func testTextSizeMenuUpdatesSelection() {
        launch()
        XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 10))
        element("demo.settings").tap()
        let textSize = app.buttons["demo.settings.text-size"]
        XCTAssertTrue(textSize.waitForExistence(timeout: 5))
        textSize.tap()
        let largest = app.buttons["Accessibility 5"].firstMatch
        XCTAssertTrue(largest.waitForExistence(timeout: 5))
        largest.tap()
        XCTAssertEqual(textSize.value as? String, "Accessibility 5")
        XCTAssertTrue(element("demo.settings.text-size.preview").exists)
        attachScreenshot(named: "settings-accessibility5")
        element("demo.settings.reset").tap()
        XCTAssertEqual(textSize.value as? String, "System")
    }

    @MainActor
    func testRightToLeftToggle() {
        launch()
        XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 10))
        element("demo.settings").tap()
        let toggle = app.switches["demo.settings.right-to-left"]
        XCTAssertTrue(toggle.waitForExistence(timeout: 5))
        XCTAssertEqual(toggle.value as? String, "0")
        toggle.tap()
        XCTAssertEqual(toggle.value as? String, "1")
        element("demo.settings.done").tap()
        XCTAssertTrue(app.navigationBars["TS Design System"].waitForExistence(timeout: 5))
        attachScreenshot(named: "catalog-rtl")
    }

    @MainActor
    private func element(_ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier].firstMatch
    }

    @MainActor
    private func scrollUntilHittable(_ element: XCUIElement) {
        var attempts = 0
        while !(element.exists && element.isHittable) && attempts < 8 {
            app.swipeUp()
            attempts += 1
        }
    }

    @MainActor
    private func attachScreenshot(named name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
