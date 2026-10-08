package com.melonityhub.sublearn.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** SDK levels shared by every module. minSdk 31 = Android 12 (see docs/DECISIONS.md D-004). */
internal object SublearnSdk {
    const val COMPILE_SDK = 36
    const val TARGET_SDK = 36
    const val MIN_SDK = 31
}

internal val Project.versionCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.catalogLibrary(alias: String): MinimalExternalModuleDependency =
    versionCatalog.findLibrary(alias).get().get()
