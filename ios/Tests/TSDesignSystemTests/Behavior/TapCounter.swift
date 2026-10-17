@MainActor
final class TapCounter {
    private(set) var count = 0

    func tap() {
        count += 1
    }
}
