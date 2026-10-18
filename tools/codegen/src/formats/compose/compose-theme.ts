import type { GeneratedFile, Generator } from "../../generate.ts";
import type { DimensionToken, TokenBase, TokenModel } from "../../model/types.ts";
import { isCapsuleRadius, isElevationShadow, kotlinIdentifier } from "../../transforms/android.ts";
import { toCamelName } from "../../transforms/naming.ts";
import { active, argumentList, block, deprecation, kotlinFile } from "./kotlin-file.ts";
import { TOKENS_PACKAGE } from "./token-values.ts";

export const COMPOSE_KOTLIN_DIR = "android/compose/src/generated/kotlin";

export const COMPOSE_THEME_PACKAGE = "com.tinysol.designsystem.compose.theme";

const THEME_DIR = `${COMPOSE_KOTLIN_DIR}/com/tinysol/designsystem/compose/theme`;

const TOKEN_VALUES = `${TOKENS_PACKAGE}.TSTokenValues`;

function name(token: TokenBase): string {
  return kotlinIdentifier(token.name.camel);
}

function replacement(target: string): string {
  return kotlinIdentifier(toCamelName(target));
}

function deprecatedGetters(tokens: TokenBase[], type: string): string[] {
  return tokens
    .filter((token) => token.deprecated !== undefined)
    .flatMap((token) => [
      ...deprecation(token, replacement),
      `public val ${name(token)}: ${type} get() = ${replacement(token.deprecated?.renamedTo ?? "")}`,
    ]);
}

function themedClass(className: string, type: string, tokens: TokenBase[]): string {
  const header = argumentList(
    "public class " + className + " internal constructor",
    active(tokens).map((token) => `public val ${name(token)}: ${type}`),
  );
  const getters = deprecatedGetters(tokens, type);
  if (getters.length === 0) {
    return ["@Immutable", ...header].join("\n");
  }
  const last = header.length - 1;
  return ["@Immutable", ...header.slice(0, last), ...block(`${header[last]}`, getters)].join("\n");
}

function factory(functionName: string, className: string, tokens: TokenBase[], argument: (token: TokenBase) => string): string {
  return [
    `public fun ${functionName}(): ${className} =`,
    ...argumentList(className, active(tokens).map((token) => `${name(token)} = ${argument(token)}`)).map((line) => `    ${line}`),
  ].join("\n");
}

function constantsObject(objectName: string, tokens: TokenBase[], type: string, expression: (token: TokenBase) => string): string {
  const lines = tokens.flatMap((token) => [
    ...deprecation(token, replacement),
    `public val ${name(token)}: ${type} = ${token.deprecated === undefined ? expression(token) : replacement(token.deprecated.renamedTo)}`,
  ]);
  return block(`public object ${objectName}`, lines).join("\n");
}

export function composeColors(model: TokenModel): string {
  return kotlinFile(
    COMPOSE_THEME_PACKAGE,
    ["androidx.compose.runtime.Immutable", "androidx.compose.ui.graphics.Color", TOKEN_VALUES],
    [
      themedClass("TSColors", "Color", model.color),
      factory("lightTSColors", "TSColors", model.color, (token) => `Color(TSTokenValues.LightColors.${name(token)})`),
      factory("darkTSColors", "TSColors", model.color, (token) => `Color(TSTokenValues.DarkColors.${name(token)})`),
    ],
  );
}

export function composeControllerColors(model: TokenModel): string {
  const tokens = model.controller.color;
  return kotlinFile(
    COMPOSE_THEME_PACKAGE,
    ["androidx.compose.runtime.Immutable", "androidx.compose.ui.graphics.Color", TOKEN_VALUES],
    [
      themedClass("TSControllerColors", "Color", tokens),
      factory("lightTSControllerColors", "TSControllerColors", tokens, (token) => `Color(TSTokenValues.LightControllerColors.${name(token)})`),
      factory("darkTSControllerColors", "TSControllerColors", tokens, (token) => `Color(TSTokenValues.DarkControllerColors.${name(token)})`),
    ],
  );
}

export function composeTypography(model: TokenModel): string {
  const textStyle = [
    "private fun tsTextStyle(values: TSTypeValues, fontWeight: (Int) -> FontWeight): TextStyle =",
    ...argumentList("TextStyle", [
      "fontFamily = if (values.monospace) FontFamily.Monospace else FontFamily.Default",
      "fontSize = values.fontSize.sp",
      "lineHeight = values.lineHeight.sp",
      "fontWeight = fontWeight(values.fontWeight)",
      "letterSpacing = values.letterSpacingEm.em",
      "lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)",
    ]).map((line) => `    ${line}`),
  ].join("\n");
  const typography = [
    "internal fun tsTypography(fontWeight: (Int) -> FontWeight = ::FontWeight): TSTypography =",
    ...argumentList(
      "TSTypography",
      active(model.typography).map((token) => `${name(token)} = tsTextStyle(TSTokenValues.Typography.${name(token)}, fontWeight)`),
    ).map((line) => `    ${line}`),
  ].join("\n");
  return kotlinFile(
    COMPOSE_THEME_PACKAGE,
    [
      "androidx.compose.runtime.Immutable",
      "androidx.compose.ui.text.TextStyle",
      "androidx.compose.ui.text.font.FontFamily",
      "androidx.compose.ui.text.font.FontWeight",
      "androidx.compose.ui.text.style.LineHeightStyle",
      "androidx.compose.ui.unit.em",
      "androidx.compose.ui.unit.sp",
      `${TOKENS_PACKAGE}.TSTypeValues`,
      TOKEN_VALUES,
    ],
    [themedClass("TSTypography", "TextStyle", model.typography), typography, textStyle],
  );
}

function dpObject(objectName: string, family: string, tokens: DimensionToken[]): string {
  return kotlinFile(
    COMPOSE_THEME_PACKAGE,
    ["androidx.compose.ui.unit.Dp", "androidx.compose.ui.unit.dp", TOKEN_VALUES],
    [constantsObject(objectName, tokens, "Dp", (token) => `TSTokenValues.${family}.${name(token)}.dp`)],
  );
}

export function composeShapes(model: TokenModel): string {
  return kotlinFile(
    COMPOSE_THEME_PACKAGE,
    ["androidx.compose.foundation.shape.RoundedCornerShape", "androidx.compose.ui.unit.dp", TOKEN_VALUES],
    [
      constantsObject("TSShapes", model.radius, "RoundedCornerShape", (token) =>
        isCapsuleRadius((token as DimensionToken).value)
          ? "RoundedCornerShape(percent = 50)"
          : `RoundedCornerShape(TSTokenValues.Radius.${name(token)}.dp)`,
      ),
    ],
  );
}

export function composeElevation(model: TokenModel): string {
  const shadows = model.shadow.filter(isElevationShadow);
  const shadowClass = ["@Immutable", ...argumentList("public class TSShadow internal constructor", ["public val elevation: Dp", "public val color: Color"])].join(
    "\n",
  );
  const themed = (functionName: string, colorProperty: string) =>
    factory(functionName, "TSElevation", shadows, (token) => {
      const values = `TSTokenValues.Shadow.${name(token)}`;
      return `TSShadow(${values}.elevation.dp, Color(${values}.${colorProperty}))`;
    });
  return kotlinFile(
    COMPOSE_THEME_PACKAGE,
    ["androidx.compose.runtime.Immutable", "androidx.compose.ui.graphics.Color", "androidx.compose.ui.unit.Dp", "androidx.compose.ui.unit.dp", TOKEN_VALUES],
    [shadowClass, themedClass("TSElevation", "TSShadow", shadows), themed("lightTSElevation", "lightColor"), themed("darkTSElevation", "darkColor")],
  );
}

export function composeMotion(model: TokenModel): string {
  const durations = model.duration.flatMap((token) => [
    ...deprecation(token, replacement),
    `public const val ${name(token)}: Int = ${token.deprecated === undefined ? `TSTokenValues.Duration.${name(token)}` : replacement(token.deprecated.renamedTo)}`,
  ]);
  const easings = model.easing.flatMap((token) => {
    const values = `TSTokenValues.Easing.${name(token)}`;
    return [
      ...deprecation(token, replacement),
      `public val ${name(token)}: Easing = ${
        token.deprecated === undefined ? `CubicBezierEasing(${values}.x1, ${values}.y1, ${values}.x2, ${values}.y2)` : replacement(token.deprecated.renamedTo)
      }`,
    ];
  });
  return kotlinFile(
    COMPOSE_THEME_PACKAGE,
    ["androidx.compose.animation.core.CubicBezierEasing", "androidx.compose.animation.core.Easing", TOKEN_VALUES],
    [block("public object TSMotion", [...durations, ...easings]).join("\n")],
  );
}

export function composeTheme(model: TokenModel): GeneratedFile[] {
  return [
    { path: `${THEME_DIR}/TSColors.kt`, contents: composeColors(model) },
    { path: `${THEME_DIR}/TSTypography.kt`, contents: composeTypography(model) },
    { path: `${THEME_DIR}/TSSpacing.kt`, contents: dpObject("TSSpacing", "Spacing", model.spacing) },
    { path: `${THEME_DIR}/TSSize.kt`, contents: dpObject("TSSize", "Size", model.size) },
    { path: `${THEME_DIR}/TSShapes.kt`, contents: composeShapes(model) },
    { path: `${THEME_DIR}/TSElevation.kt`, contents: composeElevation(model) },
    { path: `${THEME_DIR}/TSMotion.kt`, contents: composeMotion(model) },
    { path: `${THEME_DIR}/TSControllerColors.kt`, contents: composeControllerColors(model) },
    { path: `${THEME_DIR}/TSControllerSize.kt`, contents: dpObject("TSControllerSize", "ControllerSize", model.controller.size) },
  ];
}

export const composeThemeGenerator: Generator = {
  name: "compose-theme",
  target: "tokens",
  outputDir: COMPOSE_KOTLIN_DIR,
  generate: ({ model }) => composeTheme(model),
};
