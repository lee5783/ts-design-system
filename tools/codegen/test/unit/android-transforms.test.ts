import { describe, expect, it } from "vitest";
import { androidColors, androidDimens } from "../../src/formats/android-xml/tokens-resources.ts";
import { androidShapeAppearances, androidTextAppearances, androidThemeAttrs, androidThemeColors } from "../../src/formats/android-xml/views-resources.ts";
import { composeColors, composeTypography } from "../../src/formats/compose/compose-theme.ts";
import { tokenValuesKotlin } from "../../src/formats/compose/token-values.ts";
import { readNativeModel } from "../../src/model/merge-extensions.ts";
import type { ComponentToken, ShadowToken, TypographyToken } from "../../src/model/types.ts";
import {
  androidArgb,
  androidFontFace,
  componentDimensionUnit,
  elevationDp,
  formatNumber,
  isCapsuleRadius,
  isElevationShadow,
  kotlinArgb,
  kotlinFloat,
  kotlinIdentifier,
  shadowColor,
  textAppearanceName,
  themeColorAttrName,
} from "../../src/transforms/android.ts";
import { parseHexColor, parseShadow } from "../../src/transforms/css-values.ts";
import { tokenName } from "../../src/transforms/naming.ts";
import { repoRoot } from "../support.ts";

const typography = (name: string, fontWeight: number, family: "sans" | "mono" = "sans"): TypographyToken => ({
  name: tokenName(name),
  group: "Fixture",
  family,
  fontSize: 22,
  lineHeight: 28,
  fontWeight,
  letterSpacingEm: 0,
});

describe("Android transforms", () => {
  it("reorders CSS #RRGGBBAA into Android #AARRGGBB and Kotlin ARGB literals", () => {
    expect(androidArgb(parseHexColor("#18181b66").rgba)).toBe("#6618181B");
    expect(androidArgb(parseHexColor("#000000a3").rgba)).toBe("#A3000000");
    expect(androidArgb(parseHexColor("#7c3aed").rgba)).toBe("#FF7C3AED");
    expect(kotlinArgb(parseHexColor("#18181b66").rgba)).toBe("0x6618181B");
  });

  it("formats numbers without float noise", () => {
    expect(formatNumber(2)).toBe("2");
    expect(formatNumber(1.5)).toBe("1.5");
    expect(formatNumber(0.1 + 0.2)).toBe("0.3");
    expect(kotlinFloat(-0.02)).toBe("-0.02f");
    expect(() => formatNumber(Number.NaN)).toThrow(/non-finite/);
  });

  it("maps weights to fonts with the API 26-27 medium fallback for 600", () => {
    expect(androidFontFace(typography("body", 400), "base")).toEqual({ fontFamily: "sans-serif", textStyle: "normal" });
    expect(androidFontFace(typography("label", 500), "base")).toEqual({ fontFamily: "sans-serif-medium", textStyle: "normal" });
    expect(androidFontFace(typography("title-2", 600), "base")).toEqual({ fontFamily: "sans-serif-medium", textStyle: "normal" });
    expect(androidFontFace(typography("title-1", 700), "base")).toEqual({ fontFamily: "sans-serif", textStyle: "bold" });
    expect(androidFontFace(typography("thin", 300), "base")).toEqual({ fontFamily: "sans-serif-light", textStyle: "normal" });
    expect(androidFontFace(typography("title-2", 600), "v28")).toEqual({ fontFamily: "sans-serif", textStyle: "normal", textFontWeight: 600 });
    expect(androidFontFace(typography("title-1", 700), "v28")).toEqual({ fontFamily: "sans-serif", textStyle: "bold", textFontWeight: 700 });
    expect(androidFontFace(typography("code", 400, "mono"), "base")).toEqual({ fontFamily: "monospace", textStyle: "normal" });
    expect(androidFontFace(typography("code", 400, "mono"), "v28")).toEqual({ fontFamily: "monospace", textStyle: "normal", textFontWeight: 400 });
  });

  it("approximates shadows as elevation and keeps the focus ring out", () => {
    const shadow = (light: string, dark: string): ShadowToken => ({
      name: tokenName("shadow-x"),
      value: { light: parseShadow(light), dark: parseShadow(dark) },
    });
    expect(elevationDp(parseShadow("0 1px 2px #18181b0d"))).toBe(1);
    expect(elevationDp(parseShadow("0 1px 2px #18181b0f, 0 1px 3px #18181b14"))).toBe(2);
    expect(elevationDp(parseShadow("0 12px 32px -4px #18181b29, 0 4px 8px -2px #18181b14"))).toBe(16);
    expect(androidArgb(shadowColor(parseShadow("0 1px 2px #18181b0f, 0 1px 3px #18181b14")))).toBe("#FF18181B");
    expect(isElevationShadow(shadow("0 4px 6px #18181b14", "0 4px 10px #0000008c"))).toBe(true);
    expect(isElevationShadow(shadow("0 0 0 2px #fcfcfc, 0 0 0 4px #7c3aed", "0 0 0 2px #1e1e1e, 0 0 0 4px #a78bfa"))).toBe(false);
  });

  it("names resources, attrs and Kotlin identifiers", () => {
    expect(textAppearanceName(typography("title-1", 700))).toBe("TextAppearance.TS.Title1");
    expect(textAppearanceName(typography("body-lg", 400))).toBe("TextAppearance.TS.BodyLg");
    expect(themeColorAttrName("accentPrimary")).toBe("tsColorAccentPrimary");
    expect(kotlinIdentifier("object")).toBe("`object`");
    expect(kotlinIdentifier("space0_5")).toBe("space0_5");
    expect(isCapsuleRadius(9999)).toBe(true);
    expect(isCapsuleRadius(16)).toBe(false);
    const component = (name: string): ComponentToken => ({ name: tokenName(name), component: "x", type: "dimension", value: 1 });
    expect(componentDimensionUnit(component("button-sm-font-size"))).toBe("sp");
    expect(componentDimensionUnit(component("switch-track-width"))).toBe("dp");
  });
});

describe("Android formats on the real tokens", () => {
  it("emits overlay and every color in both themes", async () => {
    const model = await readNativeModel(repoRoot);
    const light = androidColors(model, "light");
    const dark = androidColors(model, "dark");
    expect(light).toContain('<color name="ts_overlay">#6618181B</color>');
    expect(dark).toContain('<color name="ts_overlay">#A3000000</color>');
    expect(light).toContain('<color name="ts_focus">@color/ts_accent_text</color>');
    expect(dark).not.toContain("ts_focus");
    expect(light.match(/<color /g)).toHaveLength(37);
    expect(dark.match(/<color /g)).toHaveLength(36);
    expect(androidThemeAttrs(model).match(/<attr /g)).toHaveLength(37);
  });

  it("maps every theme color attr to its color resource in the base theme and overlay", async () => {
    const themes = androidThemeColors(await readNativeModel(repoRoot));
    expect(themes).toContain('<style name="Base.Theme.TS" parent="Theme.Material3.DayNight.NoActionBar">');
    expect(themes).toContain('<style name="Base.ThemeOverlay.TS" parent="ThemeOverlay.Material3">');
    expect(themes).toContain('<item name="tsColorAccentPrimary">@color/ts_accent_primary</item>');
    expect(themes.match(/<item name="tsColor/g)).toHaveLength(74);
    expect(themes).toContain('tools:ignore="ResourceName"');
  });

  it("emits rounded shape appearances per radius with a 50% capsule", async () => {
    const shapes = androidShapeAppearances(await readNativeModel(repoRoot));
    expect(shapes).toContain('<item name="cornerFamily">rounded</item>');
    expect(shapes).toContain('<style name="ShapeAppearance.TS.Md">');
    expect(shapes).toContain('<item name="cornerSize">@dimen/ts_radius_md</item>');
    expect(shapes).toMatch(/<style name="ShapeAppearance.TS.Full">\n\s+<item name="cornerSize">50%<\/item>/);
  });

  it("emits dimens in dp, component font sizes in sp, and opacity as float", async () => {
    const dimens = androidDimens(await readNativeModel(repoRoot));
    expect(dimens).toContain('<dimen name="ts_space_0_5">2dp</dimen>');
    expect(dimens).toContain('<dimen name="ts_radius_full">9999dp</dimen>');
    expect(dimens).toContain('<dimen name="ts_button_sm_font_size">13sp</dimen>');
    expect(dimens).toContain('<item name="ts_opacity_pressed" format="float" type="dimen">0.85</item>');
  });

  it("emits both line height attrs and the weight 600 split per API level", async () => {
    const model = await readNativeModel(repoRoot);
    const base = androidTextAppearances(model, "base");
    const v28 = androidTextAppearances(model, "v28");
    const styleOf = (xml: string, name: string) => xml.slice(xml.indexOf(`<style name="${name}">`), xml.indexOf("</style>", xml.indexOf(`<style name="${name}">`)));
    expect(styleOf(base, "TextAppearance.TS.Title2")).toContain('<item name="android:fontFamily">sans-serif-medium</item>');
    expect(styleOf(base, "TextAppearance.TS.Title2")).not.toContain("android:textFontWeight");
    expect(styleOf(base, "TextAppearance.TS.Title2")).not.toContain("android:lineHeight");
    expect(styleOf(base, "TextAppearance.TS.Title2")).toContain('<item name="lineHeight">28sp</item>');
    expect(styleOf(v28, "TextAppearance.TS.Title2")).toContain('<item name="android:textFontWeight">600</item>');
    expect(styleOf(v28, "TextAppearance.TS.Title2")).toContain('<item name="android:lineHeight">28sp</item>');
    expect(styleOf(v28, "TextAppearance.TS.Title2")).toContain('<item name="lineHeight">28sp</item>');
    expect(styleOf(v28, "TextAppearance.TS.Overline")).toContain('<item name="android:letterSpacing">0.06</item>');
    expect(base.match(/<style name="TextAppearance\.TS\./g)).toHaveLength(11);
    expect(base).toContain('<style name="TextAppearance.TS" parent="" />');
    expect(base).toContain('tools:ignore="ResourceName"');
  });

  it("emits Kotlin values and Compose types for every token", async () => {
    const model = await readNativeModel(repoRoot);
    const values = tokenValuesKotlin(model);
    expect(values).toContain("public const val overlay: Long = 0x6618181B");
    expect(values).toContain("public const val overlay: Long = 0xA3000000");
    expect(values).toContain("public const val focus: Long = accentText");
    expect(values).toContain("public val shadowLg: TSShadowValues = TSShadowValues(16f, 0xFF18181B, 0xFF000000)");
    expect(values).not.toContain("shadowFocus");
    expect(values).toContain("public const val checkboxBorderWidth: Float = 1.5f");
    expect(values).toContain("public const val alertTitleFontWeight: Int = 600");
    expect(composeColors(model).match(/public val \w+: Color,/g)).toHaveLength(37);
    expect(composeTypography(model).match(/= tsTextStyle\(/g)).toHaveLength(11);
  });
});
