import Testing
import TSDemoSupport

@Suite("DemoStrings")
struct DemoStringsTests {
    @Test("Every fixed key resolves to English text", arguments: DemoStringKey.allCases)
    func fixedKey(key: DemoStringKey) {
        let value = DemoStrings.string(key)
        #expect(!value.isEmpty)
        #expect(value != key.rawValue)
    }

    @Test("Every page has a title", arguments: DemoPageID.allCases)
    func pageTitle(page: DemoPageID) {
        #expect(!page.title.hasPrefix("demo."))
    }

    @Test("Every section, theme and text size has a title")
    func otherTitles() {
        let titles = DemoSection.ID.allCases.map(DemoStrings.sectionTitle)
            + DemoTheme.allCases.map(\.title)
            + DemoTextSize.allCases.map(\.title)
        #expect(titles.allSatisfy { !$0.hasPrefix("demo.") })
    }

    @Test("Sample titles read as expected")
    func samples() {
        #expect(DemoPageID.bottomNav.title == "Bottom navigation")
        #expect(DemoStrings.sectionTitle(.foundations) == "Foundations")
        #expect(DemoTextSize.accessibility5.title == "Accessibility 5")
    }

    @Test("The tap counter formats its count")
    func tapCount() {
        #expect(DemoStrings.tapCount(0) == "Tapped 0 times")
        #expect(DemoStrings.tapCount(3) == "Tapped 3 times")
    }

    @Test("The dialog captions format their result, refusal and queue counts")
    func dialogCaptions() {
        #expect(DemoStrings.dialogResult("Delete") == "Result: Delete")
        #expect(DemoStrings.refusedCount(1) == "Refused presentations: 1")
        #expect(DemoStrings.queuedCount(1) == "Queued presentations: 1")
    }
}
