import SwiftUI

package extension View {
    func tsSheetPresentation(
        fitHeight: CGFloat?,
        cornerRadius: CGFloat,
        background: Color,
        path: TSCompatPath = TSCompat.sheetPresentation
    ) -> some View {
        modifier(TSSheetPresentationModifier(fitHeight: fitHeight, cornerRadius: cornerRadius, background: background, path: path))
    }
}

struct TSSheetPresentationModifier: ViewModifier {
    let fitHeight: CGFloat?
    let cornerRadius: CGFloat
    let background: Color
    let path: TSCompatPath

    func body(content: Content) -> some View {
        if path == .native, #available(iOS 16.4, *) {
            content
                .presentationDetents(fitHeight.map { [.height($0), .large] } ?? [.medium, .large])
                .presentationDragIndicator(.visible)
                .presentationCornerRadius(cornerRadius)
                .presentationBackground(background)
        } else {
            content
        }
    }
}
