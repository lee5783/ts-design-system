import { execFile } from "node:child_process";
import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { promisify } from "node:util";
import { formatOutputStatus, generateFiles, syncOutputs, type CodegenTarget, type Generator } from "./generate.ts";
import { GENERATORS } from "./generators.ts";
import { diffModels, formatDiffReport, type Renames } from "./model/diff-report.ts";
import { TOKENS_PATH, loadTsTokens, readTsTokens } from "./model/load-ts-tokens.ts";
import { readExtensionRenames, readNativeModel, readRenamesMap } from "./model/merge-extensions.ts";
import { TOKEN_FAMILIES, UPSTREAM_STATUSES, type AnyToken, type TokenModel } from "./model/types.ts";

export const USAGE = [
  "usage: codegen <all|tokens|icons> [--check] [--root <dir>]",
  "       codegen model [--json] [--root <dir>]",
  "       codegen diff-report (--base <tokens.json> | --base-ref <git-ref>) [--head <tokens.json>] [--renames <json>] [--json] [--root <dir>]",
].join("\n");

const GENERATE_COMMANDS = ["all", "tokens", "icons"] as const;

type GenerateCommand = (typeof GENERATE_COMMANDS)[number];

export type CliOptions =
  | { command: GenerateCommand; root: string; check: boolean }
  | { command: "model"; root: string; json: boolean }
  | {
      command: "diff-report";
      root: string;
      json: boolean;
      base?: string;
      baseRef?: string;
      head?: string;
      renames?: string;
    };

function isGenerateCommand(value: string): value is GenerateCommand {
  return (GENERATE_COMMANDS as readonly string[]).includes(value);
}

export function parseArgs(argv: string[], defaultRoot: string, cwd: string = process.env["INIT_CWD"] ?? process.cwd()): CliOptions {
  const [command, ...rest] = argv;
  if (command === undefined || (!isGenerateCommand(command) && command !== "model" && command !== "diff-report")) {
    throw new Error(command === undefined ? "missing command" : `unknown command: ${command}`);
  }
  const flags: Record<string, string | true> = {};
  for (let index = 0; index < rest.length; index += 1) {
    const argument = rest[index] as string;
    const takesValue = ["--root", "--base", "--base-ref", "--head", "--renames"].includes(argument);
    const allowed = isGenerateCommand(command)
      ? ["--root", "--check"]
      : command === "model"
        ? ["--root", "--json"]
        : ["--root", "--json", "--base", "--base-ref", "--head", "--renames"];
    if (!allowed.includes(argument)) {
      throw new Error(`unknown argument for ${command}: ${argument}`);
    }
    if (takesValue) {
      const next = rest[index + 1];
      if (next === undefined || next.startsWith("--")) {
        throw new Error(`${argument} needs a value`);
      }
      flags[argument] = next;
      index += 1;
    } else {
      flags[argument] = true;
    }
  }
  const root = typeof flags["--root"] === "string" ? path.resolve(cwd, flags["--root"]) : defaultRoot;
  if (isGenerateCommand(command)) {
    return { command, root, check: flags["--check"] === true };
  }
  if (command === "model") {
    return { command, root, json: flags["--json"] === true };
  }
  const value = (flag: string): string | undefined => {
    const flagValue = flags[flag];
    return typeof flagValue === "string" ? flagValue : undefined;
  };
  const base = value("--base");
  const baseRef = value("--base-ref");
  if ((base === undefined) === (baseRef === undefined)) {
    throw new Error("diff-report needs exactly one of --base or --base-ref");
  }
  const head = value("--head");
  const renames = value("--renames");
  return {
    command,
    root,
    json: flags["--json"] === true,
    ...(base === undefined ? {} : { base: path.resolve(cwd, base) }),
    ...(baseRef === undefined ? {} : { baseRef }),
    ...(head === undefined ? {} : { head: path.resolve(cwd, head) }),
    ...(renames === undefined ? {} : { renames: path.resolve(cwd, renames) }),
  };
}

function extensionSummary(model: TokenModel): string[] {
  if (model.extensions === undefined) {
    return [];
  }
  const tokens = [
    ...TOKEN_FAMILIES.flatMap((family) => (model[family] as AnyToken[]).map((token) => ({ family, token }))),
    ...[...model.controller.color, ...model.controller.size].map((token) => ({ family: "controller", token })),
  ];
  const statuses = UPSTREAM_STATUSES.map(
    (status) => `${status} ${tokens.filter(({ token }) => token.extension?.upstream === status).length}`,
  );
  const deprecated = tokens
    .filter(({ token }) => token.deprecated !== undefined)
    .map(({ family, token }) => `${family}/${token.name.token} -> ${token.deprecated?.renamedTo}`);
  return [
    `  extensions: ${model.extensions.name} v${model.extensions.version}, upstream ${statuses.join(", ")}`,
    `  deprecated aliases: ${deprecated.length > 0 ? deprecated.join(", ") : "none"}`,
  ];
}

export function summarizeModel(model: TokenModel): string {
  const counts = TOKEN_FAMILIES.map((family) => {
    const count = model[family].length;
    return family === "color" ? `${family} ${count}x${model.themes.length}` : `${family} ${count}`;
  });
  counts.push(`controller-color ${model.controller.color.length}x${model.themes.length}`, `controller-size ${model.controller.size.length}`);
  const aliases = model.color.filter((token) => token.aliasOf !== undefined).map((token) => `${token.name.token} -> {${token.aliasOf}}`);
  return [
    `model: ${model.source.name} v${model.source.version}, themes ${model.themes.join("/")}`,
    `  ${counts.join(", ")}`,
    `  aliases: ${aliases.length > 0 ? aliases.join(", ") : "none"}`,
    ...extensionSummary(model),
    ...model.dropped.map((item) => `  dropped ${item.family} (${item.count}): ${item.reason}`),
  ].join("\n");
}

async function readRenames(root: string, file: string | undefined): Promise<Renames> {
  if (file === undefined) {
    return readExtensionRenames(root);
  }
  return readRenamesMap(JSON.parse(await readFile(file, "utf8")), file);
}

async function readBaseModel(root: string, base: string | undefined, baseRef: string | undefined): Promise<TokenModel> {
  if (base !== undefined) {
    return readTsTokens(base);
  }
  const { stdout } = await promisify(execFile)("git", ["-C", root, "show", `${baseRef}:${TOKENS_PATH}`], {
    maxBuffer: 16 * 1024 * 1024,
  });
  return loadTsTokens(JSON.parse(stdout));
}

export async function run(
  options: CliOptions,
  log: (line: string) => void = console.log,
  generators: Generator[] = GENERATORS,
): Promise<number> {
  if (options.command === "diff-report") {
    const baseModel = await readBaseModel(options.root, options.base, options.baseRef);
    const headModel = await readTsTokens(options.head ?? path.join(options.root, TOKENS_PATH));
    const report = diffModels(baseModel, headModel, await readRenames(options.root, options.renames));
    log(options.json ? JSON.stringify(report, null, 2) : formatDiffReport(report));
    return 0;
  }
  const model = await readNativeModel(options.root);
  if (options.command === "model") {
    log(options.json ? JSON.stringify(model, null, 2) : summarizeModel(model));
    return 0;
  }
  const targets: CodegenTarget[] = options.command === "all" ? ["tokens", "icons"] : [options.command];
  const selected = generators.filter((generator) => targets.includes(generator.target));
  const files = await generateFiles(selected, { root: options.root, model });
  const write = !options.check;
  const status = await syncOutputs(options.root, generators, selected, files, write);
  log(`codegen ${options.command}: ${selected.length} generators (${selected.map((generator) => generator.name).join(", ") || "none registered"})`);
  log(formatOutputStatus(status, write));
  return write || status.ok ? 0 : 1;
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  const defaultRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
  let options: CliOptions | undefined;
  try {
    options = parseArgs(process.argv.slice(2), defaultRoot);
    process.exitCode = await run(options);
  } catch (error) {
    console.error(`codegen: ${error instanceof Error ? error.message : String(error)}`);
    if (options === undefined) {
      console.error(USAGE);
    }
    process.exitCode = 2;
  }
}
