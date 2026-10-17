import { execFileSync } from "node:child_process";
import { copyFile, mkdir, mkdtemp, readFile, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { parseArgs, run } from "../../src/cli.ts";
import type { Generator } from "../../src/generate.ts";
import { fixtureExtensionsPath, fixtureTokensPath, rawFixture } from "../support.ts";

let root: string;
let lines: string[];
const log = (line: string) => lines.push(line);

const tokensGenerator: Generator = {
  name: "fixture-tokens",
  target: "tokens",
  outputDir: "out/tokens",
  generate: ({ model }) => [
    { path: "out/tokens/colors.txt", contents: model.color.map((token) => `${token.name.camel}=${token.value.light.hex}\n`).join("") },
    { path: "out/tokens/spacing.txt", contents: model.spacing.map((token) => `${token.name.camel}=${token.value}\n`).join("") },
  ],
};

const iconsGenerator: Generator = {
  name: "fixture-icons",
  target: "icons",
  outputDir: "out/shared",
  generate: () => [{ path: "out/shared/icons.txt", contents: "home\n" }],
};

const sharedTokensGenerator: Generator = {
  name: "fixture-shared-tokens",
  target: "tokens",
  outputDir: "out/shared",
  generate: () => [{ path: "out/shared/tokens.txt", contents: "tokens\n" }],
};

const generators = [tokensGenerator, iconsGenerator, sharedTokensGenerator];

async function addExtensions(raw?: unknown): Promise<void> {
  await mkdir(path.join(root, "design/extensions"), { recursive: true });
  const target = path.join(root, "design/extensions/native-tokens.json");
  await (raw === undefined ? copyFile(fixtureExtensionsPath, target) : writeFile(target, JSON.stringify(raw)));
}

async function writeTokens(raw: unknown): Promise<void> {
  await writeFile(path.join(root, "design/source/tokens.json"), JSON.stringify(raw));
}

beforeEach(async () => {
  root = await mkdtemp(path.join(tmpdir(), "codegen-"));
  await mkdir(path.join(root, "design/source"), { recursive: true });
  await copyFile(fixtureTokensPath, path.join(root, "design/source/tokens.json"));
  lines = [];
});

afterEach(async () => {
  await rm(root, { recursive: true, force: true });
});

describe("parseArgs", () => {
  it("parses generation targets and --check", () => {
    expect(parseArgs(["all"], "/repo")).toEqual({ command: "all", root: "/repo", check: false });
    expect(parseArgs(["tokens", "--check"], "/repo")).toEqual({ command: "tokens", root: "/repo", check: true });
    expect(parseArgs(["icons", "--root", "/other"], "/repo")).toEqual({ command: "icons", root: "/other", check: false });
  });

  it("parses model and diff-report options", () => {
    expect(parseArgs(["model", "--json"], "/repo")).toEqual({ command: "model", root: "/repo", json: true });
    expect(parseArgs(["diff-report", "--base-ref", "HEAD~1"], "/repo")).toEqual({
      command: "diff-report",
      root: "/repo",
      json: false,
      baseRef: "HEAD~1",
    });
  });

  it("resolves relative paths against the invoking directory", () => {
    expect(parseArgs(["diff-report", "--base", "old.json", "--head", "new.json", "--renames", "r.json"], "/repo", "/work")).toEqual({
      command: "diff-report",
      root: "/repo",
      json: false,
      base: "/work/old.json",
      head: "/work/new.json",
      renames: "/work/r.json",
    });
    expect(parseArgs(["all", "--root", "sub"], "/repo", "/work")).toMatchObject({ root: "/work/sub" });
  });

  it("rejects unknown commands, unknown flags and bad diff-report bases", () => {
    expect(() => parseArgs([], "/repo")).toThrow(/missing command/);
    expect(() => parseArgs(["swift"], "/repo")).toThrow(/unknown command: swift/);
    expect(() => parseArgs(["tokens", "--json"], "/repo")).toThrow(/unknown argument for tokens: --json/);
    expect(() => parseArgs(["diff-report"], "/repo")).toThrow(/exactly one of --base or --base-ref/);
    expect(() => parseArgs(["diff-report", "--base", "a", "--base-ref", "b"], "/repo")).toThrow(/exactly one/);
    expect(() => parseArgs(["diff-report", "--base"], "/repo")).toThrow(/--base needs a value/);
  });
});

describe("codegen generation", () => {
  it("writes the outputs, then --check passes", async () => {
    expect(await run({ command: "all", root, check: false }, log, generators)).toBe(0);
    expect(await readFile(path.join(root, "out/tokens/spacing.txt"), "utf8")).toBe("space0_5=2\nspace4=16\n");
    expect(lines.join("\n")).toContain("created  out/tokens/colors.txt");
    lines = [];
    expect(await run({ command: "all", root, check: true }, log, generators)).toBe(0);
    expect(lines.at(-1)).toBe("codegen: 4 generated files are up to date");
  });

  it("fails --check on a hand-edited, missing or stale output and does not touch the files", async () => {
    await run({ command: "all", root, check: false }, log, generators);
    await writeFile(path.join(root, "out/tokens/colors.txt"), "hand edited\n");
    await rm(path.join(root, "out/tokens/spacing.txt"));
    await writeFile(path.join(root, "out/tokens/old.txt"), "stale\n");
    lines = [];
    expect(await run({ command: "tokens", root, check: true }, log, generators)).toBe(1);
    expect(lines).toContain(
      [
        "changed  out/tokens/colors.txt",
        "missing  out/tokens/spacing.txt",
        "stale    out/tokens/old.txt",
        "codegen: generated code is out of date; run make codegen and commit the result",
      ].join("\n"),
    );
    expect(await readFile(path.join(root, "out/tokens/colors.txt"), "utf8")).toBe("hand edited\n");
  });

  it("fails --check after a token change and regenerating fixes it", async () => {
    await run({ command: "all", root, check: false }, log, generators);
    const raw = rawFixture();
    raw.spacing.tokens[1].value = "18px";
    await writeTokens(raw);
    expect(await run({ command: "all", root, check: true }, log, generators)).toBe(1);
    expect(await run({ command: "all", root, check: false }, log, generators)).toBe(0);
    expect(await run({ command: "all", root, check: true }, log, generators)).toBe(0);
  });

  it("removes stale files on write but leaves directories shared with unselected targets alone", async () => {
    await run({ command: "all", root, check: false }, log, generators);
    await writeFile(path.join(root, "out/tokens/old.txt"), "stale\n");
    await writeFile(path.join(root, "out/tokens/.gitkeep"), "");
    expect(await run({ command: "tokens", root, check: false }, log, generators)).toBe(0);
    await expect(readFile(path.join(root, "out/tokens/old.txt"))).rejects.toThrow();
    await expect(readFile(path.join(root, "out/tokens/.gitkeep"))).resolves.toBeDefined();
    await expect(readFile(path.join(root, "out/shared/icons.txt"), "utf8")).resolves.toBe("home\n");
    expect(await run({ command: "tokens", root, check: true }, log, generators)).toBe(0);
  });

  it("selects generators by target", async () => {
    expect(await run({ command: "icons", root, check: false }, log, generators)).toBe(0);
    expect(lines[0]).toBe("codegen icons: 1 generators (fixture-icons)");
    await expect(readFile(path.join(root, "out/tokens/colors.txt"))).rejects.toThrow();
  });

  it("passes with no registered generators", async () => {
    expect(await run({ command: "all", root, check: true }, log, [])).toBe(0);
    expect(lines).toEqual(["codegen all: 0 generators (none registered)", "codegen: 0 generated files are up to date"]);
  });

  it("rejects outputs outside the generator directory or generated twice", async () => {
    const escaping: Generator = { ...tokensGenerator, generate: () => [{ path: "out/../ios/x.swift", contents: "" }] };
    await expect(run({ command: "all", root, check: false }, log, [escaping])).rejects.toThrow(/outside its output directory/);
    const twice: Generator = { ...iconsGenerator, outputDir: "out/tokens", target: "tokens" };
    const dup: Generator = { ...twice, name: "dup", generate: () => [{ path: "out/tokens/colors.txt", contents: "" }] };
    await expect(run({ command: "all", root, check: false }, log, [tokensGenerator, dup])).rejects.toThrow(/generated twice/);
  });

  it("rejects generators whose output directories nest", async () => {
    const nested: Generator = { ...iconsGenerator, name: "fixture-nested", outputDir: "out/tokens/icons", generate: () => [] };
    await expect(run({ command: "icons", root, check: false }, log, [tokensGenerator, nested])).rejects.toThrow(
      "fixture-nested: output directory out/tokens/icons is nested inside out/tokens of fixture-tokens",
    );
  });

  it("fails generation when tokens.json is invalid", async () => {
    const raw = rawFixture();
    raw.color.tokens.push({ name: "broken", value: "{missing}" });
    await writeTokens(raw);
    await expect(run({ command: "all", root, check: true }, log, generators)).rejects.toThrow(/unknown alias target/);
  });
});

describe("codegen model and diff-report", () => {
  it("prints a model summary", async () => {
    expect(await run({ command: "model", root, json: false }, log, [])).toBe(0);
    expect(lines[0]).toContain("color 5x2, typography 3, spacing 2, radius 2, shadow 1, size 1, opacity 1");
    expect(lines[0]).toContain("aliases: focus -> {accent-text}");
    expect(lines[0]).toContain("dropped zIndex (2)");
  });

  it("reports a rename against a base file, as major without and minor with a renames file", async () => {
    const base = path.join(root, "base.json");
    await copyFile(fixtureTokensPath, base);
    const raw = rawFixture();
    raw.size.tokens[0].name = "control-default";
    await writeTokens(raw);
    expect(await run({ command: "diff-report", root, json: false, base }, log, [])).toBe(0);
    expect(lines[0]).toMatch(/^diff-report: suggested bump major/);
    const renames = path.join(root, "native-tokens.json");
    await writeFile(renames, JSON.stringify({ renames: { "control-md": "control-default" } }));
    lines = [];
    await run({ command: "diff-report", root, json: true, base, renames }, log, []);
    expect(JSON.parse(lines[0] ?? "").bump).toBe("minor");
  });

  it("merges design/extensions into the model for the summary and the generators", async () => {
    await addExtensions();
    expect(await run({ command: "model", root, json: false }, log, [])).toBe(0);
    expect(lines[0]).toContain("color 8x2, typography 3, spacing 2, radius 2, shadow 1, size 2, opacity 1, duration 2, easing 1, component 4");
    expect(lines[0]).toContain("extensions: Fixture extensions v1, upstream pending 9, accepted 1, rejected 1");
    expect(lines[0]).toContain("deprecated aliases: none");
    lines = [];
    expect(await run({ command: "tokens", root, check: false }, log, [tokensGenerator])).toBe(0);
    expect(await readFile(path.join(root, "out/tokens/colors.txt"), "utf8")).toContain("dangerPressed=#b91c1c\nswitchThumb=#ffffff\n");
  });

  it("lists deprecated aliases and fails on an invalid extensions file", async () => {
    await addExtensions({ name: "x", version: 2, renames: { "control-default": "control-md" } });
    await run({ command: "model", root, json: false }, log, []);
    expect(lines[0]).toContain("deprecated aliases: size/control-default -> control-md");
    await addExtensions({ color: { tokens: [{ name: "overlay", value: "#000", upstream: "pending" }] } });
    await expect(run({ command: "model", root, json: false }, log, [])).rejects.toThrow(/duplicate key, already defined/);
  });

  it("takes diff-report renames from design/extensions when --renames is not given", async () => {
    const base = path.join(root, "base.json");
    await copyFile(fixtureTokensPath, base);
    const raw = rawFixture();
    raw.size.tokens[0].name = "control-default";
    await writeTokens(raw);
    await addExtensions({ renames: { "control-md": "control-default" } });
    expect(await run({ command: "diff-report", root, json: true, base }, log, [])).toBe(0);
    const report = JSON.parse(lines[0] ?? "");
    expect(report.bump).toBe("minor");
    expect(report.changes[0]).toMatchObject({ kind: "renamed", name: "control-md", renamedTo: "control-default" });
  });

  it("reads the base tokens from a git ref", async () => {
    const git = (...args: string[]) => execFileSync("git", ["-C", root, ...args], { stdio: "pipe" });
    git("init", "-q");
    git("add", "design/source/tokens.json");
    git("-c", "user.email=t@example.com", "-c", "user.name=t", "commit", "-q", "-m", "base");
    const raw = rawFixture();
    raw.spacing.tokens.push({ name: "space-20", value: "80px" });
    await writeTokens(raw);
    expect(await run({ command: "diff-report", root, json: false, baseRef: "HEAD" }, log, [])).toBe(0);
    expect(lines[0]).toBe(["diff-report: suggested bump minor (1 token changes)", "  minor  added       spacing/space-20: 80"].join("\n"));
  });
});
