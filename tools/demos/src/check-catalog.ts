import { access, readdir, readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

export const SPECS_DIRECTORY = "design/specs/components";
export const SPEC_PAGE_ALIASES: Readonly<Record<string, string>> = { icon: "icons" };

export interface CatalogSource {
  platform: string;
  pageIds: string;
  catalog: string;
}

export interface DemoApp {
  name: string;
  platform: string;
  pageFiles: (pageName: string) => string[];
}

export interface CatalogProblem {
  spec: string;
  message: string;
}

export interface CatalogReport {
  specs: string[];
  problems: CatalogProblem[];
}

export const CATALOG_SOURCES: readonly CatalogSource[] = [
  { platform: "iOS", pageIds: "demos/ios-demo-support/Sources/TSDemoSupport/Catalog/DemoPageID.swift", catalog: "demos/ios-demo-support/Sources/TSDemoSupport/Catalog/DemoCatalog.swift" },
  { platform: "Android", pageIds: "demos/android-demo-support/src/main/kotlin/com/ts/ds/demo/support/catalog/DemoPageId.kt", catalog: "demos/android-demo-support/src/main/kotlin/com/ts/ds/demo/support/catalog/DemoCatalog.kt" },
];

const inFolders = (root: string, folders: readonly string[], suffixes: readonly string[]) => (pageName: string) => folders.flatMap((folder) => suffixes.map((suffix) => `${root}/${folder}/${pageName}${suffix}`));

export const DEMO_APPS: readonly DemoApp[] = [
  { name: "ios-swiftui", platform: "iOS", pageFiles: inFolders("demos/ios-swiftui/TSDemoSwiftUI", ["Components", "Foundations"], ["Page.swift"]) },
  { name: "ios-uikit", platform: "iOS", pageFiles: inFolders("demos/ios-uikit/TSDemoUIKit", ["Components", "Foundations"], ["ViewController.swift"]) },
  { name: "android-compose", platform: "Android", pageFiles: inFolders("demos/android-compose/app/src/main/kotlin/com/ts/ds/demo/compose", ["components", "foundations"], ["Page.kt"]) },
  { name: "android-views", platform: "Android", pageFiles: inFolders("demos/android-views/app/src/main/kotlin/com/ts/ds/demo/views", ["components", "foundations"], ["Fragment.kt", "PageFragment.kt"]) },
];

export function pageName(id: string): string {
  return id
    .split("-")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join("");
}

export function swiftPageIds(source: string): Map<string, string> {
  const ids = new Map<string, string>();
  for (const match of source.matchAll(/^\s*case\s+`?([A-Za-z_]\w*)`?(?:\s*=\s*"([^"]+)")?\s*$/gm)) {
    const name = match[1] ?? "";
    ids.set(name, match[2] ?? name);
  }
  return ids;
}

export function swiftCatalogPages(source: string): string[] {
  return [...source.matchAll(/pages:\s*\[([^\]]*)\]/g)].flatMap((match) => [...(match[1] ?? "").matchAll(/\.`?([A-Za-z_]\w*)`?/g)].map((page) => page[1] ?? ""));
}

export function kotlinPageIds(source: string): Map<string, string> {
  const ids = new Map<string, string>();
  for (const match of source.matchAll(/^\s*([A-Z]\w*)\(\s*"([^"]+)"/gm)) ids.set(match[1] ?? "", match[2] ?? "");
  return ids;
}

export function kotlinCatalogPages(source: string): string[] {
  return [...source.matchAll(/DemoPageId\.([A-Z]\w*)/g)].map((match) => match[1] ?? "");
}

export async function componentSpecs(repoRoot: string): Promise<string[]> {
  const entries = await readdir(path.join(repoRoot, SPECS_DIRECTORY));
  return entries
    .filter((entry) => entry.endsWith(".md"))
    .map((entry) => entry.slice(0, -".md".length))
    .sort();
}

export async function catalogIds(repoRoot: string, source: CatalogSource): Promise<Set<string>> {
  const pageIds = await readFile(path.join(repoRoot, source.pageIds), "utf8");
  const catalog = await readFile(path.join(repoRoot, source.catalog), "utf8");
  const swift = source.pageIds.endsWith(".swift");
  const ids = swift ? swiftPageIds(pageIds) : kotlinPageIds(pageIds);
  const listed = swift ? swiftCatalogPages(catalog) : kotlinCatalogPages(catalog);
  return new Set(listed.flatMap((name) => (ids.has(name) ? [ids.get(name) ?? name] : [])));
}

async function exists(file: string): Promise<boolean> {
  try {
    await access(file);
    return true;
  } catch {
    return false;
  }
}

export async function checkDemoCatalogs(repoRoot: string, sources: readonly CatalogSource[] = CATALOG_SOURCES, apps: readonly DemoApp[] = DEMO_APPS): Promise<CatalogReport> {
  const specs = await componentSpecs(repoRoot);
  const problems: CatalogProblem[] = [];
  const catalogs = new Map<string, Set<string>>();
  for (const source of sources) catalogs.set(source.platform, await catalogIds(repoRoot, source));
  for (const spec of specs) {
    const id = SPEC_PAGE_ALIASES[spec] ?? spec;
    for (const source of sources) {
      if (!catalogs.get(source.platform)?.has(id)) problems.push({ spec, message: `has no "${id}" page in the ${source.platform} DemoCatalog (${source.catalog})` });
    }
    for (const app of apps) {
      const candidates = app.pageFiles(pageName(id));
      const found = await Promise.all(candidates.map((candidate) => exists(path.join(repoRoot, candidate))));
      if (!found.includes(true)) problems.push({ spec, message: `has no page file in ${app.name}; expected one of: ${candidates.join(", ")}` });
    }
  }
  return { specs, problems };
}

export function formatCatalogReport(report: CatalogReport, apps: readonly DemoApp[] = DEMO_APPS): string {
  if (report.problems.length === 0) {
    return `demos-check-catalog: ${report.specs.length} component spec(s), each with a page in both DemoCatalogs and in ${apps.map((app) => app.name).join(", ")}`;
  }
  const lines = [`demos-check-catalog: ${report.problems.length} missing demo page(s) for ${SPECS_DIRECTORY}/*.md:`];
  for (const problem of report.problems) lines.push(`  ${problem.spec}.md ${problem.message}`);
  lines.push("Add the page to the platform's demo-support DemoPageID/DemoPageId and DemoCatalog, and a page in every demo app of that platform.");
  return lines.join("\n");
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
  try {
    const report = await checkDemoCatalogs(repoRoot);
    const output = formatCatalogReport(report);
    if (report.problems.length > 0) {
      console.error(output);
    } else {
      console.log(output);
    }
    process.exitCode = report.problems.length > 0 ? 1 : 0;
  } catch (error) {
    console.error(`demos-check-catalog: ${error instanceof Error ? error.message : String(error)}`);
    process.exitCode = 2;
  }
}
