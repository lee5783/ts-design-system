import type { IconAsset, VectorAsset } from "../../icons/vector-assets.ts";
import { toCamelName, toSnakeName } from "../../transforms/naming.ts";

export interface NativeLogo {
  name: string;
  light: string;
  dark?: string;
}

export const NATIVE_LOGOS: NativeLogo[] = [
  { name: "ts-monogram", light: "ts-monogram" },
  { name: "ts-wordmark", light: "ts-wordmark-light", dark: "ts-wordmark-dark" },
];

export interface ResolvedLogo {
  name: string;
  light: VectorAsset;
  dark: VectorAsset | undefined;
}

export function resolveNativeLogos(logos: VectorAsset[], wanted: NativeLogo[] = NATIVE_LOGOS): ResolvedLogo[] {
  const byName = new Map(logos.map((logo) => [logo.name, logo]));
  const find = (name: string): VectorAsset => {
    const logo = byName.get(name);
    if (logo === undefined) {
      throw new Error(`logo source ${name}.svg is missing`);
    }
    return logo;
  };
  return wanted.map((entry) => ({
    name: entry.name,
    light: find(entry.light),
    dark: entry.dark === undefined ? undefined : find(entry.dark),
  }));
}

export function iconCaseName(icon: IconAsset): string {
  return toCamelName(icon.name);
}

export function iconResourceName(icon: IconAsset): string {
  return `ts_ic_${toSnakeName(icon.name)}`;
}

export function logoResourceName(name: string): string {
  return `ts_logo_${toSnakeName(name.replace(/^ts-/, ""))}`;
}

export function vectorSvg(asset: VectorAsset): string {
  const paths = asset.layers.map((layer) => {
    const rule = layer.fillRule === "evenodd" ? ' fill-rule="evenodd"' : "";
    return `  <path fill="${layer.fill}"${rule} d="${layer.pathData}"/>`;
  });
  return [
    `<svg xmlns="http://www.w3.org/2000/svg" width="${asset.width}" height="${asset.height}" viewBox="0 0 ${asset.viewportWidth} ${asset.viewportHeight}">`,
    ...paths,
    "</svg>",
    "",
  ].join("\n");
}
