import XCTest

final class PageWalkTests: XCTestCase {
    private static let foundationPages = ["colors", "typography", "spacing", "radius", "size", "opacity", "elevation", "icons"]
    private static let componentPages = [
        "button", "icon-button", "badge", "avatar", "text-field", "checkbox", "switch",
        "tabs", "list-item", "card", "alert", "dialog", "toast", "bottom-nav"
    ]

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testWalksEveryPageFromTheCatalogToItsEnd() {
        let app = XCUIApplication()
        app.launchArguments += ["-TSDemoTheme", "light"]
        app.launch()
        let catalog = app.navigationBars["TS Design System"]
        XCTAssertTrue(catalog.waitForExistence(timeout: 10))
        for page in Self.foundationPages + Self.componentPages {
            let row = app.descendants(matching: .any)["demo.catalog.\(page)"].firstMatch
            scrollUntilHittable(row, in: app, swipe: { app.swipeUp() })
            row.tap()
            let scroll = app.descendants(matching: .any)["demo.page.\(page)"].firstMatch
            XCTAssertTrue(scroll.waitForExistence(timeout: 5), "Page \(page) did not open")
            XCTAssertFalse(catalog.exists, "Page \(page) kept the catalog title")
            if Self.componentPages.contains(page) {
                let playground = playgroundHeader(in: app)
                scrollUntilHittable(playground, in: app, swipe: { scroll.swipeUp() })
                XCTAssertTrue(playground.isHittable, "Page \(page) has no playground")
            }
            scrollToEnd(scroll)
            app.navigationBars.buttons.element(boundBy: 0).tap()
            XCTAssertTrue(catalog.waitForExistence(timeout: 5), "Back from \(page) did not return to the catalog")
        }
    }

    @MainActor
    func testEveryComponentPlaygroundIsReachableAtAccessibilityFiveInRightToLeft() {
        for page in Self.componentPages {
            let app = XCUIApplication()
            app.launchArguments += ["-TSDemoPage", page, "-TSDemoTheme", "dark", "-TSDemoTextSize", "accessibility5", "-TSDemoRightToLeft", "YES"]
            app.launch()
            let scroll = app.descendants(matching: .any)["demo.page.\(page)"].firstMatch
            XCTAssertTrue(scroll.waitForExistence(timeout: 10), "Page \(page) did not open")
            let playground = playgroundHeader(in: app)
            scrollUntilHittable(playground, in: app, swipe: { scroll.swipeUp() }, limit: 40)
            XCTAssertTrue(playground.isHittable, "Page \(page) playground is not reachable at AX5 in RTL")
            revealFully(playground, in: app)
            let edge = playground.frame.width / 10
            let trailingEdge = CGRect(x: playground.frame.maxX - edge, y: playground.frame.minY, width: edge, height: playground.frame.height)
            let leadingEdge = CGRect(x: playground.frame.minX, y: playground.frame.minY, width: edge, height: playground.frame.height)
            XCTAssertGreaterThan(app.inkContrast(in: trailingEdge), app.inkContrast(in: leadingEdge) + 0.2, "Page \(page) headers should be right-aligned in RTL")
            app.terminate()
        }
    }

    @MainActor
    private func playgroundHeader(in app: XCUIApplication) -> XCUIElement {
        app.staticTexts.matching(NSPredicate(format: "label ==[c] %@", "Playground")).firstMatch
    }

    @MainActor
    private func scrollUntilHittable(_ element: XCUIElement, in app: XCUIApplication, swipe: () -> Void, limit: Int = 20) {
        var attempts = 0
        while !(element.exists && element.isHittable) && attempts < limit {
            swipe()
            attempts += 1
        }
    }

    @MainActor
    private func revealFully(_ element: XCUIElement, in app: XCUIApplication) {
        element.waitUntilSettled()
        let top = app.navigationBars.firstMatch.frame.maxY
        let bottom = app.windows.firstMatch.frame.maxY
        var attempts = 0
        while (element.frame.minY < top || element.frame.maxY > bottom) && attempts < 5 {
            let isBelow = element.frame.maxY > bottom
            let start = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: isBelow ? 0.6 : 0.4))
            let end = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: isBelow ? 0.4 : 0.6))
            start.press(forDuration: 0.05, thenDragTo: end, withVelocity: .slow, thenHoldForDuration: 0.3)
            element.waitUntilSettled()
            attempts += 1
        }
    }

    @MainActor
    private func scrollToEnd(_ scroll: XCUIElement) {
        var previous = CGFloat.nan
        var attempts = 0
        while attempts < 30 {
            let anchor = scroll.descendants(matching: .any).element(boundBy: 0).frame.minY
            if anchor == previous {
                return
            }
            previous = anchor
            scroll.swipeUp()
            attempts += 1
        }
        XCTFail("Page did not reach its end")
    }
}
