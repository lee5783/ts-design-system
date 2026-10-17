import { describe, expect, it } from "vitest";
import { diffModels, formatDiffReport } from "../../src/model/diff-report.ts";
import { loadTsTokens } from "../../src/model/load-ts-tokens.ts";
import { rawFixture, rawRealTokens } from "../support.ts";

const base = () => loadTsTokens(rawFixture());

describe("diff-report", () => {
  it("reports no bump for identical sources", () => {
    expect(diffModels(base(), base())).toEqual({ bump: "none", changes: [] });
    const real = loadTsTokens(rawRealTokens());
    expect(diffModels(real, loadTsTokens(rawRealTokens())).bump).toBe("none");
  });

  it("classifies an added token as minor", () => {
    const raw = rawFixture();
    raw.spacing.tokens.push({ name: "space-20", value: "80px" });
    const report = diffModels(base(), loadTsTokens(raw));
    expect(report.bump).toBe("minor");
    expect(report.changes).toEqual([{ family: "spacing", kind: "added", name: "space-20", bump: "minor", after: "80" }]);
  });

  it("classifies a value change as minor", () => {
    const raw = rawFixture();
    raw.color.tokens[1].value.light = "#6d28d9";
    const report = diffModels(base(), loadTsTokens(raw));
    expect(report.bump).toBe("minor");
    expect(report.changes.map((change) => [change.family, change.kind, change.name])).toEqual([
      ["color", "changed", "accent-text"],
      ["color", "changed", "focus"],
    ]);
    expect(report.changes[0]?.before).toBe("light #7c3aed dark #a78bfa");
    expect(report.changes[0]?.after).toBe("light #6d28d9 dark #a78bfa");
  });

  it("classifies a removed token as major", () => {
    const raw = rawFixture();
    raw.radius.tokens = raw.radius.tokens.filter((token: { name: string }) => token.name !== "radius-full");
    const report = diffModels(base(), loadTsTokens(raw));
    expect(report.bump).toBe("major");
    expect(report.changes).toEqual([{ family: "radius", kind: "removed", name: "radius-full", bump: "major", before: "9999" }]);
  });

  it("classifies a rename as major without an alias and minor with a renames alias", () => {
    const raw = rawFixture();
    raw.size.tokens[0].name = "control-default";
    const head = loadTsTokens(raw);
    const withoutAlias = diffModels(base(), head);
    expect(withoutAlias.bump).toBe("major");
    expect(withoutAlias.changes.map((change) => change.kind)).toEqual(["removed", "added"]);
    const withAlias = diffModels(base(), head, { "control-md": "control-default" });
    expect(withAlias.bump).toBe("minor");
    expect(withAlias.changes).toEqual([
      { family: "size", kind: "renamed", name: "control-md", renamedTo: "control-default", bump: "minor", before: "44", after: "44" },
      { family: "size", kind: "added", name: "control-default", bump: "minor", after: "44" },
    ]);
  });

  it("keeps a rename whose target does not exist as a major removal", () => {
    const raw = rawFixture();
    raw.size.tokens = [];
    expect(diffModels(base(), loadTsTokens(raw), { "control-md": "control-default" }).bump).toBe("major");
  });

  it("classifies a usage-only change as patch and ignores dropped families", () => {
    const raw = rawFixture();
    raw.color.tokens[0].usage = "Primary text.";
    raw.zIndex.tokens = [];
    raw.breakpoint.tokens[0].value = "800px";
    const report = diffModels(base(), loadTsTokens(raw));
    expect(report.bump).toBe("patch");
    expect(report.changes).toEqual([{ family: "color", kind: "documented", name: "text-primary", bump: "patch" }]);
  });

  it("detects shadow and typography value changes", () => {
    const raw = rawFixture();
    raw.shadow.tokens[0].value.dark = "0 4px 12px -1px #0000008c";
    raw.type.groups[0].styles[1].fontWeight = 500;
    const report = diffModels(base(), loadTsTokens(raw));
    expect(report.changes.map((change) => `${change.family}/${change.name}`)).toEqual(["typography/body", "shadow/shadow-md"]);
    expect(report.changes[0]?.after).toBe("16/24 500 0em sans");
  });

  it("takes the highest bump across changes and formats a readable report", () => {
    const raw = rawFixture();
    raw.spacing.tokens.push({ name: "space-20", value: "80px" });
    raw.opacity.tokens = [];
    const report = diffModels(base(), loadTsTokens(raw));
    expect(report.bump).toBe("major");
    expect(formatDiffReport(report)).toBe(
      [
        "diff-report: suggested bump major (2 token changes)",
        "  minor  added       spacing/space-20: 80",
        "  major  removed     opacity/opacity-disabled: was 0.45",
      ].join("\n"),
    );
  });
});
