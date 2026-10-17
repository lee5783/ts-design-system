import { execFile } from "node:child_process";
import { mkdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { promisify } from "node:util";

const exec = promisify(execFile);

export const VERSION_FILE = "VERSION";
export const CHANGELOG_FILE = "CHANGELOG.md";
export const CLIFF_CONFIG = "cliff.toml";
export const GIT_CLIFF_NPM_PACKAGE = "git-cliff@2.14.2";
export const RELEASE_REMOTE = "origin";
export const RELEASE_BRANCH = "master";

export const GIT_CLIFF_INSTALL_HINT = [
  "git-cliff is not available, and npx could not run the git-cliff npm package either.",
  "Install it with one of:",
  "  mise install            (mise.toml pins git-cliff 2)",
  "  brew install git-cliff",
  "  cargo install git-cliff",
  "or point GIT_CLIFF at a git-cliff executable.",
].join("\n");

export interface Version {
  major: number;
  minor: number;
  patch: number;
}

export interface Command {
  file: string;
  args: string[];
}

export class ReleaseError extends Error {}

const SEMVER = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$/;

export function parseVersion(text: string): Version {
  const match = SEMVER.exec(text.trim());
  if (!match) throw new ReleaseError(`"${text.trim()}" is not a release version: use a plain X.Y.Z semver such as 1.2.3 (no "v" prefix, no pre-release suffix), because SPM resolves plain tags`);
  return { major: Number(match[1]), minor: Number(match[2]), patch: Number(match[3]) };
}

export function formatVersion(version: Version): string {
  return `${version.major}.${version.minor}.${version.patch}`;
}

export function compareVersions(left: Version, right: Version): number {
  return left.major - right.major || left.minor - right.minor || left.patch - right.patch;
}

export function validateBump(current: string, next: string | undefined, firstRelease: boolean = false): Version {
  if (next === undefined || next.trim() === "") throw new ReleaseError("V is required: make release-bump V=X.Y.Z");
  const from = parseVersion(current);
  const to = parseVersion(next);
  const order = compareVersions(to, from);
  if (order < 0 || (order === 0 && !firstRelease)) throw new ReleaseError(`${formatVersion(to)} is not greater than the current version ${formatVersion(from)}`);
  return to;
}

export function changelogSection(changelog: string, version: string): string | undefined {
  const lines = changelog.split("\n");
  const start = lines.findIndex((line) => line === `## ${version}` || line.startsWith(`## ${version} `));
  if (start < 0) return undefined;
  const length = lines.slice(start + 1).findIndex((line) => line.startsWith("## "));
  const section = lines.slice(start, length < 0 ? undefined : start + 1 + length);
  return `${section.join("\n").trim()}\n`;
}

export function verifyTag(tag: string | undefined, version: string, changelog: string): string {
  if (tag === undefined || tag.trim() === "") throw new ReleaseError("TAG is required: make release-verify TAG=X.Y.Z");
  const parsed = formatVersion(parseVersion(tag));
  if (parsed !== tag.trim()) throw new ReleaseError(`tag ${tag} is not a plain X.Y.Z version`);
  if (parsed !== version.trim()) throw new ReleaseError(`tag ${parsed} does not match VERSION ${version.trim()}: run make release-bump V=${parsed}, merge it, and release from the commit that contains it`);
  if (changelogSection(changelog, parsed) === undefined) throw new ReleaseError(`CHANGELOG.md has no "## ${parsed}" section: run make release-bump V=${parsed}`);
  return parsed;
}

async function runs(command: Command, cwd: string): Promise<boolean> {
  try {
    await exec(command.file, [...command.args, "--version"], { cwd });
    return true;
  } catch {
    return false;
  }
}

export function gitCliffCandidates(env: NodeJS.ProcessEnv): Command[] {
  const override = env.GIT_CLIFF?.trim();
  if (override) return [{ file: override, args: [] }];
  return [
    { file: "git-cliff", args: [] },
    { file: "npx", args: ["--yes", GIT_CLIFF_NPM_PACKAGE] },
  ];
}

export async function resolveGitCliff(root: string, env: NodeJS.ProcessEnv = process.env): Promise<Command> {
  for (const candidate of gitCliffCandidates(env)) {
    if (await runs(candidate, root)) return candidate;
  }
  throw new ReleaseError(GIT_CLIFF_INSTALL_HINT);
}

async function hasRemote(root: string, remote: string): Promise<boolean> {
  try {
    await exec("git", ["remote", "get-url", remote], { cwd: root });
    return true;
  } catch {
    return false;
  }
}

export async function remoteTags(root: string, remote: string = RELEASE_REMOTE): Promise<string[] | undefined> {
  if (!(await hasRemote(root, remote))) return undefined;
  try {
    const { stdout } = await exec("git", ["ls-remote", "--tags", "--refs", remote], { cwd: root });
    return stdout
      .split("\n")
      .map((line) => line.split("\t")[1] ?? "")
      .filter((ref) => ref.startsWith("refs/tags/"))
      .map((ref) => ref.slice("refs/tags/".length));
  } catch (error) {
    throw new ReleaseError(`could not list the tags on ${remote}, so a release tag that exists only there would be missed: ${error instanceof Error ? error.message.split("\n")[0] : String(error)}`);
  }
}

export async function knownTags(root: string): Promise<Set<string>> {
  const { stdout } = await exec("git", ["tag", "--list"], { cwd: root });
  const local = stdout.split("\n").filter((tag) => tag !== "");
  return new Set([...local, ...((await remoteTags(root)) ?? [])]);
}

export async function tagExists(root: string, tag: string): Promise<boolean> {
  return (await knownTags(root)).has(tag);
}

export async function releaseTags(root: string): Promise<string[]> {
  return [...(await knownTags(root))].filter((tag) => SEMVER.test(tag)).sort();
}

export async function verifyReleaseContext(root: string, tag: string, ref: string | undefined): Promise<void> {
  if (ref !== undefined && ref.trim() !== "" && ref.trim() !== `refs/heads/${RELEASE_BRANCH}`) {
    throw new ReleaseError(`releases run only from ${RELEASE_BRANCH}, not ${ref.trim()}: start the release workflow on ${RELEASE_BRANCH}`);
  }
  if (await tagExists(root, tag)) throw new ReleaseError(`tag ${tag} already exists, so ${tag} is already released: bump to a new version`);
  if (!(await hasRemote(root, RELEASE_REMOTE))) return;
  const branch = `refs/remotes/${RELEASE_REMOTE}/${RELEASE_BRANCH}`;
  try {
    await exec("git", ["rev-parse", "-q", "--verify", branch], { cwd: root });
  } catch {
    throw new ReleaseError(`${branch} is not in the clone: fetch it (actions/checkout with fetch-depth: 0) so the release commit can be checked against it`);
  }
  try {
    await exec("git", ["merge-base", "--is-ancestor", "HEAD", branch], { cwd: root });
  } catch {
    throw new ReleaseError(`HEAD is not on ${RELEASE_REMOTE}/${RELEASE_BRANCH}: only merged commits are released`);
  }
}

export async function generateChangelog(root: string, gitCliff: Command, version: string): Promise<string> {
  const { stdout } = await exec(gitCliff.file, [...gitCliff.args, "--config", CLIFF_CONFIG, "--tag", version], { cwd: root, maxBuffer: 64 * 1024 * 1024 });
  return `${stdout.trimEnd()}\n`;
}

export interface BumpResult {
  from: string;
  to: string;
  changelog: string;
  section: string;
  written: boolean;
}

export async function bump(root: string, next: string | undefined, dryRun: boolean, env: NodeJS.ProcessEnv = process.env): Promise<BumpResult> {
  const current = (await readFile(path.join(root, VERSION_FILE), "utf8")).trim();
  const firstRelease = (await releaseTags(root)).length === 0;
  const to = formatVersion(validateBump(current, next, firstRelease));
  if (await tagExists(root, to)) throw new ReleaseError(`tag ${to} already exists`);
  const gitCliff = await resolveGitCliff(root, env);
  const changelog = await generateChangelog(root, gitCliff, to);
  const section = changelogSection(changelog, to);
  if (section === undefined) throw new ReleaseError(`git-cliff produced no "## ${to}" section: there are no commits since the last release tag, or ${CLIFF_CONFIG} skips them all`);
  if (!dryRun) {
    await writeFile(path.join(root, CHANGELOG_FILE), changelog);
    await writeFile(path.join(root, VERSION_FILE), `${to}\n`);
  }
  return { from: current, to, changelog, section, written: !dryRun };
}

function flag(name: string): string | undefined {
  const index = process.argv.indexOf(name);
  return index >= 0 ? process.argv[index + 1] : undefined;
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
  const command = process.argv[2];
  try {
    if (command === "bump") {
      const dryRun = process.argv.includes("--dry-run");
      const result = await bump(root, flag("--version"), dryRun);
      process.stdout.write(result.section);
      const change = result.from === result.to ? `the first release ${result.to}` : `${result.from} -> ${result.to}`;
      console.log(dryRun ? `release-bump: dry run for ${change}; VERSION and CHANGELOG.md left unchanged` : `release-bump: ${change}: VERSION written, CHANGELOG.md regenerated`);
      if (!dryRun) console.log(`Next: review, commit "chore(release): ${result.to}" and merge it, then run the release workflow on ${RELEASE_BRANCH} with version ${result.to}. It creates the tag after every gate and the Android publish pass.`);
    } else if (command === "verify") {
      const version = await readFile(path.join(root, VERSION_FILE), "utf8");
      const changelog = await readFile(path.join(root, CHANGELOG_FILE), "utf8");
      const tag = verifyTag(flag("--tag"), version, changelog);
      await verifyReleaseContext(root, tag, flag("--ref"));
      console.log(`release-verify: ${tag} matches VERSION, has a changelog section, is not tagged yet, and HEAD is on ${RELEASE_BRANCH}`);
    } else if (command === "notes") {
      const tag = formatVersion(parseVersion(flag("--tag") ?? ""));
      const out = flag("--out");
      if (out === undefined) throw new ReleaseError("--out is required");
      const section = changelogSection(await readFile(path.join(root, CHANGELOG_FILE), "utf8"), tag);
      if (section === undefined) throw new ReleaseError(`CHANGELOG.md has no "## ${tag}" section`);
      const target = path.resolve(root, out);
      await mkdir(path.dirname(target), { recursive: true });
      await writeFile(target, section);
      console.log(`release-notes: wrote the ${tag} section to ${path.relative(root, target)}`);
    } else {
      throw new ReleaseError(`unknown command "${command ?? ""}": use bump, verify or notes`);
    }
  } catch (error) {
    console.error(error instanceof ReleaseError ? `release: ${error.message}` : `release: ${error instanceof Error ? error.message : String(error)}`);
    process.exitCode = error instanceof ReleaseError ? 2 : 1;
  }
}
