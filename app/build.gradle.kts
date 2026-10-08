plugins {
    id("sublearn.android.application")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.melonityhub.sublearn"

    defaultConfig {
        applicationId = "com.melonityhub.sublearn"
        // CI sets GITHUB_RUN_NUMBER so every build installs over the previous one (monotonic version codes).
        versionCode = providers.environmentVariable("GITHUB_RUN_NUMBER").map { it.toInt() }.orElse(1).get()
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // No release keystore is stored in the repo (D-025). Release builds use the debug signing key.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:settings"))
    implementation(project(":core:subtitle"))
    implementation(project(":core:later"))
    implementation(project(":core:data"))
    implementation(project(":core:design"))
    implementation(project(":core:player"))
    implementation(project(":core:translation"))
    implementation(project(":feature:player"))
    implementation(project(":feature:home"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:words"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    debugImplementation(libs.compose.ui.tooling)
}
