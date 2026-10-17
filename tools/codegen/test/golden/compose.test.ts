import { mkdir, readFile, readdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { describe, expect, it } from "vitest";
import type { GeneratedFile } from "../../src/generate.ts";
import { COMPOSE_KOTLIN_DIR, composeTheme } from "../../src/formats/compose/compose-theme.ts";
import { TOKENS_KOTLIN_DIR, androidTokenValuesGenerator } from "../../src/formats/compose/token-values.ts";
import { loadTsTokens } from "../../src/model/load-ts-tokens.ts";
import { mergeExtensions } from "../../src/model/merge-extensions.ts";
import { rawFixture, rawFixtureExtensions, testDir } from "../support.ts";

const expectedDir = path.join(testDir, "fixtures/expected/compose");
const update = process.env["UPDATE_GOLDEN"] === "1";

function fixtureFiles() {
  const extensions = rawFixtureExtensions();
  extensions["renames"] = { "brand-text": "accent-text", "gap-4": "space-4", "motion-fast": "duration-fast" };
  const model = mergeExtensions(loadTsTokens(rawFixture()), extensions);
  return [
    ...(androidTokenValuesGenerator.generate({ root: "", model }) as GeneratedFile[]).map((file) => ({
      name: `tokens/${path.posix.relative(TOKENS_KOTLIN_DIR, file.path)}`,
      contents: file.contents,
    })),
    ...composeTheme(model).map((file) => ({ name: `compose/${path.posix.relative(COMPOSE_KOTLIN_DIR, file.path)}`, contents: file.contents })),
  ];
}

async function listExpected(dir: string): Promise<string[]> {
  const entries = await readdir(dir, { recursive: true, withFileTypes: true });
  return entries
    .filter((entry) => entry.isFile() && entry.name.endsWith(".kt"))
    .map((entry) => path.relative(dir, path.join(entry.parentPath, entry.name)).split(path.sep).join("/"))
    .sort();
}

describe("Compose golden files", () => {
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
