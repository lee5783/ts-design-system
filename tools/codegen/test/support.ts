import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import type { VectorAssetPaths } from "../src/icons/vector-assets.ts";
import { DEFAULT_ASSET_PATHS } from "../src/icons/vector-assets.ts";

export const testDir = path.dirname(fileURLToPath(import.meta.url));
export const repoRoot = path.resolve(testDir, "../../..");
export const fixtureTokensPath = path.join(testDir, "fixtures/tokens.min.json");
export const realTokensPath = path.join(repoRoot, "design/source/tokens.json");
export const fixtureExtensionsPath = path.join(testDir, "fixtures/native-tokens.min.json");
export const realExtensionsPath = path.join(repoRoot, "design/extensions/native-tokens.json");
export const fixtureAssetPaths: VectorAssetPaths = {
  ...DEFAULT_ASSET_PATHS,
  iconsDir: path.join(testDir, "fixtures/icons/source"),
  iconsMeta: path.join(testDir, "fixtures/icons/icons.meta.json"),
  logosDir: path.join(testDir, "fixtures/logos/source"),
};

export function rawFixture(): Record<string, any> {
  return JSON.parse(readFileSync(fixtureTokensPath, "utf8"));
}

export function rawRealTokens(): Record<string, any> {
  return JSON.parse(readFileSync(realTokensPath, "utf8"));
}

export function rawFixtureExtensions(): Record<string, any> {
  return JSON.parse(readFileSync(fixtureExtensionsPath, "utf8"));
}

export function rawRealExtensions(): Record<string, any> {
  return JSON.parse(readFileSync(realExtensionsPath, "utf8"));
}
