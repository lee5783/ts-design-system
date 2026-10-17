import Testing
import UIKit
@testable import TSFoundation

@MainActor
@Suite("Sheet compat")
struct TSSheetCompatTests {
    private func makeSheet() throws -> UISheetPresentationController {
        let controller = UIViewController()
        controller.modalPresentationStyle = .pageSheet
        return try #require(controller.sheetPresentationController)
    }

    @Test("SwiftUI sheets use the native modifiers from iOS 16.4 and custom detents from iOS 16")
    func runtimePaths() {
        if #available(iOS 16.4, *) {
            #expect(TSCompat.sheetPresentation == .native)
        } else {
            #expect(TSCompat.sheetPresentation == .fallback)
        }
        if #available(iOS 16, *) {
            #expect(TSCompat.customSheetDetents == .native)
        } else {
            #expect(TSCompat.customSheetDetents == .fallback)
        }
    }

    @Test("Detents by OS: fit + large on iOS 16+, medium + large on iOS 15")
    func detentPlan() {
        #expect(TSSheetCompat.detents(path: .native) == [.fit, .large])
        #expect(TSSheetCompat.detents(path: .fallback) == [.medium, .large])
    }

    @Test("The iOS 15 path configures medium and large detents, the 16 pt radius and the grabber")
    func fallbackConfiguration() throws {
        let sheet = try makeSheet()
        TSSheetCompat.configure(sheet, cornerRadius: 16, path: .fallback) { 200 }
        #expect(detentIdentifiers(sheet) == [.medium, .large])
        #expect(sheet.selectedDetentIdentifier == .medium)
        #expect(sheet.preferredCornerRadius == 16)
        #expect(sheet.prefersGrabberVisible)
        #expect(!sheet.prefersScrollingExpandsWhenScrolledToEdge)
    }

    @Test("The iOS 16+ path adds a fit-content custom detent that starts selected")
    func nativeConfiguration() throws {
        let sheet = try makeSheet()
        TSSheetCompat.configure(sheet, cornerRadius: 16, path: .native) { 200 }
        let fit = TSSheetCompat.identifier(for: .fit)
        if #available(iOS 16, *) {
            #expect(fit.rawValue == TSSheetCompat.fitDetentIdentifier)
            #expect(detentIdentifiers(sheet) == [fit, .large])
            #expect(sheet.selectedDetentIdentifier == fit)
        } else {
            #expect(detentIdentifiers(sheet) == [.medium, .large])
        }
    }
}

@MainActor
private func detentIdentifiers(_ sheet: UISheetPresentationController) -> [UISheetPresentationController.Detent.Identifier] {
    if #available(iOS 16, *) {
        return sheet.detents.map { $0.identifier }
    }
    return sheet.detents.isEmpty ? [] : [.medium, .large]
}
