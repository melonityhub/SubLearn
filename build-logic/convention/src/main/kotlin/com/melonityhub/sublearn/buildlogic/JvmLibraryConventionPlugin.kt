package com.melonityhub.sublearn.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Pure Kotlin/JVM module: no Android APIs, so its tests run fast on the JVM. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("java-library")
        target.pluginManager.apply("org.jetbrains.kotlin.jvm")
        target.dependencies {
            add("testImplementation", target.catalogLibrary("junit"))
        }
    }
}
