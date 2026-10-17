import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");

interface PackageRule {
  matchManagers?: string[];
  matchPackageNames?: string[];
  matchDepNames?: string[];
  matchDatasources?: string[];
  matchDepTypes?: string[];
  matchUpdateTypes?: string[];
  groupName?: string | null;
  addLabels?: string[];
  automerge?: boolean;
  dependencyDashboardApproval?: boolean;
}

interface RenovateConfig {
  extends: string[];
  schedule: string[];
  dependencyDashboard: boolean;
  automerge: boolean;
  separateMajorMinor: boolean;
  labels: string[];
  packageRules: PackageRule[];
}

const config = JSON.parse(await readFile(path.join(repoRoot, ".github/renovate.json"), "utf8")) as RenovateConfig;
const tools = JSON.parse(await readFile(path.join(repoRoot, "tools/package.json"), "utf8")) as { devDependencies: Record<string, string> };

const LIBRARY_GROUPS = ["AndroidX", "AGP, Kotlin and KSP", "Material Components", "Android test libraries", "Gradle wrapper", "npm tools", "Codegen-sensitive npm packages", "SPM test dependencies", "GitHub Actions"];
const TOOLCHAIN_GROUPS = ["Node toolchain", "JDK toolchain", "CI runner images"];
const AREA_LABELS: Record<string, string> = { gradle: "android", "gradle-wrapper": "android", swift: "ios", npm: "tools", "github-actions": "ci", mise: "toolchain" };

function group(name: string): PackageRule {
  const found = config.packageRules.find((rule) => rule.groupName === name);
  if (!found) throw new Error(`no Renovate group ${name}`);
  return found;
}

function majorRuleIndex(): number {
  const index = config.packageRules.findIndex((rule) => rule.matchUpdateTypes?.includes("major") && rule.groupName === null);
  if (index < 0) throw new Error("no rule that ungroups major updates");
  return index;
}

describe("renovate config", () => {
  it("runs once a month with the dashboard on and auto-merge off", () => {
    expect(config.schedule).toEqual(["* 0-5 1 * *"]);
    expect(config.dependencyDashboard).toBe(true);
    expect(config.automerge).toBe(false);
    expect(config.packageRules.filter((rule) => rule.automerge !== undefined)).toEqual([]);
  });

  it("labels every PR dependencies plus an area label for each manager", () => {
    expect(config.labels).toEqual(["dependencies"]);
    for (const [manager, label] of Object.entries(AREA_LABELS)) {
      const labelled = config.packageRules.some((rule) => rule.matchManagers?.includes(manager) && rule.addLabels?.includes(label));
      expect(labelled, manager).toBe(true);
    }
  });

  it("defines every library and toolchain group", () => {
    expect(config.packageRules.map((rule) => rule.groupName)).toEqual(expect.arrayContaining([...LIBRARY_GROUPS, "Compose BOM", ...TOOLCHAIN_GROUPS]));
  });

  it("splits major updates out of the library groups", () => {
    expect(config.separateMajorMinor).toBe(true);
    for (const name of LIBRARY_GROUPS) {
      expect(config.packageRules.indexOf(group(name)), name).toBeLessThan(majorRuleIndex());
    }
  });

  it("keeps the codegen-sensitive packages in their own group, all of them real tools dependencies", () => {
    const codegen = group("Codegen-sensitive npm packages");
    expect(codegen.matchPackageNames).toEqual(["canvaskit-wasm", "opentype.js", "style-dictionary"]);
    expect(codegen.addLabels).toContain("codegen");
    for (const name of codegen.matchPackageNames ?? []) expect(Object.keys(tools.devDependencies), name).toContain(name);
    expect(config.packageRules.indexOf(codegen)).toBeGreaterThan(config.packageRules.indexOf(group("npm tools")));
  });

  it("asks for dashboard approval before any toolchain update", () => {
    for (const name of TOOLCHAIN_GROUPS) {
      expect(group(name).dependencyDashboardApproval, name).toBe(true);
      expect(config.packageRules.indexOf(group(name)), name).toBeGreaterThan(majorRuleIndex());
    }
  });

  it("keeps GitHub Actions on major tags instead of digest pins", () => {
    expect(config.extends).toEqual(["config:recommended"]);
    expect(JSON.stringify(config)).not.toMatch(/pinGitHubActionDigests|pinDigests/);
  });
});
