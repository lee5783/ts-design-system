plugins {
    alias(libs.plugins.ds.android.library)
    alias(libs.plugins.ds.android.compose)
    alias(libs.plugins.ds.android.testing)
}

android {
    namespace = "com.ts.ds.testsupport"
}

dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.ui.test.manifest)
    api(libs.junit4)
    api(libs.robolectric)
    api(libs.androidx.test.core)
    api(libs.roborazzi)
    api(libs.roborazzi.compose)
    api(libs.roborazzi.junit.rule)
    api(libs.compose.ui.test.junit4)
}
