import { execFile } from "node:child_process";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { describe, expect, it } from "vitest";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const run = promisify(execFile);

async function dryRun(target: string, ...variables: string[]): Promise<string> {
  const { stdout } = await run("make", ["-n", "-s", target, ...variables], { cwd: repoRoot });
  return stdout;
}

describe("iOS SwiftUI demo targets", () => {
  it("builds the SwiftUI demo project for the simulator", async () => {
    const output = await dryRun("demo-ios-swiftui");
    expect(output).toContain("xcodebuild build -project demos/ios-swiftui/TSDemoSwiftUI.xcodeproj -scheme TSDemoSwiftUI -destination 'generic/platform=iOS Simulator'");
  });

  it("tests demo support and the SwiftUI demo on the pinned simulator", async () => {
    const output = await dryRun("demos-test", "XCRUN=false");
    expect(output).toContain("cd demos/ios-demo-support && xcodebuild test-without-building -scheme TSDemoSupport -destination 'platform=iOS Simulator,name=iPhone 17,OS=26.5'");
    expect(output).toContain("xcodebuild test-without-building -project demos/ios-swiftui/TSDemoSwiftUI.xcodeproj -scheme TSDemoSwiftUI -destination 'platform=iOS Simulator,name=iPhone 17,OS=26.5'");
  });

  it("builds the SwiftUI demo tests once, then runs only the selected tests without rebuilding", async () => {
    const full = await dryRun("demo-ios-swiftui-test", "IOS_RUNTIMES=pinned");
    expect(full.indexOf("xcodebuild build-for-testing -project demos/ios-swiftui/TSDemoSwiftUI.xcodeproj")).toBeGreaterThanOrEqual(0);
    expect(full.indexOf("xcodebuild build-for-testing")).toBeLessThan(full.indexOf("xcodebuild test-without-building"));
    const narrowed = await dryRun("demo-ios-swiftui-test-only", "IOS_RUNTIMES=pinned", "DEMO_IOS_SWIFTUI_TESTS=TSDemoSwiftUIUITests/ThemeOverrideTests");
    expect(narrowed).not.toContain("build-for-testing");
    expect(narrowed).toContain("-only-testing:TSDemoSwiftUIUITests/ThemeOverrideTests");
  });

  it("adds the oldest runtime when it differs from the pinned one", async () => {
    const output = await dryRun("demo-ios-swiftui-test", "IOS_RUNTIMES=pinned oldest", "IOS_OLDEST_OS=18.6", "IOS_OLDEST_DEVICE=iPhone 16");
    expect(output).toContain("name=iPhone 16,OS=18.6");
    expect(output.match(/xcodebuild test-without-building/g)).toHaveLength(2);
  });

  it("runs the boundary checker", async () => {
    const output = await dryRun("demos-check-boundaries");
    expect(output).toContain("npm --prefix tools run --silent demos-check-boundaries");
    expect(output).toContain("npm --prefix tools run --silent demos-check-catalog");
  });
});

describe("iOS UIKit demo targets", () => {
  it("builds the UIKit demo project for the simulator", async () => {
    const output = await dryRun("demo-ios-uikit");
    expect(output).toContain("xcodebuild build -project demos/ios-uikit/TSDemoUIKit.xcodeproj -scheme TSDemoUIKit -destination 'generic/platform=iOS Simulator'");
  });

  it("runs the UIKit demo suite as part of demos-test", async () => {
    const output = await dryRun("demos-test", "XCRUN=false");
    expect(output).toContain("xcodebuild test-without-building -project demos/ios-uikit/TSDemoUIKit.xcodeproj -scheme TSDemoUIKit -destination 'platform=iOS Simulator,name=iPhone 17,OS=26.5'");
  });

  it("builds the UIKit demo tests once, then runs only the selected tests without rebuilding", async () => {
    const full = await dryRun("demo-ios-uikit-test", "IOS_RUNTIMES=pinned");
    expect(full.indexOf("xcodebuild build-for-testing")).toBeLessThan(full.indexOf("xcodebuild test-without-building"));
    const narrowed = await dryRun("demo-ios-uikit-test-only", "IOS_RUNTIMES=pinned", "DEMO_IOS_UIKIT_TESTS=TSDemoUIKitUITests/ThemeOverrideTests");
    expect(narrowed).not.toContain("build-for-testing");
    expect(narrowed).toContain("-only-testing:TSDemoUIKitUITests/ThemeOverrideTests");
  });

  it("adds the oldest runtime when it differs from the pinned one", async () => {
    const output = await dryRun("demo-ios-uikit-test", "IOS_RUNTIMES=pinned oldest", "IOS_OLDEST_OS=18.6", "IOS_OLDEST_DEVICE=iPhone 16");
    expect(output).toContain("name=iPhone 16,OS=18.6");
    expect(output.match(/xcodebuild test-without-building/g)).toHaveLength(2);
  });

  it("installs and launches the UIKit demo with launch options", async () => {
    const output = await dryRun("demo-ios-uikit-run", "XCRUN=xcrun", "DEMO_ARGS=-TSDemoPage icons");
    expect(output).toContain("xcrun simctl install booted .build/xcode-demos/Build/Products/Debug-iphonesimulator/TSDemoUIKit.app");
    expect(output).toContain("xcrun simctl launch booted com.tinysol.design-system.sample -TSDemoPage icons");
  });
});
