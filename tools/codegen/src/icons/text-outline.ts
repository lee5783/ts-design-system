import { readFileSync } from "node:fs";
import opentype from "opentype.js";
import type { Font } from "opentype.js";
import type { SvgElement } from "./svg-source.ts";
import { numberAttribute } from "./svg-source.ts";

export const LOGO_FONT_PATH = "tools/codegen/fonts/roboto-variable.ttf";

export type TextAnchor = "start" | "middle" | "end";

export interface TextRun {
  text: string;
  x: number;
  y: number;
  fontSize: number;
  fontWeight: number;
  letterSpacing: number;
  anchor: TextAnchor;
}

const fonts = new Map<string, Font>();

export function loadLogoFont(file: string): Font {
  let font = fonts.get(file);
  if (font === undefined) {
    const bytes = readFileSync(file);
    font = opentype.parse(bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength));
    fonts.set(file, font);
  }
  return font;
}

function fontWeight(value: string | undefined): number {
  if (value === undefined || value === "normal") {
    return 400;
  }
  if (value === "bold") {
    return 700;
  }
  const weight = Number(value);
  if (!Number.isFinite(weight)) {
    throw new Error(`unsupported font-weight "${value}"`);
  }
  return weight;
}

export function textRun(element: SvgElement): TextRun {
  const attributes = element.attributes;
  const anchor = attributes["text-anchor"] ?? "start";
  if (anchor !== "start" && anchor !== "middle" && anchor !== "end") {
    throw new Error(`unsupported text-anchor "${anchor}"`);
  }
  const text = element.text.trim();
  if (text === "") {
    throw new Error("<text> without content");
  }
  return {
    text,
    x: numberAttribute(attributes, "x", 0),
    y: numberAttribute(attributes, "y", 0),
    fontSize: numberAttribute(attributes, "font-size", 16),
    fontWeight: fontWeight(attributes["font-weight"]),
    letterSpacing: numberAttribute(attributes, "letter-spacing", 0),
    anchor,
  };
}

export function textToPathData(font: Font, run: TextRun): string {
  const coordinates = { wght: run.fontWeight };
  font.variation?.set(coordinates);
  const scale = run.fontSize / font.unitsPerEm;
  const glyphs = [...run.text].map((char) => font.charToGlyph(char));
  const advances = glyphs.map((glyph, index) => {
    const next = glyphs[index + 1];
    const kerning = next === undefined ? 0 : font.getKerningValue(glyph, next);
    const spacing = next === undefined ? 0 : run.letterSpacing;
    const advance = (font.variation?.getTransform(glyph, coordinates) ?? glyph).advanceWidth ?? 0;
    return (advance + kerning) * scale + spacing;
  });
  const width = advances.reduce((sum, advance) => sum + advance, 0);
  const offset = { start: 0, middle: width / 2, end: width }[run.anchor];
  let x = run.x - offset;
  return glyphs
    .map((glyph, index) => {
      const data = glyph.getPath(x, run.y, run.fontSize, {}, font).toPathData(4);
      x += advances[index] ?? 0;
      return data;
    })
    .join("");
}
