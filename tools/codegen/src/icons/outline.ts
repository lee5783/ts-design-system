import canvasKitModule from "canvaskit-wasm/full";
import type { CanvasKit, Path } from "canvaskit-wasm";
import type { SvgElement, SvgPaint } from "./svg-source.ts";
import { numberAttribute } from "./svg-source.ts";

export type FillRule = "nonzero" | "evenodd";

export interface OutlinedPath {
  pathData: string;
  fillRule: FillRule;
}

export const STROKE_PRECISION = 16;

const initCanvasKit = canvasKitModule as unknown as typeof canvasKitModule.default;

let canvasKit: Promise<CanvasKit> | undefined;

export function loadCanvasKit(): Promise<CanvasKit> {
  if (canvasKit === undefined) {
    canvasKit = initCanvasKit();
  }
  return canvasKit;
}

function format(value: number): string {
  const rounded = Math.round(value * 1000) / 1000;
  return Object.is(rounded, -0) ? "0" : String(rounded);
}

export function roundPathData(pathData: string): string {
  return pathData.replace(/-?(?:\d+\.?\d*|\.\d+)(?:e[-+]?\d+)?/gi, (number) => format(Number(number)));
}

export function pathCommandsToData(kit: CanvasKit, commands: ArrayLike<number>): string {
  const parts: string[] = [];
  const point = (x: number, y: number) => `${format(x)} ${format(y)}`;
  let currentX = 0;
  let currentY = 0;
  let index = 0;
  const next = (): number => {
    const value = commands[index];
    index += 1;
    if (value === undefined) {
      throw new Error("truncated path commands");
    }
    return value;
  };
  while (index < commands.length) {
    const verb = next();
    if (verb === kit.MOVE_VERB || verb === kit.LINE_VERB) {
      currentX = next();
      currentY = next();
      parts.push(`${verb === kit.MOVE_VERB ? "M" : "L"}${point(currentX, currentY)}`);
    } else if (verb === kit.QUAD_VERB) {
      const [x1, y1, x2, y2] = [next(), next(), next(), next()];
      parts.push(`Q${point(x1, y1)} ${point(x2, y2)}`);
      [currentX, currentY] = [x2, y2];
    } else if (verb === kit.CONIC_VERB) {
      const [x1, y1, x2, y2, weight] = [next(), next(), next(), next(), next()];
      const k = (4 * weight) / (3 * (1 + weight));
      parts.push(`C${point(currentX + (x1 - currentX) * k, currentY + (y1 - currentY) * k)} ${point(x2 + (x1 - x2) * k, y2 + (y1 - y2) * k)} ${point(x2, y2)}`);
      [currentX, currentY] = [x2, y2];
    } else if (verb === kit.CUBIC_VERB) {
      const [x1, y1, x2, y2, x3, y3] = [next(), next(), next(), next(), next(), next()];
      parts.push(`C${point(x1, y1)} ${point(x2, y2)} ${point(x3, y3)}`);
      [currentX, currentY] = [x3, y3];
    } else if (verb === kit.CLOSE_VERB) {
      parts.push("Z");
    } else {
      throw new Error(`unknown path verb ${verb}`);
    }
  }
  return parts.join("");
}

export function circlePathData(cx: number, cy: number, r: number): string {
  const values = [cx + r, cy, r, r, cx - r, cy, r, r, cx + r, cy].map(format);
  return `M${values[0]} ${values[1]}A${values[2]} ${values[3]} 0 1 0 ${values[4]} ${values[5]}A${values[6]} ${values[7]} 0 1 0 ${values[8]} ${values[9]}Z`;
}

export function rectPathData(x: number, y: number, width: number, height: number, rx: number, ry: number): string {
  const cornerX = Math.min(Math.max(rx, 0), width / 2);
  const cornerY = Math.min(Math.max(ry, 0), height / 2);
  if (cornerX === 0 || cornerY === 0) {
    return `M${format(x)} ${format(y)}H${format(x + width)}V${format(y + height)}H${format(x)}Z`;
  }
  const arc = (endX: number, endY: number) => `A${format(cornerX)} ${format(cornerY)} 0 0 1 ${format(endX)} ${format(endY)}`;
  return [
    `M${format(x + cornerX)} ${format(y)}`,
    `H${format(x + width - cornerX)}`,
    arc(x + width, y + cornerY),
    `V${format(y + height - cornerY)}`,
    arc(x + width - cornerX, y + height),
    `H${format(x + cornerX)}`,
    arc(x, y + height - cornerY),
    `V${format(y + cornerY)}`,
    arc(x + cornerX, y),
    "Z",
  ].join("");
}

export function shapePathData(element: SvgElement): string {
  const attributes = element.attributes;
  switch (element.tag) {
    case "path": {
      const data = attributes["d"]?.trim();
      if (data === undefined || data === "") {
        throw new Error("<path> without d");
      }
      return data;
    }
    case "circle":
      return circlePathData(numberAttribute(attributes, "cx", 0), numberAttribute(attributes, "cy", 0), numberAttribute(attributes, "r"));
    case "rect": {
      const rx = attributes["rx"] ?? attributes["ry"];
      const ry = attributes["ry"] ?? attributes["rx"];
      return rectPathData(
        numberAttribute(attributes, "x", 0),
        numberAttribute(attributes, "y", 0),
        numberAttribute(attributes, "width"),
        numberAttribute(attributes, "height"),
        rx === undefined ? 0 : Number(rx),
        ry === undefined ? 0 : Number(ry),
      );
    }
    case "text":
      throw new Error("<text> must be converted with the text outliner");
  }
}

function parsePath(kit: CanvasKit, pathData: string): Path {
  const path = kit.Path.MakeFromSVGString(pathData);
  if (path === null) {
    throw new Error(`invalid path data "${pathData.slice(0, 40)}"`);
  }
  return path;
}

export function strokeOutline(kit: CanvasKit, pathData: string, paint: SvgPaint): Path {
  const source = parsePath(kit, pathData);
  const cap = { butt: kit.StrokeCap.Butt, round: kit.StrokeCap.Round, square: kit.StrokeCap.Square }[paint.strokeLinecap];
  const join = { miter: kit.StrokeJoin.Miter, round: kit.StrokeJoin.Round, bevel: kit.StrokeJoin.Bevel }[paint.strokeLinejoin];
  const stroked = source.makeStroked({ width: paint.strokeWidth, cap, join, miter_limit: 4, precision: STROKE_PRECISION });
  source.delete();
  if (stroked === null) {
    throw new Error("stroke outlining failed");
  }
  return stroked;
}

export function fillPath(kit: CanvasKit, pathData: string, fillRule: FillRule = "nonzero"): Path {
  const path = parsePath(kit, pathData);
  path.setFillType(fillRule === "evenodd" ? kit.FillType.EvenOdd : kit.FillType.Winding);
  return path;
}

export function unionPaths(kit: CanvasKit, paths: Path[]): OutlinedPath {
  const [first, ...rest] = paths;
  if (first === undefined) {
    throw new Error("nothing to outline");
  }
  let result = kit.Path.MakeFromOp(first, first, kit.PathOp.Union);
  for (const path of rest) {
    if (result === null) {
      break;
    }
    const next: Path | null = kit.Path.MakeFromOp(result, path, kit.PathOp.Union);
    result.delete();
    result = next;
  }
  paths.forEach((path) => path.delete());
  if (result === null) {
    throw new Error("path union failed");
  }
  const outlined: OutlinedPath = {
    pathData: pathCommandsToData(kit, result.toCmds()),
    fillRule: result.getFillType().value === kit.FillType.EvenOdd.value ? "evenodd" : "nonzero",
  };
  result.delete();
  return outlined;
}
