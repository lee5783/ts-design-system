import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class DsAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")

            group = dsGroup()
            version = dsVersion()

            extensions.configure<LibraryExtension> {
                compileSdk = DsBuildConstants.COMPILE_SDK
                resourcePrefix = DsBuildConstants.RESOURCE_PREFIX

                defaultConfig {
                    minSdk = DsBuildConstants.MIN_SDK
                }

                testOptions {
                    targetSdk = DsBuildConstants.TARGET_SDK
                }

                lint {
                    targetSdk = DsBuildConstants.TARGET_SDK
                    error += "NewApi"
                    disable += setOf("NewerVersionAvailable", "GradleDependency", "AndroidGradlePluginVersion")
                    abortOnError = true
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                sourceSets.getByName("main") {
                    kotlin.srcDir("src/generated/kotlin")
                    res.srcDir("src/generated/res")
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                explicitApi()
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(DsBuildConstants.JVM_TARGET.toString()))
                }
            }
        }
    }
}
