import type { GeneratedFile } from "../../generate.ts";
import type { IconAsset } from "../../icons/vector-assets.ts";
import type { ResolvedLogo } from "./asset-files.ts";
import { vectorSvg } from "./asset-files.ts";

export const IOS_ICONS_XCASSETS_DIR = "ios/Sources/TSFoundation/Resources/Icons.xcassets";
export const IOS_LOGOS_XCASSETS_DIR = "ios/Sources/TSFoundation/Resources/Logos.xcassets";

const INFO = { author: "xcode", version: 1 };

function json(value: unknown): string {
  return `${JSON.stringify(value, null, 2)}\n`;
}

function catalogRoot(dir: string): GeneratedFile {
  return { path: `${dir}/Contents.json`, contents: json({ info: INFO }) };
}

export function iconImageSetContents(icon: IconAsset): string {
  const image: Record<string, string> = { filename: `${icon.name}.svg`, idiom: "universal" };
  if (icon.mirrorsInRTL) {
    image["language-direction"] = "left-to-right";
  }
  return json({
    images: [image],
    info: INFO,
    properties: {
      "preserves-vector-representation": true,
      "template-rendering-intent": "template",
    },
  });
}

export function logoImageSetContents(logo: ResolvedLogo): string {
  const images: Record<string, unknown>[] = [{ filename: `${logo.light.name}.svg`, idiom: "universal" }];
  if (logo.dark !== undefined) {
    images.push({
      appearances: [{ appearance: "luminosity", value: "dark" }],
      filename: `${logo.dark.name}.svg`,
      idiom: "universal",
    });
  }
  return json({
    images,
    info: INFO,
    properties: {
      "preserves-vector-representation": true,
      "template-rendering-intent": "original",
    },
  });
}

export function iconCatalogFiles(icons: IconAsset[], dir: string = IOS_ICONS_XCASSETS_DIR): GeneratedFile[] {
  return [
    catalogRoot(dir),
    ...icons.flatMap((icon) => [
      { path: `${dir}/${icon.name}.imageset/Contents.json`, contents: iconImageSetContents(icon) },
      { path: `${dir}/${icon.name}.imageset/${icon.name}.svg`, contents: vectorSvg(icon) },
    ]),
  ];
}

export function logoCatalogFiles(logos: ResolvedLogo[], dir: string = IOS_LOGOS_XCASSETS_DIR): GeneratedFile[] {
  return [
    catalogRoot(dir),
    ...logos.flatMap((logo) => [
      { path: `${dir}/${logo.name}.imageset/Contents.json`, contents: logoImageSetContents(logo) },
      ...[logo.light, ...(logo.dark === undefined ? [] : [logo.dark])].map((variant) => ({
        path: `${dir}/${logo.name}.imageset/${variant.name}.svg`,
        contents: vectorSvg(variant),
      })),
    ]),
  ];
}
