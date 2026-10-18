import StyleDictionary from "style-dictionary";
import type { DesignToken, DesignTokens, PlatformConfig } from "style-dictionary/types";
import type { AnyToken, ComponentToken, ShadowLayer, ThemeId, TokenModel } from "./types.ts";

const COMPONENT_TYPES: Record<ComponentToken["type"], string> = {
  dimension: "dimension",
  "font-weight": "fontWeight",
  duration: "duration",
  number: "number",
};

export const EXTENSION_KEY = "com.tinysol.designsystem";

function designToken(token: AnyToken, type: string, value: unknown, extra: Record<string, unknown> = {}): DesignToken {
  return {
    $type: type,
    $value: value,
    ...(token.usage === undefined ? {} : { $description: token.usage }),
    $extensions: {
      [EXTENSION_KEY]: {
        camel: token.name.camel,
        snake: token.name.snake,
        ...(token.extension === undefined ? {} : { upstream: token.extension.upstream }),
        ...(token.deprecated === undefined ? {} : { renamedTo: token.deprecated.renamedTo }),
        ...extra,
      },
    },
  };
}

function group<T extends AnyToken>(tokens: T[], build: (token: T) => DesignToken): DesignTokens {
  return Object.fromEntries(tokens.map((token) => [token.name.token, build(token)]));
}

function shadowValue(layers: ShadowLayer[]): Record<string, unknown>[] {
  return layers.map((layer) => ({
    offsetX: layer.offsetX,
    offsetY: layer.offsetY,
    blur: layer.blur,
    spread: layer.spread,
    color: layer.color.hex,
    inset: layer.inset,
  }));
}

export function toDesignTokens(model: TokenModel, theme: ThemeId): DesignTokens {
  return {
    color: group(model.color, (color) =>
      color.aliasOf === undefined
        ? designToken(color, "color", color.value[theme].hex)
        : designToken(color, "color", `{color.${color.aliasOf}}`, { aliasOf: color.aliasOf }),
    ),
    typography: group(model.typography, (style) =>
      designToken(
        style,
        "typography",
        {
          fontFamily: style.family,
          fontSize: style.fontSize,
          lineHeight: style.lineHeight,
          fontWeight: style.fontWeight,
          letterSpacing: style.letterSpacingEm,
        },
        { group: style.group },
      ),
    ),
    spacing: group(model.spacing, (token) => designToken(token, "dimension", token.value)),
    radius: group(model.radius, (token) => designToken(token, "dimension", token.value)),
    shadow: group(model.shadow, (token) => designToken(token, "shadow", shadowValue(token.value[theme]))),
    size: group(model.size, (token) => designToken(token, "dimension", token.value)),
    opacity: group(model.opacity, (token) => designToken(token, "number", token.value)),
    duration: group(model.duration, (token) => designToken(token, "duration", `${token.value}ms`)),
    easing: group(model.easing, (token) => designToken(token, "cubicBezier", token.value)),
    component: group(model.component, (token) =>
      designToken(token, COMPONENT_TYPES[token.type], token.type === "duration" ? `${token.value}ms` : token.value, {
        component: token.component,
      }),
    ),
  };
}

export async function createStyleDictionary(
  model: TokenModel,
  theme: ThemeId,
  platforms: Record<string, PlatformConfig> = {},
): Promise<StyleDictionary> {
  const dictionary = new StyleDictionary({
    tokens: toDesignTokens(model, theme),
    platforms,
    log: { verbosity: "silent" },
  });
  await dictionary.hasInitialized;
  return dictionary;
}
