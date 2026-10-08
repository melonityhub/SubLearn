plugins {
    id("sublearn.android.library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.melonityhub.sublearn.feature.player"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:settings"))
    implementation(project(":core:subtitle"))
    implementation(project(":core:design"))
    implementation(project(":core:data"))
    implementation(project(":core:player"))
    implementation(project(":core:translation"))
    implementation(project(":core:later"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.koin.androidx.compose)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":core:player"))
    testImplementation(testFixtures(project(":core:player")))
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
