import { mkdir, readFile, readdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { beforeAll, describe, expect, it } from "vitest";
import { resolveNativeLogos, vectorSvg } from "../../src/formats/icons/asset-files.ts";
import { kotlinIconEnum, swiftIconName } from "../../src/formats/icons/icon-enums.ts";
import { androidIconDrawables, androidLogoDrawables } from "../../src/formats/icons/vector-drawable.ts";
import { iconCatalogFiles } from "../../src/formats/icons/xcassets.ts";
import type { VectorAssets } from "../../src/icons/vector-assets.ts";
import { loadVectorAssets } from "../../src/icons/vector-assets.ts";
import { fixtureAssetPaths, repoRoot, testDir } from "../support.ts";

const expectedDir = path.join(testDir, "fixtures/expected/icons");
const update = process.env["UPDATE_GOLDEN"] === "1";

function fixtureFiles(assets: VectorAssets): { name: string; contents: string }[] {
  const logos = resolveNativeLogos(assets.logos, [{ name: "ts-monogram", light: "ts-monogram" }]);
  return [
    ...androidIconDrawables(assets.icons, "res"),
    ...androidLogoDrawables(logos, "res"),
    ...iconCatalogFiles(assets.icons, "Icons.xcassets"),
    ...assets.icons.map((icon) => ({ path: `normalized/${icon.name}.svg`, contents: vectorSvg(icon) })),
    { path: "TSIconName.swift", contents: swiftIconName(assets.icons) },
    { path: "TSIcon.kt", contents: kotlinIconEnum(assets.icons) },
  ].map((file) => ({ name: file.path, contents: file.contents }));
}

async function listExpected(dir: string): Promise<string[]> {
  const entries = await readdir(dir, { recursive: true, withFileTypes: true });
  return entries
    .filter((entry) => entry.isFile())
    .map((entry) => path.relative(dir, path.join(entry.parentPath, entry.name)).split(path.sep).join("/"))
    .sort();
}

describe("Icon golden files", () => {
  let files: { name: string; contents: string }[] = [];

  beforeAll(async () => {
    files = fixtureFiles(await loadVectorAssets(repoRoot, fixtureAssetPaths));
    if (update) {
      await Promise.all(
        files.map(async (file) => {
          const target = path.join(expectedDir, file.name);
          await mkdir(path.dirname(target), { recursive: true });
          await writeFile(target, file.contents);
        }),
      );
    }
  });

  it("produces exactly the expected file set", async () => {
    expect(files.map((file) => file.name).sort()).toEqual(await listExpected(expectedDir));
  });

  it("matches every golden file", async () => {
    for (const file of files) {
      expect(file.contents, file.name).toBe(await readFile(path.join(expectedDir, file.name), "utf8"));
    }
  });

  it("converts the 3 fixture icons to VectorDrawables", () => {
    const drawables = files.filter((file) => file.name.startsWith("res/drawable/ts_ic_"));
    expect(drawables.map((file) => file.name)).toEqual([
      "res/drawable/ts_ic_chevron_left.xml",
      "res/drawable/ts_ic_grid.xml",
      "res/drawable/ts_ic_info.xml",
    ]);
  });
});
