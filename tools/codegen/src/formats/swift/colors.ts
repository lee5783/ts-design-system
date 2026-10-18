import type { ColorToken } from "../../model/types.ts";
import { toCamelName } from "../../transforms/naming.ts";
import { swiftIdentifier, swiftPrefixedName, swiftRgbaHex, swiftString } from "../../transforms/swift.ts";
import { active, block, currentCamel, deprecation, staticLet, swiftFile, tokenList } from "./swift-file.ts";

const RGBA_TYPE = block("public struct TSRGBA: Hashable, Sendable", [
  "public let red: UInt8",
  "public let green: UInt8",
  "public let blue: UInt8",
  "public let alpha: UInt8",
  "",
  "public init(hex: UInt32) {",
  "    red = UInt8((hex >> 24) & 0xFF)",
  "    green = UInt8((hex >> 16) & 0xFF)",
  "    blue = UInt8((hex >> 8) & 0xFF)",
  "    alpha = UInt8(hex & 0xFF)",
  "}",
  "",
  "public var hex: UInt32 {",
  "    UInt32(red) << 24 | UInt32(green) << 16 | UInt32(blue) << 8 | UInt32(alpha)",
  "}",
  "",
  "public var opacity: Double {",
  "    Double(alpha) / 255",
  "}",
  "",
  "public var uiColor: UIColor {",
  "    UIColor(red: CGFloat(red) / 255, green: CGFloat(green) / 255, blue: CGFloat(blue) / 255, alpha: CGFloat(alpha) / 255)",
  "}",
]);

const COLOR_TYPE = block("public struct TSColor: Hashable, Sendable", [
  "public let name: String",
  "public let light: TSRGBA",
  "public let dark: TSRGBA",
  "",
  "public func rgba(for style: UIUserInterfaceStyle) -> TSRGBA {",
  "    style == .dark ? dark : light",
  "}",
  "",
  "public var uiColor: UIColor {",
  "    let light = light",
  "    let dark = dark",
  "    return UIColor(dynamicProvider: { traits in",
  "        traits.userInterfaceStyle == .dark ? dark.uiColor : light.uiColor",
  "    })",
  "}",
  "",
  "public var color: Color {",
  "    Color(uiColor: uiColor)",
  "}",
]);

function tsName(token: string): string {
  return swiftPrefixedName("ts", toCamelName(token));
}

export function swiftColors(tokens: ColorToken[]): string {
  const constants = tokens.flatMap((token) =>
    staticLet(token, undefined, () => [
      `TSColor(name: ${swiftString(token.name.token)}, light: TSRGBA(hex: ${swiftRgbaHex(token.value.light.rgba)}), dark: TSRGBA(hex: ${swiftRgbaHex(token.value.dark.rgba)}))`,
    ]),
  );
  const catalog = tokenList("TSColor", active(tokens).map((token) => swiftIdentifier(token.name.camel)));
  const uiColors = tokens.flatMap((token) => [
    ...deprecation(token, tsName),
    `public static var ${tsName(token.name.token)}: UIColor { TSColor.${swiftIdentifier(currentCamel(token))}.uiColor }`,
  ]);
  const colors = tokens.flatMap((token) => [
    ...deprecation(token, tsName),
    `public static var ${tsName(token.name.token)}: Color { TSColor.${swiftIdentifier(currentCamel(token))}.color }`,
  ]);
  return swiftFile(
    ["SwiftUI", "UIKit"],
    [
      RGBA_TYPE,
      COLOR_TYPE,
      block("extension TSColor", [...constants, "", ...catalog]),
      block("extension UIColor", uiColors),
      block("extension ShapeStyle where Self == Color", colors),
    ],
  );
}

export function swiftControllerColors(tokens: ColorToken[]): string {
  const constants = tokens.flatMap((token) =>
    staticLet(token, undefined, () => [
      `TSColor(name: ${swiftString(token.name.token)}, light: TSRGBA(hex: ${swiftRgbaHex(token.value.light.rgba)}), dark: TSRGBA(hex: ${swiftRgbaHex(token.value.dark.rgba)}))`,
    ]),
  );
  const catalog = tokenList("TSColor", active(tokens).map((token) => swiftIdentifier(token.name.camel)));
  return swiftFile(["SwiftUI", "UIKit"], [block("public enum TSControllerColor", [...constants, "", ...catalog])]);
}
