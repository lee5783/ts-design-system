import { describe, expect, it } from "vitest";
import { toCamelName, toSnakeName, tokenName } from "../../src/transforms/naming.ts";

describe("naming", () => {
  it("converts kebab-case to camelCase", () => {
    expect(toCamelName("accent-primary")).toBe("accentPrimary");
    expect(toCamelName("background-secondary")).toBe("backgroundSecondary");
    expect(toCamelName("title-1")).toBe("title1");
    expect(toCamelName("space-16")).toBe("space16");
    expect(toCamelName("text-on-accent")).toBe("textOnAccent");
  });

  it("keeps adjacent numeric segments apart with an underscore", () => {
    expect(toCamelName("space-0-5")).toBe("space0_5");
    expect(toSnakeName("space-0-5")).toBe("space_0_5");
  });

  it("converts kebab-case to snake_case for resource names", () => {
    expect(toSnakeName("accent-primary")).toBe("accent_primary");
    expect(tokenName("body-lg")).toEqual({ token: "body-lg", camel: "bodyLg", snake: "body_lg" });
  });

  it("rejects names that are not lowercase kebab-case or start with a digit", () => {
    expect(() => toCamelName("Accent-primary")).toThrow(/kebab-case/);
    expect(() => toCamelName("accent--primary")).toThrow(/kebab-case/);
    expect(() => toCamelName("accent_primary")).toThrow(/kebab-case/);
    expect(() => toCamelName("2xl")).toThrow(/digit/);
  });
});
