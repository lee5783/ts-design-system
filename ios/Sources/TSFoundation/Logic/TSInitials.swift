import Foundation

public enum TSInitials {
    public static func initials(for name: String, locale: Locale = .current) -> String {
        let words = name
            .split(whereSeparator: { $0.isWhitespace })
            .map { $0.drop(while: { $0.isPunctuation }) }
            .filter { !$0.isEmpty }
        guard let first = words.first?.first else { return "" }
        guard words.count > 1, let last = words.last?.first else {
            return String(first).uppercased(with: locale)
        }
        return (String(first) + String(last)).uppercased(with: locale)
    }
}
