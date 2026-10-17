import { androidTokenResourcesGenerator } from "./formats/android-xml/tokens-resources.ts";
import { androidViewsResourcesGenerator } from "./formats/android-xml/views-resources.ts";
import { composeThemeGenerator } from "./formats/compose/compose-theme.ts";
import { androidTokenValuesGenerator } from "./formats/compose/token-values.ts";
import { ICON_GENERATORS } from "./formats/icons/index.ts";
import { swiftIconNameGenerator, swiftTokensGenerator } from "./formats/swift/index.ts";
import type { Generator } from "./generate.ts";

export const GENERATORS: Generator[] = [
  swiftTokensGenerator,
  swiftIconNameGenerator,
  androidTokenResourcesGenerator,
  androidTokenValuesGenerator,
  androidViewsResourcesGenerator,
  composeThemeGenerator,
  ...ICON_GENERATORS,
];
