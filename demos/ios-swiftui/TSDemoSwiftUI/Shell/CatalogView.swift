import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct CatalogView: View {
    @EnvironmentObject private var settings: DemoSettingsModel
    @State private var isShowingSettings = false
    @State private var selectedPage: DemoPageID?

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: TSSpacing.space6) {
                ForEach(DemoCatalog.sections) { section in
                    TSListSection(section.title) {
                        ForEach(section.pages) { page in
                            TSListItem(page.title, showsChevron: true, action: { selectedPage = page })
                                .accessibilityIdentifier("demo.catalog.\(page.rawValue)")
                        }
                    }
                }
            }
            .padding(TSSpacing.space4)
        }
        .background(TSColor.backgroundPrimary.color.edgesIgnoringSafeArea(.all))
        .background(
            NavigationLink(isActive: isShowingPage) {
                if let selectedPage {
                    DemoPageView(page: selectedPage)
                }
            } label: {
                EmptyView()
            }
            .hidden()
        )
        .navigationTitle(DemoStrings.string(.appTitle))
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                NavigationLink(destination: AboutView()) {
                    DemoIcon(name: .info)
                }
                .accessibilityLabel(DemoStrings.string(.aboutTitle))
                .accessibilityIdentifier("demo.about")
            }
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                DemoThemeToggle()
                Button {
                    isShowingSettings = true
                } label: {
                    DemoIcon(name: .settings)
                }
                .accessibilityLabel(DemoStrings.string(.settingsTitle))
                .accessibilityIdentifier("demo.settings")
            }
        }
        .sheet(isPresented: $isShowingSettings) {
            DemoSettingsSheet()
                .environmentObject(settings)
                .demoSettings(settings)
        }
    }

    private var isShowingPage: Binding<Bool> {
        Binding(
            get: { selectedPage != nil },
            set: { isActive in
                if !isActive {
                    selectedPage = nil
                }
            }
        )
    }
}
