import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal object DsBuildConstants {
    const val GROUP = "com.ts.ds"
    const val INCEPTION_YEAR = "2026"
    const val MIN_SDK = 26
    const val COMPILE_SDK = 36
    const val TARGET_SDK = 36
    const val JVM_TARGET = 17
    const val HOST_TEST_JVM = 21
    const val HOST_TEST_MAX_HEAP = "2g"
    const val RESOURCE_PREFIX = "ts_"
    const val EXPERIMENTAL_API_MARKER = "com.ts.ds.core.ExperimentalTSApi"
}

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalStateException("Missing library '$alias' in libs.versions.toml") }

internal fun Project.dsVersion(): String =
    rootDir.resolve("../VERSION").readText().trim()

internal fun Project.dsLicenseName(): String =
    rootDir.resolve("../LICENSE").readLines().first { it.isNotBlank() }.trim()
