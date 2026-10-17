import Foundation
import Testing
@testable import TSFoundation

@Suite("TSInitials")
struct TSInitialsTests {
    private let english = Locale(identifier: "en_US")

    @Test(
        "First letters of the first and last words, up to 2, uppercased",
        arguments: [
            ("ada", "A"),
            ("Ada Lovelace", "AL"),
            ("ada king lovelace", "AL"),
            ("  Grace   Brewster  Hopper ", "GH"),
        ]
    )
    func latinNames(name: String, expected: String) {
        #expect(TSInitials.initials(for: name, locale: english) == expected)
    }

    @Test(
        "Emoji are kept as whole grapheme clusters",
        arguments: [
            ("😀 Smile", "😀S"),
            ("Anna 👩🏽‍💻", "A👩🏽‍💻"),
            ("🇻🇳", "🇻🇳"),
        ]
    )
    func emoji(name: String, expected: String) {
        #expect(TSInitials.initials(for: name, locale: english) == expected)
    }

    @Test("Empty and whitespace-only names produce no initials", arguments: ["", " ", "\n\t ", "( )"])
    func empty(name: String) {
        #expect(TSInitials.initials(for: name, locale: english).isEmpty)
    }

    @Test(
        "Non-Latin scripts",
        arguments: [
            ("иван петров", "ИП"),
            ("Νίκος Καζαντζάκης", "ΝΚ"),
            ("李 小龍", "李小"),
            ("山田太郎", "山"),
            ("محمد علي", "مع"),
            ("Đặng Thái Sơn", "ĐS"),
        ]
    )
    func nonLatin(name: String, expected: String) {
        #expect(TSInitials.initials(for: name, locale: english) == expected)
    }

    @Test("Leading punctuation is skipped")
    func leadingPunctuation() {
        #expect(TSInitials.initials(for: "(Bob) \"Smith\"", locale: english) == "BS")
    }
}
