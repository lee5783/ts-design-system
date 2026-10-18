import { mkdir, mkdtemp, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import {
  DEMO_BOUNDARIES,
  type DemoBoundary,
  checkDemoBoundaries,
  checkSwiftSource,
  checkXcodeProject,
  formatReports,
} from "../src/check-boundaries.ts";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");

function boundary(name: string): DemoBoundary {
  const found = DEMO_BOUNDARIES.find((entry) => entry.name === name);
  if (!found) throw new Error(`no boundary ${name}`);
  return found;
}

const swiftUI = boundary("ios-swiftui");
const swiftUIApp = swiftUI.kind === "swift-app" ? swiftUI : undefined;

const PBXPROJ = `
		A /* TSDesignSystem */ = {
			isa = XCSwiftPackageProductDependency;
			package = B;
			productName = TSDesignSystem;
		};
		C /* XCLocalSwiftPackageReference "../.." */ = {
			isa = XCLocalSwiftPackageReference;
			relativePath = ../..;
		};
`;

let root: string;

async function put(relativePath: string, content: string): Promise<void> {
  const target = path.join(root, relativePath);
  await mkdir(path.dirname(target), { recursive: true });
  await writeFile(target, content);
}

beforeEach(async () => {
  root = await mkdtemp(path.join(tmpdir(), "demos-check-"));
});

afterEach(async () => {
  await rm(root, { recursive: true, force: true });
});

describe("checkSwiftSource", () => {
  const allowed = ["SwiftUI", "TSDesignSystem", "TSDemoSupport"];

  it("accepts the public product and demo support", () => {
    expect(checkSwiftSource("d", "f.swift", "import SwiftUI\nimport TSDesignSystem\nimport TSDemoSupport\n", allowed)).toEqual([]);
  });

  it.each(["TSFoundation", "TSTokens", "TSDesignSystemUIKit", "TSTestSupport", "UIKit"])("rejects import %s", (module) => {
    const violations = checkSwiftSource("d", "f.swift", `import SwiftUI\nimport ${module}\n`, allowed);
    expect(violations).toHaveLength(1);
    expect(violations[0]).toMatchObject({ line: 2, message: expect.stringContaining(`imports ${module}`) });
  });

  it("rejects @testable even for an allowed module", () => {
    const violations = checkSwiftSource("d", "f.swift", "@testable import TSDesignSystem\n", allowed);
    expect(violations.map((violation) => violation.message)).toEqual(["uses @testable import TSDesignSystem"]);
  });

  it("rejects @_spi and scoped imports of internals", () => {
    const source = "@_spi(Internal) import TSDesignSystem\nimport struct TSFoundation.TSFont\n";
    const messages = checkSwiftSource("d", "f.swift", source, allowed).map((violation) => violation.message);
    expect(messages).toContain("uses @_spi");
    expect(messages.some((message) => message.startsWith("imports TSFoundation"))).toBe(true);
  });
});

describe("checkXcodeProject", () => {
  it("accepts the allowed products and local packages", () => {
    expect(swiftUIApp).toBeDefined();
    expect(checkXcodeProject("ios-swiftui", "p", PBXPROJ, swiftUIApp!)).toEqual([]);
  });

  it("rejects another library product, a foreign local package and remote packages", () => {
    const pbxproj = PBXPROJ.replace("productName = TSDesignSystem;", "productName = TSTokens;")
      .replace("relativePath = ../..;", "relativePath = ../../ios;")
      .concat("\n\t\tD = {\n\t\t\tisa = XCRemoteSwiftPackageReference;\n\t\t};\n");
    const messages = checkXcodeProject("ios-swiftui", "p", pbxproj, swiftUIApp!).map((violation) => violation.message);
    expect(messages).toHaveLength(3);
    expect(messages[0]).toContain("links package product TSTokens");
    expect(messages[1]).toContain("references local package ../../ios");
    expect(messages[2]).toContain("remote Swift package");
  });

  it("keeps the UIKit demo on the UIKit product only", () => {
    const uiKit = boundary("ios-uikit");
    expect(uiKit.kind).toBe("swift-app");
    if (uiKit.kind !== "swift-app") return;
    const uiKitProject = PBXPROJ.replace("productName = TSDesignSystem;", "productName = TSDesignSystemUIKit;");
    expect(checkXcodeProject("ios-uikit", "p", uiKitProject, uiKit)).toEqual([]);
    const messages = checkXcodeProject("ios-uikit", "p", PBXPROJ, uiKit).map((violation) => violation.message);
    expect(messages).toEqual([expect.stringContaining("links package product TSDesignSystem;")]);
  });
});

describe("checkDemoBoundaries", () => {
  it("skips demos that have no sources yet", async () => {
    const reports = await checkDemoBoundaries(root);
    expect(reports.map((report) => report.demo)).toEqual(["ios-demo-support", "ios-swiftui", "ios-uikit", "android-demo-support", "android-compose", "android-views"]);
    expect(reports.every((report) => report.scannedFiles === 0 && report.violations.length === 0)).toBe(true);
    expect(formatReports(reports)).toContain("ios-uikit: no sources yet, skipped");
  });

  it("applies the test allowlist to UI test folders and the app allowlist elsewhere", async () => {
    await put("demos/ios-swiftui/TSDemoSwiftUI/App.swift", "import SwiftUI\nimport TSDesignSystem\n");
    await put("demos/ios-swiftui/TSDemoSwiftUIUITests/Smoke.swift", "import XCTest\nimport TSDesignSystem\n");
    const [report] = await checkDemoBoundaries(root, [swiftUI]);
    expect(report?.scannedFiles).toBe(2);
    expect(report?.violations).toEqual([
      expect.objectContaining({ file: "demos/ios-swiftui/TSDemoSwiftUIUITests/Smoke.swift", line: 2 }),
    ]);
  });

  it("keeps the UIKit and SwiftUI demos on their own toolkit", async () => {
    await put("demos/ios-uikit/TSDemoUIKit/Scene.swift", "import UIKit\nimport SwiftUI\nimport TSDesignSystemUIKit\n");
    const [report] = await checkDemoBoundaries(root, [boundary("ios-uikit")]);
    expect(report?.violations.map((violation) => violation.message)).toEqual([expect.stringContaining("imports SwiftUI")]);
  });

  it("forbids package dependencies in demo support", async () => {
    await put("demos/ios-demo-support/Package.swift", 'dependencies: [.package(path: "../..")],\n');
    await put("demos/ios-demo-support/Sources/TSDemoSupport/A.swift", "import Foundation\nimport TSDesignSystem\n");
    const [report] = await checkDemoBoundaries(root, [boundary("ios-demo-support")]);
    expect(report?.violations).toHaveLength(2);
  });

  it("flags Android internals and the other toolkit in the gradle hooks", async () => {
    await put("demos/android-views/app/build.gradle.kts", 'implementation("com.tinysol.designsystem:core")\nimplementation(project(":views"))\nimplementation("androidx.compose.ui:ui")\nimplementation(platform("com.tinysol.designsystem:bom:0.1.0"))\n');
    const [report] = await checkDemoBoundaries(root, [boundary("android-views")]);
    expect(report?.violations.map((violation) => violation.line)).toEqual([1, 2, 3]);
  });

  it("keeps Compose out of the Views demo's XML layouts", async () => {
    await put(
      "demos/android-views/app/src/main/res/layout/page_colors.xml",
      '<LinearLayout>\n    <androidx.compose.ui.platform.ComposeView />\n    <com.tinysol.designsystem.views.TSButtonView />\n</LinearLayout>\n',
    );
    const [report] = await checkDemoBoundaries(root, [boundary("android-views")]);
    expect(report?.violations.map((violation) => `${path.basename(violation.file)}:${violation.line}`)).toEqual(["page_colors.xml:2"]);
  });

  it("keeps the Compose demo on its public coordinates and its own toolkit", async () => {
    await put(
      "demos/android-compose/app/build.gradle.kts",
      'implementation(platform("com.tinysol.designsystem:bom:0.1.0"))\nimplementation("com.tinysol.designsystem:compose")\nimplementation("com.tinysol.designsystem:views")\ntestImplementation(project(":test-support"))\n',
    );
    await put(
      "demos/android-compose/app/src/main/kotlin/A.kt",
      "import com.tinysol.designsystem.compose.theme.TSTheme\nimport com.tinysol.designsystem.views.theme.tsColor\nimport com.tinysol.designsystem.testsupport.snapshot.SnapshotMatrix\n",
    );
    const [report] = await checkDemoBoundaries(root, [boundary("android-compose")]);
    expect(report?.violations.map((violation) => `${path.basename(violation.file)}:${violation.line}`)).toEqual([
      "build.gradle.kts:3",
      "build.gradle.kts:4",
      "A.kt:2",
      "A.kt:3",
    ]);
  });

  it("keeps Android demo support free of the library and of any toolkit", async () => {
    await put("demos/android-demo-support/build.gradle.kts", 'implementation("com.tinysol.designsystem:core:0.1.0")\nimplementation("androidx.compose.ui:ui")\ntestImplementation(libs.junit4)\n');
    await put(
      "demos/android-demo-support/src/main/kotlin/A.kt",
      "import com.tinysol.designsystem.sample.support.R\nimport com.tinysol.designsystem.core.icon.TSIcon\n",
    );
    const [report] = await checkDemoBoundaries(root, [boundary("android-demo-support")]);
    expect(report?.violations.map((violation) => `${path.basename(violation.file)}:${violation.line}`)).toEqual([
      "build.gradle.kts:1",
      "build.gradle.kts:2",
      "A.kt:2",
    ]);
  });

  it("passes on this repository", async () => {
    const reports = await checkDemoBoundaries(repoRoot);
    expect(reports.flatMap((report) => report.violations)).toEqual([]);
    expect(reports.find((report) => report.demo === "ios-swiftui")?.scannedFiles).toBeGreaterThan(0);
    expect(reports.find((report) => report.demo === "ios-uikit")?.scannedFiles).toBeGreaterThan(0);
    expect(reports.find((report) => report.demo === "android-demo-support")?.scannedFiles).toBeGreaterThan(0);
    expect(reports.find((report) => report.demo === "android-compose")?.scannedFiles).toBeGreaterThan(0);
    expect(reports.find((report) => report.demo === "android-views")?.scannedFiles).toBeGreaterThan(0);
  });
});
