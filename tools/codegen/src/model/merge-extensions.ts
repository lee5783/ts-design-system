import { readFile } from "node:fs/promises";
import path from "node:path";
import { parseCubicBezier, parseHexColor, parseMs, parseNumber, parsePx } from "../transforms/css-values.ts";
import { tokenName, tokenSegments } from "../transforms/naming.ts";
import type { Renames } from "./diff-report.ts";
import { TOKENS_PATH, readTsTokens, uniqueNames } from "./load-ts-tokens.ts";
import {
  COMPONENT_CONSTANT_TYPES,
  THEMES,
  TOKEN_FAMILIES,
  UPSTREAM_STATUSES,
  type AnyToken,
  type ColorToken,
  type ColorValue,
  type ComponentConstantType,
  type ComponentToken,
  type ExtensionInfo,
  type ThemeId,
  type Themed,
  type TokenFamily,
  type TokenModel,
} from "./types.ts";

export const EXTENSIONS_PATH = "design/extensions/native-tokens.json";

const EXTENSION_FAMILIES = ["color", "spacing", "radius", "size", "opacity", "duration", "easing", "component"] as const;

type ExtensionFamily = (typeof EXTENSION_FAMILIES)[number];

const META_KEYS = new Set(["name", "version", "renames"]);
const ENTRY_KEYS = new Set(["name", "value", "usage", "upstream", "origin", "component", "type"]);
const ALIAS = /^\{([a-z0-9-]+)\}$/;

const SCALAR_PARSERS: Record<Exclude<ExtensionFamily, "color" | "easing" | "component">, (input: string) => number> = {
  spacing: parsePx,
  radius: parsePx,
  size: parsePx,
  opacity: parseNumber,
  duration: parseMs,
};

const COMPONENT_PARSERS: Record<ComponentConstantType, (input: string) => number> = {
  dimension: parsePx,
  "font-weight": parseFontWeight,
  duration: parseMs,
  number: parseNumber,
};

interface RawEntry {
  name: string;
  value: unknown;
  usage?: string;
  extension: ExtensionInfo;
  raw: Record<string, unknown>;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function isExtensionFamily(value: string): value is ExtensionFamily {
  return (EXTENSION_FAMILIES as readonly string[]).includes(value);
}

function parseFontWeight(input: string): number {
  const weight = parseNumber(input);
  if (!Number.isInteger(weight) || weight < 100 || weight > 900 || weight % 100 !== 0) {
    throw new Error(`invalid font weight "${input}": expected 100 to 900 in steps of 100`);
  }
  return weight;
}

function fail(label: string, error: unknown): never {
  throw new Error(`${label}: ${error instanceof Error ? error.message : String(error)}`);
}

function rawEntries(input: Record<string, unknown>, family: ExtensionFamily): RawEntry[] {
  const section = input[family];
  if (section === undefined) {
    return [];
  }
  if (!isRecord(section) || !Array.isArray(section["tokens"])) {
    throw new Error(`native-tokens ${family}: expected an object with a "tokens" array`);
  }
  return section["tokens"].map((entry, index) => {
    if (!isRecord(entry) || typeof entry["name"] !== "string" || !("value" in entry)) {
      throw new Error(`native-tokens ${family}.tokens[${index}]: expected { name, value, upstream }`);
    }
    const label = `native-tokens ${family}/${entry["name"]}`;
    const unknown = Object.keys(entry).filter((key) => !ENTRY_KEYS.has(key));
    if (unknown.length > 0) {
      throw new Error(`${label}: unknown keys ${unknown.join(", ")}`);
    }
    const upstream = entry["upstream"];
    if (typeof upstream !== "string" || !(UPSTREAM_STATUSES as readonly string[]).includes(upstream)) {
      throw new Error(`${label}: "upstream" must be one of ${UPSTREAM_STATUSES.join(", ")}`);
    }
    const origin = entry["origin"];
    return {
      name: entry["name"],
      value: entry["value"],
      ...(typeof entry["usage"] === "string" ? { usage: entry["usage"] } : {}),
      extension: { upstream: upstream as ExtensionInfo["upstream"], ...(typeof origin === "string" ? { origin } : {}) },
      raw: entry,
    };
  });
}

function base(entry: RawEntry): Pick<AnyToken, "name" | "usage" | "extension"> {
  return {
    name: tokenName(entry.name),
    ...(entry.usage === undefined ? {} : { usage: entry.usage }),
    extension: entry.extension,
  };
}

function themedValue(label: string, value: unknown): Themed<string> {
  if (typeof value === "string") {
    return { light: value, dark: value };
  }
  if (isRecord(value) && THEMES.every((theme) => typeof value[theme] === "string")) {
    return { light: value["light"] as string, dark: value["dark"] as string };
  }
  throw new Error(`${label}: expected a string or a { light, dark } value`);
}

function colorEntries(entries: RawEntry[], known: ColorToken[]): ColorToken[] {
  const colors = [...known];
  const added: ColorToken[] = [];
  for (const entry of entries) {
    const label = `native-tokens color/${entry.name}`;
    const source = themedValue(label, entry.value);
    const resolve = (theme: ThemeId): ColorValue => {
      const alias = ALIAS.exec(source[theme])?.[1];
      if (alias === undefined) {
        try {
          return parseHexColor(source[theme]);
        } catch (error) {
          return fail(`${label} (${theme})`, error);
        }
      }
      const target = colors.find((color) => color.name.token === alias);
      if (target === undefined) {
        throw new Error(`${label}: unknown alias target "{${alias}}"`);
      }
      return target.value[theme];
    };
    const lightAlias = ALIAS.exec(source.light)?.[1];
    const aliasOf = lightAlias !== undefined && lightAlias === ALIAS.exec(source.dark)?.[1] ? { aliasOf: lightAlias } : {};
    const token: ColorToken = { ...base(entry), value: { light: resolve("light"), dark: resolve("dark") }, ...aliasOf };
    colors.push(token);
    added.push(token);
  }
  return added;
}

function componentEntries(entries: RawEntry[]): ComponentToken[] {
  return entries.map((entry) => {
    const label = `native-tokens component/${entry.name}`;
    const component = entry.raw["component"];
    const type = entry.raw["type"];
    if (typeof component !== "string") {
      throw new Error(`${label}: missing "component"`);
    }
    try {
      tokenSegments(component);
    } catch (error) {
      fail(label, error);
    }
    if (typeof type !== "string" || !(COMPONENT_CONSTANT_TYPES as readonly string[]).includes(type)) {
      throw new Error(`${label}: "type" must be one of ${COMPONENT_CONSTANT_TYPES.join(", ")}`);
    }
    if (typeof entry.value !== "string" && typeof entry.value !== "number") {
      throw new Error(`${label}: expected a single value`);
    }
    try {
      const constantType = type as ComponentConstantType;
      return { ...base(entry), component, type: constantType, value: COMPONENT_PARSERS[constantType](String(entry.value)) };
    } catch (error) {
      return fail(label, error);
    }
  });
}

function familyEntries(model: TokenModel, family: ExtensionFamily, entries: RawEntry[]): AnyToken[] {
  if (family === "color") {
    return colorEntries(entries, model.color);
  }
  if (family === "component") {
    return componentEntries(entries);
  }
  return entries.map((entry) => {
    const label = `native-tokens ${family}/${entry.name}`;
    if (typeof entry.value !== "string" && typeof entry.value !== "number") {
      throw new Error(`${label}: expected a single value`);
    }
    try {
      return family === "easing"
        ? { ...base(entry), value: parseCubicBezier(String(entry.value)) }
        : { ...base(entry), value: SCALAR_PARSERS[family](String(entry.value)) };
    } catch (error) {
      return fail(label, error);
    }
  });
}

function checkDuplicates(model: TokenModel, family: ExtensionFamily, entries: RawEntry[]): void {
  const sourceNames = new Set(model[family].map((token) => token.name.token));
  const seen = new Set<string>();
  for (const { name } of entries) {
    if (sourceNames.has(name)) {
      throw new Error(
        `native-tokens ${family}/${name}: duplicate key, already defined in ${TOKENS_PATH}; remove it from ${EXTENSIONS_PATH} once the DS ships it`,
      );
    }
    if (seen.has(name)) {
      throw new Error(`native-tokens ${family}/${name}: duplicate key in ${EXTENSIONS_PATH}`);
    }
    seen.add(name);
  }
}

export function readRenamesMap(input: unknown, label: string = EXTENSIONS_PATH): Renames {
  const map = isRecord(input) && "renames" in input ? input["renames"] : input;
  if (!isRecord(map) || Object.values(map).some((value) => typeof value !== "string")) {
    throw new Error(`${label}: expected a { "old-name": "new-name" } map or an object with a "renames" map`);
  }
  return map as Renames;
}

function findToken(model: TokenModel, name: string): { family: TokenFamily; token: AnyToken }[] {
  return TOKEN_FAMILIES.flatMap((family) =>
    (model[family] as AnyToken[]).filter((token) => token.name.token === name).map((token) => ({ family, token })),
  );
}

function deprecatedAlias(family: TokenFamily, target: AnyToken, oldName: string): AnyToken {
  const { extension: _extension, usage: _usage, ...rest } = target;
  return {
    ...rest,
    ...(family === "color" ? { aliasOf: target.name.token } : {}),
    name: tokenName(oldName),
    usage: `Deprecated: use ${target.name.token}.`,
    deprecated: { renamedTo: target.name.token },
  } as AnyToken;
}

function applyRenames(model: TokenModel, renames: Renames): TokenModel {
  const merged = { ...model };
  for (const [oldName, newName] of Object.entries(renames)) {
    const label = `native-tokens renames/${oldName}`;
    try {
      tokenSegments(oldName);
    } catch (error) {
      fail(label, error);
    }
    if (findToken(model, oldName).length > 0) {
      throw new Error(`${label}: "${oldName}" still exists; drop the rename or the token`);
    }
    const targets = findToken(model, newName);
    if (targets.length !== 1 || targets[0] === undefined) {
      throw new Error(
        targets.length === 0
          ? `${label}: rename target "${newName}" does not exist`
          : `${label}: rename target "${newName}" exists in ${targets.map((target) => target.family).join(" and ")}`,
      );
    }
    const { family, token } = targets[0];
    (merged as Record<TokenFamily, AnyToken[]>)[family] = [...merged[family], deprecatedAlias(family, token, oldName)];
  }
  return merged;
}

export function mergeExtensions(model: TokenModel, input: unknown): TokenModel {
  if (!isRecord(input)) {
    throw new Error(`${EXTENSIONS_PATH}: expected a JSON object`);
  }
  const unknown = Object.keys(input).filter((key) => !META_KEYS.has(key) && !isExtensionFamily(key));
  if (unknown.length > 0) {
    throw new Error(`${EXTENSIONS_PATH}: unsupported families ${unknown.join(", ")}; extensions take ${EXTENSION_FAMILIES.join(", ")}`);
  }
  let merged: TokenModel = {
    ...model,
    extensions: {
      name: typeof input["name"] === "string" ? input["name"] : "",
      version: typeof input["version"] === "number" ? input["version"] : 0,
    },
  };
  for (const family of EXTENSION_FAMILIES) {
    const entries = rawEntries(input, family);
    checkDuplicates(merged, family, entries);
    const added = familyEntries(merged, family, entries);
    merged = { ...merged, [family]: [...merged[family], ...added] };
  }
  merged = applyRenames(merged, input["renames"] === undefined ? {} : readRenamesMap(input["renames"]));
  for (const family of TOKEN_FAMILIES) {
    uniqueNames(family, merged[family] as AnyToken[]);
  }
  return merged;
}

async function readJsonIfExists(file: string): Promise<unknown> {
  try {
    return JSON.parse(await readFile(file, "utf8"));
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === "ENOENT") {
      return undefined;
    }
    throw error;
  }
}

export async function readExtensions(root: string): Promise<unknown> {
  return readJsonIfExists(path.join(root, EXTENSIONS_PATH));
}

export async function readExtensionRenames(root: string): Promise<Renames> {
  const extensions = await readExtensions(root);
  return isRecord(extensions) && extensions["renames"] !== undefined ? readRenamesMap(extensions["renames"]) : {};
}

export async function readNativeModel(root: string): Promise<TokenModel> {
  const model = await readTsTokens(path.join(root, TOKENS_PATH));
  const extensions = await readExtensions(root);
  return extensions === undefined ? model : mergeExtensions(model, extensions);
}
