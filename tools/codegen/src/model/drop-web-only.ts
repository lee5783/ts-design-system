import type { DroppedFamily } from "./types.ts";

export const WEB_ONLY_FAMILIES: Record<string, string> = {
  breakpoint: "web layout breakpoints; native uses size classes and WindowSizeClass",
  zIndex: "web stacking order; native stacking comes from presentation",
};

export const WEB_ONLY_TYPE_KEYS: Record<string, string> = {
  families: "CSS font stacks; native uses the system fonts",
  fonts: "web font files; native ships no font files",
};

export interface RawSource {
  [key: string]: unknown;
}

function countEntries(value: unknown): number {
  if (Array.isArray(value)) {
    return value.length;
  }
  if (value !== null && typeof value === "object") {
    const tokens = (value as { tokens?: unknown }).tokens;
    return Array.isArray(tokens) ? tokens.length : Object.keys(value).length;
  }
  return 0;
}

export function dropWebOnly(raw: RawSource): { kept: RawSource; dropped: DroppedFamily[] } {
  const kept: RawSource = {};
  const dropped: DroppedFamily[] = [];
  for (const [key, value] of Object.entries(raw)) {
    const reason = WEB_ONLY_FAMILIES[key];
    if (reason === undefined) {
      kept[key] = value;
    } else {
      dropped.push({ family: key, count: countEntries(value), reason });
    }
  }
  const type = kept["type"];
  if (type !== null && typeof type === "object" && !Array.isArray(type)) {
    const keptType: RawSource = {};
    for (const [key, value] of Object.entries(type)) {
      const reason = WEB_ONLY_TYPE_KEYS[key];
      if (reason === undefined) {
        keptType[key] = value;
      } else {
        dropped.push({ family: `type.${key}`, count: countEntries(value), reason });
      }
    }
    kept["type"] = keptType;
  }
  return { kept, dropped };
}
