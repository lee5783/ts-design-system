import { mkdir, readFile, readdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { swiftTokenFiles } from "../../src/formats/swift/index.ts";
import { loadTsTokens } from "../../src/model/load-ts-tokens.ts";
import { mergeExtensions } from "../../src/model/merge-extensions.ts";
import { rawFixture, rawFixtureExtensions, testDir } from "../support.ts";

const expectedDir = path.join(testDir, "fixtures/expected/swift");
const update = process.env["UPDATE_GOLDEN"] === "1";

function fixtureFiles() {
  const extensions = rawFixtureExtensions();
  extensions["renames"] = { "brand-text": "accent-text", "gap-4": "space-4", "motion-fast": "duration-fast" };
  const model = mergeExtensions(loadTsTokens(rawFixture()), extensions);
  return swiftTokenFiles(model, "out").map((file) => ({
    name: path.posix.basename(file.path),
    contents: file.contents,
  }));
}

describe("Swift golden files", () => {
  const files = fixtureFiles();

  it("produces exactly the expected file set", async () => {
    if (update) {
      await mkdir(expectedDir, { recursive: true });
      await Promise.all(files.map((file) => writeFile(path.join(expectedDir, file.name), file.contents)));
    }
    const expected = (await readdir(expectedDir)).filter((name) => name.endsWith(".swift")).sort();
    expect(files.map((file) => file.name).sort()).toEqual(expected);
  });

  it.each(files.map((file) => [file.name, file.contents] as const))("%s matches its golden file", async (name, contents) => {
    expect(contents).toBe(await readFile(path.join(expectedDir, name), "utf8"));
  });
});
