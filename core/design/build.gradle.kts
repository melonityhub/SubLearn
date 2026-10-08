plugins {
    id("sublearn.android.library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.melonityhub.sublearn.core.design"
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(projects.core.settings)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.robolectric)
}
