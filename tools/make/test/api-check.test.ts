import { execFile } from "node:child_process";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { callsOf, stubTools, type StubTools } from "./stub-tools.ts";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const run = promisify(execFile);

async function dryRun(target: string, ...variables: string[]): Promise<string> {
  const { stdout } = await run("make", ["-n", "-s", target, ...variables], { cwd: repoRoot });
  return stdout;
}

describe("make api-check", () => {
  let stubs: StubTools;

  beforeAll(async () => {
    stubs = await stubTools();
  });

  afterAll(async () => {
    await stubs.dispose();
  });

  it("runs the iOS check first, then the Android check against the in-tree dumps", async () => {
    const output = await dryRun("api-check", "BASELINE=1.0.0");
    const ios = output.indexOf('npm --prefix tools run --silent api-check-ios -- ${BASELINE:+--baseline "$BASELINE"}');
    const android = output.indexOf("cd android && ./gradlew apiCheck");
    expect(ios).toBeGreaterThanOrEqual(0);
    expect(android).toBeGreaterThan(ios);
  });

  it("passes BASELINE to the iOS tool, and leaves a missing one for the tool to reject", async () => {
    expect(callsOf(await stubs.run("ios-api-check", { env: { BASELINE: "1.0.0" } }), "npm", "api-check-ios")).toEqual([["npm", "--prefix", "tools", "run", "--silent", "api-check-ios", "--", "--baseline", "1.0.0"]]);
    expect(callsOf(await stubs.run("ios-api-check", { env: { BASELINE: "" } }), "npm", "api-check-ios")).toEqual([["npm", "--prefix", "tools", "run", "--silent", "api-check-ios", "--"]]);
  });

  it("records the acknowledged iOS breakages against BASELINE", async () => {
    const result = await stubs.run("ios-api-check-record", { env: { BASELINE: "origin/master" } });
    expect(callsOf(result, "npm", "api-check-ios")).toEqual([["npm", "--prefix", "tools", "run", "--silent", "api-check-ios", "--", "--baseline", "origin/master", "--record"]]);
  });

  it("keeps apiCheck in android-build", async () => {
    const output = await dryRun("android-build");
    expect(output).toContain("./gradlew assemble lint apiCheck");
  });
});
