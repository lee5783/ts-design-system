import { execFile } from "node:child_process";
import { appendFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { promisify } from "node:util";

const exec = promisify(execFile);

export type Area = "ios" | "android" | "boundaries" | "demo-ios-swiftui" | "demo-ios-uikit" | "demo-android-compose" | "demo-android-views";

export const IOS_DEMOS = ["swiftui", "uikit"] as const;
export const ANDROID_DEMOS = ["compose", "views"] as const;

const SHARED = ["Makefile", ".gitattributes", "tools/package.json", "tools/package-lock.json", "tools/ci/**"];
const IOS_LIBRARY = ["ios/**", "Package.swift", "Package.resolved", ".xcode-version", ".ios-simulator", "design/**", "tools/codegen/**"];
const ANDROID_LIBRARY = ["android/**", "design/**", "tools/codegen/**", "mise.toml"];
const DEMOS_WORKFLOW = ".github/workflows/demos.yml";

export const AREA_PATTERNS: Readonly<Record<Area, readonly string[]>> = {
  ios: [...SHARED, ...IOS_LIBRARY, "tools/api-check/**", ".github/workflows/ios.yml"],
  android: [...SHARED, ...ANDROID_LIBRARY, ".github/workflows/android.yml"],
  boundaries: [...SHARED, "demos/**", "tools/demos/**", "design/specs/components/**", "Package.swift", DEMOS_WORKFLOW],
  "demo-ios-swiftui": [...SHARED, ...IOS_LIBRARY, "demos/ios-demo-support/**", "demos/ios-swiftui/**", DEMOS_WORKFLOW],
  "demo-ios-uikit": [...SHARED, ...IOS_LIBRARY, "demos/ios-demo-support/**", "demos/ios-uikit/**", DEMOS_WORKFLOW],
  "demo-android-compose": [...SHARED, ...ANDROID_LIBRARY, "demos/android-demo-support/**", "demos/android-compose/**", DEMOS_WORKFLOW],
  "demo-android-views": [...SHARED, ...ANDROID_LIBRARY, "demos/android-demo-support/**", "demos/android-views/**", DEMOS_WORKFLOW],
};

const AREAS = Object.keys(AREA_PATTERNS) as Area[];

export interface ChangeSet {
  reason: string;
  files: readonly string[] | undefined;
  areas: ReadonlySet<Area>;
}

function globToRegExp(pattern: string): RegExp {
  let source = "";
  for (let index = 0; index < pattern.length; index += 1) {
    const char = pattern[index] ?? "";
    if (pattern.startsWith("**/", index)) {
      source += "(?:.*/)?";
      index += 2;
    } else if (pattern.startsWith("**", index)) {
      source += ".*";
      index += 1;
    } else if (char === "*") {
      source += "[^/]*";
    } else if (char === "?") {
      source += "[^/]";
    } else {
      source += char.replace(/[.+^${}()|[\]\\]/g, "\\$&");
    }
  }
  return new RegExp(`^${source}$`);
}

export function matchesPattern(file: string, pattern: string): boolean {
  return globToRegExp(pattern).test(file);
}

export function affectedAreas(files: readonly string[]): Set<Area> {
  return new Set(AREAS.filter((area) => files.some((file) => AREA_PATTERNS[area].some((pattern) => matchesPattern(file, pattern)))));
}

function isUsableBase(base: string | undefined): base is string {
  return base !== undefined && base.trim() !== "" && !/^0+$/.test(base.trim());
}

export async function changedFiles(root: string, base: string, git: string = "git"): Promise<string[] | undefined> {
  try {
    await exec(git, ["cat-file", "-e", `${base}^{commit}`], { cwd: root });
  } catch {
    return undefined;
  }
  const { stdout } = await exec(git, ["diff", "--name-only", "--no-renames", "-z", `${base}...HEAD`], { cwd: root, maxBuffer: 64 * 1024 * 1024 });
  return stdout.split("\0").filter((entry) => entry !== "").sort();
}

export async function detectChanges(root: string, base: string | undefined, git: string = "git"): Promise<ChangeSet> {
  if (!isUsableBase(base)) {
    return { reason: "no base commit, so every area runs", files: undefined, areas: new Set(AREAS) };
  }
  const files = await changedFiles(root, base.trim(), git);
  if (files === undefined) {
    return { reason: `base ${base.trim()} is not in the clone, so every area runs`, files: undefined, areas: new Set(AREAS) };
  }
  return { reason: `${files.length} file(s) changed since ${base.trim()}`, files, areas: affectedAreas(files) };
}

export function outputs(changes: ChangeSet): Record<string, string> {
  const selected = (prefix: string, demos: readonly string[]) => JSON.stringify(demos.filter((demo) => changes.areas.has(`${prefix}${demo}` as Area)));
  return {
    ios: String(changes.areas.has("ios")),
    android: String(changes.areas.has("android")),
    boundaries: String(changes.areas.has("boundaries")),
    ios_demos: selected("demo-ios-", IOS_DEMOS),
    android_demos: selected("demo-android-", ANDROID_DEMOS),
  };
}

export function formatOutputs(values: Record<string, string>): string {
  return Object.entries(values)
    .map(([key, value]) => `${key}=${value}\n`)
    .join("");
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
  const baseFlag = process.argv.indexOf("--base");
  const base = baseFlag >= 0 ? process.argv[baseFlag + 1] : undefined;
  try {
    const changes = await detectChanges(root, base);
    const text = formatOutputs(outputs(changes));
    console.log(`ci-changes: ${changes.reason}`);
    for (const file of changes.files ?? []) console.log(`  ${file}`);
    process.stdout.write(text);
    const githubOutput = process.env.GITHUB_OUTPUT;
    if (githubOutput !== undefined && githubOutput !== "") await appendFile(githubOutput, text);
  } catch (error) {
    console.error(`ci-changes: ${error instanceof Error ? error.message : String(error)}`);
    process.exitCode = 2;
  }
}
