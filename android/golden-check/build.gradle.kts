plugins {
    alias(libs.plugins.ds.android.library)
    alias(libs.plugins.ds.android.compose)
}

android {
    namespace = "com.tinysol.designsystem.goldencheck"

    sourceSets.getByName("main") {
        kotlin.srcDir("../../tools/codegen/test/fixtures/expected/compose/tokens")
        kotlin.srcDir("../../tools/codegen/test/fixtures/expected/compose/compose")
    }
}

dependencies {
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
}
