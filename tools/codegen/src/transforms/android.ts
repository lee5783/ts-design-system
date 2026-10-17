import type { ComponentToken, DimensionToken, Rgba, ShadowLayer, ShadowToken, ThemeId, TypographyToken } from "../model/types.ts";

export const ANDROID_RESOURCE_PREFIX = "ts_";

export const TRUE_WEIGHT_MIN_SDK = 28;

function hexByte(value: number): string {
  return value.toString(16).padStart(2, "0").toUpperCase();
}

export function androidArgb(rgba: Rgba): string {
  return `#${hexByte(rgba.a)}${hexByte(rgba.r)}${hexByte(rgba.g)}${hexByte(rgba.b)}`;
}

export function kotlinArgb(rgba: Rgba): string {
  return `0x${androidArgb(rgba).slice(1)}`;
}

export function androidResourceName(snake: string): string {
  return `${ANDROID_RESOURCE_PREFIX}${snake}`;
}

export function formatNumber(value: number): string {
  if (!Number.isFinite(value)) {
    throw new Error(`cannot format non-finite number ${value}`);
  }
  return String(Number(value.toFixed(4)));
}

export function kotlinFloat(value: number): string {
  return `${formatNumber(value)}f`;
}

export function componentDimensionUnit(token: ComponentToken): "dp" | "sp" {
  return token.name.token.endsWith("-font-size") ? "sp" : "dp";
}

export { isCapsuleRadius } from "../model/radius.ts";

export function isElevationShadow(token: ShadowToken): boolean {
  return Object.values(token.value).some((layers) => layers.some((layer) => layer.blur > 0 && !layer.inset));
}

export function elevationDp(layers: ShadowLayer[]): number {
  const outer = layers.filter((layer) => !layer.inset);
  if (outer.length === 0) {
    return 0;
  }
  return Math.ceil(Math.max(...outer.map((layer) => Math.max(layer.offsetY, layer.blur / 2))));
}

export function shadowColor(layers: ShadowLayer[]): Rgba {
  const outer = layers.filter((layer) => !layer.inset);
  const strongest = outer.reduce<ShadowLayer | undefined>(
    (best, layer) => (best === undefined || layer.color.rgba.a > best.color.rgba.a ? layer : best),
    undefined,
  );
  if (strongest === undefined) {
    throw new Error("shadow has no outer layer");
  }
  return { ...strongest.color.rgba, a: 255 };
}

export function shadowElevation(token: ShadowToken, theme: ThemeId = "light"): number {
  return elevationDp(token.value[theme]);
}

export interface AndroidFontFace {
  fontFamily: string;
  textStyle: "normal" | "bold";
  textFontWeight?: number;
}

export function androidFontFace(token: TypographyToken, sdk: "base" | "v28"): AndroidFontFace {
  const weight = token.fontWeight;
  const textStyle = weight >= 700 ? "bold" : "normal";
  if (token.family === "mono") {
    return sdk === "v28" ? { fontFamily: "monospace", textStyle, textFontWeight: weight } : { fontFamily: "monospace", textStyle };
  }
  if (sdk === "v28") {
    return { fontFamily: "sans-serif", textStyle, textFontWeight: weight };
  }
  if (weight >= 700) {
    return { fontFamily: "sans-serif", textStyle };
  }
  if (weight >= 500) {
    return { fontFamily: "sans-serif-medium", textStyle };
  }
  if (weight <= 300) {
    return { fontFamily: "sans-serif-light", textStyle };
  }
  return { fontFamily: "sans-serif", textStyle };
}

export const TEXT_APPEARANCE_ROOT = "TextAppearance.TS";

export function textAppearanceName(token: TypographyToken): string {
  const pascal = token.name.camel.charAt(0).toUpperCase() + token.name.camel.slice(1);
  return `${TEXT_APPEARANCE_ROOT}.${pascal}`;
}

export const SHAPE_APPEARANCE_ROOT = "ShapeAppearance.TS";
export const THEME_BASE = "Base.Theme.TS";
export const THEME_OVERLAY_BASE = "Base.ThemeOverlay.TS";

export function shapeAppearanceName(token: DimensionToken): string {
  const suffix = token.name.camel.replace(/^radius/, "");
  return `${SHAPE_APPEARANCE_ROOT}.${suffix.charAt(0).toUpperCase()}${suffix.slice(1)}`;
}

export function themeColorAttrName(camel: string): string {
  return `tsColor${camel.charAt(0).toUpperCase()}${camel.slice(1)}`;
}

const KOTLIN_HARD_KEYWORDS = new Set([
  "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "interface", "is", "null",
  "object", "package", "return", "super", "this", "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while",
]);

export function kotlinIdentifier(camel: string): string {
  return KOTLIN_HARD_KEYWORDS.has(camel) ? `\`${camel}\`` : camel;
}
