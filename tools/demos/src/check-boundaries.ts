import { readdir, readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

export interface SwiftModuleRules {
  sources: readonly string[];
  tests: readonly string[];
}

export interface SwiftDemoBoundary {
  kind: "swift-app";
  name: string;
  root: string;
  imports: SwiftModuleRules;
  packageProducts: readonly string[];
  localPackages: readonly string[];
}

export interface SwiftSupportBoundary {
  kind: "swift-support";
  name: string;
  root: string;
  imports: SwiftModuleRules;
}

export interface GradleDemoBoundary {
  kind: "gradle-app";
  name: string;
  root: string;
  forbidden: readonly { pattern: RegExp; reason: string }[];
}

export type DemoBoundary = SwiftDemoBoundary | SwiftSupportBoundary | GradleDemoBoundary;

export interface Violation {
  demo: string;
  file: string;
  line: number;
  message: string;
}

export interface DemoReport {
  demo: string;
  scannedFiles: number;
  violations: Violation[];
}

const ANDROID_INTERNALS = [
  { pattern: /com\.tinysol\.designsystem:(core|tokens|test-support)\b/, reason: "declares a library-internal artifact" },
  { pattern: /project\(\s*["']:(core|tokens|compose|views|bom|test-support)["']\s*\)/, reason: "uses a project-path dependency into android/" },
  { pattern: /includeBuild\(\s*["'][^"']*android\/(core|tokens|compose|views|test-support)/, reason: "includes a library module build directly" },
  { pattern: /import\s+com\.tinysol\.designsystem\.testsupport\b/, reason: "imports the unpublished test support" },
];

export const DEMO_BOUNDARIES: readonly DemoBoundary[] = [
  {
    kind: "swift-support",
    name: "ios-demo-support",
    root: "demos/ios-demo-support",
    imports: {
      sources: ["Foundation", "Combine"],
      tests: ["Testing", "Foundation", "Combine", "TSDemoSupport"],
    },
  },
  {
    kind: "swift-app",
    name: "ios-swiftui",
    root: "demos/ios-swiftui",
    imports: {
      sources: ["SwiftUI", "Foundation", "Combine", "TSDesignSystem", "TSDemoSupport"],
      tests: ["XCTest", "Foundation"],
    },
    packageProducts: ["TSDesignSystem", "TSDemoSupport"],
    localPackages: ["../..", "../ios-demo-support"],
  },
  {
    kind: "swift-app",
    name: "ios-uikit",
    root: "demos/ios-uikit",
    imports: {
      sources: ["UIKit", "Foundation", "Combine", "TSDesignSystemUIKit", "TSDemoSupport"],
      tests: ["XCTest", "Foundation"],
    },
    packageProducts: ["TSDesignSystemUIKit", "TSDemoSupport"],
    localPackages: ["../..", "../ios-demo-support"],
  },
  {
    kind: "gradle-app",
    name: "android-demo-support",
    root: "demos/android-demo-support",
    forbidden: [
      { pattern: /com\.tinysol\.designsystem:/, reason: "demo support must not depend on the library" },
      { pattern: /import\s+com\.tinysol\.designsystem\.(?!sample\.)/, reason: "demo support must not import the library" },
      { pattern: /includeBuild\(|project\(\s*["']:/, reason: "demo support must not include other builds or projects" },
      { pattern: /androidx\.compose|com\.google\.android\.material/, reason: "demo support must stay toolkit-agnostic" },
    ],
  },
  {
    kind: "gradle-app",
    name: "android-compose",
    root: "demos/android-compose",
    forbidden: [
      ...ANDROID_INTERNALS,
      { pattern: /com\.tinysol\.designsystem:views\b/, reason: "the Compose demo must not use the Views toolkit" },
      { pattern: /import\s+com\.tinysol\.designsystem\.views\b/, reason: "the Compose demo must not import the Views toolkit" },
    ],
  },
  {
    kind: "gradle-app",
    name: "android-views",
    root: "demos/android-views",
    forbidden: [
      ...ANDROID_INTERNALS,
      { pattern: /com\.tinysol\.designsystem:compose\b/, reason: "the Views demo must not use the Compose toolkit" },
      { pattern: /import\s+com\.tinysol\.designsystem\.compose\b/, reason: "the Views demo must not import the Compose toolkit" },
      { pattern: /androidx\.compose/, reason: "the Views demo must not use Compose" },
    ],
  },
];

const SKIPPED_DIRECTORIES = new Set([".build", "build", ".gradle", "DerivedData", "node_modules", "xcuserdata", ".swiftpm"]);
const IMPORT_LINE = /^\s*((?:@[\w]+(?:\([^)]*\))?\s+)*)import\s+(?:(?:typealias|struct|class|enum|protocol|let|var|func)\s+)?([A-Za-z_][\w]*)/;

async function listFiles(directory: string, predicate: (file: string) => boolean): Promise<string[]> {
  let entries;
  try {
    entries = await readdir(directory, { withFileTypes: true });
  } catch {
    return [];
  }
  const files: string[] = [];
  for (const entry of entries.sort((a, b) => a.name.localeCompare(b.name))) {
    const full = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      if (!SKIPPED_DIRECTORIES.has(entry.name)) files.push(...(await listFiles(full, predicate)));
    } else if (predicate(full)) {
      files.push(full);
    }
  }
  return files;
}

function isTestFile(relativePath: string): boolean {
  return relativePath.split(path.sep).some((segment) => segment === "Tests" || /Tests$/.test(segment));
}

export function checkSwiftSource(demo: string, file: string, source: string, allowed: readonly string[]): Violation[] {
  const violations: Violation[] = [];
  source.split("\n").forEach((text, index) => {
    const line = index + 1;
    if (/@_spi\b/.test(text)) violations.push({ demo, file, line, message: "uses @_spi" });
    const match = IMPORT_LINE.exec(text);
    if (!match) return;
    const attributes = match[1] ?? "";
    const module = match[2] ?? "";
    if (/@testable\b/.test(attributes)) violations.push({ demo, file, line, message: `uses @testable import ${module}` });
    if (!allowed.includes(module)) violations.push({ demo, file, line, message: `imports ${module}; allowed: ${allowed.join(", ")}` });
  });
  return violations;
}

export function checkXcodeProject(demo: string, file: string, pbxproj: string, boundary: SwiftDemoBoundary): Violation[] {
  const violations: Violation[] = [];
  const lineOf = (offset: number) => pbxproj.slice(0, offset).split("\n").length;
  for (const match of pbxproj.matchAll(/isa = XCSwiftPackageProductDependency;[\s\S]*?productName = "?([^";]+)"?;/g)) {
    const product = match[1] ?? "";
    if (!boundary.packageProducts.includes(product)) {
      violations.push({ demo, file, line: lineOf(match.index ?? 0), message: `links package product ${product}; allowed: ${boundary.packageProducts.join(", ")}` });
    }
  }
  for (const match of pbxproj.matchAll(/isa = XCLocalSwiftPackageReference;\s*relativePath = "?([^";]+)"?;/g)) {
    const relativePath = match[1] ?? "";
    if (!boundary.localPackages.includes(relativePath)) {
      violations.push({ demo, file, line: lineOf(match.index ?? 0), message: `references local package ${relativePath}; allowed: ${boundary.localPackages.join(", ")}` });
    }
  }
  for (const match of pbxproj.matchAll(/isa = XCRemoteSwiftPackageReference;/g)) {
    violations.push({ demo, file, line: lineOf(match.index ?? 0), message: "references a remote Swift package; demos consume the library through local package references only" });
  }
  return violations;
}

export function checkSupportManifest(demo: string, file: string, manifest: string): Violation[] {
  const violations: Violation[] = [];
  manifest.split("\n").forEach((text, index) => {
    if (/\.package\s*\(/.test(text)) {
      violations.push({ demo, file, line: index + 1, message: "declares a package dependency; demo support must not depend on anything" });
    }
  });
  return violations;
}

export function checkGradleSource(demo: string, file: string, source: string, boundary: GradleDemoBoundary): Violation[] {
  const violations: Violation[] = [];
  source.split("\n").forEach((text, index) => {
    for (const rule of boundary.forbidden) {
      if (rule.pattern.test(text)) violations.push({ demo, file, line: index + 1, message: `${rule.reason}: ${text.trim()}` });
    }
  });
  return violations;
}

async function checkSwiftFiles(repoRoot: string, boundary: SwiftDemoBoundary | SwiftSupportBoundary): Promise<{ scanned: number; violations: Violation[] }> {
  const root = path.join(repoRoot, boundary.root);
  const files = await listFiles(root, (file) => file.endsWith(".swift") && path.basename(file) !== "Package.swift");
  const violations: Violation[] = [];
  for (const file of files) {
    const relative = path.relative(root, file);
    const allowed = isTestFile(relative) ? boundary.imports.tests : boundary.imports.sources;
    violations.push(...checkSwiftSource(boundary.name, path.relative(repoRoot, file), await readFile(file, "utf8"), allowed));
  }
  return { scanned: files.length, violations };
}

export async function checkDemo(repoRoot: string, boundary: DemoBoundary): Promise<DemoReport> {
  const root = path.join(repoRoot, boundary.root);
  if (boundary.kind === "gradle-app") {
    const files = await listFiles(root, (file) => /\.(kts|gradle|kt|toml|xml)$/.test(file));
    const violations: Violation[] = [];
    for (const file of files) {
      violations.push(...checkGradleSource(boundary.name, path.relative(repoRoot, file), await readFile(file, "utf8"), boundary));
    }
    return { demo: boundary.name, scannedFiles: files.length, violations };
  }
  const swift = await checkSwiftFiles(repoRoot, boundary);
  let scanned = swift.scanned;
  const violations = [...swift.violations];
  if (boundary.kind === "swift-support") {
    const manifest = path.join(root, "Package.swift");
    const contents = await readFile(manifest, "utf8").catch(() => undefined);
    if (contents !== undefined) {
      scanned += 1;
      violations.push(...checkSupportManifest(boundary.name, path.relative(repoRoot, manifest), contents));
    }
  } else {
    const projects = await listFiles(root, (file) => path.basename(file) === "project.pbxproj");
    for (const project of projects) {
      scanned += 1;
      violations.push(...checkXcodeProject(boundary.name, path.relative(repoRoot, project), await readFile(project, "utf8"), boundary));
    }
  }
  return { demo: boundary.name, scannedFiles: scanned, violations };
}

export async function checkDemoBoundaries(repoRoot: string, boundaries: readonly DemoBoundary[] = DEMO_BOUNDARIES): Promise<DemoReport[]> {
  const reports: DemoReport[] = [];
  for (const boundary of boundaries) reports.push(await checkDemo(repoRoot, boundary));
  return reports;
}

export function formatReports(reports: readonly DemoReport[]): string {
  const lines: string[] = [];
  for (const report of reports) {
    if (report.scannedFiles === 0) {
      lines.push(`demos-check-boundaries: ${report.demo}: no sources yet, skipped`);
      continue;
    }
    const status = report.violations.length === 0 ? "ok" : `${report.violations.length} violation(s)`;
    lines.push(`demos-check-boundaries: ${report.demo}: ${report.scannedFiles} file(s), ${status}`);
    for (const violation of report.violations) lines.push(`  ${violation.file}:${violation.line}: ${violation.message}`);
  }
  return lines.join("\n");
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
  try {
    const reports = await checkDemoBoundaries(repoRoot);
    const failed = reports.some((report) => report.violations.length > 0);
    const output = formatReports(reports);
    if (failed) {
      console.error(output);
    } else {
      console.log(output);
    }
    process.exitCode = failed ? 1 : 0;
  } catch (error) {
    console.error(`demos-check-boundaries: ${error instanceof Error ? error.message : String(error)}`);
    process.exitCode = 2;
  }
}
