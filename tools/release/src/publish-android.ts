import { spawn } from "node:child_process";
import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

export const GROUP = "com.ts.ds";
export const LIBRARY_MODULES = ["tokens", "core", "compose", "views"] as const;
export const BOM_MODULE = "bom";
export const PUBLISH_TASK = "publishAllPublicationsToGitHubPackagesRepository";
export const REGISTRY = "https://maven.pkg.github.com";

export class PublishError extends Error {}

export interface Credentials {
  repository: string;
  actor: string;
  token: string;
}

export type Probe = (url: string, authorization: string) => Promise<number>;

export interface PublishPlan {
  published: string[];
  steps: string[][];
}

export function credentialsFrom(env: NodeJS.ProcessEnv): Credentials {
  const repository = env.GITHUB_REPOSITORY?.trim() ?? "";
  const actor = env.GITHUB_ACTOR?.trim() ?? "";
  const token = env.GITHUB_TOKEN?.trim() ?? "";
  if (repository === "" || actor === "" || token === "") throw new PublishError("GITHUB_REPOSITORY, GITHUB_ACTOR and GITHUB_TOKEN must be set; it runs from release.yml");
  return { repository, actor, token };
}

export function pomUrl(repository: string, module: string, version: string): string {
  return `${REGISTRY}/${repository}/${GROUP.replace(/\./g, "/")}/${module}/${version}/${module}-${version}.pom`;
}

export function basicAuthorization(credentials: Credentials): string {
  return `Basic ${Buffer.from(`${credentials.actor}:${credentials.token}`).toString("base64")}`;
}

export async function isPublished(probe: Probe, credentials: Credentials, module: string, version: string): Promise<boolean> {
  const url = pomUrl(credentials.repository, module, version);
  const status = await probe(url, basicAuthorization(credentials));
  if (status === 200) return true;
  if (status === 404 || status === 410) return false;
  throw new PublishError(`GET ${url} returned HTTP ${status}; expected 200 (published) or 404 (not yet): check the token's read:packages access`);
}

export function gradleTask(module: string): string {
  return `:${module}:${PUBLISH_TASK}`;
}

export async function planPublish(probe: Probe, credentials: Credentials, version: string, workers?: string): Promise<PublishPlan> {
  const flags = workers !== undefined && workers.trim() !== "" ? [`-Dorg.gradle.workers.max=${workers.trim()}`] : [];
  const published: string[] = [];
  const missing: string[] = [];
  for (const module of [...LIBRARY_MODULES, BOM_MODULE]) {
    if (await isPublished(probe, credentials, module, version)) published.push(module);
    else missing.push(module);
  }
  const libraries = missing.filter((module) => module !== BOM_MODULE);
  const steps = [libraries.length > 0 ? [...libraries.map(gradleTask), ...flags] : [], missing.includes(BOM_MODULE) ? [gradleTask(BOM_MODULE), ...flags] : []].filter((step) => step.length > 0);
  return { published, steps };
}

export const fetchProbe: Probe = async (url, authorization) => {
  const response = await fetch(url, { method: "GET", headers: { Authorization: authorization }, redirect: "follow" });
  await response.body?.cancel();
  return response.status;
};

async function gradle(androidDir: string, args: string[]): Promise<void> {
  const code = await new Promise<number>((resolve, reject) => {
    const child = spawn("./gradlew", args, { cwd: androidDir, stdio: "inherit" });
    child.on("error", reject);
    child.on("close", (status) => resolve(status ?? 1));
  });
  if (code !== 0) throw new PublishError(`./gradlew ${args.join(" ")} failed with exit ${code}; re-run the job: modules that already published are skipped`);
}

function flag(name: string): string | undefined {
  const index = process.argv.indexOf(name);
  return index >= 0 ? process.argv[index + 1] : undefined;
}

const isEntryPoint = process.argv[1] !== undefined && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href;

if (isEntryPoint) {
  const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
  try {
    const credentials = credentialsFrom(process.env);
    const version = (await readFile(path.join(root, "VERSION"), "utf8")).trim();
    const plan = await planPublish(fetchProbe, credentials, version, flag("--workers"));
    for (const module of plan.published) console.log(`android-publish: ${GROUP}:${module}:${version} is already in GitHub Packages; skipping it`);
    if (plan.steps.length === 0) console.log(`android-publish: every module of ${version} is already published; nothing to do`);
    for (const step of plan.steps) {
      console.log(`android-publish: ./gradlew ${step.join(" ")}`);
      await gradle(path.join(root, "android"), step);
    }
  } catch (error) {
    console.error(`android-publish: ${error instanceof Error ? error.message : String(error)}`);
    process.exitCode = error instanceof PublishError ? 2 : 1;
  }
}
