import type { Rgba, ShadowLayer, TypographyToken } from "../model/types.ts";

export const IOS_BASE_TEXT_STYLES = {
  largeTitle: 34,
  title1: 28,
  title2: 22,
  title3: 20,
  body: 17,
  callout: 16,
  subheadline: 15,
  footnote: 13,
  caption1: 12,
  caption2: 11,
} as const;

export type IosBaseTextStyle = keyof typeof IOS_BASE_TEXT_STYLES;

const ROLE_TEXT_STYLES: Record<string, IosBaseTextStyle> = {
  display: "largeTitle",
  "title-1": "title1",
  "title-2": "title2",
  "title-3": "title3",
  "body-lg": "body",
  body: "callout",
  "body-sm": "subheadline",
  label: "subheadline",
  caption: "caption1",
  overline: "caption2",
  code: "footnote",
};

const SWIFT_KEYWORDS = new Set([
  "associatedtype", "class", "default", "deinit", "enum", "extension", "func", "import", "init", "inout", "internal",
  "let", "operator", "private", "protocol", "public", "repeat", "return", "self", "static", "struct", "subscript",
  "super", "switch", "true", "false", "nil", "try", "throw", "throws", "case", "where", "while", "var", "if", "else",
  "for", "in", "do", "is", "as", "break", "continue", "fallthrough", "guard", "defer", "catch", "rethrows", "Type",
]);

export function swiftIdentifier(camel: string): string {
  return SWIFT_KEYWORDS.has(camel) ? `\`${camel}\`` : camel;
}

export function swiftPrefixedName(prefix: string, camel: string): string {
  return `${prefix}${camel.charAt(0).toUpperCase()}${camel.slice(1)}`;
}

export function swiftNumber(value: number): string {
  if (!Number.isFinite(value)) {
    throw new Error(`cannot emit ${value} as a Swift number literal`);
  }
  const rounded = Number(value.toFixed(6));
  return Object.is(rounded, -0) ? "0" : String(rounded);
}

export function swiftString(value: string): string {
  return JSON.stringify(value);
}

export function swiftRgbaHex(rgba: Rgba): string {
  const hex = [rgba.r, rgba.g, rgba.b, rgba.a].map((byte) => byte.toString(16).padStart(2, "0")).join("").toUpperCase();
  return `0x${hex}`;
}

export function iosBaseTextStyle(token: TypographyToken): IosBaseTextStyle {
  const role = ROLE_TEXT_STYLES[token.name.token];
  if (role !== undefined) {
    return role;
  }
  let best: IosBaseTextStyle = "body";
  for (const [style, size] of Object.entries(IOS_BASE_TEXT_STYLES) as [IosBaseTextStyle, number][]) {
    if (Math.abs(size - token.fontSize) < Math.abs(IOS_BASE_TEXT_STYLES[best] - token.fontSize)) {
      best = style;
    }
  }
  return best;
}

export function trackingPoints(token: TypographyToken): number {
  return Number((token.letterSpacingEm * token.fontSize).toFixed(4));
}

export function iosShadowRadius(layer: ShadowLayer): number {
  return layer.blur / 2;
}

export function assertIosShadowLayer(token: string, layer: ShadowLayer): void {
  if (layer.inset) {
    throw new Error(`${token}: inset shadows have no iOS layer equivalent`);
  }
}

export { isCapsuleRadius } from "../model/radius.ts";

export function msToSeconds(ms: number): number {
  return Number((ms / 1000).toFixed(6));
}
