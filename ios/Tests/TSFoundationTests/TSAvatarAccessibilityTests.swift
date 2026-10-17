import Testing
@testable import TSFoundation

@Suite("TSAvatarAccessibility")
struct TSAvatarAccessibilityTests {
    @Test("Only a name with visible characters is exposed", arguments: [
        ("Ada Lovelace", true),
        ("李小龍", true),
        ("!", true),
        ("", false),
        ("   ", false),
        ("\n\t", false),
    ])
    func exposure(name: String, expected: Bool) {
        #expect(TSAvatarAccessibility.isExposed(name: name) == expected)
    }
}
