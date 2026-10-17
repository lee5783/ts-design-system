import type { ShadowLayer, ShadowToken } from "../../model/types.ts";
import { assertIosShadowLayer, swiftIdentifier, swiftNumber, swiftRgbaHex, swiftString } from "../../transforms/swift.ts";
import { active, block, indent, staticLet, swiftFile, tokenList } from "./swift-file.ts";

const LAYER_TYPE = block("public struct TSShadowLayer: Hashable, Sendable", [
  "public let offsetX: CGFloat",
  "public let offsetY: CGFloat",
  "public let blur: CGFloat",
  "public let spread: CGFloat",
  "public let color: TSRGBA",
  "",
  "public var radius: CGFloat {",
  "    blur / 2",
  "}",
  "",
  "public var opacity: Float {",
  "    Float(color.opacity)",
  "}",
  "",
  "public var offset: CGSize {",
  "    CGSize(width: offsetX, height: offsetY)",
  "}",
]);

const TOKEN_TYPE = block("public struct TSShadowToken: Hashable, Sendable", [
  "public let name: String",
  "public let light: [TSShadowLayer]",
  "public let dark: [TSShadowLayer]",
  "",
  "public func layers(for style: UIUserInterfaceStyle) -> [TSShadowLayer] {",
  "    style == .dark ? dark : light",
  "}",
]);

function layerInit(token: string, layer: ShadowLayer): string {
  assertIosShadowLayer(token, layer);
  return `TSShadowLayer(offsetX: ${swiftNumber(layer.offsetX)}, offsetY: ${swiftNumber(layer.offsetY)}, blur: ${swiftNumber(layer.blur)}, spread: ${swiftNumber(layer.spread)}, color: TSRGBA(hex: ${swiftRgbaHex(layer.color.rgba)}))`;
}

function layerList(label: string, token: string, layers: ShadowLayer[], last: boolean): string[] {
  const close = last ? "]" : "],";
  if (layers.length === 0) {
    return [`${label}: []${last ? "" : ","}`];
  }
  return [`${label}: [`, ...indent(layers.map((layer) => `${layerInit(token, layer)},`)), close];
}

function constant(token: ShadowToken): string[] {
  return staticLet(token, undefined, () => [
    "TSShadowToken(",
    ...indent([
      `name: ${swiftString(token.name.token)},`,
      ...layerList("light", token.name.token, token.value.light, false),
      ...layerList("dark", token.name.token, token.value.dark, true),
    ]),
    ")",
  ]);
}

export function swiftShadows(tokens: ShadowToken[]): string {
  const constants = tokens.flatMap(constant);
  const catalog = tokenList("TSShadowToken", active(tokens).map((token) => swiftIdentifier(token.name.camel)));
  return swiftFile(["UIKit"], [LAYER_TYPE, TOKEN_TYPE, block("public enum TSShadowTokens", [...constants, "", ...catalog])]);
}
