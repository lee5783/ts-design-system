import Foundation
import Testing
@testable import TSFoundation

@Suite("TSStrings")
struct TSStringsTests {
    @Test("Every library string resolves from the TSFoundation string catalog", arguments: TSStringKey.allCases)
    func resolves(key: TSStringKey) {
        let value = TSStrings.string(key)
        #expect(!value.isEmpty)
        #expect(value != key.rawValue)
    }

    @Test("English values")
    func englishValues() {
        #expect(TSStrings.loading == "Loading")
        #expect(TSStrings.close == "Close")
        #expect(TSStrings.dismiss == "Dismiss")
        #expect(TSStrings.position(2, of: 4, locale: Locale(identifier: "en_US")) == "2 of 4")
        #expect(TSStrings.tabPosition(2, of: 4, locale: Locale(identifier: "en_US")) == "Tab, 2 of 4")
        #expect(TSStrings.countOverflow(99, locale: Locale(identifier: "en_US")) == "99+")
        #expect(TSStrings.bottomNavBadge("3", locale: Locale(identifier: "en_US")) == "3 new")
        #expect(TSStrings.listItemNavigates == "Opens another screen")
        #expect(TSAlertTone.allCases.map(TSStrings.alertTone) == ["Information", "Success", "Warning", "Error"])
    }

    @Test("A bundle without the override table falls back to the library catalog")
    func overrideFallback() {
        #expect(TSStrings.string(.close, overrideBundle: TSFoundationResources.bundle) == "Close")
    }
}
