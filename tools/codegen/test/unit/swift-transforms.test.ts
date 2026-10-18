import { describe, expect, it } from "vitest";
import { SWIFT_TOKENS_DIR, swiftIconNameGenerator, swiftTokenFiles, swiftTokensGenerator } from "../../src/formats/swift/index.ts";
import { composeShapes } from "../../src/formats/compose/compose-theme.ts";
import { swiftRadius } from "../../src/formats/swift/dimensions.ts";
import { swiftShadows } from "../../src/formats/swift/shadows.ts";
import { GENERATORS } from "../../src/generators.ts";
import { readTsTokens } from "../../src/model/load-ts-tokens.ts";
import { readNativeModel } from "../../src/model/merge-extensions.ts";
import type { DimensionToken, TokenModel, TypographyToken } from "../../src/model/types.ts";
import { tokenName } from "../../src/transforms/naming.ts";
import {
  isCapsuleRadius,
  iosBaseTextStyle,
  msToSeconds,
  swiftIdentifier,
  swiftNumber,
  swiftPrefixedName,
  swiftRgbaHex,
  trackingPoints,
} from "../../src/transforms/swift.ts";
import { realTokensPath, repoRoot } from "../support.ts";

function typography(token: string, fontSize: number, letterSpacingEm = 0): TypographyToken {
  return { name: tokenName(token), group: "g", family: "sans", fontSize, lineHeight: fontSize + 4, fontWeight: 400, letterSpacingEm };
}

describe("Swift value transforms", () => {
  it("formats numbers as stable Swift literals", () => {
    expect(swiftNumber(16)).toBe("16");
    expect(swiftNumber(1.5)).toBe("1.5");
    expect(swiftNumber(0.06 * 11)).toBe("0.66");
    expect(swiftNumber(-0)).toBe("0");
    expect(() => swiftNumber(Number.POSITIVE_INFINITY)).toThrow("cannot emit Infinity");
  });

  it("packs RGBA bytes as a 0xRRGGBBAA literal, keeping CSS alpha order", () => {
    expect(swiftRgbaHex({ r: 0x18, g: 0x18, b: 0x1b, a: 0x66 })).toBe("0x18181B66");
    expect(swiftRgbaHex({ r: 0, g: 0, b: 0, a: 0xa3 })).toBe("0x000000A3");
  });

  it("escapes Swift keywords and prefixes extension names", () => {
    expect(swiftIdentifier("default")).toBe("`default`");
    expect(swiftIdentifier("space0_5")).toBe("space0_5");
    expect(swiftPrefixedName("ts", "accentPrimary")).toBe("tsAccentPrimary");
  });

  it("converts letter spacing from em to points", () => {
    expect(trackingPoints(typography("display", 36, -0.02))).toBe(-0.72);
    expect(trackingPoints(typography("overline", 11, 0.06))).toBe(0.66);
  });

  it("converts milliseconds to TimeInterval seconds", () => {
    expect(msToSeconds(120)).toBe(0.12);
    expect(msToSeconds(700)).toBe(0.7);
  });

  it("flags only the 9999 radius as a capsule", () => {
    expect(isCapsuleRadius(9999)).toBe(true);
    expect(isCapsuleRadius(16)).toBe(false);
  });

  it("decides capsules the same way on iOS and Android", () => {
    const radius: DimensionToken[] = [16, 1000, 9999].map((value) => ({ name: tokenName(`radius-${value}`), value }));
    const swift = swiftRadius(radius);
    const compose = composeShapes({ radius } as TokenModel);
    for (const token of radius) {
      const capsule = token.value === 9999;
      expect(swift).toContain(`value: ${token.name.camel}, isCapsule: ${capsule})`);
      expect(compose).toContain(
        `public val ${token.name.camel}: RoundedCornerShape = ${capsule ? "RoundedCornerShape(percent = 50)" : "RoundedCornerShape("}`,
      );
      expect(compose.includes(`${token.name.camel}: RoundedCornerShape = RoundedCornerShape(percent = 50)`)).toBe(capsule);
    }
  });
});

describe("Dynamic Type base text styles", () => {
  it("maps every DS style to the analysis base text style", async () => {
    const model = await readTsTokens(realTokensPath);
    expect(Object.fromEntries(model.typography.map((token) => [token.name.token, iosBaseTextStyle(token)]))).toEqual({
      display: "largeTitle",
      "title-1": "title1",
      "title-2": "title2",
      "title-3": "title3",
      "body-lg": "body",
      body: "callout",
      "body-sm": "subheadline",
      label: "subheadline",
      caption: "caption1",
      overline: "caption2",
      code: "footnote",
    });
  });

  it("falls back to the nearest default point size for unknown styles", () => {
    expect(iosBaseTextStyle(typography("hero", 40))).toBe("largeTitle");
    expect(iosBaseTextStyle(typography("micro", 10))).toBe("caption2");
    expect(iosBaseTextStyle(typography("lead", 19))).toBe("title3");
  });
});

describe("Swift shadow format", () => {
  it("rejects inset shadows, which CALayer cannot draw", () => {
    const layer = { offsetX: 0, offsetY: 1, blur: 2, spread: 0, color: { hex: "#000000", rgba: { r: 0, g: 0, b: 0, a: 255 } }, inset: true };
    expect(() => swiftShadows([{ name: tokenName("shadow-inner"), value: { light: [layer], dark: [layer] } }])).toThrow(
      "shadow-inner: inset shadows have no iOS layer equivalent",
    );
  });
});

describe("Swift generators", () => {
  it("are registered for tokens and icons in the TSTokens Generated folder", () => {
    expect(GENERATORS).toContain(swiftTokensGenerator);
    expect(GENERATORS).toContain(swiftIconNameGenerator);
    expect([swiftTokensGenerator.target, swiftIconNameGenerator.target]).toEqual(["tokens", "icons"]);
    expect(swiftTokensGenerator.outputDir).toBe(SWIFT_TOKENS_DIR);
  });

  it("emits every token family of the real model", async () => {
    const model = await readNativeModel(repoRoot);
    const files = Object.fromEntries(swiftTokenFiles(model).map((file) => [file.path.split("/").at(-1), file.contents]));
    expect(Object.keys(files).sort()).toEqual([
      "TSColor.swift",
      "TSComponentTokens.swift",
      "TSControllerColor.swift",
      "TSControllerSize.swift",
      "TSDimensionToken.swift",
      "TSMotion.swift",
      "TSOpacity.swift",
      "TSRadius.swift",
      "TSShadowTokens.swift",
      "TSSize.swift",
      "TSSpacing.swift",
      "TSTypographyTokens.swift",
    ]);
    const count = (name: string, pattern: RegExp) => (files[name]?.match(pattern) ?? []).length;
    expect(count("TSColor.swift", /public static let \w+ = TSColor\(/g)).toBe(model.color.length);
    expect(count("TSColor.swift", /public static var ts\w+: UIColor/g)).toBe(model.color.length);
    expect(count("TSSpacing.swift", /public static let \w+: CGFloat/g)).toBe(11);
    expect(count("TSTypographyTokens.swift", /public static let \w+ = TSTypographyToken\(/g)).toBe(11);
    expect(count("TSShadowTokens.swift", /public static let \w+ = TSShadowToken\(/g)).toBe(5);
    expect(count("TSComponentTokens.swift", /public static let /g)).toBe(model.component.length);
    expect(count("TSControllerColor.swift", /public static let \w+ = TSColor\(/g)).toBe(8);
    expect(files["TSControllerColor.swift"]).toContain(
      'public static let backing = TSColor(name: "controller-backing", light: TSRGBA(hex: 0xFCFCFCE6), dark: TSRGBA(hex: 0x000000EC))',
    );
    expect(files["TSControllerSize.swift"]).toContain("public static let outlineWidth: CGFloat = 1.5");
    expect(files["TSColor.swift"]).toContain('public static let focus = TSColor(name: "focus", light: TSRGBA(hex: 0x7C3AEDFF), dark: TSRGBA(hex: 0xA78BFAFF))');
    expect(files["TSComponentTokens.swift"]).toContain("public static let spinnerRotationDuration: TimeInterval = 0.7");
  });
});
