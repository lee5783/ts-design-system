import { cp, mkdir, mkdtemp, readFile, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import {
  CATALOG_SOURCES,
  DEMO_APPS,
  SPECS_DIRECTORY,
  checkDemoCatalogs,
  componentSpecs,
  formatCatalogReport,
  kotlinCatalogPages,
  kotlinPageIds,
  pageName,
  swiftCatalogPages,
  swiftPageIds,
} from "../src/check-catalog.ts";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");

describe("catalog parsing", () => {
  it("reads Swift page ids, with and without raw values, and the pages each section lists", () => {
    const ids = swiftPageIds('public enum DemoPageID: String {\n    case colors\n    case iconButton = "icon-button"\n    case `switch`\n}\n');
    expect([...ids]).toEqual([
      ["colors", "colors"],
      ["iconButton", "icon-button"],
      ["switch", "switch"],
    ]);
    expect(swiftCatalogPages("DemoSection(id: .forms, pages: [.textField, .checkbox, .switch]),\nDemoSection(id: .actions, pages: [.button])")).toEqual(["textField", "checkbox", "switch", "button"]);
  });

  it("reads Kotlin page ids and the entries each section lists", () => {
    expect([...kotlinPageIds('    Colors("colors", DemoPageKind.Foundation, R.string.demo_page_colors),\n    IconButton("icon-button", DemoPageKind.Component, R.string.x),\n')]).toEqual([
      ["Colors", "colors"],
      ["IconButton", "icon-button"],
    ]);
    expect(kotlinCatalogPages("DemoSection(DemoSectionId.Actions, listOf(DemoPageId.Button, DemoPageId.IconButton)),")).toEqual(["Button", "IconButton"]);
  });

  it("names page files after the page id", () => {
    expect(pageName("icon-button")).toBe("IconButton");
    expect(pageName("icons")).toBe("Icons");
  });
});

describe("the repository", () => {
  it("has a demo page for every component spec, in both catalogs and all four demo apps", async () => {
    const report = await checkDemoCatalogs(repoRoot);
    expect(report.specs.length).toBeGreaterThan(10);
    expect(report.specs).toContain("button");
    expect(report.problems).toEqual([]);
  });
});

describe("a fixture repository", () => {
  let root: string;

  async function put(relativePath: string, content: string): Promise<void> {
    await mkdir(path.dirname(path.join(root, relativePath)), { recursive: true });
    await writeFile(path.join(root, relativePath), content);
  }

  async function edit(relativePath: string, change: (source: string) => string): Promise<void> {
    const file = path.join(root, relativePath);
    await writeFile(file, change(await readFile(file, "utf8")));
  }

  beforeEach(async () => {
    root = await mkdtemp(path.join(tmpdir(), "demos-catalog-"));
    await cp(path.join(repoRoot, SPECS_DIRECTORY), path.join(root, SPECS_DIRECTORY), { recursive: true });
    for (const source of CATALOG_SOURCES) {
      for (const file of [source.pageIds, source.catalog]) await cp(path.join(repoRoot, file), path.join(root, file));
    }
    for (const spec of await componentSpecs(root)) {
      const id = spec === "icon" ? "icons" : spec;
      for (const app of DEMO_APPS) await put(app.pageFiles(pageName(id))[0] ?? "", "");
    }
  });

  afterEach(async () => {
    await rm(root, { recursive: true, force: true });
  });

  it("passes when every spec has its pages", async () => {
    expect((await checkDemoCatalogs(root)).problems).toEqual([]);
  });

  it("fails for a new component spec with no demo page anywhere", async () => {
    await put(`${SPECS_DIRECTORY}/progress.md`, "# Progress\n");
    const report = await checkDemoCatalogs(root);
    expect(report.problems.map((problem) => problem.spec)).toEqual(Array(2 + DEMO_APPS.length).fill("progress"));
    const output = formatCatalogReport(report);
    expect(output).toContain('progress.md has no "progress" page in the iOS DemoCatalog');
    expect(output).toContain('progress.md has no "progress" page in the Android DemoCatalog');
    expect(output).toContain("progress.md has no page file in ios-swiftui; expected one of: demos/ios-swiftui/TSDemoSwiftUI/Components/ProgressPage.swift");
    expect(output).toContain("progress.md has no page file in android-views");
  });

  it("fails when a page id exists but no catalog section lists it", async () => {
    await edit(CATALOG_SOURCES[1]?.catalog ?? "", (source) => source.replace("DemoPageId.Toast", ""));
    expect((await checkDemoCatalogs(root)).problems).toEqual([{ spec: "toast", message: expect.stringContaining("Android DemoCatalog") }]);
  });

  it("fails when one demo app has no page file for a component", async () => {
    await rm(path.join(root, DEMO_APPS[1]?.pageFiles("Card")[0] ?? ""));
    expect((await checkDemoCatalogs(root)).problems).toEqual([{ spec: "card", message: expect.stringContaining("no page file in ios-uikit") }]);
  });

  it("maps the icon spec to the Icons foundation page", async () => {
    await edit(CATALOG_SOURCES[0]?.catalog ?? "", (source) => source.replace(".icons]", "]"));
    expect((await checkDemoCatalogs(root)).problems).toEqual([{ spec: "icon", message: expect.stringContaining('no "icons" page in the iOS DemoCatalog') }]);
  });
});
