import SwiftUI

struct TSTextInputTraitsModifier: ViewModifier {
    let traits: TSTextInputTraits

    func body(content: Content) -> some View {
        content
            .keyboardType(traits.keyboardType)
            .textContentType(traits.textContentType)
            .textInputAutocapitalization(traits.textInputAutocapitalization)
            .disableAutocorrection(!traits.isAutocorrectionEnabled)
            .submitLabel(traits.returnKey.submitLabel)
    }
}

extension View {
    func tsTextInputTraits(_ traits: TSTextInputTraits) -> some View {
        modifier(TSTextInputTraitsModifier(traits: traits))
    }
}

extension TSTextInputTraits {
    var textInputAutocapitalization: TextInputAutocapitalization {
        switch autocapitalization {
        case .none: .never
        case .words: .words
        case .allCharacters: .characters
        default: .sentences
        }
    }
}

extension TSReturnKey {
    var submitLabel: SubmitLabel {
        switch self {
        case .default: .return
        case .done: .done
        case .go: .go
        case .next: .next
        case .search: .search
        case .send: .send
        case .continue: .continue
        }
    }
}
