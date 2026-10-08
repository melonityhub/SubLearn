plugins {
    `kotlin-dsl`
}

group = "com.melonityhub.sublearn.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "sublearn.android.application"
            implementationClass = "com.melonityhub.sublearn.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "sublearn.android.library"
            implementationClass = "com.melonityhub.sublearn.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("jvmLibrary") {
            id = "sublearn.jvm.library"
            implementationClass = "com.melonityhub.sublearn.buildlogic.JvmLibraryConventionPlugin"
        }
    }
}
