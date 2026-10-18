plugins {
    alias(libs.plugins.ds.android.library)
    alias(libs.plugins.ds.android.testing)
    alias(libs.plugins.ds.publishing)
    alias(libs.plugins.ds.api.validation)
}

android {
    namespace = "com.ts.ds.views"
}

dependencies {
    api(project(":core"))
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.recyclerview)
    implementation(libs.androidx.core.ktx)
}
