import SwiftUI
@testable import TSDesignSystem

@available(iOS 17, *)
struct LongCenterDialogView: View {
    static let screenHeight: CGFloat = 844
    static let statusBarHeight: CGFloat = 62

    var body: some View {
        let resolved = TSDialogStyleSpec.resolve(.init(variant: .center))
        TSDialogCenterContainer(
            resolved: resolved,
            onScrimTap: {},
            panel: TSDialogPanel(
                title: "Archive this project?",
                variant: .center,
                actions: [
                    TSDialogAction("Cancel"),
                    TSDialogAction("Archive", variant: .primary),
                ],
                hasBody: true,
                onAction: { _ in },
                onClose: {},
                content: Text("Archiving hides the project from everyone in your workspace and stops every scheduled export. You can restore it later from Settings. Members keep read-only access until the project is deleted, and shared links stop working right away."),
                focusesTitle: false
            )
        )
        .defaultScrollAnchor(.bottom)
        .safeAreaInset(edge: .top, spacing: 0) {
            Text("9:41")
                .font(.system(size: 17, weight: .semibold))
                .foregroundColor(TSColor.textPrimary.color)
                .frame(maxWidth: .infinity, minHeight: Self.statusBarHeight)
        }
        .frame(height: Self.screenHeight)
    }
}
