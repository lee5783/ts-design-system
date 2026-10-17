import { execFile } from "node:child_process";
import { chmod, mkdtemp, readFile, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { afterEach, beforeEach, describe, expect, it } from "vitest";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const run = promisify(execFile);

const RUNTIMES = ["== Runtimes ==", "iOS 15.5 (15.5 - 19F70) - com.apple.CoreSimulator.SimRuntime.iOS-15-5", "iOS 26.5 (26.5 - 23F77) - com.apple.CoreSimulator.SimRuntime.iOS-26-5"];

const DEVICES_WITH_OLD_IPHONE = [
  "== Devices ==",
  "-- iOS 15.5 --",
  "    iPad (9th generation) (11111111-1111-1111-1111-111111111111) (Shutdown) ",
  "    iPhone SE (3rd generation) (22222222-2222-2222-2222-222222222222) (Shutdown) ",
  "    iPhone 13 (33333333-3333-3333-3333-333333333333) (Shutdown) ",
  "-- iOS 26.5 --",
  "    iPhone 17 (44444444-4444-4444-4444-444444444444) (Shutdown) ",
];

const DEVICES_WITHOUT_OLD_IPHONE = [
  "== Devices ==",
  "-- iOS 15.5 --",
  "    iPad (9th generation) (11111111-1111-1111-1111-111111111111) (Shutdown) ",
  "-- iOS 26.5 --",
  "    iPhone 17 (44444444-4444-4444-4444-444444444444) (Shutdown) ",
];

let dir: string;

async function fakeXcrun(runtimes: string[], devices: string[]): Promise<string> {
  const script = path.join(dir, "xcrun");
  const body = [
    "#!/bin/sh",
    'case "$*" in',
    `  "simctl list runtimes available") printf '%s\\n' ${runtimes.map((line) => `'${line}'`).join(" ")} ;;`,
    `  "simctl list devices available") printf '%s\\n' ${devices.map((line) => `'${line}'`).join(" ")} ;;`,
    "  *) exit 1 ;;",
    "esac",
  ];
  await writeFile(script, `${body.join("\n")}\n`);
  await chmod(script, 0o755);
  return script;
}

async function dryRun(xcrun: string, ...variables: string[]): Promise<string> {
  const { stdout } = await run("make", ["-n", "-s", "ios-test", `XCRUN=${xcrun}`, ...variables], { cwd: repoRoot });
  return stdout;
}

beforeEach(async () => {
  dir = await mkdtemp(path.join(tmpdir(), "make-ios-test-"));
});

afterEach(async () => {
  await rm(dir, { recursive: true, force: true });
});

describe("make ios-test destinations", () => {
  it("runs the oldest runtime on an iPhone that runtime hosts", async () => {
    const output = await dryRun(await fakeXcrun(RUNTIMES, DEVICES_WITH_OLD_IPHONE));
    expect(output).toContain("platform=iOS Simulator,name=iPhone 17,OS=26.5");
    expect(output).toContain("platform=iOS Simulator,name=iPhone SE (3rd generation),OS=15.5");
    expect(output).not.toContain("name=iPhone 17,OS=15.5");
  });

  it("skips the oldest runtime with a clear message when it has no iPhone", async () => {
    const output = await dryRun(await fakeXcrun(RUNTIMES, DEVICES_WITHOUT_OLD_IPHONE));
    expect(output).toContain("platform=iOS Simulator,name=iPhone 17,OS=26.5");
    expect(output).not.toContain("OS=15.5 ");
    expect(output).toContain("skipping iOS 15.5: no available iPhone simulator for that runtime");
  });

  it("honours an explicit IOS_OLDEST_DEVICE", async () => {
    const output = await dryRun(await fakeXcrun(RUNTIMES, DEVICES_WITH_OLD_IPHONE), "IOS_OLDEST_DEVICE=iPhone 13");
    expect(output).toContain("platform=iOS Simulator,name=iPhone 13,OS=15.5");
  });

  it("runs once when the default runtime is the oldest", async () => {
    const output = await dryRun(await fakeXcrun([RUNTIMES[0] ?? "", RUNTIMES[2] ?? ""], DEVICES_WITH_OLD_IPHONE));
    expect(output.match(/xcodebuild test/g)).toHaveLength(1);
    expect(output).toContain("name=iPhone 17,OS=26.5");
  });
});

describe("pinned snapshot simulator", () => {
  const deviceSource = path.join(repoRoot, "ios/Tests/TSTestSupport/Snapshots/TSSnapshotDevice.swift");

  function swiftConstant(source: string, name: string): string {
    const match = new RegExp(`public static let ${name} = "([^"]+)"`).exec(source);
    if (!match?.[1]) throw new Error(`TSSnapshotDevice.swift has no ${name}`);
    return match[1];
  }

  it("defaults IOS_SIM_DEVICE and IOS_SIM_OS to the constants the snapshot suites compare against", async () => {
    const source = await readFile(deviceSource, "utf8");
    const output = await dryRun("false", "IOS_RUNTIMES=pinned");
    expect(output).toContain(`-destination 'platform=iOS Simulator,name=${swiftConstant(source, "pinnedSimulator")},OS=${swiftConstant(source, "pinnedOSVersion")}'`);
  });

  it("follows a bump of the Swift constants with no Makefile edit", async () => {
    const bumped = path.join(dir, "TSSnapshotDevice.swift");
    const source = await readFile(deviceSource, "utf8");
    await writeFile(bumped, source.replace(/pinnedSimulator = "[^"]+"/, 'pinnedSimulator = "iPhone 18"').replace(/pinnedOSVersion = "[^"]+"/, 'pinnedOSVersion = "27.0"'));
    const output = await dryRun("false", "IOS_RUNTIMES=pinned", `IOS_SNAPSHOT_DEVICE_SWIFT=${bumped}`);
    expect(output).toContain("-destination 'platform=iOS Simulator,name=iPhone 18,OS=27.0'");
  });

  it("leaves every snapshot key unset in ios-test, so the snapshot suites skip on both legs", async () => {
    const source = await readFile(deviceSource, "utf8");
    const output = await dryRun(await fakeXcrun(RUNTIMES, DEVICES_WITH_OLD_IPHONE));
    expect(output.match(/xcodebuild test/g)).toHaveLength(2);
    for (const name of ["verifyEnvironmentKey", "recordEnvironmentKey", "requirePinnedEnvironmentKey"]) expect(output).not.toContain(swiftConstant(source, name));
  });

  it("verifies the snapshot targets once, on the pinned simulator, under the keys the Swift guard reads", async () => {
    const source = await readFile(deviceSource, "utf8");
    const { stdout } = await run("make", ["-n", "-s", "ios-snapshot-verify", `XCRUN=${await fakeXcrun(RUNTIMES, DEVICES_WITH_OLD_IPHONE)}`], { cwd: repoRoot });
    expect(stdout.match(/xcodebuild test/g)).toHaveLength(1);
    expect(stdout).toContain(`TEST_RUNNER_${swiftConstant(source, "verifyEnvironmentKey")}=1 TEST_RUNNER_${swiftConstant(source, "requirePinnedEnvironmentKey")}=1 xcodebuild test`);
    expect(stdout).toContain(`-destination 'platform=iOS Simulator,name=${swiftConstant(source, "pinnedSimulator")},OS=${swiftConstant(source, "pinnedOSVersion")}'`);
    expect(stdout).toContain("-only-testing:TSDesignSystemTests -only-testing:TSDesignSystemUIKitTests");
  });
});
