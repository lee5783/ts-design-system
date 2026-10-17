plugins {
    alias(libs.plugins.ds.android.library)
    alias(libs.plugins.ds.android.testing)
    alias(libs.plugins.ds.publishing)
    alias(libs.plugins.ds.api.validation)
}

android {
    namespace = "com.ts.ds.core"
}

dependencies {
    api(project(":tokens"))
}
