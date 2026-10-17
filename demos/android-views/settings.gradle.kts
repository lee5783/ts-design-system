pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

val dsUseLocal = providers.gradleProperty("ds.useLocal").map(String::toBoolean).getOrElse(true)
val dsMavenLocal = providers.gradleProperty("ds.mavenLocal").map(String::toBoolean).getOrElse(false)
val dsGithubRepository = providers.environmentVariable("GITHUB_REPOSITORY")
    .orElse(providers.gradleProperty("ds.githubRepository"))
    .orElse("unconfigured/ts-design-system")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (!dsUseLocal) {
            if (dsMavenLocal) {
                mavenLocal {
                    content { includeGroup("com.ts.ds") }
                }
            } else {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/${dsGithubRepository.get()}")
                    credentials(PasswordCredentials::class)
                    content { includeGroup("com.ts.ds") }
                }
            }
        }
    }
    versionCatalogs {
        create("libs") {
            from(files("../../android/gradle/libs.versions.toml"))
            version("androidx-navigation", "2.9.5")
            version("androidx-recyclerview", "1.4.0")
            version("androidx-test-runner", "1.7.0")
            version("guava-android", "31.0.1-android")
            library("androidx-navigation-fragment", "androidx.navigation", "navigation-fragment").versionRef("androidx-navigation")
            library("androidx-recyclerview", "androidx.recyclerview", "recyclerview").versionRef("androidx-recyclerview")
            library("espresso-contrib", "androidx.test.espresso", "espresso-contrib").versionRef("espresso")
            library("androidx-test-runner", "androidx.test", "runner").versionRef("androidx-test-runner")
            library("guava-android", "com.google.guava", "guava").versionRef("guava-android")
            plugin("android-application", "com.android.application").versionRef("agp")
        }
    }
}

rootProject.name = "ts-design-system-demo-views"

includeBuild("../android-demo-support")
if (dsUseLocal) {
    includeBuild("../../android")
}

include(":app")
