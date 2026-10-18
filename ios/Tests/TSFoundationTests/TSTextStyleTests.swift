import Testing
import TSFoundation
import TSTokens

@Suite("TSTypeRole and TSTextColor")
struct TSTextStyleTests {
    @Test("Every type role is its TSFont", arguments: zip(TSTypeRole.allCases, TSFont.allFonts))
    func roles(role: TSTypeRole, expected: TSFont) {
        #expect(role.font == expected)
    }

    @Test(
        "Every text colour is a text-* role",
        arguments: zip(TSTextColor.allCases, [TSColor.textPrimary, .textSecondary, .textMuted, .textDisabled, .textInverse, .textOnAccent])
    )
    func colors(color: TSTextColor, expected: TSColor) {
        #expect(color.color == expected)
    }

    @Test("Text colours cover every text-* role of the palette")
    func coversPalette() {
        let textRoles = Set(TSColor.allTokens.filter { $0.name.hasPrefix("text-") }.map(\.name))
        #expect(Set(TSTextColor.allCases.map(\.color.name)) == textRoles)
    }
}
