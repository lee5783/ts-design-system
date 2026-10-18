import type { GeneratedFile, Generator } from "../../generate.ts";
import type { ColorToken, ThemeId, TokenBase, TokenModel } from "../../model/types.ts";
import { androidArgb, androidResourceName, componentDimensionUnit, formatNumber } from "../../transforms/android.ts";
import { toSnakeName } from "../../transforms/naming.ts";
import { element, resourcesFile } from "./xml-file.ts";

export const TOKENS_RES_DIR = "android/tokens/src/generated/res";

function colorByToken(model: TokenModel): Map<string, ColorToken> {
  return new Map(model.color.map((token) => [token.name.token, token]));
}

function colorReference(model: TokenModel, token: ColorToken): string | undefined {
  const target = token.deprecated?.renamedTo ?? token.aliasOf;
  if (target === undefined) {
    return undefined;
  }
  const resolved = colorByToken(model).get(target);
  if (resolved === undefined) {
    throw new Error(`color ${token.name.token} refers to unknown color ${target}`);
  }
  return `@color/${androidResourceName(resolved.name.snake)}`;
}

export function androidColors(model: TokenModel, theme: ThemeId): string {
  const entries = model.color.flatMap((token) => {
    const reference = colorReference(model, token);
    if (reference !== undefined) {
      return theme === "light" ? [element("color", androidResourceName(token.name.snake), reference)] : [];
    }
    return [element("color", androidResourceName(token.name.snake), androidArgb(token.value[theme].rgba))];
  });
  return resourcesFile(entries);
}

export function androidControllerColors(model: TokenModel, theme: ThemeId): string {
  return resourcesFile(model.controller.color.map((token) => element("color", androidResourceName(token.name.snake), androidArgb(token.value[theme].rgba))));
}

export function androidControllerDimens(model: TokenModel): string {
  return resourcesFile(
    model.controller.size.map((token) => element("dimen", androidResourceName(token.name.snake), `${formatNumber(token.value)}dp`)),
  );
}

function valueOrReference(token: TokenBase, type: string, value: string): string {
  return token.deprecated === undefined ? value : `@${type}/${androidResourceName(toSnakeName(token.deprecated.renamedTo))}`;
}

export function androidDimens(model: TokenModel): string {
  const dp = (token: TokenBase, value: number, unit: "dp" | "sp" = "dp") =>
    element("dimen", androidResourceName(token.name.snake), valueOrReference(token, "dimen", `${formatNumber(value)}${unit}`));
  const entries = [
    ...model.spacing.map((token) => dp(token, token.value)),
    ...model.radius.map((token) => dp(token, token.value)),
    ...model.size.map((token) => dp(token, token.value)),
    ...model.component
      .filter((token) => token.type === "dimension")
      .map((token) => dp(token, token.value, componentDimensionUnit(token))),
    ...model.opacity.map((token) =>
      element("item", androidResourceName(token.name.snake), valueOrReference(token, "dimen", formatNumber(token.value)), {
        format: "float",
        type: "dimen",
      }),
    ),
  ];
  return resourcesFile(entries);
}

export function androidIntegers(model: TokenModel): string {
  const entries = [
    ...model.duration.map((token) =>
      element("integer", androidResourceName(token.name.snake), valueOrReference(token, "integer", formatNumber(token.value))),
    ),
    ...model.component
      .filter((token) => token.type !== "dimension")
      .map((token) =>
        element("integer", androidResourceName(token.name.snake), valueOrReference(token, "integer", formatNumber(Math.round(token.value)))),
      ),
  ];
  return resourcesFile(entries);
}

export function androidTokenResources(model: TokenModel): GeneratedFile[] {
  return [
    { path: `${TOKENS_RES_DIR}/values/ts_colors.xml`, contents: androidColors(model, "light") },
    { path: `${TOKENS_RES_DIR}/values-night/ts_colors.xml`, contents: androidColors(model, "dark") },
    { path: `${TOKENS_RES_DIR}/values/ts_dimens.xml`, contents: androidDimens(model) },
    { path: `${TOKENS_RES_DIR}/values/ts_integers.xml`, contents: androidIntegers(model) },
    { path: `${TOKENS_RES_DIR}/values/ts_controller_colors.xml`, contents: androidControllerColors(model, "light") },
    { path: `${TOKENS_RES_DIR}/values-night/ts_controller_colors.xml`, contents: androidControllerColors(model, "dark") },
    { path: `${TOKENS_RES_DIR}/values/ts_controller_dimens.xml`, contents: androidControllerDimens(model) },
  ];
}

export const androidTokenResourcesGenerator: Generator = {
  name: "android-tokens-res",
  target: "tokens",
  outputDir: TOKENS_RES_DIR,
  generate: ({ model }) => androidTokenResources(model),
};
