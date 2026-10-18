export const THEMES = ["light", "dark"] as const;

export type ThemeId = (typeof THEMES)[number];

export type Themed<T> = Record<ThemeId, T>;

export interface TokenName {
  token: string;
  camel: string;
  snake: string;
}

export interface Rgba {
  r: number;
  g: number;
  b: number;
  a: number;
}

export const UPSTREAM_STATUSES = ["pending", "accepted", "rejected"] as const;

export type UpstreamStatus = (typeof UPSTREAM_STATUSES)[number];

export interface ExtensionInfo {
  upstream: UpstreamStatus;
  origin?: string;
}

export interface Deprecation {
  renamedTo: string;
}

export interface TokenBase {
  name: TokenName;
  usage?: string;
  extension?: ExtensionInfo;
  deprecated?: Deprecation;
}

export interface ColorValue {
  hex: string;
  rgba: Rgba;
}

export interface ColorToken extends TokenBase {
  value: Themed<ColorValue>;
  aliasOf?: string;
}

export type FontFamily = "sans" | "mono";

export interface TypographyToken extends TokenBase {
  group: string;
  family: FontFamily;
  fontSize: number;
  lineHeight: number;
  fontWeight: number;
  letterSpacingEm: number;
  sample?: string;
}

export interface DimensionToken extends TokenBase {
  value: number;
}

export interface ShadowLayer {
  offsetX: number;
  offsetY: number;
  blur: number;
  spread: number;
  color: ColorValue;
  inset: boolean;
}

export interface ShadowToken extends TokenBase {
  value: Themed<ShadowLayer[]>;
}

export interface OpacityToken extends TokenBase {
  value: number;
}

export interface DurationToken extends TokenBase {
  value: number;
}

export type CubicBezier = [number, number, number, number];

export interface EasingToken extends TokenBase {
  value: CubicBezier;
}

export const COMPONENT_CONSTANT_TYPES = ["dimension", "font-weight", "duration", "number"] as const;

export type ComponentConstantType = (typeof COMPONENT_CONSTANT_TYPES)[number];

export interface ComponentToken extends TokenBase {
  component: string;
  type: ComponentConstantType;
  value: number;
}

export interface ControllerTokens {
  color: ColorToken[];
  size: DimensionToken[];
}

export interface DroppedFamily {
  family: string;
  count: number;
  reason: string;
}

export interface TokenModel {
  source: { name: string; version: number };
  themes: readonly ThemeId[];
  color: ColorToken[];
  typography: TypographyToken[];
  spacing: DimensionToken[];
  radius: DimensionToken[];
  shadow: ShadowToken[];
  size: DimensionToken[];
  opacity: OpacityToken[];
  duration: DurationToken[];
  easing: EasingToken[];
  component: ComponentToken[];
  controller: ControllerTokens;
  dropped: DroppedFamily[];
  extensions?: { name: string; version: number };
}

export const TOKEN_FAMILIES = [
  "color",
  "typography",
  "spacing",
  "radius",
  "shadow",
  "size",
  "opacity",
  "duration",
  "easing",
  "component",
] as const;

export type TokenFamily = (typeof TOKEN_FAMILIES)[number];

export type AnyToken =
  | ColorToken
  | TypographyToken
  | DimensionToken
  | ShadowToken
  | OpacityToken
  | DurationToken
  | EasingToken
  | ComponentToken;
