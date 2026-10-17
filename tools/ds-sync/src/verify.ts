import { createHash } from "node:crypto";
import { cp, mkdir, readFile, readdir, rm, stat, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

export const MANIFEST_PATH = "design/manifest.json";
export const SOURCE_DIR = "design/source";
export const ASSETS_DIR = "design/assets";
export const ICON_META_PATH = "design/assets/icons/icons.meta.json";
export const DESIGN_DIR = "design";
export const REQUIRED_EXPORT_FILES = ["tokens.json", "design-system.json"];

const IGNORED_NAMES = new Set([".DS_Store", ".gitkeep"]);
const ASSET_EXTENSION = ".svg";

export interface ArtifactInfo {
  id: string;
  url: string;
  title: string;
  version: string;
}

export interface FileEntry {
  sha256: string;
  size: number;
}

export interface AssetEntry {
  group: string;
  name: string;
  blob: string;
  indexedSize: number;
  path: string;
  sha256?: string;
  size?: number;
}

export interface Manifest {
  schemaVersion: 1;
  artifact: ArtifactInfo;
  syncedAt: string;
  sourceDir: string;
  files: Record<string, FileEntry>;
  assets: AssetEntry[];
}

export interface VerifyResult {
  ok: boolean;
  changed: string[];
  missing: string[];
  added: string[];
  unsafe: string[];
}

export interface IconMeta {
  schemaVersion: 1;
  icons: Record<string, { mirrorsInRTL: boolean }>;
}

export interface CliOptions {
  root: string;
  update: boolean;
  from?: string;
  version?: string;
}

interface AssetIndexFile {
  blob: string;
  name: string;
  size: number;
}

interface AssetIndex {
  assetGroups?: Record<string, { files?: Record<string, AssetIndexFile> }>;
}

export function sha256(data: Uint8Array): string {
  return createHash("sha256").update(data).digest("hex");
}

export async function listFiles(dir: string): Promise<string[]> {
  const entries = await readdir(dir, { recursive: true, withFileTypes: true });
  return entries
    .filter((entry) => entry.isFile() && !IGNORED_NAMES.has(entry.name))
    .map((entry) => path.relative(dir, path.join(entry.parentPath, entry.name)).split(path.sep).join("/"))
    .sort();
}

export async function hashFiles(dir: string): Promise<Record<string, FileEntry>> {
  const files: Record<string, FileEntry> = {};
  for (const relativePath of await listFiles(dir)) {
    const data = await readFile(path.join(dir, relativePath));
    files[relativePath] = { sha256: sha256(data), size: data.byteLength };
  }
  return files;
}

export async function verifySource(sourceDir: string, manifest: Manifest): Promise<VerifyResult> {
  const actual = await hashFiles(sourceDir);
  const expected = manifest.files;
  const changed: string[] = [];
  const missing: string[] = [];
  for (const [relativePath, entry] of Object.entries(expected)) {
    const found = actual[relativePath];
    if (!found) {
      missing.push(relativePath);
    } else if (found.sha256 !== entry.sha256 || found.size !== entry.size) {
      changed.push(relativePath);
    }
  }
  const added = Object.keys(actual).filter((relativePath) => !(relativePath in expected));
  return toResult(changed, missing, added, []);
}

function toResult(changed: string[], missing: string[], added: string[], unsafe: string[]): VerifyResult {
  return {
    ok: changed.length === 0 && missing.length === 0 && added.length === 0 && unsafe.length === 0,
    changed: changed.sort(),
    missing: missing.sort(),
    added: added.sort(),
    unsafe: unsafe.sort(),
  };
}

export function mergeResults(...results: VerifyResult[]): VerifyResult {
  return toResult(
    results.flatMap((result) => result.changed),
    results.flatMap((result) => result.missing),
    results.flatMap((result) => result.added),
    results.flatMap((result) => result.unsafe),
  );
}

export function assetPath(group: string, name: string): string {
  return `${ASSETS_DIR}/${group.toLowerCase()}/source/${name}`;
}

const UNSAFE_SVG_PATTERNS: [string, RegExp][] = [
  ["script", /<script\b/i],
  ["foreignObject", /<foreignObject\b/i],
  ["event handler", /\son[a-z]+\s*=/i],
  ["external reference", /\bhref\s*=\s*(?!["']?#)/i],
  ["external url", /url\(\s*(?!["']?#)/i],
  ["doctype or entity", /<!(?:DOCTYPE|ENTITY)/i],
];

export function inspectSvg(text: string): string[] {
  const issues = UNSAFE_SVG_PATTERNS.filter(([, pattern]) => pattern.test(text)).map(([issue]) => issue);
  if (!/^\s*(?:<\?xml[^>]*\?>\s*)?<svg\b/i.test(text)) {
    issues.unshift("not an svg document");
  }
  return issues;
}

async function listAssetFiles(root: string): Promise<string[]> {
  const assetsRoot = path.join(root, ASSETS_DIR);
  let groups: string[];
  try {
    groups = await readdir(assetsRoot);
  } catch {
    return [];
  }
  const files: string[] = [];
  for (const group of groups.sort()) {
    const sourceDir = path.join(assetsRoot, group, "source");
    let names: string[];
    try {
      names = await readdir(sourceDir);
    } catch {
      continue;
    }
    for (const name of names) {
      if (name.toLowerCase().endsWith(ASSET_EXTENSION)) files.push(`${ASSETS_DIR}/${group}/source/${name}`);
    }
  }
  return files.sort();
}

async function readOptional(filePath: string): Promise<Buffer | undefined> {
  try {
    return await readFile(filePath);
  } catch {
    return undefined;
  }
}

export async function hashAssets(root: string, assets: AssetEntry[]): Promise<AssetEntry[]> {
  const hashed: AssetEntry[] = [];
  for (const asset of assets) {
    const entry: AssetEntry = {
      group: asset.group,
      name: asset.name,
      blob: asset.blob,
      indexedSize: asset.indexedSize,
      path: asset.path,
    };
    const data = await readOptional(path.join(root, asset.path));
    hashed.push(data ? { ...entry, sha256: sha256(data), size: data.byteLength } : entry);
  }
  return hashed;
}

export async function verifyAssets(root: string, manifest: Manifest): Promise<VerifyResult> {
  const changed: string[] = [];
  const missing: string[] = [];
  const unsafe: string[] = [];
  for (const asset of manifest.assets) {
    const data = await readOptional(path.join(root, asset.path));
    if (!data) {
      missing.push(asset.path);
      continue;
    }
    if (sha256(data) !== asset.sha256 || data.byteLength !== asset.size) {
      changed.push(asset.path);
    }
    const issues = inspectSvg(data.toString("utf8"));
    if (issues.length > 0) unsafe.push(`${asset.path} (${issues.join(", ")})`);
  }
  const expected = new Set(manifest.assets.map((asset) => asset.path));
  const added = (await listAssetFiles(root)).filter((assetFile) => !expected.has(assetFile));
  return toResult(changed, missing, added, unsafe);
}

export async function verifyIconMeta(root: string, manifest: Manifest): Promise<VerifyResult> {
  const icons = manifest.assets
    .filter((asset) => asset.group === "Icons")
    .map((asset) => asset.name.replace(/\.svg$/i, ""));
  if (icons.length === 0) return toResult([], [], [], []);
  const data = await readOptional(path.join(root, ICON_META_PATH));
  if (!data) return toResult([], [ICON_META_PATH], [], []);
  const meta = JSON.parse(data.toString("utf8")) as Partial<IconMeta>;
  const entries = meta.icons ?? {};
  const changed = Object.entries(entries)
    .filter(([, entry]) => typeof entry?.mirrorsInRTL !== "boolean")
    .map(([name]) => `${ICON_META_PATH}#${name}`);
  const missing = icons.filter((name) => !(name in entries)).map((name) => `${ICON_META_PATH}#${name}`);
  const added = Object.keys(entries)
    .filter((name) => !icons.includes(name))
    .map((name) => `${ICON_META_PATH}#${name}`);
  return toResult(changed, missing, added, []);
}

export async function verifyAll(root: string, manifest: Manifest): Promise<VerifyResult> {
  return mergeResults(
    await verifySource(path.join(root, SOURCE_DIR), manifest),
    await verifyAssets(root, manifest),
    await verifyIconMeta(root, manifest),
  );
}

export function readAssetIndex(designSystemJson: string): AssetEntry[] {
  const index = JSON.parse(designSystemJson) as AssetIndex;
  const assets: AssetEntry[] = [];
  for (const [group, value] of Object.entries(index.assetGroups ?? {})) {
    for (const file of Object.values(value.files ?? {})) {
      assets.push({ group, name: file.name, blob: file.blob, indexedSize: file.size, path: assetPath(group, file.name) });
    }
  }
  return assets.sort((a, b) => a.group.localeCompare(b.group) || a.name.localeCompare(b.name));
}

export async function buildManifest(
  sourceDir: string,
  artifact: ArtifactInfo,
  syncedAt: string,
  manifestSourceDir: string = SOURCE_DIR,
  root?: string,
): Promise<Manifest> {
  const files = await hashFiles(sourceDir);
  const designSystemJson = files["design-system.json"]
    ? await readFile(path.join(sourceDir, "design-system.json"), "utf8")
    : "{}";
  return {
    schemaVersion: 1,
    artifact,
    syncedAt,
    sourceDir: manifestSourceDir,
    files,
    assets: root ? await hashAssets(root, readAssetIndex(designSystemJson)) : readAssetIndex(designSystemJson),
  };
}

export async function readManifest(manifestPath: string): Promise<Manifest> {
  return JSON.parse(await readFile(manifestPath, "utf8")) as Manifest;
}

export async function writeManifest(manifestPath: string, manifest: Manifest): Promise<void> {
  await mkdir(path.dirname(manifestPath), { recursive: true });
  await writeFile(manifestPath, `${JSON.stringify(manifest, null, 2)}\n`);
}

function isInside(child: string, parent: string): boolean {
  const relative = path.relative(parent, child);
  return relative === "" || (!relative.startsWith("..") && !path.isAbsolute(relative));
}

export async function validateExport(exportDir: string, root: string): Promise<void> {
  const designDir = path.resolve(root, DESIGN_DIR);
  if (isInside(path.resolve(exportDir), designDir)) {
    throw new Error(`--from ${exportDir} must be outside ${designDir}`);
  }
  let info;
  try {
    info = await stat(exportDir);
  } catch {
    throw new Error(`export directory not found: ${exportDir}`);
  }
  if (!info.isDirectory()) {
    throw new Error(`--from ${exportDir} is not a directory`);
  }
  for (const required of REQUIRED_EXPORT_FILES) {
    const file = await stat(path.join(exportDir, required)).catch(() => undefined);
    if (!file?.isFile()) {
      throw new Error(`export is missing ${required}: ${exportDir}`);
    }
  }
}

export async function copyExport(exportDir: string, sourceDir: string): Promise<void> {
  await rm(sourceDir, { recursive: true, force: true });
  await mkdir(sourceDir, { recursive: true });
  for (const relativePath of await listFiles(exportDir)) {
    if (relativePath.toLowerCase().endsWith(ASSET_EXTENSION)) {
      continue;
    }
    const target = path.join(sourceDir, relativePath);
    await mkdir(path.dirname(target), { recursive: true });
    await cp(path.join(exportDir, relativePath), target);
  }
}

export async function copyAssets(exportDir: string, root: string): Promise<string[]> {
  const copied: string[] = [];
  const exportAssets = path.join(exportDir, "assets");
  let groups: string[];
  try {
    groups = await readdir(exportAssets);
  } catch {
    return copied;
  }
  for (const group of groups.sort()) {
    let names: string[];
    try {
      names = (await readdir(path.join(exportAssets, group))).filter((name) => name.toLowerCase().endsWith(ASSET_EXTENSION));
    } catch {
      continue;
    }
    if (names.length === 0) continue;
    const targetDir = path.join(root, path.dirname(assetPath(group, names[0] ?? "")));
    await mkdir(targetDir, { recursive: true });
    for (const existing of await readdir(targetDir)) {
      if (existing.toLowerCase().endsWith(ASSET_EXTENSION)) await rm(path.join(targetDir, existing));
    }
    for (const name of names.sort()) {
      await cp(path.join(exportAssets, group, name), path.join(root, assetPath(group, name)));
      copied.push(assetPath(group, name));
    }
  }
  return copied;
}

export function formatResult(result: VerifyResult): string {
  if (result.ok) {
    return "ds-sync: design/source and design/assets match design/manifest.json";
  }
  const lines = ["ds-sync: design/source or design/assets do not match design/manifest.json"];
  for (const relativePath of result.changed) lines.push(`  changed  ${relativePath}`);
  for (const relativePath of result.missing) lines.push(`  missing  ${relativePath}`);
  for (const relativePath of result.added) lines.push(`  added    ${relativePath}`);
  for (const relativePath of result.unsafe) lines.push(`  unsafe   ${relativePath}`);
  lines.push("Run `make ds-sync UPDATE=1` after a deliberate sync to record the new checksums.");
  return lines.join("\n");
}

export function parseArgs(argv: string[], defaultRoot: string, cwd: string = process.env["INIT_CWD"] ?? process.cwd()): CliOptions {
  const options: CliOptions = { root: defaultRoot, update: false };
  for (let index = 0; index < argv.length; index += 1) {
    const argument = argv[index];
    const value = (): string => {
      const next = argv[index + 1];
      if (next === undefined || next.startsWith("--")) {
        throw new Error(`${argument} needs a value`);
      }
      index += 1;
      return next;
    };
    switch (argument) {
      case "--root":
        options.root = path.resolve(cwd, value());
        break;
      case "--update":
        options.update = true;
        break;
      case "--from":
        options.from = path.resolve(cwd, value());
        options.update = true;
        break;
      case "--version":
        options.version = value();
        options.update = true;
        break;
      default:
        throw new Error(`unknown argument: ${argument}`);
    }
  }
  return options;
}

export async function run(options: CliOptions, log: (line: string) => void = console.log): Promise<number> {
  const manifestPath = path.join(options.root, MANIFEST_PATH);
  const sourceDir = path.join(options.root, SOURCE_DIR);
  if (options.from) {
    await validateExport(options.from, options.root);
    await copyExport(options.from, sourceDir);
    const copied = await copyAssets(options.from, options.root);
    log(`ds-sync: copied ${options.from} into ${SOURCE_DIR} and ${copied.length} SVG assets into ${ASSETS_DIR}`);
  }
  if (options.update) {
    const previous = await readManifest(manifestPath);
    const artifact = { ...previous.artifact, ...(options.version ? { version: options.version } : {}) };
    const manifest = await buildManifest(sourceDir, artifact, new Date().toISOString(), SOURCE_DIR, options.root);
    await writeManifest(manifestPath, manifest);
    log(
      `ds-sync: wrote ${MANIFEST_PATH} (${Object.keys(manifest.files).length} files, ${manifest.assets.length} assets, artifact version ${artifact.version})`,
    );
    return 0;
  }
  const manifest = await readManifest(manifestPath);
  const result = await verifyAll(options.root, manifest);
  log(formatResult(result));
  if (result.ok) {
    log(`ds-sync: ${Object.keys(manifest.files).length} files, ${manifest.assets.length} assets, artifact ${manifest.artifact.id} version ${manifest.artifact.version}`);
  }
  return result.ok ? 0 : 1;
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  const defaultRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
  try {
    process.exitCode = await run(parseArgs(process.argv.slice(2), defaultRoot));
  } catch (error) {
    console.error(`ds-sync: ${error instanceof Error ? error.message : String(error)}`);
    process.exitCode = 2;
  }
}
