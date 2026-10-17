import { execFileSync } from "node:child_process";
import { mkdir, mkdtemp, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { affectedAreas, detectChanges, formatOutputs, matchesPattern, outputs } from "../src/changes.ts";

function outputsFor(files: string[]): Record<string, string> {
  return outputs({ reason: "test", files, areas: affectedAreas(files) });
}

describe("matchesPattern", () => {
  it("matches a double star across directories and a single star within one", () => {
    expect(matchesPattern("ios/Sources/TSTokens/Generated/Colors.swift", "ios/**")).toBe(true);
    expect(matchesPattern("iosx/file.swift", "ios/**")).toBe(false);
    expect(matchesPattern("demos/ios-swiftui/Package.resolved", "demos/ios-*/**/Package.resolved")).toBe(true);
    expect(matchesPattern("Makefile", "Makefile")).toBe(true);
    expect(matchesPattern("tools/Makefile", "Makefile")).toBe(false);
    expect(matchesPattern("a.b", "a*b")).toBe(true);
    expect(matchesPattern("axb", "a.b")).toBe(false);
  });
});

describe("path filters", () => {
  it("skips the Android library and demo jobs for an iOS-only change", () => {
    const result = outputsFor(["ios/Sources/TSDesignSystem/Components/Button/TSButton.swift", "ios/Tests/TSDesignSystemTests/Snapshots/__Snapshots__/TSButtonSnapshotTests/light.png"]);
    expect(result).toEqual({ ios: "true", android: "false", boundaries: "false", ios_demos: '["swiftui","uikit"]', android_demos: "[]" });
  });

  it("skips the iOS library and demo jobs for an Android-only change", () => {
    const result = outputsFor(["android/compose/src/main/kotlin/com/ts/ds/compose/components/button/TSButton.kt"]);
    expect(result).toEqual({ ios: "false", android: "true", boundaries: "false", ios_demos: "[]", android_demos: '["compose","views"]' });
  });

  it("runs only the touched demo and the boundary check for a demo-only change", () => {
    expect(outputsFor(["demos/ios-swiftui/TSDemoSwiftUI/App/ContentView.swift"])).toEqual({ ios: "false", android: "false", boundaries: "true", ios_demos: '["swiftui"]', android_demos: "[]" });
    expect(outputsFor(["demos/android-views/app/src/main/kotlin/com/ts/ds/demo/views/MainActivity.kt"])).toEqual({ ios: "false", android: "false", boundaries: "true", ios_demos: "[]", android_demos: '["views"]' });
  });

  it("runs both demos of a platform when its demo-support module changes", () => {
    expect(outputsFor(["demos/ios-demo-support/Sources/TSDemoSupport/Catalog/DemoCatalog.swift"]).ios_demos).toBe('["swiftui","uikit"]');
    expect(outputsFor(["demos/android-demo-support/src/main/kotlin/com/ts/ds/demo/support/DemoCatalog.kt"]).android_demos).toBe('["compose","views"]');
  });

  it("runs everything for a design source, Makefile or tools dependency change", () => {
    for (const file of ["design/source/tokens.json", "Makefile", "tools/package-lock.json", "tools/ci/src/changes.ts"]) {
      const result = outputsFor([file]);
      expect(result.ios).toBe("true");
      expect(result.android).toBe("true");
      expect(result.ios_demos).toBe('["swiftui","uikit"]');
      expect(result.android_demos).toBe('["compose","views"]');
    }
  });

  it("runs a platform's jobs when its own workflow or toolchain pin changes", () => {
    expect(outputsFor([".github/workflows/ios.yml"])).toMatchObject({ ios: "true", android: "false", ios_demos: "[]" });
    expect(outputsFor([".xcode-version"])).toMatchObject({ ios: "true", android: "false", ios_demos: '["swiftui","uikit"]' });
    expect(outputsFor([".github/workflows/android.yml"])).toMatchObject({ ios: "false", android: "true", android_demos: "[]" });
    expect(outputsFor(["mise.toml"])).toMatchObject({ ios: "false", android: "true", android_demos: '["compose","views"]' });
    expect(outputsFor([".github/workflows/demos.yml"])).toEqual({ ios: "false", android: "false", boundaries: "true", ios_demos: '["swiftui","uikit"]', android_demos: '["compose","views"]' });
  });

  it("runs the boundary check when the checker changes", () => {
    expect(outputsFor(["tools/demos/src/check-boundaries.ts"])).toEqual({ ios: "false", android: "false", boundaries: "true", ios_demos: "[]", android_demos: "[]" });
  });

  it("runs every area when .gitattributes changes, since it decides line endings and binary handling everywhere", () => {
    for (const file of [".gitattributes"]) expect(outputsFor([file]), file).toEqual({ ios: "true", android: "true", boundaries: "true", ios_demos: '["swiftui","uikit"]', android_demos: '["compose","views"]' });
  });

  it("runs the demo catalog check when a component spec is added or removed", () => {
    expect(outputsFor(["design/specs/components/progress.md"]).boundaries).toBe("true");
  });

  it("runs nothing for a docs-only change", () => {
    expect(outputsFor(["docs/testing.md", "README.md", "CHANGELOG.md"])).toEqual({ ios: "false", android: "false", boundaries: "false", ios_demos: "[]", android_demos: "[]" });
  });

  it("formats the outputs as GITHUB_OUTPUT lines", () => {
    expect(formatOutputs({ ios: "true", ios_demos: "[]" })).toBe("ios=true\nios_demos=[]\n");
  });
});

describe("detectChanges", () => {
  let root: string;
  const git = (...args: string[]) => execFileSync("git", ["-C", root, ...args], { stdio: "pipe", encoding: "utf8" }).trim();

  async function commit(file: string, message: string): Promise<string> {
    await mkdir(path.dirname(path.join(root, file)), { recursive: true });
    await writeFile(path.join(root, file), `${message}\n`);
    git("add", file);
    git("-c", "user.name=ci", "-c", "user.email=ci@example.com", "commit", "-q", "-m", message);
    return git("rev-parse", "HEAD");
  }

  beforeEach(async () => {
    root = await mkdtemp(path.join(tmpdir(), "ci-changes-"));
    git("init", "-q");
  });

  afterEach(async () => {
    await rm(root, { recursive: true, force: true });
  });

  it("diffs the base against HEAD", async () => {
    const base = await commit("docs/readme.md", "base");
    await commit("ios/Sources/TSTokens/Colors.swift", "ios change");
    const changes = await detectChanges(root, base);
    expect(changes.files).toEqual(["ios/Sources/TSTokens/Colors.swift"]);
    expect(outputs(changes)).toMatchObject({ ios: "true", android: "false", android_demos: "[]" });
  });

  it("lists both sides of a rename", async () => {
    await commit("android/core/src/main/kotlin/A.kt", "base");
    const base = git("rev-parse", "HEAD");
    git("mv", "android/core/src/main/kotlin/A.kt", "a.md");
    git("-c", "user.name=ci", "-c", "user.email=ci@example.com", "commit", "-q", "-m", "move");
    const changes = await detectChanges(root, base);
    expect(changes.files).toEqual(["a.md", "android/core/src/main/kotlin/A.kt"]);
    expect(changes.areas.has("android")).toBe(true);
  });

  it("runs every area when there is no usable base", async () => {
    await commit("docs/readme.md", "only");
    for (const base of [undefined, "", "0000000000000000000000000000000000000000", "1234567890abcdef1234567890abcdef12345678"]) {
      const changes = await detectChanges(root, base);
      expect(changes.files).toBeUndefined();
      expect(outputs(changes)).toEqual({ ios: "true", android: "true", boundaries: "true", ios_demos: '["swiftui","uikit"]', android_demos: '["compose","views"]' });
    }
  });
});
