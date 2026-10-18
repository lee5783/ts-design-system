import { describe, expect, it } from "vitest";
import { loadTsTokens } from "../../src/model/load-ts-tokens.ts";
import { mergeExtensions, readNativeModel } from "../../src/model/merge-extensions.ts";
import { toDesignTokens } from "../../src/model/to-style-dictionary.ts";
import { TOKEN_FAMILIES, type AnyToken, type ColorToken, type TokenModel } from "../../src/model/types.ts";
import { parseCubicBezier, parseMs } from "../../src/transforms/css-values.ts";
import { rawFixture, rawFixtureExtensions, rawRealExtensions, rawRealTokens, repoRoot } from "../support.ts";

const source = () => loadTsTokens(rawFixture());
const merged = (extensions: unknown = rawFixtureExtensions()) => mergeExtensions(source(), extensions);
const names = (tokens: AnyToken[]) => tokens.map((token) => token.name.token);
const allTokens = (model: TokenModel) => TOKEN_FAMILIES.flatMap((family) => model[family] as AnyToken[]);

describe("merge-extensions", () => {
  it("appends extension tokens after the source tokens and keeps the source untouched", () => {
    const model = merged();
    expect(names(model.color)).toEqual(["text-primary", "accent-text", "text-on-accent", "overlay", "focus", "danger-pressed", "switch-thumb", "focus-ring"]);
    expect(model.color[5]?.value.light.hex).toBe("#b91c1c");
    expect(model.color[5]?.value.dark.hex).toBe("#cd2323");
    expect(model.color[6]?.value.dark.hex).toBe("#ffffff");
    expect(model.color[7]).toMatchObject({ aliasOf: "accent-text", value: { light: { hex: "#7c3aed" }, dark: { hex: "#a78bfa" } } });
    expect(names(model.size)).toEqual(["control-md", "icon-xl"]);
    expect(model.size[1]?.value).toBe(32);
    expect(model.duration.map((token) => [token.name.camel, token.value])).toEqual([
      ["durationFast", 120],
      ["durationSlow", 200],
    ]);
    expect(model.easing[0]?.value).toEqual([0.25, 0.1, 0.25, 1]);
    expect(model.component.map((token) => [token.name.camel, token.component, token.type, token.value])).toEqual([
      ["switchTrackWidth", "switch", "dimension", 44],
      ["checkboxBorderWidth", "checkbox", "dimension", 1.5],
      ["alertTitleFontWeight", "alert", "font-weight", 600],
      ["spinnerRotationDuration", "spinner", "duration", 700],
    ]);
    expect(model.extensions).toEqual({ name: "Fixture extensions", version: 1 });
    expect(source().color).toHaveLength(5);
    expect(source().extensions).toBeUndefined();
  });

  it("tags every extension token with its upstream status and leaves source tokens untagged", () => {
    const model = merged();
    const tagged = allTokens(model).filter((token) => token.extension !== undefined);
    expect(tagged.map((token) => [token.name.token, token.extension?.upstream])).toEqual([
      ["danger-pressed", "pending"],
      ["switch-thumb", "accepted"],
      ["focus-ring", "rejected"],
      ["icon-xl", "pending"],
      ["duration-fast", "pending"],
      ["duration-slow", "pending"],
      ["easing-standard", "pending"],
      ["switch-track-width", "pending"],
      ["checkbox-border-width", "pending"],
      ["alert-title-font-weight", "pending"],
      ["spinner-rotation-duration", "pending"],
    ]);
    expect(model.color[5]?.extension).toEqual({ upstream: "pending", origin: "C-12" });
    expect(allTokens(source()).every((token) => token.extension === undefined)).toBe(true);
    const tokens = toDesignTokens(model, "light") as Record<string, Record<string, any>>;
    expect(tokens["color"]?.["danger-pressed"]?.$extensions["com.ts.ds"]).toMatchObject({ upstream: "pending" });
    expect(tokens["color"]?.["text-primary"]?.$extensions["com.ts.ds"].upstream).toBeUndefined();
  });

  it("rejects a missing or unknown upstream status", () => {
    const missing = rawFixtureExtensions();
    delete missing.color.tokens[0].upstream;
    expect(() => merged(missing)).toThrow('native-tokens color/danger-pressed: "upstream" must be one of pending, accepted, rejected');
    const unknown = rawFixtureExtensions();
    unknown.duration.tokens[0].upstream = "merged";
    expect(() => merged(unknown)).toThrow(/duration\/duration-fast: "upstream" must be one of/);
  });

  it("fails on a key that duplicates a source token", () => {
    const raw = rawFixtureExtensions();
    raw.color.tokens.push({ name: "overlay", value: "#000000", upstream: "pending" });
    expect(() => merged(raw)).toThrow(
      "native-tokens color/overlay: duplicate key, already defined in design/source/tokens.json; remove it from design/extensions/native-tokens.json once the DS ships it",
    );
  });

  it("fails on a key duplicated inside the extensions and on a camel-name collision", () => {
    const raw = rawFixtureExtensions();
    raw.duration.tokens.push({ name: "duration-fast", value: "100ms", upstream: "pending" });
    expect(() => merged(raw)).toThrow("native-tokens duration/duration-fast: duplicate key in design/extensions/native-tokens.json");
    const clash = rawFixtureExtensions();
    clash.size.tokens = [
      { name: "icon-1x", value: "16px", upstream: "pending" },
      { name: "icon1x", value: "16px", upstream: "pending" },
    ];
    expect(() => merged(clash)).toThrow('size: "icon-1x" and "icon1x" both map to "icon1x"');
  });

  it("turns renames into deprecated aliases of the new token", () => {
    const raw = rawFixtureExtensions();
    raw.renames = { "danger-active": "danger-pressed", "control-default": "control-md", "motion-fast": "duration-fast" };
    const model = merged(raw);
    const alias = model.color.find((token) => token.name.token === "danger-active") as ColorToken;
    expect(alias).toMatchObject({
      name: { token: "danger-active", camel: "dangerActive", snake: "danger_active" },
      aliasOf: "danger-pressed",
      usage: "Deprecated: use danger-pressed.",
      deprecated: { renamedTo: "danger-pressed" },
      value: { light: { hex: "#b91c1c" }, dark: { hex: "#cd2323" } },
    });
    expect(alias.extension).toBeUndefined();
    expect(model.size.at(-1)).toMatchObject({ name: { token: "control-default" }, value: 44, deprecated: { renamedTo: "control-md" } });
    expect(model.duration.at(-1)).toMatchObject({ name: { token: "motion-fast" }, value: 120, deprecated: { renamedTo: "duration-fast" } });
    const tokens = toDesignTokens(model, "dark") as Record<string, Record<string, any>>;
    expect(tokens["color"]?.["danger-active"]).toMatchObject({
      $value: "{color.danger-pressed}",
      $extensions: { "com.ts.ds": { renamedTo: "danger-pressed", aliasOf: "danger-pressed" } },
    });
  });

  it("rejects renames whose target is missing or whose old name still exists", () => {
    const missing = rawFixtureExtensions();
    missing.renames = { "old-name": "new-name" };
    expect(() => merged(missing)).toThrow('native-tokens renames/old-name: rename target "new-name" does not exist');
    const existing = rawFixtureExtensions();
    existing.renames = { overlay: "danger-pressed" };
    expect(() => merged(existing)).toThrow('native-tokens renames/overlay: "overlay" still exists; drop the rename or the token');
    const chained = rawFixtureExtensions();
    chained.renames = { "a-old": "b-old", "b-old": "danger-pressed" };
    expect(() => merged(chained)).toThrow(/rename target "b-old" does not exist/);
    const malformed = rawFixtureExtensions();
    malformed.renames = { "a-old": 3 };
    expect(() => merged(malformed)).toThrow(/expected a \{ "old-name": "new-name" \} map/);
  });

  it("rejects unsupported families, unknown keys and invalid values", () => {
    expect(() => merged({ ...rawFixtureExtensions(), shadow: { tokens: [] } })).toThrow(/unsupported families shadow/);
    const unknownKey = rawFixtureExtensions();
    unknownKey.size.tokens[0].status = "pending";
    expect(() => merged(unknownKey)).toThrow("native-tokens size/icon-xl: unknown keys status");
    const badColor = rawFixtureExtensions();
    badColor.color.tokens[0].value.dark = "red";
    expect(() => merged(badColor)).toThrow(/native-tokens color\/danger-pressed \(dark\): invalid color "red"/);
    const badAlias = rawFixtureExtensions();
    badAlias.color.tokens[2].value = "{missing}";
    expect(() => merged(badAlias)).toThrow('native-tokens color/focus-ring: unknown alias target "{missing}"');
    const badType = rawFixtureExtensions();
    badType.component.tokens[0].type = "length";
    expect(() => merged(badType)).toThrow(/component\/switch-track-width: "type" must be one of dimension, font-weight, duration, number/);
    const badWeight = rawFixtureExtensions();
    badWeight.component.tokens[2].value = "650";
    expect(() => merged(badWeight)).toThrow(/invalid font weight "650"/);
    const noComponent = rawFixtureExtensions();
    delete noComponent.component.tokens[0].component;
    expect(() => merged(noComponent)).toThrow('native-tokens component/switch-track-width: missing "component"');
  });

  it("keeps controller tokens in their own bucket, out of the main palette and sizes", () => {
    const model = merged();
    expect(names(model.color)).not.toContain("controller-key");
    expect(names(model.size)).toEqual(["control-md", "icon-xl"]);
    expect(model.controller.color.map((token) => [token.name.token, token.name.camel, token.name.snake])).toEqual([
      ["controller-key", "key", "controller_key"],
      ["controller-key-label", "keyLabel", "controller_key_label"],
      ["overlay-scrim-media", "overlayScrimMedia", "overlay_scrim_media"],
    ]);
    expect(model.controller.color[1]?.value.light.hex).toBe(model.color[0]?.value.light.hex);
    expect(model.controller.color.every((token) => token.aliasOf === undefined)).toBe(true);
    expect(model.controller.size.map((token) => [token.name.camel, token.value, token.extension?.upstream])).toEqual([["outlineWidth", 1.5, "pending"]]);
    expect(source().controller).toEqual({ color: [], size: [] });
  });

  it("rejects controller tokens that clash with the main model, with each other or carry themed sizes", () => {
    const withController = (family: string, tokens: unknown[]) => () => merged({ ...rawFixtureExtensions(), [family]: { tokens } });
    expect(withController("controller-color", [{ name: "overlay", value: "#000", upstream: "pending" }])).toThrow(/controller-color\/overlay: duplicate key, already a token of the main model/);
    expect(
      withController("controller-color", [
        { name: "controller-key", value: "#000", upstream: "pending" },
        { name: "key", value: "#fff", upstream: "pending" },
      ]),
    ).toThrow(/"controller-key" and "key" both map to "key"/);
    expect(withController("controller-size", [{ name: "controller-outline-width", value: { light: "1px", dark: "2px" }, upstream: "pending" }])).toThrow(
      /controller-size\/controller-outline-width: expected a single value/,
    );
    expect(withController("controller-color", [{ name: "controller-key", value: "#000" }])).toThrow(/"upstream" must be one of/);
  });

  it("parses durations and cubic-bezier easings", () => {
    expect(parseMs("150ms")).toBe(150);
    expect(parseMs("0.7s")).toBe(700);
    expect(() => parseMs("150")).toThrow(/invalid duration/);
    expect(parseCubicBezier("cubic-bezier(0.4, 0, 0.2, 1)")).toEqual([0.4, 0, 0.2, 1]);
    expect(() => parseCubicBezier("ease")).toThrow(/invalid easing/);
    expect(() => parseCubicBezier("cubic-bezier(1.2, 0, 0.2, 1)")).toThrow(/x1 and x2 must be between 0 and 1/);
  });
});

describe("design/extensions/native-tokens.json", () => {
  const real = () => mergeExtensions(loadTsTokens(rawRealTokens()), rawRealExtensions());
  const color = (model: TokenModel, name: string) => model.color.find((token) => token.name.token === name) as ColorToken;

  it("merges cleanly into the real model with every entry pending upstream", async () => {
    const model = await readNativeModel(repoRoot);
    expect(model).toEqual(real());
    expect([model.color.length, model.duration.length, model.easing.length, model.component.length]).toEqual([37, 3, 1, 37]);
    expect([model.controller.color.length, model.controller.size.length]).toEqual([8, 1]);
    const tagged = [...allTokens(model), ...model.controller.color, ...model.controller.size].filter((token) => token.extension !== undefined);
    expect(tagged).toHaveLength(56);
    expect(tagged.every((token) => token.extension?.upstream === "pending")).toBe(true);
    expect(model.duration.map((token) => token.value)).toEqual([120, 150, 200]);
    expect(model.easing[0]?.value).toEqual([0.25, 0.1, 0.25, 1]);
    expect(rawRealExtensions().renames).toEqual({});
  });

  it("carries the analysis off-token component constants", () => {
    const values = Object.fromEntries(real().component.map((token) => [token.name.token, token.value]));
    expect(values).toMatchObject({
      "switch-track-width": 44,
      "switch-track-height": 26,
      "switch-thumb-size": 20,
      "switch-thumb-inset": 3,
      "switch-thumb-travel": 18,
      "checkbox-border-width": 1.5,
      "bottom-nav-label-font-weight": 500,
      "alert-title-font-weight": 600,
      "dialog-max-width": 440,
      "toast-max-width": 420,
      "spinner-rotation-duration": 700,
    });
  });

  it("carries the issue #3 controller palette with the raised dark backing", () => {
    const model = real();
    expect(model.controller.color.map((token) => [token.name.token, token.value.light.hex, token.value.dark.hex])).toEqual([
      ["controller-backdrop", "#fcfcfc", "#121416"],
      ["controller-key", "#18181b29", "#ffffff29"],
      ["controller-key-pressed", "#18181b6b", "#ffffff6b"],
      ["controller-key-outline", "#18181b99", "#ffffff99"],
      ["controller-key-label", "#18181b", "#ffffff"],
      ["controller-backing", "#fcfcfce6", "#000000ec"],
      ["overlay-scrim-media", "#fcfcfc99", "#00000099"],
      ["text-on-overlay-media", "#18181b", "#ffffff"],
    ]);
    expect(model.controller.size.map((token) => [token.name.token, token.value])).toEqual([["controller-outline-width", 1.5]]);
  });

  it("uses the opposite theme status value for every on-inverse token", () => {
    const model = real();
    for (const tone of ["success", "warning", "danger", "info"]) {
      const status = color(model, tone).value;
      const onInverse = color(model, `${tone}-on-inverse`).value;
      expect([onInverse.light.hex, onInverse.dark.hex]).toEqual([status.dark.hex, status.light.hex]);
    }
  });
});
