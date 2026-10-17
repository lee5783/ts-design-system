import { execFile } from "node:child_process";
import { mkdtemp, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { promisify } from "node:util";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import {
  ACKNOWLEDGED_BREAKAGES_FILE,
  EXPERIMENTAL_SPI_GROUP,
  MISSING_BASELINE_MESSAGE,
  type PackageDescription,
  baselineVersion,
  buildArguments,
  describeResolution,
  diagnoseArguments,
  dumpArguments,
  formatAcknowledgements,
  libraryModules,
  minimumIOS,
  modulesDirectory,
  parseAcknowledgements,
  parseBaselineArgument,
  parseBreakages,
  partitionBreakages,
  productBreakages,
  resolveBaseline,
  simulatorToolchain,
} from "../src/ios.ts";

const exec = promisify(execFile);

const description: PackageDescription = {
  platforms: [{ platformName: "ios", version: "15.0" }],
  products: [
    { name: "TSDesignSystem", type: { library: ["automatic"] }, targets: ["TSDesignSystem"] },
    { name: "TSDesignSystemUIKit", type: { library: ["automatic"] }, targets: ["TSDesignSystemUIKit"] },
    { name: "TSTokens", type: { library: ["automatic"] }, targets: ["TSTokens"] },
    { name: "Tool", type: { executable: null }, targets: ["Tool"] },
  ],
  targets: [
    { name: "TSTokens", type: "regular", dependencies: [] },
    { name: "TSFoundation", type: "regular", dependencies: [{ byName: ["TSTokens", null] }] },
    { name: "TSDesignSystem", type: "regular", dependencies: [{ byName: ["TSFoundation", null] }, { target: ["TSTokens", null] }] },
    { name: "TSDesignSystemUIKit", type: "regular", dependencies: [{ byName: ["TSFoundation", null] }] },
    { name: "TSTestSupport", type: "regular", dependencies: [{ byName: ["TSTokens", null] }] },
    { name: "TSDesignSystemTests", type: "test", dependencies: [{ byName: ["TSDesignSystem", null] }, { byName: ["TSTestSupport", null] }] },
    { name: "Tool", type: "executable", dependencies: [{ byName: ["TSTestSupport", null] }] },
  ],
};

describe("library modules", () => {
  it("covers every library product and the modules they re-export, without test support or tests", () => {
    expect(libraryModules(description)).toEqual(["TSDesignSystem", "TSDesignSystemUIKit", "TSFoundation", "TSTokens"]);
  });

  it("reads the minimum iOS version", () => {
    expect(minimumIOS(description)).toBe("15.0");
    expect(() => minimumIOS({ ...description, platforms: [] })).toThrow("no iOS platform");
  });
});

describe("digester invocation", () => {
  const sdk = "/Xcode/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator26.5.sdk";
  const toolchain = simulatorToolchain("arm64", "15.0", sdk);

  it("targets the iOS simulator at the package's minimum OS", () => {
    expect(toolchain).toEqual({
      arch: "arm64",
      triple: "arm64-apple-ios15.0-simulator",
      sdk,
      frameworks: "/Xcode/Platforms/iPhoneSimulator.platform/Developer/Library/Frameworks",
      libraries: "/Xcode/Platforms/iPhoneSimulator.platform/Developer/usr/lib",
    });
  });

  it("builds one module at a time for the simulator", () => {
    expect(buildArguments(toolchain, "/pkg", "/scratch", "TSTokens")).toEqual([
      "build", "-q", "--package-path", "/pkg", "--scratch-path", "/scratch", "--triple", "arm64-apple-ios15.0-simulator", "--sdk", sdk, "--target", "TSTokens",
    ]);
    expect(modulesDirectory("/scratch", toolchain)).toBe("/scratch/arm64-apple-ios-simulator/debug/Modules");
  });

  it("passes the simulator target to the dump, which swift package diagnose-api-breaking-changes omits", () => {
    const args = dumpArguments(toolchain, "/scratch/Modules", "TSTokens", "/out/TSTokens.json");
    expect(args.slice(0, 5)).toEqual(["-dump-sdk", "-module", "TSTokens", "-o", "/out/TSTokens.json"]);
    expect(args[args.indexOf("-target") + 1]).toBe("arm64-apple-ios15.0-simulator");
    expect(args[args.indexOf("-sdk") + 1]).toBe(sdk);
    expect(args).toContain("-abort-on-module-fail");
  });

  it("compares two dumps and ignores the Experimental SPI group", () => {
    expect(diagnoseArguments("/b.json", "/c.json", "/r.txt")).toEqual([
      "-diagnose-sdk", "-input-paths", "/b.json", "-input-paths", "/c.json", "-ignore-spi-group", EXPERIMENTAL_SPI_GROUP, "-o", "/r.txt",
    ]);
    expect(EXPERIMENTAL_SPI_GROUP).toBe("Experimental");
  });

  it("reads breakages from the report and ignores the section headers", () => {
    const clean = "\n/* Generic Signature Changes */\n\n/* Removed Decls */\n\n/* Others */\n";
    expect(parseBreakages(clean)).toEqual([]);
    const broken = "\n/* Removed Decls */\nAPI breakage: enum TSExperimentalProbe has been removed\n\n/* Type Changes */\nAPI breakage: func f(_:) has parameter 0 type change from Swift.Int to Swift.String\n";
    expect(parseBreakages(broken)).toEqual([
      "API breakage: enum TSExperimentalProbe has been removed",
      "API breakage: func f(_:) has parameter 0 type change from Swift.Int to Swift.String",
    ]);
  });

  it("reads the baseline argument", () => {
    expect(parseBaselineArgument(["--baseline", "1.0.0"])).toBe("1.0.0");
    expect(parseBaselineArgument([])).toBeUndefined();
  });
});

describe("baseline resolution", () => {
  let root: string;

  async function git(...args: string[]): Promise<string> {
    const { stdout } = await exec("git", ["-c", "user.name=t", "-c", "user.email=t@example.com", ...args], { cwd: root });
    return stdout.trim();
  }

  beforeEach(async () => {
    root = await mkdtemp(path.join(tmpdir(), "api-check-"));
    await git("init", "-q");
    await writeFile(path.join(root, "LICENSE"), "MIT\n");
    await git("add", "LICENSE");
    await git("commit", "-q", "-m", "initial");
    await git("tag", "pre-package");
    await writeFile(path.join(root, "Package.swift"), "// swift-tools-version: 6.0\n");
    await git("add", "Package.swift");
    await git("commit", "-q", "-m", "package");
    await git("tag", "1.0.0");
  });

  afterEach(async () => {
    await rm(root, { recursive: true, force: true });
  });

  it("fails clearly when no baseline is given", async () => {
    for (const ref of [undefined, "", "  "]) {
      expect(await resolveBaseline(root, ref)).toEqual({ kind: "missing" });
    }
    expect(describeResolution({ kind: "missing" })).toBe(MISSING_BASELINE_MESSAGE);
    expect(MISSING_BASELINE_MESSAGE).toContain("BASELINE is required");
    expect(MISSING_BASELINE_MESSAGE).toContain("No release tag exists");
  });

  it("rejects a ref that does not exist", async () => {
    const resolution = await resolveBaseline(root, "9.9.9");
    expect(resolution).toEqual({ kind: "unknown-ref", ref: "9.9.9" });
    expect(describeResolution(resolution)).toContain("'9.9.9' is not a git tag, branch or commit");
  });

  it("accepts a tag, a branch-relative ref and a commit", async () => {
    const commit = await git("rev-parse", "HEAD");
    for (const ref of ["1.0.0", "HEAD", commit, commit.slice(0, 10)]) {
      expect(await resolveBaseline(root, ref)).toEqual({ kind: "ready", ref, commit });
    }
  });

  it("reports a baseline that predates Package.swift as nothing to compare", async () => {
    const commit = await git("rev-parse", "pre-package");
    const resolution = await resolveBaseline(root, "pre-package");
    expect(resolution).toEqual({ kind: "no-package", ref: "pre-package", commit });
    expect(describeResolution(resolution)).toContain("has no Package.swift");
    expect(describeResolution(resolution)).toContain("skipping iOS");
  });
});

describe("acknowledged breakages", () => {
  const rename = "TSFoundation: Func TSBadgeCountFormatter.string(for:maximum:locale:) has been renamed to Func string(for:maximum:locale:grouping:)";
  const removal = "TSDesignSystem: Struct TSChip has been removed";

  it("lives next to the iOS sources, as Android keeps its api dumps in-tree", () => {
    expect(ACKNOWLEDGED_BREAKAGES_FILE).toBe("ios/api/breaking-changes.txt");
  });

  it("parses one '<baseline VERSION> <breakage>' entry per line and round-trips through the formatter", () => {
    const text = `0.1.0 ${rename}\n\n0.1.0 ${removal}\n`;
    const parsed = parseAcknowledgements(text);
    expect(parsed).toEqual([
      { since: "0.1.0", breakage: rename },
      { since: "0.1.0", breakage: removal },
    ]);
    expect(formatAcknowledgements(parsed)).toBe(`0.1.0 ${removal}\n0.1.0 ${rename}\n`);
    expect(formatAcknowledgements([])).toBe("");
  });

  it("rejects a malformed entry instead of ignoring it", () => {
    expect(() => parseAcknowledgements(`${rename}\n`)).toThrow(`${ACKNOWLEDGED_BREAKAGES_FILE}:1`);
    expect(() => parseAcknowledgements("v0.1.0 x\n")).toThrow("expected");
    expect(() => parseAcknowledgements("0.1.0\n")).toThrow("expected");
  });

  it("passes an intended break listed against the baseline's VERSION and fails any other", () => {
    const acknowledged = [{ since: "0.1.0", breakage: rename }];
    expect(partitionBreakages([rename], acknowledged, "0.1.0")).toEqual({ unacknowledged: [], acknowledged: [rename], stale: [] });
    expect(partitionBreakages([rename, removal], acknowledged, "0.1.0")).toEqual({ unacknowledged: [removal], acknowledged: [rename], stale: [] });
  });

  it("ignores entries made against an earlier release once a newer baseline is compared, so they never mask a new break", () => {
    const acknowledged = [{ since: "0.1.0", breakage: rename }];
    expect(partitionBreakages([rename], acknowledged, "0.2.0")).toEqual({ unacknowledged: [rename], acknowledged: [], stale: acknowledged });
    expect(partitionBreakages([rename], acknowledged, undefined).unacknowledged).toEqual([rename]);
  });

  it("reports entries that no longer match a breakage as stale, without failing", () => {
    const acknowledged = [{ since: "0.1.0", breakage: removal }];
    expect(partitionBreakages([], acknowledged, "0.1.0")).toEqual({ unacknowledged: [], acknowledged: [], stale: acknowledged });
  });
});

describe("product breakages", () => {
  const withProducts = (products: PackageDescription["products"]): PackageDescription => ({ ...description, products });

  it("finds nothing when every library product keeps its name and modules", () => {
    expect(productBreakages(description, description)).toEqual([]);
  });

  it("reports a removed or renamed library product, which breaks .product(name:) without any digester line", () => {
    const renamed = withProducts(description.products.map((product) => (product.name === "TSDesignSystemUIKit" ? { ...product, name: "TSUIKit" } : product)));
    expect(productBreakages(description, renamed)).toEqual(["Package: library product TSDesignSystemUIKit has been removed or renamed"]);
  });

  it("reports a module dropped from a product", () => {
    const baseline = withProducts([{ name: "TSDesignSystem", type: { library: ["automatic"] }, targets: ["TSDesignSystem", "TSTokens"] }]);
    const current = withProducts([{ name: "TSDesignSystem", type: { library: ["automatic"] }, targets: ["TSDesignSystem"] }]);
    expect(productBreakages(baseline, current)).toEqual(["Package: library product TSDesignSystem no longer contains module TSTokens"]);
  });

  it("ignores added products and non-library products", () => {
    const current = withProducts([...description.products.filter((product) => product.name !== "Tool"), { name: "TSCharts", type: { library: ["automatic"] }, targets: ["TSCharts"] }]);
    expect(productBreakages(description, current)).toEqual([]);
  });
});

describe("baselineVersion", () => {
  let repo: string;
  const git = (...args: string[]) => exec("git", ["-c", "user.name=t", "-c", "user.email=t@example.com", ...args], { cwd: repo });

  beforeEach(async () => {
    repo = await mkdtemp(path.join(tmpdir(), "api-check-version-"));
    await git("init", "-q");
  });

  afterEach(async () => {
    await rm(repo, { recursive: true, force: true });
  });

  it("reads VERSION at the baseline commit, not in the working tree", async () => {
    await writeFile(path.join(repo, "VERSION"), "0.1.0\n");
    await git("add", "VERSION");
    await git("commit", "-q", "-m", "first");
    const { stdout } = await git("rev-parse", "HEAD");
    await writeFile(path.join(repo, "VERSION"), "0.2.0\n");
    expect(await baselineVersion(repo, stdout.trim())).toBe("0.1.0");
  });

  it("is undefined when the baseline has no VERSION", async () => {
    await git("commit", "-q", "--allow-empty", "-m", "first");
    const { stdout } = await git("rev-parse", "HEAD");
    expect(await baselineVersion(repo, stdout.trim())).toBeUndefined();
  });
});
