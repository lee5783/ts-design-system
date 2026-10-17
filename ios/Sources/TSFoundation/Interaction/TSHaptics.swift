import UIKit

public enum TSHapticEvent: String, CaseIterable, Hashable, Sendable {
    case selection
    case success
    case warning
    case error

    public var notificationType: UINotificationFeedbackGenerator.FeedbackType? {
        switch self {
        case .selection: nil
        case .success: .success
        case .warning: .warning
        case .error: .error
        }
    }
}

@MainActor
public final class TSHaptics {
    public static let shared = TSHaptics()

    public var isEnabled: Bool

    private let perform: @MainActor (TSHapticEvent) -> Void

    public init(isEnabled: Bool = true, perform: @escaping @MainActor (TSHapticEvent) -> Void = TSHaptics.playSystemFeedback) {
        self.isEnabled = isEnabled
        self.perform = perform
    }

    public func play(_ event: TSHapticEvent) {
        guard isEnabled else { return }
        perform(event)
    }

    public static func playSystemFeedback(_ event: TSHapticEvent) {
        if let type = event.notificationType {
            let generator = UINotificationFeedbackGenerator()
            generator.notificationOccurred(type)
        } else {
            let generator = UISelectionFeedbackGenerator()
            generator.selectionChanged()
        }
    }
}
