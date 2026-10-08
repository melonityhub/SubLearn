package com.melonityhub.sublearn.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.android.application")
        target.extensions.configure<ApplicationExtension> {
            compileSdk = SublearnSdk.COMPILE_SDK
            defaultConfig {
                minSdk = SublearnSdk.MIN_SDK
                targetSdk = SublearnSdk.TARGET_SDK
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
                unitTests.isReturnDefaultValues = true
            }
            lint {
                abortOnError = true
                warningsAsErrors = false
                checkDependencies = false
            }
        }
        target.dependencies {
            add("testImplementation", target.catalogLibrary("junit"))
        }
    }
}
