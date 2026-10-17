import { execFile } from "node:child_process";
import { createHash } from "node:crypto";
import { mkdir, mkdtemp, readdir, readFile, rm, writeFile, chmod } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { promisify } from "node:util";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { ACTIONLINT_SHA256, ACTIONLINT_VERSION, binaryPath, ensureActionlint, releaseAsset, releasePlatform } from "../src/actionlint.ts";

const run = promisify(execFile);

let dir: string;

beforeEach(async () => {
  dir = await mkdtemp(path.join(tmpdir(), "actionlint-"));
});

afterEach(async () => {
  await rm(dir, { recursive: true, force: true });
});

async function fakeRelease(): Promise<Buffer> {
  const source = path.join(dir, "release");
  await mkdir(source, { recursive: true });
  await writeFile(path.join(source, "actionlint"), "#!/bin/sh\necho fake-actionlint \"$@\"\n");
  await chmod(path.join(source, "actionlint"), 0o755);
  await writeFile(path.join(source, "LICENSE.txt"), "MIT\n");
  const archive = path.join(dir, "release.tar.gz");
  await run("tar", ["-czf", archive, "-C", source, "actionlint", "LICENSE.txt"]);
  return readFile(archive);
}

const sha256 = (bytes: Buffer) => createHash("sha256").update(bytes).digest("hex");

describe("pinned release", () => {
  it("maps the Node platform to the release asset name", () => {
    expect(releasePlatform("darwin", "arm64")).toBe("darwin_arm64");
    expect(releasePlatform("darwin", "x64")).toBe("darwin_amd64");
    expect(releasePlatform("linux", "x64")).toBe("linux_amd64");
    expect(releasePlatform("linux", "arm64")).toBe("linux_arm64");
    expect(() => releasePlatform("win32", "x64")).toThrow("no pinned actionlint build for win32/x64");
  });

  it("downloads a fixed version from the rhysd/actionlint GitHub release", () => {
    expect(releaseAsset("1.7.12", "linux_amd64")).toEqual({
      name: "actionlint_1.7.12_linux_amd64.tar.gz",
      url: "https://github.com/rhysd/actionlint/releases/download/v1.7.12/actionlint_1.7.12_linux_amd64.tar.gz",
    });
  });

  it("pins a SHA-256 for every supported platform", () => {
    expect(ACTIONLINT_VERSION).toMatch(/^\d+\.\d+\.\d+$/);
    expect(Object.keys(ACTIONLINT_SHA256).sort()).toEqual(["darwin_amd64", "darwin_arm64", "linux_amd64", "linux_arm64"]);
    for (const checksum of Object.values(ACTIONLINT_SHA256)) expect(checksum).toMatch(/^[0-9a-f]{64}$/);
  });
});

describe("ensureActionlint", () => {
  it("installs a verified download into the cache, then reuses it without downloading again", async () => {
    const archive = await fakeRelease();
    const urls: string[] = [];
    const download = async (url: string) => {
      urls.push(url);
      return archive;
    };
    const cacheDir = path.join(dir, "cache");
    const options = { cacheDir, version: "9.9.9", platform: "linux_amd64", checksums: { linux_amd64: sha256(archive) }, download };
    const first = await ensureActionlint(options);
    expect(first).toEqual({ binary: binaryPath(cacheDir, "9.9.9", "linux_amd64"), downloaded: true });
    expect((await run(first.binary, ["-color"])).stdout).toBe("fake-actionlint -color\n");
    expect(await ensureActionlint(options)).toEqual({ binary: first.binary, downloaded: false });
    expect(urls).toEqual(["https://github.com/rhysd/actionlint/releases/download/v9.9.9/actionlint_9.9.9_linux_amd64.tar.gz"]);
    expect((await readdir(cacheDir)).filter((entry) => entry.startsWith(".staging-"))).toEqual([]);
  });

  it("refuses a download whose checksum differs from the pin, and installs nothing", async () => {
    const archive = await fakeRelease();
    const cacheDir = path.join(dir, "cache");
    await expect(ensureActionlint({ cacheDir, version: "9.9.9", platform: "linux_amd64", checksums: { linux_amd64: "0".repeat(64) }, download: async () => archive })).rejects.toThrow(
      `actionlint_9.9.9_linux_amd64.tar.gz has SHA-256 ${sha256(archive)}, expected the pinned ${"0".repeat(64)}; nothing was installed`,
    );
    await expect(readdir(path.join(cacheDir, "9.9.9"))).rejects.toThrow();
  });

  it("refuses a platform without a pinned checksum before downloading", async () => {
    let downloaded = false;
    await expect(
      ensureActionlint({
        cacheDir: path.join(dir, "cache"),
        platform: "freebsd_amd64",
        download: async () => {
          downloaded = true;
          return Buffer.alloc(0);
        },
      }),
    ).rejects.toThrow(`no pinned checksum for actionlint ${ACTIONLINT_VERSION} on freebsd_amd64`);
    expect(downloaded).toBe(false);
  });
});
