import { execFile } from "node:child_process";
import { mkdtemp, readdir, readFile, rm, stat, symlink } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { promisify } from "node:util";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { parse } from "yaml";
import { callsOf, repoRoot, stubTools, type StubTools } from "./stub-tools.ts";

const run = promisify(execFile);

const UNTRUSTED_CONTEXT = /\$\{\{[^}]*\b(inputs\.|github\.(ref|ref_name|base_ref|head_ref|event)\b|steps\.|needs\b|secrets\.)/;
const PAYLOADS = ["0.1.0'; echo INJECTED; '", "$(shell echo INJECTED >&2)0.1.0", '0.1.0"; echo INJECTED; "$(echo INJECTED)`echo INJECTED`'];

type EnvBlock = Record<string, unknown> | undefined;

function collectEnv(node: unknown, found: Map<string, string>): void {
  if (Array.isArray(node)) {
    for (const entry of node) collectEnv(entry, found);
    return;
  }
  if (node === null || typeof node !== "object") return;
  for (const [key, value] of Object.entries(node as Record<string, unknown>)) {
    if (key === "env") {
      for (const [name, expression] of Object.entries((value as EnvBlock) ?? {})) found.set(name, String(expression));
    } else {
      collectEnv(value, found);
    }
  }
}

async function workflowFedVariables(): Promise<string[]> {
  const directory = path.join(repoRoot, ".github/workflows");
  const found = new Map<string, string>();
  for (const file of (await readdir(directory)).filter((name) => name.endsWith(".yml"))) {
    collectEnv(parse(await readFile(path.join(directory, file), "utf8")), found);
  }
  return [...found].filter(([, expression]) => UNTRUSTED_CONTEXT.test(expression)).map(([name]) => name).sort();
}

describe("workflow-fed variables", () => {
  it("are the ones this suite expects", async () => {
    expect(await workflowFedVariables()).toEqual(["BASE", "BASELINE", "GH_TOKEN", "NEEDS", "RELEASE_REF", "TAG"]);
  });

  it("are never expanded by make, only read by the shell, so neither a quote nor a $(...) in them can reach the recipe text", async () => {
    const makefile = await readFile(path.join(repoRoot, "Makefile"), "utf8");
    for (const name of await workflowFedVariables()) {
      expect(makefile, name).not.toMatch(new RegExp(`(?<!\\$)\\$[({]${name}[)}]`));
    }
    for (const name of ["V", "FROM", "DS_VERSION", "RELEASE_TARGET", "LAST_TAG"]) {
      expect(makefile, name).not.toMatch(new RegExp(`(?<!\\$)\\$[({]${name}[)}]`));
    }
  });
});

describe("recipes that take a value from the environment", () => {
  let stubs: StubTools;

  beforeAll(async () => {
    stubs = await stubTools();
  });

  afterAll(async () => {
    await stubs.dispose();
  });

  const cases: { target: string; env: (payload: string) => Record<string, string>; expected: (payload: string) => string[][]; tool?: string }[] = [
    { target: "release-verify", env: (value) => ({ TAG: value, RELEASE_REF: value }), expected: (value) => [["npm", "--prefix", "tools", "run", "--silent", "release", "--", "verify", "--tag", value, "--ref", value]] },
    { target: "release-notes", env: (value) => ({ TAG: value }), expected: (value) => [["npm", "--prefix", "tools", "run", "--silent", "release", "--", "notes", "--tag", value, "--out", ".build/release/notes.md"]] },
    { target: "release-github", tool: "gh", env: (value) => ({ TAG: value }), expected: (value) => [["gh", "release", "create", value, "--verify-tag", "--title", value, "--notes-file", ".build/release/notes.md"]] },
    { target: "release-bump", env: (value) => ({ V: value }), expected: (value) => [["npm", "--prefix", "tools", "run", "--silent", "release", "--", "bump", "--version", value]] },
    { target: "ios-api-check", env: (value) => ({ BASELINE: value }), expected: (value) => [["npm", "--prefix", "tools", "run", "--silent", "api-check-ios", "--", "--baseline", value]] },
    { target: "ios-api-check-record", env: (value) => ({ BASELINE: value }), expected: (value) => [["npm", "--prefix", "tools", "run", "--silent", "api-check-ios", "--", "--baseline", value, "--record"]] },
    { target: "ci-changes", env: (value) => ({ BASE: value }), expected: (value) => [["npm", "--prefix", "tools", "run", "--silent", "ci-changes", "--", "--base", value]] },
    { target: "ds-sync", env: (value) => ({ FROM: value, DS_VERSION: value }), expected: (value) => [["npm", "--prefix", "tools", "run", "--silent", "ds-sync", "--", "--from", value, "--version", value]] },
  ];

  for (const { target, env, expected, tool = "npm" } of cases) {
    it(`${target} passes each value to the tool as one verbatim argument`, async () => {
      for (const payload of PAYLOADS) {
        const result = await stubs.run(target, { env: env(payload) });
        expect(result.output, `${target} ${payload}`).not.toContain("INJECTED");
        expect(callsOf(result, tool).filter((call) => !call.includes("ci")), `${target} ${payload}`).toEqual(expected(payload));
      }
    });
  }

  it("leaves out optional flags whose value is unset or empty", async () => {
    const verify = await stubs.run("release-verify", { env: { TAG: "1.2.3", RELEASE_REF: "" } });
    expect(callsOf(verify, "npm", "release")).toEqual([["npm", "--prefix", "tools", "run", "--silent", "release", "--", "verify", "--tag", "1.2.3"]]);
    const check = await stubs.run("ios-api-check", { env: { BASELINE: "" } });
    expect(callsOf(check, "npm", "api-check-ios")).toEqual([["npm", "--prefix", "tools", "run", "--silent", "api-check-ios", "--"]]);
    const changes = await stubs.run("ci-changes", { env: { BASE: "" } });
    expect(callsOf(changes, "npm", "ci-changes")).toEqual([["npm", "--prefix", "tools", "run", "--silent", "ci-changes", "--"]]);
    const sync = await stubs.run("ds-sync", { env: { FROM: "", DS_VERSION: "", UPDATE: "1" } });
    expect(callsOf(sync, "npm", "ds-sync")).toEqual([["npm", "--prefix", "tools", "run", "--silent", "ds-sync", "--", "--update"]]);
  });
});

describe("ios-api-check-last-tag", () => {
  let stubs: StubTools;
  let repo: string;
  const git = (...args: string[]) => run("git", ["-c", "user.name=t", "-c", "user.email=t@example.com", ...args], { cwd: repo });

  beforeAll(async () => {
    stubs = await stubTools();
    repo = await mkdtemp(path.join(tmpdir(), "make-last-tag-injection-"));
    await symlink(path.join(repoRoot, "Makefile"), path.join(repo, "Makefile"));
    await symlink(path.join(repoRoot, "tools"), path.join(repo, "tools"));
    await git("init", "-q");
    await git("commit", "-q", "--allow-empty", "-m", "first");
  });

  afterAll(async () => {
    await stubs.dispose();
    await rm(repo, { recursive: true, force: true });
  });

  it("skips with a notice when there is no release tag yet", async () => {
    const result = await stubs.run("ios-api-check-last-tag", { cwd: repo });
    expect(result.code).toBe(0);
    expect(result.output).toContain("ios-api-check-last-tag: no tag yet");
    expect(callsOf(result, "npm", "api-check-ios")).toEqual([]);
  });

  it("checks against the latest release tag before HEAD", async () => {
    await git("tag", "1.2.2");
    await git("commit", "-q", "--allow-empty", "-m", "after 1.2.2");
    const result = await stubs.run("ios-api-check-last-tag", { cwd: repo });
    expect(callsOf(result, "npm", "api-check-ios")).toEqual([["npm", "--prefix", "tools", "run", "--silent", "api-check-ios", "--", "--baseline", "1.2.2"]]);
  });

  it("passes a tag name with quotes and command substitutions to the API check verbatim", async () => {
    const tag = "1.2.3';touch${IFS}INJECTED;'$(touch${IFS}INJECTED)";
    await git("tag", tag);
    await git("commit", "-q", "--allow-empty", "-m", "second");
    const result = await stubs.run("ios-api-check-last-tag", { cwd: repo });
    expect(callsOf(result, "npm", "api-check-ios")).toEqual([["npm", "--prefix", "tools", "run", "--silent", "api-check-ios", "--", "--baseline", tag]]);
    await expect(stat(path.join(repo, "INJECTED"))).rejects.toThrow();
  });
});
