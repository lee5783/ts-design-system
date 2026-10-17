import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.HostTestBuilder
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class DsAndroidTestingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("ds.android.library")
            pluginManager.apply("io.github.takahirom.roborazzi")

            val hostTestLauncher = extensions.getByType<JavaToolchainService>().launcherFor {
                languageVersion.set(JavaLanguageVersion.of(DsBuildConstants.HOST_TEST_JVM))
            }

            extensions.configure<LibraryExtension> {
                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                        all { test ->
                            test.javaLauncher.set(hostTestLauncher)
                            test.maxHeapSize = DsBuildConstants.HOST_TEST_MAX_HEAP
                            test.inputs.files(layout.projectDirectory.dir("src/test/snapshots").asFileTree)
                                .withPropertyName("snapshotReferences")
                                .withPathSensitivity(PathSensitivity.RELATIVE)
                        }
                    }
                }
            }

            extensions.configure<LibraryAndroidComponentsExtension> {
                beforeVariants(selector().withBuildType("release")) { variant ->
                    variant.hostTests.getValue(HostTestBuilder.UNIT_TEST_TYPE).enable = false
                }
            }

            dependencies {
                add("testImplementation", libs.library("junit4"))
                add("testImplementation", libs.library("robolectric"))
                add("testImplementation", libs.library("androidx-test-core"))
                add("testImplementation", libs.library("androidx-test-ext-junit"))
                add("testImplementation", libs.library("espresso-core"))
                add("testImplementation", libs.library("accessibility-test-framework"))
                add("testImplementation", libs.library("roborazzi"))
                add("testImplementation", libs.library("roborazzi-junit-rule"))
            }

            pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
                dependencies {
                    add("testImplementation", libs.library("compose-ui-test-junit4"))
                    add("testImplementation", libs.library("compose-ui-test-manifest"))
                    add("testImplementation", libs.library("roborazzi-compose"))
                }
            }
        }
    }
}
