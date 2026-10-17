plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

group = "com.ts.ds.demo"

android {
    namespace = "com.ts.ds.demo.support"
    compileSdk = 36
    resourcePrefix = "demo_"

    defaultConfig {
        minSdk = 26
    }

    testOptions {
        targetSdk = 36
    }

    lint {
        targetSdk = 36
        error += "NewApi"
        disable += setOf("NewerVersionAvailable", "GradleDependency", "AndroidGradlePluginVersion")
        abortOnError = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(libs.junit4)
}
