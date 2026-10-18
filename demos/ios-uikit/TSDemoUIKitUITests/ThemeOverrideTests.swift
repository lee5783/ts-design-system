import XCTest

final class ThemeOverrideTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    override func tearDownWithError() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-demoTheme", "system"]
        app.launch()
        app.terminate()
    }

    @MainActor
    func testAppLaunchedDarkStaysDarkOnALightDevice() throws {
        try requireDevice(dark: false)
        let catalog = launch(["-demoTheme", "dark"])
        XCTAssertEqual(themeToggle(catalog).label, "Theme: Dark")
        assertDark(catalog, "catalog")
        attachScreenshot(catalog, named: "device-light-app-dark-catalog")
        catalog.terminate()

        let page = launch(["-demoTheme", "dark", "-TSDemoPage", "button"])
        XCTAssertEqual(themeToggle(page).label, "Theme: Dark")
        assertDark(page, "button page")
    }

    @MainActor
    func testAppLaunchedLightStaysLightOnADarkDevice() throws {
        try requireDevice(dark: true)
        let catalog = launch(["-demoTheme", "light"])
        XCTAssertEqual(themeToggle(catalog).label, "Theme: Light")
        assertLight(catalog, "catalog")
        attachScreenshot(catalog, named: "device-dark-app-light-catalog")
        catalog.terminate()

        let page = launch(["-demoTheme", "light", "-TSDemoPage", "button"])
        XCTAssertEqual(themeToggle(page).label, "Theme: Light")
        assertLight(page, "button page")
    }

    @MainActor
    func testUnknownValuesFallBackToSystemAndSystemFollowsTheDevice() throws {
        let app = launch(["-demoTheme", "sepia"])
        XCTAssertEqual(themeToggle(app).label, "Theme: System")
        let systemLuminance = luminance(app, in: backgroundSample(app))
        try openSettingsLink(app, theme: "dark")
        XCTAssertEqual(themeToggle(app).label, "Theme: Dark")
        try openSettingsLink(app, theme: "sepia")
        XCTAssertEqual(themeToggle(app).label, "Theme: System")
        XCTAssertTrue(waitFor { abs(self.luminance(app, in: self.backgroundSample(app)) - systemLuminance) < 0.05 }, "System should render like the device again")
    }

    @MainActor
    func testToggleCyclesAndPersistsAcrossRelaunch() {
        let app = launch(["-demoTheme", "system", "-TSDemoPage", "badge"])
        let toggle = themeToggle(app)
        XCTAssertEqual(toggle.label, "Theme: System")
        toggle.tap()
        XCTAssertEqual(toggle.label, "Theme: Light")
        toggle.tap()
        XCTAssertEqual(toggle.label, "Theme: Dark")
        assertDark(app, "badge page after the toggle")
        app.terminate()

        let relaunched = XCUIApplication()
        relaunched.launch()
        let catalogToggle = themeToggle(relaunched)
        XCTAssertEqual(catalogToggle.label, "Theme: Dark")
        assertDark(relaunched, "catalog after relaunch")
        relaunched.descendants(matching: .any)["demo.settings"].firstMatch.tap()
        let picker = relaunched.segmentedControls["demo.settings.theme"]
        XCTAssertTrue(picker.buttons["Dark"].waitForExistence(timeout: 5))
        XCTAssertTrue(picker.buttons["Dark"].isSelected)
        relaunched.descendants(matching: .any)["demo.settings.reset"].firstMatch.tap()
        XCTAssertTrue(picker.buttons["System"].isSelected)
        relaunched.descendants(matching: .any)["demo.settings.done"].firstMatch.tap()
        XCTAssertEqual(themeToggle(relaunched).label, "Theme: System")
        catalogToggle.tap()
        XCTAssertEqual(catalogToggle.label, "Theme: Light")
    }

    @MainActor
    func testOpenDialogFollowsALiveFlipWithoutClosing() throws {
        let app = launch(["-demoTheme", "light", "-TSDemoPage", "dialog"])
        app.descendants(matching: .any)["demo.dialog.open.sheet"].firstMatch.tap()
        let title = app.staticTexts["Delete this project?"].firstMatch
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        let panel = panelSample(below: title)
        XCTAssertGreaterThan(luminance(app, in: panel), 0.6)
        try openSettingsLink(app, theme: "dark")
        XCTAssertTrue(title.exists, "The sheet should stay open through the flip")
        XCTAssertTrue(waitForLuminance(app, below: 0.3, in: panel), "The open sheet should re-render dark")
        attachScreenshot(app, named: "theme-flip-sheet-dark")
        app.buttons["Cancel"].firstMatch.tap()
        XCTAssertTrue(title.waitForNonExistence(timeout: 5))
    }

    @MainActor
    func testShownToastFollowsALiveFlipAndKeepsItsPlace() throws {
        let app = launch(["-demoTheme", "light", "-TSDemoPage", "toast"])
        let show = app.descendants(matching: .any)["demo.toast.playground.show"].firstMatch
        var attempts = 0
        while !show.isHittable && attempts < 20 {
            app.swipeUp()
            attempts += 1
        }
        show.tap()
        let message = app.images["info"].firstMatch
        XCTAssertTrue(message.waitForExistence(timeout: 5))
        let strip = CGRect(x: message.frame.minX - 8, y: message.frame.minY - 6, width: 6, height: 4)
        let before = luminance(app, in: strip)
        try openSettingsLink(app, theme: "dark")
        XCTAssertTrue(message.exists, "The toast should survive the flip")
        XCTAssertTrue(waitForLuminance(app, above: before + 0.3, in: strip), "The toast surface should invert with the theme")
        attachScreenshot(app, named: "theme-flip-toast-dark")
    }

    @MainActor
    private func launch(_ arguments: [String]) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += arguments
        app.launch()
        XCTAssertTrue(themeToggle(app).waitForExistence(timeout: 10))
        return app
    }

    @MainActor
    private func requireDevice(dark: Bool) throws {
        let probe = XCUIApplication()
        probe.launchArguments += ["-demoTheme", "system"]
        probe.launch()
        XCTAssertTrue(themeToggle(probe).waitForExistence(timeout: 10))
        let isDark = luminance(probe, in: backgroundSample(probe)) < 0.3
        probe.terminate()
        guard isDark == dark else {
            throw XCTSkip("The simulator is in \(isDark ? "dark" : "light") appearance. Run `xcrun simctl ui <udid> appearance \(dark ? "dark" : "light")` first.")
        }
    }

    @MainActor
    private func themeToggle(_ app: XCUIApplication) -> XCUIElement {
        app.buttons["demo.action.theme"].firstMatch
    }

    @MainActor
    private func openSettingsLink(_ app: XCUIApplication, theme: String) throws {
        guard #available(iOS 16.4, *) else {
            throw XCTSkip("XCUISystem.open(_:) needs iOS 16.4")
        }
        let url = try XCTUnwrap(URL(string: "tsds-uikit://settings?theme=\(theme)"))
        XCUIDevice.shared.system.open(url)
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 5))
    }

    private func panelSample(below title: XCUIElement) -> CGRect {
        CGRect(x: title.frame.maxX + 12, y: title.frame.midY - 3, width: 8, height: 6)
    }

    @MainActor
    private func assertDark(_ app: XCUIApplication, _ screen: String) {
        XCTAssertTrue(waitForLuminance(app, below: 0.3), "\(screen) should render dark")
    }

    @MainActor
    private func assertLight(_ app: XCUIApplication, _ screen: String) {
        XCTAssertTrue(waitForLuminance(app, above: 0.6), "\(screen) should render light")
    }

    @MainActor
    private func backgroundSample(_ app: XCUIApplication) -> CGRect {
        let window = app.windows.firstMatch.frame
        return CGRect(x: window.minX + 2, y: window.midY, width: 6, height: 6)
    }

    @MainActor
    private func waitForLuminance(_ app: XCUIApplication, below limit: Double, in rect: CGRect? = nil) -> Bool {
        waitFor { self.luminance(app, in: rect ?? self.backgroundSample(app)) < limit }
    }

    @MainActor
    private func waitForLuminance(_ app: XCUIApplication, above limit: Double, in rect: CGRect? = nil) -> Bool {
        waitFor { self.luminance(app, in: rect ?? self.backgroundSample(app)) > limit }
    }

    @MainActor
    private func waitFor(_ condition: () -> Bool) -> Bool {
        for _ in 0..<20 {
            if condition() {
                return true
            }
            RunLoop.current.run(until: Date().addingTimeInterval(0.2))
        }
        return condition()
    }

    @MainActor
    private func luminance(_ app: XCUIApplication, in rect: CGRect) -> Double {
        let image = app.screenshot().image
        guard let cgImage = image.cgImage else { return .nan }
        let scale = CGFloat(cgImage.width) / image.size.width
        let pixels = CGRect(x: rect.minX * scale, y: rect.minY * scale, width: rect.width * scale, height: rect.height * scale).integral
        guard let crop = cgImage.cropping(to: pixels) else { return .nan }
        var rgba = [UInt8](repeating: 0, count: 4)
        let space = CGColorSpaceCreateDeviceRGB()
        let drawn: Bool = rgba.withUnsafeMutableBytes { buffer in
            guard let context = CGContext(
                data: buffer.baseAddress, width: 1, height: 1, bitsPerComponent: 8, bytesPerRow: 4,
                space: space, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
            ) else { return false }
            context.interpolationQuality = .medium
            context.draw(crop, in: CGRect(x: 0, y: 0, width: 1, height: 1))
            return true
        }
        guard drawn else { return .nan }
        return (0.2126 * Double(rgba[0]) + 0.7152 * Double(rgba[1]) + 0.0722 * Double(rgba[2])) / 255
    }

    @MainActor
    private func attachScreenshot(_ app: XCUIApplication, named name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
