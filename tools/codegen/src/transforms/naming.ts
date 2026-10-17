import type { TokenName } from "../model/types.ts";

const SEGMENT = /^[a-z0-9]+$/;
const DIGIT = /^[0-9]/;

export function tokenSegments(token: string): string[] {
  const segments = token.split("-");
  if (segments.some((segment) => !SEGMENT.test(segment))) {
    throw new Error(`invalid token name "${token}": use lowercase kebab-case segments`);
  }
  return segments;
}

export function toCamelName(token: string): string {
  const segments = tokenSegments(token);
  let result = "";
  segments.forEach((segment, index) => {
    const previous = segments[index - 1];
    if (previous === undefined) {
      result = segment;
    } else if (DIGIT.test(previous) && DIGIT.test(segment)) {
      result += `_${segment}`;
    } else {
      result += segment.charAt(0).toUpperCase() + segment.slice(1);
    }
  });
  if (DIGIT.test(result)) {
    throw new Error(`invalid token name "${token}": must not start with a digit`);
  }
  return result;
}

export function toSnakeName(token: string): string {
  return tokenSegments(token).join("_");
}

export function tokenName(token: string): TokenName {
  return { token, camel: toCamelName(token), snake: toSnakeName(token) };
}
