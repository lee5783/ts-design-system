import { readFile, readdir } from "node:fs/promises";
import path from "node:path";
import type { CanvasKit, Path } from "canvaskit-wasm";
import type { Font } from "opentype.js";
import { parseHexColor } from "../transforms/css-values.ts";
import type { FillRule } from "./outline.ts";
import { fillPath, loadCanvasKit, shapePathData, strokeOutline, unionPaths } from "./outline.ts";
import type { SvgDocument, SvgElement } from "./svg-source.ts";
import { parseSvg, resolvePaint } from "./svg-source.ts";
import { LOGO_FONT_PATH, loadLogoFont, textRun, textToPathData } from "./text-outline.ts";

export const ICONS_SOURCE_DIR = "design/assets/icons/source";
export const ICONS_META_PATH = "design/assets/icons/icons.meta.json";
export const LOGOS_SOURCE_DIR = "design/assets/logos/source";
export const ICON_FILL = "#000000";

export interface VectorLayer {
  pathData: string;
  fill: string;
  fillRule: FillRule;
}

export interface VectorAsset {
  name: string;
  width: number;
  height: number;
  viewportWidth: number;
  viewportHeight: number;
  layers: VectorLayer[];
}

export interface IconAsset extends VectorAsset {
  mirrorsInRTL: boolean;
}

export interface VectorAssets {
  icons: IconAsset[];
  logos: VectorAsset[];
}

export interface VectorAssetPaths {
  iconsDir: string;
  iconsMeta: string;
  logosDir: string;
  font: string;
}

export const DEFAULT_ASSET_PATHS: VectorAssetPaths = {
  iconsDir: ICONS_SOURCE_DIR,
  iconsMeta: ICONS_META_PATH,
  logosDir: LOGOS_SOURCE_DIR,
  font: LOGO_FONT_PATH,
};

const ASSET_NAME = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

function frame(document: SvgDocument, name: string): Omit<VectorAsset, "layers"> {
  const [x, y, width, height] = document.viewBox;
  if (x !== 0 || y !== 0) {
    throw new Error(`${name}: the viewBox must start at 0 0`);
  }
  return { name, width: document.width, height: document.height, viewportWidth: width, viewportHeight: height };
}

function elementPaths(kit: CanvasKit, font: Font | undefined, document: SvgDocument, element: SvgElement): { fill?: Path; stroke?: Path; fillColor?: string; strokeColor?: string } {
  const paint = resolvePaint(document, element);
  if (element.tag === "text") {
    if (font === undefined) {
      throw new Error("<text> is only supported in logos");
    }
    if (paint.stroke !== undefined) {
      throw new Error("stroked <text> is not supported");
    }
    return paint.fill === undefined ? {} : { fill: fillPath(kit, textToPathData(font, textRun(element))), fillColor: paint.fill };
  }
  const data = shapePathData(element);
  const fillRule = element.attributes["fill-rule"] === "evenodd" ? "evenodd" : "nonzero";
  return {
    ...(paint.fill === undefined ? {} : { fill: fillPath(kit, data, fillRule), fillColor: paint.fill }),
    ...(paint.stroke === undefined || paint.strokeWidth <= 0 ? {} : { stroke: strokeOutline(kit, data, paint), strokeColor: paint.stroke }),
  };
}

export function convertIcon(kit: CanvasKit, name: string, source: string, mirrorsInRTL: boolean): IconAsset {
  const document = parseSvg(source, name);
  const paths = document.elements.flatMap((element) => {
    const { fill, stroke } = elementPaths(kit, undefined, document, element);
    return [fill, stroke].filter((entry): entry is Path => entry !== undefined);
  });
  const outlined = unionPaths(kit, paths);
  return { ...frame(document, name), layers: [{ ...outlined, fill: ICON_FILL }], mirrorsInRTL };
}

export function convertLogo(kit: CanvasKit, font: Font, name: string, source: string): VectorAsset {
  const document = parseSvg(source, name);
  const layers = document.elements.flatMap((element) => {
    const { fill, stroke, fillColor, strokeColor } = elementPaths(kit, font, document, element);
    const layer = (pathValue: Path | undefined, color: string | undefined): VectorLayer[] =>
      pathValue === undefined || color === undefined ? [] : [{ ...unionPaths(kit, [pathValue]), fill: parseHexColor(color).hex }];
    return [...layer(fill, fillColor), ...layer(stroke, strokeColor)];
  });
  return { ...frame(document, name), layers };
}

async function svgNames(dir: string): Promise<string[]> {
  const names = (await readdir(dir)).filter((file) => file.endsWith(".svg")).map((file) => file.slice(0, -4));
  for (const name of names) {
    if (!ASSET_NAME.test(name)) {
      throw new Error(`${name}.svg: asset names must be lowercase kebab-case`);
    }
  }
  return names.sort();
}

async function readIconMeta(file: string): Promise<Record<string, { mirrorsInRTL: boolean }>> {
  const meta = JSON.parse(await readFile(file, "utf8")) as { icons?: Record<string, { mirrorsInRTL?: unknown }> };
  const icons = meta.icons ?? {};
  return Object.fromEntries(
    Object.entries(icons).map(([name, entry]) => {
      if (typeof entry.mirrorsInRTL !== "boolean") {
        throw new Error(`icons.meta.json: ${name}.mirrorsInRTL must be a boolean`);
      }
      return [name, { mirrorsInRTL: entry.mirrorsInRTL }];
    }),
  );
}

export async function loadVectorAssets(root: string, paths: VectorAssetPaths = DEFAULT_ASSET_PATHS): Promise<VectorAssets> {
  const kit = await loadCanvasKit();
  const iconsDir = path.resolve(root, paths.iconsDir);
  const logosDir = path.resolve(root, paths.logosDir);
  const meta = await readIconMeta(path.resolve(root, paths.iconsMeta));
  const iconNames = await svgNames(iconsDir);
  const unknown = Object.keys(meta).filter((name) => !iconNames.includes(name));
  const undescribed = iconNames.filter((name) => meta[name] === undefined);
  if (unknown.length > 0 || undescribed.length > 0) {
    throw new Error(`icons.meta.json does not match the icon sources (unknown: ${unknown.join(", ") || "none"}; missing: ${undescribed.join(", ") || "none"})`);
  }
  const icons = await Promise.all(
    iconNames.map(async (name) => convertIcon(kit, name, await readFile(path.join(iconsDir, `${name}.svg`), "utf8"), meta[name]?.mirrorsInRTL ?? false)),
  );
  const logoNames = await svgNames(logosDir);
  const font = logoNames.length === 0 ? undefined : loadLogoFont(path.resolve(root, paths.font));
  const logos = await Promise.all(
    logoNames.map(async (name) => convertLogo(kit, font as Font, name, await readFile(path.join(logosDir, `${name}.svg`), "utf8"))),
  );
  return { icons, logos };
}

const cache = new Map<string, Promise<VectorAssets>>();

export function cachedVectorAssets(root: string): Promise<VectorAssets> {
  let assets = cache.get(root);
  if (assets === undefined) {
    assets = loadVectorAssets(root);
    cache.set(root, assets);
  }
  return assets;
}
