import { execFile } from "node:child_process";
import { mkdtemp, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { callsOf, stubTools, type StubTools } from "./stub-tools.ts";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const run = promisify(execFile);

async function dryRun(target: string, ...variables: string[]): Promise<string> {
  const { stdout } = await run("make", ["-n", "-s", target, ...variables], { cwd: repoRoot });
  return stdout;
}

const RELEASE = ["npm", "--prefix", "tools", "run", "--silent", "release", "--"];

describe("release targets", () => {
  let stubs: StubTools;

  beforeAll(async () => {
    stubs = await stubTools();
  });

  afterAll(async () => {
    await stubs.dispose();
  });

  it("passes V to the release tool, and --dry-run only with DRY_RUN", async () => {
    expect(callsOf(await stubs.run("release-bump", { env: { V: "1.2.3", DRY_RUN: "" } }), "npm", "release")).toEqual([[...RELEASE, "bump", "--version", "1.2.3"]]);
    expect(callsOf(await stubs.run("release-bump", { env: { V: "1.2.3", DRY_RUN: "1" } }), "npm", "release")).toEqual([[...RELEASE, "bump", "--version", "1.2.3", "--dry-run"]]);
  });

  it("verifies the release, passing the branch ref only when it is set, and writes its notes", async () => {
    expect(callsOf(await stubs.run("release-verify", { env: { TAG: "1.2.3", RELEASE_REF: "refs/heads/master" } }), "npm", "release")).toEqual([[...RELEASE, "verify", "--tag", "1.2.3", "--ref", "refs/heads/master"]]);
    expect(callsOf(await stubs.run("release-verify", { env: { TAG: "1.2.3", RELEASE_REF: "" } }), "npm", "release")).toEqual([[...RELEASE, "verify", "--tag", "1.2.3"]]);
    expect(callsOf(await stubs.run("release-notes", { env: { TAG: "1.2.3" } }), "npm", "release")).toEqual([[...RELEASE, "notes", "--tag", "1.2.3", "--out", ".build/release/notes.md"]]);
  });

  it("creates the tag at the released commit with the GitHub release, from the notes", async () => {
    const result = await stubs.run("release-github", { env: { TAG: "1.2.3", GITHUB_SHA: "0123abcd", RELEASE_TARGET: "" } });
    expect(result.calls.filter((call) => call.includes("notes") || call[0] === "gh")).toEqual([
      [...RELEASE, "notes", "--tag", "1.2.3", "--out", ".build/release/notes.md"],
      ["gh", "release", "create", "1.2.3", "--target", "0123abcd", "--title", "1.2.3", "--notes-file", ".build/release/notes.md"],
    ]);
  });

  it("prefers RELEASE_TARGET over GITHUB_SHA", async () => {
    const result = await stubs.run("release-github", { env: { TAG: "1.2.3", GITHUB_SHA: "0123abcd", RELEASE_TARGET: "fedc" } });
    expect(callsOf(result, "gh")).toEqual([["gh", "release", "create", "1.2.3", "--target", "fedc", "--title", "1.2.3", "--notes-file", ".build/release/notes.md"]]);
  });

  it("refuses to create a release without a target commit, before calling gh", async () => {
    const result = await stubs.run("release-github", { env: { TAG: "1.2.3", GITHUB_SHA: "", RELEASE_TARGET: "" } });
    expect(result.code).not.toBe(0);
    expect(result.output).toContain("release-github: RELEASE_TARGET (default GITHUB_SHA) must name the commit to tag");
    expect(callsOf(result, "gh")).toEqual([]);
  });
});

describe("release CI targets", () => {
  it("publishes to GitHub Packages through the re-runnable tool, only with the CI credentials, and to mavenLocal without", async () => {
    const publish = await dryRun("android-publish", "GRADLE_WORKERS=2");
    expect(publish).toContain('test -n "$GITHUB_REPOSITORY" -a -n "$GITHUB_ACTOR" -a -n "$GITHUB_TOKEN"');
    expect(publish).toContain("npm --prefix tools run --silent publish-android -- --workers '2'\n");
    expect(publish).not.toContain("./gradlew");
    expect(await dryRun("android-publish", "GRADLE_WORKERS=")).toContain("npm --prefix tools run --silent publish-android --\n");
    expect(await dryRun("android-publish-local")).toContain("cd android && ./gradlew publishToMavenLocal\n");
  });

  it("fails android-publish with a clear message when the credentials are missing", async () => {
    const env = { ...process.env, GITHUB_REPOSITORY: "", GITHUB_ACTOR: "", GITHUB_TOKEN: "" };
    await expect(run("make", ["-s", "android-publish"], { cwd: repoRoot, env })).rejects.toMatchObject({ stderr: expect.stringContaining("GITHUB_REPOSITORY, GITHUB_ACTOR and GITHUB_TOKEN must be set") });
  });
});

describe("LAST_TAG_COMMAND", () => {
  let repo: string;
  const git = (...args: string[]) => run("git", ["-c", "user.name=t", "-c", "user.email=t@example.com", ...args], { cwd: repo });
  const lastTag = async () => {
    await writeFile(path.join(repo, "print-last-tag.mk"), "print-last-tag:\n\t@printf '%s\\n' \"$$($(LAST_TAG_COMMAND))\"\n");
    return (await run("make", ["-s", "-f", path.join(repoRoot, "Makefile"), "-f", "print-last-tag.mk", "print-last-tag"], { cwd: repo })).stdout.trim();
  };

  beforeEach(async () => {
    repo = await mkdtemp(path.join(tmpdir(), "make-last-tag-"));
    await git("init", "-q");
    await git("commit", "-q", "--allow-empty", "-m", "first");
  });

  afterEach(async () => {
    await rm(repo, { recursive: true, force: true });
  });

  it("is the previous release tag on a tagged release commit, not the tag being released", async () => {
    await git("tag", "0.1.0");
    await git("commit", "-q", "--allow-empty", "-m", "second");
    await git("tag", "0.2.0");
    expect(await lastTag()).toBe("0.1.0");
  });

  it("is the latest release tag on an untagged commit, ignoring non-release tags", async () => {
    await git("tag", "0.1.0");
    await git("commit", "-q", "--allow-empty", "-m", "second");
    await git("tag", "experiment");
    await git("commit", "-q", "--allow-empty", "-m", "third");
    expect(await lastTag()).toBe("0.1.0");
  });

  it("is empty before the first release", async () => {
    await git("tag", "0.1.0");
    expect(await lastTag()).toBe("");
  });
});
