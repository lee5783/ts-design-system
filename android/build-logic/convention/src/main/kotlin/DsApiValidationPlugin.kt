import kotlinx.validation.ApiValidationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class DsApiValidationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlinx.binary-compatibility-validator")
        target.extensions.configure<ApiValidationExtension> {
            nonPublicMarkers += DsBuildConstants.EXPERIMENTAL_API_MARKER
        }
    }
}
