import SwiftUI
import Testing
import UIKit
@testable import TSFoundation

@Suite("Compat shims")
struct TSCompatTests {
    private var expectedRuntimePath: TSCompatPath {
        if #available(iOS 17, *) { .native } else { .fallback }
    }

    @Test("onChange, the toggle trait and the focus effect pick their path by OS availability")
    func runtimePaths() {
        #expect(TSCompat.onChange == expectedRuntimePath)
        #expect(TSCompat.toggleTrait == expectedRuntimePath)
        #expect(TSCompat.focusEffect == expectedRuntimePath)
    }

    @Test("Hiding the system tab bar uses the iOS 18 API where it exists")
    func tabBarHiddenPath() {
        if #available(iOS 18, *) {
            #expect(TSCompat.tabBarHidden == .native)
        } else {
            #expect(TSCompat.tabBarHidden == .fallback)
        }
    }

    @Test("Both tab bar paths hide the system bar and take it out of touch and VoiceOver", arguments: [TSCompatPath.native, .fallback])
    @MainActor
    func hideSystemTabBar(path: TSCompatPath) {
        let controller = UITabBarController()
        controller.setViewControllers([UIViewController(), UIViewController()], animated: false)
        TSTabBarCompat.hideSystemTabBar(of: controller, path: path)
        #expect(controller.tabBar.isHidden)
        #expect(!controller.tabBar.isUserInteractionEnabled)
        #expect(controller.tabBar.accessibilityElementsHidden)
        if path == .native, #available(iOS 18, *) {
            #expect(controller.isTabBarHidden)
        }
    }

    @Test("Reading a controller's trait overrides uses the iOS 17 API where it exists")
    func traitOverridesPath() {
        #expect(TSCompat.traitOverrides == expectedRuntimePath)
    }

    @Test("The native path reports the overridden appearance, size and direction; the fallback reports none", arguments: [TSCompatPath.native, .fallback])
    @MainActor
    func traitOverrides(path: TSCompatPath) {
        let controller = UIViewController()
        if #available(iOS 17, *) {
            controller.traitOverrides.userInterfaceStyle = .dark
            controller.traitOverrides.layoutDirection = .rightToLeft
        }
        let overridden = TSTraitOverridesCompat.overriddenTraits(of: controller, path: path)
        if path == .native, #available(iOS 17, *) {
            #expect(overridden == TSOverriddenTraits(userInterfaceStyle: true, layoutDirection: true))
        } else {
            #expect(overridden == TSOverriddenTraits())
        }
        #expect(TSTraitOverridesCompat.overriddenTraits(of: UIViewController(), path: path) == TSOverriddenTraits())
    }

    @Test(
        "The toggle fallback is a button trait plus an on/off value",
        arguments: [
            (TSToggleKind.switch, true, "On"),
            (.switch, false, "Off"),
            (.checkbox, true, "Checked"),
            (.checkbox, false, "Unchecked"),
        ]
    )
    func toggleFallback(kind: TSToggleKind, isOn: Bool, value: String) {
        let resolved = TSAccessibilityTraitsCompat.toggle(isOn: isOn, kind: kind, path: .fallback)
        #expect(resolved.trait == .button)
        #expect(resolved.value == value)
        #expect(TSAccessibilityTraitsCompat.uiTraits(for: resolved.trait) == .button)
    }

    @Test("The native toggle path uses the iOS 17 toggle trait")
    func toggleNative() {
        let resolved = TSAccessibilityTraitsCompat.toggle(isOn: true, kind: .switch, path: .native)
        #expect(resolved.trait == .toggleButton)
        #expect(resolved.value == "On")
        if #available(iOS 17, *) {
            #expect(TSAccessibilityTraitsCompat.uiTraits(for: .toggleButton) == .toggleButton)
        } else {
            #expect(TSAccessibilityTraitsCompat.uiTraits(for: .toggleButton) == .button)
        }
    }

    @Test("Applying the toggle traits to a UIView follows the runtime path and replaces stale state")
    @MainActor
    func toggleApplyToView() {
        let view = UIView()
        view.accessibilityTraits = [.button, .header]

        TSAccessibilityTraitsCompat.apply(to: view, isOn: true, kind: .checkbox)
        TSAccessibilityTraitsCompat.apply(to: view, isOn: false, kind: .checkbox)

        let runtime = TSAccessibilityTraitsCompat.toggle(isOn: false, kind: .checkbox)
        #expect(view.isAccessibilityElement)
        #expect(view.accessibilityValue == "Unchecked")
        #expect(view.accessibilityTraits.contains(.header))
        #expect(view.accessibilityTraits.contains(TSAccessibilityTraitsCompat.uiTraits(for: runtime.trait)))
        if #available(iOS 17, *) {
            #expect(view.accessibilityTraits.contains(.toggleButton))
            #expect(!view.accessibilityTraits.contains(.button))
        } else {
            #expect(view.accessibilityTraits.contains(.button))
        }
    }

    @Test("An explicit toggle path is honoured and the selected trait never doubles the on/off value", arguments: [TSCompatPath.native, .fallback])
    @MainActor
    func toggleApplyWithPath(path: TSCompatPath) {
        let view = UIView()
        view.accessibilityTraits = [.selected]

        TSAccessibilityTraitsCompat.apply(to: view, isOn: true, kind: .switch, path: path)

        #expect(view.accessibilityValue == "On")
        #expect(!view.accessibilityTraits.contains(.selected))
        if path == .native, #available(iOS 17, *) {
            #expect(view.accessibilityTraits == .toggleButton)
        } else {
            #expect(view.accessibilityTraits == .button)
        }
    }

    @Test("The tracking shim writes kerning only when tracking is non-zero")
    func trackingShim() {
        #expect(TSTrackingCompat.attributedString("A", tracking: 0).kern == nil)
        #expect(TSTrackingCompat.attributedString("A", tracking: 1.5).kern == 1.5)
    }
}
