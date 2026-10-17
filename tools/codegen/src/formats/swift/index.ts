import type { GeneratedFile, Generator } from "../../generate.ts";
import { cachedVectorAssets } from "../../icons/vector-assets.ts";
import type { TokenModel } from "../../model/types.ts";
import { swiftIconNameFile } from "../icons/icon-enums.ts";
import { swiftColors } from "./colors.ts";
import { swiftComponents } from "./components.ts";
import { swiftDimensionToken, swiftDimensions, swiftOpacity, swiftRadius } from "./dimensions.ts";
import { swiftMotion } from "./motion.ts";
import { swiftShadows } from "./shadows.ts";
import { swiftTypography } from "./typography.ts";

export const SWIFT_TOKENS_DIR = "ios/Sources/TSTokens/Generated";

export function swiftTokenFiles(model: TokenModel, outputDir: string = SWIFT_TOKENS_DIR): GeneratedFile[] {
  const file = (name: string, contents: string): GeneratedFile => ({ path: `${outputDir}/${name}`, contents });
  return [
    file("TSColor.swift", swiftColors(model.color)),
    file("TSDimensionToken.swift", swiftDimensionToken()),
    file("TSSpacing.swift", swiftDimensions("TSSpacing", model.spacing)),
    file("TSRadius.swift", swiftRadius(model.radius)),
    file("TSSize.swift", swiftDimensions("TSSize", model.size)),
    file("TSOpacity.swift", swiftOpacity(model.opacity)),
    file("TSShadowTokens.swift", swiftShadows(model.shadow)),
    file("TSTypographyTokens.swift", swiftTypography(model.typography)),
    file("TSMotion.swift", swiftMotion(model.duration, model.easing)),
    file("TSComponentTokens.swift", swiftComponents(model.component)),
  ];
}

export const swiftTokensGenerator: Generator = {
  name: "swift-tokens",
  target: "tokens",
  outputDir: SWIFT_TOKENS_DIR,
  generate: ({ model }) => swiftTokenFiles(model),
};

export const swiftIconNameGenerator: Generator = {
  name: "swift-icon-name",
  target: "icons",
  outputDir: SWIFT_TOKENS_DIR,
  generate: async ({ root }) => [swiftIconNameFile((await cachedVectorAssets(root)).icons, SWIFT_TOKENS_DIR)],
};
