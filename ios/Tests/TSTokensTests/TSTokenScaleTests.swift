import SwiftUI
import Testing
import TSTokens
import UIKit

@Suite("TSTokens scales")
struct TSTokenScaleTests {
    @Test("radius-full is flagged as a capsule and clamps to half the height")
    func radiusFullIsCapsule() throws {
        let full = try #require(TSRadius.allTokens.first { $0.name == "radius-full" })
        #expect(full.isCapsule)
        #expect(full.cornerRadius(forHeight: 44) == 22)
        #expect(TSRadius.allTokens.filter(\.isCapsule).map(\.name) == ["radius-full"])
        #expect(TSRadius.cornerRadius(TSRadius.radiusFull, forHeight: 32) == 16)
        #expect(TSRadius.cornerRadius(TSRadius.radiusMd, forHeight: 44) == 8)
    }

    @Test("Spacing, radius, size and opacity scales match the DS")
    func scales() {
        #expect(TSSpacing.allTokens.map(\.value) == [2, 4, 8, 12, 16, 20, 24, 32, 40, 48, 64])
        #expect(TSSpacing.space0_5 == 2)
        #expect(TSRadius.allTokens.map(\.value) == [4, 6, 8, 12, 16, 9999])
        #expect(TSSize.controlMd == 44)
        #expect(TSSize.allTokens.count == 9)
        #expect(TSOpacity.opacityDisabled == 0.45)
        #expect(TSOpacity.opacityPressed == 0.85)
    }

    @Test("Typography keeps DS point sizes and maps each style to a Dynamic Type base")
    func typography() {
        #expect(TSTypographyTokens.allTokens.count == 11)
        #expect(TSTypographyTokens.display.baseTextStyle.uiTextStyle == .largeTitle)
        #expect(TSTypographyTokens.body.baseTextStyle == .callout)
        #expect(TSTypographyTokens.title1.baseTextStyle.textStyle == .title)
        #expect(TSTypographyTokens.overline.tracking == 0.66)
        #expect(TSTypographyTokens.display.tracking == -0.72)
        #expect(TSTypographyTokens.title2.uiFontWeight == .semibold)
        #expect(TSTypographyTokens.label.swiftUIFontWeight == .medium)
        #expect(TSTypographyTokens.code.family == .mono)
        #expect(TSFontWeight.uiFontWeight(700) == .bold)
    }

    @Test("Shadows keep both light layers and the single dark layer")
    func shadows() {
        let medium = TSShadowTokens.shadowMd
        #expect(medium.layers(for: .light).count == 2)
        #expect(medium.layers(for: .dark).count == 1)
        let first = medium.light[0]
        #expect(first.offset == CGSize(width: 0, height: 4))
        #expect(first.radius == 3)
        #expect(first.spread == -1)
        #expect(abs(medium.dark[0].opacity - 0.55) < 0.005)
        #expect(TSShadowTokens.allTokens.map(\.name) == ["shadow-xs", "shadow-sm", "shadow-md", "shadow-lg", "shadow-focus"])
    }

    @Test("Motion durations are seconds and the easing is the CSS ease curve")
    @MainActor
    func motion() {
        #expect(TSMotion.allDurations.map(\.value) == [0.12, 0.15, 0.2])
        let easing = TSMotion.easingStandard
        #expect([easing.x1, easing.y1, easing.x2, easing.y2] == [0.25, 0.1, 0.25, 1])
        let timing = TSMotion.easingStandard.timingParameters
        #expect(timing.controlPoint1 == CGPoint(x: 0.25, y: 0.1))
        #expect(timing.controlPoint2 == CGPoint(x: 0.25, y: 1))
        #expect(TSComponentTokens.spinnerRotationDuration == 0.7)
        #expect(TSComponentTokens.checkboxBorderWidth == 1.5)
        #expect(TSComponentTokens.alertTitleFontWeight == 600)
    }

    @Test("TSIconName cases map to the kebab-case DS icon names")
    func iconNames() {
        #expect(TSIconName.allCases.count == 33)
        #expect(TSIconName.xCircle.assetName == "x-circle")
        #expect(TSIconName.checkCircle.rawValue == "check-circle")
        #expect(TSIconName.home.flipsForRightToLeftLayoutDirection == false)
        #expect(TSIconName.logout.flipsForRightToLeftLayoutDirection)
    }
}
