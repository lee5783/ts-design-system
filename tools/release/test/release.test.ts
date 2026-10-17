import { execFile } from "node:child_process";
import { chmod, mkdtemp, readFile, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { promisify } from "node:util";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { GIT_CLIFF_INSTALL_HINT, ReleaseError, bump, changelogSection, compareVersions, gitCliffCandidates, parseVersion, releaseTags, tagExists, validateBump, verifyReleaseContext, verifyTag } from "../src/release.ts";

const exec = promisify(execFile);

const CHANGELOG = ["# Changelog", "", "## 1.1.0 - 2026-11-01", "", "### Features", "", "- Add chips", "", "## 1.0.0 - 2026-10-20", "", "### Fixes", "", "- Fix badge", ""].join("\n");

describe("parseVersion", () => {
  it("accepts plain X.Y.Z versions", () => {
    expect(parseVersion("0.1.0")).toEqual({ major: 0, minor: 1, patch: 0 });
    expect(parseVersion(" 12.0.34\n")).toEqual({ major: 12, minor: 0, patch: 34 });
  });

  it("rejects anything SPM cannot resolve as a plain tag", () => {
    for (const text of ["v1.2.3", "1.2", "1.2.3.4", "1.2.3-beta.1", "1.2.3+build", "01.2.3", "1.02.3", "a.b.c", ""]) {
      expect(() => parseVersion(text), text).toThrow(ReleaseError);
    }
  });
});

describe("validateBump", () => {
  it("orders versions numerically, not as text", () => {
    expect(compareVersions(parseVersion("0.10.0"), parseVersion("0.9.9"))).toBeGreaterThan(0);
    expect(compareVersions(parseVersion("1.0.0"), parseVersion("0.99.99"))).toBeGreaterThan(0);
  });

  it("accepts a patch, minor or major bump", () => {
    expect(validateBump("0.1.0", "0.1.1")).toEqual({ major: 0, minor: 1, patch: 1 });
    expect(validateBump("0.1.0", "0.2.0")).toEqual({ major: 0, minor: 2, patch: 0 });
    expect(validateBump("0.1.0", "1.0.0")).toEqual({ major: 1, minor: 0, patch: 0 });
  });

  it("refuses a version that is not greater than the current one", () => {
    expect(() => validateBump("0.1.0", "0.1.0")).toThrow("0.1.0 is not greater than the current version 0.1.0");
    expect(() => validateBump("0.2.0", "0.1.9")).toThrow("not greater");
  });

  it("allows the current version only for the first release, before any release tag exists", () => {
    expect(validateBump("0.1.0", "0.1.0", true)).toEqual({ major: 0, minor: 1, patch: 0 });
    expect(() => validateBump("0.1.0", "0.0.9", true)).toThrow("not greater");
  });

  it("requires V", () => {
    expect(() => validateBump("0.1.0", undefined)).toThrow("V is required");
    expect(() => validateBump("0.1.0", " ")).toThrow("V is required");
  });
});

describe("changelogSection", () => {
  it("returns one version's section without the next one", () => {
    expect(changelogSection(CHANGELOG, "1.1.0")).toBe("## 1.1.0 - 2026-11-01\n\n### Features\n\n- Add chips\n");
    expect(changelogSection(CHANGELOG, "1.0.0")).toBe("## 1.0.0 - 2026-10-20\n\n### Fixes\n\n- Fix badge\n");
  });

  it("does not match a version that only shares a prefix", () => {
    expect(changelogSection(CHANGELOG, "1.1")).toBeUndefined();
    expect(changelogSection(CHANGELOG, "1.1.00")).toBeUndefined();
    expect(changelogSection(CHANGELOG, "2.0.0")).toBeUndefined();
  });
});

describe("verifyTag", () => {
  it("passes when the tag equals VERSION and the changelog has its section", () => {
    expect(verifyTag("v1.1.0", "1.1.0\n", CHANGELOG)).toBe("1.1.0");
  });

  it("fails when the tag differs from VERSION", () => {
    expect(() => verifyTag("v1.1.0", "1.0.0\n", CHANGELOG)).toThrow("tag v1.1.0 does not match VERSION 1.0.0");
  });

  it("fails for a tag that is not plain vX.Y.Z", () => {
    expect(() => verifyTag("1.1.0", "1.1.0", CHANGELOG)).toThrow(ReleaseError);
    expect(() => verifyTag("v1.1.0-rc.1", "1.1.0", CHANGELOG)).toThrow(ReleaseError);
  });

  it("fails when the changelog has no section for the tag", () => {
    expect(() => verifyTag("v1.2.0", "1.2.0", CHANGELOG)).toThrow('CHANGELOG.md has no "## 1.2.0" section');
  });

  it("requires TAG", () => {
    expect(() => verifyTag(undefined, "1.1.0", CHANGELOG)).toThrow("TAG is required");
  });
});

describe("gitCliffCandidates", () => {
  it("prefers an installed git-cliff and falls back to an exact npm package version through npx", () => {
    expect(gitCliffCandidates({})).toEqual([
      { file: "git-cliff", args: [] },
      { file: "npx", args: ["--yes", "git-cliff@2.14.2"] },
    ]);
  });

  it("uses only GIT_CLIFF when it is set", () => {
    expect(gitCliffCandidates({ GIT_CLIFF: "/opt/git-cliff" })).toEqual([{ file: "/opt/git-cliff", args: [] }]);
  });
});

describe("bump", () => {
  let root: string;
  let fakeCliff: string;
  const generated = "# Changelog\n\n## 0.2.0 - 2026-10-16\n\n### Features\n\n- Add chips\n\n## 0.1.0 - 2026-10-01\n\n### Other\n\n- Initial commit\n";

  beforeEach(async () => {
    root = await mkdtemp(path.join(tmpdir(), "release-bump-"));
    await exec("git", ["init", "-q"], { cwd: root });
    await writeFile(path.join(root, "VERSION"), "0.1.0\n");
    await writeFile(path.join(root, "CHANGELOG.md"), "# Changelog\n");
    fakeCliff = path.join(root, "..", `${path.basename(root)}-git-cliff`);
    await writeFile(fakeCliff, `#!/bin/sh\nif [ "$1" = "--version" ]; then echo 'git-cliff 2.0.0'; exit 0; fi\necho "$@" > "${root}/cliff-args"\nprintf '%s' '${generated}'\n`);
    await chmod(fakeCliff, 0o755);
  });

  afterEach(async () => {
    await rm(root, { recursive: true, force: true });
    await rm(fakeCliff, { force: true });
  });

  it("runs git-cliff with the repo config and the new tag, and writes VERSION and CHANGELOG.md", async () => {
    const result = await bump(root, "0.2.0", false, { GIT_CLIFF: fakeCliff });
    expect(result).toMatchObject({ from: "0.1.0", to: "0.2.0", written: true });
    expect(result.section).toBe("## 0.2.0 - 2026-10-16\n\n### Features\n\n- Add chips\n");
    expect((await readFile(path.join(root, "cliff-args"), "utf8")).trim()).toBe("--config cliff.toml --tag 0.2.0");
    expect(await readFile(path.join(root, "VERSION"), "utf8")).toBe("0.2.0\n");
    expect(await readFile(path.join(root, "CHANGELOG.md"), "utf8")).toBe(generated);
  });

  it("changes nothing in a dry run", async () => {
    const result = await bump(root, "0.2.0", true, { GIT_CLIFF: fakeCliff });
    expect(result.written).toBe(false);
    expect(result.section).toContain("## 0.2.0");
    expect(await readFile(path.join(root, "VERSION"), "utf8")).toBe("0.1.0\n");
    expect(await readFile(path.join(root, "CHANGELOG.md"), "utf8")).toBe("# Changelog\n");
  });

  it("refuses a version whose tag already exists", async () => {
    await exec("git", ["-c", "user.name=t", "-c", "user.email=t@example.com", "commit", "-q", "--allow-empty", "-m", "init"], { cwd: root });
    await exec("git", ["tag", "0.2.0"], { cwd: root });
    await expect(bump(root, "0.2.0", true, { GIT_CLIFF: fakeCliff })).rejects.toThrow("tag v0.2.0 already exists");
  });

  it("writes the first release at the current VERSION when no release tag exists", async () => {
    const result = await bump(root, "0.1.0", true, { GIT_CLIFF: fakeCliff });
    expect(result).toMatchObject({ from: "0.1.0", to: "0.1.0" });
  });

  it("refuses the current VERSION once a release tag exists", async () => {
    await exec("git", ["-c", "user.name=t", "-c", "user.email=t@example.com", "commit", "-q", "--allow-empty", "-m", "init"], { cwd: root });
    await exec("git", ["tag", "0.0.1"], { cwd: root });
    await expect(bump(root, "0.1.0", true, { GIT_CLIFF: fakeCliff })).rejects.toThrow("0.1.0 is not greater than the current version 0.1.0");
  });

  it("refuses a version that is not greater before running git-cliff", async () => {
    await expect(bump(root, "0.0.9", true, { GIT_CLIFF: path.join(root, "missing") })).rejects.toThrow("not greater");
  });

  it("explains how to install git-cliff when it cannot run", async () => {
    await expect(bump(root, "0.2.0", true, { GIT_CLIFF: path.join(root, "missing") })).rejects.toThrow(GIT_CLIFF_INSTALL_HINT);
    expect(GIT_CLIFF_INSTALL_HINT).toContain("mise install");
    expect(GIT_CLIFF_INSTALL_HINT).toContain("brew install git-cliff");
  });

  it("fails when git-cliff produces no section for the new version", async () => {
    await writeFile(fakeCliff, `#!/bin/sh\nif [ "$1" = "--version" ]; then exit 0; fi\nprintf '# Changelog\\n'\n`);
    await expect(bump(root, "0.2.0", true, { GIT_CLIFF: fakeCliff })).rejects.toThrow('git-cliff produced no "## 0.2.0" section');
  });
});

describe("tags on origin", () => {
  let base: string;
  let clone: string;
  const git = (cwd: string, ...args: string[]) => exec("git", ["-c", "user.name=t", "-c", "user.email=t@example.com", ...args], { cwd });

  beforeEach(async () => {
    base = await mkdtemp(path.join(tmpdir(), "release-origin-"));
    await git(base, "init", "-q", "--bare", "-b", "master", "origin.git");
    await git(base, "init", "-q", "-b", "master", "seed");
    const seed = path.join(base, "seed");
    await writeFile(path.join(seed, "VERSION"), "0.1.0\n");
    await git(seed, "add", "VERSION");
    await git(seed, "commit", "-q", "-m", "first");
    await git(seed, "tag", "0.1.0");
    await git(seed, "remote", "add", "origin", path.join(base, "origin.git"));
    await git(seed, "push", "-q", "origin", "master", "0.1.0");
    clone = path.join(base, "clone");
    await git(base, "clone", "-q", "--no-tags", path.join(base, "origin.git"), "clone");
  });

  afterEach(async () => {
    await rm(base, { recursive: true, force: true });
  });

  it("sees a release tag that exists only on origin, as in a clone without fetched tags", async () => {
    expect((await exec("git", ["tag", "--list"], { cwd: clone })).stdout).toBe("");
    expect(await releaseTags(clone)).toEqual(["0.1.0"]);
    expect(await tagExists(clone, "0.1.0")).toBe(true);
  });

  it("refuses the current VERSION as a first release when origin already has a release tag", async () => {
    await expect(bump(clone, "0.1.0", true, { GIT_CLIFF: path.join(base, "missing") })).rejects.toThrow("0.1.0 is not greater than the current version 0.1.0");
  });

  it("fails instead of guessing when origin cannot be reached", async () => {
    await git(clone, "remote", "set-url", "origin", path.join(base, "gone.git"));
    await expect(releaseTags(clone)).rejects.toThrow("could not list the tags on origin");
  });
});

describe("verifyReleaseContext", () => {
  let base: string;
  let clone: string;
  const git = (cwd: string, ...args: string[]) => exec("git", ["-c", "user.name=t", "-c", "user.email=t@example.com", ...args], { cwd });

  beforeEach(async () => {
    base = await mkdtemp(path.join(tmpdir(), "release-context-"));
    await git(base, "init", "-q", "--bare", "-b", "master", "origin.git");
    await git(base, "clone", "-q", path.join(base, "origin.git"), "clone");
    clone = path.join(base, "clone");
    await git(clone, "checkout", "-q", "-b", "master");
    await git(clone, "commit", "-q", "--allow-empty", "-m", "first");
    await git(clone, "push", "-q", "origin", "master");
    await git(clone, "fetch", "-q", "origin");
  });

  afterEach(async () => {
    await rm(base, { recursive: true, force: true });
  });

  it("passes for the pushed tag on a commit that is on master", async () => {
    await expect(verifyReleaseContext(clone, "v0.2.0", "refs/tags/v0.2.0")).resolves.toBeUndefined();
  });

  it("refuses a run that was not started by the release tag", async () => {
    await expect(verifyReleaseContext(clone, "v0.2.0", "refs/heads/master")).rejects.toThrow("releases run only from the pushed tag refs/tags/v0.2.0, not refs/heads/master");
    await expect(verifyReleaseContext(clone, "v0.2.0", "refs/tags/v0.3.0")).rejects.toThrow(ReleaseError);
  });

  it("refuses a commit that is not on origin/master", async () => {
    await git(clone, "checkout", "-q", "-b", "unmerged");
    await git(clone, "commit", "-q", "--allow-empty", "-m", "unmerged");
    await expect(verifyReleaseContext(clone, "v0.2.0", undefined)).rejects.toThrow("HEAD is not on origin/master");
  });
});
