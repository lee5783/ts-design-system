import SwiftUI
import UIKit

struct TSMultilineTextView: UIViewRepresentable {
    @Binding var text: String
    @Binding var isEditing: Bool
    let placeholder: String
    let font: TSFont
    let textColor: TSColor
    let placeholderColor: TSColor
    let verticalInset: CGFloat
    let isEditable: Bool
    let inputTraits: TSTextInputTraits
    let sizeCategory: UIContentSizeCategory
    let layoutDirection: LayoutDirection

    func makeCoordinator() -> Coordinator {
        Coordinator(text: $text, isEditing: $isEditing)
    }

    func makeUIView(context: Context) -> TSGrowingTextView {
        let textView = TSGrowingTextView()
        textView.delegate = context.coordinator
        return textView
    }

    func updateUIView(_ textView: TSGrowingTextView, context: Context) {
        context.coordinator.text = $text
        context.coordinator.isEditing = $isEditing
        textView.verticalInset = verticalInset
        textView.apply(TSGrowingTextView.Typography(
            font: font,
            color: textColor.uiColor,
            placeholder: placeholder,
            placeholderColor: placeholderColor.uiColor,
            sizeCategory: sizeCategory,
            alignment: layoutDirection == .rightToLeft ? .right : .left
        ))
        textView.styledText = text
        textView.isEditable = isEditable
        textView.isSelectable = isEditable
        inputTraits.apply(to: textView)
    }

    @available(iOS 16, *)
    func sizeThatFits(_ proposal: ProposedViewSize, uiView: TSGrowingTextView, context: Context) -> CGSize? {
        guard let width = proposal.width, width > 0 else { return nil }
        return CGSize(width: width, height: uiView.layout(forWidth: width).height)
    }

    final class Coordinator: NSObject, UITextViewDelegate {
        var text: Binding<String>
        var isEditing: Binding<Bool>

        init(text: Binding<String>, isEditing: Binding<Bool>) {
            self.text = text
            self.isEditing = isEditing
        }

        func textViewDidChange(_ textView: UITextView) {
            text.wrappedValue = textView.text ?? ""
        }

        func textViewDidBeginEditing(_ textView: UITextView) {
            isEditing.wrappedValue = true
        }

        func textViewDidEndEditing(_ textView: UITextView) {
            isEditing.wrappedValue = false
        }
    }
}
