import XCTest

final class LaunchOptionsTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testLaunchesDirectlyIntoAPage() {
        let app = XCUIApplication()
        app.launchArguments += ["-TSDemoPage", "typography", "-TSDemoTheme", "dark", "-TSDemoTextSize", "accessibility5", "-TSDemoRightToLeft", "YES"]
        app.launch()
        XCTAssertTrue(app.descendants(matching: .any)["demo.page.typography"].firstMatch.waitForExistence(timeout: 10))
        XCTAssertTrue(app.descendants(matching: .any)["demo.token.display"].firstMatch.exists)
    }
}
