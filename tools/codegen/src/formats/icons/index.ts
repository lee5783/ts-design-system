import type { GeneratedFile, Generator } from "../../generate.ts";
import type { VectorAssets } from "../../icons/vector-assets.ts";
import { cachedVectorAssets } from "../../icons/vector-assets.ts";
import { resolveNativeLogos, vectorSvg } from "./asset-files.ts";
import { ANDROID_CORE_KOTLIN_DIR, kotlinIconFile } from "./icon-enums.ts";
import { ANDROID_CORE_RES_DIR, androidIconDrawables, androidLogoDrawables } from "./vector-drawable.ts";
import { IOS_ICONS_XCASSETS_DIR, IOS_LOGOS_XCASSETS_DIR, iconCatalogFiles, logoCatalogFiles } from "./xcassets.ts";

export const ICONS_NORMALIZED_DIR = "design/assets/icons/normalized";
export const LOGOS_NORMALIZED_DIR = "design/assets/logos/normalized";

function assetGenerator(name: string, outputDir: string, files: (assets: VectorAssets) => GeneratedFile[]): Generator {
  return {
    name,
    target: "icons",
    outputDir,
    generate: async ({ root }) => files(await cachedVectorAssets(root)),
  };
}

export function normalizedSvgFiles(assets: VectorAssets): GeneratedFile[] {
  return [
    ...assets.icons.map((icon) => ({ path: `${ICONS_NORMALIZED_DIR}/${icon.name}.svg`, contents: vectorSvg(icon) })),
    ...assets.logos.map((logo) => ({ path: `${LOGOS_NORMALIZED_DIR}/${logo.name}.svg`, contents: vectorSvg(logo) })),
  ];
}

export const iconsNormalizedGenerator = assetGenerator("icons-normalized", ICONS_NORMALIZED_DIR, (assets) =>
  normalizedSvgFiles({ icons: assets.icons, logos: [] }),
);

export const logosNormalizedGenerator = assetGenerator("logos-normalized", LOGOS_NORMALIZED_DIR, (assets) =>
  normalizedSvgFiles({ icons: [], logos: assets.logos }),
);

export const iosIconsCatalogGenerator = assetGenerator("ios-icons-xcassets", IOS_ICONS_XCASSETS_DIR, (assets) => iconCatalogFiles(assets.icons));

export const iosLogosCatalogGenerator = assetGenerator("ios-logos-xcassets", IOS_LOGOS_XCASSETS_DIR, (assets) =>
  logoCatalogFiles(resolveNativeLogos(assets.logos)),
);

export const androidIconResourcesGenerator = assetGenerator("android-icons-res", ANDROID_CORE_RES_DIR, (assets) => [
  ...androidIconDrawables(assets.icons),
  ...androidLogoDrawables(resolveNativeLogos(assets.logos)),
]);

export const kotlinIconGenerator = assetGenerator("android-icon-kotlin", ANDROID_CORE_KOTLIN_DIR, (assets) => [kotlinIconFile(assets.icons)]);

export const ICON_GENERATORS: Generator[] = [
  iconsNormalizedGenerator,
  logosNormalizedGenerator,
  iosIconsCatalogGenerator,
  iosLogosCatalogGenerator,
  androidIconResourcesGenerator,
  kotlinIconGenerator,
];
