import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct DemoRootView: View {
    let launchPage: DemoPageID?

    @EnvironmentObject private var settings: DemoSettingsModel

    var body: some View {
        DemoNavigationContainer {
            if let launchPage {
                DemoPageView(page: launchPage)
            } else {
                CatalogView()
            }
        }
        .tsToastHost()
        .demoSettings(settings)
    }
}
