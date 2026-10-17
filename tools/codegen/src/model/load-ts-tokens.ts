import { readFile } from "node:fs/promises";
import { parseEm, parseHexColor, parseNumber, parsePx, parseShadow } from "../transforms/css-values.ts";
import { tokenName } from "../transforms/naming.ts";
import { dropWebOnly, type RawSource } from "./drop-web-only.ts";
import {
  THEMES,
  type ColorToken,
  type ColorValue,
  type DimensionToken,
  type FontFamily,
  type OpacityToken,
  type ShadowToken,
  type ThemeId,
  type Themed,
  type TokenModel,
  type TokenName,
  type TypographyToken,
} from "./types.ts";

export const TOKENS_PATH = "design/source/tokens.json";

const META_KEYS = new Set(["name", "version"]);
const FAMILY_KEYS = new Set(["color", "type", "spacing", "radius", "shadow", "size", "opacity"]);
const FONT_FAMILIES = new Set<string>(["sans", "mono"]);
const ALIAS = /^\{([a-z0-9-]+)\}$/;

interface RawToken {
  name: string;
  value: unknown;
  usage?: string;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function rawTokens(raw: RawSource, family: string): RawToken[] {
  const section = raw[family];
  if (section === undefined) {
    return [];
  }
  if (!isRecord(section) || !Array.isArray(section["tokens"])) {
    throw new Error(`${family}: expected an object with a "tokens" array`);
  }
  return section["tokens"].map((token, index) => {
    if (!isRecord(token) || typeof token["name"] !== "string" || !("value" in token)) {
      throw new Error(`${family}.tokens[${index}]: expected { name, value }`);
    }
    return {
      name: token["name"],
      value: token["value"],
      ...(typeof token["usage"] === "string" ? { usage: token["usage"] } : {}),
    };
  });
}

function withUsage<T extends object>(token: T, usage: string | undefined): T & { usage?: string } {
  return usage === undefined ? token : { ...token, usage };
}

export function uniqueNames<T extends { name: TokenName }>(family: string, tokens: T[]): T[] {
  const tokenNames = new Set<string>();
  const camelNames = new Map<string, string>();
  for (const { name } of tokens) {
    if (tokenNames.has(name.token)) {
      throw new Error(`${family}: duplicate token "${name.token}"`);
    }
    const clash = camelNames.get(name.camel);
    if (clash !== undefined) {
      throw new Error(`${family}: "${clash}" and "${name.token}" both map to "${name.camel}"`);
    }
    tokenNames.add(name.token);
    camelNames.set(name.camel, name.token);
  }
  return tokens;
}

function themedStrings(family: string, token: RawToken): Themed<string> {
  if (typeof token.value === "string") {
    return { light: token.value, dark: token.value };
  }
  if (isRecord(token.value)) {
    const value = token.value;
    const result = {} as Themed<string>;
    for (const theme of THEMES) {
      const themeValue = value[theme];
      if (typeof themeValue !== "string") {
        throw new Error(`${family}/${token.name}: missing "${theme}" value`);
      }
      result[theme] = themeValue;
    }
    return result;
  }
  throw new Error(`${family}/${token.name}: expected a string or a { light, dark } value`);
}

function checkThemes(raw: RawSource): void {
  const color = raw["color"];
  const themes = isRecord(color) && Array.isArray(color["themes"]) ? color["themes"] : [];
  const ids = themes.map((theme) => (isRecord(theme) ? theme["id"] : undefined));
  if (ids.length !== THEMES.length || !THEMES.every((theme) => ids.includes(theme))) {
    throw new Error(`color.themes: expected exactly ${THEMES.join(" and ")}, got ${JSON.stringify(ids)}`);
  }
}

function loadColors(raw: RawSource): ColorToken[] {
  const tokens = rawTokens(raw, "color");
  const sources = new Map(tokens.map((token) => [token.name, themedStrings("color", token)]));

  const resolve = (name: string, theme: ThemeId, chain: string[]): ColorValue => {
    if (chain.includes(name)) {
      throw new Error(`color: alias cycle ${[...chain, name].join(" -> ")}`);
    }
    const source = sources.get(name);
    if (source === undefined) {
      throw new Error(`color/${chain.at(-1) ?? name}: unknown alias target "{${name}}"`);
    }
    const alias = ALIAS.exec(source[theme]);
    if (alias?.[1]) {
      return resolve(alias[1], theme, [...chain, name]);
    }
    try {
      return parseHexColor(source[theme]);
    } catch (error) {
      throw new Error(`color/${name} (${theme}): ${error instanceof Error ? error.message : String(error)}`);
    }
  };

  return uniqueNames(
    "color",
    tokens.map((token) => {
      const source = sources.get(token.name) as Themed<string>;
      const lightAlias = ALIAS.exec(source.light)?.[1];
      const darkAlias = ALIAS.exec(source.dark)?.[1];
      const value = { light: resolve(token.name, "light", []), dark: resolve(token.name, "dark", []) };
      const aliasOf = lightAlias !== undefined && lightAlias === darkAlias ? { aliasOf: lightAlias } : {};
      return withUsage({ name: tokenName(token.name), value, ...aliasOf }, token.usage);
    }),
  );
}

function loadTypography(raw: RawSource): TypographyToken[] {
  const type = raw["type"];
  if (type === undefined) {
    return [];
  }
  if (!isRecord(type) || !Array.isArray(type["groups"])) {
    throw new Error(`type: expected an object with a "groups" array`);
  }
  const styles: TypographyToken[] = [];
  for (const group of type["groups"]) {
    if (!isRecord(group) || typeof group["name"] !== "string" || !Array.isArray(group["styles"])) {
      throw new Error(`type.groups: expected { name, styles }`);
    }
    for (const style of group["styles"]) {
      if (!isRecord(style) || typeof style["name"] !== "string") {
        throw new Error(`type/${group["name"]}: expected styles with a name`);
      }
      const name = style["name"];
      const family = style["family"] ?? group["family"];
      if (typeof family !== "string" || !FONT_FAMILIES.has(family)) {
        throw new Error(`type/${name}: unsupported font family "${String(family)}"`);
      }
      const read = <T>(label: string, parse: (input: string) => T, input: unknown): T => {
        if (typeof input !== "string") {
          throw new Error(`type/${name}: missing ${label}`);
        }
        try {
          return parse(input);
        } catch (error) {
          throw new Error(`type/${name}: ${error instanceof Error ? error.message : String(error)}`);
        }
      };
      const token: TypographyToken = {
        name: tokenName(name),
        group: group["name"],
        family: family as FontFamily,
        fontSize: read("fontSize", parsePx, style["fontSize"]),
        lineHeight: read("lineHeight", parsePx, style["lineHeight"]),
        fontWeight: read(
          "fontWeight",
          parseNumber,
          typeof style["fontWeight"] === "number" ? String(style["fontWeight"]) : style["fontWeight"],
        ),
        letterSpacingEm: style["letterSpacing"] === undefined ? 0 : read("letterSpacing", parseEm, style["letterSpacing"]),
        ...(typeof style["sample"] === "string" ? { sample: style["sample"] } : {}),
      };
      styles.push(withUsage(token, typeof style["usage"] === "string" ? style["usage"] : undefined));
    }
  }
  return uniqueNames("type", styles);
}

function loadScalars<T extends DimensionToken | OpacityToken>(
  raw: RawSource,
  family: string,
  parse: (input: string) => number,
): T[] {
  return uniqueNames(
    family,
    rawTokens(raw, family).map((token) => {
      if (typeof token.value !== "string" && typeof token.value !== "number") {
        throw new Error(`${family}/${token.name}: expected a single value`);
      }
      try {
        return withUsage({ name: tokenName(token.name), value: parse(String(token.value)) }, token.usage) as T;
      } catch (error) {
        throw new Error(`${family}/${token.name}: ${error instanceof Error ? error.message : String(error)}`);
      }
    }),
  );
}

function loadShadows(raw: RawSource): ShadowToken[] {
  return uniqueNames(
    "shadow",
    rawTokens(raw, "shadow").map((token) => {
      const source = themedStrings("shadow", token);
      try {
        const value = { light: parseShadow(source.light), dark: parseShadow(source.dark) };
        return withUsage({ name: tokenName(token.name), value }, token.usage);
      } catch (error) {
        throw new Error(`shadow/${token.name}: ${error instanceof Error ? error.message : String(error)}`);
      }
    }),
  );
}

export function loadTsTokens(input: unknown): TokenModel {
  if (!isRecord(input)) {
    throw new Error("tokens.json: expected a JSON object");
  }
  const { kept, dropped } = dropWebOnly(input);
  const unknown = Object.keys(kept).filter((key) => !META_KEYS.has(key) && !FAMILY_KEYS.has(key));
  if (unknown.length > 0) {
    throw new Error(`tokens.json: unknown token families ${unknown.join(", ")}; map them in the adapter or drop them in drop-web-only`);
  }
  checkThemes(kept);
  return {
    source: {
      name: typeof kept["name"] === "string" ? kept["name"] : "",
      version: typeof kept["version"] === "number" ? kept["version"] : 0,
    },
    themes: THEMES,
    color: loadColors(kept),
    typography: loadTypography(kept),
    spacing: loadScalars<DimensionToken>(kept, "spacing", parsePx),
    radius: loadScalars<DimensionToken>(kept, "radius", parsePx),
    shadow: loadShadows(kept),
    size: loadScalars<DimensionToken>(kept, "size", parsePx),
    opacity: loadScalars<OpacityToken>(kept, "opacity", parseNumber),
    duration: [],
    easing: [],
    component: [],
    dropped,
  };
}

export async function readTsTokens(file: string): Promise<TokenModel> {
  return loadTsTokens(JSON.parse(await readFile(file, "utf8")));
}
