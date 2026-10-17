import { execFile } from "node:child_process";
import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { describe, expect, it } from "vitest";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const run = promisify(execFile);

async function dryRun(target: string, ...variables: string[]): Promise<string> {
  const { stdout } = await run("make", ["-n", "-s", target, "GRADLE_WORKERS=", ...variables], { cwd: repoRoot });
  return stdout;
}

async function rule(name: string): Promise<string | undefined> {
  const makefile = await readFile(path.join(repoRoot, "Makefile"), "utf8");
  return makefile.split("\n").find((line) => line.startsWith(`${name}:`));
}

describe("make android-snapshot-record", () => {
  it("records the Roborazzi references", async () => {
    expect(await dryRun("android-snapshot-record")).toBe("cd android && ./gradlew recordRoborazziDebug\n");
  });

  it("has no prerequisite", async () => {
    expect((await rule("android-snapshot-record"))?.trim()).toBe("android-snapshot-record:");
  });
});

describe("make android-test and android-snapshot-verify", () => {
  it("runs the tests without comparing snapshots in android-test", async () => {
    expect(await dryRun("android-test")).toBe("cd android && ./gradlew test\n");
    expect((await rule("android-test"))?.trim()).toBe("android-test:");
  });

  it("compares the snapshots with the local references only in android-snapshot-verify", async () => {
    expect(await dryRun("android-snapshot-verify")).toBe("cd android && ./gradlew test -Proborazzi.test.verify=true\n");
  });

  it("keeps the snapshot verify out of make test", async () => {
    expect((await rule("test"))?.split(/\s+/)).not.toContain("android-snapshot-verify");
  });
});
