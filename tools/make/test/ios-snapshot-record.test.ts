import { execFile } from "node:child_process";
import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { describe, expect, it } from "vitest";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const run = promisify(execFile);

async function dryRun(...variables: string[]): Promise<string> {
  const { stdout } = await run("make", ["-n", "-s", "ios-snapshot-record", ...variables], { cwd: repoRoot });
  return stdout;
}

describe("make ios-snapshot-record", () => {
  it("records only the snapshot test targets on the pinned simulator", async () => {
    const output = await dryRun();
    expect(output).toContain("TEST_RUNNER_TS_SNAPSHOT_RECORD=1 xcodebuild test");
    expect(output).toContain("platform=iOS Simulator,name=iPhone 17,OS=26.5");
    expect(output).toContain("-only-testing:TSDesignSystemTests -only-testing:TSDesignSystemUIKitTests");
  });

  it("never records on the oldest-runtime destination", async () => {
    const output = await dryRun("XCRUN=false");
    expect(output.match(/xcodebuild test/g)).toHaveLength(1);
  });

  it("keeps the snapshot suites out of make test", async () => {
    const makefile = await readFile(path.join(repoRoot, "Makefile"), "utf8");
    const rule = makefile.split("\n").find((line) => line.startsWith("test:"));
    expect(rule?.split(/\s+/)).not.toContain("ios-snapshot-verify");
    expect(rule?.split(/\s+/)).not.toContain("ios-snapshot-record");
  });
});
