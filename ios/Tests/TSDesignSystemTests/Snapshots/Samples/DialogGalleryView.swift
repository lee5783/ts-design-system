import SwiftUI
@testable import TSDesignSystem

struct DialogGalleryView: View {
    let variant: TSDialogVariant

    var body: some View {
        let resolved = TSDialogStyleSpec.resolve(.init(variant: variant))
        TSDialogPanel(
            title: "Delete this project?",
            variant: variant,
            actions: [
                TSDialogAction("Cancel"),
                TSDialogAction("Delete", variant: .danger),
            ],
            hasBody: true,
            onAction: { _ in },
            onClose: {},
            content: Text("This removes the project and its files for everyone. This cannot be undone."),
            focusesTitle: false
        )
        .padding(variant == .center ? resolved.outerMargin : 0)
        .padding(.top, variant == .sheet ? TSSpacing.space6 : 0)
        .frame(maxWidth: .infinity)
        .background(resolved.scrim.color)
    }
}
