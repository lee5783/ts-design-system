import Foundation
import Testing
@testable import TSFoundation

@Suite("TSBadgeCountFormatter")
struct TSBadgeCountFormatterTests {
    private let english = Locale(identifier: "en_US")

    @Test("Zero and negative counts hide the badge", arguments: [0, -1])
    func hidden(count: Int) {
        #expect(TSBadgeCountFormatter.string(for: count, locale: english) == nil)
    }

    @Test("Counts up to the maximum are shown as is", arguments: [(1, "1"), (9, "9"), (99, "99")])
    func shown(count: Int, expected: String) {
        #expect(TSBadgeCountFormatter.string(for: count, locale: english) == expected)
    }

    @Test("Counts above the maximum overflow to 99+", arguments: [100, 101, 5_000])
    func overflow(count: Int) {
        #expect(TSBadgeCountFormatter.string(for: count, locale: english) == "99+")
    }

    @Test("A custom maximum overflows at its own value")
    func customMaximum() {
        #expect(TSBadgeCountFormatter.string(for: 9, maximum: 9, locale: english) == "9")
        #expect(TSBadgeCountFormatter.string(for: 10, maximum: 9, locale: english) == "9+")
    }
}
