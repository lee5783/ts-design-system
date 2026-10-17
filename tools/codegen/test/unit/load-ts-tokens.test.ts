import { describe, expect, it } from "vitest";
import { loadTsTokens, readTsTokens } from "../../src/model/load-ts-tokens.ts";
import { fixtureTokensPath, rawFixture, rawRealTokens, realTokensPath } from "../support.ts";

describe("loadTsTokens on design/source/tokens.json", () => {
  it("maps every native token family with the expected counts", async () => {
    const model = await readTsTokens(realTokensPath);
    expect(model.themes).toEqual(["light", "dark"]);
    expect(model.color).toHaveLength(31);
    expect(model.color.every((token) => token.value.light.hex && token.value.dark.hex)).toBe(true);
    expect(model.typography).toHaveLength(11);
    expect(model.spacing).toHaveLength(11);
    expect(model.radius).toHaveLength(6);
    expect(model.shadow).toHaveLength(5);
    expect(model.size).toHaveLength(9);
    expect(model.opacity).toHaveLength(2);
  });

  it("drops breakpoint, zIndex and the web font families", async () => {
    const model = await readTsTokens(realTokensPath);
    expect(model.dropped.map((item) => [item.family, item.count])).toEqual([
      ["breakpoint", 4],
      ["zIndex", 5],
      ["type.fonts", 0],
      ["type.families", 2],
    ]);
    expect(Object.keys(model)).not.toContain("breakpoint");
    expect(Object.keys(model)).not.toContain("zIndex");
  });

  it("keeps the #RRGGBBAA alpha of overlay in both themes", async () => {
    const overlay = (await readTsTokens(realTokensPath)).color.find((token) => token.name.token === "overlay");
    expect(overlay?.value.light).toEqual({ hex: "#18181b66", rgba: { r: 0x18, g: 0x18, b: 0x1b, a: 0x66 } });
    expect(overlay?.value.dark).toEqual({ hex: "#000000a3", rgba: { r: 0, g: 0, b: 0, a: 0xa3 } });
  });

  it("parses typography, dimensions, opacity and shadows", async () => {
    const model = await readTsTokens(realTokensPath);
    const overline = model.typography.find((token) => token.name.token === "overline");
    expect(overline).toMatchObject({ family: "sans", fontSize: 11, lineHeight: 16, fontWeight: 600, letterSpacingEm: 0.06 });
    expect(model.typography.find((token) => token.name.token === "code")).toMatchObject({ family: "mono", letterSpacingEm: 0 });
    expect(model.spacing[0]).toMatchObject({ name: { token: "space-0-5", camel: "space0_5", snake: "space_0_5" }, value: 2 });
    expect(model.radius.find((token) => token.name.token === "radius-full")?.value).toBe(9999);
    expect(model.size.find((token) => token.name.token === "control-md")?.value).toBe(44);
    expect(model.opacity.map((token) => token.value)).toEqual([0.45, 0.85]);
    const shadowLg = model.shadow.find((token) => token.name.token === "shadow-lg");
    expect(shadowLg?.value.light).toHaveLength(2);
    expect(shadowLg?.value.light[0]).toMatchObject({ offsetX: 0, offsetY: 12, blur: 32, spread: -4, inset: false });
    expect(shadowLg?.value.light[0]?.color.hex).toBe("#18181b29");
    expect(shadowLg?.value.dark).toHaveLength(1);
  });
});

describe("loadTsTokens on the fixture", () => {
  it("expands shorthand colors and single values into both themes", async () => {
    const model = await readTsTokens(fixtureTokensPath);
    const onAccent = model.color.find((token) => token.name.token === "text-on-accent");
    expect(onAccent?.value.light.hex).toBe("#ffffff");
    expect(onAccent?.value.dark.hex).toBe("#ffffff");
    expect(model.color.find((token) => token.name.token === "text-primary")?.usage).toBe("Body text.");
  });

  it("keeps the group and sample of typography styles", async () => {
    const title = (await readTsTokens(fixtureTokensPath)).typography[0];
    expect(title).toMatchObject({ group: "Titles", sample: "Projects", letterSpacingEm: -0.01, name: { camel: "title1" } });
  });

  it("matches the real source format", () => {
    const fixtureKeys = Object.keys(rawFixture()).sort();
    expect(fixtureKeys).toEqual(Object.keys(rawRealTokens()).sort());
  });
});

describe("loadTsTokens errors", () => {
  it("fails on an unknown token family", () => {
    const raw = rawFixture();
    raw.motion = { tokens: [] };
    expect(() => loadTsTokens(raw)).toThrow(/unknown token families motion/);
  });

  it("fails when the themes are not light and dark", () => {
    const raw = rawFixture();
    raw.color.themes.push({ id: "contrast", name: "Contrast" });
    expect(() => loadTsTokens(raw)).toThrow(/color.themes/);
  });

  it("fails when a themed value misses a theme", () => {
    const raw = rawFixture();
    raw.color.tokens.push({ name: "half", value: { light: "#000000" } });
    expect(() => loadTsTokens(raw)).toThrow(/color\/half: missing "dark" value/);
  });

  it("fails on invalid colors, lengths and font families", () => {
    const badColor = rawFixture();
    badColor.color.tokens[0].value.light = "rgb(0,0,0)";
    expect(() => loadTsTokens(badColor)).toThrow(/color\/text-primary \(light\): invalid color/);
    const badLength = rawFixture();
    badLength.spacing.tokens[0].value = "2rem";
    expect(() => loadTsTokens(badLength)).toThrow(/spacing\/space-0-5: invalid length "2rem"/);
    const badFamily = rawFixture();
    badFamily.type.groups[0].family = "serif";
    expect(() => loadTsTokens(badFamily)).toThrow(/type\/title-1: unsupported font family "serif"/);
  });

  it("fails on duplicate names and names that collide after conversion", () => {
    const duplicate = rawFixture();
    duplicate.radius.tokens.push({ name: "radius-md", value: "10px" });
    expect(() => loadTsTokens(duplicate)).toThrow(/radius: duplicate token "radius-md"/);
    const collision = rawFixture();
    collision.spacing.tokens.push({ name: "space-05", value: "3px" }, { name: "space0-5", value: "1px" });
    expect(() => loadTsTokens(collision)).toThrow(/"space-05" and "space0-5" both map to "space05"/);
  });
});
