export type SvgShapeTag = "path" | "circle" | "rect" | "text";

export interface SvgElement {
  tag: SvgShapeTag;
  attributes: Record<string, string>;
  text: string;
}

export interface SvgDocument {
  width: number;
  height: number;
  viewBox: [number, number, number, number];
  attributes: Record<string, string>;
  elements: SvgElement[];
}

export interface SvgPaint {
  fill: string | undefined;
  stroke: string | undefined;
  strokeWidth: number;
  strokeLinecap: "butt" | "round" | "square";
  strokeLinejoin: "miter" | "round" | "bevel";
}

const SHAPE_TAGS = new Set<string>(["path", "circle", "rect", "text"]);
const PAINT_ATTRIBUTES = ["fill", "stroke", "stroke-width", "stroke-linecap", "stroke-linejoin"];
const SUPPORTED_ATTRIBUTES: Record<SvgShapeTag | "svg", ReadonlySet<string>> = {
  svg: new Set(["xmlns", "version", "width", "height", "viewBox", ...PAINT_ATTRIBUTES]),
  path: new Set(["d", "fill-rule", ...PAINT_ATTRIBUTES]),
  circle: new Set(["cx", "cy", "r", ...PAINT_ATTRIBUTES]),
  rect: new Set(["x", "y", "width", "height", "rx", "ry", ...PAINT_ATTRIBUTES]),
  text: new Set(["x", "y", "font-family", "font-size", "font-weight", "letter-spacing", "text-anchor", ...PAINT_ATTRIBUTES]),
};
const TOKEN = /<!--[\s\S]*?-->|<[?!][^>]*>|<\s*(\/?)\s*([a-zA-Z][\w:-]*)([^>]*?)(\/?)\s*>|([^<]+)/g;
const ATTRIBUTE = /([a-zA-Z_:][\w:.-]*)\s*=\s*(?:"([^"]*)"|'([^']*)')/g;
const ENTITIES: Record<string, string> = { quot: '"', apos: "'", amp: "&", lt: "<", gt: ">" };

export function decodeEntities(value: string): string {
  return value.replace(/&(#x[0-9a-fA-F]+|#[0-9]+|[a-z]+);/g, (match, entity: string) => {
    if (entity.startsWith("#x")) {
      return String.fromCodePoint(Number.parseInt(entity.slice(2), 16));
    }
    if (entity.startsWith("#")) {
      return String.fromCodePoint(Number.parseInt(entity.slice(1), 10));
    }
    return ENTITIES[entity] ?? match;
  });
}

function parseAttributes(source: string): Record<string, string> {
  const attributes: Record<string, string> = {};
  for (const match of source.matchAll(ATTRIBUTE)) {
    const [, name = "", doubleQuoted, singleQuoted] = match;
    attributes[name] = decodeEntities(doubleQuoted ?? singleQuoted ?? "");
  }
  return attributes;
}

export function numberAttribute(attributes: Record<string, string>, name: string, fallback?: number): number {
  const raw = attributes[name];
  if (raw === undefined) {
    if (fallback === undefined) {
      throw new Error(`missing numeric attribute "${name}"`);
    }
    return fallback;
  }
  const value = Number(raw.trim().replace(/px$/, ""));
  if (!Number.isFinite(value)) {
    throw new Error(`attribute "${name}" is not a number: "${raw}"`);
  }
  return value;
}

function checkAttributes(tag: SvgShapeTag | "svg", attributes: Record<string, string>, label: string): Record<string, string> {
  const supported = SUPPORTED_ATTRIBUTES[tag];
  for (const name of Object.keys(attributes)) {
    if (!supported.has(name)) {
      throw new Error(`${label}: unsupported attribute "${name}" on <${tag}>`);
    }
  }
  return attributes;
}

function parseViewBox(attributes: Record<string, string>, label: string): [number, number, number, number] {
  const parts = (attributes["viewBox"] ?? "").trim().split(/[\s,]+/).map(Number);
  if (parts.length !== 4 || parts.some((part) => !Number.isFinite(part))) {
    throw new Error(`${label}: the root <svg> needs a numeric viewBox`);
  }
  const [x = 0, y = 0, width = 0, height = 0] = parts;
  if (width <= 0 || height <= 0) {
    throw new Error(`${label}: the viewBox must have a positive size`);
  }
  return [x, y, width, height];
}

export function parseSvg(source: string, label: string): SvgDocument {
  let root: Record<string, string> | undefined;
  let rootClosed = false;
  let openText: SvgElement | undefined;
  const elements: SvgElement[] = [];
  for (const match of source.matchAll(TOKEN)) {
    const [, closing = "", rawTag = "", rawAttributes = "", selfClosing = "", content] = match;
    if (content !== undefined) {
      if (openText !== undefined) {
        openText.text += decodeEntities(content);
      } else if (content.trim() !== "") {
        throw new Error(`${label}: unexpected text content outside <text>`);
      }
      continue;
    }
    if (rawTag === "") {
      continue;
    }
    const tag = rawTag.toLowerCase();
    if (rootClosed) {
      throw new Error(`${label}: content after the closing </svg>`);
    }
    if (tag === "svg") {
      if (closing === "/") {
        rootClosed = true;
      } else if (root !== undefined) {
        throw new Error(`${label}: nested <svg> is not supported`);
      } else {
        root = checkAttributes("svg", parseAttributes(rawAttributes), label);
      }
      continue;
    }
    if (root === undefined) {
      throw new Error(`${label}: <${tag}> before the root <svg>`);
    }
    if (!SHAPE_TAGS.has(tag)) {
      throw new Error(`${label}: unsupported element <${tag}>`);
    }
    if (closing === "/") {
      if (tag === "text") {
        openText = undefined;
      }
      continue;
    }
    const shapeTag = tag as SvgShapeTag;
    const element: SvgElement = { tag: shapeTag, attributes: checkAttributes(shapeTag, parseAttributes(rawAttributes), label), text: "" };
    elements.push(element);
    if (tag === "text" && selfClosing !== "/") {
      openText = element;
    }
  }
  if (root === undefined || !rootClosed) {
    throw new Error(`${label}: not a complete <svg> document`);
  }
  const viewBox = parseViewBox(root, label);
  return {
    width: numberAttribute(root, "width", viewBox[2]),
    height: numberAttribute(root, "height", viewBox[3]),
    viewBox,
    attributes: root,
    elements,
  };
}

function paintValue(value: string | undefined): string | undefined {
  if (value === undefined) {
    return undefined;
  }
  const trimmed = value.trim();
  return trimmed === "none" || trimmed === "transparent" ? undefined : trimmed;
}

export function resolvePaint(document: SvgDocument, element: SvgElement): SvgPaint {
  const inherited = (name: string): string | undefined => element.attributes[name] ?? document.attributes[name];
  const fill = inherited("fill");
  const linecap = inherited("stroke-linecap") ?? "butt";
  const linejoin = inherited("stroke-linejoin") ?? "miter";
  if (linecap !== "butt" && linecap !== "round" && linecap !== "square") {
    throw new Error(`unsupported stroke-linecap "${linecap}"`);
  }
  if (linejoin !== "miter" && linejoin !== "round" && linejoin !== "bevel") {
    throw new Error(`unsupported stroke-linejoin "${linejoin}"`);
  }
  return {
    fill: fill === undefined ? "#000000" : paintValue(fill),
    stroke: paintValue(inherited("stroke")),
    strokeWidth: Number(inherited("stroke-width") ?? "1"),
    strokeLinecap: linecap,
    strokeLinejoin: linejoin,
  };
}
