import SwiftUI
import Testing
import UIKit
@testable import TSFoundation

@Suite("Compat shims")
struct TSCompatTests {
    private var expectedRuntimePath: TSCompatPath {
        if #available(iOS 17, *) { .native } else { .fallback }
    }

    @Test("onChange and the focus effect pick their path by OS availability")
    func runtimePaths() {
        #expect(TSCompat.onChange == expectedRuntimePath)
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

    @Test("Both tab bar paths hide the system bar and take it out of touch", arguments: [TSCompatPath.native, .fallback])
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

    @Test("The tracking shim writes kerning only when tracking is non-zero")
    func trackingShim() {
        #expect(TSTrackingCompat.attributedString("A", tracking: 0).kern == nil)
        #expect(TSTrackingCompat.attributedString("A", tracking: 1.5).kern == 1.5)
    }
}
