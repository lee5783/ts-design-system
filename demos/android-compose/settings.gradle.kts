pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

val dsUseLocal = providers.gradleProperty("ds.useLocal").map(String::toBoolean).getOrElse(true)
val dsMavenLocal = providers.gradleProperty("ds.mavenLocal").map(String::toBoolean).getOrElse(false)

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (!dsUseLocal && dsMavenLocal) {
            mavenLocal {
                content { includeGroup("com.tinysol.designsystem") }
            }
        }
    }
    versionCatalogs {
        create("libs") {
            from(files("../../android/gradle/libs.versions.toml"))
            version("androidx-activity", "1.11.0")
            version("androidx-test-runner", "1.7.0")
            library("androidx-activity-compose", "androidx.activity", "activity-compose").versionRef("androidx-activity")
            library("androidx-test-runner", "androidx.test", "runner").versionRef("androidx-test-runner")
            plugin("android-application", "com.android.application").versionRef("agp")
        }
    }
}

rootProject.name = "ts-design-system-demo-compose"

includeBuild("../android-demo-support")
if (dsUseLocal) {
    includeBuild("../../android")
}

include(":app")
