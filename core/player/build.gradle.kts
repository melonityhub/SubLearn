plugins {
    id("sublearn.android.library")
}

android {
    namespace = "com.melonityhub.sublearn.core.player"
    testFixtures {
        enable = true
    }
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.settings)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}
