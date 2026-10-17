import { execFile, spawn } from "node:child_process";
import { createHash } from "node:crypto";
import { access, chmod, mkdir, mkdtemp, rename, rm, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { promisify } from "node:util";

const exec = promisify(execFile);

export const ACTIONLINT_VERSION = "1.7.12";
export const ACTIONLINT_SHA256: Readonly<Record<string, string>> = {
  darwin_amd64: "5b44c3bc2255115c9b69e30efc0fecdf498fdb63c5d58e17084fd5f16324c644",
  darwin_arm64: "aba9ced2dee8d27fecca3dc7feb1a7f9a52caefa1eb46f3271ea66b6e0e6953f",
  linux_amd64: "8aca8db96f1b94770f1b0d72b6dddcb1ebb8123cb3712530b08cc387b349a3d8",
  linux_arm64: "325e971b6ba9bfa504672e29be93c24981eeb1c07576d730e9f7c8805afff0c6",
};
export const ACTIONLINT_CACHE = ".build/tools/actionlint";

export type Download = (url: string) => Promise<Buffer>;

export interface EnsureOptions {
  cacheDir: string;
  version?: string;
  platform?: string;
  checksums?: Readonly<Record<string, string>>;
  download?: Download;
}

export function releasePlatform(platform: string = process.platform, arch: string = process.arch): string {
  const os = { darwin: "darwin", linux: "linux" }[platform];
  const cpu = { x64: "amd64", arm64: "arm64" }[arch];
  if (os === undefined || cpu === undefined) throw new Error(`no pinned actionlint build for ${platform}/${arch}; supported: ${Object.keys(ACTIONLINT_SHA256).join(", ")}`);
  return `${os}_${cpu}`;
}

export function releaseAsset(version: string, platform: string): { name: string; url: string } {
  const name = `actionlint_${version}_${platform}.tar.gz`;
  return { name, url: `https://github.com/rhysd/actionlint/releases/download/v${version}/${name}` };
}

export function binaryPath(cacheDir: string, version: string, platform: string): string {
  return path.join(cacheDir, version, platform, "actionlint");
}

async function fetchBytes(url: string): Promise<Buffer> {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`downloading ${url} failed: HTTP ${response.status}`);
  return Buffer.from(await response.arrayBuffer());
}

async function exists(file: string): Promise<boolean> {
  try {
    await access(file);
    return true;
  } catch {
    return false;
  }
}

export async function ensureActionlint(options: EnsureOptions): Promise<{ binary: string; downloaded: boolean }> {
  const version = options.version ?? ACTIONLINT_VERSION;
  const platform = options.platform ?? releasePlatform();
  const checksums = options.checksums ?? ACTIONLINT_SHA256;
  const binary = binaryPath(options.cacheDir, version, platform);
  if (await exists(binary)) return { binary, downloaded: false };
  const expected = checksums[platform];
  if (expected === undefined) throw new Error(`no pinned checksum for actionlint ${version} on ${platform}`);
  const asset = releaseAsset(version, platform);
  const archive = await (options.download ?? fetchBytes)(asset.url);
  const actual = createHash("sha256").update(archive).digest("hex");
  if (actual !== expected) throw new Error(`${asset.name} has SHA-256 ${actual}, expected the pinned ${expected}; nothing was installed`);
  await mkdir(options.cacheDir, { recursive: true });
  const staging = await mkdtemp(path.join(options.cacheDir, ".staging-"));
  try {
    const tarball = path.join(staging, asset.name);
    await writeFile(tarball, archive);
    await exec("tar", ["-xzf", tarball, "-C", staging, "actionlint"]);
    await chmod(path.join(staging, "actionlint"), 0o755);
    await mkdir(path.dirname(binary), { recursive: true });
    await rename(path.join(staging, "actionlint"), binary);
  } finally {
    await rm(staging, { recursive: true, force: true });
  }
  return { binary, downloaded: true };
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
  try {
    const { binary, downloaded } = await ensureActionlint({ cacheDir: path.join(root, ACTIONLINT_CACHE) });
    console.log(`actionlint: ${ACTIONLINT_VERSION} (${downloaded ? "downloaded and checksum-verified" : "cached"}) at ${path.relative(root, binary)}`);
    const child = spawn(binary, process.argv.slice(2), { cwd: root, stdio: "inherit" });
    child.on("exit", (code, signal) => {
      process.exitCode = code ?? (signal === null ? 1 : 128);
    });
  } catch (error) {
    console.error(`actionlint: ${error instanceof Error ? error.message : String(error)}`);
    process.exitCode = 2;
  }
}
