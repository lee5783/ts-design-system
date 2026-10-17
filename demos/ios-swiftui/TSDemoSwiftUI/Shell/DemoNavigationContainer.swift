import SwiftUI

struct DemoNavigationContainer<Root: View>: View {
    private let root: Root

    init(@ViewBuilder root: () -> Root) {
        self.root = root()
    }

    var body: some View {
        if #available(iOS 16, *) {
            NavigationStack {
                root
            }
        } else {
            NavigationView {
                root
            }
            .navigationViewStyle(.stack)
        }
    }
}
