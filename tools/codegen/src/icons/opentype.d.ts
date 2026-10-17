declare module "opentype.js" {
  export interface PathCommandsLike {
    toPathData(decimalPlaces?: number): string;
  }

  export interface Glyph {
    index: number;
    advanceWidth: number | undefined;
    getPath(x: number, y: number, fontSize: number, options: Record<string, unknown>, font: Font): PathCommandsLike;
  }

  export interface VariationManager {
    set(coordinates: Record<string, number>): void;
    getTransform(glyph: Glyph, coordinates?: Record<string, number>): Glyph;
  }

  export interface Font {
    unitsPerEm: number;
    variation: VariationManager | undefined;
    charToGlyph(char: string): Glyph;
    getKerningValue(left: Glyph, right: Glyph): number;
  }

  export function parse(buffer: ArrayBuffer): Font;

  const opentype: { parse: typeof parse };
  export default opentype;
}
