import { readdir, readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";
import { parse } from "yaml";
import { affectedAreas, matchesPattern, outputs } from "../src/changes.ts";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const workflowsDir = path.join(repoRoot, ".github/workflows");

interface Step {
  id?: string;
  if?: string;
  uses?: string;
  run?: string;
  with?: Record<string, unknown>;
  env?: Record<string, unknown>;
}

interface Job {
  name?: string;
  needs?: string | string[];
  if?: string;
  "runs-on": string;
  "timeout-minutes"?: number;
  permissions?: Record<string, string>;
  concurrency?: { group: string; "cancel-in-progress": unknown };
  outputs?: Record<string, string>;
  env?: Record<string, unknown>;
  steps: Step[];
}

interface CallJob {
  needs?: string | string[];
  uses: string;
}

interface Workflow {
  name: string;
  on: Record<string, unknown>;
  permissions?: Record<string, string>;
  concurrency?: { group: string; "cancel-in-progress": unknown };
  env?: Record<string, unknown>;
  jobs: Record<string, Job | CallJob>;
}

const files = (await readdir(workflowsDir)).filter((file) => file.endsWith(".yml")).sort();
const sources = new Map(await Promise.all(files.map(async (file) => [file, await readFile(path.join(workflowsDir, file), "utf8")] as const)));
const workflows = new Map([...sources].map(([file, source]) => [file, parse(source) as Workflow]));

function workflow(file: string): Workflow {
  const found = workflows.get(file);
  if (!found) throw new Error(`no workflow ${file}`);
  return found;
}

function isCallJob(entry: Job | CallJob): entry is CallJob {
  return "uses" in entry;
}

function job(file: string, name: string): Job {
  const found = workflow(file).jobs[name];
  if (!found || isCallJob(found)) throw new Error(`no job ${name} with steps in ${file}`);
  return found;
}

function callJob(file: string, name: string): CallJob {
  const found = workflow(file).jobs[name];
  if (!found || !isCallJob(found)) throw new Error(`no reusable workflow call ${name} in ${file}`);
  return found;
}

function allJobs(): [string, string, Job][] {
  return [...workflows].flatMap(([file, flow]) => Object.entries(flow.jobs).flatMap(([name, entry]) => (isCallJob(entry) ? [] : [[file, name, entry] as [string, string, Job]])));
}

const RELEASE_GATES = { "codegen-check": "codegen-check.yml", ios: "ios.yml", android: "android.yml", demos: "demos.yml" } as const;
const AUDIT_CONDITION =
  "github.event_name != 'pull_request' || (github.event.action == 'labeled' && github.event.label.name == 'a11y-audit') || (github.event.action == 'synchronize' && contains(github.event.pull_request.labels.*.name, 'a11y-audit'))";
const AGGREGATORS: Record<string, string> = { "codegen-check.yml": "codegen-check-ok", "ios.yml": "ios-ok", "android.yml": "android-ok", "demos.yml": "demos-ok" };
const RELEASE_WRITE_PERMISSIONS: Record<string, Record<string, string>> = {
  "publish-android": { contents: "read", packages: "write" },
  "github-release": { contents: "write" },
};

function makeTargets(entry: Job): string[] {
  const commands = entry.steps.flatMap((step) => [step.run, typeof step.with?.script === "string" ? step.with.script : undefined]).filter((command): command is string => command !== undefined);
  return commands.map((command) => command.trim().split(/\s+/)[1]?.replace(/"/g, "") ?? "");
}

function checkout(entry: Job): Step {
  const found = entry.steps.find((step) => step.uses?.startsWith("actions/checkout@"));
  if (!found) throw new Error("no checkout step");
  return found;
}

async function miseTool(name: string): Promise<string> {
  const mise = await readFile(path.join(repoRoot, "mise.toml"), "utf8");
  const match = new RegExp(`^${name} = "([^"]+)"`, "m").exec(mise);
  if (!match?.[1]) throw new Error(`mise.toml has no ${name}`);
  return match[1];
}

describe("workflow files", () => {
  it("has the expected workflows", () => {
    expect(files).toEqual(["android.yml", "codegen-check.yml", "demos-audit.yml", "demos.yml", "ios.yml", "release.yml"]);
  });

  it("grants only read access to the repository contents, except the two release jobs that need write", () => {
    for (const [file, flow] of workflows) {
      expect(flow.permissions, file).toEqual({ contents: "read" });
    }
    for (const [file, name, entry] of allJobs()) {
      expect(entry.permissions, `${file} ${name}`).toEqual(file === "release.yml" ? RELEASE_WRITE_PERMISSIONS[name] : undefined);
    }
  });

  it("uses no secret beyond GITHUB_TOKEN", () => {
    for (const [file, source] of sources) {
      const secrets = [...source.matchAll(/secrets\.([A-Za-z_]+)/g)].map((match) => match[1]);
      expect(secrets.filter((secret) => secret !== "GITHUB_TOKEN"), file).toEqual([]);
    }
  });

  it("keeps every run step a single make call", () => {
    for (const [file, name, entry] of allJobs()) {
      for (const step of entry.steps) {
        const commands = [step.run, typeof step.with?.script === "string" ? step.with.script : undefined].filter((command): command is string => command !== undefined);
        for (const command of commands) {
          expect(command.trim(), `${file} ${name}`).toMatch(/^make [^\n;&|`$]*(\$DEMO[^\n;&|`$]*)?$/);
        }
      }
    }
  });

  it("pins every action to a major version tag", () => {
    for (const [file, name, entry] of allJobs()) {
      for (const step of entry.steps.filter((candidate) => candidate.uses !== undefined)) {
        expect(step.uses, `${file} ${name}`).toMatch(/^[a-z0-9-]+\/[a-z0-9-]+(\/[a-z0-9-]+)?@v\d+$/);
      }
    }
  });

  it("uses one version of each action across all workflows", () => {
    const versions = new Map<string, Set<string>>();
    for (const [, , entry] of allJobs()) {
      for (const uses of entry.steps.flatMap((step) => (step.uses ? [step.uses] : []))) {
        const [action = "", version = ""] = uses.split("@");
        versions.set(action, (versions.get(action) ?? new Set()).add(version));
      }
    }
    for (const [action, found] of versions) expect([...found], action).toHaveLength(1);
  });

  it("gives every job a timeout", () => {
    for (const [file, name, entry] of allJobs()) {
      expect(entry["timeout-minutes"], `${file} ${name}`).toBeGreaterThan(0);
    }
  });

  it("uploads artifacts only on failure, except the release packages", () => {
    for (const [file, name, entry] of allJobs().filter(([candidate, job]) => !(candidate === "release.yml" && job === "android-packages"))) {
      for (const step of entry.steps.filter((candidate) => candidate.uses?.startsWith("actions/upload-artifact@"))) {
        expect(step.if, `${file} ${name}`).toBe("failure()");
      }
    }
  });

  it("selects the pinned Xcode first on every macOS job", () => {
    for (const [file, name, entry] of allJobs().filter(([, , candidate]) => candidate["runs-on"].startsWith("macos-"))) {
      const targets = makeTargets(entry);
      if (!targets.some((target) => target.startsWith("ios-") || target.startsWith("demo-ios-"))) continue;
      expect(targets[0], `${file} ${name}`).toBe("ci-xcode");
    }
  });

  it("turns the hardware keyboard off before every iOS simulator test", () => {
    for (const [file, name, entry] of allJobs()) {
      const targets = makeTargets(entry);
      const firstTest = targets.findIndex((target) => /^(ios-test|demo-ios-.*-(test|audit))$/.test(target));
      if (firstTest < 0) continue;
      expect(targets.indexOf("ci-ios-simulator"), `${file} ${name}`).toBeGreaterThanOrEqual(0);
      expect(targets.indexOf("ci-ios-simulator"), `${file} ${name}`).toBeLessThan(firstTest);
    }
  });

  it("uses the Node and Java versions pinned in mise.toml", async () => {
    const node = await miseTool("node");
    const [distribution, java] = (await miseTool("java")).split("-");
    for (const [file, name, entry] of allJobs()) {
      for (const step of entry.steps) {
        if (step.uses?.startsWith("actions/setup-node@")) expect(String(step.with?.["node-version"]), `${file} ${name}`).toBe(node);
        if (step.uses?.startsWith("actions/setup-java@")) {
          expect(step.with?.distribution, `${file} ${name}`).toBe(distribution);
          expect(String(step.with?.["java-version"]), `${file} ${name}`).toBe(java);
        }
      }
    }
  });
});

describe("required jobs", () => {
  it("runs tools-test, actionlint, codegen-check and ds-sync on every pull request", () => {
    const flow = workflow("codegen-check.yml");
    expect(Object.keys(flow.on)).toContain("pull_request");
    expect(makeTargets(job("codegen-check.yml", "tools"))).toEqual(["tools-test", "actionlint", "codegen-check", "ds-sync"]);
  });

  it("runs the iOS library build, tests on the pinned and oldest runtimes, and api-check", () => {
    const test = job("ios.yml", "test");
    expect(makeTargets(test)).toEqual(["ci-xcode", "ci-ios-simulator", "ios-build", "ios-test"]);
    expect(sources.get("ios.yml")).toContain("runtime: [pinned, oldest]");
    expect(test.env).toEqual({ IOS_RUNTIMES: "${{ matrix.runtime }}" });
    expect(makeTargets(job("ios.yml", "api-check"))).toEqual(["ci-xcode", "ios-api-check", "ios-api-check-last-tag"]);
  });

  it("compares the iOS API with the PR base branch, and with the last tag on master", () => {
    const steps = job("ios.yml", "api-check").steps;
    const pr = steps.find((step) => step.run === "make ios-api-check");
    expect(pr?.if).toBe("github.event_name == 'pull_request'");
    expect(pr?.env?.BASELINE).toBe("origin/${{ github.base_ref }}");
    expect(steps.find((step) => step.run === "make ios-api-check-last-tag")?.if).toBe("github.event_name != 'pull_request'");
    expect(checkout(job("ios.yml", "api-check")).with?.["fetch-depth"]).toBe(0);
  });

  it("runs the Android build with lint and apiCheck, and the Android tests on macOS", () => {
    expect(makeTargets(job("android.yml", "build"))).toEqual(["android-build"]);
    expect(makeTargets(job("android.yml", "test"))).toEqual(["android-test"]);
    expect(job("android.yml", "test")["runs-on"]).toMatch(/^macos-/);
  });

  it("keeps the oldest-runtime legs warn-only, so a runner image without an older iOS runtime does not fail CI", () => {
    for (const [file, name, entry] of allJobs()) {
      expect(entry.env?.IOS_REQUIRE_OLDEST, `${file} ${name}`).toBeUndefined();
      for (const step of entry.steps) expect(step.env?.IOS_REQUIRE_OLDEST, `${file} ${name}`).toBeUndefined();
    }
    for (const [file, flow] of workflows) expect(flow.env?.IOS_REQUIRE_OLDEST, file).toBeUndefined();
  });

  it("caps the Gradle workers in every workflow that runs Gradle", () => {
    for (const file of ["android.yml", "demos.yml", "release.yml"]) {
      expect(workflow(file).env?.GRADLE_WORKERS, file).toBe(2);
    }
  });

  it("runs the demo matrix with boundaries and demo-support tests", () => {
    expect(makeTargets(job("demos.yml", "boundaries"))).toEqual(["demos-check-boundaries"]);
    expect(makeTargets(job("demos.yml", "ios-demo-support"))).toEqual(["ci-xcode", "ci-ios-simulator", "demo-ios-support-test"]);
    expect(makeTargets(job("demos.yml", "ios-demo"))).toEqual(["ci-xcode", "ci-ios-simulator", "demo-ios-$DEMO", "demo-ios-$DEMO-test", "ci-ios-simulator", "demo-ios-$DEMO-theme-test"]);
    expect(makeTargets(job("demos.yml", "android-demo"))).toEqual(["ci-android-kvm", "demo-android-$DEMO", "demo-android-$DEMO-test"]);
    const source = sources.get("demos.yml") ?? "";
    expect(source).toContain("demo: ${{ fromJSON(needs.changes.outputs.ios_demos) }}");
    expect(source).toContain("runtime: [pinned, oldest]");
    expect(source).toContain("demo: ${{ fromJSON(needs.changes.outputs.android_demos) }}");
    expect(source).toContain("api: [26, 36]");
  });

  it("keeps the iOS accessibility audits out of the pull request run", () => {
    for (const [file, name, entry] of allJobs().filter(([candidate]) => candidate !== "demos-audit.yml")) {
      expect(makeTargets(entry).filter((target) => target.endsWith("-audit") || target === "demos-audit"), `${file} ${name}`).toEqual([]);
    }
    const audit = workflow("demos-audit.yml");
    expect(Object.keys(audit.on).sort()).toEqual(["pull_request", "schedule", "workflow_dispatch"]);
    expect(audit.on.pull_request).toEqual({ types: ["labeled", "synchronize"] });
    expect(job("demos-audit.yml", "ios-audit").if).toBe(AUDIT_CONDITION);
    expect(audit.concurrency).toBeUndefined();
    expect(job("demos-audit.yml", "ios-audit").concurrency).toEqual({ group: "demos-audit-${{ matrix.demo }}-${{ github.ref }}", "cancel-in-progress": "${{ github.event_name == 'pull_request' }}" });
    expect(makeTargets(job("demos-audit.yml", "ios-audit"))).toEqual(["ci-xcode", "ci-ios-simulator", "demo-ios-$DEMO-audit"]);
  });
});

describe("required-check aggregators", () => {
  function needsOf(entry: Job): string[] {
    return (Array.isArray(entry.needs) ? entry.needs : entry.needs === undefined ? [] : [entry.needs]).slice().sort();
  }

  it("adds one always-run aggregator to each gate workflow, needing every other job", () => {
    for (const [file, name] of Object.entries(AGGREGATORS)) {
      const aggregator = job(file, name);
      expect(aggregator.if, file).toBe("always()");
      expect(needsOf(aggregator), file).toEqual(Object.keys(workflow(file).jobs).filter((candidate) => candidate !== name).sort());
      expect(makeTargets(aggregator), file).toEqual(["ci-required"]);
      expect(aggregator.steps.find((step) => step.run === "make ci-required")?.env, file).toEqual({ NEEDS: "${{ toJSON(needs) }}" });
      expect(Object.values(workflow(file).jobs).filter((entry) => !isCallJob(entry) && entry.if === "always()"), file).toHaveLength(1);
    }
  });

  it("is the only job that runs whatever the other jobs did", () => {
    for (const [file, name, entry] of allJobs()) {
      if (AGGREGATORS[file] === name) continue;
      expect(entry.if ?? "", `${file} ${name}`).not.toContain("always()");
    }
  });
});

describe("theme override cross case", () => {
  it("runs the demo suite on a light simulator, then ThemeOverrideTests again on a dark one, so neither cross case always skips", () => {
    const demo = job("demos.yml", "ios-demo");
    expect(demo.env?.IOS_SIM_APPEARANCE).toBe("light");
    const index = (run: string) => demo.steps.findIndex((step) => step.run === run);
    const suite = index('make "demo-ios-$DEMO-test"');
    const flip = demo.steps.findIndex((step, position) => position > suite && step.run === "make ci-ios-simulator");
    const theme = index('make "demo-ios-$DEMO-theme-test"');
    expect(suite).toBeGreaterThan(0);
    expect(flip).toBeGreaterThan(suite);
    expect(theme).toBeGreaterThan(flip);
    expect(demo.steps[flip]?.env).toEqual({ IOS_SIM_APPEARANCE: "dark" });
    expect(demo.steps[flip]?.if).toBe("matrix.runtime == 'pinned'");
    expect(demo.steps[theme]?.if).toBe("matrix.runtime == 'pinned'");
  });
});

describe("audit trigger", () => {
  type Event = { name: string; action?: string; label?: string; labels?: string[] };

  function auditRuns(event: Event): boolean {
    const expression = job("demos-audit.yml", "ios-audit").if ?? "";
    expect(expression).toBe(AUDIT_CONDITION);
    if (event.name !== "pull_request") return true;
    if (event.action === "labeled") return event.label === "a11y-audit";
    if (event.action === "synchronize") return (event.labels ?? []).includes("a11y-audit");
    return false;
  }

  it("runs when the a11y-audit label is added, and again on each push to a PR that carries it", () => {
    expect(auditRuns({ name: "pull_request", action: "labeled", label: "a11y-audit", labels: ["a11y-audit"] })).toBe(true);
    expect(auditRuns({ name: "pull_request", action: "synchronize", labels: ["ios", "a11y-audit"] })).toBe(true);
  });

  it("does not run for another label or for a push to an unlabelled PR", () => {
    expect(auditRuns({ name: "pull_request", action: "labeled", label: "ios", labels: ["ios", "a11y-audit"] })).toBe(false);
    expect(auditRuns({ name: "pull_request", action: "synchronize", labels: ["ios"] })).toBe(false);
  });

  it("always runs on the nightly schedule and on dispatch", () => {
    expect(auditRuns({ name: "schedule" })).toBe(true);
    expect(auditRuns({ name: "workflow_dispatch" })).toBe(true);
  });
});

describe("snapshot references", () => {
  it("never compares, records or downloads snapshot references in CI, since they are local and git-ignored", () => {
    for (const [file, name, entry] of allJobs()) {
      expect(makeTargets(entry).filter((target) => target.includes("snapshot")), `${file} ${name}`).toEqual([]);
      for (const step of entry.steps.filter((candidate) => candidate.uses?.startsWith("actions/checkout@"))) expect(step.with?.lfs, `${file} ${name}`).toBeUndefined();
      for (const step of entry.steps) expect(step.with?.path ?? "", `${file} ${name}`).not.toContain("snapshot");
    }
    for (const [file, source] of sources) expect(source, file).not.toMatch(/lfs|SNAPSHOT_ARTIFACTS|TS_SNAPSHOT_VERIFY|roborazzi\.test\.verify/i);
  });
});

describe("cache keys", () => {
  const SKIPPED = new Set([".git", "node_modules", ".build", "build", ".gradle", "DerivedData", ".swiftpm", ".codegraph", ".claude"]);

  async function repositoryFiles(directory: string = repoRoot): Promise<string[]> {
    const files: string[] = [];
    for (const entry of await readdir(directory, { withFileTypes: true })) {
      if (SKIPPED.has(entry.name)) continue;
      const full = path.join(directory, entry.name);
      if (entry.isDirectory()) files.push(...(await repositoryFiles(full)));
      else files.push(path.relative(repoRoot, full));
    }
    return files;
  }

  it("hash only patterns that match a file in the repository", async () => {
    const files = await repositoryFiles();
    for (const [file, source] of sources) {
      for (const match of source.matchAll(/hashFiles\(([^)]*)\)/g)) {
        for (const pattern of [...(match[1] ?? "").matchAll(/'([^']+)'/g)].map((quoted) => quoted[1] ?? "")) {
          expect(files.some((candidate) => matchesPattern(candidate, pattern)), `${file} ${pattern}`).toBe(true);
        }
      }
    }
  });
});

describe("path filters", () => {
  const filtered = ["android.yml", "demos.yml", "ios.yml"];
  const keys = Object.keys(outputs({ reason: "", files: [], areas: new Set() }));

  it("computes the changes from the PR base or the previous push", () => {
    for (const file of filtered) {
      const changes = job(file, "changes");
      expect(checkout(changes).with?.["fetch-depth"], file).toBe(0);
      const step = changes.steps.find((candidate) => candidate.id === "changes");
      expect(step?.run, file).toBe("make ci-changes");
      expect(step?.env?.BASE, file).toBe("${{ github.event.pull_request.base.sha || github.event.before }}");
      for (const [key, value] of Object.entries(changes.outputs ?? {})) {
        expect(keys, file).toContain(key);
        expect(value, file).toBe(`\${{ steps.changes.outputs.${key} }}`);
      }
    }
  });

  it("gates every other job on a changes output", () => {
    for (const file of filtered) {
      for (const name of Object.keys(workflow(file).jobs).filter((candidate) => candidate !== "changes" && candidate !== AGGREGATORS[file])) {
        const entry = job(file, name);
        expect(entry.needs, `${file} ${name}`).toBe("changes");
        const output = /^needs\.changes\.outputs\.([a-z_]+) (==|!=) '([^']*)'$/.exec(entry.if ?? "");
        expect(output, `${file} ${name}`).not.toBeNull();
        expect(Object.keys(job(file, "changes").outputs ?? {}), `${file} ${name}`).toContain(output?.[1]);
      }
    }
  });

  function runs(file: string, name: string, changed: string[]): boolean {
    const values = outputs({ reason: "", files: changed, areas: affectedAreas(changed) });
    const [, key = "", operator, expected] = /^needs\.changes\.outputs\.([a-z_]+) (==|!=) '([^']*)'$/.exec(job(file, name).if ?? "") ?? [];
    return operator === "==" ? values[key] === expected : values[key] !== expected;
  }

  it("skips the Android library and demo jobs for an iOS-only change", () => {
    const iosOnly = ["ios/Sources/TSDesignSystem/Components/Badge/TSBadge.swift"];
    expect(runs("android.yml", "build", iosOnly)).toBe(false);
    expect(runs("android.yml", "test", iosOnly)).toBe(false);
    expect(runs("demos.yml", "android-demo", iosOnly)).toBe(false);
    expect(runs("ios.yml", "test", iosOnly)).toBe(true);
    expect(runs("ios.yml", "api-check", iosOnly)).toBe(true);
    expect(runs("demos.yml", "ios-demo", iosOnly)).toBe(true);
    expect(runs("demos.yml", "ios-demo-support", iosOnly)).toBe(true);
  });

  it("skips the iOS library and demo jobs for an Android-only change", () => {
    const androidOnly = ["android/views/src/main/kotlin/com/ts/ds/views/components/badge/TSBadgeView.kt"];
    expect(runs("ios.yml", "test", androidOnly)).toBe(false);
    expect(runs("demos.yml", "ios-demo", androidOnly)).toBe(false);
    expect(runs("android.yml", "test", androidOnly)).toBe(true);
    expect(runs("demos.yml", "android-demo", androidOnly)).toBe(true);
  });

  it("runs the boundary check for any demo change", () => {
    expect(runs("demos.yml", "boundaries", ["demos/android-compose/app/build.gradle.kts"])).toBe(true);
    expect(runs("demos.yml", "boundaries", ["ios/Sources/TSTokens/Colors.swift"])).toBe(false);
  });
});

describe("release workflow", () => {
  const release = workflow("release.yml");

  it("runs only on a pushed vX.Y.Z tag, one release at a time, never cancelled", () => {
    expect(release.on).toEqual({ push: { tags: ["v[0-9]+.[0-9]+.[0-9]+"] } });
    expect(release.concurrency).toEqual({ group: "release", "cancel-in-progress": false });
  });

  it("verifies the tag against VERSION and that the tagged commit is on master, before anything else", () => {
    const verify = job("release.yml", "verify");
    expect(verify.needs).toBeUndefined();
    expect(makeTargets(verify)).toEqual(["release-verify"]);
    expect(verify.steps.find((step) => step.run === "make release-verify")?.env).toEqual({ TAG: "${{ github.ref_name }}", RELEASE_REF: "${{ github.ref }}" });
    expect(checkout(verify).with?.["fetch-depth"]).toBe(0);
  });

  it("runs every pull request gate as a reusable workflow after the verify job", () => {
    for (const [name, file] of Object.entries(RELEASE_GATES)) {
      const gate = callJob("release.yml", name);
      expect(gate.uses, name).toBe(`./.github/workflows/${file}`);
      expect(gate.needs, name).toBe("verify");
      expect(Object.keys(workflow(file).on), file).toContain("workflow_call");
    }
    expect(Object.values(release.jobs).filter(isCallJob)).toHaveLength(Object.keys(RELEASE_GATES).length);
  });

  it("gives each reusable gate its own concurrency group per caller and event, so the release neither cancels its gates, queues behind master CI, nor shares a group with a manual run of that gate", () => {
    const evaluate = (group: string, context: Record<string, string>) => group.replace(/\$\{\{ github\.(\w+) \}\}/g, (_, key: string) => context[key] ?? "");
    for (const file of Object.values(RELEASE_GATES)) {
      const group = workflow(file).concurrency?.group ?? "";
      expect(group, file).toBe(`${file.replace(/\.yml$/, "")}-\${{ github.workflow }}-\${{ github.event_name }}-\${{ github.ref }}`);
      const master = { event_name: "workflow_dispatch", ref: "refs/heads/master" };
      const calledByRelease = evaluate(group, { ...master, workflow: release.name });
      const manualRun = evaluate(group, { ...master, workflow: workflow(file).name });
      const masterPush = evaluate(group, { event_name: "push", ref: "refs/heads/master", workflow: workflow(file).name });
      expect(new Set([calledByRelease, manualRun, masterPush]).size, file).toBe(3);
    }
  });

  it("publishes Android only after every gate, with GITHUB_TOKEN and packages: write", () => {
    const publish = job("release.yml", "publish-android");
    expect(publish.needs).toEqual(Object.keys(RELEASE_GATES));
    expect(makeTargets(publish)).toEqual(["tools-deps", "android-publish"]);
    expect(publish.steps.find((step) => step.run === "make android-publish")?.env).toEqual({ GITHUB_TOKEN: "${{ secrets.GITHUB_TOKEN }}" });
  });

  it("builds the Android demo APKs and library AARs after every gate, and uploads them for the release", () => {
    const packages = job("release.yml", "android-packages");
    expect(packages.needs).toEqual(Object.keys(RELEASE_GATES));
    expect(makeTargets(packages)).toEqual(["release-android-packages"]);
    expect(packages.steps.find((step) => step.uses?.startsWith("actions/upload-artifact@"))?.with).toMatchObject({ name: "release-assets", path: ".build/release/assets", "if-no-files-found": "error" });
  });

  it("creates the GitHub release for the pushed tag last, with the release packages attached", () => {
    const github = job("release.yml", "github-release");
    expect(github.needs).toEqual(["publish-android", "android-packages"]);
    expect(makeTargets(github)).toEqual(["tools-deps", "release-github"]);
    expect(github.steps.find((step) => step.uses?.startsWith("actions/download-artifact@"))?.with).toEqual({ name: "release-assets", path: ".build/release/assets" });
    expect(github.steps.find((step) => step.run === "make release-github")?.env).toEqual({ TAG: "${{ github.ref_name }}", GH_TOKEN: "${{ secrets.GITHUB_TOKEN }}" });
  });

  it("does not persist the checkout token in the write-scoped jobs", () => {
    for (const name of Object.keys(RELEASE_WRITE_PERMISSIONS)) {
      expect(checkout(job("release.yml", name)).with?.["persist-credentials"], name).toBe(false);
    }
  });

  it("installs the npm dependencies in a step of their own, so install scripts never run with the write token in the environment", () => {
    for (const name of Object.keys(RELEASE_WRITE_PERMISSIONS)) {
      const steps = job("release.yml", name).steps;
      const install = steps.findIndex((step) => step.run === "make tools-deps");
      const firstToken = steps.findIndex((step) => JSON.stringify(step.env ?? {}).includes("secrets."));
      expect(install, name).toBeGreaterThan(steps.findIndex((step) => step.uses?.startsWith("actions/setup-node@") ?? false));
      expect(install, name).toBeLessThan(firstToken);
      expect(steps[install]?.env, name).toBeUndefined();
    }
  });

  it("passes the token only to the steps that publish", () => {
    const tokenSteps = allJobs()
      .filter(([file]) => file === "release.yml")
      .flatMap(([, name, entry]) => entry.steps.filter((step) => JSON.stringify(step.env ?? {}).includes("secrets.")).map((step) => `${name}: ${step.run ?? ""}`));
    expect(tokenSteps).toEqual(["publish-android: make android-publish", "github-release: make release-github"]);
  });

  it("is the only workflow that can push a tag", () => {
    for (const [file, source] of sources) {
      if (file === "release.yml") continue;
      expect(source, file).not.toMatch(/gh release|git push|git tag/);
    }
  });
});
