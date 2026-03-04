/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalWasmDsl::class)

package dev.megatilus.atelier

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configures all available targets on a Kotlin Multiplatform project.
 */
@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.configureTargets(
    excludeTargets: Set<String> = emptySet()
) {
    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    macosX64()
    macosArm64()

    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    linuxArm64()
    linuxX64()

    mingwX64()

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    wasmJs {
        browser()
        nodejs()
    }

    if ("wasmWasi" !in excludeTargets) {
        wasmWasi {
            nodejs()
        }
    }
}
