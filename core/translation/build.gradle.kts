plugins {
    id("sublearn.android.library")
}

android {
    namespace = "com.melonityhub.sublearn.core.translation"
}

dependencies {
    implementation(libs.mlkit.translate)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
}
