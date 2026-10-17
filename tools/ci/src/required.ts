import path from "node:path";
import { pathToFileURL } from "node:url";

export const CHANGES_JOB = "changes";

export interface NeededJob {
  result: string;
}

export interface Verdict {
  ok: boolean;
  lines: string[];
}

export function parseNeeds(json: string | undefined): Record<string, NeededJob> {
  if (json === undefined || json.trim() === "") throw new Error("NEEDS is empty: pass ${{ toJSON(needs) }}");
  const parsed = JSON.parse(json) as unknown;
  if (parsed === null || typeof parsed !== "object" || Array.isArray(parsed)) throw new Error("NEEDS is not a JSON object of jobs");
  const needs: Record<string, NeededJob> = {};
  for (const [name, entry] of Object.entries(parsed as Record<string, unknown>)) {
    const result = (entry as { result?: unknown } | null)?.result;
    if (typeof result !== "string") throw new Error(`NEEDS.${name} has no result`);
    needs[name] = { result };
  }
  if (Object.keys(needs).length === 0) throw new Error("NEEDS lists no jobs");
  return needs;
}

export function evaluateNeeds(needs: Record<string, NeededJob>): Verdict {
  const changes = needs[CHANGES_JOB];
  const skipsAllowed = changes?.result === "success";
  const lines: string[] = [];
  let ok = true;
  for (const [name, { result }] of Object.entries(needs).sort(([left], [right]) => left.localeCompare(right))) {
    if (result === "success") {
      lines.push(`  ${name}: success`);
    } else if (result === "skipped" && skipsAllowed && name !== CHANGES_JOB) {
      lines.push(`  ${name}: skipped (no change in its area)`);
    } else {
      ok = false;
      const reason = result === "skipped" ? (changes === undefined ? "skipped, and this workflow has no changes job to justify a skip" : `skipped because changes did not succeed (${changes.result})`) : result;
      lines.push(`  ${name}: ${reason}`);
    }
  }
  return { ok, lines };
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  try {
    const verdict = evaluateNeeds(parseNeeds(process.env.NEEDS));
    console.log(verdict.ok ? "ci-required: every needed job passed or was skipped by the path filter" : "ci-required: a needed job failed, was cancelled, or was skipped without a successful changes job");
    for (const line of verdict.lines) console.log(line);
    process.exitCode = verdict.ok ? 0 : 1;
  } catch (error) {
    console.error(`ci-required: ${error instanceof Error ? error.message : String(error)}`);
    process.exitCode = 2;
  }
}
