import { describe, expect, it } from "vitest";
import { loadTsTokens } from "../../src/model/load-ts-tokens.ts";
import { rawFixture, rawRealTokens } from "../support.ts";

function color(raw: Record<string, any>, name: string) {
  const token = loadTsTokens(raw).color.find((item) => item.name.token === name);
  if (!token) throw new Error(`missing ${name}`);
  return token;
}

describe("alias resolution", () => {
  it("resolves focus to accent-text per theme in the real tokens", () => {
    const focus = color(rawRealTokens(), "focus");
    expect(focus.aliasOf).toBe("accent-text");
    expect(focus.value.light.hex).toBe("#7c3aed");
    expect(focus.value.dark.hex).toBe("#a78bfa");
  });

  it("resolves alias chains", () => {
    const raw = rawFixture();
    raw.color.tokens.push({ name: "ring", value: "{focus}" });
    const ring = color(raw, "ring");
    expect(ring.aliasOf).toBe("focus");
    expect(ring.value.dark.hex).toBe("#a78bfa");
  });

  it("resolves a per-theme alias without marking the token as a whole alias", () => {
    const raw = rawFixture();
    raw.color.tokens.push({ name: "mixed", value: { light: "{text-primary}", dark: "#123456" } });
    const mixed = color(raw, "mixed");
    expect(mixed.aliasOf).toBeUndefined();
    expect(mixed.value.light.hex).toBe("#18181b");
    expect(mixed.value.dark.hex).toBe("#123456");
  });

  it("fails on an unknown alias target", () => {
    const raw = rawFixture();
    raw.color.tokens.push({ name: "broken", value: "{does-not-exist}" });
    expect(() => loadTsTokens(raw)).toThrow(/unknown alias target "\{does-not-exist\}"/);
  });

  it("fails on an alias cycle", () => {
    const raw = rawFixture();
    raw.color.tokens.push({ name: "loop-a", value: "{loop-b}" }, { name: "loop-b", value: "{loop-a}" });
    expect(() => loadTsTokens(raw)).toThrow(/alias cycle/);
  });
});
