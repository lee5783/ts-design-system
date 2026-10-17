import Combine
import Testing
import TSDemoSupport

@Suite("DemoLaunchOptions")
struct DemoLaunchOptionsTests {
    @Test("Parses every option")
    func parsesAll() {
        let options = DemoLaunchOptions(arguments: [
            "TSDemoSwiftUI",
            "-TSDemoTheme", "dark",
            "-TSDemoTextSize", "accessibility5",
            "-TSDemoRightToLeft", "YES",
            "-TSDemoReduceMotion", "true",
            "-TSDemoPage", "typography"
        ])
        #expect(options == DemoLaunchOptions(theme: .dark, textSize: .accessibility5, isRightToLeft: true, reduceMotion: true, page: .typography))
    }

    @Test("Ignores missing and unknown values")
    func ignoresInvalid() {
        let options = DemoLaunchOptions(arguments: ["-TSDemoTheme", "sepia", "-TSDemoRightToLeft", "maybe", "-TSDemoPage"])
        #expect(options == DemoLaunchOptions())
    }

    @Test("Round-trips through its argument list")
    func roundTrip() {
        let options = DemoLaunchOptions(theme: .light, textSize: .small, isRightToLeft: false, reduceMotion: true, page: .icons)
        #expect(DemoLaunchOptions(arguments: options.arguments) == options)
    }
}

@Suite("DemoSettingsModel")
@MainActor
struct DemoSettingsModelTests {
    @Test("Defaults follow the system")
    func defaults() {
        let model = DemoSettingsModel()
        #expect(model.isDefault)
        #expect(model.theme == .system)
        #expect(model.textSize == .system)
    }

    @Test("Starts from launch options")
    func launchOptions() {
        let model = DemoSettingsModel(launchOptions: DemoLaunchOptions(theme: .dark, textSize: .accessibility5, isRightToLeft: true))
        #expect(model.theme == .dark)
        #expect(model.textSize == .accessibility5)
        #expect(model.isRightToLeft)
        #expect(!model.reduceMotion)
        #expect(!model.isDefault)
    }

    @Test("Publishes changes and resets")
    func publishesAndResets() {
        let model = DemoSettingsModel()
        var changes = 0
        let cancellable = model.objectWillChange.sink { changes += 1 }
        model.theme = .dark
        model.reduceMotion = true
        #expect(changes == 2)
        model.reset()
        #expect(model.isDefault)
        cancellable.cancel()
    }

    @Test("Accessibility sizes are flagged")
    func accessibilitySizes() {
        #expect(DemoTextSize.allCases.filter(\.isAccessibilitySize).count == 5)
        #expect(!DemoTextSize.extraExtraExtraLarge.isAccessibilitySize)
    }
}
