import type { DimensionToken, OpacityToken } from "../../model/types.ts";
import { isCapsuleRadius, swiftIdentifier, swiftNumber, swiftString } from "../../transforms/swift.ts";
import { active, block, staticLet, swiftFile, tokenList } from "./swift-file.ts";

function constants(tokens: (DimensionToken | OpacityToken)[], type: string): string[] {
  return tokens.flatMap((token) => staticLet(token, type, () => [swiftNumber(token.value)]));
}

export function swiftDimensionToken(): string {
  return swiftFile(
    ["CoreGraphics"],
    [block("public struct TSDimensionToken: Hashable, Sendable", ["public let name: String", "public let value: CGFloat"])],
  );
}

export function swiftDimensions(typeName: string, tokens: DimensionToken[]): string {
  const catalog = tokenList(
    "TSDimensionToken",
    active(tokens).map((token) => `TSDimensionToken(name: ${swiftString(token.name.token)}, value: ${swiftIdentifier(token.name.camel)})`),
  );
  return swiftFile(["CoreGraphics"], [block(`public enum ${typeName}`, [...constants(tokens, "CGFloat"), "", ...catalog])]);
}

export function swiftRadius(tokens: DimensionToken[]): string {
  const catalog = tokenList(
    "TSRadiusToken",
    active(tokens).map(
      (token) =>
        `TSRadiusToken(name: ${swiftString(token.name.token)}, value: ${swiftIdentifier(token.name.camel)}, isCapsule: ${isCapsuleRadius(token.value)})`,
    ),
  );
  const tokenType = block("public struct TSRadiusToken: Hashable, Sendable", [
    "public let name: String",
    "public let value: CGFloat",
    "public let isCapsule: Bool",
    "",
    "public func cornerRadius(forHeight height: CGFloat) -> CGFloat {",
    "    isCapsule ? height / 2 : TSRadius.cornerRadius(value, forHeight: height)",
    "}",
  ]);
  const radius = block("public enum TSRadius", [
    ...constants(tokens, "CGFloat"),
    "",
    ...catalog,
    "",
    "public static func cornerRadius(_ radius: CGFloat, forHeight height: CGFloat) -> CGFloat {",
    "    min(radius, height / 2)",
    "}",
  ]);
  return swiftFile(["CoreGraphics"], [tokenType, radius]);
}

export function swiftOpacity(tokens: OpacityToken[]): string {
  const catalog = tokenList(
    "TSOpacityToken",
    active(tokens).map((token) => `TSOpacityToken(name: ${swiftString(token.name.token)}, value: ${swiftIdentifier(token.name.camel)})`),
  );
  const tokenType = block("public struct TSOpacityToken: Hashable, Sendable", ["public let name: String", "public let value: Double"]);
  return swiftFile(["Foundation"], [tokenType, block("public enum TSOpacity", [...constants(tokens, "Double"), "", ...catalog])]);
}
