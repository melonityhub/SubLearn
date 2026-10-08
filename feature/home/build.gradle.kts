plugins {
    id("sublearn.android.library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.melonityhub.sublearn.feature.home"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:design"))
    implementation(project(":core:settings"))
    implementation(project(":core:model"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.koin.androidx.compose)
    debugImplementation(libs.compose.ui.tooling)
}
