import Foundation
import TSTokens

struct DesignTokensSource {
    enum SourceError: Error {
        case unreadable(String)
        case unknownColor(String)
        case invalidHex(String)
    }

    let colors: [String: Any]

    init(filePath: String = #filePath) throws {
        let root = URL(fileURLWithPath: filePath)
            .deletingLastPathComponent()
            .deletingLastPathComponent()
            .deletingLastPathComponent()
            .deletingLastPathComponent()
        let data = try Data(contentsOf: root.appendingPathComponent("design/source/tokens.json"))
        guard
            let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
            let color = json["color"] as? [String: Any],
            let tokens = color["tokens"] as? [[String: Any]]
        else {
            throw SourceError.unreadable("design/source/tokens.json")
        }
        colors = Dictionary(uniqueKeysWithValues: tokens.compactMap { token in
            (token["name"] as? String).map { ($0, token["value"] as Any) }
        })
    }

    func rgba(_ name: String, theme: String) throws -> TSRGBA {
        guard let value = colors[name] else { throw SourceError.unknownColor(name) }
        let raw: String? = if let themed = value as? [String: String] { themed[theme] } else { value as? String }
        guard let raw else { throw SourceError.unknownColor(name) }
        if raw.hasPrefix("{"), raw.hasSuffix("}") {
            return try rgba(String(raw.dropFirst().dropLast()), theme: theme)
        }
        return try Self.parseHex(raw)
    }

    static func parseHex(_ raw: String) throws -> TSRGBA {
        var digits = String(raw.dropFirst())
        if digits.count == 3 || digits.count == 4 {
            digits = digits.map { "\($0)\($0)" }.joined()
        }
        if digits.count == 6 {
            digits += "ff"
        }
        guard raw.hasPrefix("#"), digits.count == 8, let hex = UInt32(digits, radix: 16) else {
            throw SourceError.invalidHex(raw)
        }
        return TSRGBA(hex: hex)
    }
}
