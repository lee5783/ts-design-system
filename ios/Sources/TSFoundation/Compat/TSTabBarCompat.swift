import UIKit

package enum TSTabBarCompat {
    @MainActor
    package static func hideSystemTabBar(of controller: UITabBarController, path: TSCompatPath = TSCompat.tabBarHidden) {
        if path == .native, #available(iOS 18, *) {
            if !controller.isTabBarHidden {
                controller.setTabBarHidden(true, animated: false)
            }
        }
        controller.tabBar.isHidden = true
        controller.tabBar.isUserInteractionEnabled = false
        controller.tabBar.accessibilityElementsHidden = true
    }
}
