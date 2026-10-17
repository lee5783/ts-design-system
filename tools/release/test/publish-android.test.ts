import { readFile } from "node:fs/promises";
import { createServer, type Server } from "node:http";
import type { AddressInfo } from "node:net";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { afterEach, describe, expect, it } from "vitest";
import { BOM_MODULE, LIBRARY_MODULES, PublishError, basicAuthorization, credentialsFrom, fetchProbe, isPublished, planPublish, pomUrl, type Credentials, type Probe } from "../src/publish-android.ts";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const credentials: Credentials = { repository: "owner/ts-design-system", actor: "bot", token: "secret" };

function registry(published: string[], status: Record<string, number> = {}): { probe: Probe; asked: string[] } {
  const asked: string[] = [];
  const probe: Probe = async (url, authorization) => {
    expect(authorization).toBe(basicAuthorization(credentials));
    const module = url.split("/").at(-3) ?? "";
    asked.push(module);
    return status[module] ?? (published.includes(module) ? 200 : 404);
  };
  return { probe, asked };
}

describe("pomUrl", () => {
  it("points at the module's POM in the repository's GitHub Packages Maven registry", () => {
    expect(pomUrl("owner/ts-design-system", "compose", "0.2.0")).toBe("https://maven.pkg.github.com/owner/ts-design-system/com/ts/ds/compose/0.2.0/compose-0.2.0.pom");
  });
});

describe("planPublish", () => {
  it("publishes every library in one Gradle run, then the BOM on its own, on a first attempt", async () => {
    const { probe } = registry([]);
    expect(await planPublish(probe, credentials, "0.2.0", "2")).toEqual({
      published: [],
      steps: [
        [":tokens:publishAllPublicationsToGitHubPackagesRepository", ":core:publishAllPublicationsToGitHubPackagesRepository", ":compose:publishAllPublicationsToGitHubPackagesRepository", ":views:publishAllPublicationsToGitHubPackagesRepository", "-Dorg.gradle.workers.max=2"],
        [":bom:publishAllPublicationsToGitHubPackagesRepository", "-Dorg.gradle.workers.max=2"],
      ],
    });
  });

  it("skips the modules a failed earlier attempt already published, so a re-run does not hit 409", async () => {
    const { probe } = registry(["tokens", "core"]);
    const plan = await planPublish(probe, credentials, "0.2.0");
    expect(plan.published).toEqual(["tokens", "core"]);
    expect(plan.steps).toEqual([[":compose:publishAllPublicationsToGitHubPackagesRepository", ":views:publishAllPublicationsToGitHubPackagesRepository"], [":bom:publishAllPublicationsToGitHubPackagesRepository"]]);
  });

  it("publishes only the BOM when every library is already there", async () => {
    const { probe } = registry([...LIBRARY_MODULES]);
    expect((await planPublish(probe, credentials, "0.2.0")).steps).toEqual([[":bom:publishAllPublicationsToGitHubPackagesRepository"]]);
  });

  it("does nothing when the whole version is published", async () => {
    const { probe } = registry([...LIBRARY_MODULES, BOM_MODULE]);
    expect(await planPublish(probe, credentials, "0.2.0")).toEqual({ published: [...LIBRARY_MODULES, BOM_MODULE], steps: [] });
  });

  it("probes every module, the BOM last", async () => {
    const { probe, asked } = registry([]);
    await planPublish(probe, credentials, "0.2.0");
    expect(asked).toEqual([...LIBRARY_MODULES, BOM_MODULE]);
  });

  it("stops on any answer other than published or not found, rather than publishing blind", async () => {
    const { probe } = registry([], { core: 401 });
    await expect(planPublish(probe, credentials, "0.2.0")).rejects.toThrow("returned HTTP 401");
  });
});

describe("isPublished", () => {
  it("reads 404 and 410 as not published", async () => {
    for (const status of [404, 410]) expect(await isPublished(async () => status, credentials, "core", "0.2.0")).toBe(false);
    expect(await isPublished(async () => 200, credentials, "core", "0.2.0")).toBe(true);
    await expect(isPublished(async () => 500, credentials, "core", "0.2.0")).rejects.toThrow(PublishError);
  });
});

describe("credentialsFrom", () => {
  it("needs the repository, the actor and the token", () => {
    expect(credentialsFrom({ GITHUB_REPOSITORY: "o/r", GITHUB_ACTOR: "a", GITHUB_TOKEN: "t" })).toEqual({ repository: "o/r", actor: "a", token: "t" });
    expect(() => credentialsFrom({ GITHUB_REPOSITORY: "o/r", GITHUB_ACTOR: "a" })).toThrow("GITHUB_REPOSITORY, GITHUB_ACTOR and GITHUB_TOKEN must be set");
  });
});

describe("fetchProbe", () => {
  let server: Server | undefined;

  afterEach(async () => {
    await new Promise<void>((resolve) => (server ? server.close(() => resolve()) : resolve()));
    server = undefined;
  });

  it("sends basic auth and returns the HTTP status", async () => {
    const seen: (string | undefined)[] = [];
    server = createServer((request, response) => {
      seen.push(request.headers.authorization);
      response.statusCode = request.url?.includes("/core/") ? 200 : 404;
      response.end("<project/>");
    });
    await new Promise<void>((resolve) => server?.listen(0, "127.0.0.1", resolve));
    const { port } = server.address() as AddressInfo;
    const authorization = basicAuthorization(credentials);
    expect(await fetchProbe(`http://127.0.0.1:${port}/com/ts/ds/core/0.2.0/core-0.2.0.pom`, authorization)).toBe(200);
    expect(await fetchProbe(`http://127.0.0.1:${port}/com/ts/ds/views/0.2.0/views-0.2.0.pom`, authorization)).toBe(404);
    expect(seen).toEqual([authorization, authorization]);
  });
});

describe("published modules", () => {
  it("are exactly the Android modules that apply ds.publishing", async () => {
    const settings = await readFile(path.join(repoRoot, "android/settings.gradle.kts"), "utf8");
    const modules = [...settings.matchAll(/include\(":([a-z-]+)"\)/g)].map((match) => match[1] ?? "");
    const publishing: string[] = [];
    for (const module of modules) {
      if ((await readFile(path.join(repoRoot, "android", module, "build.gradle.kts"), "utf8")).includes("ds.publishing")) publishing.push(module);
    }
    expect(publishing.sort()).toEqual([...LIBRARY_MODULES, BOM_MODULE].sort());
  });
});
