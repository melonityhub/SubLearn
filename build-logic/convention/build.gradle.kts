plugins {
    `kotlin-dsl`
}

group = "com.melonityhub.sublearn.buildlogic"

dependencies {
    // implementation (not compileOnly): the convention plugins apply these plugins by id at runtime.
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
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
