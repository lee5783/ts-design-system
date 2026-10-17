import { execFile } from "node:child_process";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { describe, expect, it } from "vitest";
import { evaluateNeeds, parseNeeds } from "../src/required.ts";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const run = promisify(execFile);

function verdict(results: Record<string, string>): boolean {
  return evaluateNeeds(Object.fromEntries(Object.entries(results).map(([name, result]) => [name, { result }]))).ok;
}

describe("evaluateNeeds", () => {
  it("passes when every needed job succeeded", () => {
    expect(verdict({ changes: "success", test: "success", "api-check": "success" })).toBe(true);
  });

  it("passes a job the path filter skipped, when changes succeeded", () => {
    expect(verdict({ changes: "success", test: "skipped", "api-check": "skipped" })).toBe(true);
  });

  it("fails when changes failed, even though every other job was skipped", () => {
    expect(verdict({ changes: "failure", test: "skipped", "api-check": "skipped" })).toBe(false);
  });

  it("fails when changes was cancelled or skipped", () => {
    expect(verdict({ changes: "cancelled", test: "skipped" })).toBe(false);
    expect(verdict({ changes: "skipped", test: "skipped" })).toBe(false);
  });

  it("fails when any needed job failed or was cancelled", () => {
    expect(verdict({ changes: "success", test: "failure", "api-check": "success" })).toBe(false);
    expect(verdict({ changes: "success", test: "success", "api-check": "cancelled" })).toBe(false);
  });

  it("does not accept a skip in a workflow that has no changes job", () => {
    expect(verdict({ tools: "success" })).toBe(true);
    expect(verdict({ tools: "skipped" })).toBe(false);
  });

  it("names each job and why it failed", () => {
    const { lines } = evaluateNeeds({ changes: { result: "failure" }, test: { result: "skipped" } });
    expect(lines).toEqual(["  changes: failure", "  test: skipped because changes did not succeed (failure)"]);
  });
});

describe("parseNeeds", () => {
  it("reads the toJSON(needs) shape GitHub passes", () => {
    const json = JSON.stringify({ changes: { result: "success", outputs: { ios: "true" } }, test: { result: "skipped", outputs: {} } });
    expect(parseNeeds(json)).toEqual({ changes: { result: "success" }, test: { result: "skipped" } });
  });

  it("refuses empty or malformed input instead of passing", () => {
    expect(() => parseNeeds(undefined)).toThrow("NEEDS is empty");
    expect(() => parseNeeds("{}")).toThrow("NEEDS lists no jobs");
    expect(() => parseNeeds("[]")).toThrow("not a JSON object");
    expect(() => parseNeeds(JSON.stringify({ test: {} }))).toThrow("NEEDS.test has no result");
  });
});

describe("make ci-required", () => {
  async function required(needs: string): Promise<{ code: number; output: string }> {
    try {
      const { stdout, stderr } = await run("make", ["-s", "ci-required"], { cwd: repoRoot, env: { ...process.env, NEEDS: needs } });
      return { code: 0, output: stdout + stderr };
    } catch (error) {
      const failure = error as { code: number; stdout: string; stderr: string };
      return { code: failure.code, output: failure.stdout + failure.stderr };
    }
  }

  it("exits 0 for a filtered skip and non-zero for a failed changes job", async () => {
    const pass = await required(JSON.stringify({ changes: { result: "success" }, test: { result: "skipped" } }));
    expect(pass.code).toBe(0);
    expect(pass.output).toContain("test: skipped (no change in its area)");
    const fail = await required(JSON.stringify({ changes: { result: "failure" }, test: { result: "skipped" } }));
    expect(fail.code).not.toBe(0);
    expect(fail.output).toContain("test: skipped because changes did not succeed (failure)");
  });
});
