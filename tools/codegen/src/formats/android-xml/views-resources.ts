import type { GeneratedFile, Generator } from "../../generate.ts";
import type { DimensionToken, TokenModel, TypographyToken } from "../../model/types.ts";
import {
  SHAPE_APPEARANCE_ROOT,
  TEXT_APPEARANCE_ROOT,
  THEME_BASE,
  THEME_OVERLAY_BASE,
  androidFontFace,
  androidResourceName,
  formatNumber,
  isCapsuleRadius,
  shapeAppearanceName,
  textAppearanceName,
  themeColorAttrName,
} from "../../transforms/android.ts";
import { resourcesFile, style } from "./xml-file.ts";

export const VIEWS_RES_DIR = "android/views/src/generated/res";

function textAppearanceItems(token: TypographyToken, sdk: "base" | "v28"): [string, string][] {
  const face = androidFontFace(token, sdk);
  const lineHeight = `${formatNumber(token.lineHeight)}sp`;
  return [
    ["android:textSize", `${formatNumber(token.fontSize)}sp`],
    ["android:fontFamily", face.fontFamily],
    ["android:textStyle", face.textStyle],
    ...(face.textFontWeight === undefined ? [] : ([["android:textFontWeight", String(face.textFontWeight)]] as [string, string][])),
    ["android:letterSpacing", formatNumber(token.letterSpacingEm)],
    ...(sdk === "v28" ? ([["android:lineHeight", lineHeight]] as [string, string][]) : []),
    ["lineHeight", lineHeight],
  ];
}

export function androidTextAppearances(model: TokenModel, sdk: "base" | "v28"): string {
  const entries = [
    ...style(TEXT_APPEARANCE_ROOT, [], ""),
    ...model.typography.flatMap((token) => style(textAppearanceName(token), textAppearanceItems(token, sdk))),
  ];
  return resourcesFile(entries, STYLE_FILE_ATTRIBUTES);
}

export function androidThemeAttrs(model: TokenModel): string {
  return resourcesFile(model.color.map((token) => `<attr name="${themeColorAttrName(token.name.camel)}" format="color" />`));
}

const STYLE_FILE_ATTRIBUTES = ['xmlns:tools="http://schemas.android.com/tools"', 'tools:ignore="ResourceName"'];

export function androidThemeColors(model: TokenModel): string {
  const items = model.color.map((token): [string, string] => [themeColorAttrName(token.name.camel), `@color/${androidResourceName(token.name.snake)}`]);
  const entries = [
    ...style(THEME_BASE, items, "Theme.Material3.DayNight.NoActionBar"),
    ...style(THEME_OVERLAY_BASE, items, "ThemeOverlay.Material3"),
  ];
  return resourcesFile(entries, STYLE_FILE_ATTRIBUTES);
}

function cornerSize(token: DimensionToken): string {
  return isCapsuleRadius(token.value) ? "50%" : `@dimen/${androidResourceName(token.name.snake)}`;
}

export function androidShapeAppearances(model: TokenModel): string {
  const entries = [
    ...style(SHAPE_APPEARANCE_ROOT, [["cornerFamily", "rounded"]], ""),
    ...model.radius
      .filter((token) => token.deprecated === undefined)
      .flatMap((token) => style(shapeAppearanceName(token), [["cornerSize", cornerSize(token)]])),
  ];
  return resourcesFile(entries, STYLE_FILE_ATTRIBUTES);
}

export function androidViewsResources(model: TokenModel): GeneratedFile[] {
  return [
    { path: `${VIEWS_RES_DIR}/values/ts_text_appearances.xml`, contents: androidTextAppearances(model, "base") },
    { path: `${VIEWS_RES_DIR}/values-v28/ts_text_appearances.xml`, contents: androidTextAppearances(model, "v28") },
    { path: `${VIEWS_RES_DIR}/values/ts_theme_attrs.xml`, contents: androidThemeAttrs(model) },
    { path: `${VIEWS_RES_DIR}/values/ts_theme_colors.xml`, contents: androidThemeColors(model) },
    { path: `${VIEWS_RES_DIR}/values/ts_shape_appearances.xml`, contents: androidShapeAppearances(model) },
  ];
}

export const androidViewsResourcesGenerator: Generator = {
  name: "android-views-res",
  target: "tokens",
  outputDir: VIEWS_RES_DIR,
  generate: ({ model }) => androidViewsResources(model),
};
