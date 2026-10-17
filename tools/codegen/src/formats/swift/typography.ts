import type { TypographyToken } from "../../model/types.ts";
import { IOS_BASE_TEXT_STYLES, iosBaseTextStyle, swiftIdentifier, swiftNumber, swiftString, trackingPoints } from "../../transforms/swift.ts";
import { active, block, staticLet, swiftFile, tokenList } from "./swift-file.ts";

const SWIFTUI_TEXT_STYLES: Record<keyof typeof IOS_BASE_TEXT_STYLES, string> = {
  largeTitle: "largeTitle",
  title1: "title",
  title2: "title2",
  title3: "title3",
  body: "body",
  callout: "callout",
  subheadline: "subheadline",
  footnote: "footnote",
  caption1: "caption",
  caption2: "caption2",
};

const FONT_WEIGHTS: [number, string][] = [
  [100, "ultraLight"],
  [200, "thin"],
  [300, "light"],
  [400, "regular"],
  [500, "medium"],
  [600, "semibold"],
  [700, "bold"],
  [800, "heavy"],
  [900, "black"],
];

const FONT_FAMILY = block("public enum TSFontFamily: String, Hashable, Sendable", ["case sans", "case mono"]);

const BASE_TEXT_STYLE = block("public enum TSBaseTextStyle: String, CaseIterable, Hashable, Sendable", [
  ...Object.keys(IOS_BASE_TEXT_STYLES).map((style) => `case ${style}`),
  "",
  "public var defaultPointSize: CGFloat {",
  "    switch self {",
  ...Object.entries(IOS_BASE_TEXT_STYLES).map(([style, size]) => `    case .${style}: ${size}`),
  "    }",
  "}",
  "",
  "public var uiTextStyle: UIFont.TextStyle {",
  "    switch self {",
  ...Object.keys(IOS_BASE_TEXT_STYLES).map((style) => `    case .${style}: .${style}`),
  "    }",
  "}",
  "",
  "public var textStyle: Font.TextStyle {",
  "    switch self {",
  ...Object.entries(SWIFTUI_TEXT_STYLES).map(([style, swiftUI]) => `    case .${style}: .${swiftUI}`),
  "    }",
  "}",
]);

const FONT_WEIGHT = block("public enum TSFontWeight", [
  "public static func uiFontWeight(_ weight: Int) -> UIFont.Weight {",
  "    switch weight {",
  ...FONT_WEIGHTS.slice(0, -1).map(([weight, name]) => `    case ..<${weight + 50}: .${name}`),
  `    default: .${FONT_WEIGHTS.at(-1)?.[1]}`,
  "    }",
  "}",
  "",
  "public static func fontWeight(_ weight: Int) -> Font.Weight {",
  "    switch weight {",
  ...FONT_WEIGHTS.slice(0, -1).map(([weight, name]) => `    case ..<${weight + 50}: .${name}`),
  `    default: .${FONT_WEIGHTS.at(-1)?.[1]}`,
  "    }",
  "}",
]);

const TOKEN_TYPE = block("public struct TSTypographyToken: Hashable, Sendable", [
  "public let name: String",
  "public let family: TSFontFamily",
  "public let fontSize: CGFloat",
  "public let lineHeight: CGFloat",
  "public let fontWeight: Int",
  "public let letterSpacingEm: CGFloat",
  "public let tracking: CGFloat",
  "public let baseTextStyle: TSBaseTextStyle",
  "",
  "public var uiFontWeight: UIFont.Weight {",
  "    TSFontWeight.uiFontWeight(fontWeight)",
  "}",
  "",
  "public var swiftUIFontWeight: Font.Weight {",
  "    TSFontWeight.fontWeight(fontWeight)",
  "}",
]);

function tokenInit(token: TypographyToken): string {
  return [
    `TSTypographyToken(name: ${swiftString(token.name.token)}`,
    `family: .${token.family}`,
    `fontSize: ${swiftNumber(token.fontSize)}`,
    `lineHeight: ${swiftNumber(token.lineHeight)}`,
    `fontWeight: ${swiftNumber(token.fontWeight)}`,
    `letterSpacingEm: ${swiftNumber(token.letterSpacingEm)}`,
    `tracking: ${swiftNumber(trackingPoints(token))}`,
    `baseTextStyle: .${iosBaseTextStyle(token)})`,
  ].join(", ");
}

export function swiftTypography(tokens: TypographyToken[]): string {
  const constants = tokens.flatMap((token) => staticLet(token, undefined, () => [tokenInit(token)]));
  const catalog = tokenList("TSTypographyToken", active(tokens).map((token) => swiftIdentifier(token.name.camel)));
  return swiftFile(
    ["SwiftUI", "UIKit"],
    [FONT_FAMILY, BASE_TEXT_STYLE, FONT_WEIGHT, TOKEN_TYPE, block("public enum TSTypographyTokens", [...constants, "", ...catalog])],
  );
}
