import type { ColorValue, CubicBezier, ShadowLayer } from "../model/types.ts";

const HEX_COLOR = /^#([0-9a-f]{3,4}|[0-9a-f]{6}|[0-9a-f]{8})$/i;
const PX_LENGTH = /^(-?\d+(?:\.\d+)?)(px)?$/;
const EM_LENGTH = /^(-?\d+(?:\.\d+)?)em$/;
const NUMBER = /^-?\d+(?:\.\d+)?$/;
const DURATION = /^(\d+(?:\.\d+)?)(ms|s)$/;
const CUBIC_BEZIER = /^cubic-bezier\(([^)]*)\)$/;

function hexByte(value: number): string {
  return value.toString(16).padStart(2, "0");
}

export function parseHexColor(input: string): ColorValue {
  const match = HEX_COLOR.exec(input.trim());
  if (!match?.[1]) {
    throw new Error(`invalid color "${input}": expected #rgb, #rgba, #rrggbb or #rrggbbaa`);
  }
  let digits = match[1].toLowerCase();
  if (digits.length <= 4) {
    digits = [...digits].map((digit) => digit + digit).join("");
  }
  if (digits.length === 6) {
    digits += "ff";
  }
  const byte = (offset: number): number => Number.parseInt(digits.slice(offset, offset + 2), 16);
  const rgba = { r: byte(0), g: byte(2), b: byte(4), a: byte(6) };
  return { hex: formatHexColor(rgba), rgba };
}

export function formatHexColor(rgba: ColorValue["rgba"]): string {
  const rgb = `#${hexByte(rgba.r)}${hexByte(rgba.g)}${hexByte(rgba.b)}`;
  return rgba.a === 255 ? rgb : `${rgb}${hexByte(rgba.a)}`;
}

export function parsePx(input: string): number {
  const match = PX_LENGTH.exec(input.trim());
  if (!match?.[1] || (match[2] === undefined && Number(match[1]) !== 0)) {
    throw new Error(`invalid length "${input}": expected a px value`);
  }
  return Number(match[1]);
}

export function parseEm(input: string): number {
  const match = EM_LENGTH.exec(input.trim());
  if (!match?.[1]) {
    throw new Error(`invalid letter spacing "${input}": expected an em value`);
  }
  return Number(match[1]);
}

export function parseNumber(input: string | number): number {
  if (typeof input === "number" && Number.isFinite(input)) {
    return input;
  }
  if (typeof input === "string" && NUMBER.test(input.trim())) {
    return Number(input.trim());
  }
  throw new Error(`invalid number "${String(input)}"`);
}

export function parseMs(input: string): number {
  const match = DURATION.exec(input.trim());
  if (!match?.[1]) {
    throw new Error(`invalid duration "${input}": expected a ms or s value`);
  }
  const value = Number(match[1]);
  return match[2] === "s" ? Math.round(value * 1000) : value;
}

export function parseCubicBezier(input: string): CubicBezier {
  const match = CUBIC_BEZIER.exec(input.trim());
  const points = match?.[1]?.split(",").map((part) => part.trim()) ?? [];
  if (points.length !== 4 || points.some((point) => !NUMBER.test(point))) {
    throw new Error(`invalid easing "${input}": expected cubic-bezier(x1, y1, x2, y2)`);
  }
  const [x1, y1, x2, y2] = points.map(Number) as CubicBezier;
  if (x1 < 0 || x1 > 1 || x2 < 0 || x2 > 1) {
    throw new Error(`invalid easing "${input}": x1 and x2 must be between 0 and 1`);
  }
  return [x1, y1, x2, y2];
}

export function parseShadow(input: string): ShadowLayer[] {
  return input.split(",").map((layerSource) => {
    const parts = layerSource.trim().split(/\s+/);
    const inset = parts[0] === "inset";
    const rest = inset ? parts.slice(1) : parts;
    const colorPart = rest.at(-1);
    const lengths = rest.slice(0, -1);
    if (colorPart === undefined || lengths.length < 2 || lengths.length > 4) {
      throw new Error(`invalid shadow layer "${layerSource.trim()}": expected "<x> <y> [blur] [spread] <#color>"`);
    }
    const [offsetX = 0, offsetY = 0, blur = 0, spread = 0] = lengths.map(parsePx);
    return { offsetX, offsetY, blur, spread, color: parseHexColor(colorPart), inset };
  });
}
