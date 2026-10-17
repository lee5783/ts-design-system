import Foundation

public enum TSAvatarAccessibility {
    public static func isExposed(name: String) -> Bool {
        !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
}
