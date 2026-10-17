import CoreGraphics
import Testing
import TSFoundation
import TSTokens

@Suite("TSDialogStyleSpec")
struct TSDialogStyleSpecTests {
    @Test("The matrix covers center and sheet × default and accessibility text once each")
    func matrixIsComplete() {
        let configurations = TSDialogStyleSpec.allConfigurations
        #expect(configurations.count == 4)
        #expect(Set(configurations).count == 4)
    }

    @Test("Both variants share the scrim, surface, radius and text tokens", arguments: TSDialogStyleSpec.allConfigurations)
    func sharedTokens(configuration: TSDialogStyleSpec.Configuration) {
        let resolved = TSDialogStyleSpec.resolve(configuration)
        #expect(resolved.scrim == .overlay)
        #expect(resolved.fill == .surfaceElevated)
        #expect(resolved.cornerRadius == 16)
        #expect(resolved.paddingBottom == 24)
        #expect(resolved.titleFont == .title2)
        #expect(resolved.titleColor == .textPrimary)
        #expect(resolved.titleSpacing == 8)
        #expect(resolved.bodyFont == .body)
        #expect(resolved.bodyColor == .textSecondary)
        #expect(resolved.actionsTopSpacing == 24)
        #expect(resolved.actionsSpacing == 8)
        #expect(resolved.grabberWidth == 36)
        #expect(resolved.grabberHeight == 4)
        #expect(resolved.grabberColor == .borderStrong)
    }

    @Test("Center: a 440 pt panel with 16 pt margin, 24 pt padding, all corners rounded and shadow-lg")
    func center() {
        let resolved = TSDialogStyleSpec.resolve(.init(variant: .center))
        #expect(resolved.maxWidth == 440)
        #expect(resolved.outerMargin == 16)
        #expect(resolved.paddingTop == 24)
        #expect(resolved.paddingHorizontal == 24)
        #expect(resolved.contentTopInset == 24)
        #expect(resolved.roundsBottomCorners)
        #expect(resolved.shadow == TSShadowTokens.shadowLg)
        #expect(!resolved.showsGrabber)
        #expect(resolved.actionsAxis == .horizontal)
        #expect(!resolved.primaryActionFirst)
        #expect(!resolved.blockActions)
    }

    @Test("Sheet: full width, top corners only, 12/16/24 padding below a 36 × 4 grabber and stacked actions, primary on top")
    func sheet() {
        let resolved = TSDialogStyleSpec.resolve(.init(variant: .sheet))
        #expect(resolved.maxWidth == nil)
        #expect(resolved.outerMargin == 0)
        #expect(resolved.paddingTop == 12)
        #expect(resolved.paddingHorizontal == 16)
        #expect(resolved.contentTopInset == 32)
        #expect(!resolved.roundsBottomCorners)
        #expect(resolved.shadow == nil)
        #expect(resolved.showsGrabber)
        #expect(resolved.actionsAxis == .vertical)
        #expect(resolved.primaryActionFirst)
        #expect(resolved.blockActions)
    }

    @Test("At accessibility text sizes the center actions stack like the sheet's", arguments: TSDialogVariant.allCases)
    func accessibilitySizesStack(variant: TSDialogVariant) {
        let resolved = TSDialogStyleSpec.resolve(.init(variant: variant, isAccessibilitySize: true))
        #expect(resolved.actionsAxis == .vertical)
        #expect(resolved.primaryActionFirst)
        #expect(resolved.blockActions)
    }

    @Test("Actions keep primary last in a row and move it first when stacked")
    @MainActor
    func actionOrder() {
        let actions = [TSDialogAction("Cancel"), TSDialogAction("Delete", variant: .danger)]
        #expect(TSDialogLayout.orderedActions(actions, primaryFirst: false).map(\.label) == ["Cancel", "Delete"])
        #expect(TSDialogLayout.orderedActions(actions, primaryFirst: true).map(\.label) == ["Delete", "Cancel"])
        #expect(actions[0].variant == .secondary)
        #expect(actions[0].dismisses)
    }
}
