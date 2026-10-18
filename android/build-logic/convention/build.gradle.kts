import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.tinysol.designsystem.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.vanniktech.publish.gradle.plugin)
    compileOnly(libs.binary.compatibility.validator.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("dsAndroidLibrary") {
            id = libs.plugins.ds.android.library.get().pluginId
            implementationClass = "DsAndroidLibraryPlugin"
        }
        register("dsAndroidCompose") {
            id = libs.plugins.ds.android.compose.get().pluginId
            implementationClass = "DsAndroidComposePlugin"
        }
        register("dsAndroidTesting") {
            id = libs.plugins.ds.android.testing.get().pluginId
            implementationClass = "DsAndroidTestingPlugin"
        }
        register("dsPublishing") {
            id = libs.plugins.ds.publishing.get().pluginId
            implementationClass = "DsPublishingPlugin"
        }
        register("dsApiValidation") {
            id = libs.plugins.ds.api.validation.get().pluginId
            implementationClass = "DsApiValidationPlugin"
        }
    }
}
