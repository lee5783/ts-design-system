import { describe, expect, it } from "vitest";
import { readTsTokens } from "../../src/model/load-ts-tokens.ts";
import { EXTENSION_KEY, createStyleDictionary, toDesignTokens } from "../../src/model/to-style-dictionary.ts";
import { readNativeModel } from "../../src/model/merge-extensions.ts";
import { realTokensPath, repoRoot } from "../support.ts";

describe("Style Dictionary bridge", () => {
  it("emits the focus alias as a Style Dictionary reference", async () => {
    const tokens = toDesignTokens(await readTsTokens(realTokensPath), "dark") as Record<string, Record<string, unknown>>;
    expect(tokens["color"]?.["focus"]).toMatchObject({ $type: "color", $value: "{color.accent-text}" });
    expect(tokens["color"]?.["accent-text"]).toMatchObject({ $value: "#a78bfa" });
    expect(tokens["spacing"]?.["space-0-5"]).toMatchObject({
      $value: 2,
      $extensions: { [EXTENSION_KEY]: { camel: "space0_5", snake: "space_0_5" } },
    });
  });

  it("loads every native token into Style Dictionary 4 and resolves references per theme", async () => {
    const model = await readTsTokens(realTokensPath);
    for (const [theme, focus] of [
      ["light", "#7c3aed"],
      ["dark", "#a78bfa"],
    ] as const) {
      const dictionary = await createStyleDictionary(model, theme, { model: {} });
      const { allTokens } = await dictionary.getPlatformTokens("model");
      const count = (family: string) => allTokens.filter((token) => token.path[0] === family).length;
      expect([count("color"), count("typography"), count("spacing"), count("radius"), count("shadow"), count("size"), count("opacity")]).toEqual([
        31, 11, 11, 6, 5, 9, 2,
      ]);
      const resolved = allTokens.find((token) => token.path.join(".") === "color.focus");
      expect(resolved?.$value).toBe(focus);
      expect(resolved?.original.$value).toBe("{color.accent-text}");
    }
  });

  it("loads the merged extension families into Style Dictionary 4", async () => {
    const dictionary = await createStyleDictionary(await readNativeModel(repoRoot), "dark", { model: {} });
    const { allTokens } = await dictionary.getPlatformTokens("model");
    const token = (name: string) => allTokens.find((item) => item.path.join(".") === name);
    expect(allTokens.filter((item) => item.path[0] === "color")).toHaveLength(37);
    expect(token("color.danger-pressed")?.$value).toBe("#cd2323");
    expect(token("duration.duration-base")).toMatchObject({ $type: "duration", $value: "150ms" });
    expect(token("easing.easing-standard")).toMatchObject({ $type: "cubicBezier", $value: [0.25, 0.1, 0.25, 1] });
    expect(token("component.switch-track-width")).toMatchObject({
      $type: "dimension",
      $value: 44,
      $extensions: { "com.tinysol.designsystem": { component: "switch", upstream: "pending" } },
    });
    expect(token("component.alert-title-font-weight")).toMatchObject({ $type: "fontWeight", $value: 600 });
  });
});
