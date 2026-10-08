plugins {
    id("sublearn.android.library")
}

android {
    namespace = "com.melonityhub.sublearn.core.player"
    testFixtures {
        enable = true
    }
}

lint {
    // Media3 codec and renderer APIs are @UnstableApi. They are used on purpose (decoder modes, see DECISIONS D-012).
    warning.add("UnsafeOptInUsageError")
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:settings"))
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    // Test fixtures do not inherit the main source set's implementation dependencies.
    testFixturesImplementation(project(":core:settings"))
    testFixturesImplementation(libs.kotlinx.coroutines.core)
    testFixturesImplementation(libs.androidx.media3.common)
}
