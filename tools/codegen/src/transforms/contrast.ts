import type { Rgba } from "../model/types.ts";

function channel(value: number): number {
  const srgb = value / 255;
  return srgb <= 0.03928 ? srgb / 12.92 : ((srgb + 0.055) / 1.055) ** 2.4;
}

export function relativeLuminance(color: Rgba): number {
  return 0.2126 * channel(color.r) + 0.7152 * channel(color.g) + 0.0722 * channel(color.b);
}

export function contrastRatio(first: Rgba, second: Rgba): number {
  const [lighter, darker] = [relativeLuminance(first), relativeLuminance(second)].sort((left, right) => right - left) as [number, number];
  return (lighter + 0.05) / (darker + 0.05);
}

export function compositeOver(top: Rgba, bottom: Rgba): Rgba {
  const topAlpha = top.a / 255;
  const bottomAlpha = (bottom.a / 255) * (1 - topAlpha);
  const alpha = topAlpha + bottomAlpha;
  if (alpha === 0) {
    return { r: 0, g: 0, b: 0, a: 0 };
  }
  const mix = (topChannel: number, bottomChannel: number) => (topChannel * topAlpha + bottomChannel * bottomAlpha) / alpha;
  return { r: mix(top.r, bottom.r), g: mix(top.g, bottom.g), b: mix(top.b, bottom.b), a: alpha * 255 };
}

export function flatten(layers: Rgba[]): Rgba {
  const [bottom, ...rest] = layers;
  if (bottom === undefined || bottom.a !== 255) {
    throw new Error("flatten: the bottom layer must be opaque");
  }
  return rest.reduce((result, layer) => compositeOver(layer, result), bottom);
}

export function compositedContrastRatio(foreground: Rgba, background: Rgba[]): number {
  return contrastRatio(flatten([...background, foreground]), flatten(background));
}
