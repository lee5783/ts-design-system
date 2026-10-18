import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val repoRoot = rootDir.resolve("../..")
val dsVersion = providers.gradleProperty("ds.version")
    .orElse(providers.fileContents(layout.projectDirectory.file("../../../VERSION")).asText.map(String::trim))
    .get()
val designVersion = (JsonSlurper().parse(repoRoot.resolve("design/manifest.json")) as Map<*, *>)
    .let { it["artifact"] as Map<*, *> }["version"].toString()

android {
    namespace = "com.ts.ds.demo.compose"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ts.ds.demo.compose"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = dsVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DS_LIBRARY_VERSION", "\"$dsVersion\"")
        buildConfigField("String", "DS_DESIGN_VERSION", "\"$designVersion\"")
    }

    buildFeatures {
        compose = true
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
    implementation(platform("com.ts.ds:bom:$dsVersion"))
    implementation("com.ts.ds:compose")
    implementation("com.ts.ds.demo:support")
    implementation(libs.androidx.activity.compose)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.guava.android)
    debugImplementation(platform(libs.compose.bom))
    debugImplementation(libs.compose.ui.test.manifest)
}
