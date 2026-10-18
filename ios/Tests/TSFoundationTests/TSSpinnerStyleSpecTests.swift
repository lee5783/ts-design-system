import Testing
import TSFoundation
import TSTokens
import UIKit

@Suite("TSSpinnerStyleSpec")
struct TSSpinnerStyleSpecTests {
    @Test("The matrix is 3 sizes × 3 tones once each")
    func matrixIsComplete() {
        #expect(TSSpinnerStyleSpec.allConfigurations.count == 9)
        #expect(Set(TSSpinnerStyleSpec.allConfigurations).count == 9)
    }

    @Test("Sizes map to the icon-sm/md/lg tokens", arguments: zip(TSSpinnerSize.allCases, [TSSize.iconSm, TSSize.iconMd, TSSize.iconLg]))
    func sizes(size: TSSpinnerSize, expected: CGFloat) {
        #expect(TSSpinnerStyleSpec.resolve(.init(size: size)).size == expected)
    }

    @Test("The stroke keeps the Button spinner's ratio", arguments: TSSpinnerStyleSpec.allConfigurations)
    func stroke(configuration: TSSpinnerStyleSpec.Configuration) {
        let resolved = TSSpinnerStyleSpec.resolve(configuration)
        let ratio = TSComponentTokens.spinnerStrokeWidth / TSComponentTokens.spinnerSize
        #expect(abs(resolved.strokeWidth - resolved.size * ratio) < 0.0001)
        #expect(resolved.rotationDuration == TSComponentTokens.spinnerRotationDuration)
    }

    @Test("Strokes are 2 / 2.5 / 3 and the small spinner equals the Button spinner")
    func smallMatchesButton() {
        let strokes = TSSpinnerSize.allCases.map { TSSpinnerStyleSpec.resolve(.init(size: $0)).strokeWidth }
        #expect(strokes == [2, 2.5, 3])
        let button = TSButtonStyleSpec.resolve(.init(state: .loading))
        let small = TSSpinnerStyleSpec.resolve(.init(size: .sm))
        #expect(small.size == button.spinnerSize)
        #expect(small.strokeWidth == button.spinnerStrokeWidth)
    }

    @Test("Tones map to text-primary, text-secondary and accent-primary", arguments: zip(TSSpinnerTone.allCases, [TSColor.textPrimary, .textSecondary, .accentPrimary]))
    func tones(tone: TSSpinnerTone, expected: TSColor) {
        #expect(TSSpinnerStyleSpec.resolve(.init(tone: tone)).color == expected)
    }

    @Test("Defaults are md and primary")
    func defaults() {
        #expect(TSSpinnerStyleSpec.Configuration() == .init(size: .md, tone: .primary))
    }
}
