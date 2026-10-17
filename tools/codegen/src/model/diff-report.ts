import {
  TOKEN_FAMILIES,
  type AnyToken,
  type ColorToken,
  type ComponentToken,
  type DimensionToken,
  type EasingToken,
  type ShadowLayer,
  type ShadowToken,
  type TokenFamily,
  type TokenModel,
  type TypographyToken,
} from "./types.ts";

export type Bump = "none" | "patch" | "minor" | "major";

export type ChangeKind = "added" | "removed" | "renamed" | "changed" | "documented";

export type Renames = Record<string, string>;

export interface TokenChange {
  family: TokenFamily;
  kind: ChangeKind;
  name: string;
  bump: Bump;
  renamedTo?: string;
  before?: string;
  after?: string;
}

export interface DiffReport {
  bump: Bump;
  changes: TokenChange[];
}

const BUMP_ORDER: Bump[] = ["none", "patch", "minor", "major"];

const KIND_BUMP: Record<ChangeKind, Bump> = {
  added: "minor",
  removed: "major",
  renamed: "minor",
  changed: "minor",
  documented: "patch",
};

function maxBump(bumps: Bump[]): Bump {
  return bumps.reduce<Bump>((current, next) => (BUMP_ORDER.indexOf(next) > BUMP_ORDER.indexOf(current) ? next : current), "none");
}

function describeLayers(layers: ShadowLayer[]): string {
  return layers
    .map((layer) => `${layer.inset ? "inset " : ""}${layer.offsetX} ${layer.offsetY} ${layer.blur} ${layer.spread} ${layer.color.hex}`)
    .join(", ");
}

export function describeToken(family: TokenFamily, token: AnyToken): string {
  switch (family) {
    case "color": {
      const color = token as ColorToken;
      const alias = color.aliasOf === undefined ? "" : ` ({${color.aliasOf}})`;
      return `light ${color.value.light.hex} dark ${color.value.dark.hex}${alias}`;
    }
    case "typography": {
      const style = token as TypographyToken;
      return `${style.fontSize}/${style.lineHeight} ${style.fontWeight} ${style.letterSpacingEm}em ${style.family}`;
    }
    case "shadow": {
      const shadow = token as ShadowToken;
      return `light ${describeLayers(shadow.value.light)} | dark ${describeLayers(shadow.value.dark)}`;
    }
    case "easing":
      return `cubic-bezier(${(token as EasingToken).value.join(", ")})`;
    case "component": {
      const constant = token as ComponentToken;
      return `${constant.value} ${constant.type}`;
    }
    default:
      return String((token as DimensionToken).value);
  }
}

function documentation(token: AnyToken): string {
  return JSON.stringify([token.usage ?? null, "sample" in token ? (token.sample ?? null) : null]);
}

function change(family: TokenFamily, kind: ChangeKind, name: string, extra: Partial<TokenChange> = {}): TokenChange {
  return { family, kind, name, bump: KIND_BUMP[kind], ...extra };
}

function diffFamily(family: TokenFamily, base: AnyToken[], head: AnyToken[], renames: Renames): TokenChange[] {
  const baseByName = new Map(base.map((token) => [token.name.token, token]));
  const headByName = new Map(head.map((token) => [token.name.token, token]));
  const changes: TokenChange[] = [];
  for (const [name, before] of baseByName) {
    const after = headByName.get(name);
    if (after === undefined) {
      const target = renames[name];
      const renamed = target === undefined ? undefined : headByName.get(target);
      changes.push(
        renamed === undefined || target === undefined
          ? change(family, "removed", name, { before: describeToken(family, before) })
          : change(family, "renamed", name, {
              renamedTo: target,
              before: describeToken(family, before),
              after: describeToken(family, renamed),
            }),
      );
      continue;
    }
    const beforeValue = describeToken(family, before);
    const afterValue = describeToken(family, after);
    if (beforeValue !== afterValue) {
      changes.push(change(family, "changed", name, { before: beforeValue, after: afterValue }));
    } else if (documentation(before) !== documentation(after)) {
      changes.push(change(family, "documented", name));
    }
  }
  for (const [name, after] of headByName) {
    if (!baseByName.has(name)) {
      changes.push(change(family, "added", name, { after: describeToken(family, after) }));
    }
  }
  return changes;
}

export function diffModels(base: TokenModel, head: TokenModel, renames: Renames = {}): DiffReport {
  const changes = TOKEN_FAMILIES.flatMap((family) => diffFamily(family, base[family], head[family], renames));
  return { bump: maxBump(changes.map((item) => item.bump)), changes };
}

export function formatDiffReport(report: DiffReport): string {
  const lines = [`diff-report: suggested bump ${report.bump} (${report.changes.length} token changes)`];
  for (const item of report.changes) {
    const name = item.renamedTo === undefined ? item.name : `${item.name} -> ${item.renamedTo} (deprecated alias)`;
    const detail =
      item.before !== undefined && item.after !== undefined
        ? `: ${item.before} => ${item.after}`
        : item.after !== undefined
          ? `: ${item.after}`
          : item.before !== undefined
            ? `: was ${item.before}`
            : "";
    lines.push(`  ${item.bump.padEnd(5)}  ${item.kind.padEnd(10)}  ${item.family}/${name}${detail}`);
  }
  return lines.join("\n");
}
