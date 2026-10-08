plugins {
    id("sublearn.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.melonityhub.sublearn.core.data"
}

dependencies {
    api(project(":core:settings"))
    api(project(":core:model"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}
