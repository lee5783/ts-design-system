import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val repoRoot = rootDir.resolve("../..")
val dsVersion = providers.gradleProperty("ds.version")
    .orElse(providers.fileContents(layout.projectDirectory.file("../../../VERSION")).asText.map(String::trim))
    .get()
val designVersion = (JsonSlurper().parse(repoRoot.resolve("design/manifest.json")) as Map<*, *>)
    .let { it["artifact"] as Map<*, *> }["version"].toString()

android {
    namespace = "com.tinysol.designsystem.views.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tinysol.designsystem.views.sample"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = dsVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DS_LIBRARY_VERSION", "\"$dsVersion\"")
        buildConfigField("String", "DS_DESIGN_VERSION", "\"$designVersion\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        error += "NewApi"
        disable += setOf("NewerVersionAvailable", "GradleDependency", "AndroidGradlePluginVersion")
        abortOnError = true
    }

    testOptions {
        animationsDisabled = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("com.tinysol.designsystem:bom:$dsVersion"))
    implementation("com.tinysol.designsystem:views")
    implementation("com.tinysol.designsystem.sample:support")
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.recyclerview)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
}
