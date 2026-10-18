import type { GeneratedFile, Generator } from "../../generate.ts";
import type { ColorToken, ThemeId, TokenBase, TokenModel } from "../../model/types.ts";
import { isElevationShadow, kotlinArgb, kotlinFloat, kotlinIdentifier, shadowColor, shadowElevation } from "../../transforms/android.ts";
import { toCamelName } from "../../transforms/naming.ts";
import { argumentList, block, deprecation, kotlinFile } from "./kotlin-file.ts";

export const TOKENS_KOTLIN_DIR = "android/tokens/src/generated/kotlin";

export const TOKENS_PACKAGE = "com.ts.ds.tokens";

const TOKEN_VALUES_PATH = `${TOKENS_KOTLIN_DIR}/com/ts/ds/tokens/TSTokenValues.kt`;

const VALUE_TYPES = [
  argumentList("public class TSTypeValues internal constructor", [
    "public val fontSize: Float",
    "public val lineHeight: Float",
    "public val fontWeight: Int",
    "public val letterSpacingEm: Float",
    "public val monospace: Boolean",
  ]).join("\n"),
  argumentList("public class TSShadowValues internal constructor", [
    "public val elevation: Float",
    "public val lightColor: Long",
    "public val darkColor: Long",
  ]).join("\n"),
  argumentList("public class TSCubicBezierValues internal constructor", [
    "public val x1: Float",
    "public val y1: Float",
    "public val x2: Float",
    "public val y2: Float",
  ]).join("\n"),
];

function name(token: TokenBase): string {
  return kotlinIdentifier(token.name.camel);
}

function replacement(target: string): string {
  return kotlinIdentifier(toCamelName(target));
}

function constant(token: TokenBase, type: string, value: string): string[] {
  const resolved = token.deprecated === undefined ? value : replacement(token.deprecated.renamedTo);
  return [...deprecation(token, replacement), `public const val ${name(token)}: ${type} = ${resolved}`];
}

function value(token: TokenBase, type: string, expression: string): string[] {
  const resolved = token.deprecated === undefined ? expression : replacement(token.deprecated.renamedTo);
  return [...deprecation(token, replacement), `public val ${name(token)}: ${type} = ${resolved}`];
}

function colorConstant(token: ColorToken, theme: ThemeId): string[] {
  const alias = token.deprecated === undefined && token.aliasOf !== undefined ? replacement(token.aliasOf) : undefined;
  return constant(token, "Long", alias ?? kotlinArgb(token.value[theme].rgba));
}

function group(objectName: string, lines: string[]): string[] {
  return block(`public object ${objectName}`, lines);
}

export function tokenValuesKotlin(model: TokenModel): string {
  const typeValues = model.typography.flatMap((token) =>
    value(
      token,
      "TSTypeValues",
      `TSTypeValues(${kotlinFloat(token.fontSize)}, ${kotlinFloat(token.lineHeight)}, ${token.fontWeight}, ${kotlinFloat(token.letterSpacingEm)}, ${token.family === "mono"})`,
    ),
  );
  const shadows = model.shadow
    .filter(isElevationShadow)
    .flatMap((token) =>
      value(
        token,
        "TSShadowValues",
        `TSShadowValues(${kotlinFloat(shadowElevation(token))}, ${kotlinArgb(shadowColor(token.value.light))}, ${kotlinArgb(shadowColor(token.value.dark))})`,
      ),
    );
  const component = model.component.flatMap((token) =>
    token.type === "dimension" ? constant(token, "Float", kotlinFloat(token.value)) : constant(token, "Int", String(Math.round(token.value))),
  );
  const groups = [
    group("LightColors", model.color.flatMap((token) => colorConstant(token, "light"))),
    group("DarkColors", model.color.flatMap((token) => colorConstant(token, "dark"))),
    group("Typography", typeValues),
    group("Spacing", model.spacing.flatMap((token) => constant(token, "Float", kotlinFloat(token.value)))),
    group("Radius", model.radius.flatMap((token) => constant(token, "Float", kotlinFloat(token.value)))),
    group("Size", model.size.flatMap((token) => constant(token, "Float", kotlinFloat(token.value)))),
    group("Opacity", model.opacity.flatMap((token) => constant(token, "Float", kotlinFloat(token.value)))),
    group("Shadow", shadows),
    group("Duration", model.duration.flatMap((token) => constant(token, "Int", String(Math.round(token.value))))),
    group(
      "Easing",
      model.easing.flatMap((token) => value(token, "TSCubicBezierValues", `TSCubicBezierValues(${token.value.map(kotlinFloat).join(", ")})`)),
    ),
    group("Component", component),
    group("LightControllerColors", model.controller.color.flatMap((token) => constant(token, "Long", kotlinArgb(token.value.light.rgba)))),
    group("DarkControllerColors", model.controller.color.flatMap((token) => constant(token, "Long", kotlinArgb(token.value.dark.rgba)))),
    group("ControllerSize", model.controller.size.flatMap((token) => constant(token, "Float", kotlinFloat(token.value)))),
  ];
  const body = groups.flatMap((lines, index) => (index === 0 ? lines : ["", ...lines]));
  return kotlinFile(TOKENS_PACKAGE, [], [...VALUE_TYPES, block("public object TSTokenValues", body).join("\n")]);
}

export const androidTokenValuesGenerator: Generator = {
  name: "android-tokens-kotlin",
  target: "tokens",
  outputDir: TOKENS_KOTLIN_DIR,
  generate: ({ model }): GeneratedFile[] => [{ path: TOKEN_VALUES_PATH, contents: tokenValuesKotlin(model) }],
};
