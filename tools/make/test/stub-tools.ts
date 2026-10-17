import { execFile } from "node:child_process";
import { chmod, mkdtemp, readFile, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";

export const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const run = promisify(execFile);

const ARGUMENT_SEPARATOR = "\u001f";
const CALL_SEPARATOR = "\u001e";
const STUB = `#!/bin/bash\n{ printf '%s' "$(basename "$0")"; printf '\\037%s' "$@"; printf '\\036'; } >> "$STUB_LOG"\n`;

export interface StubRun {
  code: number;
  output: string;
  calls: string[][];
}

export interface StubTools {
  directory: string;
  run(target: string, options?: { env?: Record<string, string>; args?: string[]; cwd?: string }): Promise<StubRun>;
  dispose(): Promise<void>;
}

export async function stubTools(names: readonly string[] = ["npm", "gh"]): Promise<StubTools> {
  const directory = await mkdtemp(path.join(tmpdir(), "make-stubs-"));
  const bin = path.join(directory, "bin");
  const log = path.join(directory, "calls.log");
  await run("mkdir", ["-p", bin]);
  for (const name of names) {
    await writeFile(path.join(bin, name), STUB);
    await chmod(path.join(bin, name), 0o755);
  }
  return {
    directory,
    async run(target, options = {}) {
      await writeFile(log, "");
      const env = { ...process.env, GITHUB_ACTIONS: "", ...options.env, PATH: `${bin}:${process.env.PATH ?? ""}`, STUB_LOG: log };
      let code = 0;
      let output = "";
      try {
        const result = await run("make", ["-s", target, ...(options.args ?? [])], { cwd: options.cwd ?? repoRoot, env });
        output = result.stdout + result.stderr;
      } catch (error) {
        const failure = error as { code: number; stdout: string; stderr: string };
        code = failure.code;
        output = failure.stdout + failure.stderr;
      }
      const calls = (await readFile(log, "utf8"))
        .split(CALL_SEPARATOR)
        .filter((entry) => entry !== "")
        .map((entry) => entry.split(ARGUMENT_SEPARATOR));
      return { code, output, calls };
    },
    async dispose() {
      await rm(directory, { recursive: true, force: true });
    },
  };
}

export function callsOf(result: StubRun, tool: string, script?: string): string[][] {
  return result.calls.filter((call) => call[0] === tool && (script === undefined || call.includes(script)));
}
