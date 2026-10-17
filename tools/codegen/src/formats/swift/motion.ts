import type { DurationToken, EasingToken } from "../../model/types.ts";
import { msToSeconds, swiftIdentifier, swiftNumber, swiftString } from "../../transforms/swift.ts";
import { active, block, staticLet, swiftFile, tokenList } from "./swift-file.ts";

const BEZIER_TYPE = block("public struct TSCubicBezier: Hashable, Sendable", [
  "public let x1: Double",
  "public let y1: Double",
  "public let x2: Double",
  "public let y2: Double",
  "",
  "@MainActor",
  "public var timingParameters: UICubicTimingParameters {",
  "    UICubicTimingParameters(controlPoint1: CGPoint(x: x1, y: y1), controlPoint2: CGPoint(x: x2, y: y2))",
  "}",
  "",
  "public func animation(duration: TimeInterval) -> Animation {",
  "    .timingCurve(x1, y1, x2, y2, duration: duration)",
  "}",
]);

const DURATION_TYPE = block("public struct TSDurationToken: Hashable, Sendable", ["public let name: String", "public let value: TimeInterval"]);

const EASING_TYPE = block("public struct TSEasingToken: Hashable, Sendable", ["public let name: String", "public let value: TSCubicBezier"]);

export function swiftMotion(durations: DurationToken[], easings: EasingToken[]): string {
  const durationConstants = durations.flatMap((token) =>
    staticLet(token, "TimeInterval", () => [swiftNumber(msToSeconds(token.value))]),
  );
  const easingConstants = easings.flatMap((token) =>
    staticLet(token, undefined, () => [
      `TSCubicBezier(${token.value.map((value, index) => `${["x1", "y1", "x2", "y2"][index]}: ${swiftNumber(value)}`).join(", ")})`,
    ]),
  );
  const motion = block("public enum TSMotion", [
    ...durationConstants,
    ...easingConstants,
    "",
    ...tokenList(
      "TSDurationToken",
      active(durations).map((token) => `TSDurationToken(name: ${swiftString(token.name.token)}, value: ${swiftIdentifier(token.name.camel)})`),
      "allDurations",
    ),
    ...tokenList(
      "TSEasingToken",
      active(easings).map((token) => `TSEasingToken(name: ${swiftString(token.name.token)}, value: ${swiftIdentifier(token.name.camel)})`),
      "allEasings",
    ),
  ]);
  return swiftFile(["SwiftUI", "UIKit"], [BEZIER_TYPE, DURATION_TYPE, EASING_TYPE, motion]);
}
