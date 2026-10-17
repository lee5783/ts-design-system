import { createHash } from "node:crypto";
import { mkdir, mkdtemp, readFile, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import {
  ICON_META_PATH,
  MANIFEST_PATH,
  SOURCE_DIR,
  buildManifest,
  formatResult,
  inspectSvg,
  listFiles,
  parseArgs,
  readAssetIndex,
  readManifest,
  run,
  verifyAll,
  verifyAssets,
  verifyIconMeta,
  verifySource,
  writeManifest,
  type ArtifactInfo,
} from "../src/verify.ts";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");

const artifact: ArtifactInfo = {
  id: "artifact-id",
  url: "https://claude.ai/artifact/artifact-id",
  title: "Fixture",
  version: "1-abcd",
};

const designSystemJson = JSON.stringify({
  assetGroups: {
    Logos: { files: { "mark.svg": { blob: "b2", name: "mark.svg", size: 20 } } },
    Icons: {
      files: {
        "home.svg": { blob: "b1", name: "home.svg", size: 10 },
        "close.svg": { blob: "b0", name: "close.svg", size: 12 },
      },
    },
  },
});

let root: string;
let sourceDir: string;
let manifestPath: string;

async function writeSource(relativePath: string, content: string): Promise<void> {
  const target = path.join(sourceDir, relativePath);
  await mkdir(path.dirname(target), { recursive: true });
  await writeFile(target, content);
}

const iconSvg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M15 6l-6 6 6 6"></path></svg>';

async function writeRepoFile(relativePath: string, content: string): Promise<void> {
  const target = path.join(root, relativePath);
  await mkdir(path.dirname(target), { recursive: true });
  await writeFile(target, content);
}

async function seedRepo(): Promise<void> {
  await writeSource("tokens.json", '{"color":{}}\n');
  await writeSource("README.md", "# Fixture\n");
  await writeSource("components/Button/README.md", "# Button\n");
  await writeSource("design-system.json", designSystemJson);
  await writeRepoFile("design/assets/icons/source/home.svg", iconSvg);
  await writeRepoFile("design/assets/icons/source/close.svg", iconSvg.replace("M15", "M9"));
  await writeRepoFile("design/assets/logos/source/mark.svg", '<svg xmlns="http://www.w3.org/2000/svg"></svg>');
  await writeRepoFile(
    ICON_META_PATH,
    JSON.stringify({ schemaVersion: 1, icons: { close: { mirrorsInRTL: false }, home: { mirrorsInRTL: false } } }),
  );
  await writeManifest(
    manifestPath,
    await buildManifest(sourceDir, artifact, "2026-10-15T00:00:00.000Z", SOURCE_DIR, root),
  );
}

const silent = (): void => {};

beforeEach(async () => {
  root = await mkdtemp(path.join(tmpdir(), "ds-sync-"));
  sourceDir = path.join(root, SOURCE_DIR);
  manifestPath = path.join(root, MANIFEST_PATH);
  await seedRepo();
});

afterEach(async () => {
  await rm(root, { recursive: true, force: true });
});

describe("verifySource", () => {
  it("passes when every checksum matches", async () => {
    const result = await verifySource(sourceDir, await readManifest(manifestPath));
    expect(result).toEqual({ ok: true, changed: [], missing: [], added: [], unsafe: [] });
    expect(await run({ root, update: false }, silent)).toBe(0);
  });

  it("reports a tampered file after a one-byte edit", async () => {
    const target = path.join(sourceDir, "tokens.json");
    const bytes = await readFile(target);
    bytes[2] = (bytes[2] ?? 0) ^ 0x01;
    await writeFile(target, bytes);
    const result = await verifySource(sourceDir, await readManifest(manifestPath));
    expect(result).toEqual({ ok: false, changed: ["tokens.json"], missing: [], added: [], unsafe: [] });
    expect(await run({ root, update: false }, silent)).toBe(1);
  });

  it("reports a missing file", async () => {
    await rm(path.join(sourceDir, "components/Button/README.md"));
    const result = await verifySource(sourceDir, await readManifest(manifestPath));
    expect(result).toEqual({ ok: false, changed: [], missing: ["components/Button/README.md"], added: [], unsafe: [] });
    expect(await run({ root, update: false }, silent)).toBe(1);
  });

  it("reports a new file", async () => {
    await writeSource("components/Chip/README.md", "# Chip\n");
    const result = await verifySource(sourceDir, await readManifest(manifestPath));
    expect(result).toEqual({ ok: false, changed: [], missing: [], added: ["components/Chip/README.md"], unsafe: [] });
    expect(await run({ root, update: false }, silent)).toBe(1);
  });

  it("ignores .DS_Store and .gitkeep", async () => {
    await writeSource(".DS_Store", "x");
    await writeSource("components/.gitkeep", "");
    expect((await verifySource(sourceDir, await readManifest(manifestPath))).ok).toBe(true);
  });
});

describe("formatResult", () => {
  it("lists every drifted path", () => {
    const text = formatResult({ ok: false, changed: ["a"], missing: ["b"], added: ["c"], unsafe: ["d (script)"] });
    expect(text).toContain("changed  a");
    expect(text).toContain("missing  b");
    expect(text).toContain("added    c");
    expect(text).toContain("unsafe   d (script)");
  });
});

describe("manifest", () => {
  it("records sorted posix paths with sha256 and size", async () => {
    const manifest = await readManifest(manifestPath);
    expect(Object.keys(manifest.files)).toEqual(await listFiles(sourceDir));
    expect(manifest.files["README.md"]).toEqual({
      sha256: createHash("sha256").update("# Fixture\n").digest("hex"),
      size: 10,
    });
    expect(manifest.files["components/Button/README.md"]).toBeDefined();
  });

  it("reads asset blob ids from design-system.json sorted by group and name", () => {
    expect(readAssetIndex(designSystemJson)).toEqual([
      { group: "Icons", name: "close.svg", blob: "b0", indexedSize: 12, path: "design/assets/icons/source/close.svg" },
      { group: "Icons", name: "home.svg", blob: "b1", indexedSize: 10, path: "design/assets/icons/source/home.svg" },
      { group: "Logos", name: "mark.svg", blob: "b2", indexedSize: 20, path: "design/assets/logos/source/mark.svg" },
    ]);
  });

  it("update rewrites checksums and keeps the artifact unless a version is given", async () => {
    await writeSource("tokens.json", '{"color":{"x":1}}\n');
    expect(await run({ root, update: true, version: "2-ef01" }, silent)).toBe(0);
    const manifest = await readManifest(manifestPath);
    expect(manifest.artifact).toEqual({ ...artifact, version: "2-ef01" });
    expect(await run({ root, update: false }, silent)).toBe(0);
  });

  it("records sha256 and size of every asset found on disk", async () => {
    const manifest = await readManifest(manifestPath);
    const home = manifest.assets.find((asset) => asset.name === "home.svg");
    expect(home).toMatchObject({
      sha256: createHash("sha256").update(iconSvg).digest("hex"),
      size: Buffer.byteLength(iconSvg),
    });
  });

  it("from copies an export verbatim, drops removed files and routes SVG assets to design/assets", async () => {
    const exportDir = path.join(root, "export");
    await mkdir(path.join(exportDir, "assets/Icons"), { recursive: true });
    await writeFile(path.join(exportDir, "tokens.json"), '{"v":2}\n');
    await writeFile(path.join(exportDir, "design-system.json"), designSystemJson);
    await writeFile(path.join(exportDir, "assets/Icons/README.md"), "# Icons\n");
    await writeFile(path.join(exportDir, "assets/Icons/home.svg"), "<svg/>");
    await writeFile(path.join(exportDir, "assets/Icons/close.svg"), "<svg></svg>");
    await mkdir(path.join(exportDir, "assets/Logos"), { recursive: true });
    await writeFile(path.join(exportDir, "assets/Logos/mark.svg"), "<svg><rect></rect></svg>");
    await writeRepoFile("design/assets/icons/source/stale.svg", "<svg/>");
    expect(await run({ root, update: true, from: exportDir }, silent)).toBe(0);
    expect(await listFiles(sourceDir)).toEqual(["assets/Icons/README.md", "design-system.json", "tokens.json"]);
    expect(await listFiles(path.join(root, "design/assets"))).toEqual([
      "icons/icons.meta.json",
      "icons/source/close.svg",
      "icons/source/home.svg",
      "logos/source/mark.svg",
    ]);
    expect(await readFile(path.join(root, "design/assets/icons/source/home.svg"), "utf8")).toBe("<svg/>");
    expect(await run({ root, update: false }, silent)).toBe(0);
  });
});

describe("from guards", () => {
  async function expectUntouched(from: string, message: string): Promise<void> {
    const before = await listFiles(sourceDir);
    const manifestBefore = await readFile(manifestPath, "utf8");
    await expect(run({ root, update: true, from }, silent)).rejects.toThrow(message);
    expect(await listFiles(sourceDir)).toEqual(before);
    expect(before.length).toBeGreaterThan(0);
    expect(await readFile(manifestPath, "utf8")).toBe(manifestBefore);
  }

  it("refuses a missing export without deleting design/source", async () => {
    await expectUntouched(path.join(root, "does-not-exist"), "export directory not found");
  });

  it("refuses a file instead of an export directory", async () => {
    await writeRepoFile("export.json", "{}");
    await expectUntouched(path.join(root, "export.json"), "is not a directory");
  });

  it.each(["tokens.json", "design-system.json"])("refuses an export without %s", async (required) => {
    const exportDir = path.join(root, "export");
    await mkdir(exportDir, { recursive: true });
    for (const name of ["tokens.json", "design-system.json"].filter((name) => name !== required)) {
      await writeFile(path.join(exportDir, name), "{}");
    }
    await expectUntouched(exportDir, `export is missing ${required}`);
  });

  it.each(["design", "design/source", "design/assets/icons"])("refuses an export inside the repo design folder (%s)", async (inside) => {
    await writeRepoFile(`${inside}/tokens.json`, "{}");
    await writeRepoFile(`${inside}/design-system.json`, designSystemJson);
    await expectUntouched(path.join(root, inside), "must be outside");
  });
});

describe("verifyAssets", () => {
  it("passes when every asset exists with a matching hash", async () => {
    const result = await verifyAssets(root, await readManifest(manifestPath));
    expect(result).toEqual({ ok: true, changed: [], missing: [], added: [], unsafe: [] });
  });

  it("reports a tampered asset after a one-byte edit", async () => {
    const target = path.join(root, "design/assets/icons/source/home.svg");
    const bytes = await readFile(target);
    bytes[20] = (bytes[20] ?? 0) ^ 0x01;
    await writeFile(target, bytes);
    const result = await verifyAssets(root, await readManifest(manifestPath));
    expect(result.changed).toEqual(["design/assets/icons/source/home.svg"]);
    expect(await run({ root, update: false }, silent)).toBe(1);
  });

  it("reports a missing asset and an unlisted asset", async () => {
    await rm(path.join(root, "design/assets/logos/source/mark.svg"));
    await writeRepoFile("design/assets/icons/source/extra.svg", iconSvg);
    const result = await verifyAssets(root, await readManifest(manifestPath));
    expect(result.missing).toEqual(["design/assets/logos/source/mark.svg"]);
    expect(result.added).toEqual(["design/assets/icons/source/extra.svg"]);
    expect(result.ok).toBe(false);
  });

  it("flags unsafe content even when the hash was recorded", async () => {
    await writeRepoFile("design/assets/icons/source/home.svg", '<svg><script>alert(1)</script></svg>');
    await run({ root, update: true }, silent);
    const result = await verifyAssets(root, await readManifest(manifestPath));
    expect(result.changed).toEqual([]);
    expect(result.unsafe).toEqual(["design/assets/icons/source/home.svg (script)"]);
    expect(await run({ root, update: false }, silent)).toBe(1);
  });
});

describe("inspectSvg", () => {
  it("accepts a plain outline icon", () => {
    expect(inspectSvg(iconSvg)).toEqual([]);
    expect(inspectSvg(`<?xml version="1.0"?>\n${iconSvg}`)).toEqual([]);
    expect(inspectSvg('<svg><use href="#a"></use><rect fill="url(#g)"></rect></svg>')).toEqual([]);
  });

  it("names every unsafe construct", () => {
    expect(inspectSvg('<svg onload="x()"><foreignObject></foreignObject></svg>')).toEqual([
      "foreignObject",
      "event handler",
    ]);
    expect(inspectSvg('<svg><image href="https://x/y.png"></image></svg>')).toEqual(["external reference"]);
    expect(inspectSvg('<svg><rect style="fill:url(https://x)"></rect></svg>')).toEqual(["external url"]);
    expect(inspectSvg('<!DOCTYPE svg [<!ENTITY a "b">]><svg></svg>')).toEqual([
      "not an svg document",
      "doctype or entity",
    ]);
    expect(inspectSvg("<html></html>")).toEqual(["not an svg document"]);
  });
});

describe("verifyIconMeta", () => {
  it("passes when every icon has a mirrorsInRTL flag", async () => {
    expect((await verifyIconMeta(root, await readManifest(manifestPath))).ok).toBe(true);
  });

  it("reports missing, unknown and malformed entries", async () => {
    await writeRepoFile(
      ICON_META_PATH,
      JSON.stringify({ schemaVersion: 1, icons: { home: { mirrorsInRTL: "yes" }, ghost: { mirrorsInRTL: true } } }),
    );
    const result = await verifyIconMeta(root, await readManifest(manifestPath));
    expect(result.changed).toEqual([`${ICON_META_PATH}#home`]);
    expect(result.missing).toEqual([`${ICON_META_PATH}#close`]);
    expect(result.added).toEqual([`${ICON_META_PATH}#ghost`]);
  });

  it("reports a missing meta file", async () => {
    await rm(path.join(root, ICON_META_PATH));
    const result = await verifyIconMeta(root, await readManifest(manifestPath));
    expect(result.missing).toEqual([ICON_META_PATH]);
    expect(await run({ root, update: false }, silent)).toBe(1);
  });
});

describe("parseArgs", () => {
  it("defaults to verify mode", () => {
    expect(parseArgs([], "/repo")).toEqual({ root: "/repo", update: false });
  });

  it("treats --from and --version as updates", () => {
    expect(parseArgs(["--from", "/x", "--version", "9-z"], "/repo")).toEqual({
      root: "/repo",
      update: true,
      from: "/x",
      version: "9-z",
    });
  });

  it("resolves relative --from and --root against the invoking directory", () => {
    expect(parseArgs(["--from", "inputs/x", "--root", "repo"], "/repo", "/work")).toEqual({
      root: "/work/repo",
      update: true,
      from: "/work/inputs/x",
    });
  });

  it("rejects unknown arguments and missing values", () => {
    expect(() => parseArgs(["--nope"], "/repo")).toThrow("unknown argument: --nope");
    expect(() => parseArgs(["--from"], "/repo")).toThrow("--from needs a value");
  });
});

describe("checked-in design/source", () => {
  it("matches design/manifest.json", async () => {
    const manifest = await readManifest(path.join(repoRoot, MANIFEST_PATH));
    const result = await verifySource(path.join(repoRoot, SOURCE_DIR), manifest);
    expect(formatResult(result)).toBe("ds-sync: design/source and design/assets match design/manifest.json");
  });

  it("has every manifest asset on disk with a matching hash, safe content and icon meta", async () => {
    const manifest = await readManifest(path.join(repoRoot, MANIFEST_PATH));
    const result = await verifyAll(repoRoot, manifest);
    expect(formatResult(result)).toBe("ds-sync: design/source and design/assets match design/manifest.json");
    for (const asset of manifest.assets) {
      expect(asset.sha256).toMatch(/^[0-9a-f]{64}$/);
      expect(asset.path).toBe(`design/assets/${asset.group.toLowerCase()}/source/${asset.name}`);
    }
  });

  it("mirrors exactly the directional icons in RTL", async () => {
    const meta = JSON.parse(await readFile(path.join(repoRoot, ICON_META_PATH), "utf8")) as {
      icons: Record<string, { mirrorsInRTL: boolean }>;
    };
    expect(Object.keys(meta.icons)).toHaveLength(33);
    const mirrored = Object.entries(meta.icons)
      .filter(([, entry]) => entry.mirrorsInRTL)
      .map(([name]) => name)
      .sort();
    expect(mirrored).toEqual(["arrow-left", "arrow-right", "chevron-left", "chevron-right", "logout"]);
  });

  it("records the 37 asset blob ids (33 icons, 4 logos)", async () => {
    const manifest = await readManifest(path.join(repoRoot, MANIFEST_PATH));
    expect(manifest.assets.filter((asset) => asset.group === "Icons")).toHaveLength(33);
    expect(manifest.assets.filter((asset) => asset.group === "Logos")).toHaveLength(4);
    for (const asset of manifest.assets) expect(asset.blob).toMatch(/^[0-9a-f]{32}$/);
  });
});
