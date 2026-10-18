import { execFile } from "node:child_process";
import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { describe, expect, it } from "vitest";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const run = promisify(execFile);

const BOTH = "IOS_RUNTIMES=pinned oldest";
const OLDEST = ["IOS_OLDEST_OS=18.6", "IOS_OLDEST_DEVICE=iPhone 16"];
const NO_OLDER_RUNTIME = ["IOS_OLDEST_OS=26.5", "IOS_OLDEST_DEVICE=iPhone 17"];
const NO_OLD_IPHONE = ["IOS_OLDEST_OS=18.6", "IOS_OLDEST_DEVICE="];

async function dryRun(target: string, ...variables: string[]): Promise<string> {
  const { stdout } = await run("make", ["-n", "-s", target, "XCRUN=false", ...variables], { cwd: repoRoot, env: { ...process.env, GITHUB_ACTIONS: "" } });
  return stdout;
}

async function runOldestLeg(...variables: string[]): Promise<{ code: number; output: string }> {
  try {
    const { stdout, stderr } = await run("make", ["-s", "-o", "ios-golden-typecheck", "-o", "ios-build-for-testing", "ios-test", "XCRUN=false", "IOS_RUNTIMES=oldest", ...variables], { cwd: repoRoot, env: { ...process.env, GITHUB_ACTIONS: "" } });
    return { code: 0, output: stdout + stderr };
  } catch (error) {
    const failure = error as { code: number; stdout: string; stderr: string };
    return { code: failure.code, output: failure.stdout + failure.stderr };
  }
}

function destinations(output: string): string[] {
  return [...output.matchAll(/-destination 'platform=iOS Simulator,name=([^,]+),OS=([0-9.]+)'/g)].map((match) => `${match[1]} / ${match[2]}`);
}

describe("IOS_RUNTIMES", () => {
  it("runs the pinned and oldest runtimes by default", async () => {
    expect(destinations(await dryRun("ios-test", BOTH, ...OLDEST))).toEqual(["iPhone 17 / 26.5", "iPhone 16 / 18.6"]);
  });

  it("runs only the pinned runtime for IOS_RUNTIMES=pinned", async () => {
    expect(destinations(await dryRun("ios-test", "IOS_RUNTIMES=pinned", ...OLDEST))).toEqual(["iPhone 17 / 26.5"]);
    expect(destinations(await dryRun("demo-ios-swiftui-test", "IOS_RUNTIMES=pinned", ...OLDEST))).toEqual(["iPhone 17 / 26.5"]);
    expect(destinations(await dryRun("demo-ios-support-test", "IOS_RUNTIMES=pinned", ...OLDEST))).toEqual(["iPhone 17 / 26.5"]);
  });

  it("runs only the oldest runtime for IOS_RUNTIMES=oldest", async () => {
    expect(destinations(await dryRun("ios-test", "IOS_RUNTIMES=oldest", ...OLDEST))).toEqual(["iPhone 16 / 18.6"]);
    expect(destinations(await dryRun("demo-ios-uikit-test", "IOS_RUNTIMES=oldest", ...OLDEST))).toEqual(["iPhone 16 / 18.6"]);
  });

  it("says so when no runtime is older than the pinned one", async () => {
    const output = await dryRun("ios-test", "IOS_RUNTIMES=oldest", ...NO_OLDER_RUNTIME);
    expect(destinations(output)).toEqual([]);
    expect(output).toContain("no iOS runtime older than the pinned iOS 26.5 is available");
    expect(output).not.toContain("exit 1");
  });

  it("fails when IOS_REQUIRE_OLDEST is set and no runtime is older than the pinned one, and only warns without it", async () => {
    const required = await runOldestLeg("IOS_REQUIRE_OLDEST=1", ...NO_OLDER_RUNTIME);
    expect(required.code).not.toBe(0);
    expect(required.output).toContain("ios-test: no iOS runtime older than the pinned iOS 26.5 is available but IOS_REQUIRE_OLDEST=1 requires one");
    const soft = await runOldestLeg(...NO_OLDER_RUNTIME);
    expect(soft.code).toBe(0);
    expect(soft.output).toContain("no iOS runtime older than the pinned iOS 26.5 is available: the oldest-runtime run has nothing to test");
    expect(await dryRun("ios-test", BOTH, "IOS_REQUIRE_OLDEST=1", ...NO_OLDER_RUNTIME)).toMatch(/IOS_REQUIRE_OLDEST=1 requires one.*>&2 && exit 1/);
  });

  it("annotates the missing older runtime as an error only with IOS_REQUIRE_OLDEST under GitHub Actions", async () => {
    expect((await runOldestLeg("IOS_REQUIRE_OLDEST=1", "GITHUB_ACTIONS=true", ...NO_OLDER_RUNTIME)).output).toContain("::error::ios-test: no iOS runtime older than the pinned iOS 26.5");
    expect((await runOldestLeg("GITHUB_ACTIONS=true", ...NO_OLDER_RUNTIME)).output).toContain("::warning::ios-test: no iOS runtime older than the pinned iOS 26.5");
  });

  it("makes a missing oldest-runtime iPhone fatal only with IOS_REQUIRE_OLDEST", async () => {
    const soft = await dryRun("ios-test", "IOS_RUNTIMES=oldest", ...NO_OLD_IPHONE);
    expect(soft).toContain("skipping iOS 18.6: no available iPhone simulator for that runtime");
    expect(soft).not.toContain("exit 1");
    const fatal = await dryRun("ios-test", "IOS_RUNTIMES=oldest", "IOS_REQUIRE_OLDEST=1", ...NO_OLD_IPHONE);
    expect(fatal).toMatch(/skipping iOS 18\.6: no available iPhone simulator for that runtime.*>&2 && exit 1/);
  });

  it("annotates the skip for GitHub Actions", async () => {
    const { stdout } = await run("make", ["-n", "-s", "ios-test", "XCRUN=false", "IOS_RUNTIMES=oldest", "IOS_REQUIRE_OLDEST=1", ...NO_OLD_IPHONE], { cwd: repoRoot, env: { ...process.env, GITHUB_ACTIONS: "true" } });
    expect(stdout).toContain("printf '::error::ios-test: skipping iOS 18.6");
  });
});

describe("GRADLE_WORKERS", () => {
  const targets = ["android-build", "android-test", "android-api-check", "demo-android-compose", "demo-android-views", "demo-android-compose-test", "demo-android-views-test"];

  it("caps the Gradle workers through org.gradle.workers.max", async () => {
    for (const target of targets) {
      const output = await dryRun(target, "GRADLE_WORKERS=2");
      expect(output, target).toMatch(/\.\/gradlew [^\n]* -Dorg\.gradle\.workers\.max=2\n/);
    }
  });

  it("adds nothing when unset", async () => {
    for (const target of targets) {
      expect(await dryRun(target, "GRADLE_WORKERS="), target).not.toContain("org.gradle.workers.max");
    }
  });
});

describe("make test", () => {
  it("runs every local gate, the platform suites last, and says so in the help", async () => {
    const output = await dryRun("test", "IOS_RUNTIMES=pinned");
    const order = ["run --silent typecheck", "codegen -- all --check", "run --silent ds-sync", "run --silent demos-check-catalog", "xcodebuild build-for-testing -scheme TSDesignSystem-Package", "xcodebuild test-without-building -scheme TSDesignSystem-Package", "./gradlew test"].map((command) => output.indexOf(command));
    expect(order.every((position) => position >= 0)).toBe(true);
    expect(order).toEqual([...order].sort((left, right) => left - right));
    const { stdout } = await run("make", ["-s", "help"], { cwd: repoRoot });
    expect(stdout).toMatch(/test +Run tools-test, codegen-check, ds-sync, demos-check-boundaries, ios-test and android-test\n/);
  });
});

describe("CI helper targets", () => {
  it("computes the changed areas from BASE, read by the shell", async () => {
    expect(await dryRun("ci-changes", "BASE=abc123")).toContain('npm --prefix tools run --silent ci-changes -- ${BASE:+--base "$BASE"}\n');
  });

  it("selects the Xcode named in .xcode-version through GITHUB_ENV", async () => {
    const version = (await readFile(path.join(repoRoot, ".xcode-version"), "utf8")).trim();
    const output = await dryRun("ci-xcode", `CI_XCODE_APP=/Applications/Xcode_${version}.app`);
    expect(output).toContain(`printf 'DEVELOPER_DIR=%s/Contents/Developer\\n' '/Applications/Xcode_${version}.app' >> "\${GITHUB_ENV:?`);
    expect(output).toContain(`DEVELOPER_DIR='/Applications/Xcode_${version}.app/Contents/Developer' xcodebuild -version`);
    expect(await dryRun("ci-xcode", "CI_XCODE_APP=")).toContain(`ci-xcode: Xcode %s is not in /Applications`);
  });

  it("turns the hardware keyboard off and checks the pinned simulator only when it is used", async () => {
    const pinned = await dryRun("ci-ios-simulator", "IOS_RUNTIMES=pinned");
    expect(pinned).toContain("defaults write com.apple.iphonesimulator ConnectHardwareKeyboard -bool false");
    expect(pinned).toContain("if [ -n 'pinned' ]");
    expect(pinned).toContain("grep -qF '    iPhone 17 ('");
    expect(await dryRun("ci-ios-simulator", "IOS_RUNTIMES=oldest")).toContain("if [ -n '' ]");
  });
});

describe("simulator appearance", () => {
  it("boots the pinned simulator and sets its appearance only when IOS_SIM_APPEARANCE is set", async () => {
    const dark = await dryRun("ci-ios-simulator", "IOS_RUNTIMES=pinned", "IOS_SIM_APPEARANCE=dark");
    expect(dark).toContain("false simctl boot \"$udid\"");
    expect(dark).toContain("false simctl bootstatus \"$udid\" -b");
    expect(dark).toContain("false simctl ui \"$udid\" appearance 'dark'");
    expect(dark).toContain("/^-- iOS 26.5 --$/,/^-- /s/^    iPhone 17 [(]([0-9A-F-]{36})[)].*$/\\1/p");
    expect(dark.indexOf("ConnectHardwareKeyboard")).toBeLessThan(dark.indexOf("simctl boot"));
    expect(await dryRun("ci-ios-simulator", "IOS_RUNTIMES=pinned", "IOS_SIM_APPEARANCE=")).not.toContain("appearance");
    expect(await dryRun("ci-ios-simulator", "IOS_RUNTIMES=oldest", "IOS_SIM_APPEARANCE=dark")).not.toContain("simctl ui");
  });

  it("refuses an appearance other than light or dark", async () => {
    expect(await dryRun("ci-ios-simulator", "IOS_SIM_APPEARANCE=blue")).toContain("IOS_SIM_APPEARANCE must be light or dark; got %s\\n' 'blue' >&2; exit 1");
  });

  it("runs only ThemeOverrideTests on the pinned runtime in the theme targets", async () => {
    for (const [demo, scheme] of [["swiftui", "TSDemoSwiftUI"], ["uikit", "TSDemoUIKit"]]) {
      const output = await dryRun(`demo-ios-${demo}-theme-test`, ...OLDEST);
      expect(output, demo).toContain(`-scheme ${scheme} -destination 'platform=iOS Simulator,name=iPhone 17,OS=26.5' -derivedDataPath .build/xcode-demos -only-testing:${scheme}UITests/ThemeOverrideTests`);
      expect(destinations(output), demo).toEqual(["iPhone 17 / 26.5"]);
    }
  });
});
