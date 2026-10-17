import { mkdir, readFile, readdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { TOKENS_RES_DIR, androidTokenResources } from "../../src/formats/android-xml/tokens-resources.ts";
import { VIEWS_RES_DIR, androidViewsResources } from "../../src/formats/android-xml/views-resources.ts";
import { loadTsTokens } from "../../src/model/load-ts-tokens.ts";
import { mergeExtensions } from "../../src/model/merge-extensions.ts";
import { rawFixture, rawFixtureExtensions, testDir } from "../support.ts";

const expectedDir = path.join(testDir, "fixtures/expected/android-xml");
const update = process.env["UPDATE_GOLDEN"] === "1";

function fixtureFiles() {
  const extensions = rawFixtureExtensions();
  extensions["renames"] = { "brand-text": "accent-text", "gap-4": "space-4", "motion-fast": "duration-fast" };
  const model = mergeExtensions(loadTsTokens(rawFixture()), extensions);
  return [
    ...androidTokenResources(model).map((file) => ({ name: `tokens/${path.posix.relative(TOKENS_RES_DIR, file.path)}`, contents: file.contents })),
    ...androidViewsResources(model).map((file) => ({ name: `views/${path.posix.relative(VIEWS_RES_DIR, file.path)}`, contents: file.contents })),
  ];
}

async function listExpected(dir: string): Promise<string[]> {
  const entries = await readdir(dir, { recursive: true, withFileTypes: true });
  return entries
    .filter((entry) => entry.isFile() && entry.name.endsWith(".xml"))
    .map((entry) => path.relative(dir, path.join(entry.parentPath, entry.name)).split(path.sep).join("/"))
    .sort();
}

describe("Android XML golden files", () => {
  const files = fixtureFiles();

  it("produces exactly the expected file set", async () => {
    if (update) {
      await Promise.all(
        files.map(async (file) => {
          const target = path.join(expectedDir, file.name);
          await mkdir(path.dirname(target), { recursive: true });
          await writeFile(target, file.contents);
        }),
      );
    }
    expect(files.map((file) => file.name).sort()).toEqual(await listExpected(expectedDir));
  });

  it.each(files.map((file) => [file.name, file.contents] as const))("%s matches its golden file", async (name, contents) => {
    expect(contents).toBe(await readFile(path.join(expectedDir, name), "utf8"));
  });
});
