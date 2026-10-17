import { mkdir, readFile, readdir, rm, writeFile } from "node:fs/promises";
import path from "node:path";
import type { TokenModel } from "./model/types.ts";

export const CODEGEN_TARGETS = ["tokens", "icons"] as const;

export type CodegenTarget = (typeof CODEGEN_TARGETS)[number];

export interface GeneratedFile {
  path: string;
  contents: string;
}

export interface GeneratorContext {
  root: string;
  model: TokenModel;
}

export interface Generator {
  name: string;
  target: CodegenTarget;
  outputDir: string;
  generate(context: GeneratorContext): GeneratedFile[] | Promise<GeneratedFile[]>;
}

export interface OutputStatus {
  ok: boolean;
  files: number;
  changed: string[];
  missing: string[];
  stale: string[];
}

const IGNORED_NAMES = new Set([".DS_Store", ".gitkeep"]);

function toPosix(value: string): string {
  return value.split(path.sep).join("/");
}

function isInside(dir: string, file: string): boolean {
  const relative = path.posix.relative(dir, file);
  return relative !== "" && !relative.startsWith("..") && !path.posix.isAbsolute(relative);
}

async function listOutputFiles(root: string, dir: string): Promise<string[]> {
  try {
    const entries = await readdir(path.join(root, dir), { recursive: true, withFileTypes: true });
    return entries
      .filter((entry) => entry.isFile() && !IGNORED_NAMES.has(entry.name))
      .map((entry) => toPosix(path.relative(root, path.join(entry.parentPath, entry.name))))
      .sort();
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === "ENOENT") {
      return [];
    }
    throw error;
  }
}

async function readIfExists(file: string): Promise<string | undefined> {
  try {
    return await readFile(file, "utf8");
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === "ENOENT") {
      return undefined;
    }
    throw error;
  }
}

export async function generateFiles(generators: Generator[], context: GeneratorContext): Promise<GeneratedFile[]> {
  const files = new Map<string, GeneratedFile>();
  for (const generator of generators) {
    for (const file of await generator.generate(context)) {
      const normalized = path.posix.normalize(file.path);
      if (!isInside(generator.outputDir, normalized)) {
        throw new Error(`${generator.name}: ${file.path} is outside its output directory ${generator.outputDir}`);
      }
      if (files.has(normalized)) {
        throw new Error(`${generator.name}: ${normalized} is generated twice`);
      }
      files.set(normalized, { path: normalized, contents: file.contents });
    }
  }
  return [...files.values()].sort((left, right) => left.path.localeCompare(right.path));
}

export function assertDisjointOutputDirs(generators: Generator[]): void {
  for (const inner of generators) {
    const outer = generators.find((generator) => isInside(generator.outputDir, inner.outputDir));
    if (outer !== undefined) {
      throw new Error(`${inner.name}: output directory ${inner.outputDir} is nested inside ${outer.outputDir} of ${outer.name}`);
    }
  }
}

export async function syncOutputs(
  root: string,
  allGenerators: Generator[],
  selected: Generator[],
  files: GeneratedFile[],
  write: boolean,
): Promise<OutputStatus> {
  assertDisjointOutputDirs(allGenerators);
  const changed: string[] = [];
  const missing: string[] = [];
  const stale: string[] = [];
  for (const file of files) {
    const target = path.join(root, file.path);
    const current = await readIfExists(target);
    if (current === file.contents) {
      continue;
    }
    (current === undefined ? missing : changed).push(file.path);
    if (write) {
      await mkdir(path.dirname(target), { recursive: true });
      await writeFile(target, file.contents);
    }
  }
  const produced = new Set(files.map((file) => file.path));
  const ownedDirs = [...new Set(selected.map((generator) => generator.outputDir))].filter((dir) =>
    allGenerators.filter((generator) => generator.outputDir === dir).every((generator) => selected.includes(generator)),
  );
  for (const dir of ownedDirs) {
    for (const existing of await listOutputFiles(root, dir)) {
      if (!produced.has(existing)) {
        stale.push(existing);
        if (write) {
          await rm(path.join(root, existing));
        }
      }
    }
  }
  return { ok: changed.length + missing.length + stale.length === 0, files: files.length, changed, missing, stale };
}

export function formatOutputStatus(status: OutputStatus, write: boolean): string {
  const lines = [
    ...status.changed.map((file) => `${write ? "updated" : "changed"}  ${file}`),
    ...status.missing.map((file) => `${write ? "created" : "missing"}  ${file}`),
    ...status.stale.map((file) => `${write ? "removed" : "stale  "}  ${file}`),
  ];
  if (write) {
    lines.push(`codegen: ${status.files} generated files, ${lines.length} written or removed`);
  } else {
    lines.push(
      status.ok
        ? `codegen: ${status.files} generated files are up to date`
        : "codegen: generated code is out of date; run make codegen and commit the result",
    );
  }
  return lines.join("\n");
}
