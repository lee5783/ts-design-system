import Foundation

public struct SampleMessage: Hashable, Sendable, Identifiable {
    public let id: String
    public let title: String
    public let body: String
    public let actionTitle: String?

    public init(id: String, title: String, body: String, actionTitle: String?) {
        self.id = id
        self.title = title
        self.body = body
        self.actionTitle = actionTitle
    }
}

public enum SampleMessages {
    public static let saved = SampleMessage(id: "saved", title: "Changes saved", body: "Your profile was updated.", actionTitle: nil)
    public static let deleted = SampleMessage(id: "deleted", title: "Message deleted", body: "The message was moved to the trash.", actionTitle: "Undo")
    public static let offline = SampleMessage(id: "offline", title: "You are offline", body: "Some features are unavailable until the connection is restored.", actionTitle: "Retry")
    public static let confirmDelete = SampleMessage(id: "confirm-delete", title: "Delete this project?", body: "This removes the project and its files for everyone. This cannot be undone.", actionTitle: "Delete")

    public static let all: [SampleMessage] = [saved, deleted, offline, confirmDelete]
}
