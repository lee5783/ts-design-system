import { execFile, spawn } from "node:child_process";
import { mkdir, readFile, rm, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { promisify } from "node:util";

const exec = promisify(execFile);

export const MISSING_BASELINE_MESSAGE = [
  "api-check: BASELINE is required. Pass the release tag or any git ref or commit to compare against:",
  "  make api-check BASELINE=1.2.0",
  "  make api-check BASELINE=HEAD",
  "No release tag exists until the first release; use a branch or commit until then.",
].join("\n");

export const SCRATCH_PATH = ".build/api-check";
export const ACKNOWLEDGED_BREAKAGES_FILE = "ios/api/breaking-changes.txt";
export const PACKAGE_SCOPE = "Package";
export const EXPERIMENTAL_SPI_GROUP = "Experimental";

export interface PackageTargetDependency {
  byName?: [string, unknown];
  target?: [string, unknown];
}

export interface PackageDescription {
  platforms?: { platformName: string; version: string }[];
  products: { name: string; type: Record<string, unknown>; targets: string[] }[];
  targets: { name: string; type: string; dependencies?: PackageTargetDependency[] }[];
}

export interface SimulatorToolchain {
  arch: string;
  triple: string;
  sdk: string;
  frameworks: string;
  libraries: string;
}

export type BaselineResolution =
  | { kind: "missing" }
  | { kind: "unknown-ref"; ref: string }
  | { kind: "no-package"; ref: string; commit: string }
  | { kind: "ready"; ref: string; commit: string };

export interface Acknowledgement {
  since: string;
  breakage: string;
}

export interface BreakageReport {
  unacknowledged: string[];
  acknowledged: string[];
  stale: Acknowledgement[];
}

const RELEASE_VERSION = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$/;

export function parseAcknowledgements(text: string): Acknowledgement[] {
  return text
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line !== "")
    .map((line, index) => {
      const space = line.indexOf(" ");
      const since = space < 0 ? line : line.slice(0, space);
      const breakage = space < 0 ? "" : line.slice(space + 1).trim();
      if (!RELEASE_VERSION.test(since) || breakage === "") throw new Error(`${ACKNOWLEDGED_BREAKAGES_FILE}:${index + 1}: expected "<baseline VERSION> <breakage line>", got "${line}"`);
      return { since, breakage };
    });
}

export function formatAcknowledgements(acknowledgements: Acknowledgement[]): string {
  const lines = [...new Set(acknowledgements.map(({ since, breakage }) => `${since} ${breakage}`))].sort();
  return lines.length === 0 ? "" : `${lines.join("\n")}\n`;
}

export function partitionBreakages(breakages: string[], acknowledgements: Acknowledgement[], baselineVersion: string | undefined): BreakageReport {
  const applicable = new Set(acknowledgements.filter(({ since }) => since === baselineVersion).map(({ breakage }) => breakage));
  const reported = new Set(breakages);
  return {
    unacknowledged: breakages.filter((breakage) => !applicable.has(breakage)),
    acknowledged: breakages.filter((breakage) => applicable.has(breakage)),
    stale: acknowledgements.filter(({ since, breakage }) => since !== baselineVersion || !reported.has(breakage)),
  };
}

function libraryProducts(description: PackageDescription): Map<string, string[]> {
  return new Map(description.products.filter((product) => "library" in product.type).map((product) => [product.name, product.targets]));
}

export function productBreakages(baseline: PackageDescription, current: PackageDescription): string[] {
  const currentProducts = libraryProducts(current);
  const breakages: string[] = [];
  for (const [name, targets] of [...libraryProducts(baseline)].sort(([left], [right]) => left.localeCompare(right))) {
    const now = currentProducts.get(name);
    if (now === undefined) {
      breakages.push(`${PACKAGE_SCOPE}: library product ${name} has been removed or renamed`);
      continue;
    }
    for (const target of targets.filter((candidate) => !now.includes(candidate)).sort()) breakages.push(`${PACKAGE_SCOPE}: library product ${name} no longer contains module ${target}`);
  }
  return breakages;
}

export function libraryModules(description: PackageDescription): string[] {
  const targets = new Map(description.targets.map((target) => [target.name, target]));
  const modules = new Set<string>();
  const visit = (name: string): void => {
    const target = targets.get(name);
    if (target === undefined || target.type !== "regular" || modules.has(name)) return;
    modules.add(name);
    for (const dependency of target.dependencies ?? []) {
      const dependencyName = dependency.byName?.[0] ?? dependency.target?.[0];
      if (dependencyName !== undefined) visit(dependencyName);
    }
  };
  for (const product of description.products) {
    if (!("library" in product.type)) continue;
    for (const name of product.targets) visit(name);
  }
  return [...modules].sort();
}

export function minimumIOS(description: PackageDescription): string {
  const ios = description.platforms?.find((platform) => platform.platformName === "ios");
  if (ios === undefined) throw new Error("Package.swift declares no iOS platform");
  return ios.version;
}

export function simulatorToolchain(arch: string, minimumIOSVersion: string, sdk: string): SimulatorToolchain {
  const platform = path.dirname(path.dirname(sdk));
  return {
    arch,
    triple: `${arch}-apple-ios${minimumIOSVersion}-simulator`,
    sdk,
    frameworks: path.join(platform, "Library", "Frameworks"),
    libraries: path.join(platform, "usr", "lib"),
  };
}

export function buildArguments(toolchain: SimulatorToolchain, packagePath: string, scratchPath: string, module: string): string[] {
  return ["build", "-q", "--package-path", packagePath, "--scratch-path", scratchPath, "--triple", toolchain.triple, "--sdk", toolchain.sdk, "--target", module];
}

export function modulesDirectory(scratchPath: string, toolchain: SimulatorToolchain): string {
  return path.join(scratchPath, `${toolchain.arch}-apple-ios-simulator`, "debug", "Modules");
}

export function dumpArguments(toolchain: SimulatorToolchain, modules: string, module: string, output: string): string[] {
  return [
    "-dump-sdk",
    "-module",
    module,
    "-o",
    output,
    "-target",
    toolchain.triple,
    "-sdk",
    toolchain.sdk,
    "-I",
    modules,
    "-F",
    toolchain.frameworks,
    "-I",
    toolchain.libraries,
    "-abort-on-module-fail",
    "-avoid-location",
  ];
}

export function diagnoseArguments(baseline: string, current: string, output: string): string[] {
  return ["-diagnose-sdk", "-input-paths", baseline, "-input-paths", current, "-ignore-spi-group", EXPERIMENTAL_SPI_GROUP, "-o", output];
}

export function parseBreakages(report: string): string[] {
  return report
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line !== "" && !(line.startsWith("/*") && line.endsWith("*/")));
}

export async function resolveBaseline(root: string, ref: string | undefined, git: string = "git"): Promise<BaselineResolution> {
  if (ref === undefined || ref.trim() === "") return { kind: "missing" };
  let commit: string;
  try {
    const { stdout } = await exec(git, ["rev-parse", "--verify", "--quiet", `${ref}^{commit}`], { cwd: root });
    commit = stdout.trim();
  } catch {
    return { kind: "unknown-ref", ref };
  }
  try {
    await exec(git, ["cat-file", "-e", `${commit}:Package.swift`], { cwd: root });
  } catch {
    return { kind: "no-package", ref, commit };
  }
  return { kind: "ready", ref, commit };
}

export function describeResolution(resolution: BaselineResolution): string {
  switch (resolution.kind) {
    case "missing":
      return MISSING_BASELINE_MESSAGE;
    case "unknown-ref":
      return `api-check: BASELINE '${resolution.ref}' is not a git tag, branch or commit in this repository.`;
    case "no-package":
      return `api-check: iOS: '${resolution.ref}' (${resolution.commit.slice(0, 12)}) has no Package.swift, so there is no published Swift API to break yet. Nothing to compare; skipping iOS.`;
    case "ready":
      return `api-check: iOS: comparing against '${resolution.ref}' (${resolution.commit.slice(0, 12)})`;
  }
}

export function parseBaselineArgument(argv: string[]): string | undefined {
  const index = argv.indexOf("--baseline");
  return index === -1 ? undefined : argv[index + 1];
}

export async function baselineVersion(root: string, commit: string, git: string = "git"): Promise<string | undefined> {
  try {
    const { stdout } = await exec(git, ["show", `${commit}:VERSION`], { cwd: root });
    const version = stdout.trim();
    return RELEASE_VERSION.test(version) ? version : undefined;
  } catch {
    return undefined;
  }
}

async function readAcknowledgements(root: string): Promise<Acknowledgement[]> {
  try {
    return parseAcknowledgements(await readFile(path.join(root, ACKNOWLEDGED_BREAKAGES_FILE), "utf8"));
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === "ENOENT") return [];
    throw error;
  }
}

async function run(command: string, args: string[], cwd: string): Promise<void> {
  const code = await new Promise<number>((resolve, reject) => {
    const child = spawn(command, args, { cwd, stdio: "inherit" });
    child.on("error", reject);
    child.on("close", (status) => resolve(status ?? 1));
  });
  if (code !== 0) throw new Error(`${path.basename(command)} ${args.slice(0, 2).join(" ")} failed with exit ${code}`);
}

async function describePackage(packagePath: string): Promise<PackageDescription> {
  const { stdout } = await exec("swift", ["package", "--package-path", packagePath, "dump-package"], { maxBuffer: 16 * 1024 * 1024 });
  return JSON.parse(stdout) as PackageDescription;
}

async function extractBaseline(root: string, commit: string, destination: string): Promise<void> {
  await rm(destination, { recursive: true, force: true });
  await mkdir(destination, { recursive: true });
  await new Promise<void>((resolve, reject) => {
    const archive = spawn("git", ["archive", commit], { cwd: root, stdio: ["ignore", "pipe", "inherit"] });
    const untar = spawn("tar", ["-x", "-C", destination], { stdio: ["pipe", "inherit", "inherit"] });
    archive.stdout.pipe(untar.stdin);
    archive.on("error", reject);
    untar.on("error", reject);
    untar.on("close", (code) => (code === 0 ? resolve() : reject(new Error(`git archive ${commit} could not be extracted`))));
  });
}

async function dumpModules(digester: string, toolchain: SimulatorToolchain, packagePath: string, scratchPath: string, modules: string[], output: string): Promise<Map<string, string | undefined>> {
  await mkdir(output, { recursive: true });
  const dumps = new Map<string, string | undefined>();
  for (const module of modules) {
    try {
      await run("swift", buildArguments(toolchain, packagePath, scratchPath, module), packagePath);
      const dump = path.join(output, `${module}.json`);
      await run(digester, dumpArguments(toolchain, modulesDirectory(scratchPath, toolchain), module, dump), packagePath);
      dumps.set(module, dump);
    } catch {
      dumps.set(module, undefined);
    }
  }
  return dumps;
}

async function main(root: string): Promise<number> {
  const argv = process.argv.slice(2);
  const record = argv.includes("--record");
  const resolution = await resolveBaseline(root, parseBaselineArgument(argv));
  if (resolution.kind === "missing" || resolution.kind === "unknown-ref") {
    console.error(describeResolution(resolution));
    return 2;
  }
  console.log(describeResolution(resolution));
  if (resolution.kind === "no-package") return 0;

  const acknowledgements = await readAcknowledgements(root);
  const since = await baselineVersion(root, resolution.commit);
  if (record && since === undefined) {
    console.error(`api-check: iOS: '${resolution.ref}' has no X.Y.Z VERSION file, so a breakage cannot be acknowledged against it.`);
    return 2;
  }

  const scratch = path.join(root, SCRATCH_PATH);
  const baselineSource = path.join(scratch, "baseline-source");
  await extractBaseline(root, resolution.commit, baselineSource);

  const baselineDescription = await describePackage(baselineSource);
  const currentDescription = await describePackage(root);
  const modules = libraryModules(baselineDescription);
  const { stdout: sdk } = await exec("xcrun", ["--sdk", "iphonesimulator", "--show-sdk-path"]);
  const { stdout: digester } = await exec("xcrun", ["--find", "swift-api-digester"]);
  const { stdout: arch } = await exec("uname", ["-m"]);
  const toolchain = simulatorToolchain(arch.trim(), minimumIOS(baselineDescription), sdk.trim());
  console.log(`api-check: iOS: modules ${modules.join(", ")} on ${toolchain.triple}`);

  const baselineDumps = await dumpModules(digester.trim(), toolchain, baselineSource, path.join(scratch, "baseline-build"), modules, path.join(scratch, "dumps", "baseline"));
  const failedBaseline = modules.filter((module) => baselineDumps.get(module) === undefined);
  if (failedBaseline.length > 0) throw new Error(`could not build or dump the baseline API of ${failedBaseline.join(", ")}`);
  const currentDumps = await dumpModules(digester.trim(), toolchain, root, path.join(scratch, "current-build"), modules, path.join(scratch, "dumps", "current"));

  const breakages: string[] = productBreakages(baselineDescription, currentDescription);
  for (const module of modules) {
    const current = currentDumps.get(module);
    if (current === undefined) {
      breakages.push(`${module}: module could not be built or dumped (removed or broken)`);
      continue;
    }
    const report = path.join(scratch, "dumps", `${module}-report.txt`);
    await run(digester.trim(), diagnoseArguments(baselineDumps.get(module) as string, current, report), root);
    for (const breakage of parseBreakages(await readFile(report, "utf8"))) breakages.push(`${module}: ${breakage}`);
  }

  if (record) {
    const recorded = breakages.map((breakage) => ({ since: since as string, breakage }));
    const target = path.join(root, ACKNOWLEDGED_BREAKAGES_FILE);
    await mkdir(path.dirname(target), { recursive: true });
    await writeFile(target, formatAcknowledgements(recorded));
    console.log(`api-check: iOS: recorded ${recorded.length} breaking change(s) against '${resolution.ref}' (VERSION ${since}) in ${ACKNOWLEDGED_BREAKAGES_FILE}`);
    for (const breakage of breakages) console.log(`  ${breakage}`);
    return 0;
  }

  const result = partitionBreakages(breakages, acknowledgements, since);
  for (const breakage of result.acknowledged) console.log(`api-check: iOS: acknowledged in ${ACKNOWLEDGED_BREAKAGES_FILE}: ${breakage}`);
  if (result.stale.length > 0) console.log(`api-check: iOS: ignoring ${result.stale.length} stale acknowledgement(s) in ${ACKNOWLEDGED_BREAKAGES_FILE}: made against another VERSION than '${resolution.ref}' (${since ?? "unknown"}) or no longer reported. Delete them when convenient.`);
  if (result.unacknowledged.length === 0) {
    console.log(`api-check: iOS: no unacknowledged breaking changes against '${resolution.ref}'`);
    return 0;
  }
  console.error(`api-check: iOS: ${result.unacknowledged.length} breaking change(s) against '${resolution.ref}':`);
  for (const breakage of result.unacknowledged) console.error(`  ${breakage}`);
  console.error(`If the break is intended, record it with make ios-api-check-record BASELINE=${resolution.ref} and commit ${ACKNOWLEDGED_BREAKAGES_FILE}.`);
  return 1;
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
  try {
    process.exitCode = await main(root);
  } catch (error) {
    console.error(`api-check: ${error instanceof Error ? error.message : String(error)}`);
    process.exitCode = 2;
  }
}
