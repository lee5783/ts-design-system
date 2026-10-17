import Testing
import TSFoundation
import TSTokens

@Suite("TSBadgeStyleSpec")
struct TSBadgeStyleSpecTests {
    static let expectedColors: [TSBadgeTone: (fill: TSColor, foreground: TSColor)] = [
        .neutral: (.backgroundSecondary, .textSecondary),
        .accent: (.accentSubtle, .accentText),
        .success: (.successSubtle, .success),
        .warning: (.warningSubtle, .warning),
        .danger: (.dangerSubtle, .danger),
        .info: (.infoSubtle, .info),
    ]

    @Test("The matrix covers 6 tones × dot × icon once each")
    func matrixIsComplete() {
        let configurations = TSBadgeStyleSpec.allConfigurations
        #expect(configurations.count == 24)
        #expect(Set(configurations).count == 24)
    }

    @Test("Every configuration maps its tone to fill, foreground and border tokens", arguments: TSBadgeStyleSpec.allConfigurations)
    func colors(configuration: TSBadgeStyleSpec.Configuration) throws {
        let resolved = TSBadgeStyleSpec.resolve(configuration)
        let expected = try #require(Self.expectedColors[configuration.tone])
        #expect(resolved.fill == expected.fill)
        #expect(resolved.foreground == expected.foreground)
        if configuration.tone == .neutral {
            #expect(resolved.border == .borderSubtle)
            #expect(resolved.borderWidth == 1)
        } else {
            #expect(resolved.border == nil)
            #expect(resolved.borderWidth == 0)
        }
        #expect(resolved.showsDot == configuration.hasDot)
        #expect(resolved.showsIcon == configuration.hasIcon)
    }

    @Test("Anatomy: min height 22, padding 8, gap 4, radius-sm, 6 pt dot, 16 pt icon, 12/16 at weight 500")
    func anatomy() {
        let resolved = TSBadgeStyleSpec.resolve(.init())
        #expect(resolved.minHeight == 22)
        #expect(resolved.horizontalPadding == 8)
        #expect(resolved.spacing == 4)
        #expect(resolved.cornerRadius == TSRadius.radiusSm)
        #expect(resolved.dotSize == 6)
        #expect(resolved.iconSize.pointSize == 16)
        #expect(resolved.font.token == TSTypographyTokens.caption)
        #expect(resolved.font.fontWeight == 500)
        #expect(resolved.font.token.lineHeight + 2 * resolved.verticalPadding <= resolved.minHeight)
    }
}
