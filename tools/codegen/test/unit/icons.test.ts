import { mkdtemp, mkdir, readFile, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import type { CanvasKit } from "canvaskit-wasm";
import { beforeAll, describe, expect, it } from "vitest";
import { generateFiles } from "../../src/generate.ts";
import { GENERATORS } from "../../src/generators.ts";
import { resolveNativeLogos } from "../../src/formats/icons/asset-files.ts";
import { ICON_GENERATORS } from "../../src/formats/icons/index.ts";
import { swiftIconNameGenerator } from "../../src/formats/swift/index.ts";
import { circlePathData, loadCanvasKit, rectPathData, roundPathData, strokeOutline, unionPaths } from "../../src/icons/outline.ts";
import { parseSvg, resolvePaint } from "../../src/icons/svg-source.ts";
import { LOGO_FONT_PATH, loadLogoFont, textToPathData } from "../../src/icons/text-outline.ts";
import type { VectorAssets } from "../../src/icons/vector-assets.ts";
import { convertIcon, convertLogo, loadVectorAssets } from "../../src/icons/vector-assets.ts";
import { readNativeModel } from "../../src/model/merge-extensions.ts";
import { fixtureAssetPaths, repoRoot } from "../support.ts";

let kit: CanvasKit;

function bounds(pathData: string): number[] {
  const parsed = kit.Path.MakeFromSVGString(pathData);
  if (parsed === null) {
    throw new Error("invalid path data");
  }
  const rect = Array.from(parsed.computeTightBounds()).map((value) => Math.round(value * 1000) / 1000);
  parsed.delete();
  return rect;
}

beforeAll(async () => {
  kit = await loadCanvasKit();
});

describe("SVG source parsing", () => {
  it("reads the root frame, shapes and decoded text", () => {
    const document = parseSvg(
      '<?xml version="1.0"?><!-- c --><svg xmlns="http://www.w3.org/2000/svg" width="48" height="24" viewBox="0 0 48 24" stroke="#71717a"><path d="M1 1h2"/><text x="2" font-family="a, &#34;B C&#34;">T&amp;S</text></svg>',
      "fixture",
    );
    expect([document.width, document.height, document.viewBox]).toEqual([48, 24, [0, 0, 48, 24]]);
    expect(document.elements.map((element) => element.tag)).toEqual(["path", "text"]);
    expect(document.elements[1]?.attributes["font-family"]).toBe('a, "B C"');
    expect(document.elements[1]?.text).toBe("T&S");
  });

  it.each([
    ['<svg viewBox="0 0 24 24"><g><path d="M0 0h1"/></g></svg>', "unsupported element <g>"],
    ['<svg viewBox="0 0 24 24"><script>x</script></svg>', "unsupported element <script>"],
    ['<svg width="24"><path d="M0 0h1"/></svg>', "numeric viewBox"],
    ['<svg viewBox="0 0 24 24"><path d="M0 0h1"/>', "not a complete <svg> document"],
    ['<svg viewBox="0 0 24 24"></svg><path d="M0 0"/>', "content after the closing </svg>"],
  ])("rejects %s", (source, message) => {
    expect(() => parseSvg(source, "bad")).toThrow(message);
  });

  it.each([
    "transform",
    "style",
    "opacity",
    "fill-opacity",
    "stroke-opacity",
    "stroke-dasharray",
    "stroke-dashoffset",
    "stroke-miterlimit",
    "clip-path",
    "clip-rule",
    "mask",
    "filter",
    "class",
    "visibility",
    "display",
    "vector-effect",
  ])("rejects the unsupported attribute %s on a shape", (attribute) => {
    expect(() => parseSvg(`<svg viewBox="0 0 24 24"><path d="M0 0h1" ${attribute}="x"/></svg>`, "bad")).toThrow(
      `bad: unsupported attribute "${attribute}" on <path>`,
    );
  });

  it.each(["transform", "style", "opacity", "class", "fill-rule"])("rejects the unsupported attribute %s on the root", (attribute) => {
    expect(() => parseSvg(`<svg viewBox="0 0 24 24" ${attribute}="x"><path d="M0 0h1"/></svg>`, "bad")).toThrow(
      `bad: unsupported attribute "${attribute}" on <svg>`,
    );
  });

  it.each([
    ["circle", 'r="1" d="M0 0"', "d"],
    ["rect", 'width="1" height="1" cx="1"', "cx"],
    ["text", 'x="1" r="2"', "r"],
    ["path", 'd="M0 0" x="1"', "x"],
  ])("rejects a geometry attribute <%s> does not use", (tag, attributes, rejected) => {
    expect(() => parseSvg(`<svg viewBox="0 0 24 24"><${tag} ${attributes}/></svg>`, "bad")).toThrow(
      `bad: unsupported attribute "${rejected}" on <${tag}>`,
    );
  });

  it("inherits paint from the root and lets elements override it", () => {
    const document = parseSvg(
      '<svg viewBox="0 0 24 24" fill="none" stroke="#71717a" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M0 0h1"/><circle r="1" fill="#71717a" stroke="none"/></svg>',
      "paint",
    );
    const [path, dot] = document.elements.map((element) => resolvePaint(document, element));
    expect(path).toEqual({ fill: undefined, stroke: "#71717a", strokeWidth: 1.75, strokeLinecap: "round", strokeLinejoin: "round" });
    expect(dot?.fill).toBe("#71717a");
    expect(dot?.stroke).toBeUndefined();
  });
});

describe("Outline geometry", () => {
  it("writes circles and rounded rects as path data", () => {
    expect(circlePathData(12, 12, 9)).toBe("M21 12A9 9 0 1 0 3 12A9 9 0 1 0 21 12Z");
    expect(rectPathData(3, 5, 18, 16, 0, 0)).toBe("M3 5H21V21H3Z");
    expect(rectPathData(3, 5, 18, 16, 2, 2)).toBe("M5 5H19A2 2 0 0 1 21 7V19A2 2 0 0 1 19 21H5A2 2 0 0 1 3 19V7A2 2 0 0 1 5 5Z");
    expect(roundPathData("M1.23456 -0.00001L2e-5 3")).toBe("M1.235 0L0 3");
  });

  it("expands a round-capped stroke to a filled outline of the stroke width", () => {
    const paint = { fill: undefined, stroke: "#000", strokeWidth: 2, strokeLinecap: "round", strokeLinejoin: "round" } as const;
    const outlined = unionPaths(kit, [strokeOutline(kit, "M4 12h16", paint)]);
    expect(outlined.pathData).not.toMatch(/[A]/);
    expect(bounds(outlined.pathData)).toEqual([3, 11, 21, 13]);
  });

  it("keeps butt caps flush with the path ends", () => {
    const paint = { fill: undefined, stroke: "#000", strokeWidth: 2, strokeLinecap: "butt", strokeLinejoin: "miter" } as const;
    expect(bounds(unionPaths(kit, [strokeOutline(kit, "M4 12h16", paint)]).pathData)).toEqual([4, 11, 20, 13]);
  });
});

describe("Icon conversion", () => {
  it("outlines the chevron stroke into one black filled layer", async () => {
    const source = await readFile(path.join(fixtureAssetPaths.iconsDir, "chevron-left.svg"), "utf8");
    const icon = convertIcon(kit, "chevron-left", source, true);
    expect(icon).toMatchObject({ width: 24, height: 24, viewportWidth: 24, viewportHeight: 24, mirrorsInRTL: true });
    expect(icon.layers).toHaveLength(1);
    expect(icon.layers[0]?.fill).toBe("#000000");
    expect(bounds(icon.layers[0]?.pathData ?? "")).toEqual([8.125, 5.125, 15.875, 18.875]);
  });

  it("merges stroked shapes with filled dots", async () => {
    const source = await readFile(path.join(fixtureAssetPaths.iconsDir, "info.svg"), "utf8");
    const icon = convertIcon(kit, "info", source, false);
    expect(icon.layers).toHaveLength(1);
    bounds(icon.layers[0]?.pathData ?? "").forEach((value, index) => {
      expect(value).toBeCloseTo([2.125, 2.125, 21.875, 21.875][index] ?? 0, 2);
    });
  });
});

describe("Logo conversion", () => {
  it("lays out text with the anchor and weight from the source", () => {
    const font = loadLogoFont(path.join(repoRoot, LOGO_FONT_PATH));
    const run = { text: "TS", x: 32, y: 42, fontSize: 28, fontWeight: 700, letterSpacing: -0.5, anchor: "middle" } as const;
    const [left = 0, top = 0, right = 0, bottom = 0] = bounds(textToPathData(font, run));
    expect(Math.abs((left + right) / 2 - 32)).toBeLessThan(1);
    expect(bottom).toBeCloseTo(42, 0);
    expect(bottom - top).toBeGreaterThan(18);
    const regular = bounds(textToPathData(font, { ...run, fontWeight: 400 }));
    expect((regular[2] ?? 0) - (regular[0] ?? 0)).toBeLessThan(right - left);
  });

  it("produces the same outlines regardless of conversion order", async () => {
    const font = loadLogoFont(path.join(repoRoot, LOGO_FONT_PATH));
    const read = (name: string) => readFile(path.join(repoRoot, "design/assets/logos/source", `${name}.svg`), "utf8");
    const light = await read("ts-wordmark-light");
    const dark = await read("ts-wordmark-dark");
    const first = convertLogo(kit, font, "ts-wordmark-light", light);
    convertLogo(kit, font, "ts-wordmark-dark", dark);
    expect(convertLogo(kit, font, "ts-wordmark-light", light)).toEqual(first);
  });

  it("keeps one colored layer per source element", async () => {
    const font = loadLogoFont(path.join(repoRoot, LOGO_FONT_PATH));
    const source = await readFile(path.join(repoRoot, "design/assets/logos/source/ts-wordmark-light.svg"), "utf8");
    expect(convertLogo(kit, font, "ts-wordmark-light", source).layers.map((layer) => layer.fill)).toEqual(["#7c3aed", "#ffffff", "#18181b"]);
  });

  it("requires every native logo source", () => {
    expect(() => resolveNativeLogos([], [{ name: "ts-wordmark", light: "ts-wordmark-light" }])).toThrow("logo source ts-wordmark-light.svg is missing");
  });
});

describe("Icon meta validation", () => {
  it("fails when icons.meta.json and the sources disagree", async () => {
    const dir = await mkdtemp(path.join(tmpdir(), "ts-icons-"));
    await mkdir(path.join(dir, "icons"));
    await mkdir(path.join(dir, "logos"));
    await writeFile(path.join(dir, "icons/home.svg"), '<svg viewBox="0 0 24 24" stroke="#000"><path d="M1 1h2"/></svg>');
    await writeFile(path.join(dir, "meta.json"), JSON.stringify({ icons: { star: { mirrorsInRTL: false } } }));
    await expect(
      loadVectorAssets(dir, { iconsDir: "icons", iconsMeta: "meta.json", logosDir: "logos", font: LOGO_FONT_PATH }),
    ).rejects.toThrow("unknown: star; missing: home");
  });
});

describe("Generated icon outputs for the real assets", () => {
  let assets: VectorAssets;
  let files: { path: string; contents: string }[];

  beforeAll(async () => {
    assets = await loadVectorAssets(repoRoot);
    files = await generateFiles(
      GENERATORS.filter((generator) => generator.target === "icons"),
      { root: repoRoot, model: await readNativeModel(repoRoot) },
    );
  });

  it("covers 33 icons and 4 logos", () => {
    expect(assets.icons).toHaveLength(33);
    expect(assets.logos.map((logo) => logo.name)).toEqual(["ts-app-icon", "ts-monogram", "ts-wordmark-dark", "ts-wordmark-light"]);
  });

  it("leaves no #71717a, stroke or <text> in any output", () => {
    for (const file of files) {
      expect(file.contents.toLowerCase(), file.path).not.toContain("#71717a");
      expect(file.contents, file.path).not.toMatch(/stroke/i);
      expect(file.contents, file.path).not.toContain("<text");
      expect(file.contents, file.path).not.toContain("font-family");
    }
  });

  it("applies the RTL mirror flags from icons.meta.json on both platforms", () => {
    const mirrored = assets.icons.filter((icon) => icon.mirrorsInRTL).map((icon) => icon.name);
    expect(mirrored).toEqual(["arrow-left", "arrow-right", "chevron-left", "chevron-right", "logout"]);
    for (const icon of assets.icons) {
      const snake = icon.name.replaceAll("-", "_");
      const drawable = files.find((file) => file.path.endsWith(`/drawable/ts_ic_${snake}.xml`));
      const imageSet = files.find((file) => file.path.endsWith(`Icons.xcassets/${icon.name}.imageset/Contents.json`));
      expect(drawable?.contents.includes('android:autoMirrored="true"'), icon.name).toBe(icon.mirrorsInRTL);
      expect(imageSet?.contents.includes('"language-direction": "left-to-right"'), icon.name).toBe(icon.mirrorsInRTL);
      expect(imageSet?.contents).toContain('"template-rendering-intent": "template"');
      expect(imageSet?.contents).toContain('"preserves-vector-representation": true');
    }
    const swift = files.find((file) => file.path.endsWith("TSIconName.swift"))?.contents ?? "";
    expect(swift).toContain("case .arrowLeft, .arrowRight, .chevronLeft, .chevronRight, .logout:");
    const kotlin = files.find((file) => file.path.endsWith("TSIcon.kt"))?.contents ?? "";
    expect(kotlin).toContain("Logout(R.drawable.ts_ic_logout, true)");
    expect(kotlin).toContain("Download(R.drawable.ts_ic_download, false)");
  });

  it("pairs the wordmark light and dark variants on both platforms", () => {
    const paths = files.map((file) => file.path);
    expect(paths).toContain("android/core/src/generated/res/drawable/ts_logo_wordmark.xml");
    expect(paths).toContain("android/core/src/generated/res/drawable-night/ts_logo_wordmark.xml");
    expect(paths).toContain("ios/Sources/TSFoundation/Resources/Logos.xcassets/ts-wordmark.imageset/ts-wordmark-dark.svg");
    const contents = files.find((file) => file.path.endsWith("ts-wordmark.imageset/Contents.json"))?.contents ?? "";
    expect(JSON.parse(contents).images).toEqual([
      { filename: "ts-wordmark-light.svg", idiom: "universal" },
      { appearances: [{ appearance: "luminosity", value: "dark" }], filename: "ts-wordmark-dark.svg", idiom: "universal" },
    ]);
    const night = files.find((file) => file.path.endsWith("drawable-night/ts_logo_wordmark.xml"))?.contents ?? "";
    expect(night).toContain('android:fillColor="#FFDCDDDE"');
  });

  it("registers the icon generators with distinct output folders", () => {
    expect(GENERATORS).toEqual(expect.arrayContaining(ICON_GENERATORS));
    expect(ICON_GENERATORS.every((generator) => generator.target === "icons")).toBe(true);
    expect(new Set(ICON_GENERATORS.map((generator) => generator.outputDir)).size).toBe(ICON_GENERATORS.length);
    expect(swiftIconNameGenerator.target).toBe("icons");
  });
});
