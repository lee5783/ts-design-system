import type { ComponentConstantType, ComponentToken } from "../../model/types.ts";
import { msToSeconds, swiftNumber } from "../../transforms/swift.ts";
import { block, staticLet, swiftFile } from "./swift-file.ts";

const SWIFT_TYPES: Record<ComponentConstantType, string> = {
  dimension: "CGFloat",
  "font-weight": "Int",
  duration: "TimeInterval",
  number: "Double",
};

function swiftValue(token: ComponentToken): string {
  return swiftNumber(token.type === "duration" ? msToSeconds(token.value) : token.value);
}

export function swiftComponents(tokens: ComponentToken[]): string {
  const constants = tokens.flatMap((token) => staticLet(token, SWIFT_TYPES[token.type], () => [swiftValue(token)]));
  return swiftFile(["Foundation"], [block("public enum TSComponentTokens", constants)]);
}
