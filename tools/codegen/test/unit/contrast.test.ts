import { describe, expect, it } from "vitest";
import { compositeOver, compositedContrastRatio, contrastRatio, flatten } from "../../src/transforms/contrast.ts";
import { parseHexColor } from "../../src/transforms/css-values.ts";

const rgba = (hex: string) => parseHexColor(hex).rgba;

describe("contrast", () => {
  it("measures opaque pairs with the WCAG 2.x formula", () => {
    expect(contrastRatio(rgba("#000000"), rgba("#ffffff"))).toBeCloseTo(21, 5);
    expect(contrastRatio(rgba("#ffffff"), rgba("#ffffff"))).toBe(1);
    expect(contrastRatio(rgba("#767676"), rgba("#ffffff"))).toBeCloseTo(4.54, 2);
  });

  it("composites a translucent layer over another with the source-over rule", () => {
    expect(compositeOver(rgba("#ffffff80"), rgba("#000000"))).toEqual({ r: 128, g: 128, b: 128, a: 255 });
    expect(compositeOver(rgba("#ff0000"), rgba("#0000ff"))).toEqual({ r: 255, g: 0, b: 0, a: 255 });
    expect(compositeOver(rgba("#00000000"), rgba("#123456"))).toEqual({ r: 0x12, g: 0x34, b: 0x56, a: 255 });
    expect(compositeOver(rgba("#00000000"), rgba("#00000000"))).toEqual({ r: 0, g: 0, b: 0, a: 0 });
    const translucent = compositeOver(rgba("#ffffff80"), rgba("#00000080"));
    expect(translucent.a).toBeCloseTo(191.75, 1);
    expect(translucent.r).toBeCloseTo(170.2, 1);
  });

  it("flattens a stack of layers from an opaque bottom", () => {
    expect(flatten([rgba("#000000"), rgba("#ffffff80"), rgba("#00000080")])).toEqual(
      compositeOver(rgba("#00000080"), compositeOver(rgba("#ffffff80"), rgba("#000000"))),
    );
    expect(flatten([rgba("#fcfcfc")])).toEqual(rgba("#fcfcfc"));
    expect(() => flatten([])).toThrow(/bottom layer must be opaque/);
    expect(() => flatten([rgba("#ffffff80")])).toThrow(/bottom layer must be opaque/);
  });

  it("measures a translucent foreground against the flattened background it sits on", () => {
    expect(compositedContrastRatio(rgba("#000000"), [rgba("#ffffff")])).toBeCloseTo(21, 5);
    expect(compositedContrastRatio(rgba("#00000000"), [rgba("#ffffff")])).toBe(1);
    expect(compositedContrastRatio(rgba("#18181b99"), [rgba("#000000"), rgba("#fcfcfce6")])).toBeCloseTo(4.26, 2);
    expect(compositedContrastRatio(rgba("#ffffff"), [rgba("#ffffff"), rgba("#000000bf"), rgba("#ffffff6b")])).toBeCloseTo(3.19, 2);
  });
});
