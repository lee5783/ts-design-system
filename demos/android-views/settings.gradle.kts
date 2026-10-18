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
            version("androidx-navigation", "2.9.5")
            version("androidx-recyclerview", "1.4.0")
            version("androidx-test-runner", "1.7.0")
            library("androidx-navigation-fragment", "androidx.navigation", "navigation-fragment").versionRef("androidx-navigation")
            library("androidx-recyclerview", "androidx.recyclerview", "recyclerview").versionRef("androidx-recyclerview")
            library("espresso-contrib", "androidx.test.espresso", "espresso-contrib").versionRef("espresso")
            library("androidx-test-runner", "androidx.test", "runner").versionRef("androidx-test-runner")
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
