import Foundation

public struct SamplePerson: Hashable, Sendable, Identifiable {
    public let id: String
    public let name: String
    public let role: String

    public init(id: String, name: String, role: String) {
        self.id = id
        self.name = name
        self.role = role
    }
}

public enum SamplePeople {
    public static let all: [SamplePerson] = [
        SamplePerson(id: "ada", name: "Ada Lovelace", role: "Engineer"),
        SamplePerson(id: "grace", name: "Grace Brewster Hopper", role: "Rear admiral"),
        SamplePerson(id: "alan", name: "Alan Turing", role: "Mathematician"),
        SamplePerson(id: "hedy", name: "Hedy", role: "Inventor"),
        SamplePerson(id: "taro", name: "山田太郎", role: "Designer"),
        SamplePerson(id: "layla", name: "ليلى أحمد", role: "Researcher")
    ]
}
