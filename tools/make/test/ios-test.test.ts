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
  const { stdout } = await run("make", ["-n", "-s", "ios-test", `XCRUN=${xcrun}`, "IOS_RUNTIMES=pinned oldest", ...variables], { cwd: repoRoot });
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

describe("pinned simulator", () => {
  function pinFile(source: string, name: string): string {
    const match = new RegExp(`^${name}=(.+)$`, "m").exec(source);
    if (!match?.[1]) throw new Error(`.ios-simulator has no ${name}`);
    return match[1];
  }

  it("defaults IOS_SIM_DEVICE and IOS_SIM_OS to the simulator pinned in .ios-simulator", async () => {
    const pin = await readFile(path.join(repoRoot, ".ios-simulator"), "utf8");
    const output = await dryRun("false", "IOS_RUNTIMES=pinned");
    expect(output).toContain(`-destination 'platform=iOS Simulator,name=${pinFile(pin, "device")},OS=${pinFile(pin, "os")}'`);
  });

  it("follows a bump of the pin file with no Makefile edit", async () => {
    const bumped = path.join(dir, ".ios-simulator");
    await writeFile(bumped, "device=iPhone 18\nos=27.0\n");
    const output = await dryRun("false", "IOS_RUNTIMES=pinned", `IOS_SIMULATOR_FILE=${bumped}`);
    expect(output).toContain("-destination 'platform=iOS Simulator,name=iPhone 18,OS=27.0'");
  });
});

describe("make ios-build-for-testing and ios-test-only", () => {
  it("builds the tests once for the generic simulator, before any test run", async () => {
    const output = await dryRun(await fakeXcrun(RUNTIMES, DEVICES_WITH_OLD_IPHONE));
    expect(output.match(/xcodebuild build-for-testing/g)).toHaveLength(1);
    expect(output).toContain("xcodebuild build-for-testing -scheme TSDesignSystem-Package -destination 'generic/platform=iOS Simulator'");
    expect(output.indexOf("build-for-testing")).toBeLessThan(output.indexOf("test-without-building"));
    expect(output.match(/xcodebuild test-without-building/g)).toHaveLength(2);
  });

  it("runs the selected tests without a build or the golden type-check", async () => {
    const { stdout } = await run("make", ["-n", "-s", "ios-test-only", `XCRUN=${await fakeXcrun(RUNTIMES, DEVICES_WITH_OLD_IPHONE)}`, "IOS_RUNTIMES=pinned", "IOS_TESTS=TSFoundationTests/TSToastQueueTests TSTokensTests"], { cwd: repoRoot });
    expect(stdout).not.toContain("build-for-testing");
    expect(stdout).not.toContain("swiftc");
    expect(stdout).toContain("-only-testing:TSFoundationTests/TSToastQueueTests -only-testing:TSTokensTests");
  });

  it("defaults the local run to the pinned runtime", async () => {
    const { stdout } = await run("make", ["-n", "-s", "ios-test", `XCRUN=${await fakeXcrun(RUNTIMES, DEVICES_WITH_OLD_IPHONE)}`], { cwd: repoRoot });
    expect(stdout.match(/xcodebuild test-without-building/g)).toHaveLength(1);
    expect(stdout).not.toContain("OS=15.5");
  });
});
